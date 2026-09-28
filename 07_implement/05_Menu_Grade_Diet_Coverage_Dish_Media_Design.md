# 05 — Thiết kế: Thực đơn mẫu theo khối, ràng buộc không trùng menu, checklist đủ nhóm chế độ ăn, ảnh/mô tả món

**Ngày lập:** 2026-08-05 · **Trạng thái:** ✅ XONG toàn bộ (backend + FE), verify qua curl thật
**Người đề xuất:** Nam (chủ MF02, qua trao đổi khi tiếp tục nối FE MF05)

> **Tóm tắt 1 câu:** 3 việc chốt cùng lúc — Thực đơn mẫu (MenuTemplate) gắn được với khối để
> cảnh báo dị ứng/chế độ ăn trước, ràng buộc "1 bữa/khối không trùng menu" mở rộng sang cả thực
> đơn theo chế độ ăn riêng (trước chỉ chặn thực đơn chuẩn), và Dish có thêm ảnh/mô tả/hướng dẫn
> chế biến.

---

## 1. Hiện trạng đã xác nhận qua đọc code (không suy đoán)

Trước khi thiết kế, đọc lại `MenuServiceImpl`/`DietCoverageValidator`/migration `V8` và phát
hiện phần lớn yêu cầu về "đủ menu cho các nhóm chế độ ăn" **đã có sẵn**, không phải viết mới
từ đầu:

- `Menu.grade` — **đã là cột thật** (`VARCHAR(16)`, VD `"Khối 3"`), không phải chỉ FE mock.
- `ux_menu_all_per_slot` (V8) — **đã chặn trùng** menu CHUẨN (`audience='ALL'`) theo
  `(school_id, menu_date, grade, meal_type)`, nhưng **CHỈ cho audience=ALL**.
- `DietCoverageValidator.findGaps()` — **đã tồn tại và đã được gọi trong `doSubmit()`** (BR-065):
  chặn cứng submit() một menu ALL nếu có nhóm chế độ ăn đang có HS đã duyệt (`APPROVED`) trong
  khối mà CHƯA có menu (không REJECTED) cho đúng (khối, ngày, buổi) đó.
- `ReferenceResolver.verifiedAllergenCountsByGrade()` / `.approvedSpecialDietStudentCountsByGrade()`
  — 2 query native đã có sẵn, CHỈ dùng nội bộ (AllergyConflictChecker/DietCoverageValidator),
  **chưa từng lộ ra GET endpoint nào cho FE hiển thị trước**.

**Chưa có** (đúng phần việc thật sự cần làm):
- `MenuTemplate` không có field `grade`.
- Ràng buộc unique chỉ áp `audience=ALL` — thực đơn riêng (VD "Ăn chay") vẫn tạo trùng được
  nhiều bản cho cùng (khối, ngày, buổi).
- Không có endpoint GET nào lộ 2 query có sẵn ở trên ra FE (chỉ dùng nội bộ khi submit()/approve()).
- `Dish` chưa có ảnh, mô tả, hướng dẫn chế biến.
- `runApprovalGate()` (chạy ở APPROVE/PUBLISH) **không** tái kiểm diet coverage — chỉ `doSubmit()`
  kiểm. Menu đã qua submit() nhưng menu riêng "phủ" nó bị REJECTED/xoá SAU đó vẫn duyệt/công bố
  được — khe hở đua hẹp.

## 2. Quyết định (Nam, 05/08/2026 — AskUserQuestion 3 câu, cả 3 chọn phương án khuyến nghị)

1. **MenuTemplate.grade — NULLABLE, tuỳ chọn.** Trống = dùng chung mọi khối (không đổi hành vi
   cũ của các mẫu hiện có); set khi mẫu cần khác biệt dinh dưỡng/dị ứng riêng cho khối đó.
2. **Mở rộng ràng buộc unique sang audience đặc biệt.** Mỗi (khối, ngày, buổi, audience) chỉ 1
   menu KHÔNG REJECTED — kể cả thực đơn theo chế độ ăn riêng, không chỉ thực đơn chuẩn.
3. **Chặn cứng khi PUBLISH** (không chỉ cảnh báo) nếu thiếu menu cho nhóm chế độ ăn cần —
   `doSubmit()` đã chặn từ trước; bổ sung tái kiểm ở `runApprovalGate()` (APPROVE/PUBLISH) để
   đóng khe hở đua ở mục 1 (§1).

## 3. Triển khai — Backend

### 3.1. Migration `V33__mf05_template_grade_dish_media_diet_unique.sql`
- `menu_templates.grade VARCHAR(16)` — nullable.
- `dishes.image_url VARCHAR(500)`, `dishes.description VARCHAR(2000)`,
  `dishes.cooking_instructions TEXT`.
