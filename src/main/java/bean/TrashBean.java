package bean;

import entity.Files;
import entity.Folders;
import entity.SharedFiles;
import entity.Users;
import facadeLocal.FileFacadeLocal;
import facadeLocal.FolderFacadeLocal;
import facadeLocal.SharedFilesFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.IOException;
import java.io.Serializable;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class TrashBean implements Serializable {

    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";

    @EJB
    private FileFacadeLocal fileFacade;

    @EJB
    private FolderFacadeLocal folderFacade;

    @EJB
    private SharedFilesFacadeLocal sharedFilesFacade;

    private List<Files> deletedFiles;
    private List<Folders> deletedFolders;

    public List<Files> getDeletedFiles() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            deletedFiles = new ArrayList<>();
            return deletedFiles;
        }

        deletedFiles = fileFacade.findDeleted().stream()
                .filter(file -> file.getOwner().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        return deletedFiles;
    }

    public List<Folders> getDeletedFolders() {
        Users currentUser = getCurrentUser();
        if (currentUser == null) {
            deletedFolders = new ArrayList<>();
            return deletedFolders;
        }

        deletedFolders = folderFacade.findDeleted().stream()
                .filter(folder -> folder.getOwner().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());
        return deletedFolders;
    }

    public void restoreFile(Files file) {
        Users currentUser = getCurrentUser();

        if (currentUser == null || file == null || !file.getOwner().getId().equals(currentUser.getId())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "You do not have permission to restore this file.");
            return;
        }

        file.setDeleted(false);
        fileFacade.edit(file);
        refreshLists();
        addMessage(FacesMessage.SEVERITY_INFO, "Success", "File restored successfully.");
    }

    public void restoreFolder(Folders folder) {
        Users currentUser = getCurrentUser();

        if (currentUser == null || folder == null || !folder.getOwner().getId().equals(currentUser.getId())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "You do not have permission to restore this folder.");
            return;
        }

        restoreFolderTree(folder, currentUser.getId());
        refreshLists();
        addMessage(FacesMessage.SEVERITY_INFO, "Success", "Folder restored successfully.");
    }

    public void permanentlyDeleteFile(Files file) {
        Users currentUser = getCurrentUser();

        if (currentUser == null || file == null || !file.getOwner().getId().equals(currentUser.getId())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "You do not have permission to delete this file permanently.");
            return;
        }

        deletePhysicalFile(file.getPath());
        removeSharesForFile(file.getId());
        fileFacade.remove(file);
        refreshLists();
        addMessage(FacesMessage.SEVERITY_INFO, "Success", "File deleted permanently.");
    }

    public void permanentlyDeleteFolder(Folders folder) {
        Users currentUser = getCurrentUser();

        if (currentUser == null || folder == null || !folder.getOwner().getId().equals(currentUser.getId())) {
            addMessage(FacesMessage.SEVERITY_ERROR, "Error", "You do not have permission to delete this folder permanently.");
            return;
        }

        permanentlyDeleteFolderTree(folder, currentUser.getId());
        refreshLists();
        addMessage(FacesMessage.SEVERITY_INFO, "Success", "Folder deleted permanently.");
    }

    private void restoreFolderTree(Folders folder, Long ownerId) {
        if (folder == null || !folder.isDeleted()) {
            return;
        }

        folder.setDeleted(false);
        folderFacade.edit(folder);

        List<Files> folderFiles = fileFacade.findDeleted().stream()
                .filter(file -> file.getOwner().getId().equals(ownerId))
                .filter(file -> file.getFolder() != null && file.getFolder().getId().equals(folder.getId()))
                .collect(Collectors.toList());

        for (Files file : folderFiles) {
            file.setDeleted(false);
            fileFacade.edit(file);
        }

        List<Folders> childFolders = folderFacade.findDeleted().stream()
                .filter(child -> child.getOwner().getId().equals(ownerId))
                .filter(child -> child.getParentFolder() != null && child.getParentFolder().getId().equals(folder.getId()))
                .collect(Collectors.toList());

        for (Folders childFolder : childFolders) {
            restoreFolderTree(childFolder, ownerId);
        }
    }

    private void permanentlyDeleteFolderTree(Folders folder, Long ownerId) {
        if (folder == null) {
            return;
        }

        List<Folders> childFolders = folderFacade.findDeleted().stream()
                .filter(child -> child.getOwner().getId().equals(ownerId))
                .filter(child -> child.getParentFolder() != null && child.getParentFolder().getId().equals(folder.getId()))
                .collect(Collectors.toList());
        for (Folders childFolder : childFolders) {
            permanentlyDeleteFolderTree(childFolder, ownerId);
        }

        List<Files> folderFiles = fileFacade.findDeleted().stream()
                .filter(file -> file.getOwner().getId().equals(ownerId))
                .filter(file -> file.getFolder() != null && file.getFolder().getId().equals(folder.getId()))
                .collect(Collectors.toList());
        for (Files file : folderFiles) {
            permanentlyDeleteFile(file);
        }

        deletePhysicalFolder(folder);
        folderFacade.remove(folder);
    }

    private void removeSharesForFile(Long fileId) {
        if (fileId == null) {
            return;
        }

        List<SharedFiles> shares = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getFile() != null && sf.getFile().getId().equals(fileId))
                .collect(Collectors.toList());

        for (SharedFiles share : shares) {
            sharedFilesFacade.remove(share);
        }
    }

    private void deletePhysicalFile(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return;
        }

        try {
            java.nio.file.Files.deleteIfExists(Path.of(filePath));
        } catch (IOException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Could not delete file from storage: " + e.getMessage());
        }
    }

    private void deletePhysicalFolder(Folders folder) {
        if (folder == null || folder.getOwner() == null || folder.getId() == null) {
            return;
        }

        Path folderPath = Path.of(ROOT_UPLOAD_DIR, "user_" + folder.getOwner().getId(), "folder_" + folder.getId());
        try {
            if (java.nio.file.Files.exists(folderPath)) {
                try (java.util.stream.Stream<Path> paths = java.nio.file.Files.walk(folderPath)) {
                    paths.sorted(java.util.Comparator.reverseOrder()).forEach(path -> {
                        try {
                            java.nio.file.Files.deleteIfExists(path);
                        } catch (IOException ignored) {
                            // Best-effort cleanup; database removal still proceeds.
                        }
                    });
                }
            }
        } catch (IOException e) {
            addMessage(FacesMessage.SEVERITY_WARN, "Warning", "Could not delete folder from storage: " + e.getMessage());
        }
    }

    private Users getCurrentUser() {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context == null || context.getExternalContext() == null) {
            return null;
        }
        return (Users) context.getExternalContext().getSessionMap().get("user");
    }

    private void refreshLists() {
        deletedFiles = null;
        deletedFolders = null;
    }

    private void addMessage(FacesMessage.Severity severity, String summary, String detail) {
        FacesContext context = FacesContext.getCurrentInstance();
        if (context != null) {
            context.addMessage(null, new FacesMessage(severity, summary, detail));
        }
    }

}

