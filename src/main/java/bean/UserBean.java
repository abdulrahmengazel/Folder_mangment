package bean;

import entity.Users;
import facadeLocal.UserFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;
import org.mindrot.jbcrypt.BCrypt;

import java.io.Serializable;
import java.util.List;

@Named
@ViewScoped
public class UserBean implements Serializable {
    // تأكد من تعريف المسار الأساسي في أعلى الكلاس
    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";
    private Users user;
    private List<Users> users;
    @EJB
    private UserFacadeLocal userFacade;

    public void clearForm() {
        user = new Users();
    }

    public String createUser() {
        try {
            // Hash the password before saving to the database
            String plainPassword = user.getPassword();
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)); // Cost factor 12
            user.setPassword(hashedPassword);

            // 1. حفظ المستخدم في قاعدة البيانات أولاً ليحصل على ID
            userFacade.create(user);
            System.out.println("Kullanıcı veritabanına kaydedildi. ID: " + user.getId());

            // 2. بناء المسار الفيزيائي للمستخدم الجديد على نظام لينكس
            try {
                String userDirectory = "user_" + user.getId();
                java.nio.file.Path userPath = java.nio.file.Paths.get(ROOT_UPLOAD_DIR, userDirectory);

                // أمر نظام التشغيل بإنشاء المجلد
                if (!java.nio.file.Files.exists(userPath)) {
                    java.nio.file.Files.createDirectories(userPath);
                    System.out.println("Kullanıcı için fiziksel kök klasör oluşturuldu: " + userPath.toString());
                }
            } catch (Exception e) {
                // في حال فشل نظام التشغيل، نطبع الخطأ لكن لا نوقف التسجيل
                System.out.println("Uyarı: Kullanıcı için işletim sistemi klasörü oluşturulamadı: " + e.getMessage());
            }

            // توجيه المستخدم إلى صفحة الدخول بعد نجاح العملية
            return "login.xhtml?faces-redirect=true";

        } catch (Exception e) {
            System.out.println("Kullanıcı kaydı sırasında hata oluştu: " + e.getMessage());
            return null;
        }
    }

    public void editUser() {
        // إذا كان هناك تعديل لكلمة المرور يجب تشفيرها أيضاً
        if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
             String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
             user.setPassword(hashedPassword);
        } else {
             // إبقاء كلمة المرور القديمة إذا لم يتم إدخال كلمة جديدة
             Users oldUser = userFacade.find(user.getId());
             if (oldUser != null) {
                 user.setPassword(oldUser.getPassword());
             }
        }
        
        if (user.getId() == null) {
            // إضافة مستخدم جديد من لوحة التحكم
             userFacade.create(user);
             System.out.println("Yeni kullanıcı eklendi");
        } else {
             userFacade.edit(user);
             System.out.println("Kullanıcı güncellendi");
        }
        
        // تفريغ الفورم بعد الحفظ
        user = new Users();
    }

    public void updateForm(Users u) {
        this.user = new Users();
        this.user.setId(u.getId());
        this.user.setName(u.getName());
        this.user.setEmail(u.getEmail());
        // لا ننقل كلمة المرور لتجنب إظهارها في الواجهة
    }

    public void deleteUser(Users u) {
        userFacade.remove(u);
        System.out.println("Kullanıcı silindi");
    }

    // --- Profile Management ---
    public void loadCurrentUser() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            Users sessionUser = (Users) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            if (sessionUser != null) {
                // جلب بيانات المستخدم من قاعدة البيانات لضمان تحديثها
                Users dbUser = userFacade.find(sessionUser.getId());
                if(dbUser != null) {
                    this.user = new Users();
                    this.user.setId(dbUser.getId());
                    this.user.setName(dbUser.getName());
                    this.user.setEmail(dbUser.getEmail());
                    // لا نحمل كلمة المرور، لتبقى فارغة في حقل الإدخال
                }
            }
        }
    }

    public void updateProfile() {
        if (user == null || user.getId() == null) {
            return;
        }
        
        Users oldUser = userFacade.find(user.getId());
        if (oldUser != null) {
            // تحديث الاسم
            oldUser.setName(user.getName());
            
            // تحديث كلمة المرور فقط في حال قام المستخدم بإدخال واحدة جديدة
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
                oldUser.setPassword(hashedPassword);
            }
            
            userFacade.edit(oldUser);
            
            // تحديث بيانات الجلسة (Session)
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", oldUser);
            
            FacesContext.getCurrentInstance().addMessage(null, 
                new FacesMessage(FacesMessage.SEVERITY_INFO, "Başarılı", "Profiliniz güncellendi."));
                
            // تفريغ حقل كلمة المرور من الواجهة بعد التحديث
            user.setPassword("");
        }
    }

    public String deleteMyAccount() {
        if (user == null || user.getId() == null) {
            return null;
        }

        try {
            // حذف حساب المستخدم
            userFacade.remove(userFacade.find(user.getId()));

            // إنهاء جلسة المستخدم
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

            // توجيه المستخدم لصفحة تسجيل الدخول
            return "login.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Hata", "Hesap silinirken bir hata oluştu: " + e.getMessage()));
            return null;
        }
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
        users = userFacade.findAll();
        return users;
    }

    public void setUsers(List<Users> users) {
        this.users = users;
    }
}