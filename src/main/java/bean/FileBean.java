package bean;

import entity.Files;
import entity.Folders;
import entity.Users;
import facadeLocal.FileFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import jakarta.servlet.http.Part;

import java.io.InputStream;
import java.io.Serializable;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

@Named
@ViewScoped
public class FileBean implements Serializable {

    private Files fileEntity;
    private List<Files> filesList;
    private Part uploadedFile;
    private Folders targetFolder;

    @EJB
    private FileFacadeLocal fileFacade;

    // المسار الجذري على نظام Linux الخاص بك
    private static final String ROOT_UPLOAD_DIR = "/home/your_username/cloud_uploads";

    public void clearForm() {
        fileEntity = new Files();
        uploadedFile = null;
    }

    public void uploadFile() {
        if (uploadedFile != null && targetFolder != null) {

            // استخراج المستخدم المسجل من الجلسة
            FacesContext context = FacesContext.getCurrentInstance();
            Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

            if (currentUser != null) {
                try {
                    // 1. استخراج وتأمين اسم الملف
                    String originalFileName = Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString();
                    String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

                    // 2. بناء المسار الهرمي المنظم (مستخدم -> مجلد)
                    String userDirectory = "user_" + currentUser.getId();
                    String folderDirectory = "folder_" + targetFolder.getId();
                    Path dynamicFolderPath = Paths.get(ROOT_UPLOAD_DIR, userDirectory, folderDirectory);

                    // 3. التحقق من وجود المجلدات الفيزيائية وإنشاؤها
                    if (!java.nio.file.Files.exists(dynamicFolderPath)) {
                        java.nio.file.Files.createDirectories(dynamicFolderPath);
                    }

                    // 4. تحديد المسار النهائي وبدء عملية النسخ
                    Path finalPath = dynamicFolderPath.resolve(uniqueFileName);

                    try (InputStream input = uploadedFile.getInputStream()) {
                        java.nio.file.Files.copy(input, finalPath, StandardCopyOption.REPLACE_EXISTING);
                    }

                    // 5. حفظ بيانات الملف في قاعدة البيانات
                    getFileEntity().setName(originalFileName);
                    getFileEntity().setPath(finalPath.toString());
                    getFileEntity().setType(uploadedFile.getContentType());
                    getFileEntity().setSize(uploadedFile.getSize());

                    getFileEntity().setFolder(targetFolder);
                    getFileEntity().setOwner(currentUser);

                    fileFacade.create(fileEntity);

                    System.out.println("File uploaded successfully to: " + finalPath.toString());
                    clearForm();

                } catch (Exception e) {
                    System.out.println("Error during file upload: " + e.getMessage());
                }
            } else {
                System.out.println("Error: User session expired or not logged in.");
            }
        } else {
            System.out.println("Error: Uploaded file or target folder is null.");
        }
    }

    public void deleteFile(Files f) {
        // سيتم برمجة الحذف الفيزيائي لاحقاً، حالياً نحذف من القاعدة فقط
        fileFacade.remove(f);
        System.out.println("File deleted from database");
    }

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
        filesList = fileFacade.findAll();
        return filesList;
    }

    public void setFilesList(List<Files> filesList) {
        this.filesList = filesList;
    }

    public Part getUploadedFile() { return uploadedFile; }
    public void setUploadedFile(Part uploadedFile) { this.uploadedFile = uploadedFile; }

    public Folders getTargetFolder() { return targetFolder; }
    public void setTargetFolder(Folders targetFolder) { this.targetFolder = targetFolder; }
}