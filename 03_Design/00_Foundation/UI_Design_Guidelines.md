# UI Design Guidelines — SFMS

**Trạng thái**: Confirmed (2026-07-01)
**Mục đích**: Tài liệu "gốc" — nguyên tắc thiết kế xuyên suốt, cách Web (shadcn/ui + Tailwind) và Mobile (Material Design 3) cùng phục vụ 1 thương hiệu, và các pattern tương tác dùng chung (form, GPS/camera permission, notification, offline sync). Các tài liệu khác trong `00_Foundation/` cụ thể hoá từng khía cạnh riêng — xem mục 7 "Liên kết tài liệu".

---

## 1. Nguyên tắc thiết kế cốt lõi

1. **An toàn trước, thẩm mỹ sau**: mọi quyết định màu sắc/bố cục ưu tiên truyền tải đúng mức độ nghiêm trọng của cảnh báo an toàn thực phẩm (dị ứng, sự cố, quá hạn) — không hy sinh độ rõ ràng cảnh báo vì thẩm mỹ (xem `Design_System.md` mục 3).
2. **Rõ ràng hơn tối giản**: người dùng thực tế gồm nhân viên bếp/y tế thao tác nhanh giữa ca làm việc, phụ huynh không rành công nghệ — ưu tiên label rõ nghĩa, icon kèm chữ (không chỉ icon), tránh thuật ngữ kỹ thuật.
3. **Nhất quán xuyên nền tảng ở tầng ý nghĩa, không nhất quán ở tầng pixel**: Web và Mobile dùng 2 ngôn ngữ thị giác khác nhau (shadcn/Tailwind vs Material 3) — điều bắt buộc giống nhau là **ý nghĩa** (đỏ luôn là nguy hiểm, cùng nhãn MSG, cùng luồng nghiệp vụ), không bắt buộc giống nhau về pixel-perfect layout.
4. **Không suy diễn ngoài nguồn**: mọi field/validation/trạng thái phải bắt nguồn từ UC/BR/MSG đã có trong `Report 3.md`/`business_rules.md`/`error_message.md` — không tự thêm tính năng UI khi thiết kế wireframe.

---

## 2. Form & Validation Pattern

