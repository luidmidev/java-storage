package io.github.luidmidev.storage.core;

import lombok.Data;

@Data
public class PathFile {

    private final String path;
    private final String filename;

    public PathFile(String path, String filename) {
        StorageAssertions.validFilename(filename);
        StorageAssertions.validPath(path);
        this.path = StorageUtils.normalizePath(path);
        this.filename = filename;
    }

    public String getCompletePath() {
        return StorageUtils.factoryPathFile(path, filename);
    }

}
