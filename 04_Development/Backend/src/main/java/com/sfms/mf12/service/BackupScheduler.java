package com.sfms.mf12.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/** BR-160/161 — sao lưu CSDL tự động hàng ngày 02:00 (giờ server). */
@Component
public class BackupScheduler {

    private static final Logger log = LoggerFactory.getLogger(BackupScheduler.class);

    private final BackupService backupService;

    public BackupScheduler(BackupService backupService) {
        this.backupService = backupService;
    }

    @Scheduled(cron = "${sfms.backup.cron:0 0 2 * * *}")
    public void runScheduledBackup() {
        var record = backupService.runBackup();
        log.info("Backup định kỳ {} — status={} size={}B", record.id(), record.status(), record.sizeBytes());
    }
}