- `ux_menu_diet_per_slot` — unique index mới trên
  `(school_id, menu_date, grade, meal_type, diet_group_id) WHERE diet_group_id IS NOT NULL AND status <> 'REJECTED'`
  — cùng tinh thần `ux_menu_all_per_slot` (V8) nhưng cho audience khác ALL. Hai index KHÔNG
  chồng lấn (loại trừ lẫn nhau theo `diet_group_id IS NULL`/`IS NOT NULL`).

### 3.2. `MenuTemplate` — grade nullable
`domain/MenuTemplate.java`, `dto/MenuTemplateDtos.java` (Create/Update/Response record thêm
`grade`), `service/MenuTemplateServiceImpl.java` (wire create/update, `""`/null → null),
`mapper/MenuTemplateMapper.java`, `service/MenuTemplateSpec.java` (thêm `grade` vào FIELD_MAP
để lọc được `GET /menu-templates?grade=`).

### 3.3. Pre-check thân thiện thay lỗi DB thô
Trước đây tạo/sửa menu trùng slot chỉ có **duy nhất** ràng buộc DB (`ux_menu_all_per_slot`) làm
lưới an toàn cuối — vi phạm sẽ ném `DataIntegrityViolationException` thô (500, không có global
handler bắt riêng). Thêm `MenuServiceImpl.assertSlotAvailable()` — kiểm TRƯỚC khi lưu ở cả
`createDraft()` và `update()` (khi PUT đổi `grade`/`menuDateIso`/`mealType`/`audience`), ném
`MnuExceptions.duplicateSlot()` (409 MENU-409) thân thiện. Áp dụng cho CẢ audience=ALL lẫn
audience đặc biệt — cùng 1 hàm, không phân biệt.

### 3.4. `GET /menus/grade-summary` — cảnh báo dị ứng/chế độ ăn theo khối
Lộ `ReferenceResolver.verifiedAllergenCountsByGrade()` +
`.approvedSpecialDietStudentCountsByGrade()` (đã có sẵn, trước chỉ dùng nội bộ) qua endpoint mới
— dùng khi soạn Thực đơn mẫu cho một khối cụ thể, KHÔNG gắn ngày/buổi. Trả
`GradeSummaryResponse{grade, allergens[], dietGroups[]}` — chỉ số đếm, không danh tính HS (PDPD,
cùng nguyên tắc D11 áp dụng toàn hệ thống).

### 3.5. `GET /menus/diet-coverage` — checklist "cần menu cho nhóm nào"
`DietCoverageValidator` thêm method `coverage()` trả **CẢ nhóm đã có menu** (không chỉ nhóm
thiếu như `findGaps()` cũ) — `findGaps()` refactor thành gọi `coverage()` rồi lọc
`!covered`, một nguồn sự thật duy nhất. Endpoint mới `GET /menus/diet-coverage?grade=&menuDateIso=&mealType=`
trả `DietCoverageResponse{grade, menuDateIso, mealType, items[{dietGroupCode, dietGroupLabel,
students, covered}]}` — dùng ở màn lập thực đơn để biết TRƯỚC khi bấm nộp duyệt, không phải đợi
`submit()` báo lỗi mới biết cần thêm gì.

### 3.6. Tái kiểm diet coverage ở APPROVE/PUBLISH
`runApprovalGate()` (chạy ở CẢ `approve()` lẫn `publish()`, kể cả nhánh PUT nhảy thẳng
PENDING_APPROVAL→PUBLISHED) thêm gọi `dietCoverageValidator.findGaps()` cho menu `audience=ALL`
— đóng khe hở §1: menu riêng "phủ" một nhóm bị REJECTED/xoá SAU khi submit() đã qua vẫn không
cho duyệt/công bố menu chuẩn tương ứng.

### 3.7. `Dish` — ảnh/mô tả/hướng dẫn chế biến
`domain/Dish.java` thêm `imageUrl`/`description`/`cookingInstructions`; `DishDtos`/`DishMapper`/
`DishServiceImpl` wire đủ create/update. Ảnh qua endpoint riêng
`POST /dishes/{code}/image` (multipart, D10-BIS — mirror `ProfileService.updatePhoto()`, URL
CÔNG KHAI lưu thẳng vào `imageUrl` vì ảnh món không phải tài liệu riêng tư), giới hạn
jpeg/png/webp, key lưu `dish-images/{code}.{ext}`.

## 4. Triển khai — Frontend

