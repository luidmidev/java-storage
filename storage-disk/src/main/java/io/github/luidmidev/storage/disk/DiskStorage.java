package io.github.luidmidev.storage.disk;

import io.github.luidmidev.storage.*;
import io.github.luidmidev.storage.exceptions.AlreadyFileExistsStorageException;
import io.github.luidmidev.storage.exceptions.FileNotFoundStorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static io.github.luidmidev.storage.StorageUtils.constructFileInfo;
import static java.lang.System.getProperty;

@Slf4j
public class DiskStorage extends Storage {

    private final String storagePath;
    private static final String USER_DIR_PROPERTY = "{user.dir}";
    private static final String DEFAULT_STORAGE_PATH = USER_DIR_PROPERTY + "/uploads";

    public DiskStorage() {
        this(DEFAULT_STORAGE_PATH);
    }

    public DiskStorage(String storagePath) {
        this.storagePath = reolveStoragePath(storagePath);
        log.debug("Storage path {}", this.storagePath);
        createDirIfNotExists(this.storagePath);
    }

    private static String reolveStoragePath(String path) {

        if (path.contains(USER_DIR_PROPERTY)) {
            return path.replace(USER_DIR_PROPERTY, getProperty("user.dir"));
        }

        if (path.contains("{user.home}")) {
            return path.replace("{user.home}", getProperty("user.home"));
        }

        return path.endsWith("/") || path.endsWith("\\") ? path.substring(0, path.length() - 1) : path;
    }

    @Override
    protected void internalStore(final ToStore toStore) throws IOException {

        var path = toStore.getPath();
        var completePath = toStore.getCompletePath();

        createDirIfNotExists(storagePath + "/" + path);
        var file = new File(storagePath + "/" + completePath);
        var created = file.createNewFile();

        if (!created) throw new AlreadyFileExistsStorageException(toStore);

        try (var fos = new FileOutputStream(file)) {
            fos.write(toStore.getContent());
        }
    }

    @Override
    protected Optional<Stored> internalDownload(final PathFile pathFile) throws IOException {

        var completePath = pathFile.getCompletePath();
        var filename = pathFile.getFilename();
        var path = pathFile.getPath();

        var fileOptional = getFile(completePath);
        if (fileOptional.isEmpty()) return Optional.empty();
        var file = fileOptional.get();
        var bytes = new byte[(int) file.length()];
        try (var fis = new FileInputStream(file)) {
            var read = fis.read(bytes);
            if (read != file.length()) throw new IOException("File not read correctly: " + read + " of " + file.length() + " bytes on " + completePath);
            return Optional.of(StorageUtils.constructStoredFile(bytes, filename, path));
        }
    }


    @Override
    protected Optional<Stored.Info> internalInfo(final PathFile pathFile) throws IOException {

        var completePath = pathFile.getCompletePath();
        var filename = pathFile.getFilename();
        var path = pathFile.getPath();

        var pathObject = Paths.get(storagePath + "/" + completePath);
        if (!Files.exists(pathObject)) return Optional.empty();
        return Optional.of(constructFileInfo(filename, Files.size(pathObject), path));
    }

    @Override
    protected boolean internalExists(final PathFile pathFile) {
        return getFile(pathFile.getCompletePath()).isPresent();
    }

    @Override
    protected void internalRemove(final PathFile pathFile) throws IOException {
        var file = getFile(pathFile.getCompletePath());
        if (file.isEmpty()) throw new FileNotFoundStorageException(pathFile);

        Files.delete(file.get().toPath());
    }

    private Optional<File> getFile(String completePath) {
        var file = new File(storagePath + "/" + completePath);
        return file.exists() ? Optional.of(file) : Optional.empty();
    }

    private void createDirIfNotExists(String path) {
        var dirs = new File(path);
        if (!dirs.exists()) {
            var created = dirs.mkdirs();
            if (!created) throw new IllegalStateException("Path not created: " + path);
        }
    }
}
