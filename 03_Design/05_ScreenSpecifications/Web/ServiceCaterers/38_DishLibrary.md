# Screen Spec — Dish Library (Thư viện món)

**Actor**: Service Caterers | **UC liên quan**: hỗ trợ UC-041 - Create Menu / UC-043 - Update Menu (màn nền, không phải UC độc lập) | **Status**: Draft
**BR liên quan**: BR-062, BR-063, BR-069, BR-076
**MSG liên quan**: MSG-042 (Success), MSG-004 (Error — không tìm thấy)

> **Vì sao có màn này** (bổ sung 21/07/2026): trước đây tên món trong thực đơn là **chuỗi tự do** — mỗi lần lập thực đơn phải gõ lại thành phần, định lượng, kcal; món chỉ nối với nhau bằng tên nên thống kê dinh dưỡng và truy xuất nguồn gốc đều lệch. Thư viện món là nơi khai món **một lần** rồi dùng lại ở mọi thực đơn.

## Business Logic (dùng chung mọi nền tảng)

### Danh sách Field / Thành phần
| # | Thành phần | Loại | Validation/Ràng buộc | BR | Ghi chú |
| --- | --- | --- | --- | --- | --- |
| 1 | Tìm theo tên món | Input search | — | — | Lọc client, không phân trang riêng |
| 2 | Lọc theo phân loại | Select | Món chính / Món mặn / Canh / Rau / Bữa phụ | — | |
| 3 | Hiện cả món đã ngừng dùng | Checkbox | Mặc định TẮT | — | |
| 4 | Bảng món | Table | Cột: món · phân loại · kcal/suất · thành phần · tác nhân dị ứng | BR-069 | Đánh dấu số thành phần **ngoài danh mục** |
| 5 | Tên món | Input | Bắt buộc; **không trùng tên** món đã có | — | Trùng tên → yêu cầu sửa món cũ thay vì tạo bản trùng |
| 6 | Phân loại | Select | Bắt buộc | — | |
| 7 | NCC nguyên liệu chính, Bếp chế biến | Input | Tùy chọn | BR-076 | Hiển thị ở màn dinh dưỡng của Phụ huynh |
| 8 | Kcal / suất | Number | Bắt buộc, > 0 | BR-062 | |
| 9 | Đạm / bột đường / béo (g) | Number | Tùy chọn, ≥ 0 | BR-062 | |
| 10 | Thành phần + định lượng cho 1 suất | Bảng động (name + quantity + unit) | Bắt buộc ≥1 dòng; **mỗi dòng phải có định lượng > 0** | BR-069, BR-076 | Chọn từ danh mục nguyên liệu để có `ingredientId`; ngoài danh mục thì cảnh báo không truy xuất được lô |
| 11 | Tác nhân dị ứng | Badge (auto) + combobox thêm tay | **Auto suy từ thành phần**, không nhập tay được phần auto | BR-063 | Chỉ khai tay cho nguyên liệu ngoài danh mục |
| 12 | Ngừng dùng / Dùng lại | Button toggle | **Không có xoá** | — | Thực đơn cũ còn trỏ `dishId`; xoá là mất truy xuất lịch sử |

### Quy tắc nghiệp vụ riêng
- **Định lượng là bắt buộc**: món thiếu định lượng khi vào thực đơn sẽ không sinh được nhu cầu xuất kho (nhân với số suất ra 0) và không lên được đơn đặt hàng.
- **Allergen không lưu**: dẫn xuất từ thành phần mỗi lần đọc. Thành phần có tôm mà người nhập quên khai "Tôm" thì hệ thống **vẫn** cảnh báo.
- **Không xoá món**: chỉ `isActive = false`. Món ngừng dùng không còn hiện trong combobox khi lập thực đơn nhưng thực đơn đã lập vẫn đọc được.

### Trạng thái màn hình
- Loading: dòng "Đang tải thư viện món…" trong bảng.
- Empty: "Không có món nào khớp bộ lọc."
- Error: `Alert` trong dialog nêu rõ field sai (trùng tên, thiếu kcal, thiếu định lượng).
- Success: `Toast` MSG-042 khi thêm/sửa/ngừng dùng.

---

## Platform Layout — Web

**Entry/Exit points**: Sidebar Service Caterers → nhóm "Thực đơn" → "Thư viện món". Cũng vào được từ màn lập thực đơn (link "thư viện" ở ô tên món). Thoát: điều hướng sidebar.
**Bố cục**: `PageHeader` + nút "Thêm món" → 1 `Card` chứa thanh lọc và `Table`; tạo/sửa bằng `Dialog` cuộn dọc.

## Liên kết với màn khác
- **03_CreateMenuWorkspace / 06_UpdateMenuScreen**: ô tên món là combobox đọc thư viện; chọn xong tự đổ thành phần + kcal + macro và chốt `dishId`. Món gõ tay có nút "Lưu vào thư viện" ngay tại chỗ.
- **27_ManageSpecialMealsConfiguration**: suất **trọn bộ** (MenuVariant mode FULL) chọn món từ chính thư viện này ⇒ tổng kcal/đạm của suất kiểm được theo BR-062.
- **Parent 03_NutritionDetail**: đọc trực tiếp bản ghi món (`/parent/nutrition/[dishId]`).

## AI Generation Prompt ⭐

```
Tạo wireframe cho màn hình Dish Library, nền tảng Web, actor Service Caterers.
Bố cục: PageHeader + nút Thêm món; Card chứa ô tìm kiếm, select phân loại, checkbox hiện món ngừng dùng, và Table (món, phân loại, kcal/suất, thành phần, tác nhân dị ứng, nút Sửa / Ngừng dùng).
Dialog tạo-sửa: tên món, phân loại, NCC, bếp, 4 ô dinh dưỡng, bảng thành phần + định lượng/suất, badge tác nhân dị ứng auto.
Thành phần bắt buộc: cảnh báo thành phần ngoài danh mục; KHÔNG có nút xoá món.
Áp dụng Design System: xem 00_Foundation/Design_System.md.
Trạng thái cần thể hiện: loading, empty, error, success (MSG-042).
KHÔNG được thêm field/nghiệp vụ ngoài danh sách trên.
```

## Nguồn dữ liệu
- business_rules.md BR-062, BR-063, BR-069, BR-076
- error_message.md MSG-004, MSG-042
- `review/plan_menu_dish_library_and_special_meal_variants.md` — Đợt B (quyết định thiết kế)
