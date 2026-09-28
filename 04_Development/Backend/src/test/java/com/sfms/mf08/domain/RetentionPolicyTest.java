package com.sfms.mf08.domain;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF08-TC-008 — BR-111 (sửa 26/07/2026): hạn lưu tối thiểu là 24 GIỜ, không phải 72h
 * (CF-M08-1). Hằng số duy nhất: {@link RetentionPolicy#MIN_RETENTION_HOURS}.
 *
 * <p>⚠️ Chống tautology (Red Gate §5.1, mục 3): assert bằng giá trị TUYỆT ĐỐI
 * ({@code Instant.parse(...)}), không tính lại bằng chính hằng số đang kiểm — nếu ai đó lỡ
 * đổi {@code MIN_RETENTION_HOURS} về 72, test này phải đỏ ngay chứ không tự điều chỉnh theo.
 */
class RetentionPolicyTest {

    @Test
    void minimumRetentionIsExactly24Hours() {
        assertThat(RetentionPolicy.MIN_RETENTION_HOURS).isEqualTo(24L);
    }

    @Test
    void deadlineIsAbsoluteTwentyFourHoursAfterCollection() {
        Instant collectedAt = Instant.parse("2026-07-06T03:15:00Z");
        Instant deadline = collectedAt.plus(RetentionPolicy.MIN_RETENTION_HOURS, ChronoUnit.HOURS);

        // Giá trị tuyệt đối, không suy ngược từ hằng số — khoá cứng CF-M08-1.
        assertThat(deadline).isEqualTo(Instant.parse("2026-07-07T03:15:00Z"));
        assertThat(deadline).isNotEqualTo(collectedAt.plus(72, ChronoUnit.HOURS));
    }

    @Test
    void noSeventyTwoHourConstantAnywhereInPolicy() {
        // AP-AI-011 (Stale 72h) — DoD exit criteria đòi grep "72" gắn hạn lưu = no output.
        assertThat(RetentionPolicy.MIN_RETENTION_HOURS).isNotEqualTo(72L);
    }
}
