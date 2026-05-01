package bean;

import entity.Folders;
import facadeLocal.FolderFacadeLocal;
import jakarta.ejb.EJB;

import java.io.Serializable;
import java.util.List;

public class FolderBean implements Serializable {
    private Folders folder;
    private List<Folders> foldersList;

    @EJB
    private FolderFacadeLocal folderFacade;

    public Folders getFolder(){
        if (folder == null){
            folder = new Folders();
        }
        return folder;
    }
    public void setFolder (Folders folder){
        this.folder = folder;
    }

    public void clearForm (){
        folder = new Folders();
    }

    public void createFolder(){
        folderFacade.create(folder);
        System.out.println("Folder Created successfully");
        clearForm();
    }

}
