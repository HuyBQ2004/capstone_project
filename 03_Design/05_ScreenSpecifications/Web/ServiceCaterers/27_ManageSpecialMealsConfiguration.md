# Screen Spec — Manage Special Meals Configuration

**Actor**: Service Caterers | **UC liên quan**: UC-023 - Create Special Meal Plan (Report 3 §3.2) | **Status**: Draft
**BR liên quan**: BR-063, BR-065, BR-066, BR-069, BR-082, BR-156, BR-157
**MSG liên quan**: MSG-072 (Success), MSG-051 (Error — chặn nếu trùng allergen), MSG-004/MSG-005 (Error)

> **Lưu ý mâu thuẫn nguồn**: `Report 3.md` UC-023 Normal Flow bước 5 trích "BR-071" cho "dụng cụ nấu riêng biệt", nhưng theo `business_rules.md` hiện hành BR-071 = "Chứng nhận lô hàng bắt buộc" (Ingredient Receiving), không liên quan chế biến riêng biệt. Quy tắc đúng là **BR-082** (Chế biến riêng cho bữa ăn đặc biệt) — Screen Spec này dùng BR-082 (đúng), không dùng BR-071.
>
> **✅ Đã xử lý (2026-07-01)**: field "Món thay thế cho 5 loại dị ứng phổ biến nhất" (trước đây trích BR-081) **đã bị gỡ bỏ** — team quyết định xoá hẳn BR-081 khỏi `business_rules.md` vì quy tắc không thực tế (danh sách "5 loại phổ biến" không có định nghĩa rõ, không có data model hỗ trợ, trùng chức năng với chính màn hình này — Special Meal Plan đã là cơ chế "món thay thế" thực sự cho từng học sinh dị ứng đã xác minh). Không cần thay thế bằng field nào khác.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Danh sách học sinh có dị ứng/chế độ ăn đặc biệt đã xác minh | Table (Web) / Card list (Mobile) | Chỉ hiển thị hồ sơ đã Medical Staff xác nhận | BR-065 | UC-023 Normal Flow bước 2 |
| 2 | Loại chế độ ăn đặc biệt | Badge/Select | Halal, Vegetarian, Gluten-Free, Nut-Free, khác | BR-066 | UC-023 Normal Flow bước 4 |
| 3 | Công thức + hướng dẫn nấu riêng biệt | Textarea | Bắt buộc mô tả dụng cụ nấu riêng, chống nhiễm chéo | BR-082 | UC-023 Normal Flow bước 5 |
| 4 | Khai báo nguyên liệu + dấu vết dị ứng tiềm ẩn | Tag input | Bắt buộc đầy đủ | BR-069 | UC-023 Normal Flow bước 6 |
| 5 | Kiểm tra không trùng allergen với hồ sơ học sinh | Validation tự động | Chặn lưu nếu phát hiện trùng | BR-063 | UC-023 Normal Flow bước 7, MSG-051 |
| 6 | Nút "AI Assist" gợi ý công thức an toàn | Button | Mở panel gợi ý (Gemini AI) | BR-156, BR-157 | UC-023 Alternative Flow AF1 |
| 7 | Nút "Lưu Kế hoạch Bữa ăn Đặc biệt" | Button | Lưu, hiển thị MSG-072 | — | UC-023 Normal Flow bước 8 |
| 8 | **Mức độ thay đổi** | Select | `PARTIAL` (mặc định) / `FULL` | BR-062 | Bổ sung 21/07/2026 — xem ghi chú dưới |
| 9 | **Bộ món của suất trọn bộ** (chỉ khi FULL) | Multi-select từ Thư viện món | ≥1 món; hiện tổng kcal/đạm đối chiếu chuẩn ±10% | BR-062 | Chọn từ `38_DishLibrary` nên dinh dưỡng cộng tự động |

### Bổ sung 21/07/2026 — hai mức độ thay đổi

Mô hình ban đầu giả định **suất đặc biệt = thay từng món lẻ**. Đúng với ăn chay/Halal (thay 2–3 món) nhưng **sai với tiểu đường**: đổi cơm trắng sang gạo lứt, giảm khẩu phần tinh bột, bỏ món ngọt — gần như một thực đơn khác, và vì suất chỉ là các mẩu rời rạc nên **không kiểm được tổng kcal/đạm** của chính suất đó (BR-062).

| Mức độ | Ghi ở đâu | Khi nào dùng |
|---|---|---|
| `PARTIAL` (mặc định) | `substitutes[]` gắn vào món gốc trong thực đơn | Nhóm chỉ kiêng vài món |
| `FULL` | Bản ghi `MenuVariant` riêng (`menuId` + `mealType` + `groupId`) | Nhóm ăn trọn bộ món khác — chế độ y tế |

- **Nhóm 0 học sinh không lập được suất** (cả hai mức độ): nhóm chế độ ăn phải phát sinh từ hồ sơ HS đã Y tế xác minh (BR-065/066) ⇒ màn này **không có** lựa chọn "tạo nhóm mới" tuỳ ý.
- **Thành phần món thay thế bắt buộc có định lượng + mã nguyên liệu** (bảng `IngredientRows` dùng chung với màn lập thực đơn): thiếu định lượng thì nhu cầu xuất kho của suất đặc biệt bằng 0 và không truy xuất được lô (BR-069/076).
- **Kiểm dị ứng cho cả hai mức độ**: món thay thế / bộ món trọn bộ phải đối chiếu hồ sơ dị ứng **đã xác minh** của chính học sinh trong nhóm+khối đó — hợp chế độ ăn không có nghĩa là an toàn (HS ăn chay vẫn có thể dị ứng đậu nành). Chạm thì chặn lưu (MSG-051).
- Suất `FULL` hiển thị kèm dinh dưỡng ở màn **duyệt thực đơn của Ban Giám hiệu** để BGH duyệt đúng cái mình duyệt.

### Trạng thái màn hình
- Loading: `Skeleton` khi tải danh sách học sinh.
- Empty: Chưa có học sinh nào cần kế hoạch bữa ăn đặc biệt.
- Error: `Alert variant="destructive"` MSG-051 nếu phát hiện trùng allergen (chặn lưu).
- Success: `Toast` MSG-072.

---

## Platform Layout — Web

**Entry/Exit points**: Từ Update Menu Screen (`04_Service_Caterers_Screen_Flow.md`: `UpdateMenuScr --> SpecialMealCfg`) hoặc từ Meal Service Management Dashboard. Thoát: lưu xong quay lại màn gọi.
**Bố cục**: Trang 2 cột — danh sách học sinh bên trái (`Table`), form cấu hình kế hoạch bữa ăn đặc biệt bên phải.

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Manage Special Meals Configuration, nền tảng Web, actor Service Caterers.
Bố cục: 2 cột — Table danh sách học sinh dị ứng/đặc biệt bên trái, form cấu hình (loại chế độ ăn, công thức riêng, khai báo nguyên liệu) bên phải, nút AI Assist, nút Lưu Kế hoạch.
Thành phần bắt buộc: cảnh báo trùng allergen (MSG-051).
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-072).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- Report 3.md dòng 2845–2863 (UC-023 - Create Special Meal Plan)
- business_rules.md BR-063, BR-065, BR-066, BR-069, BR-082, BR-156, BR-157
- error_message.md MSG-072, MSG-051
