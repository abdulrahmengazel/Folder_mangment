package facade;

import entity.Files;
import facadeLocal.FileFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import java.util.List;
@Stateless
public class FileFacade extends AbstractFacade implements FileFacadeLocal {
    @Override
    public void create(Files files) {
        entityManager.persist(files);
    }

    @Override
    public void edit(Files files) {
        entityManager.merge(files);
    }

    @Override
    public void remove(Files files) {
        entityManager.remove(entityManager.merge(files));
    }

    @Override
    public Files find(Object id) {
        return entityManager.find(Files.class, id);
    }

    @Override
    public List<Files> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Files> cq = cb.createQuery(Files.class);
        cq.select(cq.from(Files.class));
        return entityManager.createQuery(cq).getResultList();
    }
}
