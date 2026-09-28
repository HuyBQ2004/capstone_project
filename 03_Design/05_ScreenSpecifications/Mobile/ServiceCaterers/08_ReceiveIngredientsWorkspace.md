# Screen Spec — Receive Ingredients Workspace (Start Receiving Session)

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (khởi tạo phiên), UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071, BR-078, BR-079, BR-080
**MSG liên quan**: MSG-015 (Error — GPS ngoài khu vực), MSG-041 (Success), MSG-077 (Error — cần kết nối mạng), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin phiên nhận hàng (nhà cung cấp, giờ dự kiến) | Read-only header | Lấy từ Receiving Management Dashboard | BR-078 | UC-060 Trigger |
| 2 | Xác minh trạng thái Approved của nhà cung cấp | Kiểm tra tự động | **Bắt buộc kết nối mạng** — chặn hoàn toàn nếu offline, hiển thị MSG-077 | BR-070, BR-197 | UI_Design_Guidelines.md mục 5.2 |
| 3 | Xác minh GPS trong bán kính cho phép | Kiểm tra tự động (GPS thiết bị) | Trong 50m khu vực cổng trường; ngoài dung sai hiển thị cảnh báo `warning`, cho xác nhận thủ công kèm lý do (ghi audit log) | BR-079 | UC-060 Normal Flow bước 2, MSG-015 |
| 4 | Xác thực khuôn mặt nhân viên giao hàng | Camera scan (native) → Verify Supplier Delivery Panel | **Bắt buộc online** | BR-050, BR-197 | UC-060 Normal Flow bước 3 |
| 5 | Danh sách hành động con của phiên (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) | Danh sách bước dạng Card | Điều hướng tới màn con tương ứng | BR-071, BR-080 | ScreenFlow: `RecvWS` branches |
| 6 | Nút "Hoàn tất Phiên Nhận hàng" | FilledButton (cố định cuối màn) | Chỉ bật khi đã hoàn tất các bước bắt buộc | — | UC-060 Postconditions |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực) dạng `AlertDialog`; chặn hoàn toàn nếu nhà cung cấp không Approved hoặc mất kết nối (BR-070/BR-197, MSG-077) — nút hành động chuyển `disabled` kèm tooltip.
- Success: `SnackBar` MSG-041 sau khi hoàn tất phiên.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Receiving Management Dashboard (nút "Bắt đầu Nhận hàng" — `04_Service_Caterers_Screen_Flow.md`). Điều hướng tiếp: Verify Supplier Delivery Panel, Capture Receiving Evidence Screen, Reject Ingredient Batch Form, Record Ingredient Receiving.
**Bố cục**: 1 cột, mỗi bước là 1 `Card` có trạng thái (Chưa làm/Đang làm/Hoàn tất), thứ tự Verify → Inspect Quality → Capture Evidence → (Reject nếu cần) → Send Receipt; nút "Hoàn tất Phiên" cố định cuối màn. Trường cần camera/GPS đặt ưu tiên đầu luồng theo `Layout_Guidelines.md` mục 2.2. Cho phép ghi nhận ảnh bằng chứng offline theo BR-196 (badge "Chờ đồng bộ" nếu mất mạng khi ở bước Capture Evidence), nhưng bước xác minh Approved/GPS/khuôn mặt vẫn chặn nếu offline (BR-197).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receive Ingredients Workspace, nền tảng Mobile, actor Service Caterers.
Bố cục: 1 cột, danh sách bước dạng Card (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) với trạng thái từng bước, nút Hoàn tất Phiên cố định dưới cùng.
Thành phần bắt buộc: banner xác minh trạng thái Approved nhà cung cấp, banner GPS, khối xác thực khuôn mặt (camera), badge offline "Chờ đồng bộ" nếu áp dụng.
Áp dụng Design System: SfmsStatusColors.danger cho banner chặn (MSG-015/MSG-077), SfmsStatusColors.warning cho badge chờ đồng bộ (MSG-078).
Trạng thái cần thể hiện: loading, error (chặn do GPS/trạng thái NCC/offline), success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044), dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071, BR-078, BR-079, BR-080, BR-196, BR-197
- error_message.md MSG-015, MSG-041, MSG-077, MSG-078
