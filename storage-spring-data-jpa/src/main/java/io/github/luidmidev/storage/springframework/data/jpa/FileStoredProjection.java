package io.github.luidmidev.storage.springframework.data.jpa;

public interface FileStoredProjection {

    Long getContentLength();

    String getContentType();

    String getOriginalFileName();

    String getPath();
}
