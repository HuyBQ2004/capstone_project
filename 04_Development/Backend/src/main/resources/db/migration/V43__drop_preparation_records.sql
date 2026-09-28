-- Bỏ hẳn "Sổ chế biến" (MF06 UC-066, màn `preparation-log`) — xác nhận 06/08/2026: có backend
-- thật (V10/V11) nhưng hoàn toàn cô lập, không service nào khác (kể cả TraceabilityService) đọc
-- bảng này để dùng cho truy vết/báo cáo. Bản thân spec thiết kế của nó
-- (03_Design/05_ScreenSpecifications/Web/ServiceCaterers/19_RecordPreparationInformationForm.md)
-- ghi rõ "Không có BR trực tiếp". Trùng nội dung nghiệp vụ với Bước 2 (Mẫu số 2 — chế biến) của
-- Kiểm thực ba bước, vốn có căn cứ pháp lý thật (QĐ 1246/QĐ-BYT, BR-090/BR-083) — bản kiểm thực
-- mới là nguồn có giá trị.
-- `preparation_batches` (V10) là bảng nối riêng cho preparation_records, KHÔNG có entity/
-- service/controller Java nào (kiểm 06/08/2026) — mồ côi ngay từ đầu, dọn cùng đợt.
DROP TABLE IF EXISTS preparation_batches;
DROP TABLE IF EXISTS preparation_records;
