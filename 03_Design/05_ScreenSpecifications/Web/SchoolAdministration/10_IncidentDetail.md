# Screen Spec — Incident Detail

**Actor**: School Administration | **UC liên quan**: UC-116 (Report 3 §3.2 "View Food Safety Incident Detail") | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023

> **Cập nhật sau review (mục 6, `00_Team_Work_Assignment_Plan.md`)**: `14_Screen_Authorization_Matrix.md` đánh dấu "Incident Detail" có cột `x` cho cả School Admin/Teacher/Medical Staff, nhưng đối chiếu thực tế phạm vi giao việc (mục 2 của Assignment Plan, Teacher cố định 13 màn/62 file) thì **Teacher không có file "Incident Detail" riêng** — Teacher tiếp cận thông tin sự cố qua `Mobile(Web)/Teacher/13_IncidentHistory.md` (chỉ đọc, giới hạn báo cáo do chính giáo viên đó tạo, không phải view quản trị đầy đủ này). Ghi nhận đây là điểm cần team xác nhận: hoặc bổ sung 1 file Incident Detail thu gọn cho Teacher sau này, hoặc coi Incident History là đủ đáp ứng cột `x` của Teacher trong ma trận. Màn hình này **thực tế chỉ dùng chung 2 actor**: School Administration (file này) + Medical Staff (`Web/MedicalStaff/26_IncidentDetail.md`) — đã hợp nhất phần field dữ liệu với bản Medical Staff, bổ sung "Suspected food item + meal time" và "First aid administered" (trước đây thiếu ở bản này).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Triệu chứng báo cáo | Text/List hiển thị | — | — | |
| 2 | Danh sách học sinh bị ảnh hưởng | Table | — | BR-146 (ngưỡng ≥3 học sinh/24h tự động kích hoạt điều tra) | |
| 3 | Suspected food item + meal time (món ăn nghi vấn + thời điểm ăn) | Text hiển thị | — | BR-146, `suspectedDishIds` (class diagram) | Hợp nhất từ bản Medical Staff |
| 4 | Tường trình ban đầu (trước đây "First aid administered", đổi 11/07/2026 — xem `review/medical_staff/review_medicalstaff_incidentmanagement.md` Mục 9.12) | Text hiển thị (read-only phía School Administration) | — | — | Hợp nhất từ bản Medical Staff — mô tả diễn biến ban đầu, không chỉ sơ cứu y tế |
| 5 | Trạng thái điều tra | Badge (info=Investigating, danger=Active, success=Closed) | — | — | |
| 6 | Bản đồ truy xuất nguồn gốc (nếu có) | Panel/link | Biên soạn trong 4 giờ | BR-151 | |
| 7 | Danh sách lô hàng bị Quarantine | Table rút gọn | — | BR-149 | |
| 8 | Nút "Xem CAPA / Corrective Action Review" | Button/Link (hành động riêng School Administration) | — | BR-152, BR-153 | Mở `Corrective Action Review` |
| 9 | Nút "Export" | Button | — | — | AF1 UC-116 |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: MSG-004/MSG-005/MSG-023.
- Success: Chi tiết sự cố hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident List` (click 1 dòng). Điều hướng ra: `Corrective Action Review`.
**Bố cục**: Dashboard Shell — Page Header (mô tả tóm tắt + badge trạng thái + nút Export), Content 2 cột: triệu chứng + học sinh ảnh hưởng (`col-span-8`), bản đồ truy xuất + lô Quarantine (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident Detail (bản School Administration), nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (badge trạng thái + nút Export), 2 cột: triệu chứng/học sinh ảnh hưởng, bản đồ truy xuất/lô Quarantine.
Thành phần bắt buộc: triệu chứng, bảng học sinh ảnh hưởng, món ăn nghi vấn + thời điểm ăn, sơ cứu đã thực hiện, badge trạng thái điều tra, bản đồ truy xuất, bảng lô Quarantine, nút Xem CAPA, nút Export.
Áp dụng Design System: badge critical/danger cho Quarantine/High severity.
Trạng thái cần thể hiện: success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4573–4593 (Table 3-140 View Food Safety Incident Detail Use Cases Specification)
- traceability_matrix.md §1.11 (UC-116)
- business_rules.md BR-146, BR-149, BR-151
