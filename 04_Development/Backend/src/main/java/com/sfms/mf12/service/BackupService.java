package com.sfms.mf12.service;

import com.sfms.mf12.dto.SystemDtos.BackupRecord;
import com.sfms.mf12.dto.SystemDtos.BackupsResponse;

/** BR-160/161 — sao lưu CSDL thật (pg_dump + mã hoá + R2/local qua StorageService). */
public interface BackupService {

    BackupsResponse list();

    /** Chạy đồng bộ — chấp nhận block vài giây/phút, khối lượng DB dev/demo nhỏ. */
    BackupRecord runBackup();

    /** Tải + giải mã file backup — dùng để phục hồi (pg_restore) hoặc kiểm tra khả năng khôi phục. */
    byte[] downloadDecrypted(String code);
}
