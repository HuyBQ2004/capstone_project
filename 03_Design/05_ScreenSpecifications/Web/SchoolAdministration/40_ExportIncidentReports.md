# Screen Spec — Export Incident Reports

**Actor**: School Administration | **UC liên quan**: UC-031 (Report 3 §3.2 "Export Incident Reports") | **Status**: Draft
**BR liên quan**: BR-146, BR-147, BR-148, BR-149, BR-150, BR-151, BR-154, BR-155, BR-178 (định dạng PDF/Excel)
**MSG liên quan**: MSG-004, MSG-005, MSG-023

> **Ghi chú số thứ tự**: Màn hình này không có dòng riêng trong bảng gốc `14_Screen_Authorization_Matrix.md` khối 3 (chỉ có ghi chú cuối bảng: *"Export Incident Reports — UC-031 — không có dòng riêng trong Screen Authorization gốc nhưng có UC/Screen Spec chính thức"*). Theo `00_Team_Work_Assignment_Plan.md` §1 (đánh số theo đúng thứ tự xuất hiện trong ma trận), màn hình không có dòng gốc được đặt số thứ tự **40** — nối tiếp sau 39 màn hình có dòng chính thức, trước khối Academic Year/Violation Management. Cần team xác nhận cách đánh số này khi rà soát (mục 6).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date Range | DatePicker (range) | — | BR-177 | |
| 2 | Filter Severity Level | Select | — | BR-147 | |
| 3 | Filter Incident Status | Select | — | — | |
| 4 | Filter Suspected Service Caterers | Select | — | — | |
| 5 | Nút "Generate Preview" | Button | — | — | Hiển thị preview danh sách khớp filter trước khi export |
| 6 | Bảng Preview | Table | — | — | |
| 7 | Chọn định dạng export (PDF/Excel) | RadioGroup | — | BR-178 | |
| 8 | Nút "Export" | Button (primary) | — | BR-154 | Sinh file kèm Incident ID, symptoms, affected counts, root cause, CAPA status |

### Trạng thái màn hình
- Loading: Button "Generate Preview"/"Export" loading.
- Empty: "Không có sự cố nào khớp bộ lọc".
- Error: MSG-004/MSG-005/MSG-023.
- Success: MSG-045 (theo Normal Flow bước 6 — tải file, ghi log giao dịch export).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Dashboard` (nút "Export Incident Reports"). Không có màn hình con.
**Bố cục**: Dashboard Shell — filter bar trên cùng, nút "Generate Preview" cạnh filter, bảng preview giữa trang, radio chọn định dạng + nút Export cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Export Incident Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter bar (Date/Severity/Status/Caterer), nút Generate Preview, bảng preview, radio định dạng PDF/Excel, nút Export.
Thành phần bắt buộc: 4 filter, nút Generate Preview, bảng preview, radio định dạng, nút Export.
Áp dụng Design System: không cần badge đặc biệt.
Trạng thái cần thể hiện: empty, success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4375–4395 (Table 3-131 Export Incident Reports Use Cases Specification)
- traceability_matrix.md §1.11 (UC-031 ↔ BR-154)
- 14_Screen_Authorization_Matrix.md (ghi chú cuối khối 3 về UC-031)
