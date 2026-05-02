package facade;

import entity.Files;
import facadeLocal.FileFacadeLocal;
import jakarta.ejb.Stateless;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
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
        Root<Files> root = cq.from(Files.class);
        cq.select(root).where(cb.isFalse(root.get("deleted")));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Files> findDeleted() {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Files> cq = cb.createQuery(Files.class);
        Root<Files> root = cq.from(Files.class);
        cq.select(root).where(cb.isTrue(root.get("deleted")));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Files> findStarredFiles(Long ownerId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Files> cq = cb.createQuery(Files.class);
        Root<Files> root = cq.from(Files.class);
        cq.select(root).where(cb.and(
                cb.equal(root.get("owner").get("id"), ownerId),
                cb.isFalse(root.get("deleted")),
                cb.isTrue(root.get("starred"))
        )).orderBy(cb.desc(root.get("createdAt")));
        return entityManager.createQuery(cq).getResultList();
    }

    @Override
    public List<Files> findRecentFiles(Long ownerId) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Files> cq = cb.createQuery(Files.class);
        Root<Files> root = cq.from(Files.class);
        cq.select(root).where(cb.and(
                cb.equal(root.get("owner").get("id"), ownerId),
                cb.isFalse(root.get("deleted"))
        )).orderBy(cb.desc(root.get("createdAt")));
        return entityManager.createQuery(cq).getResultList();
    }
}
