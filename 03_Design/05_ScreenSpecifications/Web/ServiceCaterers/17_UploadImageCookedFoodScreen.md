# Screen Spec — Upload Image Cooked Food Screen (Step 2)

**Actor**: Service Caterers | **UC liên quan**: UC-063 - Record Ingredients In Processing (theo `14_Screen_Authorization_Matrix.md` + danh sách UC tổng hợp Report 3 dòng 1070) | **Status**: Draft
**BR liên quan**: BR-090, BR-091, BR-093, BR-095, BR-097
**MSG liên quan**: MSG-017 (Error — nhiệt độ nấu chưa đạt), MSG-004/MSG-005 (Error)

> **Lưu ý khoảng trống nguồn**: `Report 3.md` chỉ có **1 dòng khai báo UC-063** trong bảng tổng hợp Use Case (dòng 1070 "Record Ingredients In Processing"), **không có bảng đặc tả UC đầy đủ riêng** (Normal Flow/Alternative Flow/Exceptions) cho UC-063 — khác với UC-062 (Step 1) đã có bảng chi tiết đầy đủ. Nội dung Business Logic dưới đây được suy ra từ **Step 2 trong Normal Flow của UC-062** (Report 3.md dòng 3542, đoạn "Conduct Step 2 (In-process inspection – Form No. 2)"), vì UC-052/UC-062 mô tả rõ cả 3 bước trong cùng 1 quy trình tổng. **[Giả định — cần team xác nhận]** nếu team muốn viết bảng UC-063 riêng đầy đủ trong Report 3/traceability_matrix.md.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tên món / Meal Slot | Read-only (kế thừa từ phiên sản xuất) | — | — | UC-062 Step 2 |
| 2 | Thời điểm bắt đầu/kết thúc sơ chế và nấu | Time picker | Bắt buộc | — | UC-062 Step 2 |
| 3 | Checklist điều kiện vệ sinh (nhân viên, thiết bị, khu vực sơ chế) | Checklist | Bắt buộc đủ mục | BR-095 | UC-062 Step 2 |
| 4 | Nhiệt độ lõi thức ăn sau nấu | Input số (°C) | Phải ≥ 75°C trong ≥ 15 giây | BR-093 | UC-062 Step 2, MSG-017 nếu không đạt |
| 5 | Ảnh món ăn sau khi nấu | Upload ảnh | Bắt buộc | BR-090 (tinh thần ghi nhận bằng chứng, đồng bộ BR-083 Step 1) | Tên màn hình gốc "Upload Image Cooked Food" |
| 6 | GPS + timestamp | Tự động ghi nhận | — | BR-079 (kế thừa từ UC-062) | UC-062 Step 2 |
| 7 | Nút "Nộp Bước 2" | Button | Nếu nhiệt độ < 75°C → chặn, MSG-017, tạo Corrective Action; nếu đạt → mở khoá Step 3 | BR-097 | UC-052 Normal Flow bước 3 |

### Trạng thái màn hình
- Loading: `Skeleton` khi tải thông tin phiên.
- Empty: Không áp dụng.
- Error: `Alert variant="destructive"` MSG-017 khi nhiệt độ chưa đạt — chặn, tạo Corrective Action Request.
- Success: Chuyển trạng thái sang Step 3 (mở khoá theo BR-093).

---

## Platform Layout — Web

**Entry/Exit points**: Từ Three-Step Inspection Dashboard, chỉ khả dụng sau khi Step 1 = Passed. Thoát: nộp thành công → Step 3.
**Bố cục**: Form 1 trang trong Stepper của Inspection Dashboard, upload ảnh thay chụp trực tiếp (secondary path — khuyến nghị Mobile tại bếp).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Upload Image Cooked Food Screen (Step 2), nền tảng Web, actor Service Caterers.
Bố cục: form 1 trang, checklist vệ sinh, input nhiệt độ lõi, upload ảnh món ăn, khối GPS/timestamp, nút Nộp Bước 2.
Thành phần bắt buộc: cảnh báo nhiệt độ chưa đạt 75°C (MSG-017).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, error (chặn + Corrective Action), success (mở khoá Step 3).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 3542 (UC-062, đoạn mô tả "Conduct Step 2") — UC-063 không có bảng đặc tả riêng trong Report 3, xem ghi chú khoảng trống nguồn ở trên
- business_rules.md BR-090, BR-091, BR-093, BR-095, BR-097
- error_message.md MSG-017
