# Kế hoạch — Thực đơn, Suất ăn đặc biệt, Thư viện món & Nối kho ↔ Đặt hàng

> **Phạm vi:** `03_Design/08_FrontEnd/Web` — cụm thực đơn & suất ăn đặc biệt (Service Caterers), kéo theo màn Parent, tầng cá nhân hoá, và luồng xuất kho/đặt hàng.
> **Cập nhật:** 21/07/2026 · **Duyệt:** nhóm trưởng (toàn quyền chỉnh sửa).
> **File này tự chứa** — phiên làm việc mới đọc file này là đủ ngữ cảnh để làm tiếp.

---

## 0. Phân vai đã CHỐT (căn cứ mọi quyết định bên dưới)

| Màn | Vai trò |
|---|---|
| `service-caterers/menus` (+ `menu-form`) | **Thực đơn chính**. Theo NGÀY × KHỐI. Phát hiện món chạm dị ứng của ≥1 HS → bắt khai **món thay thế nhóm `ALLERGY`**. |
| `service-caterers/special-meals` | **Nhóm chế độ ăn đặc biệt** (chay, tiểu đường, Halal…). Khai **suất theo `DietGroup.id`**. Cũng theo KHỐI. |
| `teacher/distribution`, `service-caterers/meal-service` | Khâu **phát suất** — nơi duy nhất được hiển thị danh tính học sinh. |

**Nguyên tắc quyền riêng tư (đã áp):** màn lập kế hoạch (menu, special-meals) chỉ hiện **lớp + số suất**, KHÔNG hiện tên/mã học sinh — đơn vị suất ăn là bên thứ ba. Ngữ cảnh gửi AI (BR-156/157) cũng chỉ gửi số liệu tổng hợp.

**Bữa ăn đặc biệt CÓ xếp theo khối** — đã xác nhận đúng, vì: (1) suất gắn vào `blocksData` của một menu vốn là ngày×khối; (2) BR-062 chuẩn dinh dưỡng khác nhau theo nhóm tuổi; (3) món gốc mỗi khối mỗi khác. Nhóm trải nhiều khối thì mỗi khối lập riêng ("hướng A", chốt 07/07).

---

## 1. Trạng thái các đợt

| Đợt | Nội dung | Trạng thái |
|---|---|---|
| **A** | `substitutes[]` — 1 món → N suất theo nhóm | ✅ **XONG** — commit `e9b9a05` |
| **A2** | Vá 3 lỗ hổng màn Suất ăn đặc biệt (L1/L2/L3) | ✅ **XONG** |
| **B** | Thư viện món (Dish master) | ✅ **XONG** |
| **E** | Nối nhu cầu nguyên liệu ↔ đặt hàng | ✅ **XONG** |
| **C** | `MenuVariant` FULL (nhóm đổi trọn bộ) | ✅ **XONG** |
| **D** | Dọn deprecated + docs | ✅ **XONG** |
| **F** | Vá màn duyệt thực đơn của BGH (phát sinh) | ✅ **XONG** |

### Đã xong ở Đợt A (không làm lại)

- `DishForm.substitutes: SubstituteDish[]`; `substitute?` giữ **deprecated chỉ để ĐỌC** record cũ.
- **`lib/menu-substitutes.ts` = nguồn truy cập DUY NHẤT**: `subsOf` · `subOfGroup` · `allergySubOf` · `upsertSub` · `removeSub` · `totalSubPortions` · `groupIdOf` · `GROUP_ALLERGY`.
  > ⚠️ **QUY ƯỚC BẮT BUỘC:** KHÔNG đọc/ghi `dish.substitute` trực tiếp ở bất kỳ đâu ngoài file này. Có 82 vị trí liên quan — đây là cách duy nhất không sót.
- `special-meals` dùng `upsertSub` → hết ghi đè suất dị ứng.
- `menu-form` khoá nhóm `ALLERGY`, hiển thị read-only các suất chế độ ăn đang có.
- `mealRequirements` nổ định mức cho **mọi** suất (trước chỉ tính 1).

---

## 2. ĐỢT A2 — Vá 3 lỗ hổng Suất ăn đặc biệt (ưu tiên cao nhất)

File chính: `src/app/(app)/service-caterers/special-meals/page.tsx`

### L1 — Chặn tạo "thực đơn rác" cho nhóm không có học sinh

