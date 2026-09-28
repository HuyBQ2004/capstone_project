# Screen Spec — Receive Ingredients Workspace (Start Receiving Session)

**Actor**: Service Caterers | **UC liên quan**: UC-044 - Manage Ingredient Receiving (khởi tạo phiên), UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-050, BR-070, BR-071, BR-078, BR-079, BR-080
**MSG liên quan**: MSG-015 (Error — GPS ngoài khu vực), MSG-041 (Success), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Thông tin phiên nhận hàng (nhà cung cấp, giờ dự kiến) | Read-only header | Lấy từ Receiving Management Dashboard | BR-078 | UC-060 Trigger |
| 2 | Xác minh trạng thái Approved của nhà cung cấp | Kiểm tra tự động (block nếu không Approved) | **Bắt buộc kết nối mạng — không cho phép offline** | BR-070, BR-197 | UC-044 Postconditions ngầm định qua BR-070; UI Guidelines mục 5.2 |
| 3 | Xác minh GPS trong bán kính cho phép | Kiểm tra tự động | Trong 50m khu vực cổng trường; ngoài dung sai hiển thị cảnh báo `warning` cho phép xác nhận thủ công kèm lý do | BR-079 | UC-060 Normal Flow bước 2, MSG-015 |
| 4 | Xác thực khuôn mặt nhân viên giao hàng | Face scan trigger → điều hướng Verify Supplier Delivery Panel | **Bắt buộc online** | BR-050, BR-197 | UC-060 Normal Flow bước 3 |
| 5 | Danh sách hành động con của phiên (Verify Invoice, Inspect Quality, Capture Evidence, Log Rejections, Send Receipt) | Danh sách bước / Tab | Điều hướng tới các màn con tương ứng | BR-071, BR-080 | ScreenFlow: `RecvWS` branches |
| 6 | Nút "Hoàn tất Phiên Nhận hàng" | Button | Chỉ bật khi đã hoàn tất các bước bắt buộc | — | UC-060 Postconditions |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-015 (GPS ngoài khu vực) dạng `Alert variant="destructive"`; chặn hoàn toàn nếu nhà cung cấp không Approved (BR-070/BR-197, không cho qua bằng cache).
- Success: `Toast` MSG-041 sau khi hoàn tất phiên.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Receiving Management Dashboard (nút "Bắt đầu Nhận hàng"). Điều hướng tiếp: Verify Supplier Delivery Panel, Capture Receiving Evidence Screen, Reject Ingredient Batch Form, Record Ingredient Receiving.
**Bố cục**: Layout dạng Wizard theo `Tabs` ngang (Xác minh → Kiểm tra chất lượng → Bằng chứng → Xác nhận), mỗi tab là 1 khối nội dung trong cùng trang; GPS xác nhận qua Geolocation API trình duyệt + nhập/xác nhận tọa độ thủ công (theo `09_Service_Caterers_Screen_Flow_Web.md`: "Web thay bằng upload file + xác nhận tọa độ thủ công").

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Receive Ingredients Workspace, nền tảng Web, actor Service Caterers.
Bố cục: Tabs ngang (Xác minh, Kiểm tra chất lượng, Bằng chứng, Xác nhận), header hiển thị thông tin phiên nhận hàng.
Thành phần bắt buộc: banner xác minh trạng thái Approved nhà cung cấp, banner GPS, khối xác thực khuôn mặt, nút Hoàn tất Phiên.
Áp dụng Design System: banner chặn dùng token `danger` (MSG-015/MSG-077 nếu mất kết nối), xem Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn do GPS/trạng thái NCC), success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3111–3129 (UC-044), dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-050, BR-070, BR-071, BR-078, BR-079, BR-080, BR-197
- error_message.md MSG-015, MSG-041, MSG-077
- `03_Design/04_ScreenFlow/09_Service_Caterers_Screen_Flow_Web.md` (khác biệt GPS/camera Web vs Mobile)
