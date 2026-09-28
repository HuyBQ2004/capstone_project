# Screen Spec — Inspect Meals Before Serving Screen (Step 3)

**Actor**: Service Caterers | **UC liên quan**: UC-065 - Record Meals Before Serving (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1072) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099, BR-196
**MSG liên quan**: MSG-016 (Error), MSG-043 (Success), MSG-078 (Success — lưu offline), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: giống bản Web — UC-065 không có bảng đặc tả UC đầy đủ riêng trong `Report 3.md`. Nội dung dưới đây suy ra từ **Step 3 trong Normal Flow của UC-062** (dòng 3542). **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Số suất ăn (portion count) | TextField số | Bắt buộc | — | UC-062 Step 3 |
| 2 | Thời điểm chia suất / bắt đầu bữa ăn | Time picker (M3) | Bắt buộc | — | UC-062 Step 3 |
| 3 | Mô tả dụng cụ chứa (sạch, có nắp đậy) | CheckboxListTile | Bắt buộc | BR-095 | UC-062 Step 3 |
| 4 | Nhiệt độ giữ ấm | TextField số (°C) | Phải ≥ 60°C | BR-094 | UC-062 Step 3, chặn nếu không đạt |
| 5 | Kiểm tra cảm quan trước khi ăn | CheckboxListTile | Bắt buộc | BR-095 | UC-062 Step 3 |
| 6 | Ảnh bằng chứng bữa ăn hoàn thiện | Camera capture | Bắt buộc — cho phép offline (BR-196) | BR-090, BR-196 | UC-062 Step 3 |
| 7 | Đồng ký số (Catering Manager + School Representative) | Chữ ký số kép (Signature pad) | Bắt buộc cả 2 bên | BR-099 | UC-062 bước 5 "Co-sign & Approve" |
| 8 | Nút "Nộp Bước 3 & Phê duyệt Phục vụ" | FilledButton (cố định cuối màn) | Nếu đạt đủ 3 bước → chuyển "Approved for Serving" | BR-098 | UC-052 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Shimmer` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-016 nếu checklist chưa đầy đủ; chặn phê duyệt nếu nhiệt độ giữ ấm < 60°C — tạo Corrective Action.
- Success: `SnackBar` MSG-043 khi bữa ăn được phê duyệt phục vụ.

---

## Platform Layout — Mobile

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 2 = Passed. Thoát: nộp thành công + đồng ký số → hoàn tất phiên kiểm tra.
**Bố cục**: Camera chụp ảnh + input nhiệt độ giữ ấm ưu tiên đầu form, checklist bên dưới, khối chữ ký số kép (2 signature pad tuần tự) cuối màn, nút "Nộp Bước 3 & Phê duyệt Phục vụ" cố định cuối.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Meals Before Serving Screen (Step 3), nền tảng Mobile, actor Service Caterers.
Bố cục: camera + input nhiệt độ giữ ấm ưu tiên đầu form, checklist dụng cụ + cảm quan, khối chữ ký số kép, nút Nộp Bước 3 & Phê duyệt Phục vụ cố định dưới cùng.
Thành phần bắt buộc: cảnh báo nhiệt độ giữ ấm chưa đạt 60°C, yêu cầu đủ 2 chữ ký số, badge "Chờ đồng bộ" nếu offline.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (online/offline).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 3" + "Co-sign & Approve") — xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099, BR-196
- error_message.md MSG-016, MSG-043, MSG-078
