# Screen Spec — Incident List

**Actor**: School Administration | **UC liên quan**: UC-114 (Report 3 §3.2 "View Food Poisoning Incident List") | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155
**MSG liên quan**: MSG-004, MSG-005, MSG-023 (báo cáo sự cố thiếu thông tin)

> **Đã đối chiếu hợp nhất (mục 6, `00_Team_Work_Assignment_Plan.md`)** với `Web/MedicalStaff/25_IncidentList.md`: 2 bản cùng phản ánh đủ dữ liệu cốt lõi (ngày/severity/status/số học sinh ảnh hưởng/export) — không phát hiện field dữ liệu bị thiếu ở bản nào, chỉ khác cách trình bày bộ lọc (School Administration dùng Select Status; Medical Staff dùng 2 Tab Processing/Resolved) — giữ nguyên vì đây là lựa chọn UI/layout, không phải khác biệt dữ liệu.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter theo Severity Level | Select | — | BR-147 | |
| 2 | Filter theo Status | Select | Active/Under Investigation/Closed | — | |
| 3 | Thanh Search (từ khoá) | Input (search) | — | — | |
| 4 | Bảng danh sách sự cố | Table | Cột: Ngày báo cáo, Mô tả tóm tắt, Severity, Status, Số học sinh ảnh hưởng | — | |
| 5 | Badge Severity | Badge (danger=High, warning=Medium, info=Low) | — | BR-147 | |
| 6 | Action: xem chi tiết | Row click | — | — | Mở `Incident Detail` |
| 7 | Nút "Export" | Button | — | — | AF1 UC-114 |

### Trạng thái màn hình
- Loading: Skeleton table.
- Empty: "Không có sự cố nào khớp bộ lọc".
- Error: MSG-004/MSG-005/MSG-023.
- Success: Bảng hiển thị đầy đủ, Export tải file CSV/PDF (AF1).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Dashboard`. Điều hướng ra: `Incident Detail`.
**Bố cục**: Dashboard Shell — filter bar (Severity, Status, search) trên bảng `Table`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Incident List, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Severity/Status/search), bảng dữ liệu (Ngày/Mô tả/Severity/Status/Số HS ảnh hưởng).
Thành phần bắt buộc: filter, bảng, badge severity, action xem chi tiết, nút Export.
Áp dụng Design System: badge danger/warning/info theo severity.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4555–4571 (Table 3-139 View Food Poisoning Incident List Use Cases Specification)
- traceability_matrix.md §1.11 (UC-114)
