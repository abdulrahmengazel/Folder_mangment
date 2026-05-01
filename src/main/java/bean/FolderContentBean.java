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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Named
@ViewScoped
public class FolderContentBean implements Serializable {

    private Long folderId;
    private Folders currentFolder;
    private List<Files> filesInFolder;

    @EJB
    private FolderFacadeLocal folderFacade;

    @EJB
    private FileFacadeLocal fileFacade;

    public void init() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            return;
        }

        if (folderId != null) {
            currentFolder = folderFacade.find(folderId);
            
            // Check if folder exists and belongs to the current user
            if (currentFolder != null && currentFolder.getOwner().getId().equals(currentUser.getId())) {
                loadFilesInFolder(currentUser.getId());
            } else {
                currentFolder = null; // Prevent access to others' folders
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Access denied or folder not found."));
            }
        }
    }

    private void loadFilesInFolder(Long userId) {
        List<Files> allFiles = fileFacade.findAll();
        filesInFolder = new ArrayList<>();
        if (allFiles != null && currentFolder != null) {
            for (Files file : allFiles) {
                // Filter by folder ID and make sure the file belongs to the user
                if (file.getFolder() != null && 
                    file.getFolder().getId().equals(currentFolder.getId()) &&
                    file.getOwner().getId().equals(userId)) {
                    filesInFolder.add(file);
                }
            }
        }
    }

    public void deleteFile(Files file) {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser != null && file != null && file.getOwner().getId().equals(currentUser.getId())) {
            fileFacade.remove(file);
            loadFilesInFolder(currentUser.getId()); // Reload list after deletion
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "File deleted successfully."));
        } else {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You don't have permission to delete this file."));
        }
    }

    public Long getFolderId() {
        return folderId;
    }

    public void setFolderId(Long folderId) {
        this.folderId = folderId;
    }

    public Folders getCurrentFolder() {
        return currentFolder;
    }

    public void setCurrentFolder(Folders currentFolder) {
        this.currentFolder = currentFolder;
    }

    public List<Files> getFilesInFolder() {
        return filesInFolder;
    }

    public void setFilesInFolder(List<Files> filesInFolder) {
        this.filesInFolder = filesInFolder;
    }
}