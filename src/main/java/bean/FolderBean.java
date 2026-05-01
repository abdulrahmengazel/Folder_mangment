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

    public void createFolder() {
        // استخراج المستخدم المسجل من الجلسة
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null) {
            // ربط المجلد بالمستخدم الحالي
            getFolder().setOwner(currentUser);

            folderFacade.create(folder);
            System.out.println("Folder created successfully!");
            clearForm();
        } else {
            System.out.println("Error: No user logged in.");
        }
    }

    public void deleteFolder(Folders f) {
        folderFacade.remove(f);
        System.out.println("Folder deleted");
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
        foldersList = folderFacade.findAll();
        return foldersList;
    }

    public void setFoldersList(List<Folders> foldersList) {
        this.foldersList = foldersList;
    }
}