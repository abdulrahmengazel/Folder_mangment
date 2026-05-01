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

    // التعديل الأول: نستقبل رقم المجلد بدلاً من الكائن الكامل
    private Long targetFolderId;

    @EJB
    private FileFacadeLocal fileFacade;

    // التعديل الثاني: نحتاج طبقة المجلدات للبحث عن المجلد برقمه
    @EJB
    private FolderFacadeLocal folderFacade;

    private static final String ROOT_UPLOAD_DIR = "/home/your_username/cloud_uploads";

    public void clearForm() {
        fileEntity = new Files();
        uploadedFile = null;
    }

    public void uploadFile() {
        // نتحقق من أن المستخدم اختار ملفاً ورقم مجلد صالح
        if (uploadedFile != null && targetFolderId != null) {

            FacesContext context = FacesContext.getCurrentInstance();
            Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

            if (currentUser != null) {
                // التعديل الثالث: نجلب المجلد الحقيقي من قاعدة البيانات باستخدام الرقم
                Folders actualTargetFolder = folderFacade.find(targetFolderId);

                if (actualTargetFolder != null) {
                    try {
                        String originalFileName = Paths.get(uploadedFile.getSubmittedFileName()).getFileName().toString();
                        String uniqueFileName = System.currentTimeMillis() + "_" + originalFileName;

                        // نستخدم المجلد الحقيقي الذي جلبناه لبناء المسار
                        String userDirectory = "user_" + currentUser.getId();
                        String folderDirectory = "folder_" + actualTargetFolder.getId();
                        Path dynamicFolderPath = Paths.get(ROOT_UPLOAD_DIR, userDirectory, folderDirectory);

                        if (!java.nio.file.Files.exists(dynamicFolderPath)) {
                            java.nio.file.Files.createDirectories(dynamicFolderPath);
                        }

                        Path finalPath = dynamicFolderPath.resolve(uniqueFileName);

                        try (InputStream input = uploadedFile.getInputStream()) {
                            java.nio.file.Files.copy(input, finalPath, StandardCopyOption.REPLACE_EXISTING);
                        }

                        getFileEntity().setName(originalFileName);
                        getFileEntity().setPath(finalPath.toString());
                        getFileEntity().setType(uploadedFile.getContentType());
                        getFileEntity().setSize(uploadedFile.getSize());

                        // نربط الملف بالمجلد الحقيقي
                        getFileEntity().setFolder(actualTargetFolder);
                        getFileEntity().setOwner(currentUser);

                        fileFacade.create(fileEntity);

                        System.out.println("File uploaded successfully to: " + finalPath.toString());
                        clearForm();

                    } catch (Exception e) {
                        System.out.println("Error during file upload: " + e.getMessage());
                    }
                }
            } else {
                System.out.println("Error: User session expired.");
            }
        } else {
            System.out.println("Error: File or Folder ID is missing.");
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