**Hiện trạng:** dropdown chọn nhóm (`~dòng 327`) *hiển thị* số HS nhưng **vẫn cho chọn** nhóm 0 HS. Nút **"+ Tạo nhóm mới…"** tạo nhóm tuỳ ý **không gắn HS nào**.

**Sửa:**
- Lọc dropdown: chỉ liệt kê nhóm có `dietGroupMembersInGrade(g.id, grade).length > 0`.
- Nếu khối không có nhóm nào đủ điều kiện → hiện thông báo thay vì dropdown rỗng.
- **"Tạo nhóm mới"**: chỉ cho phép nếu nhóm mới được gắn ≥1 HS; nếu mô hình chưa hỗ trợ gắn HS vào nhóm tuỳ ý thì **bỏ hẳn lựa chọn này** và ghi chú lý do (nhóm chế độ ăn phải xuất phát từ hồ sơ HS đã Y tế xác minh — BR-065/066).

### L2 — Món thay thế phải có ĐỊNH LƯỢNG + `ingredientId` (nặng nhất)

**Hiện trạng (`~dòng 235`):**
```ts
ingredients: subIngredients.split(",").map((name) => ({ name, quantity: "", unit: "g" }))
```
Nhập bằng **Textarea phẩy-ngăn-cách** → `quantity` **rỗng**, không có `ingredientId`.

**Hệ quả dây chuyền (nghiêm trọng):**
- `mealRequirements` nhân `quantity × portions` = **0** ⇒ **bếp không xuất kho gì cho suất đặc biệt**
- Không `ingredientId` ⇒ **không truy xuất được lô** (BR-076)
- Kéo theo: đơn đặt hàng (Đợt E) sẽ **thiếu toàn bộ nguyên liệu suất đặc biệt**

**Sửa:** thay Textarea bằng **cùng component `IngredientRows`** mà `menu-form` dùng (name + quantity + unit + `ingredientId` khi chọn từ catalog). Đây là điều kiện tiên quyết của Đợt E.

### L3 — Kiểm dị ứng cho món thay thế của nhóm

**Hiện trạng:** `submit()` (`~dòng 212-226`) chỉ validate: tên, kcal, số suất, có thành phần, tích chế biến riêng. **KHÔNG** đối chiếu allergen.

**Rủi ro thật:** HS ăn chay **đồng thời** dị ứng đậu nành → món "Đậu phụ sốt" lọt lưới.

**Sửa:** tái dùng đúng cơ chế của `menu-form`:
```ts
// allergen của món thay thế ∩ hồ sơ dị ứng ĐÃ XÁC MINH của chính HS trong nhóm+khối đó
const members = dietGroupMembersInGrade(groupId, grade);
const memberAllergens = [...new Set(members.filter(isAllergyVerified).flatMap(s => s.allergies))];
const subAllergens = dishAllergensOf(subDish);        // lib/menu-personalization
const conflicts = memberAllergens.filter(sa => subAllergens.some(da => allergenTouches(sa, da)));
// conflicts.length > 0 → CHẶN submit, nêu rõ tác nhân
```
Dùng `allergenTouches` (ô Hải sản 2 chiều) chứ **không** so khớp chuỗi.

**Nghiệm thu A2:** không tạo được suất cho nhóm 0 HS · suất đặc biệt có định lượng và sinh đúng nhu cầu ở màn Xuất kho · món thay thế chạm dị ứng của thành viên nhóm thì bị chặn kèm lý do.

### Đã xong ở Đợt A2 (không làm lại)

- **L1** — dropdown nhóm chỉ liệt kê nhóm có `dietGroupMembersInGrade > 0`; đổi khối mà nhóm hết HS thì tự bỏ chọn; **bỏ hẳn "+ Tạo nhóm mới…"** (nhóm chế độ ăn phải phát sinh từ hồ sơ HS đã Y tế xác minh — BR-065/066, có ghi chú tại chỗ trong UI). Nhóm "custom" trong bảng tuần vẫn ĐỌC được record cũ.
- **L2** — `IngredientEditor` (bảng name + quantity + unit + `ingredientId`) **đã export từ `menu-form`** cùng `emptyIngredient`, `IngredientCatalogDatalist`, `INGREDIENT_DATALIST_ID`; màn special-meals dùng chung, thay Textarea phẩy-ngăn-cách. Submit chặn dòng thiếu định lượng.
  > `mealRequirements()` nay ưu tiên `ing.ingredientId` rồi mới tra theo tên — nguyên liệu gõ thêm hậu tố không còn trượt khoá join (BR-076).
