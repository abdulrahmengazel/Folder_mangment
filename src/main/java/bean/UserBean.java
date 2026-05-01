package bean;

import entity.Users;
import facadeLocal.UserFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class UserBean implements Serializable {
    private Users user;
    private List<Users> users;

    @EJB
    private UserFacadeLocal userFacade;

    public void clearForm() {
        user = new Users();
    }

    // تأكد من تعريف المسار الأساسي في أعلى الكلاس
    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";

    public String createUser() {
        try {
            // 1. حفظ المستخدم في قاعدة البيانات أولاً ليحصل على ID
            userFacade.create(user);
            System.out.println("User saved to database with ID: " + user.getId());

            // 2. بناء المسار الفيزيائي للمستخدم الجديد على نظام لينكس
            try {
                String userDirectory = "user_" + user.getId();
                java.nio.file.Path userPath = java.nio.file.Paths.get(ROOT_UPLOAD_DIR, userDirectory);

                // أمر نظام التشغيل بإنشاء المجلد
                if (!java.nio.file.Files.exists(userPath)) {
                    java.nio.file.Files.createDirectories(userPath);
                    System.out.println("Physical root folder created for user at: " + userPath.toString());
                }
            } catch (Exception e) {
                // في حال فشل نظام التشغيل، نطبع الخطأ لكن لا نوقف التسجيل
                System.out.println("Warning: Failed to create OS folder for user: " + e.getMessage());
            }

            // توجيه المستخدم إلى صفحة الدخول بعد نجاح العملية
            return "login.xhtml?faces-redirect=true";

        } catch (Exception e) {
            System.out.println("Error registering user: " + e.getMessage());
            return null;
        }
    }

    public void editUser() {
        // تم التعديل لتتطابق مع دالة التعديل في Facade
        userFacade.edit(user);
        System.out.println("User edited");
    }

    public void updateForm(Users u) {
        this.user = u;
    }

    public void deleteUser(Users u) {
        userFacade.remove(u);
        System.out.println("User deleted");
    }

    public Users getUser() {
        if (user == null) {
            user = new Users();
        }
        return user;
    }

    public void setUser(Users user) {
        this.user = user;
    }

    public List<Users> getUsers() {
        // تم التعديل لتتطابق مع دالة البحث في Facade
        users = userFacade.findAll();
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }
}