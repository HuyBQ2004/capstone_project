package com.sfms.mf12.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.storage.StorageService;
import com.sfms.mf12.domain.Backup;
import com.sfms.mf12.domain.BackupStatus;
import com.sfms.mf12.dto.SystemDtos.BackupRecord;
import com.sfms.mf12.dto.SystemDtos.BackupsResponse;
import com.sfms.mf12.error.AisExceptions;
import com.sfms.mf12.repository.BackupRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URI;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.Year;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

/**
 * BR-160/161 — chạy {@code pg_dump} thật (custom format {@code -F c}), mã hoá AES-256-GCM
 * (cùng khoá {@code SFMS_CRED_MASTER_KEY} với {@link CredentialCipher}), rồi lưu qua
 * {@link StorageService} — {@code STORAGE_PROVIDER=r2} (ADR-001) ⇒ backup THẬT off-site trên
 * Cloudflare R2, {@code =local} (ADR-000, dev) ⇒ vẫn local disk nhưng đã mã hoá.
 *
 * <p>{@code pg_dump} là tiến trình con — BẮT BUỘC ghi ra file cục bộ trước (không thể pipe
 * thẳng lên R2), nên vẫn cần {@code sfms.backup.dir} làm nơi trung gian; file plaintext đó bị
 * xoá NGAY sau khi mã hoá+upload xong (kể cả khi bước sau lỗi — {@code finally}).
 *
 * <p>Định dạng file lưu: 12 byte IV nối trước ciphertext (không cần cột IV riêng trong DB).
 *
 * <p>KHÔNG {@code @Transactional} bọc quanh {@code pg_dump}/upload R2 — có thể chạy vài phút,
 * giữ 1 connection Hikari suốt thời gian đó là lãng phí không cần thiết; 2 lần
 * {@code saveAndFlush} (trước/sau) tự có transaction ngắn riêng của Spring Data JPA.
 */
@Service
public class BackupServiceImpl implements BackupService {

    private static final Logger log = LoggerFactory.getLogger(BackupServiceImpl.class);
    private static final long TIMEOUT_MINUTES = 10;
    private static final int IV_LENGTH = 12;

    private final BackupRepository backupRepository;
    private final CodeGenerator codeGenerator;
    private final CredentialCipher credentialCipher;
    private final StorageService storageService;
    private final Clock clock;
    private final String dbUrl;
    private final String dbUser;
    private final String dbPassword;
    private final Path tempDir;
    private final long capacityBytes;

    public BackupServiceImpl(BackupRepository backupRepository, CodeGenerator codeGenerator,
                              CredentialCipher credentialCipher, StorageService storageService, Clock clock,
                              @Value("${spring.datasource.url}") String dbUrl,
                              @Value("${spring.datasource.username}") String dbUser,
                              @Value("${spring.datasource.password}") String dbPassword,
                              @Value("${sfms.backup.dir:./data/backups}") String backupDirPath,
                              @Value("${sfms.backup.capacity-bytes:5368709120}") long capacityBytes) {
        this.backupRepository = backupRepository;
        this.codeGenerator = codeGenerator;
        this.credentialCipher = credentialCipher;
        this.storageService = storageService;
        this.clock = clock;
        this.dbUrl = dbUrl;
        this.dbUser = dbUser;
        this.dbPassword = dbPassword;
        this.tempDir = Path.of(backupDirPath).toAbsolutePath().normalize();
        this.capacityBytes = capacityBytes;
        try {
            Files.createDirectories(this.tempDir);
        } catch (IOException e) {
            throw new IllegalStateException("Không tạo được thư mục tạm backup: " + this.tempDir, e);
        }
    }

    @Override
    @Transactional(readOnly = true)
    public byte[] downloadDecrypted(String code) {
        Backup backup = backupRepository.findByCode(code).orElseThrow(AisExceptions::notFound);
        if (backup.getStatus() != BackupStatus.SUCCESS || backup.getFilePath() == null) {
            throw AisExceptions.notFound();
        }
        byte[] encoded;
        try {
            encoded = storageService.load(backup.getFilePath()).getInputStream().readAllBytes();
        } catch (IOException e) {
            throw new IllegalStateException("Không đọc được file backup: " + backup.getFilePath(), e);
        }
        byte[] iv = Arrays.copyOfRange(encoded, 0, IV_LENGTH);
        byte[] ciphertext = Arrays.copyOfRange(encoded, IV_LENGTH, encoded.length);
        return credentialCipher.decryptBytes(ciphertext, iv);
    }

