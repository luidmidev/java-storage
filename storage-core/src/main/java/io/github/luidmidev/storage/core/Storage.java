package io.github.luidmidev.storage.core;

import io.github.luidmidev.storage.core.exceptions.*;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Interfaz que define las operaciones básicas de un almacen de archivos.
 */
@Slf4j
public abstract class Storage {


    /**
     * @param content  Contenido del archivo
     * @param filename Nombre del archivo
     * @param path     Ruta donde se almacenará el archivo
     * @throws IOException Si ocurre un error de lectura o escritura al almacenar el archivo
     */
    protected abstract void internalStore(byte[] content, String filename, String path) throws IOException;

    /**
     * Descarga un archivo almacenado a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @return Objeto que representa el archivo almacenado
     * @throws IOException Si ocurre un error de lectura o escritura al descargar el archivo
     */
    protected abstract Optional<Stored> internalDownload(String filename, String path) throws IOException;

    /**
     * Obtiene la información de un archivo almacenado a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @return Objeto que representa la información del archivo almacenado
     * @throws IOException Si ocurre un error de lectura o escritura al obtener la información del archivo
     */
    protected abstract Optional<Stored.Info> internalInfo(String filename, String path) throws IOException;

    /**
     * Verifica si un archivo almacenado existe a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @return Si el archivo existe o no
     * @throws IOException Si ocurre un error de lectura o escritura al verificar la existencia del archivo
     */
    protected abstract boolean internalExists(String filename, String path) throws IOException;

    /**
     * Elimina un archivo almacenado a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @throws IOException Si ocurre un error de lectura o escritura al eliminar el archivo
     */
    protected abstract void internalRemove(String filename, String path) throws IOException;


    /**
     * Guarda un archivo en el almacen a partir de un input stream en la ruta raíz
     *
     * @param content  Contenido del archivo
     * @param filename Nombre del archivo
     * @throws IOException                     Si ocurre un error de lectura o escritura al almacenar el archivo
     * @throws InvalidFileNameStorageException Si el nombre del archivo es inválido
     * @throws InvalidPathStorageException     Si el path es inválido
     */
    public void store(InputStream content, String filename) throws IOException {
        store(content, filename, "/");
    }

    /**
     * Guarda un archivo en el almacen a partir de sus bytes en la ruta raíz
     *
     * @param content  Contenido del archivo
     * @param filename Nombre del archivo
     * @throws IOException                     Si ocurre un error de lectura o escritura al almacenar el archivo
     * @throws InvalidFileNameStorageException Si el nombre del archivo es inválido
     * @throws InvalidPathStorageException     Si el path es inválido
     */
    public void store(byte[] content, String filename) throws IOException {
        store(content, filename, "/");
    }

    /**
     * Guarda un archivo en el almacen a partir de un input stream en un path específico
     *
     * @param content  Contenido del archivo
     * @param filename Nombre del archivo
     * @param path     Ruta donde se almacenará el archivo
     * @throws IOException                     Si ocurre un error de lectura o escritura al almacenar el archivo
     * @throws InvalidFileNameStorageException Si el nombre del archivo es inválido
     * @throws InvalidPathStorageException     Si el path es inválido
     */
    public void store(InputStream content, String filename, String path) throws IOException {
        var bytes = content.readAllBytes();
        store(bytes, filename, path);
    }

    /**
     * Guarda un archivo en el almacen a partir de sus bytes en un path específico
     *
     * @param content  Contenido del archivo
     * @param filename Nombre del archivo
     * @param path     Ruta donde se almacenará el archivo
     * @throws IOException                     Si ocurre un error de lectura o escritura al almacenar el archivo
     * @throws InvalidFileNameStorageException Si el nombre del archivo es inválido
     * @throws InvalidPathStorageException     Si el path es inválido
     */
    public void store(byte[] content, String filename, String path) throws IOException {

        log.info("Storing file {} in path {}", filename, path);

        var normalizedPath = normalizePath(path);

        throwIfInvalidFilename(filename);
        throwIfInvalidPath(normalizedPath);


        if (internalExists(filename, normalizedPath)) {
            throw new AlreadyFileExistsStorageException(filename, normalizedPath);
        }

        internalStore(content, filename, normalizedPath);
    }


    /**
     * Descarga un archivo almacenado a partir de su ruta completa
     *
     * @param fullPath Ruta completa del archivo
     * @return Objeto que representa el archivo almacenado
     * @throws IOException Si ocurre un error de lectura o escritura al descargar el archivo
     */
    public Optional<Stored> download(String fullPath) throws IOException {
        var split = SplitPath.from(fullPath);
        return download(split.filename(), split.path());
    }

