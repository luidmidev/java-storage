package io.github.luidmidev.storage.core.utils;

import io.github.luidmidev.storage.core.Stored;
import org.apache.tika.Tika;


public final class StorageUtils {

    private StorageUtils() {
        throw new IllegalStateException("Utility class");
    }

    private static final Tika tika = new Tika();

    public static Stored constructDownloadedFile(byte[] bytes, String filename, String path) {
        return constructDownloadedFile(bytes, filename, path, guessContentType(filename));
    }

    public static Stored constructDownloadedFile(byte[] bytes, String filename, String path, String contentType) {
        return constructDownloadedFile(bytes, bytes.length, filename, path, contentType);
    }

    public static Stored constructDownloadedFile(byte[] bytes, long fileSize, String filename, String path, String contentType) {
        return Stored.builder()
                .content(bytes)
                .info(constructFileInfo(filename, fileSize, path, contentType))
                .build();
    }

    public static Stored.Info constructFileInfo(String filename, long fileSize, String path) {
        return constructFileInfo(filename, fileSize, path, guessContentType(filename));
    }

    public static Stored.Info constructFileInfo(String filename, long fileSize, String path, String contentType) {

        return Stored.Info.builder()
                .filename(filename)
                .fileSize(fileSize)
                .path(path)
                .contentType(contentType)
                .build();

    }

    public static String guessContentType(String filename) {
        return tika.detect(filename);
    }
}