- **L3** — submit đối chiếu allergen món thay thế ∩ hồ sơ dị ứng **đã xác minh** của chính HS trong nhóm+khối, qua `allergenTouches`; có Alert đỏ realtime + nút "Gắn vào thực đơn" bị disable.
  > `dishAllergensOf` (`lib/menu-personalization`) nới tham số về `Pick<…,"ingredients"|"manualAllergens">` để nhận cả `SubstituteDish` — món gốc và món thay thế soi bằng **một** luật.

---

## 3. ĐỢT B — Thư viện món (Dish master)

**Vấn đề:** `DishForm.name` là chuỗi tự do; mỗi lần lập thực đơn phải gõ lại thành phần + định lượng + kcal. Trong khi dish master **đã tồn tại** — `DISH_NUTRITION` (`lib/mock-data.ts`, 16 món, có `id`, kcal, macro, `ingredients`, `allergens`) — nhưng `menu-form` **không hề import**.

Hiện chỉ có "Sao chép món từ thực đơn khác" — workaround, không phải thư viện.

**Việc:**
1. Nâng `DishNutrition` → `Dish` master. ⚠️ `ingredients` hiện là **`string[]` (chỉ tên, KHÔNG định lượng)** → phải nâng 16 món seed lên `DishIngredient[] { ingredientId, name, quantity, unit }`.
2. Thêm resource `dishes` vào `lib/api/db.ts` (hiện **chưa có**). `allergens` **dẫn xuất** qua `resolveIngredientAllergens`, bỏ nhập tay.
3. `menu-form`: ô tên món → **combobox chọn từ thư viện**; chọn xong tự đổ thành phần/kcal/allergen; vẫn cho **tạo món mới** → lưu vào thư viện.
4. `DishForm.dishId` set khi chọn từ thư viện.
5. Màn quản lý thư viện món cho Service Caterers (list + tạo/sửa + `isActive`).

**Lợi ích kép:** gỡ luôn 3 lỗi đã ghi nhận — món nối bằng tên (gap A1/T1), thành phần lệch catalog làm mất phát hiện allergen, allergen phụ thuộc kỷ luật nhập liệu.

⚠️ **Rủi ro:** đổi `ingredients` phá màn `parent/nutrition/[dishId]` (đang render `string[]`) → sửa cùng đợt.

### Đã xong ở Đợt B (không làm lại)

- `DishNutrition` → **`Dish`** (`DISH_NUTRITION` → **`DISH_LIBRARY`**, tên cũ không còn). `ingredients: DishIngredient[] { ingredientId?, name, quantity, unit }` — 16 món seed đã nổ định lượng/suất, phần lớn có `ingredientId`; gia vị/rau thơm chưa vào catalog (Hành lá, Hẹ, Súp lơ, Dứa, Me, Xương lợn) để trống id và UI báo "ngoài danh mục".
- **`allergens` đã BỎ khỏi model** — dẫn xuất qua `dishLibraryAllergens(dish)`. Hệ quả có chủ đích: các món dùng nước mắm nay hiện thêm tác nhân **Cá** (catalog khai vậy), trước đây khai tay nên sót.
- Helper dùng chung trong `mock-data`: **`dishIdOfName(name, taken)`** sinh id slug ("Cá basa sốt cà chua" → `ca-basa-sot-ca-chua`, khớp quy ước seed).
- Resource **`dishes`** trong `lib/api/db.ts`.
- `menu-form` export thêm **`IngredientEditor` · `IngredientCatalogDatalist` · `INGREDIENT_DATALIST_ID` · `emptyIngredient`** (đã dùng lại ở màn Suất ăn đặc biệt và Thư viện món).
- `DishForm.dishId?` — ô tên món là **combobox `<datalist id="dish-library">`**; trúng tên món trong thư viện thì đổ BOM + kcal + macro và chốt `dishId` (đã gắn rồi thì không đổ đè chỉnh tay). Món gõ tay có nút **"Lưu vào thư viện"** ngay tại chỗ.
- Màn mới **`/service-caterers/dishes`** (đã vào nav Service Caterers → nhóm "Thực đơn"): tìm/lọc, tạo/sửa, **Ngừng dùng thay cho xoá** (thực đơn cũ còn trỏ `dishId`), cảnh báo số thành phần ngoài danh mục.
- Đã sửa theo: `parent/nutrition/[dishId]` (render định lượng), `parent/school-meals` (dùng `dishLibraryAllergens`).