    @Override
    @Transactional(readOnly = true)
    public BackupsResponse list() {
        List<BackupRecord> records = backupRepository.findAllByOrderByStartedAtDesc().stream()
                .map(BackupServiceImpl::toRecord).toList();
        long totalBytes = records.stream().mapToLong(BackupRecord::sizeBytes).sum();
        double pct = capacityBytes > 0 ? Math.min(100.0, (totalBytes * 100.0) / capacityBytes) : 0.0;
        return new BackupsResponse(records, pct);
    }

    @Override
    public BackupRecord runBackup() {
        Instant startedAt = clock.instant();
        Backup backup = new Backup();
        int year = Year.now(clock).getValue();
        backup.setCode(codeGenerator.next("mf12_backups:" + year, "BKP-" + year, 4));
        backup.setStartedAt(startedAt);
        backup.setStatus(BackupStatus.RUNNING);
        backup = backupRepository.saveAndFlush(backup);

        Path plaintextFile = tempDir.resolve(backup.getCode() + "-" + UUID.randomUUID() + ".dump.tmp");
        try {
            dumpToFile(plaintextFile);

            byte[] plaintext = Files.readAllBytes(plaintextFile);
            CredentialCipher.Encrypted encrypted = credentialCipher.encryptBytes(plaintext);
            byte[] encoded = ByteBuffer.allocate(IV_LENGTH + encrypted.ciphertext().length)
                    .put(encrypted.iv()).put(encrypted.ciphertext()).array();

            String key = "backups/" + backup.getCode() + ".dump.enc";
            storageService.store(key, encoded, "application/octet-stream");

            backup.setFinishedAt(clock.instant());
            backup.setStatus(BackupStatus.SUCCESS);
            backup.setSizeBytes(encoded.length);
            backup.setFilePath(key);
        } catch (DumpFailedException e) {
            backup.setFinishedAt(clock.instant());
            backup.setStatus(BackupStatus.FAILED);
            backup.setErrorMessage(truncate(e.getMessage()));
            log.error("Backup {} — pg_dump thất bại: {}", backup.getCode(), e.getMessage());
        } catch (IOException | RuntimeException e) {
            backup.setFinishedAt(clock.instant());
            backup.setStatus(BackupStatus.FAILED);
            backup.setErrorMessage(truncate(e.getMessage()));
            log.error("Backup {} lỗi khi mã hoá/upload", backup.getCode(), e);
        } finally {
            try {
                Files.deleteIfExists(plaintextFile);
            } catch (IOException e) {
                log.warn("Không xoá được file plaintext tạm {}: {}", plaintextFile, e.getMessage());
            }
        }
        backup = backupRepository.saveAndFlush(backup);
        return toRecord(backup);
    }

    private void dumpToFile(Path target) throws IOException {
        try {
            ConnInfo conn = parseJdbcUrl(dbUrl);
            ProcessBuilder pb = new ProcessBuilder("pg_dump",
                    "-h", conn.host(), "-p", String.valueOf(conn.port()),
                    "-U", dbUser, "-d", conn.database(), "-F", "c", "-f", target.toString());
            pb.environment().put("PGPASSWORD", dbPassword);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            ByteArrayOutputStream outBuf = new ByteArrayOutputStream();
            process.getInputStream().transferTo(outBuf);
            boolean finished = process.waitFor(TIMEOUT_MINUTES, TimeUnit.MINUTES);
            int exitCode = finished ? process.exitValue() : -1;

            if (!finished) {
                process.destroyForcibly();
                throw new DumpFailedException("pg_dump timeout sau " + TIMEOUT_MINUTES + " phút");
            }
            if (exitCode != 0 || !Files.exists(target)) {
                throw new DumpFailedException(outBuf.toString(StandardCharsets.UTF_8));
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new DumpFailedException("pg_dump bị interrupt: " + e.getMessage());
        }
    }

    private static final class DumpFailedException extends RuntimeException {
        DumpFailedException(String message) {
            super(message);
        }
    }

    private static String truncate(String s) {
        if (s == null) return "unknown error";
        return s.length() > 500 ? s.substring(0, 500) : s;
    }

    /** {@code jdbc:postgresql://host:port/dbname} → host/port/dbname cho tham số dòng lệnh pg_dump. */
    private record ConnInfo(String host, int port, String database) {}

    private static ConnInfo parseJdbcUrl(String jdbcUrl) {
        URI uri = URI.create(jdbcUrl.substring("jdbc:".length()));
        String database = uri.getPath().startsWith("/") ? uri.getPath().substring(1) : uri.getPath();
        int port = uri.getPort() > 0 ? uri.getPort() : 5432;
        return new ConnInfo(uri.getHost(), port, database);
    }

    private static BackupRecord toRecord(Backup b) {
        return new BackupRecord(b.getCode(), b.getStartedAt(), b.getFinishedAt(), b.getStatus().name(), b.getSizeBytes());
    }
}
