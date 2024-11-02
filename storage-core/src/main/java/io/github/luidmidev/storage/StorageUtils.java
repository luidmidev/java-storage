package io.github.luidmidev.storage;

import org.apache.tika.Tika;


public final class StorageUtils {

    private static final Tika TIKA = new Tika();

    private StorageUtils() {
        throw new IllegalStateException("Utility class");
    }


    /**
     * Obtiene la ruta ideal de un path para almacenar un archivo
     *
     * @param path Path a normalizar
     * @return Path normalizado
     */
    public static String normalizePath(String path) {
        if (path == null || path.isBlank() || path.equals("/")) return "";
        var normalizedPath = path.startsWith("/") ? path.substring(1) : path;
        return normalizedPath.endsWith("/") ? normalizedPath.substring(0, normalizedPath.length() - 1) : normalizedPath;
    }

    public static String factoryPathFile(String path, String filename) {
        return path.isEmpty() ? filename : path + "/" + filename;
    }

    /**
     * Construye un archivo descargado con los bytes, nombre y ruta, el tipo de contenido se detecta automáticamente con el nombre del archivo
     *
     * @param bytes Contenido del archivo
     * @param filename Nombre del archivo
     * @param path Ruta del archivo
     * @return Archivo descargado
     */
    public static Stored constructStoredFile(byte[] bytes, String filename, String path) {
        return constructStoredFile(bytes, filename, path, guessContentType(filename));
    }

    /**
     * Construye un archivo descargado con los bytes, nombre, ruta y tipo de contenido
     *
     * @param bytes Contenido del archivo
     * @param filename Nombre del archivo
     * @param path Ruta del archivo
     * @param contentType Tipo de contenido
     * @return Archivo descargado
     */
    public static Stored constructStoredFile(byte[] bytes, String filename, String path, String contentType) {
        return constructStoredFile(bytes, bytes.length, filename, path, contentType);
    }

    /**
     * Construye un archivo descargado con los bytes, tamaño, nombre, ruta y tipo de contenido
     *
     * @param bytes Contenido del archivo
     * @param fileSize Tamaño del archivo
     * @param filename Nombre del archivo
     * @param path Ruta del archivo
     * @param contentType Tipo de contenido
     * @return Archivo descargado
     */
    public static Stored constructStoredFile(byte[] bytes, long fileSize, String filename, String path, String contentType) {
        return Stored.builder()
                .content(bytes)
                .info(constructFileInfo(filename, fileSize, path, contentType))
                .build();
    }

    /**
     * Construye la información de un archivo almacenado con el nombre, tamaño y ruta, el tipo de contenido se detecta automáticamente con el nombre del archivo
     * @param filename Nombre del archivo
     * @param fileSize Tamaño del archivo
     * @param path Ruta del archivo
     * @return Información del archivo almacenado
     */
    public static Stored.Info constructFileInfo(String filename, long fileSize, String path) {
        return constructFileInfo(filename, fileSize, path, guessContentType(filename));
    }

    /**
     * Construye la información de un archivo almacenado con el nombre, tamaño, ruta y tipo de contenido
     * @param filename Nombre del archivo
     * @param fileSize Tamaño del archivo
     * @param path Ruta del archivo
     * @param contentType Tipo de contenido
     * @return Información del archivo almacenado
     */
    public static Stored.Info constructFileInfo(String filename, long fileSize, String path, String contentType) {

        return Stored.Info.builder()
                .filename(filename)
                .fileSize(fileSize)
                .path(path)
                .contentType(contentType)
                .build();

    }

    /**
     * Detecta el tipo de contenido de un archivo a partir de su nombre
     * @param filename Nombre del archivo
     * @return Tipo de contenido
     */
    public static String guessContentType(String filename) {
        return TIKA.detect(filename);
    }
}
