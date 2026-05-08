package bean;

import entity.Users;
import facadeLocal.UserFacadeLocal;
import jakarta.ejb.EJB;
import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.ViewScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class LoginBean implements Serializable {

    @EJB
    UserFacadeLocal userFacade;
    @Inject
    FacesContext facesContext;
    private Users user;

    public String login() {
        String email = user.getEmail() != null ? user.getEmail().trim() : null;
        String password = user.getPassword();

        Users user= userFacade.login(email, password);

        if (user!= null) {
            facesContext.getExternalContext().getSessionMap().put("user", user);
            return "dashboard.xhtml?faces-redirect=true";
        } else {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Giriş başarısız", "E-posta veya şifre hatalı.");
            facesContext.addMessage(null, msg);
            return null;
        }
    }

    public String logout() {
        facesContext.getExternalContext().invalidateSession(); // Invalidate entire session securely
        return "/login.xhtml?faces-redirect=true";
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
}
