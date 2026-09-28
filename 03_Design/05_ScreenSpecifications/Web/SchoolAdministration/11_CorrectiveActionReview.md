# Screen Spec — Corrective Action Review

**Actor**: School Administration | **UC liên quan**: UC-005 (Report 3 §3.2 "Approval / Reject Corrective Action") | **Status**: Draft
**BR liên quan**: BR-152 (quản lý CAPA), BR-153 (phê duyệt hành động khắc phục)
**MSG liên quan**: MSG-004, MSG-005, MSG-029 (cập nhật thành công — Approve), MSG-049 (nộp CAPA thành công — tham chiếu vòng đời)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Mô tả sự cố liên quan | Text hiển thị | — | — | |
| 2 | Yêu cầu CAPA đã giao | Text/List hiển thị | — | BR-152 | |
| 3 | Báo cáo thực hiện của Service Caterers + bằng chứng (ảnh, checklist) | Text + gallery ảnh | — | — | |
| 4 | Ghi chú xác minh | Textarea | Bắt buộc khi Approve | — | |
| 5 | Nút "Approve" | Button (primary) | — | BR-153 | MSG-043 khi Approve theo Normal Flow |
| 6 | Nút "Reject" + lý do | Button (destructive) + Textarea bắt buộc | Bắt buộc lý do (EX1 chặn nếu thiếu) | BR-153 | Deadline resubmit mới (VD 6 giờ, AF1) |

### Trạng thái màn hình
- Loading: Button Approve/Reject loading khi submit.
- Empty: Không áp dụng.
- Error: EX1 — thiếu ghi chú lý do khi Reject → validation error, chặn submit; MSG-004/MSG-005.
- Success: Approve → cập nhật CAPA "Approved", thông báo Service Caterers + Medical Staff; Reject → CAPA "Rejected", thông báo kèm deadline resubmit mới.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ `Incident Detail` (nút "Xem CAPA") hoặc dashboard CAPA riêng. Sau khi xử lý quay về `Incident Detail`/`Resolution Report` nếu đủ điều kiện đóng sự cố.
**Bố cục**: Dashboard Shell — Page Header (nút Approve/Reject góc phải), Content: mô tả sự cố + yêu cầu CAPA (`col-span-6`), báo cáo thực hiện + gallery ảnh (`col-span-6`), ghi chú xác minh cuối trang.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Corrective Action Review, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell — Page Header (nút Approve/Reject), 2 cột: mô tả sự cố + yêu cầu CAPA, báo cáo thực hiện + gallery ảnh bằng chứng, textarea ghi chú xác minh cuối trang.
Thành phần bắt buộc: mô tả sự cố, yêu cầu CAPA, báo cáo + ảnh bằng chứng, textarea ghi chú, nút Approve, nút Reject + lý do bắt buộc.
Áp dụng Design System: nút Reject dùng token danger.
Trạng thái cần thể hiện: error (thiếu lý do reject), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4307–4329 (Table 3-128 Approval/Reject Corrective Action Use Cases Specification)
- traceability_matrix.md §1.11 (UC-005 ↔ BR-152/BR-153)
