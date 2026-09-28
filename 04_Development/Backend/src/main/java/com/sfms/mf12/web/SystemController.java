package com.sfms.mf12.web;

import com.sfms.mf12.dto.SystemDtos.BackupRecord;
import com.sfms.mf12.dto.SystemDtos.BackupsResponse;
import com.sfms.mf12.dto.SystemDtos.SystemActivitiesHistoryResponse;
import com.sfms.mf12.dto.SystemDtos.SystemActivitiesResponse;
import com.sfms.mf12.dto.SystemDtos.SystemStatusResponse;
import com.sfms.mf12.dto.SystemDtos.UsageStatisticsResponse;
import com.sfms.mf12.service.BackupService;
import com.sfms.mf12.service.SystemMetricsService;
import com.sfms.mf12.service.SystemStatusService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/** MF12 — {@code system} (UC-057/149/150 + BR-160/161). ADR-M12-04: metric từ JVM/DB pool. */
@Tag(name = "MF12 — AI & Supporting Services")
@RestController
@RequestMapping("/api/v1/system")
@PreAuthorize("hasRole('SYSTEM_ADMIN')")
public class SystemController {

    private final SystemMetricsService systemMetricsService;
    private final SystemStatusService systemStatusService;
    private final BackupService backupService;

    public SystemController(SystemMetricsService systemMetricsService, SystemStatusService systemStatusService,
                             BackupService backupService) {
        this.systemMetricsService = systemMetricsService;
        this.systemStatusService = systemStatusService;
        this.backupService = backupService;
    }

    @Operation(summary = "Hoạt động hệ thống", description = "UC-057 · BR-165/170")
    @GetMapping("/activities")
    public SystemActivitiesResponse activities() {
        return systemMetricsService.activities();
    }

    @Operation(summary = "Lịch sử CPU/heap/DB pool", description = "UC-149 — biểu đồ monitoring, V36")
    @GetMapping("/activities/history")
    public SystemActivitiesHistoryResponse activitiesHistory(@RequestParam(defaultValue = "7") int days) {
        return systemMetricsService.activitiesHistory(days);
    }

    @Operation(summary = "Trạng thái hệ thống", description = "UC-149 · BR-165 — L14/CF-10: một nguồn duy nhất")
    @GetMapping("/status")
    public SystemStatusResponse status() {
        return systemStatusService.healthChecks();
    }

    @Operation(summary = "Thống kê sử dụng", description = "UC-150 · BR-165")
    @GetMapping("/usage")
    public UsageStatisticsResponse usage(@RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                                         @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return systemMetricsService.usage(from, to);
    }

    @Operation(summary = "Lịch sử sao lưu", description = "BR-160/161")
    @GetMapping("/backups")
    public BackupsResponse backups() {
        return systemStatusService.backups();
    }

    @Operation(summary = "Chạy sao lưu ngay", description = "BR-160/161 — pg_dump đồng bộ, chạy song song với "
            + "scheduler 02:00 hàng ngày")
    @PostMapping("/backups/run")
    @ResponseStatus(HttpStatus.CREATED)
    public BackupRecord runBackup() {
        return backupService.runBackup();
    }

    @Operation(summary = "Tải xuống backup (đã giải mã)", description = "BR-160/161 — dùng để pg_restore/kiểm "
            + "tra khả năng khôi phục")
    @GetMapping("/backups/{code}/download")
    public ResponseEntity<ByteArrayResource> download(@PathVariable String code) {
        byte[] content = backupService.downloadDecrypted(code);
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        ContentDisposition.attachment().filename(code + ".dump").build().toString())
                .body(new ByteArrayResource(content));
    }
}
