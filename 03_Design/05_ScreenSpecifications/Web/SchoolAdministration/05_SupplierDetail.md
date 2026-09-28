# Screen Spec — Supplier Detail

**Actor**: School Administration | **UC liên quan**: UC-148 (View Supplier Detail), UC-007 (Approval / Reject Supplier) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-046 (tài liệu tuân thủ), BR-047 (cảnh báo hết hạn 30 ngày), BR-048 (từ chối kèm lý do)
**MSG liên quan**: MSG-004, MSG-005, MSG-013 (chứng nhận hết hạn — chặn duyệt), MSG-037 (phê duyệt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin công ty (tên, liên hệ, tax ID) | Text hiển thị | — | — | |
| 2 | Trạng thái duyệt | Badge (warning/success/danger) | — | — | |
| 3 | Giấy phép kinh doanh + Chứng nhận ATTP | File preview + ngày hết hạn | Badge "Expiring Soon" nếu <30 ngày | BR-047 | AF2 UC-148 |
| 4 | Lịch sử giao hàng (tóm tắt) | Table rút gọn | — | — | |
| 5 | Chỉ số hiệu suất hàng tháng | Text/số + badge cảnh báo nếu <70% 2 tháng liên tiếp | — | MSG-060 | |
| 6 | Nút "Approve" | Button (primary) | **Chặn nếu chứng nhận ATTP đã hết hạn** (MSG-013) | BR-048 | AF1 UC-007 |
| 7 | Nút "Reject" + lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc lý do | BR-048 | |

### Trạng thái màn hình
- Loading: Skeleton card.
- Empty: Không áp dụng.
- Error: MSG-013 nếu cố Approve khi chứng nhận hết hạn (AF1); AF2 thiếu tài liệu bắt buộc → highlight field thiếu, chặn Approve; MSG-004/MSG-005.
- Success: MSG-037 khi Approve; toast xác nhận khi Reject.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Supplier Partnership Requests` (click 1 dòng). Không có màn hình con.
**Bố cục**: Dashboard Shell — Page Header (tên công ty + nút Approve/Reject), Content 2 cột: thông tin + tài liệu (`col-span-8`), chỉ số hiệu suất + lịch sử giao hàng rút gọn (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Supplier Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), 2 cột: thông tin công ty + tài liệu, chỉ số hiệu suất + lịch sử giao hàng.
Thành phần bắt buộc: thông tin công ty, badge trạng thái, preview giấy phép/chứng nhận (badge Expiring Soon nếu <30 ngày), chỉ số hiệu suất (cảnh báo nếu <70%), nút Approve (chặn nếu hết hạn chứng nhận), nút Reject + lý do bắt buộc.
Áp dụng Design System: badge warning cho Expiring Soon, badge danger cho hiệu suất thấp.
Trạng thái cần thể hiện: error (chứng nhận hết hạn chặn Approve), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2287–2307 (Table 3-46 Approval/Reject Supplier), 2617–2637 (Table 3-61 View Supplier Detail)
- traceability_matrix.md §1.4 (UC-007, UC-148)
