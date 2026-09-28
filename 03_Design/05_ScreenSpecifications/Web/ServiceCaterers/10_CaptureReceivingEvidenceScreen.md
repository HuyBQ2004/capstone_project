# Screen Spec — Capture Receiving Evidence Screen

**Actor**: Service Caterers | **UC liên quan**: bundled UC-044 (theo `14_Screen_Authorization_Matrix.md`), chi tiết hoá từ UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-072, BR-073, BR-074, BR-079
**MSG liên quan**: MSG-008 (Error — vượt dung lượng file), MSG-009 (Error — sai định dạng file), MSG-015 (Error — GPS ngoài khu vực), MSG-041 (Success)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ảnh bằng chứng nguyên liệu, nhãn, bao bì, tài liệu | Upload nhiều ảnh + preview thumbnail | Bắt buộc tối thiểu 1 ảnh; định dạng PDF/JPG/PNG, tối đa dung lượng theo `Component_Guidelines.md` (TODO #11: 5MB vs 10MB — dùng ngưỡng MSG-008 hiện hành 10MB cho tới khi team chốt) | BR-072 | UC-060 Normal Flow bước 5 |
| 2 | Toạ độ GPS + timestamp | Tự động ghi nhận, hiển thị read-only | Không cho sửa tay; xác minh trong khu vực nhận hàng cho phép | BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 2/5 |
| 3 | Thông tin thiết bị (device info) | Tự động ghi nhận (ẩn, chỉ log audit) | — | BR-073 | Biện pháp chống gian lận |
| 4 | Nút "Lưu Bằng chứng" | Button | Lưu, hiển thị MSG-041 | — | — |

### Trạng thái màn hình
- Loading: `Skeleton` cho vùng preview ảnh khi đang tải lên.
- Empty: Chưa chụp/tải ảnh nào → placeholder khung ảnh + hướng dẫn.
- Error: MSG-008/MSG-009 khi ảnh không hợp lệ; MSG-015 nếu GPS ngoài khu vực cho phép (cho phép xác nhận thủ công kèm lý do, ghi audit log theo `UI_Design_Guidelines.md` mục 3.3).
- Success: `Toast` MSG-041.

---

## Platform Layout — Web

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (tab "Bằng chứng"). Thoát: "Lưu Bằng chứng" → quay lại Workspace.
**Bố cục**: Panel upload file (`Input type="file"` + preview thumbnail) thay cho chụp ảnh trực tiếp (không có camera), kèm ô nhập/xác nhận tọa độ GPS thủ công hoặc qua Geolocation API trình duyệt — đánh dấu rõ là **"secondary path", độ tin cậy chống gian lận thấp hơn Mobile**, theo `09_Service_Caterers_Screen_Flow_Web.md` mục "Khác biệt so với bản Mobile". Offline không áp dụng trên Web (BR-197 luôn yêu cầu kết nối).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Capture Receiving Evidence Screen, nền tảng Web, actor Service Caterers.
Bố cục: Panel upload nhiều ảnh (input file + preview thumbnail lưới), khối tọa độ GPS + timestamp read-only, nút Lưu Bằng chứng.
Thành phần bắt buộc: cảnh báo dung lượng/định dạng file (MSG-008/MSG-009), cảnh báo GPS ngoài khu vực (MSG-015).
Áp dụng Design System: ảnh dùng aspect-ratio cố định theo Responsive_Guidelines.md mục 4.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-041).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-072, BR-073, BR-074, BR-079
- error_message.md MSG-008, MSG-009, MSG-015, MSG-041
- `14_Screen_Authorization_Matrix.md` (ghi chú "bundled UC-044")
