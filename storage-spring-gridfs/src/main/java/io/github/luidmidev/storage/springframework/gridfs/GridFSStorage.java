package io.github.luidmidev.storage.springframework.gridfs;

import com.mongodb.BasicDBObject;
import io.github.luidmidev.storage.PathFile;
import io.github.luidmidev.storage.Stored;
import io.github.luidmidev.storage.Storage;
import io.github.luidmidev.storage.ToStore;
import io.github.luidmidev.storage.exceptions.StorageException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.luidmidev.storage.StorageUtils.*;


/**
 * Servicio para operaciones relacionadas con archivos.
 */
@RequiredArgsConstructor
public final class GridFSStorage extends Storage {

    private final GridFsTemplate template;
    private final GridFsOperations operations;
    private static final String PATH_KEY = "path";
    private static final String FILE_SIZE_KEY = "filesize";


    @Override
    protected void internalStore(ToStore toStore) {
        var filename = toStore.getFilename();
        var content = toStore.getContent();

        var metadata = new BasicDBObject();
        metadata.put(FILE_SIZE_KEY, content.length);
        metadata.put(PATH_KEY, toStore.getCompletePath());
        metadata.put("dateUpload", LocalDateTime.now());
        template.store(new ByteArrayInputStream(content), filename, guessContentType(filename), metadata);
    }

    @Override
    protected Optional<Stored> internalDownload(final PathFile pathFile) throws IOException {

        var gridFSFile = template.findOne(createQuery(pathFile));

        if (gridFSFile == null) return Optional.empty();
        var metadata = gridFSFile.getMetadata();
        if (metadata == null) throw new StorageException("Metadata not found for file: " + pathFile.getCompletePath());

        return Optional.ofNullable(constructStoredFile(
                operations.getResource(gridFSFile).getContentAsByteArray(),
                Long.parseLong(metadata.get(FILE_SIZE_KEY).toString()),
                pathFile.getFilename(),
                metadata.get(PATH_KEY).toString(),
                metadata.get("_contentType").toString()
        ));
    }

    @Override
    protected Optional<Stored.Info> internalInfo(final PathFile pathFile) {

        var gridFSFile = template.findOne(createQuery(pathFile));

        if (gridFSFile == null) return Optional.empty();
        var metadata = gridFSFile.getMetadata();
        if (metadata == null) throw new StorageException("Metadata not found for file: " + pathFile.getCompletePath());

        return Optional.of(constructFileInfo(
                pathFile.getFilename(),
                Long.parseLong(metadata.get(FILE_SIZE_KEY).toString()),
                metadata.get(PATH_KEY).toString(),
                metadata.get("_contentType").toString()
        ));
    }

    @Override
    protected boolean internalExists(final PathFile pathFile) {
        return template.findOne(createQuery(pathFile)) != null;
    }

    @Override
    protected void internalRemove(final PathFile pathFile) {
        template.delete(createQuery(pathFile));
    }

    private static Query createQuery(final PathFile pathFile) {
        var filename = pathFile.getFilename();
        var path = pathFile.getPath();
        return new Query(Criteria.where("filename").is(filename).and("metadata." + PATH_KEY).is(path));
    }


}