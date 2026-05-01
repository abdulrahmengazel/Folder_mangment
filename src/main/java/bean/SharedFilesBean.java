package bean;

import entity.SharedFiles;
import entity.Files;
import entity.Users;
import enums.PermissionEnum;
import facadeLocal.SharedFilesFacadeLocal;
import facadeLocal.FileFacadeLocal;
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

@Named
@ViewScoped
public class SharedFilesBean implements Serializable {

    private SharedFiles sharedFile;
    private List<SharedFiles> sharedFilesList;
    private List<SharedFiles> sharedWithMeList;

    // لتخزين الملفات والمستخدمين المتاحين للمشاركة
    private List<Files> availableFiles;
    private List<Users> availableUsers;

    // للبحث والتصفية
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
     * مشاركة ملف مع مستخدم آخر
     */
    public void shareFile() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        // التحقق من البيانات المدخلة
        if (selectedFileId == null || selectedFileId <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", "يرجى اختيار ملف"));
            return;
        }

        if (selectedUserId == null || selectedUserId <= 0) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", "يرجى اختيار مستخدم"));
            return;
        }

        if (currentUser == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", "لم يتم العثور على المستخدم الحالي"));
            return;
        }

        // التحقق من أن المستخدم الحالي هو مالك الملف
        Files file = fileFacade.find(selectedFileId);
        if (file == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", "الملف غير موجود"));
            return;
        }

        if (!file.getOwner().getId().equals(currentUser.getId())) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", "لا يمكنك مشاركة ملف لا تملكه"));
            return;
        }

        // التحقق من أن المستخدم المستقبل موجود
        Users recipient = userFacade.find(selectedUserId);
        if (recipient == null) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", "المستخدم المختار غير موجود"));
            return;
        }

        // التحقق من عدم مشاركة الملف مسبقاً مع نفس المستخدم
        List<SharedFiles> existing = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getFile().getId().equals(selectedFileId) &&
                        sf.getRecipient().getId().equals(selectedUserId))
                .collect(Collectors.toList());

        if (!existing.isEmpty()) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_WARN,
                    "تحذير", "هذا الملف مشارك مسبقاً مع هذا المستخدم"));
            return;
        }

        try {
            // إنشاء تسجيل مشاركة جديد
            sharedFile = new SharedFiles();
            sharedFile.setFile(file);
            sharedFile.setRecipient(recipient);
            sharedFile.setPermission(selectedPermission);

            sharedFilesFacade.create(sharedFile);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "نجاح", "تم مشاركة الملف بنجاح!"));

            clearForm();
            refreshLists();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ في النظام", e.getMessage()));
        }
    }

    /**
     * إلغاء مشاركة ملف
     */
    public void removeSharedFile(SharedFiles sf) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (sf == null || sf.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "خطأ", "لا يمكن حذف مشاركة فارغة"));
                return;
            }

            sharedFilesFacade.remove(sf);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "نجاح", "تم إلغاء المشاركة بنجاح"));

            refreshLists();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", e.getMessage()));
        }
    }

    /**
     * تعديل صلاحيات الملف المشارك
     */
    public void changePermission(SharedFiles sf, PermissionEnum newPermission) {
        FacesContext context = FacesContext.getCurrentInstance();

        try {
            if (sf == null || sf.getId() == null) {
                context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                        "خطأ", "لا يمكن تعديل مشاركة فارغة"));
                return;
            }

            sf.setPermission(newPermission);
            sharedFilesFacade.edit(sf);

            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_INFO,
                    "نجاح", "تم تحديث الصلاحيات بنجاح"));

            refreshLists();

        } catch (Exception e) {
            context.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR,
                    "خطأ", e.getMessage()));
        }
    }

    /**
     * الحصول على جميع الملفات المشاركة معي (كمستقبل)
     */
    public List<SharedFiles> getSharedWithMe() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            return new ArrayList<>();
        }

        sharedWithMeList = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getRecipient().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        return sharedWithMeList;
    }

    /**
     * الحصول على جميع الملفات التي شاركتها (كمالك)
     */
    public List<SharedFiles> getMySharedFiles() {
        FacesContext context = FacesContext.getCurrentInstance();
        Users currentUser = (Users) context.getExternalContext().getSessionMap().get("user");

        if (currentUser == null) {
            return new ArrayList<>();
        }

        sharedFilesList = sharedFilesFacade.findAll().stream()
                .filter(sf -> sf.getFile().getOwner().getId().equals(currentUser.getId()))
                .collect(Collectors.toList());

        return sharedFilesList;
    }

    /**
     * الحصول على الملفات المتاحة للمشاركة (ملفاتي فقط)
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
                .collect(Collectors.toList());

        return availableFiles;
    }

    /**
     * الحصول على جميع المستخدمين (ما عدا المستخدم الحالي)
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
     * التحقق من وجود صلاحية الكتابة للملف المشارك
     */
    public boolean hasWritePermission(SharedFiles sf) {
        return sf != null && sf.getPermission() == PermissionEnum.WRITE;
    }

    /**
     * التحقق من وجود صلاحية القراءة
     */
    public boolean hasReadPermission(SharedFiles sf) {
        return sf != null && (sf.getPermission() == PermissionEnum.READ ||
                sf.getPermission() == PermissionEnum.WRITE);
    }

    /**
     * تنظيف نموذج الإدخال
     */
    public void clearForm() {
        sharedFile = null;
        selectedFileId = null;
        selectedUserId = null;
        selectedPermission = PermissionEnum.READ;
    }

    /**
     * تحديث القوائم
     */
    public void refreshLists() {
        getMySharedFiles();
        getSharedWithMe();
        getAvailableFiles();
        getAvailableUsers();
    }

    // ============ GETTERS AND SETTERS ============

    public SharedFiles getSharedFile() {
        if (sharedFile == null) {
            sharedFile = new SharedFiles();
        }
        return sharedFile;
    }

    public void setSharedFile(SharedFiles sharedFile) {
        this.sharedFile = sharedFile;
    }

    public List<SharedFiles> getSharedFilesList() {
        if (sharedFilesList == null) {
            getMySharedFiles();
        }
        return sharedFilesList;
    }

    public void setSharedFilesList(List<SharedFiles> sharedFilesList) {
        this.sharedFilesList = sharedFilesList;
    }

    public List<SharedFiles> getSharedWithMeList() {
        if (sharedWithMeList == null) {
            getSharedWithMe();
        }
        return sharedWithMeList;
    }

    public void setSharedWithMeList(List<SharedFiles> sharedWithMeList) {
        this.sharedWithMeList = sharedWithMeList;
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
