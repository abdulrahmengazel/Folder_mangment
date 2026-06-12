package bean;

import entity.Files;
import entity.Folders;
import entity.Users;
import facadeLocal.FileFacadeLocal;
import facadeLocal.FolderFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

@Named("fileBean")
@ViewScoped
public class FileBean implements Serializable {

    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";
    private Files fileEntity;
    private Part uploadedFile;
    private Long targetFolderId;
    private Folders targetFolder;
    
    @EJB
    private FileFacadeLocal fileFacade;
    @EJB
    private FolderFacadeLocal folderFacade;

    public void clearForm() {
        fileEntity = new Files();
        uploadedFile = null;
    }

    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null || targetFolderId == null) {
            targetFolder = null;
            return;
        }

        targetFolder = folderFacade.find(targetFolderId);

        if (targetFolder == null || targetFolder.isDeleted() || !targetFolder.getOwner().getId().equals(currentUser.getId())) {
            targetFolder = null;
            targetFolderId = null;
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Target folder could not be determined."));
        }
    }

    public String uploadFile() {
        FacesContext context = FacesContext.getCurrentInstance();

        // Check if file is provided
        if (uploadedFile == null) {
            context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error", "File could not be received."));
            return null;
        }

        // Check if target folder ID is provided
        if (targetFolderId == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Target folder could not be determined."));
            return null;
        }

        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            Folders actualTargetFolder = targetFolder != null ? targetFolder : folderFacade.find(targetFolderId);

            if (actualTargetFolder != null && actualTargetFolder.getOwner().getId().equals(currentUser.getId())) {
                try {
                    String originalFileName = java.nio.file.Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString();
                    String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;
                    String storedType = resolveStoredType(uploadedFile.getContentType(), originalFileName);

                    java.nio.file.Path dynamicFolderPath = FolderStorageHelper.resolveFolderPath(ROOT_UPLOAD_DIR, actualTargetFolder);
                    if (dynamicFolderPath == null) {
                        context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Target folder path could not be determined."));
                        return null;
                    }

                    if (!java.nio.file.Files.exists(dynamicFolderPath)) {
                        java.nio.file.Files.createDirectories(dynamicFolderPath);
                    }

                    java.nio.file.Path finalPath = dynamicFolderPath.resolve(uniqueFileName);

                    try (InputStream input = uploadedFile.getInputStream()) {
                        java.nio.file.Files.copy(input, finalPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }

                    getFileEntity().setName(originalFileName);
                    getFileEntity().setPath(finalPath.toString());
                    getFileEntity().setType(storedType);
                    getFileEntity().setSize(uploadedFile.getSize());

                    getFileEntity().setFolder(actualTargetFolder);
                    getFileEntity().setOwner(currentUser);

                    fileFacade.create(fileEntity);

                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "File uploaded successfully!"));
                    clearForm();

                    return "dashboard.xhtml?faces-redirect=true";

                } catch (Exception e) {
                    context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error", e.getMessage()));
                }
            } else {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Access to the folder is denied."));
            }
        }
        return null;
    }

    public void toggleStar(Files file) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && file != null && file.getOwner().getId().equals(currentUser.getId()) && !file.isDeleted()) {
            file.setStarred(!file.isStarred());
            fileFacade.edit(file);
        }
    }

    // --- Getters and Setters ---

    public Long getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(Long targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    public Folders getTargetFolder() {
        return targetFolder;
    }

    public void setTargetFolder(Folders targetFolder) {
        this.targetFolder = targetFolder;
    }

    public Files getFileEntity() {
        if (fileEntity == null) {
            fileEntity = new Files();
        }
        return fileEntity;
    }

    public List<Files> getStarredFiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            return fileFacade.findStarredFiles(currentUser.getId());
        }

        return java.util.Collections.emptyList();
    }

    public List<Files> getRecentFiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            return fileFacade.findRecentFiles(currentUser.getId());
        }

        return java.util.Collections.emptyList();
    }

    public Part getUploadedFile() {
        return uploadedFile;
    }

    public void setUploadedFile(Part uploadedFile) {
        this.uploadedFile = uploadedFile;
    }

    private String resolveStoredType(String contentType, String originalFileName) {
        if (contentType != null && contentType.length() <= 50) {
            return contentType;
        }

        String fileName = originalFileName == null ? "" : originalFileName;
        int lastDotIndex = fileName.lastIndexOf('.');
        if (lastDotIndex > -1 && lastDotIndex < fileName.length() - 1) {
            String extension = fileName.substring(lastDotIndex + 1).toLowerCase();
            if (!extension.isBlank() && extension.length() <= 50) {
                return extension;
            }
        }

        if (contentType != null && !contentType.isBlank()) {
            int slashIndex = contentType.indexOf('/');
            String subtype = slashIndex >= 0 && slashIndex < contentType.length() - 1
                    ? contentType.substring(slashIndex + 1)
                    : contentType;
            if (subtype.length() <= 50) {
                return subtype;
            }
            return subtype.substring(0, 50);
        }

        return "unknown";
    }
}
