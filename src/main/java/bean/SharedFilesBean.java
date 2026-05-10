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

    // To store available files and users for sharing
    private List<Files> availableFiles;
    private List<Users> availableUsers;

    // For searching and filtering
    private Long selectedFileId;
    private Long selectedUserId;
    private PermissionEnum selectedPermission = PermissionEnum.READ;

    @EJB
    private SharedFilesFacadeLocal sharedFilesFacade;

    @EJB
    private FileFacadeLocal fileFacade;

    @EJB
    private UserFacadeLocal userFacade;

    // ============ METHODS ============

    /**
     * Share a file with another user
     */
    public void shareFile() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        // Validate input data
        if (selectedFileId == null || selectedFileId <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Please select a file."));
            return;
        }

        if (selectedUserId == null || selectedUserId <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Please select a user."));
            return;
        }

        if (currentUser == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "Current user is not found."));
            return;
        }

        // Verify that the current user is the owner of the file
        Files file = fileFacade.find(selectedFileId);
        if (file == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "File not found."));
            return;
        }

        if (file.isDeleted()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "File is deleted."));
            return;
        }

        if (!file.getOwner().getId().equals(currentUser.getId())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "You are not the owner of this file."));
            return;
        }

        // Verify that the recipient exists
        Users recipient = userFacade.find(selectedUserId);
        if (recipient == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", "User not found."));
            return;
        }

        // Prevent sharing the file again with the same user
        List<SharedFiles> existing = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getFile().getId().equals(selectedFileId) &&
                        sf.getRecipient().getId().equals(selectedUserId))
                .collect(Collectors.toList());

        if (!existing.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "Warning", "File already shared with this user."));
            return;
        }

        try {
            // Create a new share record
            sharedFile = new SharedFiles();
            sharedFile.setFile(file);
            sharedFile.setRecipient(recipient);
            sharedFile.setPermission(selectedPermission);

            sharedFilesFacade.create(sharedFile);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Success", "File shared successfully."));

            clearForm();
            refreshLists();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "System Error", e.getMessage()));
        }
    }

    /**
     * Remove a shared file
     */
    public void removeSharedFile(SharedFiles sf) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (sf == null || sf.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Shared file cannot be deleted."));
                return;
            }

            sharedFilesFacade.remove(sf);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Success", "Sharing removed successfully."));

            refreshLists();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", e.getMessage()));
        }
    }

    /**
     * Change permissions for a shared file
     */
    public void changePermission(SharedFiles sf, PermissionEnum newPermission) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (sf == null || sf.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "Error", "Shared file cannot be updated."));
                return;
            }

            sf.setPermission(newPermission);
            sharedFilesFacade.edit(sf);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "Success", "Permission updated successfully."));

            refreshLists();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "Error", e.getMessage()));
        }
    }

    /**
     * Get all files shared with me (as a recipient)
     */
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

    /**
     * Get all files I shared (as an owner)
     */
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

    /**
     * Get files available to share (my files only)
     */
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

    /**
     * Get all users (except the current user)
     */
    public List<Users> getAvailableUsers() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            availableUsers = new ArrayList<>();
            return availableUsers;
        }

        availableUsers = userFacade.findAll().stream()
                .filter(u -> !u.getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        return availableUsers;
    }

    /**
     * Check for write permission on a shared file
     */
    public boolean hasWritePermission(SharedFiles sf) {
        return sf != null && sf.getPermission() == PermissionEnum.WRITE;
    }

    /**
     * Check for read permission
     */
    public boolean hasReadPermission(SharedFiles sf) {
        return sf != null && (sf.getPermission() == PermissionEnum.READ ||
                sf.getPermission() == PermissionEnum.WRITE);
    }

    /**
     * Clear the input form
     */
    public void clearForm() {
        sharedFile = null;
        selectedFileId = null;
        selectedUserId = null;
        selectedPermission = PermissionEnum.READ;
    }

    /**
     * Refresh lists
     */
    public void refreshLists() {
        getMySharedFiles();
        getSharedWithMe();
        getAvailableFiles();
        getAvailableUsers();
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

    public Long getSelectedUserId() {
        return selectedUserId;
    }

    public void setSelectedUserId(Long selectedUserId) {
        this.selectedUserId = selectedUserId;
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