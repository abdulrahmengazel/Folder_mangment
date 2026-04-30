package facadeLocal;

import entity.Files;

import java.util.List;

public interface FileFacadeLocal {
    void create(Files files);

    void edit(Files files);

    void remove(Files files);
    Files find(Object id);

    List<Files> findAll();
}
