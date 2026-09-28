# Screen Spec — Capture Receiving Evidence Screen

**Actor**: Service Caterers | **UC liên quan**: bundled UC-044 (theo `14_Screen_Authorization_Matrix.md`), chi tiết hoá từ UC-060 - Record Ingredient Receiving (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-072, BR-073, BR-074, BR-079, BR-196
**MSG liên quan**: MSG-008 (Error), MSG-009 (Error), MSG-015 (Error), MSG-041 (Success), MSG-078 (Success — lưu offline)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Ảnh bằng chứng nguyên liệu, nhãn, bao bì, tài liệu | Chụp ảnh trực tiếp (camera, `image_picker`) + preview | Bắt buộc tối thiểu 1 ảnh; giới hạn dung lượng theo `Component_Guidelines.md` (TODO #11: 5MB vs 10MB) | BR-072 | UC-060 Normal Flow bước 5 |
| 2 | Toạ độ GPS + timestamp | Tự động ghi nhận (GPS thiết bị), hiển thị read-only | Không cho sửa tay; xác minh trong khu vực nhận hàng cho phép | BR-073, BR-074, BR-079 | UC-060 Normal Flow bước 2/5 |
| 3 | Thông tin thiết bị (device info) | Tự động ghi nhận (ẩn, chỉ log audit) | — | BR-073 | Biện pháp chống gian lận |
| 4 | Nút "Lưu Bằng chứng" | FilledButton | Lưu; nếu online hiển thị MSG-041, nếu offline hiển thị MSG-078 + badge "Chờ đồng bộ" | BR-196 | Cho phép offline (evidence capture) theo BR-196 |

### Trạng thái màn hình
- Loading: `Shimmer` cho vùng preview ảnh.
- Empty: Chưa chụp ảnh nào → placeholder khung ảnh có dấu "X" + hướng dẫn chụp.
- Error: MSG-008/MSG-009 khi ảnh không hợp lệ; MSG-015 nếu GPS ngoài khu vực cho phép (cho xác nhận thủ công kèm lý do, ghi audit log).
- Success: `SnackBar` MSG-041 (online) hoặc MSG-078 kèm badge "Chờ đồng bộ" màu vàng/cam (offline, theo `UI_Design_Guidelines.md` mục 5.1).

---

## Platform Layout — Mobile

**Entry/Exit points**: Mở từ Receive Ingredients Workspace (bước "Capture Evidence"). Thoát: "Lưu Bằng chứng" → quay lại Workspace.
**Bố cục**: Camera view chiếm phần lớn màn hình (kênh chính, độ tin cậy chống gian lận cao nhất), dải thumbnail ảnh đã chụp bên dưới, khối GPS/timestamp hiển thị góc trên, nút "Lưu Bằng chứng" cố định cuối màn. Cho phép ghi nhận offline (BR-196) — hiện badge "Chờ đồng bộ" (vàng/cam) nếu mất kết nối khi lưu, chuỗi trạng thái `Pending Sync → Syncing → Synced`.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Capture Receiving Evidence Screen, nền tảng Mobile, actor Service Caterers.
Bố cục: Camera view lớn, dải thumbnail ảnh đã chụp, khối GPS/timestamp read-only, nút Lưu Bằng chứng cố định dưới cùng, badge trạng thái đồng bộ.
Thành phần bắt buộc: cảnh báo dung lượng/định dạng file (MSG-008/MSG-009), cảnh báo GPS ngoài khu vực (MSG-015), badge offline (MSG-078).
Áp dụng Design System: ảnh dùng aspect-ratio cố định 4:3 hoặc 1:1 theo Responsive_Guidelines.md mục 4; badge màu warning cho "Chờ đồng bộ".
Trạng thái cần thể hiện: loading, empty, error, success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3155–3174 (UC-060 - Record Ingredient Receiving)
- business_rules.md BR-072, BR-073, BR-074, BR-079, BR-196
- error_message.md MSG-008, MSG-009, MSG-015, MSG-041, MSG-078
- `14_Screen_Authorization_Matrix.md` (ghi chú "bundled UC-044", "Mobile ưu tiên — GPS/camera")
