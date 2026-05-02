package facade;

import entity.Users;
import facadeLocal.UserFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import org.mindrot.jbcrypt.BCrypt;

import java.util.List;
@Stateless
public class UserFacade extends AbstractFacade implements UserFacadeLocal {

    @Override
    public void create(Users users) {
        entityManager.persist(users);
    }

    @Override
    public void edit(Users users) {
        entityManager.merge(users);
    }

    @Override
    public void remove(Users users) {
        entityManager.remove(entityManager.merge(users));
    }

    @Override
    public Users find(Object id) {
        return entityManager.find(Users.class, id);
    }

    @Override
    public List<Users> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Users> cq = cb.createQuery(Users.class);
        cq.select(cq.from(Users.class));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public Users login(String email, String password) {
        try {
            CriteriaBuilder cb = entityManager.getCriteriaBuilder();
            CriteriaQuery<Users> cq = cb.createQuery(Users.class);
            Root<Users> root = cq.from(Users.class);

            cq.select(root).where(cb.equal(root.get("email"), email));

            Users user = entityManager.createQuery(cq).getSingleResult();
            if (user != null && password != null && BCrypt.checkpw(password, user.getPassword())) {
                return user;
            }
            return null;
        }catch (Exception e){
            return null;
        }
    }
}

