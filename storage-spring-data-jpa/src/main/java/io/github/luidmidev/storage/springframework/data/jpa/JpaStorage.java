package io.github.luidmidev.storage.springframework.data.jpa;

import io.github.luidmidev.storage.core.Stored;
import io.github.luidmidev.storage.core.Storage;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.luidmidev.storage.core.utils.StorageUtils.*;

@RequiredArgsConstructor
public final class JpaStorage extends Storage {

    private final FileStoredRepository repository;

    @Override
    protected String internalStore(byte[] upload, String filename, String path) {

        var contentType = guessContentType(filename);

        var dbFile = FileStored.builder()
                .content(upload)
                .contentType(contentType)
                .contentLength((long) upload.length)
                .originalFileName(filename)
                .path(path)
                .uploadedAt(LocalDateTime.now())
                .build();

        var saved = repository.save(dbFile);
        return saved.getId().toString();
    }

    @Override
    protected Optional<Stored> internalDownload(String filename, String path) {

        var dbFileOptional = repository.findByOriginalFileNameAndPath(filename, path);
        if (dbFileOptional.isEmpty()) return Optional.empty();

        var dbFile = dbFileOptional.get();

        return Optional.of(constructDownloadedFile(
                dbFile.getContent(),
                dbFile.getContentLength(),
                dbFile.getOriginalFileName(),
                dbFile.getPath(),
                dbFile.getContentType()
        ));
    }

    @Override
    protected Optional<Stored.Info> internalInfo(String filename, String path) {

        var dbFileInfoOptional = repository.findProjectedByOriginalFileNameAndPath(filename, path);
        if (dbFileInfoOptional.isEmpty()) return Optional.empty();

        var dbFileInfo = dbFileInfoOptional.get();

        return Optional.of(constructFileInfo(
                dbFileInfo.getOriginalFileName(),
                dbFileInfo.getContentLength(),
                dbFileInfo.getPath(),
                dbFileInfo.getContentType()
        ));
    }

    @Override
    protected boolean internalExists(String filename, String path) {
        return repository.existsByOriginalFileNameAndPath(filename, path);
    }

    @Override
    protected void internalRemove(String filename, String path) {
        repository.deleteByOriginalFileNameAndPath(filename, path);
    }
}
