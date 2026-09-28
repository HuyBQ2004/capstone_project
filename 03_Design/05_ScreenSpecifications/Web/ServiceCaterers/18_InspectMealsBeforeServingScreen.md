# Screen Spec — Inspect Meals Before Serving Screen (Step 3)

**Actor**: Service Caterers | **UC liên quan**: UC-065 - Record Meals Before Serving (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1072) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099
**MSG liên quan**: MSG-016 (Error), MSG-043 (Success), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: giống UC-063, `Report 3.md` chỉ có 1 dòng khai báo UC-065 trong bảng tổng hợp UC (dòng 1072 "Record Meals Before Serving"), **không có bảng đặc tả UC đầy đủ riêng**. Nội dung dưới đây suy ra từ **Step 3 trong Normal Flow của UC-062** (Report 3.md dòng 3542, đoạn "Conduct Step 3 (Pre-meal inspection – Form No. 3)") và bước 5 "Co-sign & Approve". **[Giả định — cần team xác nhận]**.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Số suất ăn (portion count) | Input số | Bắt buộc | — | UC-062 Step 3 |
| 2 | Thời điểm chia suất / bắt đầu bữa ăn | Time picker | Bắt buộc | — | UC-062 Step 3 |
| 3 | Mô tả dụng cụ chứa (sạch, có nắp đậy) | Checklist | Bắt buộc | BR-095 | UC-062 Step 3 |
| 4 | Nhiệt độ giữ ấm | Input số (°C) | Phải ≥ 60°C | BR-094 | UC-062 Step 3, chặn nếu không đạt |
| 5 | Kiểm tra cảm quan trước khi ăn | Checklist | Bắt buộc | BR-095 | UC-062 Step 3 |
| 6 | Ảnh bằng chứng bữa ăn hoàn thiện | Upload ảnh | Bắt buộc | BR-090 | UC-062 Step 3 |
| 7 | Đồng ký số (Catering Manager + School Representative) | Chữ ký số kép | Bắt buộc cả 2 bên trước khi phê duyệt phục vụ | BR-099 | UC-062 bước 5 "Co-sign & Approve" |
| 8 | Nút "Nộp Bước 3 & Phê duyệt Phục vụ" | Button | Nếu đạt đủ 3 bước → chuyển trạng thái "Approved for Serving" | BR-098 | UC-052 Normal Flow bước 5 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: MSG-016 nếu checklist chưa đầy đủ; chặn phê duyệt nếu nhiệt độ giữ ấm < 60°C (BR-094) — tạo Corrective Action.
- Success: `Toast` MSG-043 khi bữa ăn được phê duyệt phục vụ (đủ cả 3 bước Passed + co-sign).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 2 = Passed. Thoát: nộp thành công + đồng ký số → hoàn tất phiên kiểm tra, dữ liệu xuất hiện ở View Preparation Records Ledger/View Inspection Records Log.
**Bố cục**: Form 1 trang trong Stepper, khối chữ ký số 2 bên ở cuối form, upload ảnh thay chụp trực tiếp (secondary path).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Inspect Meals Before Serving Screen (Step 3), nền tảng Web, actor Service Caterers.
Bố cục: form 1 trang, checklist dụng cụ + cảm quan, input nhiệt độ giữ ấm, upload ảnh, khối chữ ký số kép, nút Nộp Bước 3 & Phê duyệt Phục vụ.
Thành phần bắt buộc: cảnh báo nhiệt độ giữ ấm chưa đạt 60°C, yêu cầu đủ 2 chữ ký số trước khi bật nút phê duyệt.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (MSG-043, Approved for Serving).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 3" + "Co-sign & Approve") — xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-094, BR-095, BR-097, BR-098, BR-099
- error_message.md MSG-016, MSG-043
