# Screen Spec — Record Staff Violation

**Actor**: Service Caterers (đồng thời Medical Staff — cả 2 vai trò đều có quyền lập biên bản theo BR-184) | **UC liên quan**: UC-179 - Record Staff Violation (Report 3 §3.2, mới 2026-07-01) | **Status**: Draft
**BR liên quan**: BR-184, BR-187, BR-190
**MSG liên quan**: MSG-085 (Error), MSG-086 (Success), MSG-088 (Warning — nghi ngờ tái diễn), MSG-004/MSG-005 (Error)

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên nhân viên vi phạm (staffName) | Text input tự do (không phải khóa ngoại) | Bắt buộc | BR-184 | Nhân viên bếp/catering không có tài khoản User trong hệ thống — ghi text tự do |
| 2 | Chức vụ nhân viên (tuỳ chọn) | Text input | Tuỳ chọn | BR-184 | UC-179 Normal Flow bước 2 |
| 3 | Loại vi phạm (violation type) | Select | Bắt buộc (bao gồm giá trị `OVERRIDE_ABUSE` cho lạm dụng manual override) | BR-184, BR-190 | UC-179 Normal Flow bước 2 |
| 4 | Mô tả vi phạm | Textarea | Bắt buộc | BR-184 | MSG-085 nếu thiếu |
| 5 | Ảnh bằng chứng | Upload ảnh | Bắt buộc | BR-184 | MSG-085 nếu thiếu |
| 6 | Mức độ nghiêm trọng | Select | Bắt buộc | BR-184 | UC-179 Normal Flow bước 2 |
| 7 | Ngày giờ xảy ra vi phạm | DateTimePicker | Bắt buộc | — | UC-179 Normal Flow bước 2 |
| 8 | Cảnh báo nghi ngờ tái diễn vi phạm | Alert banner (best-effort, không chặn) | Hiện khi staffName trùng khớp (chính xác/gần đúng) với 1 bản ghi Medium+ trong 90 ngày gần nhất | BR-187 | UC-179 Normal Flow bước 4, MSG-088 |
| 9 | Nút "Lưu Biên bản Vi phạm Nhân viên" | Button | Chặn nếu thiếu field bắt buộc (MSG-085); không bị chặn bởi cảnh báo tái diễn (MSG-088 chỉ cảnh báo) | BR-184 | UC-179 Normal Flow bước 5, MSG-086 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải form.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-085 nếu thiếu mô tả/ảnh/field bắt buộc.
- Success: `Toast` MSG-086; nếu phát hiện nghi ngờ tái diễn, hiển thị thêm `Alert variant="warning"` MSG-088 (không chặn lưu), ghi audit log bất biến (BR-188).

---

## Platform Layout — Web

**Entry/Exit points**: Từ mục "Vi phạm" trong Sidebar (`09_Service_Caterers_Screen_Flow_Web.md` không liệt kê tường minh nhưng thuộc nhóm "Sự cố & Vi phạm", đồng thời truy cập được từ Violation Management theo `04_Service_Caterers_Screen_Flow.md`: ghi chú "→ Medical Staff: Record Staff Violation (UC-179)" — Service Caterers cũng có quyền lập biên bản theo BR-184). Thoát: lưu xong quay lại Corrective Actions Dashboard/Violation Management.
**Bố cục**: Form dài trong `Dialog`/trang riêng — thông tin nhân viên + loại vi phạm ở trên, mô tả + ảnh + mức độ + ngày giờ ở giữa, banner cảnh báo tái diễn (nếu có) trước nút lưu.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Record Staff Violation, nền tảng Web, actor Service Caterers.
Bố cục: form gồm tên nhân viên (text tự do), chức vụ, loại vi phạm, mô tả, upload ảnh, mức độ nghiêm trọng, ngày giờ xảy ra, banner cảnh báo nghi ngờ tái diễn, nút Lưu Biên bản Vi phạm Nhân viên.
Thành phần bắt buộc: cảnh báo MSG-085 khi thiếu field, banner MSG-088 (warning, không chặn) khi nghi ngờ tái diễn.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (MSG-085), success (MSG-086 + có thể kèm MSG-088).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 4639–4659 (UC-179 - Record Staff Violation)
- business_rules.md BR-184, BR-187, BR-190
- error_message.md MSG-085, MSG-086, MSG-088
