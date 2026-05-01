package bean;

import entity.Folders;
import entity.Users;
import facadeLocal.FolderFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

@Named
@ViewScoped
public class FolderBean implements Serializable {

    private Folders folder;
    private List<Folders> foldersList;

    @EJB
    private FolderFacadeLocal folderFacade;

    public void clearForm() {
        folder = new Folders();
    }

    // يجب إضافة هذا المسار الثابت في أعلى الكلاس إذا لم يكن موجوداً
    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";

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
                    System.out.println("Physical folder created at: " + physicalPath.toString());
                }
            } catch (Exception e) {
                System.out.println("Error creating physical folder: " + e.getMessage());
            }

            System.out.println("Folder created successfully in DB and Linux!");
            clearForm();
            
            return "dashboard.xhtml?faces-redirect=true";
        } else {
            System.out.println("Error: No user logged in.");
            return null;
        }
    }

    public void deleteFolder(Folders f) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && f != null && f.getOwner().getId().equals(currentUser.getId())) {
            folderFacade.remove(f);
            System.out.println("Folder deleted");
            foldersList = null; // force reload
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
                    .collect(Collectors.toList());
        } else {
            foldersList = java.util.Collections.emptyList();
        }
        return foldersList;
    }

    public void setFoldersList(List<Folders> foldersList) {
        this.foldersList = foldersList;
    }
}