package io.github.luidmidev.storage.core.exceptions;


import lombok.Getter;

@Getter
public class AlreadyFileExistsStorageException extends StorageException {

    private final String filename;
    private final String path;

    public AlreadyFileExistsStorageException(String filename, String path) {
        super("File already exists: " + filename + " in " + path);
        this.filename = filename;
        this.path = path;
    }
}
