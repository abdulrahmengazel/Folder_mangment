package facadeLocal;

import entity.Files;
import jakarta.ejb.Local;

import java.util.List;
@Local
public interface FileFacadeLocal {
    void create(Files files);

    void edit(Files files);

    void remove(Files files);
    Files find(Object id);

    List<Files> findAll();

    List<Files> findDeleted();

    List<Files> findStarredFiles(Long ownerId);

    List<Files> findRecentFiles(Long ownerId);
}
