

-- ───────────────────────── BR-142 (ADR-M10-6) — 10 từ khóa bệnh lý của mock ─────────────────────────
INSERT INTO illness_keywords (keyword, active) VALUES
  ('đau bụng', TRUE), ('nôn mửa', TRUE), ('tiêu chảy', TRUE), ('dị ứng', TRUE),
  ('phát ban', TRUE), ('sốt', TRUE), ('buồn nôn', TRUE), ('khó thở', TRUE),
  ('nổi mẩn', TRUE), ('ngộ độc', TRUE);

INSERT INTO code_sequences (scope, next_value) VALUES ('mf10_feedback:2026', 8810)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, 8810);

INSERT INTO code_sequences (scope, next_value) VALUES ('mf10_announcement:2026', 114)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, 114);

INSERT INTO code_sequences (scope, next_value) VALUES ('mf10_notification:2026', 482914)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, 482914);
INSERT INTO code_sequences (scope, next_value) VALUES ('mf10_feedback_response:2026', 1)
ON CONFLICT (scope) DO UPDATE SET next_value = GREATEST(code_sequences.next_value, 1);
