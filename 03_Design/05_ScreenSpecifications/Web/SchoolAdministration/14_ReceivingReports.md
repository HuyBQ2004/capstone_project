# Screen Spec — Receiving Reports

**Actor**: School Administration | **UC liên quan**: UC-123 - View Ingredient Receiving Records (Report 3.md §3.2.6.18, dòng 3397–3417 — Primary Actor School Administration, UC có sẵn đầy đủ) | **Status**: Draft
**BR liên quan**: BR-070–BR-078 (Ingredient & Inventory), BR-178
**MSG liên quan**: MSG-004, MSG-005

> **Đã xác nhận 2026-07-01 (audit A8)**: màn hình này KHÔNG cần UC-171/172 (Reporting & Analytics) — nó khớp trực tiếp với UC-123 (View Ingredient Receiving Records), một UC đã có Functional Specification đầy đủ từ trước, Primary Actor = School Administration. Đây là kết luận "gộp UC có sẵn" mạnh nhất trong nhóm A8 vì không cần suy luận hay viết UC mới.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Filter Date range | DatePicker (range) | — | BR-177 | **[Giả định]** |
| 2 | Filter Supplier | Select | — | — | **[Giả định]** |
| 3 | Bảng tổng hợp nhận hàng (số lô nhận, số lô từ chối, tỷ lệ đúng hạn/GPS hợp lệ) | Table | — | BR-070, BR-074, BR-078 | Nguồn tham chiếu: UC-123 View Ingredient Receiving Records |
| 4 | Nút "Export PDF/Excel" | Button | — | BR-178 | |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: **[Giả định]**.
- Error: MSG-004/MSG-005.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Báo cáo & Thống kê".
**Bố cục**: Dashboard Shell — filter bar + bảng tổng hợp + nút Export.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receiving Reports, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — filter Date range + Supplier, bảng tổng hợp nhận hàng (số lô nhận/từ chối, tỷ lệ đúng hạn/GPS hợp lệ), nút Export PDF/Excel.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md §3.2.6.18, dòng 3397–3417 (UC-123 View Ingredient Receiving Records)
- traceability_matrix.md §1.5
- business_rules.md BR-070–BR-078, BR-178
- 14_Screen_Authorization_Matrix.md khối 3
