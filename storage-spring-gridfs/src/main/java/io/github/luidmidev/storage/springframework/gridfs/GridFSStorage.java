package io.github.luidmidev.storage.springframework.gridfs;

import com.mongodb.BasicDBObject;
import io.github.luidmidev.storage.core.Stored;
import io.github.luidmidev.storage.core.Storage;
import io.github.luidmidev.storage.core.exceptions.StorageException;
import io.github.luidmidev.storage.core.utils.StorageUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.apache.commons.io.IOUtils;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.gridfs.GridFsOperations;
import org.springframework.data.mongodb.gridfs.GridFsTemplate;
import org.springframework.stereotype.Service;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Optional;

import static io.github.luidmidev.storage.core.utils.StorageUtils.*;


/**
 * Servicio para operaciones relacionadas con archivos.
 */
@Service
@Log4j2
@RequiredArgsConstructor
public class GridFSStorage extends Storage {

    private final GridFsTemplate template;
    private final GridFsOperations operations;
    private static final String PATH_KEY = "path";
    private static final String FILE_SIZE_KEY = "fileSize";


    @Override
    protected String internalStore(byte[] content, String filename, String path) {

        var contentType = guessContentType(filename);

        var metadata = new BasicDBObject();
        metadata.put(FILE_SIZE_KEY, content.length);
        metadata.put(PATH_KEY, path);
        metadata.put("dateUpload", LocalDateTime.now());
        template.store(new ByteArrayInputStream(content), filename, contentType, metadata);
        return filename;
    }

    @Override
    protected Optional<Stored> internalDownload(String filename, String path) throws IOException {

        var gridFSFile = template.findOne(getQuery(filename, path));

        if (gridFSFile == null) return Optional.empty();
        var metadata = gridFSFile.getMetadata();
        if (metadata == null) throw new StorageException("Metadata not found for file: " + path + "/" + filename);

        return Optional.ofNullable(constructDownloadedFile(
                IOUtils.toByteArray(operations.getResource(gridFSFile).getInputStream()),
                Long.parseLong(metadata.get(FILE_SIZE_KEY).toString()),
                filename,
                metadata.get(PATH_KEY).toString(),
                metadata.get("_contentType").toString()
        ));
    }

    @Override
    protected Optional<Stored.Info> internalInfo(String filename, String path) {

        var gridFSFile = template.findOne(getQuery(filename, path));

        if (gridFSFile == null) return Optional.empty();
        var metadata = gridFSFile.getMetadata();
        if (metadata == null) throw new StorageException("Metadata not found for file: " + path + "/" + filename);

        return Optional.of(constructFileInfo(
                filename,
                Long.parseLong(metadata.get(FILE_SIZE_KEY).toString()),
                metadata.get(PATH_KEY).toString(),
                metadata.get("_contentType").toString()
        ));
    }

    @Override
    protected boolean internalExists(String filename, String path) {
        return template.findOne(getQuery(filename, path)) != null;
    }

    @Override
    protected void internalRemove(String filename, String path) {
        template.delete(getQuery(filename, path));
    }

    private static Query getQuery(String filename, String path) {
        return new Query(Criteria.where("filename").is(filename).and("metadata.path").is(path));
    }


}