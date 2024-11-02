package io.github.luidmidev.storage.firestore;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import io.github.luidmidev.storage.core.Stored;
import io.github.luidmidev.storage.core.Storage;
import io.github.luidmidev.storage.core.exceptions.FileNotFoundStorageException;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

import static io.github.luidmidev.storage.core.utils.StorageUtils.*;

@RequiredArgsConstructor
public class GoogleCloudStorage extends Storage {

    private final Bucket bucket;

    @Override
    protected String internalStore(byte[] content, String filename, String path) {

        var fullPath = path + filename;
        var contentType = guessContentType(filename);
        var blob = bucket.create(fullPath, content, contentType);

        return blob.getName();
    }

    @Override
    protected Optional<Stored> internalDownload(String filename, String path) {
        var fullPath = path + filename;
        var blob = getBlob(fullPath);

        if (blob == null || !blob.exists()) {
            return Optional.empty();
        }

        var split = SplitPath.from(fullPath);

        return Optional.ofNullable(constructDownloadedFile(
                blob.getContent(),
                blob.getSize(),
                split.filename(),
                split.path(),
                blob.getContentType()
        ));
    }

    @Override
    protected Optional<Stored.Info> internalInfo(String filename, String path) {
        var fullPath = path + filename;
        var blob = getBlob(fullPath);

        if (blob == null || !blob.exists()) {
            return Optional.empty();
        }

        var split = SplitPath.from(fullPath);

        return Optional.ofNullable(constructFileInfo(
                split.filename(),
                blob.getSize(),
                split.path(),
                blob.getContentType()
        ));
    }

    @Override
    protected boolean internalExists(String filename, String path) {
        var blob = getBlob(filename, path);
        return blob != null && blob.exists();
    }

    @Override
    protected void internalRemove(String filename, String path) {
        var fullPath = path + filename;
        var blob = getBlob(fullPath);
        if (blob == null || !blob.exists()) {
            throw new FileNotFoundStorageException(filename, path);
        }
        blob.delete();
    }

    private Blob getBlob(String filename, String path) {
        return getBlob(filename + path);
    }

    private Blob getBlob(String blobName) {
        return bucket.get(blobName);
    }
}