- **Validate tại chỗ (inline)**: hiển thị lỗi ngay dưới field khi rời khỏi field (on-blur), không đợi submit — trừ các lỗi cần xác minh phía server (VD MSG-012 mã nhập học đã dùng).
- **Bắt buộc vs tuỳ chọn**: field bắt buộc đánh dấu `*` đỏ nhạt (không phải `danger` đậm — tránh gây cảm giác "đã có lỗi" khi form còn trống).
- **Thông báo lỗi luôn dùng đúng nội dung MSG đã định nghĩa** trong `error_message.md`/`Report3_SystemMessages_EN.md` — không tự viết lại câu chữ khác trong wireframe.
- **Giới hạn file upload**: theo `Component_Guidelines.md` (Component: File Upload) — 5MB ảnh hồ sơ/bằng chứng (BR-013), lưu ý điểm chưa thống nhất 5MB vs 10MB (TODO #11 trong `00_UI_Design_Documentation_Plan.md`) cần chốt trước khi khoá spec.

## 3. Pattern xin quyền Camera/GPS

Đặc thù SFSMSS: GPS bắt buộc khi nhận hàng/lưu mẫu/kiểm tra 3 bước (BR-073, BR-074, BR-079, BR-108), camera bắt buộc khi chụp bằng chứng và xác thực khuôn mặt (ADR-002).

1. **Xin quyền đúng lúc (contextual), không xin hàng loạt lúc mở app lần đầu**: chỉ hiện dialog xin quyền camera/GPS ngay trước bước cần dùng (VD mở màn "Nhận hàng" mới xin GPS), kèm 1 câu giải thích ngắn lý do cần quyền.
2. **Từ chối quyền**: hiển thị màn hình chặn thao tác kèm nút "Mở Cài đặt" trỏ tới system settings — không được cho phép tiếp tục thao tác cần GPS/camera mà không có quyền (rủi ro dữ liệu bằng chứng giả).
3. **GPS ngoài dung sai 50m** (Report 3 Performance Requirement, BR-079): hiển thị cảnh báo `warning` kèm khoảng cách lệch thực tế, cho phép Service Caterers xác nhận thủ công kèm lý do (ghi audit log).
4. **Xác thực khuôn mặt lỗi/timeout** (ADR-002): hiện luồng "Manual Override" — bắt buộc nhập lý do + ảnh bằng chứng trước khi tiếp tục (xem AI Prompt Constraint #4 của `03_Design/02_ADR/ADR-002.md`).

## 4. Notification & Toast Pattern

- **3 loại thông báo trong hệ thống, màu theo `Design_System.md` mục 3**: Error (`danger`, chặn thao tác), Success (`success`, tự ẩn sau 3–4s), Warning (`warning`, không tự ẩn nếu cần hành động tiếp theo).
- **Toast vs Banner cố định**: lỗi/thành công tức thời dùng Toast (góc trên bên phải Web / dưới cùng Mobile); trạng thái kéo dài (offline, cache hết hạn, quarantine đang active) dùng Banner cố định trên đầu màn hình, không tự ẩn.
- **Quiet Hours** (BR-158, mặc định 22:00–06:00): thông báo push không quan trọng bị hoãn tới hết Quiet Hours; thông báo `critical` (sự cố an toàn) **luôn được gửi ngay**, không bị hoãn bởi Quiet Hours.
- **Nội dung thông báo lấy nguyên văn từ MSG code** tương ứng — xem `error_message.md`.

## 5. Offline & Đồng bộ dữ liệu (Mobile) — BR-196–200

> Áp dụng cho Mobile (5 role field: Service Caterers, Medical Staff, Parent, Raw Food Supplier, Teacher). Web luôn yêu cầu kết nối, không áp dụng mục này. Quyết định chính sách: xem `00_UI_Design_Documentation_Plan.md` mục 2.3 (ĐÃ CHỐT).

**Nguyên tắc chung**: hỗ trợ offline **có giới hạn** — cho phép ghi nhận bằng chứng khi mất kết nối, bắt buộc kết nối mạng cho mọi thao tác xác minh an toàn thời gian thực.

### 5.1. Nhóm được phép Offline (Evidence Capture — BR-196)

| Thao tác | UC liên quan | BR |
|---|---|---|
| Chụp ảnh + GPS + timestamp khi kiểm tra 3 bước | UC-052 | BR-083, BR-196 |
| Chụp ảnh bằng chứng khi nhận hàng | UC-044/UC-060 | BR-072, BR-196 |
| Chụp ảnh + nhãn khi lưu mẫu thực phẩm | UC-069 | BR-107, BR-196 |
| Điền checklist kiểm tra tại chỗ | UC-052, UC-065 | BR-095, BR-196 |

**Hành vi UI bắt buộc**: lưu offline hiện **MSG-078** kèm badge "Chờ đồng bộ" (màu vàng/cam, không phải đỏ lỗi); icon trạng thái theo chuỗi `Pending Sync → Syncing → Synced (✓ xanh) → Sync Failed`; hàng đợi đồng bộ hiển thị được (badge số lượng trên nav); tự động gửi FIFO khi có kết nối trở lại.

### 5.2. Nhóm BẮT BUỘC Online (Real-time Safety Verification — BR-197)

| Thao tác | UC liên quan | BR |
|---|---|---|
| Cảnh báo dị ứng khi quét ID học sinh | UC-016 | BR-123, BR-197 |
| Xác minh trạng thái Approved của nhà cung cấp | UC-044/UC-060 | BR-070, BR-197 |
| Kiểm tra trạng thái Quarantine của lô hàng | UC-041 | BR-149, BR-197 |
| Xác thực sinh trắc học khuôn mặt | UC-068/UC-079 | BR-051, BR-197 |
| Tự động tổng hợp/kích hoạt điều tra sự cố | UC-041 | BR-146, BR-198 |

**Hành vi UI bắt buộc**: mất kết nối → **chặn hoàn toàn**, hiện **MSG-077**; **không** cho "lưu offline, đồng bộ sau" với nhóm này kể cả có cache cũ; nút hành động chuyển `disabled` kèm tooltip giải thích lý do (không ẩn nút).

### 5.3. Xung đột đồng bộ (BR-199)

Nguyên tắc **last-write-wins theo timestamp server** (không dùng giờ thiết bị). Cả bản thắng và bản bị ghi đè đều lưu audit log bất biến (BR-162). UI không hỏi người dùng chọn bản nào — xử lý tự động, chỉ thông báo passive nếu bản ghi của người dùng bị ghi đè.

### 5.4. Giới hạn hiệu lực Cache (BR-200)

Cache offline (thực đơn, danh sách lớp/học sinh) hợp lệ tối đa **[TODO — team xác định số giờ]** kể từ lần đồng bộ gần nhất. Quá hạn → **MSG-079**, màn hình chuyển "Cần đồng bộ lại", hạn chế ghi mới tới khi đồng bộ thành công.

### 5.5. Trạng thái mạng cần thể hiện trên UI

| Trạng thái | Hiển thị |
|---|---|
| Online, đã đồng bộ | Không cần banner, icon ✓ xanh nhỏ góc màn hình |
| Offline, có bản ghi chờ đồng bộ | Banner cố định: "Đang offline — X bản ghi chờ đồng bộ" |
| Offline, cố thao tác nhóm bắt buộc online | Chặn + MSG-077, dialog/toast rõ ràng (không banner mờ) |
| Cache quá hạn | Banner vàng: "Dữ liệu đã cũ — Cần đồng bộ lại" |
| Đang đồng bộ | Spinner nhỏ + đếm số bản ghi còn lại |

---

## 6. Trạng thái màn hình bắt buộc (Loading / Empty / Error / Success)

Mọi Screen Specification (`08_ScreenSpecifications` / `05_ScreenSpecifications` tuỳ theo cấu trúc thực tế) phải khai báo đủ 4 trạng thái:

| Trạng thái | Web (shadcn) | Mobile (Material 3) |
|---|---|---|
| Loading | `Skeleton` component hoặc `Spinner` cho action nhỏ | `CircularProgressIndicator` / `Shimmer` |
| Empty | Illustration placeholder + câu hướng dẫn hành động tiếp theo (không chỉ "Không có dữ liệu") | Tương tự, dùng `Column` giữa màn hình |
| Error | `Alert variant="destructive"` kèm nút "Thử lại" | `SnackBar`/`AlertDialog` màu `danger` |
| Success | `Toast`/`Alert variant="success"` | `SnackBar` màu `success` |

---

## 7. Liên kết tài liệu khác trong `00_Foundation/`

- Màu sắc/font/spacing cụ thể → `Design_System.md`
- Bố cục trang/grid → `Layout_Guidelines.md`
- Breakpoint & hành vi responsive → `Responsive_Guidelines.md`
- Thư viện component shadcn ↔ Material 3 → `Component_Guidelines.md`
- Điều hướng (sidebar Web / bottom-nav Mobile) → `Navigation_Guidelines.md`
- Quy ước đặt tên file/component/token → `Naming_Convention.md`
- Chuẩn accessibility (contrast, touch target) → `Accessibility.md`
- Quy trình dùng AI sinh wireframe/prototype → `AI_Design_Guidelines.md`
