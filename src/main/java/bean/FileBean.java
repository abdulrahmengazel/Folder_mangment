package bean;

import entity.Files;
import entity.Folders;
import entity.Users;
import facadeLocal.FileFacadeLocal;
import facadeLocal.FolderFacadeLocal; // أضفنا هذه المكتبة
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.InputStream;
import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class FileBean implements Serializable {

    private Files fileEntity;
    private List<Files> filesList;
    private Part uploadedFile;
    private Folders targetFolder;

    // التعديل الأول: نستقبل رقم المجلد بدلاً من الكائن الكامل
    private Long targetFolderId;

    @EJB
    private FileFacadeLocal fileFacade;

    // التعديل الثاني: نحتاج طبقة المجلدات للبحث عن المجلد برقمه
    @EJB
    private FolderFacadeLocal folderFacade;

    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";

    public void clearForm() {
        fileEntity = new Files();
        uploadedFile = null;
    }

    public String uploadFile() {
        FacesContext context = FacesContext.getCurrentInstance();

        // التحقق من وصول الملف
        if (uploadedFile == null) {
            context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error", "No file received."));
            return null;
        }

        // التحقق من وصول رقم المجلد
        if (targetFolderId == null) {
            context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error", "Target folder not identified."));
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

                    context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Success", "File uploaded successfully!"));
                    clearForm();
                    
                    return "dashboard.xhtml?faces-redirect=true";

                } catch (Exception e) {
                    context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "System Error", e.getMessage()));
                }
            } else {
                context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error", "Access denied to folder."));
            }
        }
        return null;
    }

    public void deleteFile(Files file) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && file != null && file.getOwner().getId().equals(currentUser.getId())) {
            file.setDeleted(true);
            fileFacade.edit(file);
            filesList = null; // force reload
            context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_INFO, "Success", "File deleted."));
        } else {
             context.addMessage(null, new jakarta.faces.application.FacesMessage(jakarta.faces.application.FacesMessage.SEVERITY_ERROR, "Error", "You do not have permission to delete this file."));
        }
    }

    public void toggleStar(Files file) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && file != null && file.getOwner().getId().equals(currentUser.getId()) && !file.isDeleted()) {
            file.setStarred(!file.isStarred());
            fileFacade.edit(file);
            filesList = null;
        }
    }


    // دوال الجلب والتعيين للمتغير الجديد
    public Long getTargetFolderId() {
        return targetFolderId;
    }

    public void setTargetFolderId(Long targetFolderId) {
        this.targetFolderId = targetFolderId;
    }

    // دوال الجلب والتعيين القديمة (اقتطعتها للاختصار، أبقها كما هي في كودك)
    // getFileEntity, setFileEntity, getFilesList, setFilesList, getUploadedFile, setUploadedFile


    // --- Getters and Setters ---

    public Files getFileEntity() {
        if (fileEntity == null) {
            fileEntity = new Files();
        }
        return fileEntity;
    }

    public void setFileEntity(Files fileEntity) {
        this.fileEntity = fileEntity;
    }

    public List<Files> getFilesList() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
             filesList = fileFacade.findAll().stream()
                    .filter(f -> f.getOwner().getId().equals(currentUser.getId()))
                    .filter(f -> !f.isDeleted())
                    .collect(Collectors.toList());
        } else {
             filesList = java.util.Collections.emptyList();
        }
        return filesList;
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

    public void setFilesList(List<Files> filesList) {
        this.filesList = filesList;
    }

    public Part getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(Part uploadedFile) { this.uploadedFile = uploadedFile; }

    public Folders getTargetFolder() { return targetFolder; }
    public void setTargetFolder(Folders targetFolder) { this.targetFolder = targetFolder; }
}