    /**
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @return Objeto que representa el archivo almacenado
     * @throws IOException Si ocurre un error de lectura o escritura al descargar el archivo
     */
    public Optional<Stored> download(String filename, String path) throws IOException {
        var normalizedPath = normalizePath(path);
        return internalDownload(filename, normalizedPath);
    }


    /**
     * Obtiene la información de un archivo almacenado a partir de su ruta completa
     *
     * @param fullPath Ruta completa del archivo
     * @return Objeto que representa la información del archivo almacenado
     * @throws IOException Si ocurre un error de lectura o escritura al obtener la información del archivo
     */
    public Optional<Stored.Info> info(String fullPath) throws IOException {
        var split = SplitPath.from(fullPath);
        return info(split.filename(), split.path());
    }

    /**
     * Obtiene la información de un archivo almacenado a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @return Objeto que representa la información del archivo almacenado
     * @throws IOException Si ocurre un error de lectura o escritura al obtener la información del archivo
     */
    public Optional<Stored.Info> info(String filename, String path) throws IOException {
        var normalizedPath = normalizePath(path);
        return internalInfo(filename, normalizedPath);
    }

    /**
     * Verifica si un archivo almacenado existe a partir de su ruta completa
     *
     * @param fullPath Ruta completa del archivo
     * @return Si el archivo existe o no
     * @throws IOException Si ocurre un error de lectura o escritura al verificar la existencia del archivo
     */
    public boolean exists(String fullPath) throws IOException {
        var split = SplitPath.from(fullPath);
        return exists(split.filename(), split.path());
    }

    /**
     * Verifica si un archivo almacenado existe a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @return Si el archivo existe o no
     * @throws IOException Si ocurre un error de lectura o escritura al verificar la existencia del archivo
     */
    public boolean exists(String filename, String path) throws IOException {
        var normalizedPath = normalizePath(path);
        return internalExists(filename, normalizedPath);
    }

    /**
     * Elimina un archivo almacenado a partir de su ruta completa
     *
     * @param fullPath Ruta completa del archivo
     * @throws IOException Si ocurre un error de lectura o escritura al eliminar el archivo
     */
    public void remove(String fullPath) throws IOException {
        var split = SplitPath.from(fullPath);
        remove(split.filename(), split.path());
    }

    /**
     * Elimina un archivo almacenado a partir de su nombre y ruta
     *
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @throws IOException Si ocurre un error de lectura o escritura al eliminar el archivo
     */
    public void remove(String filename, String path) throws IOException {
        var normalizedPath = normalizePath(path);
        internalRemove(filename, normalizedPath);
    }


    /**
     * Elimina los archivos almacenados a partir de un objeto que contiene las referencias a los archivos
     * a eliminar
     *
     * @param purgable Objeto que contiene las referencias a los archivos a eliminar
     * @throws IOException Si ocurre un error de lectura o escritura al eliminar los archivos almacenados a partir del objeto purgable
     */
    public void purge(PurgableStored purgable) throws IOException {
        for (var fullPath : purgable.filesFullPaths()) remove(fullPath);
    }

    /**
     * Elimina los archivos almacenados a partir de una colección de objetos que contienen las referencias a los archivos
     *
     * @param purgables Objetos que contienen las referencias a los archivos a eliminar
     * @throws IOException Si ocurre un error de lectura o escritura al eliminar los archivos almacenados a partir de los objetos purgables
     */
    public void purge(Iterable<? extends PurgableStored> purgables) throws IOException {
        for (PurgableStored purgable : purgables) {
            purge(purgable);
        }
    }


    /**
     * Transfiere un archivo almacenado a otro almacen
     *
     * @param target   Almacen donde se almacenará el archivo
     * @param filename Nombre del archivo
     * @param path     Ruta donde se encuentra el archivo
     * @throws IOException Si ocurre un error de lectura o escritura al transferir el archivo
     */
    public void transferTo(Storage target, String filename, String path) throws IOException {
        var normalizedPath = normalizePath(path);
        var downloadedFile = internalDownload(normalizedPath, path);
        if (downloadedFile.isEmpty()) {
            throw new FileNotFoundStorageException(normalizedPath, filename);
        }
        target.store(downloadedFile.get().getContent(), filename, path);
    }

    /**
     * Transfiere un archivo almacenado a otro almacen
     *
     * @param target   Almacen donde se almacenará el archivo
     * @param fullPath Ruta completa del archivo
     * @throws IOException Si ocurre un error de lectura o escritura al transferir el archivo
     */
    public void transferTo(Storage target, String fullPath) throws IOException {
        var split = SplitPath.from(fullPath);
        transferTo(target, split.filename(), split.path());
    }

