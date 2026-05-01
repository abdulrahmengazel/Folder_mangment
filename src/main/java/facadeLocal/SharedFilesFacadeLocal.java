package facadeLocal;

import entity.SharedFiles;
import jakarta.ejb.Local;

import java.util.List;
@Local
public interface SharedFilesFacadeLocal {
    void create(SharedFiles sharedFiles);

    void edit(SharedFiles sharedFiles);

    void remove(SharedFiles sharedFiles);

    SharedFiles find(Object id);

    List<SharedFiles> findAll();

}
