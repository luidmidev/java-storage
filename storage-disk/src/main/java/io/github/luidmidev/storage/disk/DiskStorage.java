package io.github.luidmidev.storage.disk;

import io.github.luidmidev.storage.core.Stored;
import io.github.luidmidev.storage.core.Storage;
import io.github.luidmidev.storage.core.exceptions.AlreadyFileExistsStorageException;
import io.github.luidmidev.storage.core.exceptions.FileNotFoundStorageException;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Optional;

import static io.github.luidmidev.storage.core.utils.StorageUtils.constructDownloadedFile;
import static io.github.luidmidev.storage.core.utils.StorageUtils.constructFileInfo;
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
    protected void internalStore(byte[] content, String filename, String path) throws IOException {
        log.debug("Storing file {} in path {}", filename, path);
        var relativePath = path + filename;

        var file = new File(storagePath + relativePath);

        createDirIfNotExists(storagePath + path);

        var created = file.createNewFile();

        if (!created) throw new AlreadyFileExistsStorageException(filename, path);

        try (var fos = new FileOutputStream(file)) {
            fos.write(content);
        }
    }

    @Override
    protected Optional<Stored> internalDownload(String filename, String path) throws IOException {
        var fullPath = path + filename;
        var fileOptional = getFile(fullPath);
        if (fileOptional.isEmpty()) return Optional.empty();
        var file = fileOptional.get();
        var bytes = new byte[(int) file.length()];
        try (var fis = new FileInputStream(file)) {
            var read = fis.read(bytes);
            if (read != file.length()) throw new IOException("File not read correctly: " + read + " of " + file.length() + " bytes on " + fullPath);

            return Optional.of(constructDownloadedFile(bytes, filename, path));
        }
    }


    @Override
    protected Optional<Stored.Info> internalInfo(String filename, String path) throws IOException {
        var fullPath = path + filename;
        var pathObject = Paths.get(storagePath + fullPath);
        if (!Files.exists(pathObject)) return Optional.empty();
        return Optional.of(constructFileInfo(filename, Files.size(pathObject), fullPath));
    }

    @Override
    protected boolean internalExists(String filename, String path) {
        return getFile(path + filename).isPresent();
    }

    @Override
    protected void internalRemove(String filename, String path) throws IOException {
        var fullPath = path + filename;
        var file = getFile(fullPath);
        if (file.isEmpty()) throw new FileNotFoundStorageException(filename, path);

        Files.delete(file.get().toPath());

    }

    private Optional<File> getFile(String fullPath) {
        var file = new File(storagePath + fullPath);
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
