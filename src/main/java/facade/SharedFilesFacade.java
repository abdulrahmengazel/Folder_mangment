package facade;


import entity.SharedFiles;
import facadeLocal.SharedFilesFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;

@Stateless
public class SharedFilesFacade extends AbstractFacade implements SharedFilesFacadeLocal {

    @Override
    public void create(SharedFiles sharedFiles) {
        entityManager.persist(sharedFiles);
    }

    @Override
    public void edit(SharedFiles sharedFiles) {
        entityManager.merge(sharedFiles);
    }

    @Override
    public void remove(SharedFiles sharedFiles) {
        entityManager.remove(entityManager.merge(sharedFiles));
    }

    @Override
    public SharedFiles find(Object id) {
        return entityManager.find(SharedFiles.class, id);
    }

    @Override
    public List<SharedFiles> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<SharedFiles> cq = cb.createQuery(SharedFiles.class);
        cq.select(cq.from(SharedFiles.class));
        return entityManager.createQuery(cq).getResultList();
    }
}
