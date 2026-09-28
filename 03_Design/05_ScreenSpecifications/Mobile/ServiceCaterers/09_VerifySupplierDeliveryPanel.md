# Screen Spec — Verify Supplier Delivery Panel

**Actor**: Service Caterers | **UC liên quan**: UC-060 - Record Ingredient Receiving (Report 3 §3.2, sub-bước "Verify Invoice") | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071
**MSG liên quan**: MSG-013 (Error — chứng nhận ATTP hết hạn), MSG-014 (Error — xác thực khuôn mặt thất bại), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin nhà cung cấp (tên, trạng thái Approved) | Read-only card | Chặn nếu không Approved; **bắt buộc online** (BR-197) | BR-070 | UC-044/UC-060 |
| 2 | Danh sách chứng nhận lô hàng (số lô, ngày sản xuất, hạn dùng, số lượng, chứng chỉ) | Read-only list card | Bắt buộc đầy đủ | BR-071 | UC-060 Normal Flow bước 4 |
| 3 | Kết quả xác thực khuôn mặt nhân viên giao hàng (camera thiết bị) | Camera capture + trạng thái Passed/Failed | Nếu Failed → MSG-014, chuyển luồng "Manual Override" | BR-050 | UC-060 Normal Flow bước 3; `ADR-002.md` |
| 4 | Nút "Xác nhận Hồ sơ Hợp lệ" | FilledButton | Điều hướng tiếp sang Inspect Quality/Capture Evidence | — | — |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin chứng nhận.
- Empty: Không áp dụng.
- Error: MSG-013 nếu chứng nhận ATTP hết hạn (chặn xác nhận); MSG-014 nếu xác thực khuôn mặt thất bại → luồng Manual Override (nhập lý do + ảnh bằng chứng).
- Success: Chuyển sang bước tiếp theo trong Receive Ingredients Workspace.

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (bước "Verify Invoice"). Thoát: xác nhận hợp lệ → quay lại Workspace, chuyển bước kế tiếp.
**Bố cục**: Trang riêng trong luồng nhiều trang: card thông tin nhà cung cấp + list chứng nhận, khối camera xác thực khuôn mặt full-width (kênh chính cho thao tác hiện trường), nút "Xác nhận Hồ sơ Hợp lệ" cố định cuối màn.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Verify Supplier Delivery Panel, nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột gồm card thông tin nhà cung cấp, list chứng nhận lô hàng, khối camera xác thực khuôn mặt, nút Xác nhận Hồ sơ Hợp lệ cố định dưới cùng.
Thành phần bắt buộc: badge trạng thái Approved, cảnh báo MSG-013/MSG-014 khi lỗi, luồng Manual Override khi xác thực khuôn mặt thất bại.
Áp dụng Design System: xem 00_Foundation/Design_System.md, ADR-002.md (Face Biometric).
Trạng thái cần thể hiện: loading, error (MSG-013/MSG-014 + Manual Override), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071, BR-197
- error_message.md MSG-013, MSG-014
- `03_Design/02_ADR/ADR-002.md` (Face Biometric, Manual Override)
