# Screen Spec — Verification Documents

**Actor**: School Administration | **UC liên quan**: bundled trong UC-007 (Approval/Reject Supplier), UC-004 (Approval/Reject Service Caterers) | **Status**: Draft
**BR liên quan**: BR-046 (tài liệu tuân thủ), BR-047 (cảnh báo hết hạn 30 ngày)
**MSG liên quan**: MSG-004, MSG-005, MSG-013 (chứng nhận hết hạn)

> **Nguồn**: Không phải màn hình điều hướng riêng theo Normal Flow thật — license/chứng nhận ATTP/ngày hết hạn hiển thị ngay trong `Supplier Detail`/`Service Caterers Detail` (xem field #3 ở 2 file đó). File này đặc tả riêng khối "Verification Documents" theo đúng tên trong Screen Authorization Matrix, dùng khi cần 1 view phóng to/tách riêng (VD xem toàn màn hình 1 tài liệu PDF).

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách tài liệu (giấy phép kinh doanh, chứng nhận ATTP, tài liệu xuất xứ) | List + file preview | — | BR-046 | |
| 2 | Ngày hết hạn từng tài liệu | Text + badge | Badge warning nếu <30 ngày (Expiring Soon) | BR-047 | |
| 3 | Nút "Xem tài liệu" (mở PDF/ảnh full-screen) | Button/Link | — | — | |
| 4 | Nút "Tải xuống" | Button | — | — | |

### Trạng thái màn hình
- Loading: Skeleton list.
- Empty: "Đối tác chưa upload tài liệu nào" (trường hợp hồ sơ chưa đầy đủ — AF2 UC-007).
- Error: MSG-004/MSG-005.
- Success: Danh sách tài liệu + trạng thái hết hạn hiển thị đầy đủ.

---

## Platform Layout — Web
**Entry/Exit points**: Nhúng trong `Supplier Detail`/`Service Caterers Detail` (không phải trang điều hướng độc lập theo Normal Flow thật) — có thể mở rộng thành Dialog xem full tài liệu khi click "Xem tài liệu".
**Bố cục**: Card panel trong `Supplier Detail`/`Service Caterers Detail`, hoặc `Dialog` toàn màn hình khi xem chi tiết 1 tài liệu.

## AI Generation Prompt ⭐

```
Tạo wireframe cho panel Verification Documents, nền tảng Web, actor School Administration.
Bố cục: Card panel (nhúng trong Supplier Detail/Service Caterers Detail) — danh sách tài liệu kèm ngày hết hạn, nút Xem/Tải xuống.
Thành phần bắt buộc: danh sách tài liệu + preview, badge Expiring Soon, nút Xem tài liệu, nút Tải xuống.
Áp dụng Design System: badge warning cho Expiring Soon.
Trạng thái cần thể hiện: empty (chưa upload), success.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 2287–2307 (UC-007 AF2), 2269–2285 (UC-004)
- business_rules.md BR-046, BR-047
