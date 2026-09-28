package com.sfms.mf04.port;

import com.sfms.common.enums.PartnerType;

import java.util.UUID;

/**
 * Hồ sơ tối thiểu của {@code partners} cho module khác đọc (ADR-M04-7) — KHÔNG import
 * entity/repository của {@code mf04} (ADR-M02-5/AP-AI-005 áp dụng chéo module).
 */
public record PartnerSummary(UUID id, String code, String name, PartnerType type, String status) {
}
