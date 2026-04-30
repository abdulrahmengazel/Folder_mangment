package facadeLocal;

import entity.Folders;
import jakarta.ejb.Local;

import java.util.List;
@Local
public interface FolderFacadeLocal {
    void create(Folders folders);

    void edit(Folders folders);

    void remove(Folders folders);
    Folders find(Object id);

    List<Folders> findAll();

}
