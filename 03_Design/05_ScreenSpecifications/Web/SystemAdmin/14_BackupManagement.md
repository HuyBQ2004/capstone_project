# Screen Spec — Backup Management

**Actor**: System Admin | **UC liên quan**: bundled trong UC-149 (Report 3 §3.2 "View System Status", bước 4) | **Status**: Draft
**BR liên quan**: BR-160 (sao lưu CSDL tự động hàng tuần), BR-161 (bảo vệ file sao lưu)
**MSG liên quan**: MSG-050 (sao lưu thành công), MSG-070 (cảnh báo dung lượng backup gần đầy)

> **Nguồn**: Không phải UC riêng — Normal Flow thật của UC-149 (View System Status) bước 4 nêu rõ: "Load and display the configuration and operating status of the weekly backup scheduler (BR-160)". Đây là 1 panel hiển thị bên trong System Status, không có luồng "tạo backup thủ công" nào được đặc tả trong Report 3. Nội dung dưới đây tách riêng thành 1 file theo đúng tên trong Screen Authorization Matrix, nhưng bố cục logic vẫn là panel con của System Status.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Lịch backup (Chủ nhật 02:00 sáng) | Text hiển thị | — | BR-160 | Cố định theo cấu hình hệ thống, không cho sửa lịch trên UI (chưa có UC "Configure Backup Schedule") |
| 2 | Trạng thái lần backup gần nhất (thành công/thất bại + thời gian) | Badge + timestamp | success/danger | BR-160 | |
| 3 | Dung lượng backup đã dùng | Progress bar + % | Cảnh báo khi ≥ 92% (MSG-070) | BR-161 | |
| 4 | Vị trí lưu trữ (cloud off-site) | Text hiển thị | — | BR-161 | Không hiển thị chi tiết credential (bảo mật) |
| 5 | Lịch sử các lần backup gần đây | Table rút gọn (5-10 dòng) | — | BR-160 | **[Giả định — cần team xác nhận]**: Normal Flow gốc chỉ nói "display configuration and operating status", không xác nhận có lịch sử dạng bảng hay không |

### Trạng thái màn hình
- Loading: Skeleton panel.
- Empty: Không áp dụng (backup chạy tự động hàng tuần, luôn có ít nhất 1 bản ghi sau tuần đầu vận hành).
- Error: Badge danger nếu lần backup gần nhất thất bại; MSG-070 nếu dung lượng gần đầy.
- Success: MSG-050 hiển thị dạng log/badge (backup được thực hiện tự động, không phải hành động người dùng chủ động trigger).

---

## Platform Layout — Web
**Entry/Exit points**: Là 1 panel/tab bên trong `System Status Screen` (UC-149) — xem `18_SystemStatusScreen.md`. Có thể truy cập trực tiếp qua Sidebar nếu team quyết định tách trang riêng theo tên gọi trong Screen Authorization Matrix.
**Bố cục**: Card panel trong Dashboard Shell — nếu là tab con của System Status: nằm trong `Tabs` cùng health-check dashboard; nếu tách trang riêng: Page Header + Content đơn giản 1 cột (không cần bảng dữ liệu dày đặc).

## AI Generation Prompt ⭐

```
Tạo wireframe cho panel Backup Management, nền tảng Web, actor System Admin.
Bố cục: Card panel (trong System Status hoặc trang riêng) — lịch backup, trạng thái lần gần nhất, dung lượng đã dùng (progress bar), vị trí lưu trữ, lịch sử rút gọn.
Thành phần bắt buộc: text lịch backup cố định, badge trạng thái, progress bar dung lượng, bảng lịch sử rút gọn.
Áp dụng Design System: badge success/danger cho trạng thái, progress bar chuyển sang warning khi ≥92%.
Trạng thái cần thể hiện: success, warning (dung lượng gần đầy), error (backup thất bại).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên — đặc biệt KHÔNG thêm nút "Backup thủ công" vì không có UC nào mô tả hành vi này.
```

## Nguồn dữ liệu
- Report 3.md, dòng 4971–4991 (Table 3-153 View System Status Use Cases Specification, bước 4 Normal Flow)
- business_rules.md BR-160, BR-161
- error_message.md MSG-050, MSG-070