- `lib/mock-data.ts` — `interface MenuTemplate` thêm `grade?: string`; `interface Dish` thêm
  `imageUrl?`/`description?`/`cookingInstructions?` (2 interface này dùng làm type shim cho
  response THẬT — `"menu-templates"`/`"dishes"` đã nối thật từ trước, không phải mock).
- `lib/api/types.ts` — thêm alias `GradeSummaryDto`/`DietCoverageDto` (+2 item-type con).
- `service-caterers/menu-templates/page.tsx` — thêm Select khối (trống = "Dùng chung mọi khối",
  nguồn `GRADE_LEVELS`), cột + bộ lọc "Khối" ở bảng, và banner cảnh báo dị ứng/chế độ ăn của
  khối đang chọn khi soạn mẫu (đọc `menus/grade-summary`, chỉ gọi khi đã chọn khối cụ thể).
- `service-caterers/menus/page.tsx` (Lịch thực đơn tuần) — `TemplatePickerDialog` (mở khi bấm
  "+ Thêm thực đơn" cho 1 ô ngày×buổi) thêm banner checklist "còn thiếu nhóm chế độ ăn nào"
  (đọc `menus/diet-coverage` theo đúng khối/ngày/buổi của ô đang mở) — biết TRƯỚC khi submit()
  chặn cứng, không phải đoán.
- `service-caterers/dishes/page.tsx` — form thêm ô chọn ảnh (preview + upload qua
  `useUploadMutation("dishes")` NGAY SAU khi tạo/sửa món xong, vì endpoint ảnh cần `code` đã
  tồn tại) + 2 Textarea mô tả/hướng dẫn chế biến; bảng danh sách hiện thumbnail nếu có ảnh.

**Ghi chú kỹ thuật quan trọng — vì sao KHÔNG gọi `usePagedList("classrooms")` cho dropdown
khối**: `ClassroomController` hiện `@PreAuthorize("hasRole('SCHOOL_ADMIN')")` nên Service
Caterers gọi vào sẽ 403 (gap đã ghi nhận sẵn ở `real-resources.ts` dòng ~207 — "quyết định của
Nam, không tự mở rộng ở đây"; `service-caterers/menus/page.tsx` gốc đã có gap này từ trước, không
phải do đợt này). Dropdown khối ở `menu-templates/page.tsx` dùng tạm hằng số `GRADE_LEVELS`
(deprecated nhưng vẫn hoạt động) thay vì classrooms thật — KHÔNG phải giải pháp cuối cùng, chỉ
né gap có sẵn chứ không mở rộng phạm vi đợt này để tự quyết định cách scope Service Caterers đọc
classrooms (việc đó vẫn cần quyết định riêng của Nam như đã ghi ở `real-resources.ts`).

## 5. Verify

- Backend: `mvn package` sạch, migrate V33 áp thành công (log Flyway xác nhận), restart +
  `npm run gen:api` lại.
- Curl thật (tài khoản `duchm.caterer.synthetic`):
  - `GET /menus/grade-summary?grade=Khối 3` → trả đúng allergen counts từ dữ liệu seed thật.
  - `GET /menus/diet-coverage?grade=Khối 3&menuDateIso=2026-08-06&mealType=LUNCH` → `items: []`
    (đúng — không có HS nào cần chế độ ăn đặc biệt đã duyệt ở khối/ngày đó trong seed).
  - Tạo `MenuTemplate` với `grade="Khối 3"` → lưu đúng, đọc lại đúng UTF-8 (xác nhận qua
    `docker exec psql` trực tiếp — lần đầu test qua `curl -d` bash bị lỗi encoding do
    shell/quoting, KHÔNG phải bug backend; sửa bằng `--data-binary @file.json`, cùng lớp vấn đề
    đã gặp ở phiên trước với field `note` tiếng Việt).
  - Tạo 1 menu `audience=ALL` cho (Khối 3, 2026-08-20, LUNCH) → 201; tạo lần 2 CÙNG slot → 409
    `MENU-409` với message rõ ràng (trước đây sẽ là 500 DB thô).
  - `POST /dishes/com-trang/image` (ảnh PNG test 1×1) → 200, `imageUrl` trả về đúng
    `/media/dish-images/com-trang.png`; `PUT /dishes/com-trang` với `description`/
    `cookingInstructions` → lưu và đọc lại đúng.
  - Toàn bộ dữ liệu test (menu, template, ảnh, description) đã dọn sạch sau verify
    (`docker exec psql DELETE`/`UPDATE ... SET NULL`, xoá file ảnh test khỏi
    `data/uploads/dish-images/`).
- Frontend: `npx tsc --noEmit`, `npx eslint`, `npm run audit` sạch toàn dự án sau mỗi bước.
