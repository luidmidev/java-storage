package io.github.luidmidev.storage.core;

import lombok.EqualsAndHashCode;
import lombok.Getter;

import java.io.IOException;
import java.io.InputStream;

@Getter
@EqualsAndHashCode(callSuper = true)
public class ToStore extends PathFile {

    private final byte[] content;

    public ToStore(String path, String filename, byte[] content) {
        super(path, filename);
        if (content == null || content.length == 0) {
            throw new IllegalArgumentException("Content is required");
        }
        this.content = content;
    }

    public ToStore(String filename, byte[] content) {
        this("", filename, content);
    }

    public ToStore(String path, String filename, InputStream content) throws IOException {
        this(path, filename, content.readAllBytes());
    }

    public ToStore(String filename, InputStream content) throws IOException {
        this("", filename, content);
    }
}
