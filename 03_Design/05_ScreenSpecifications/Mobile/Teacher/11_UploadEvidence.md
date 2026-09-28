# Screen Spec — Upload Evidence

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (thành phần "Attached Photos") | **Status**: Draft
**BR liên quan**: BR-196 (cho phép ghi nhận bằng chứng offline), BR-200 (giới hạn hiệu lực cache offline)
**MSG liên quan**: MSG-008 (file vượt 10MB), MSG-009 (định dạng không hỗ trợ), MSG-078 (đã lưu offline, chờ đồng bộ)

> **Ghi chú nguồn**: elevate từ field "Attached Photos Component" trong Health Incident Report (Report 3 dòng 6381) thành 1 màn hình riêng để mô tả đầy đủ luồng chụp/chọn ảnh + hàng đợi đồng bộ offline (theo `UI_Design_Guidelines.md` mục 3, mục 5 — Teacher là 1 trong 5 role field được áp dụng offline evidence capture BR-196). [Giả định — cần team xác nhận đây có phải màn hình độc lập hay chỉ là 1 component nhúng trong Incident Form].

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Take Photo Button | Button (camera) | Mở camera thiết bị | ADR-002 (ngữ cảnh camera), BR-196 | Xin quyền camera đúng lúc (`UI_Design_Guidelines.md` mục 3) |
| 2 | Choose From Gallery Button | Button | Chọn ảnh có sẵn | — | |
| 3 | File Size Validation | Validation | Tối đa 10MB/ảnh | BR-013 | MSG-008 nếu vượt |
| 4 | File Type Validation | Validation | Chỉ nhận PDF/JPG/PNG | — | MSG-009 nếu sai định dạng |
| 5 | Photo Preview Thumbnail List | List ảnh | Xem trước ảnh đã chọn, cho phép xoá từng ảnh | — | |
| 6 | Offline Sync Badge | Badge | "Chờ đồng bộ" (vàng/cam) khi lưu lúc mất kết nối | BR-196 | MSG-078 |
| 7 | Attach & Continue Button | Button (primary) | Xác nhận đính kèm, quay lại Incident Form | — | |

### Trạng thái màn hình
- Loading: Spinner khi đang tải/nén ảnh.
- Empty: "Chưa có ảnh nào được đính kèm".
- Error: `SnackBar` `danger` + MSG-008/MSG-009.
- Success: Nếu offline → MSG-078 + badge "Chờ đồng bộ"; nếu online → ảnh hiển thị trạng thái đã tải lên (✓ xanh).

---

## Platform Layout — Mobile
**Entry/Exit points**: Vào từ Incident Form (nút "Take"/"Choose" trong Attached Photos Component). Ra: quay lại Incident Form với ảnh đã đính kèm.
**Bố cục**: Toàn màn hình camera/preview theo Material 3 — 2 nút lớn (Take Photo/Choose from Gallery) phía trên, danh sách thumbnail cuộn ngang phía dưới, nút "Attach & Continue" cố định đáy màn hình.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Evidence, nền tảng Mobile, actor Teacher.
Bố cục: 2 nút lớn Take Photo/Choose from Gallery + danh sách thumbnail ảnh đã chọn (cuộn ngang) + nút Attach & Continue cố định đáy.
Thành phần bắt buộc: Take Photo Button, Choose From Gallery Button, Photo Preview Thumbnail List, Offline Sync Badge, Attach & Continue Button.
Áp dụng Design System: badge "Chờ đồng bộ" màu warning theo UI_Design_Guidelines.md mục 5.
Trạng thái cần thể hiện: loading, empty, error (MSG-008/009), success (MSG-078 khi offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386 (Health Incident Report, field "Attached Photos Component").
- `business_rules.md` BR-013, BR-196, BR-200; `error_message.md` MSG-008, MSG-009, MSG-078.
- `03_Design/00_Foundation/UI_Design_Guidelines.md` mục 3, mục 5.