    private static final Pattern INVALID_FILENAME_CHARACTERS = Pattern.compile("[\\\\/:*?\"<>|]");
    private static final int MAX_FILENAME_LENGTH = 255;

    /**
     * Lanza una excepción si el nombre del archivo es inválido
     *
     * @param filename Nombre del archivo a validar
     * @throws InvalidFileNameStorageException Si el nombre del archivo es inválido
     */
    private static void throwIfInvalidFilename(String filename) throws InvalidFileNameStorageException {

        if (filename == null || filename.isEmpty()) {
            throw new InvalidFileNameStorageException(filename, "The filename is required.");
        }

        if (filename.length() > MAX_FILENAME_LENGTH) {
            throw new InvalidFileNameStorageException(filename, "The filename is too long, it must be less than " + MAX_FILENAME_LENGTH + " characters.");
        }

        var invalidCharacters = new StringBuilder();
        for (char c : filename.toCharArray()) {
            var charact = String.valueOf(c);
            if (INVALID_FILENAME_CHARACTERS.matcher(charact).find() && invalidCharacters.indexOf(charact) == -1) {
                invalidCharacters.append(c).append(" ");
            }
        }

        if (!invalidCharacters.isEmpty()) {
            throw new InvalidFileNameStorageException(filename, "The filename contains invalid characters: " + invalidCharacters.toString().trim());
        }
    }


    private static final Pattern INVALID_PATH_CHARACTERS = Pattern.compile("[<>:\"|?*\\\\]");

    /**
     * Lanza una excepción si el path es inválido
     *
     * @param path Path a validar
     * @throws StorageException Si el path es inválido
     */
    private static void throwIfInvalidPath(String path) throws InvalidPathStorageException {
        if (path == null || path.isEmpty()) {
            throw new InvalidPathStorageException(path, "The path is required.");
        }

        if (path.equals("/")) return;

        var invalidCharacters = new StringBuilder();
        for (char c : path.toCharArray()) {
            var charact = String.valueOf(c);
            if (INVALID_PATH_CHARACTERS.matcher(charact).find() && invalidCharacters.indexOf(charact) == -1) {
                invalidCharacters.append(c).append(" ");
            }

        }

        if (!invalidCharacters.isEmpty()) {
            throw new InvalidPathStorageException(path, "The path contains invalid characters: " + invalidCharacters.toString().trim());
        }

        validateInPathSegments(path);
    }

    private static void validateInPathSegments(String path) {
        var segments = path.startsWith("/") ? path.substring(1).split("/") : path.split("/");

        for (var segment : segments) {

            if (segment.isEmpty()) {
                throw new InvalidPathStorageException(path, "The path cannot contain empty segments.");
            }

            if (segment.startsWith(" ")) {
                throw new InvalidPathStorageException(path, "The path cannot contain segments starting with spaces.");
            }

            if (segment.endsWith(" ")) {
                throw new InvalidPathStorageException(path, "The path cannot contain segments ending with spaces.");
            }

            if (segment.endsWith(".")) {
                throw new InvalidPathStorageException(path, "The path cannot contain segments ending with a dot.");
            }

            if (segment.chars().allMatch(c -> c == '.')) {
                throw new InvalidPathStorageException(path, "The path cannot contain segments with only dots.");
            }
        }
    }

    /**
     * Obtiene la ruta ideal de un path para almacenar un archivo
     *
     * @param path Path a normalizar
     * @return Path normalizado
     */
    protected static String normalizePath(String path) {
        if (path == null || path.isBlank()) return "/";
        if (path.equals("/")) return path;
        var normalizedPath = path.startsWith("/") ? path : "/" + path;
        return normalizedPath.endsWith("/") ? normalizedPath : normalizedPath + "/";
    }

    protected record SplitPath(String path, String filename) {
        public static SplitPath from(String fullPath) {
            var split = new SplitPath(extractPath(fullPath), extractFilename(fullPath));
            log.debug("Split path: {}", split);
            return split;
        }

        /**
         * Extrae la ruta de un archivo a partir de su ruta completa
         *
         * @param fullpath Ruta completa del archivo
         * @return Ruta del archivo
         */
        private static String extractPath(String fullpath) {
            return fullpath.contains("/") ? fullpath.substring(0, fullpath.lastIndexOf("/") + 1) : "/";
        }

        /**
         * Extrae el nombre de un archivo a partir de su ruta completa
         *
         * @param fullpath Ruta completa del archivo
         * @return Nombre del archivo
         */
        private static String extractFilename(String fullpath) {
            return fullpath.contains("/") ? fullpath.substring(fullpath.lastIndexOf("/") + 1) : fullpath;
        }

        @Override
        public String toString() {
            return "[path= " + path + ", filename= " + filename + "]";
        }
    }
}

