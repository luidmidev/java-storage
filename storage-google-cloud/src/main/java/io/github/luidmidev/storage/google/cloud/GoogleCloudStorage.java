package io.github.luidmidev.storage.google.cloud;

import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import io.github.luidmidev.storage.core.*;
import io.github.luidmidev.storage.core.exceptions.FileNotFoundStorageException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Optional;

import static io.github.luidmidev.storage.core.StorageUtils.*;

@Slf4j
@RequiredArgsConstructor
public class GoogleCloudStorage extends Storage {

    private final Bucket bucket;

    @Override
    protected void internalStore(final ToStore toStore) {
        var contentType = guessContentType(toStore.getFilename());
        var blob = bucket.create(toStore.getCompletePath(), toStore.getContent(), contentType);
        log.debug("Stored blob: {}", blob.getName());
    }

    @Override
    protected Optional<Stored> internalDownload(final PathFile pathFile) {

        var filename = pathFile.getFilename();
        var path = pathFile.getPath();

        var blob = getBlob(pathFile.getCompletePath());

        if (blob == null || !blob.exists()) {
            return Optional.empty();
        }

        return Optional.ofNullable(constructStoredFile(
                blob.getContent(),
                blob.getSize(),
                filename,
                path,
                blob.getContentType()
        ));
    }

    @Override
    protected Optional<Stored.Info> internalInfo(final PathFile pathFile) {

        var filename = pathFile.getFilename();
        var path = pathFile.getPath();

        var blob = getBlob(pathFile.getCompletePath());
        if (blob == null || !blob.exists()) {
            return Optional.empty();
        }

        return Optional.ofNullable(constructFileInfo(
                filename,
                blob.getSize(),
                path,
                blob.getContentType()
        ));
    }

    @Override
    protected boolean internalExists(final PathFile pathFile) {
        var blob = getBlob(pathFile.getCompletePath());
        return blob != null && blob.exists();
    }

    @Override
    protected void internalRemove(final PathFile pathFile) {
        var blob = getBlob(pathFile.getCompletePath());
        if (blob == null || !blob.exists()) {
            throw new FileNotFoundStorageException(pathFile);
        }
        blob.delete();
    }

    private Blob getBlob(String blobName) {
        return bucket.get(blobName);
    }
}