---

## 4. ĐỢT E — Nối nhu cầu nguyên liệu ↔ Đặt hàng (mới)

**Cơ chế tính ĐÃ CÓ và tốt** — `mealRequirements()` trong `mock-data.ts`:
```
BOM × số suất → quy đổi đơn vị → allocateFEFO() (HSD gần nhất trước, BR-075;
loại lô hết hạn/cách ly BR-205/149) → { allocation[], shortfall, unknownIngredient }
```
**Nhưng chỉ 1 màn dùng** — `inventory/issue`. Cơ chế mạnh, chưa khai thác.

**Đứt mạch:** màn xuất kho phát hiện đúng thiếu hàng rồi chỉ **viết chữ** *"Tạo Purchase Request để bổ sung…"* (`inventory/issue/page.tsx ~dòng 280-293`) — **không có nút**. Sang `orders/new` phải **gõ lại từ đầu** (chọn nguyên liệu, gõ số lượng, chọn NCC) trong khi hệ thống vừa tính ra chính xác. Vừa bất tiện vừa là nguồn sai số.

**Việc:**
1. **Nút "Tạo đơn đặt hàng"** ngay tại Alert thiếu hàng → sang `orders/new` **prefill** các dòng thiếu (nguyên liệu + số lượng + đơn vị).
2. `PurchaseRequest` ghi **nguồn phát sinh** (`menuId`, `serviceDate`) → truy được "đơn này đặt vì thực đơn nào"; **gợi ý NCC** theo `ingredientId` từ lịch sử lô.
3. **Cảnh báo sớm (giá trị cao nhất):** quét thực đơn đã duyệt của **3 ngày tới** (BR-061 vốn bắt lập trước ≥3 ngày) → dashboard Caterers cảnh báo *"thực đơn 24/07 thiếu 12kg thịt gà"*. Hiện chỉ phát hiện **đúng lúc chuẩn bị nấu** — đã muộn để đặt hàng.

**Phụ thuộc:** cần A2 (định lượng suất đặc biệt) xong trước, nếu không đơn hàng sẽ thiếu toàn bộ nguyên liệu cho suất đặc biệt. Cần B để gợi ý NCC theo `ingredientId` và gộp dòng trùng.

### Đã xong ở Đợt E (không làm lại)

- **`lib/mock-data`** — tầng dùng chung, KHÔNG tính lại ở màn nào:
  `ShortageLine` · `mealShortages(menu, mealType, servings)` (gộp theo nguyên liệu, kèm `dishes[]` + gợi ý NCC) · `lastSupplierOfIngredient(ingredientId)` (lô nhận gần nhất) · `upcomingShortages(days=3)` (bỏ qua buổi ĐÃ xuất kho để không kêu oan).
- **Nút "Tạo đơn đặt hàng bổ sung"** tại Alert thiếu hàng ở `inventory/issue` → `orders/new?menu=&meal=&servings=`.
- **`orders/new`** (nay bọc `Suspense` vì đọc searchParams): prefill dòng thiếu, **tính lại tại chỗ** chứ không bê số qua URL (tồn có thể đổi giữa lúc bấm và lúc gửi); tự chọn NCC gợi ý; nguyên liệu ngoài danh mục **không** điền sẵn mà báo rõ (không có option catalog để chọn).
- **`PurchaseRequest`** thêm `sourceMenuId` · `sourceMealType` · `serviceDate`.
- **Dashboard Caterers** — dòng cảnh báo sớm cho từng thực đơn đã duyệt trong 3 ngày tới.
- **Seed**: thêm `MENU-2026-30` trạng thái APPROVED, ngày **tính động** `hôm nay + 2` (`isoInDays`) — seed cố định thì cảnh báo sớm chỉ đúng trong tuần demo rồi im lặng vĩnh viễn.
  > Đã chạy thật (jiti): cảnh báo ra `Tôm sú 7.6 kg` (lô duy nhất đang cách ly), `Su su 25.2 kg`, `Gừng 1.14 kg` — kèm NCC gợi ý Đại Dương / Vân Nội.

---

## 5. ĐỢT C — MenuVariant cho nhóm đổi trọn bộ

