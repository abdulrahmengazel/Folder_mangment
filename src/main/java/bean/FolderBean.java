package bean;

import entity.Files;
import entity.Folders;
import entity.Users;
import facadeLocal.FileFacadeLocal;
import facadeLocal.FolderFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named("folderBean")
@ViewScoped
public class FolderBean implements Serializable {

    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";
    private Folders folder;
    private List<Folders> foldersList;
    private Long parentFolderId;
    
    @EJB
    private FolderFacadeLocal folderFacade;
    @EJB
    private FileFacadeLocal fileFacade;

    public void clearForm() {
        folder = new Folders();
        parentFolderId = null;
    }

    public String createFolder() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            getFolder().setOwner(currentUser);
            
            // Set parent folder if provided
            if (parentFolderId != null) {
                Folders parentFolder = folderFacade.find(parentFolderId);
                if (parentFolder != null && parentFolder.getOwner().getId().equals(currentUser.getId())) {
                    getFolder().setParentFolder(parentFolder);
                } else {
                    context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error", "Invalid parent folder."));
                    return null;
                }
            }

            // 1. Save to DB first to generate the Folder ID
            folderFacade.create(folder);

            // 2. Create the physical path on the system for each user and folder
            try {
                String userDirectory = "user_" + currentUser.getId();
                String folderDirectory = "folder_" + getFolder().getId();

                java.nio.file.Path physicalPath = java.nio.file.Paths.get(ROOT_UPLOAD_DIR, userDirectory, folderDirectory);

                if (!java.nio.file.Files.exists(physicalPath)) {
                    java.nio.file.Files.createDirectories(physicalPath);
                    System.out.println("Physical folder created: " + physicalPath);
                }
            } catch (Exception e) {
                System.out.println("Error creating physical folder: " + e.getMessage());
            }

            System.out.println("Folder successfully created in DB and filesystem");
            
            String redirectUrl = parentFolderId != null ? "folder-content.xhtml?faces-redirect=true&folderId=" + parentFolderId : "dashboard.xhtml?faces-redirect=true";
            clearForm();

            return redirectUrl;
        } else {
            System.out.println("Error: Logged in user not found.");
            return null;
        }
    }

    public void deleteFolder(Folders f) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && f != null && f.getOwner().getId().equals(currentUser.getId())) {
            softDeleteFolderTree(f, currentUser.getId());
            System.out.println("Folder deleted");
            foldersList = null; // force reload
        }
    }

    private void softDeleteFolderTree(Folders folderToDelete, Long ownerId) {
        if (folderToDelete == null || folderToDelete.isDeleted()) {
            return;
        }

        folderToDelete.setDeleted(true);
        folderFacade.edit(folderToDelete);

        List<Files> filesToDelete = fileFacade.findAll().stream()
                .filter(file -> file.getOwner().getId().equals(ownerId))
                .filter(file -> file.getFolder() != null && file.getFolder().getId().equals(folderToDelete.getId()))
                .toList();

        for (Files file : filesToDelete) {
            file.setDeleted(true);
            fileFacade.edit(file);
        }

        List<Folders> childFolders = folderFacade.findAll().stream()
                .filter(folder -> folder.getOwner().getId().equals(ownerId))
                .filter(folder -> folder.getParentFolder() != null && folder.getParentFolder().getId().equals(folderToDelete.getId()))
                .toList();

        for (Folders childFolder : childFolders) {
            softDeleteFolderTree(childFolder, ownerId);
        }
    }

    public Folders getFolder() {
        if (folder == null) {
            folder = new Folders();
        }
        return folder;
    }

    public void setFolder(Folders folder) {
        this.folder = folder;
    }

    public List<Folders> getFoldersList() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            // Only fetch root folders (parent is null) for the dashboard
            foldersList = folderFacade.findAll().stream()
                    .filter(f -> f.getOwner().getId().equals(currentUser.getId()))
                    .filter(f -> !f.isDeleted())
                    .filter(f -> f.getParentFolder() == null)
                    .toList();
        } else {
            foldersList = java.util.Collections.emptyList();
        }
        return foldersList;
    }

    public void setFoldersList(List<Folders> foldersList) {
        this.foldersList = foldersList;
    }
    
    public Long getParentFolderId() {
        return parentFolderId;
    }

    public void setParentFolderId(Long parentFolderId) {
        this.parentFolderId = parentFolderId;
    }
}