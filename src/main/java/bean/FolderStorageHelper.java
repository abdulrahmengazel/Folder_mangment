package bean;

import entity.Folders;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

final class FolderStorageHelper {

    private FolderStorageHelper() {
    }

    static Path userRootPath(String rootUploadDir, Long userId) {
        return Path.of(rootUploadDir, "user_" + userId);
    }

    static Path resolveFolderPath(String rootUploadDir, Folders folder) {
        if (folder == null || folder.getOwner() == null || folder.getId() == null) {
            return null;
        }

        List<String> folderSegments = new ArrayList<>();
        Folders current = folder;
        while (current != null && current.getId() != null) {
            folderSegments.add("folder_" + current.getId());
            current = current.getParentFolder();
        }

        Collections.reverse(folderSegments);
        String[] parts = new String[folderSegments.size() + 1];
        parts[0] = "user_" + folder.getOwner().getId();
        for (int i = 0; i < folderSegments.size(); i++) {
            parts[i + 1] = folderSegments.get(i);
        }
        return Path.of(rootUploadDir, parts);
    }
}
