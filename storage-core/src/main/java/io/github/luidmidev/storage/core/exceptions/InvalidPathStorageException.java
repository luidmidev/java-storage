package io.github.luidmidev.storage.core.exceptions;

import lombok.Getter;

@Getter
public class InvalidPathStorageException extends StorageException {

    private final String path;

    public InvalidPathStorageException(String path, String reason) {
        super("Invalid path: " + path + " because " + reason);
        this.path = path;
    }
}
