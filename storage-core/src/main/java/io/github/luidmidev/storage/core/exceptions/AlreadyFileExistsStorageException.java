package io.github.luidmidev.storage.core.exceptions;


import io.github.luidmidev.storage.core.PathFile;
import lombok.Getter;

@Getter
public class AlreadyFileExistsStorageException extends StorageException {

    private final String filename;
    private final String path;

    private AlreadyFileExistsStorageException(String filename, String path) {
        super("File already exists: " + filename + " in " + (path.isEmpty() ? "root path" : path));
        this.filename = filename;
        this.path = path;
    }

    public AlreadyFileExistsStorageException(PathFile pathFile) {
        this(pathFile.getFilename(), pathFile.getPath());
    }
}
