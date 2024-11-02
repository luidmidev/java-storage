package io.github.luidmidev.storage.springframework.data.jpa;

import io.github.luidmidev.storage.core.PathFile;
import io.github.luidmidev.storage.core.Stored;
import io.github.luidmidev.storage.core.Storage;
import io.github.luidmidev.storage.core.ToStore;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.luidmidev.storage.core.StorageUtils.*;

@Slf4j
@RequiredArgsConstructor
public final class JpaStorage extends Storage {

    private final FileStoredRepository repository;

    @Override
    protected void internalStore(final ToStore toStore) {

        var filename = toStore.getFilename();
        var content = toStore.getContent();

        var dbFile = FileStored.builder()
                .content(content)
                .contentType(guessContentType(filename))
                .contentLength((long) content.length)
                .originalFileName(filename)
                .path(toStore.getCompletePath())
                .uploadedAt(LocalDateTime.now())
                .build();

        var saved = repository.save(dbFile);
        log.debug("Stored file: {}", saved.getId());
    }

    @Override
    protected Optional<Stored> internalDownload(final PathFile pathFile) {

        var dbFileOptional = repository.findByOriginalFileNameAndPath(pathFile.getFilename(), pathFile.getPath());
        if (dbFileOptional.isEmpty()) return Optional.empty();

        var dbFile = dbFileOptional.get();

        return Optional.of(constructStoredFile(
                dbFile.getContent(),
                dbFile.getContentLength(),
                dbFile.getOriginalFileName(),
                dbFile.getPath(),
                dbFile.getContentType()
        ));
    }

    @Override
    protected Optional<Stored.Info> internalInfo(final PathFile pathFile) {

        var dbFileInfoOptional = repository.findProjectedByOriginalFileNameAndPath(pathFile.getFilename(), pathFile.getPath());
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
    protected boolean internalExists(final PathFile pathFile) {
        return repository.existsByOriginalFileNameAndPath(pathFile.getFilename(), pathFile.getPath());
    }

    @Override
    protected void internalRemove(final PathFile pathFile) {
        repository.deleteByOriginalFileNameAndPath(pathFile.getFilename(), pathFile.getPath());
    }
}
