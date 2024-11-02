package io.github.luidmidev.storage;

import lombok.Builder;
import lombok.Data;

/**
 * Representa un archivo descargado
 */
@Data
@Builder
public class Stored {

    private byte[] content;
    private Info info;

    /**
     * Representa la información de un archivo almacenado
     */
    @Data
    @Builder
    public static class Info {
        private String path;
        private String filename;
        private String contentType;
        private Long fileSize;
    }
}