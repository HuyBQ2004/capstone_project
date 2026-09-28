# Screen Spec — School Configuration

**Actor**: School Administration | **UC liên quan**: UC-095 - Update Information School (Report 3.md dòng 1777–1797 — Description xác nhận rõ "Enables the System Admin (platform-level) OR School Administration (school-level) to modify school profile details and operational configuration fields") | **Status**: Draft
**BR liên quan**: BR-028 (geofence), BR-080 (trần chi phí thực đơn)
**MSG liên quan**: MSG-004, MSG-005, MSG-029

> **Đã xác nhận 2026-07-01 (audit A8)**: màn hình này KHÔNG cần UC mới — nó khớp với UC-095 (Update Information School), phần "School Administration (school-level)" theo Description. Đây là phần cấu hình vận hành (operational configuration) mà School Admin được sửa, khác với phần hồ sơ trường (Name/Address/Coordinates/Tax ID) do System Admin sở hữu — cùng 1 UC, 2 actor với phạm vi field khác nhau.
>
> **⚠️ Lưu ý phát hiện thêm (không thuộc phạm vi A8, cần team/Huy xác nhận riêng)**: UC-095 Normal Flow bước 4 trích dẫn "BR-030" cho "operational rules", nhưng BR-030 trong `business_rules.md` thực tế là "Liên kết học sinh với phụ huynh" (Student-Parent Mgmt) — không liên quan đến cấu hình trường. Đây có thể là 1 ghost-citation khác giống lỗi BR-038/BR-040 đã sửa ở mục A1/A2, hoặc UC-095 cần 1 BR mới về "operational rules" mà hiện chưa tồn tại. Trần chi phí thực đơn (BR-080) và bán kính geofence (BR-028) không có vấn đề trích dẫn.
>
> **✅ Đã xác nhận 2026-07-01 (loại bỏ Campus Config)**: chủ dự án xác nhận hệ thống không quản lý campus — màn `Campus Config` (System Admin) đã bị loại bỏ hoàn toàn (xem `Web/SystemAdmin/13_CampusConfig.md`). Đối chiếu lại nguyên văn BR-028: *"School Administration có thể cấu hình bán kính này trong khoảng 50m – 500m"* — xác nhận rõ **field bán kính geofence chính thức thuộc School Administration**, không phải System Admin. Field #2 dưới đây nay là chính thức, không còn là Giả định.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Trần chi phí thực đơn (per suất ăn) | Input (number, VNĐ) | — | BR-080 | **[Giả định]** |
| 2 | Bán kính geofence | Input (number, mét) | 50-500m | BR-028 | Mặc định 200m — xác nhận 2026-07-01: BR-028 nêu rõ School Administration là chủ sở hữu cấu hình này (không phải System Admin) |
| 3 | Danh sách email nhận báo cáo định kỳ | Input (multi-email) | — | BR-179 | **[Giả định]** |
| 4 | Nút "Lưu" | Button (primary) | — | — | **[Giả định]** |

### Trạng thái màn hình
- Loading: **[Giả định]**.
- Empty: Không áp dụng.
- Error: **[Giả định]** — chưa có MSG code chính thức.
- Success: **[Giả định]**.

---

## Platform Layout — Web
**Entry/Exit points**: **[Giả định]** — vào từ Sidebar nhóm "Cấu hình" (nếu tồn tại như trang riêng).
**Bố cục**: **[Giả định]** — Dashboard Shell, form 1 cột.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình School Configuration, nền tảng Web, actor School Administration.
Bố cục: Dashboard Shell, form 1 cột — trần chi phí thực đơn, bán kính geofence (50-500m, mặc định 200m), danh sách email nhận báo cáo định kỳ, nút Lưu.
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md, dòng 1777–1797 (UC-095 Update Information School) — xác nhận 2026-07-01
- business_rules.md BR-028 (xác nhận School Administration sở hữu cấu hình bán kính geofence), BR-080, BR-179
- 14_Screen_Authorization_Matrix.md khối 3
- `Web/SystemAdmin/13_CampusConfig.md` (đã loại bỏ — lý do và bối cảnh chuyển field)
