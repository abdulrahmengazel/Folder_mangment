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

@Named("userBean")
@ViewScoped
public class UserBean implements Serializable {

    private static final String ROOT_UPLOAD_DIR = "/home/abdulrahman/cloud_uploads";
    private Users user;
    private List<Users> users;
    @EJB
    private UserFacadeLocal userFacade;
    

    public String createUser() {
        try {
            // Hash the password before saving to the database
            String plainPassword = user.getPassword();
            String hashedPassword = BCrypt.hashpw(plainPassword, BCrypt.gensalt(12)); // Cost factor 12
            user.setPassword(hashedPassword);

            // 1. Save user to DB first to generate ID
            userFacade.create(user);
            System.out.println("User saved to database. ID: " + user.getId());

            // 2. Create the physical path on the system for the new user
            try {
                String userDirectory = "user_" + user.getId();
                java.nio.file.Path userPath = java.nio.file.Paths.get(ROOT_UPLOAD_DIR, userDirectory);

                // OS command to create directory
                if (!java.nio.file.Files.exists(userPath)) {
                    java.nio.file.Files.createDirectories(userPath);
                    System.out.println("Physical root folder created for user: " + userPath.toString());
                }
            } catch (Exception e) {
                // In case of OS failure, print error but do not stop registration
                System.out.println("Warning: Could not create OS directory for user: " + e.getMessage());
            }

            // Redirect user to login page upon success
            return "login.xhtml?faces-redirect=true";

        } catch (Exception e) {
            System.out.println("Error during user registration: " + e.getMessage());
            return null;
        }
    }
    // --- Profile Management ---
    public void loadCurrentUser() {
        if (!FacesContext.getCurrentInstance().isPostback()) {
            Users sessionUser = (Users) FacesContext.getCurrentInstance().getExternalContext().getSessionMap().get("user");
            if (sessionUser != null) {
                // Fetch user data from database to ensure it's up to date
                Users dbUser = userFacade.find(sessionUser.getId());
                if (dbUser != null) {
                    this.user = new Users();
                    this.user.setId(dbUser.getId());
                    this.user.setName(dbUser.getName());
                    this.user.setEmail(dbUser.getEmail());
                    // Do not load password, leave it empty in input field
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
            // Update name
            oldUser.setName(user.getName());

            // Update password only if the user entered a new one
            if (user.getPassword() != null && !user.getPassword().trim().isEmpty()) {
                String hashedPassword = BCrypt.hashpw(user.getPassword(), BCrypt.gensalt(12));
                oldUser.setPassword(hashedPassword);
            }

            userFacade.edit(oldUser);

            // Update Session data
            FacesContext.getCurrentInstance().getExternalContext().getSessionMap().put("user", oldUser);

            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_INFO, "Success", "Profile updated."));

            // Clear password field from UI after update
            user.setPassword("");
        }
    }

    public String deleteMyAccount() {
        if (user == null || user.getId() == null) {
            return null;
        }

        try {
            // Delete user account
            userFacade.remove(userFacade.find(user.getId()));

            // Invalidate user session
            FacesContext.getCurrentInstance().getExternalContext().invalidateSession();

            // Redirect user to login page
            return "login.xhtml?faces-redirect=true";
        } catch (Exception e) {
            FacesContext.getCurrentInstance().addMessage(null,
                    new FacesMessage(FacesMessage.SEVERITY_ERROR, "Error", "Error deleting account: " + e.getMessage()));
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