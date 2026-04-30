package facade;

import entity.Folders;
import facadeLocal.FolderFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;

import java.util.List;

@Stateless
public class FolderFacade extends AbstractFacade implements FolderFacadeLocal {
    @Override
    public void create(Folders folders) {
        entityManager.persist(folders);
    }

    @Override
    public void edit(Folders folders) {
        entityManager.merge(folders);
    }

    @Override
    public void remove(Folders folders) {
        entityManager.remove(entityManager.merge(folders));
    }

    @Override
    public Folders find(Object id) {
        return entityManager.find(Folders.class, id);
    }

    @Override
    public List<Folders> findAll() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Folders> cq = cb.createQuery(Folders.class);
        cq.select(cq.from(Folders.class));
        return entityManager.createQuery(cq).getResultList();
    }
}
