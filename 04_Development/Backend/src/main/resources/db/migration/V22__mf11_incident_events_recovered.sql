-- M11-D20 (Bước 3, 04/08/2026) — BR-155 (C7): xác nhận học sinh bị ảnh hưởng đã hồi phục
-- là 1 trong 3 điều kiện gate đóng sự cố (closure-readiness/close), nhưng chưa có endpoint
-- nào ghi được `incident_affected_students.recovered` — thêm hành động
-- POST /incidents/{code}/recovered-students, cần thêm 1 giá trị sự kiện mới vào sổ.
-- V20 đã merge (Bước 0 quy ước) ⇒ không sửa CHECK cũ tại chỗ, ALTER thêm giá trị.
ALTER TABLE incident_events DROP CONSTRAINT incident_events_type_check;
ALTER TABLE incident_events ADD CONSTRAINT incident_events_type_check CHECK (type IN (
  'REPORTED','SEVERITY_SET','QUARANTINE','RELEASE','SAMPLE_SENT','TRACE_READY',
  'CAPA_SUBMITTED','CAPA_APPROVED','CAPA_EVIDENCE','CAPA_VALIDATED','REPORT_SENT','CLOSED',
  'STUDENT_RECOVERED'
));
