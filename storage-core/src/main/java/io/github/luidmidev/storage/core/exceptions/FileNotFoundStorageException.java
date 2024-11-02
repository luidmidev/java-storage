package io.github.luidmidev.storage.core.exceptions;

import io.github.luidmidev.storage.core.PathFile;
import lombok.Getter;

import java.util.function.Supplier;

@Getter
public class FileNotFoundStorageException extends StorageException {

    private final String filename;
    private final String path;

    private FileNotFoundStorageException(String filename, String path) {
        super("File not found: " + filename + " in " + path);
        this.filename = filename;
        this.path = path;
    }

    public FileNotFoundStorageException(PathFile pathFile) {
        this(pathFile.getFilename(), pathFile.getPath());
    }

    public static Supplier<StorageException> fileNotFound(String filename, String path) {
        return () -> new FileNotFoundStorageException(new PathFile(path, filename));
    }

}