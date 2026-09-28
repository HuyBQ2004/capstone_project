# Screen Spec — Traceability Reports

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard + UC-172 - Export Report (Report 3.md §3.2.13.1–3.2.13.2, lọc theo truy xuất nguồn gốc) | **Status**: Draft
**BR liên quan**: BR-076 (truy xuất lô nguyên liệu), BR-151 (bản đồ truy xuất trong 4 giờ khi có sự cố), BR-175, BR-176, BR-178
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171/172 nay đã có Functional Specification đầy đủ trong Report 3.md. Màn hình này là 1 view lọc theo danh mục "Traceability" trong Analytics Dashboard (UC-171), có nút Export dùng UC-172. Suy luận field từ BR-076 (truy xuất lô nguyên liệu qua menu/phiên sản xuất) và BR-151 (bản đồ truy xuất khi có sự cố, đã xuất hiện trong `10_IncidentDetail.md`) vẫn giữ nguyên và nay được coi là chính thức.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | — | **[Giả định]** |
| 2 | Filter theo Batch ID/Supplier | Input/Select | — | BR-076 | **[Giả định]** |
| 3 | Bảng truy xuất lô nguyên liệu → menu → phiên sản xuất | Table | — | BR-076 | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + bảng truy xuất.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Traceability Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + Batch ID/Supplier, bảng truy xuất lô nguyên liệu → menu → phiên sản xuất, nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1–3.2.13.2 (UC-171/172) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-076, BR-151, BR-175, BR-176, BR-178
- 14_Screen_Authorization_Matrix.md khối 3