**Vấn đề:** mô hình hiện giả định *suất đặc biệt = thay từng món lẻ*. Đúng với chay (thay 2–3 món), **sai với tiểu đường**: đổi cơm trắng → gạo lứt, đổi khẩu phần tinh bột, bỏ món ngọt — gần như **một thực đơn khác**. Và **không kiểm được tổng kcal/đạm** của suất đó (BR-062).

```
MenuVariant     { id, menuId, groupId, mode: "PARTIAL" | "FULL", portions }
MenuVariantDish { variantId, dishId, replacesDishId?: string }
```
- `PARTIAL` — chỉ ghi phần khác biệt. **Tương đương `substitutes[]` của Đợt A** ⇒ A → C là nâng cấp tự nhiên, KHÔNG phải làm lại.
- `FULL` — ghi trọn bộ món (`replacesDishId = null`).

**Việc:** thêm model + db; dialog tạo suất có 2 chế độ (mặc định PARTIAL, FULL là lựa chọn có chủ đích); hiện **kcal/đạm từng variant** cạnh chuẩn dinh dưỡng khối (BR-062); `menu-personalization` trả trọn bộ khi HS thuộc nhóm có variant FULL; `school-admin/menu-approval/[id]` hiện variant kèm dinh dưỡng để BGH duyệt đúng cái mình duyệt.

### Đã xong ở Đợt C (không làm lại)

- `MenuVariant` + `MenuVariantDish` + `variantNutrition()` trong `mock-data`; resource **`menu-variants`** trong db. Backend là 2 bảng, mock **nhúng `dishes[]`** vào variant — quan hệ vẫn 1-n.
- **PARTIAL giữ nguyên `substitutes[]`** — KHÔNG chuyển đổi, không đụng luồng cũ. Chỉ FULL đi vào `menu-variants`.
- Dish của variant tham chiếu **`Dish.id` của thư viện** (Đợt B) ⇒ dinh dưỡng cộng tự động, không nhập tay.
- `special-meals`: chọn "Mức độ thay đổi" (PARTIAL mặc định) → FULL đổi bước 3 thành **chọn buổi ăn** + bảng chọn món từ thư viện, hiện **tổng kcal/đạm đối chiếu `NUTRITION_TARGET` ±10%**, chặn nếu bộ món chạm dị ứng đã xác minh của thành viên nhóm. Bảng nhóm hiện badge **TRỌN BỘ** kèm dinh dưỡng.
- `personalizeBlock(..., fullVariant?)` — có variant FULL thì trả **trọn bộ món của variant**; **vẫn qua cửa dị ứng** (hợp chế độ ăn ≠ an toàn với hồ sơ dị ứng). `parent/weekly-menu` đã truyền variant vào.
- `school-admin/menu-approval/[id]`: card "Suất trọn bộ theo nhóm" — nhóm · buổi · số suất · bộ món · kcal/đạm, đánh dấu vàng khi lệch chuẩn >±10%.
- Seed `MV-001` (nhóm DIABETIC, `MENU-2026-30`, 4 món, 2 suất).
  > Đã chạy thật (jiti): variant = 595 kcal / 33g đạm (chuẩn 715/28 → bị đánh dấu THẤP, đúng ý đồ BR-062); HS tiểu đường nhận trọn 4 món; HS tiểu đường **kiêm dị ứng Cá** vẫn bị cảnh báo ở "Gà rang gừng" (nước mắm).

---

## 6. ĐỢT D — Dọn dẹp

- Xoá nhánh đọc `substitute?` sau khi seed + dữ liệu phiên đã chuyển hết.
- Cập nhật `review_data_model_traceability_for_backend.md` (T3/D1) theo mô hình mới.
- Screen Spec cho màn Thư viện món (màn mới, chưa có spec).

---

## 7. Nghiệm thu chung MỌI đợt

```
npx tsc --noEmit     # sạch
npx next build       # 0 error (hiện 118 trang)
npx next lint        # 0 error, KHÔNG phát sinh warning mới (baseline: 17)
```
Mỗi đợt **commit riêng** để dễ revert. Không đảo lại quyết định đã chốt ở Mục 0.

---

## 8. Ngoài phạm vi file này

- **T3** (`ServingDish` thay `assignedMeal` blob) và **T4** (`IncidentAffectedStudent` thay số đếm) — xem `review_data_model_traceability_for_backend.md`, thuộc đợt schema riêng.
- Nối AI (BR-156) vào thư viện món — sau Đợt B, khi đã có `dishId` để map ngược.
- Ghi lịch sử suất ăn bất biến lúc phát suất — phụ thuộc T3.
