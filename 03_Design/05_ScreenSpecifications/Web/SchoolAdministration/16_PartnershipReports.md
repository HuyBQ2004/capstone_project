# Screen Spec — Partnership Reports

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard + UC-172 - Export Report (Report 3.md §3.2.13.1–3.2.13.2, lọc theo Partnership) | **Status**: Draft
**BR liên quan**: BR-175 (phân quyền xem báo cáo), BR-176 (phạm vi theo trường), BR-178 (định dạng PDF/Excel)
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171/172 nay đã có Functional Specification đầy đủ trong Report 3.md. Màn hình này là 1 view lọc theo danh mục "Partnership" trong Analytics Dashboard (UC-171), có nút Export dùng UC-172. Cấu trúc field dưới đây vẫn giữ nguyên và nay được coi là chính thức.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | BR-177 (lưu trữ tối thiểu 12 tháng) | **[Giả định]** |
| 2 | Filter loại đối tác (Supplier/Service Caterers) | Select | — | — | **[Giả định]** |
| 3 | Bảng tổng hợp (số đăng ký mới, tỷ lệ approve/reject, thời gian xử lý trung bình) | Table | — | BR-176 | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]** — "Không có dữ liệu trong khoảng thời gian đã chọn".
- Error: MSG-004/MSG-005.
- Success: **[Giả định]** — bảng + export hoạt động.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê", hoặc là 1 view lọc trong `Analytics Dashboard` chung (UC-171) — team cần xác nhận có tách trang riêng hay không.
**Bố cục**: Dashboard Shell — filter bar + bảng tổng hợp + nút Export.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Partnership Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + loại đối tác, bảng tổng hợp (số đăng ký mới, tỷ lệ approve/reject, thời gian xử lý trung bình), nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1–3.2.13.2 (UC-171/172) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-175, BR-176, BR-177, BR-178
- 14_Screen_Authorization_Matrix.md khối 3
