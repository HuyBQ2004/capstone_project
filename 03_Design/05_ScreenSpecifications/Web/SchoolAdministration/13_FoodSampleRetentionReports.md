# Screen Spec — Food Sample Retention Reports

**Actor**: School Administration | **UC liên quan**: UC-171 - View Analytics Dashboard + UC-172 - Export Report (Report 3.md §3.2.13.1–3.2.13.2, lọc theo Food Sample Retention) | **Status**: Draft
**BR liên quan**: BR-105–BR-114 (toàn bộ Food Sample Retention), BR-175, BR-176, BR-178
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: UC-171/172 nay đã có Functional Specification đầy đủ trong Report 3.md (viết bổ sung qua audit A8). Màn hình này là 1 view lọc theo danh mục "Food Sample Retention" trong Analytics Dashboard (UC-171), có nút Export dùng UC-172. Cấu trúc field dưới đây (suy luận từ BR-105–114) vẫn giữ nguyên và nay được coi là chính thức.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | BR-177 | **[Giả định]** |
| 2 | Bảng tổng hợp lưu mẫu (số mẫu lưu, số mẫu hủy đúng hạn, số mẫu thu hồi điều tra) | Table | — | BR-105, BR-112, BR-113 | **[Giả định]** |
| 3 | Cảnh báo nhiệt độ tủ lưu mẫu vi phạm (nếu có trong kỳ báo cáo) | Alert/Table rút gọn | — | BR-110 | **[Giả định]** |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê" hoặc view lọc trong `Analytics Dashboard`.
**Bố cục**: Dashboard Shell — filter bar + bảng tổng hợp + nút Export.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Food Sample Retention Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range, bảng tổng hợp lưu mẫu, cảnh báo nhiệt độ (nếu có), nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.13.1 (UC-171 View Analytics Dashboard), §3.2.13.2 (UC-172 Export Report) — bổ sung 2026-07-01
- traceability_matrix.md §1.13
- business_rules.md BR-105–BR-114, BR-175, BR-176, BR-178
- 14_Screen_Authorization_Matrix.md khối 3
