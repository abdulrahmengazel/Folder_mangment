package facadeLocal;

import entity.Users;
import jakarta.ejb.Local;

import java.util.List;

@Local
public interface UserFacadeLocal {
    void create(Users users);

    void edit(Users users);

    void remove(Users users);

    Users find(Object id);

    List<Users> findAll();

    Users login (String email, String password);
}
