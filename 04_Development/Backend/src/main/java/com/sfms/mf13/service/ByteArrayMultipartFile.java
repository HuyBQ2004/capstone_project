package com.sfms.mf13.service;

import org.springframework.lang.NonNull;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Bọc {@code byte[]} thành {@link MultipartFile} — {@code StorageService.store(key, file)}
 * (D10) chỉ nhận {@link MultipartFile}, nhưng renderer (PDFBox/POI) sinh trực tiếp {@code
 * byte[]} trong bộ nhớ, không đi qua request HTTP. KHÔNG có tương đương sẵn có trên classpath
 * chính (chỉ {@code MockMultipartFile} của {@code spring-test}, phạm vi test).
 */
public class ByteArrayMultipartFile implements MultipartFile {

    private final String name;
    private final String contentType;
    private final byte[] content;

    public ByteArrayMultipartFile(String name, String contentType, byte[] content) {
        this.name = name;
        this.contentType = contentType;
        this.content = content == null ? new byte[0] : content;
    }

    @Override
    @NonNull
    public String getName() {
        return name;
    }

    @Override
    public String getOriginalFilename() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public boolean isEmpty() {
        return content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    @NonNull
    public byte[] getBytes() {
        return content;
    }

    @Override
    @NonNull
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(@NonNull Path dest) throws IOException, IllegalStateException {
        Files.write(dest, content);
    }

    @Override
    public void transferTo(@NonNull java.io.File dest) throws IOException, IllegalStateException {
        try (OutputStream out = Files.newOutputStream(dest.toPath())) {
            out.write(content);
        }
    }
}
