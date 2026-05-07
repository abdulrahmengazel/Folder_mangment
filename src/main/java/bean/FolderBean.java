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

@Named
@ViewScoped
public class FolderBean implements Serializable {

    // يجب إضافة هذا المسار الثابت في أعلى الكلاس إذا لم يكن موجوداً
    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";
    private Folders folder;
    private List<Folders> foldersList;
    @EJB
    private FolderFacadeLocal folderFacade;
    @EJB
    private FileFacadeLocal fileFacade;

    public void clearForm() {
        folder = new Folders();
    }

    public String createFolder() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            getFolder().setOwner(currentUser);

            // 1. الحفظ في قاعدة البيانات أولاً لكي يتم توليد المعرف (ID) للمجلد
            folderFacade.create(folder);

            // 2. إنشاء المسار الفيزيائي على نظام لينكس لكل مستخدم ومجلد
            try {
                String userDirectory = "user_" + currentUser.getId();
                String folderDirectory = "folder_" + getFolder().getId();

                java.nio.file.Path physicalPath = java.nio.file.Paths.get(ROOT_UPLOAD_DIR, userDirectory, folderDirectory);

                if (!java.nio.file.Files.exists(physicalPath)) {
                    java.nio.file.Files.createDirectories(physicalPath);
                    System.out.println("Fiziksel klasör oluşturuldu: " + physicalPath);
                }
            } catch (Exception e) {
                System.out.println("Fiziksel klasör oluşturulurken hata oluştu: " + e.getMessage());
            }

            System.out.println("Klasör veritabanında ve dosya sisteminde başarıyla oluşturuldu");
            clearForm();

            return "dashboard.xhtml?faces-redirect=true";
        } else {
            System.out.println("Hata: Oturum açmış kullanıcı bulunamadı.");
            return null;
        }
    }

    public void deleteFolder(Folders f) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && f != null && f.getOwner().getId().equals(currentUser.getId())) {
            softDeleteFolderTree(f, currentUser.getId());
            System.out.println("Klasör silindi");
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
            foldersList = folderFacade.findAll().stream()
                    .filter(f -> f.getOwner().getId().equals(currentUser.getId()))
                    .filter(f -> !f.isDeleted())
                    .toList();
        } else {
            foldersList = java.util.Collections.emptyList();
        }
        return foldersList;
    }

    public void setFoldersList(List<Folders> foldersList) {
        this.foldersList = foldersList;
    }
}