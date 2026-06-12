package bean;

import entity.Files;
import entity.SharedFiles;
import entity.Users;
import enums.PermissionEnum;
import facadeLocal.FileFacadeLocal;
import facadeLocal.SharedFilesFacadeLocal;
import facadeLocal.UserFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Named("sharedFilesBean")
@ViewScoped
public class SharedFilesBean implements Serializable {

    private SharedFiles sharedFile;
    private List<SharedFiles> sharedFilesList;
    private List<SharedFiles> sharedWithMeList;
    private List<Files> availableFiles;

    private Long selectedFileId;
    private String recipientEmail;
    private PermissionEnum selectedPermission = PermissionEnum.READ;

    @EJB
    private SharedFilesFacadeLocal sharedFilesFacade;

    @EJB
    private FileFacadeLocal fileFacade;

    @EJB
    private UserFacadeLocal userFacade;

    public void shareFile() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (selectedFileId == null || selectedFileId <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please select a file."));
            return;
        }

        if (recipientEmail == null || recipientEmail.trim().isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Please enter the recipient email."));
            return;
        }

        if (currentUser == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Current user is not found."));
            return;
        }

        Files file = fileFacade.find(selectedFileId);
        if (file == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File not found."));
            return;
        }

        if (file.isDeleted()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "File is deleted."));
            return;
        }

        if (!file.getOwner().getId().equals(currentUser.getId())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You are not the owner of this file."));
            return;
        }

        String normalizedEmail = recipientEmail.trim();
        if (currentUser.getEmail() != null && currentUser.getEmail().equalsIgnoreCase(normalizedEmail)) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "You cannot share a file with yourself."));
            return;
        }

        Users recipient = userFacade.findByEmail(normalizedEmail);
        if (recipient == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "No user found with that email."));
            return;
        }

        boolean alreadyShared = sharedFilesFacade.findAll().stream()
                .anyMatch(sf -> sf.getFile() != null
                        && sf.getRecipient() != null
                        && sf.getFile().getId().equals(selectedFileId)
                        && sf.getRecipient().getId().equals(recipient.getId()));
        if (alreadyShared) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN, "Warning", "File already shared with this user."));
            return;
        }

        try {
            sharedFile = new SharedFiles();
            sharedFile.setFile(file);
            sharedFile.setRecipient(recipient);
            sharedFile.setPermission(selectedPermission);

            sharedFilesFacade.create(sharedFile);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "File shared successfully."));

            clearForm();
            refreshLists();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "System Error", e.getMessage()));
        }
    }

    public void removeSharedFile(SharedFiles sf) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (sf == null || sf.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Shared file cannot be deleted."));
                return;
            }

            sharedFilesFacade.remove(sf);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Sharing removed successfully."));
            refreshLists();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public void changePermission(SharedFiles sf, PermissionEnum newPermission) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (sf == null || sf.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Shared file cannot be updated."));
                return;
            }

            sf.setPermission(newPermission);
            sharedFilesFacade.edit(sf);
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Permission updated successfully."));
            refreshLists();
        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", e.getMessage()));
        }
    }

    public List<SharedFiles> getSharedWithMe() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            return new ArrayList<>();
        }

        sharedWithMeList = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getRecipient().getId().equals(currentUser.getId()))
                .filter(sf -> sf.getFile() != null && !sf.getFile().isDeleted())
                .collect(Collectors.toList());

        return sharedWithMeList;
    }

    public List<SharedFiles> getMySharedFiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            return new ArrayList<>();
        }

        sharedFilesList = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getFile() != null && !sf.getFile().isDeleted())
                .filter(sf -> sf.getFile().getOwner().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        return sharedFilesList;
    }

    public List<Files> getAvailableFiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            availableFiles = new ArrayList<>();
            return availableFiles;
        }

        availableFiles = fileFacade.findAll().stream()
                .filter(f -> f.getOwner().getId().equals(currentUser.getId()))
                .filter(f -> !f.isDeleted())
                .collect(Collectors.toList());

        return availableFiles;
    }

    public boolean hasWritePermission(SharedFiles sf) {
        return sf != null && sf.getPermission() == PermissionEnum.WRITE;
    }

    public boolean hasReadPermission(SharedFiles sf) {
        return sf != null && (sf.getPermission() == PermissionEnum.READ || sf.getPermission() == PermissionEnum.WRITE);
    }

    public void clearForm() {
        sharedFile = null;
        selectedFileId = null;
        recipientEmail = null;
        selectedPermission = PermissionEnum.READ;
    }

    public void refreshLists() {
        getMySharedFiles();
        getSharedWithMe();
        getAvailableFiles();
    }

    public List<SharedFiles> getSharedFilesList() {
        if (sharedFilesList == null) {
            getMySharedFiles();
        }
        return sharedFilesList;
    }

    public List<SharedFiles> getSharedWithMeList() {
        if (sharedWithMeList == null) {
            getSharedWithMe();
        }
        return sharedWithMeList;
    }

    public Long getSelectedFileId() {
        return selectedFileId;
    }

    public void setSelectedFileId(Long selectedFileId) {
        this.selectedFileId = selectedFileId;
    }

    public String getRecipientEmail() {
        return recipientEmail;
    }

    public void setRecipientEmail(String recipientEmail) {
        this.recipientEmail = recipientEmail;
    }

    public PermissionEnum getSelectedPermission() {
        return selectedPermission;
    }

    public void setSelectedPermission(PermissionEnum selectedPermission) {
        this.selectedPermission = selectedPermission;
    }

    public PermissionEnum[] getPermissions() {
        return PermissionEnum.values();
    }
}
