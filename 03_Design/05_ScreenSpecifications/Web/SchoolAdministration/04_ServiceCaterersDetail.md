# Screen Spec — Service Caterers Detail

**Actor**: School Administration | **UC liên quan**: UC-108 (View Service Caterers Detail), UC-004 (Approval / Reject Service Caterers) — Report 3 §3.2 | **Status**: Draft
**BR liên quan**: BR-046 (yêu cầu tài liệu tuân thủ), BR-048 (từ chối kèm lý do)
**MSG liên quan**: MSG-004, MSG-005, MSG-037 (phê duyệt thành công)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin công ty (tên, liên hệ) | Text hiển thị | — | — | |
| 2 | Trạng thái hợp đồng | Badge (warning/success/danger) | — | — | |
| 3 | Giấy phép kinh doanh + Chứng nhận ATTP | File preview + ngày hết hạn | Xem `Verification Documents` (bundled) | BR-046 | |
| 4 | Điểm site inspection | Text/số hiển thị | — | — | |
| 5 | Nút "Approve" | Button (primary) | Chỉ hiện khi trạng thái Pending | — | Cập nhật trạng thái Approved, tạo contract record, gửi email (Normal Flow bước 7) |
| 6 | Nút "Reject" + lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc nhập lý do trước khi xác nhận | BR-048 | |

### Trạng thái màn hình
- Loading: Skeleton card khi tải hồ sơ.
- Empty: Không áp dụng.
- Error: MSG-004/MSG-005.
- Success: MSG-037 khi Approve; toast xác nhận khi Reject (gửi email kèm lý do).

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Catering Partnership Requests` (click 1 dòng). Không có màn hình con (Verification Documents hiển thị bundled ngay trong trang).
**Bố cục**: Dashboard Shell — Page Header (tên công ty + nút Approve/Reject góc phải), Content chia 2 khối: thông tin công ty + tài liệu (`col-span-8`), điểm inspection + trạng thái (`col-span-4`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Service Caterers Detail, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject góc phải), 2 cột: thông tin công ty + tài liệu, điểm inspection + badge trạng thái.
Thành phần bắt buộc: thông tin công ty, badge trạng thái hợp đồng, preview giấy phép/chứng nhận ATTP kèm ngày hết hạn, điểm site inspection, nút Approve, nút Reject + textarea lý do bắt buộc.
Áp dụng Design System: nút Reject dùng token danger.
Trạng thái cần thể hiện: success (approve/reject).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2269–2285 (Table 3-45 Approval/Reject Service Caterers), 2533–2549 (Table 3-57 View Service Caterers Detail)
- traceability_matrix.md §1.4 (UC-004, UC-108)
