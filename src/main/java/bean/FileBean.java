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
import jakarta.servlet.http.Part;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class FileBean implements Serializable {

    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";
    private Files fileEntity;
    private Part uploadedFile;
    private Long targetFolderId;
    
    @EJB
    private FileFacadeLocal fileFacade;
    @EJB
    private FolderFacadeLocal folderFacade;

    public void clearForm() {
        fileEntity = new Files();
        uploadedFile = null;
    }

    public String uploadFile() {
        FacesContext context = FacesContext.getCurrentInstance();

        // التحقق من وصول الملف
        if (uploadedFile == null) {
            context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Hata", "Dosya alınamadı."));
            return null;
        }

        // التحقق من وصول رقم المجلد
        if (targetFolderId == null) {
            context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Hata", "Hedef klasör belirlenemedi."));
            return null;
        }

        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            Folders actualTargetFolder = folderFacade.find(targetFolderId);

            if (actualTargetFolder != null && actualTargetFolder.getOwner().getId().equals(currentUser.getId())) {
                try {
                    String originalFileName = java.nio.file.Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString();
                    String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

                    String userDirectory = "user_" + currentUser.getId();
                    String folderDirectory = "folder_" + actualTargetFolder.getId();
                    java.nio.file.Path dynamicFolderPath = java.nio.file.Paths.get(ROOT_UPLOAD_DIR, userDirectory, folderDirectory);

                    if (!java.nio.file.Files.exists(dynamicFolderPath)) {
                        java.nio.file.Files.createDirectories(dynamicFolderPath);
                    }

                    java.nio.file.Path finalPath = dynamicFolderPath.resolve(uniqueFileName);

                    try (InputStream input = uploadedFile.getInputStream()) {
                        java.nio.file.Files.copy(input, finalPath, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
                    }

                    getFileEntity().setName(originalFileName);
                    getFileEntity().setPath(finalPath.toString());
                    getFileEntity().setType(uploadedFile.getContentType());
                    getFileEntity().setSize(uploadedFile.getSize());

                    getFileEntity().setFolder(actualTargetFolder);
                    getFileEntity().setOwner(currentUser);

                    fileFacade.create(fileEntity);

                    context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Başarılı", "Dosya başarıyla yüklendi!"));
                    clearForm();

                    return "dashboard.xhtml?faces-redirect=true";

                } catch (Exception e) {
                    context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Sistem hatası", e.getMessage()));
                }
            } else {
                context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Hata", "Klasöre erişim reddedildi."));
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
}