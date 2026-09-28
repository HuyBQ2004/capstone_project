package com.sfms.common.storage;

/** Lỗi lưu/đọc file ở tầng {@link StorageService} — unchecked, tránh boilerplate try/catch ở service gọi. */
public class StorageException extends RuntimeException {

    public StorageException(String message) {
        super(message);
    }

    public StorageException(String message, Throwable cause) {
        super(message, cause);
    }
}
