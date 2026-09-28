# Screen Spec — Upload Evidence

**Actor**: Teacher | **UC liên quan**: UC-074 - Report Food Safety Incident (thành phần "Attached Photos") | **Status**: Draft
**BR liên quan**: BR-013 (giới hạn dung lượng file)
**MSG liên quan**: MSG-008, MSG-009

> Bản Web — không áp dụng offline (Web luôn yêu cầu kết nối, `Responsive_Guidelines.md`/`UI_Design_Guidelines.md` mục 5). Field/BR dùng chung với bản Mobile trừ phần đồng bộ offline.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần

| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | File Upload Input | `Input type="file"` (kéo-thả + chọn file) | Chọn ảnh/tài liệu từ máy tính | BR-013 | |
| 2 | File Size Validation | Validation | Tối đa 10MB/ảnh | BR-013 | MSG-008 |
| 3 | File Type Validation | Validation | Chỉ PDF/JPG/PNG | — | MSG-009 |
| 4 | Photo Preview Thumbnail List | List ảnh + nút xoá | Xem trước, xoá từng ảnh | — | |
| 5 | Attach & Continue Button | Button (primary) | Xác nhận đính kèm, quay lại Incident Form | — | |

### Trạng thái màn hình
- Loading: `Skeleton`/progress bar khi tải lên.
- Empty: "Chưa có ảnh nào được đính kèm".
- Error: `Alert variant="destructive"` + MSG-008/MSG-009.
- Success: ảnh hiển thị trạng thái đã tải lên thành công.

---

## Platform Layout — Web
**Entry/Exit points**: Vào từ Incident Form (khối "Attached Photos"). Ra: quay lại Incident Form.
**Bố cục**: `Dialog`/`Sheet` ngắn nổi trên Incident Form — vùng kéo-thả file phía trên, danh sách thumbnail lưới bên dưới, nút "Attach & Continue" góc dưới phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Evidence, nền tảng Web, actor Teacher.
Bố cục: Dialog/Sheet với vùng kéo-thả file + lưới thumbnail ảnh đã chọn + nút Attach & Continue.
Thành phần bắt buộc: File Upload Input, Photo Preview Thumbnail List, Attach & Continue Button.
Trạng thái cần thể hiện: loading, empty, error (MSG-008/009), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 6367–6386.
- `business_rules.md` BR-013; `error_message.md` MSG-008, MSG-009.
