# Screen Spec — Verify Supplier Delivery Panel

**Actor**: Service Caterers | **UC liên quan**: UC-060 - Record Ingredient Receiving (Report 3 §3.2, sub-bước "Verify Invoice") | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071
**MSG liên quan**: MSG-013 (Error — chứng nhận ATTP hết hạn), MSG-014 (Error — xác thực khuôn mặt thất bại), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin nhà cung cấp (tên, trạng thái Approved) | Read-only card | Chặn nếu không Approved | BR-070 | UC-044/UC-060 |
| 2 | Danh sách chứng nhận lô hàng (số lô, ngày sản xuất, hạn dùng, số lượng, chứng chỉ) | Read-only table/list | Bắt buộc đầy đủ | BR-071 | UC-060 Normal Flow bước 4 |
| 3 | Kết quả xác thực khuôn mặt nhân viên giao hàng | Trạng thái (Passed/Failed) | Nếu Failed → MSG-014, chuyển luồng "Manual Override" (nhập lý do + ảnh bằng chứng) | BR-050 | UC-060 Normal Flow bước 3; `UI_Design_Guidelines.md` mục 3.4, `ADR-002.md` |
| 4 | Nút "Xác nhận Hồ sơ Hợp lệ" | Button | Điều hướng tiếp sang Inspect Quality/Capture Evidence | — | — |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin chứng nhận.
- Empty: Không áp dụng.
- Error: MSG-013 nếu chứng nhận ATTP hết hạn (chặn xác nhận); MSG-014 nếu xác thực khuôn mặt thất bại → hiện luồng Manual Override bắt buộc nhập lý do + ảnh bằng chứng.
- Success: Chuyển sang bước tiếp theo trong Receive Ingredients Workspace.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (tab "Xác minh"). Thoát: xác nhận hợp lệ → quay lại Workspace, chuyển tab kế tiếp.
**Bố cục**: Panel/Dialog trong Workspace, 2 khối: (1) thông tin nhà cung cấp + chứng nhận dạng bảng, (2) khối xác thực khuôn mặt hiển thị kết quả webcam-based (thứ yếu, khuyến nghị dùng Mobile tại hiện trường theo `09_Service_Caterers_Screen_Flow_Web.md`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Verify Supplier Delivery Panel, nền tảng Web, actor Service Caterers.
Bố cục: Panel trong Workspace gồm thông tin nhà cung cấp, bảng chứng nhận lô hàng, khối kết quả xác thực khuôn mặt, nút Xác nhận Hồ sơ Hợp lệ.
Thành phần bắt buộc: badge trạng thái Approved, cảnh báo MSG-013/MSG-014 khi lỗi.
Áp dụng Design System: xem 00_Foundation/Design_System.md, ADR-002.md (Face Biometric).
Trạng thái cần thể hiện: loading, error (MSG-013/MSG-014 + Manual Override), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071
- error_message.md MSG-013, MSG-014
- `03_Design/02_ADR/ADR-002.md` (Face Biometric, Manual Override)
