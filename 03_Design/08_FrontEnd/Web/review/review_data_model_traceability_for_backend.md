# Rà soát liên kết dữ liệu & truy xuất — chốt mô hình cho Backend

> **Phạm vi:** codebase Web sau khi hợp nhất 5 nhánh thành viên (`dev` @ `a9610cd`, 21/07/2026).
> **Mục tiêu:** chỉ ra các liên kết **lỏng lẻo / nối bằng chuỗi tên / mất chiều thời gian** trên 4 chuỗi nghiệp vụ xương sống, làm căn cứ chốt schema DB trước khi code backend.
> **Người rà:** Nam (School Admin / System Admin).

---

## 0. Tóm tắt cho người quyết

Sau merge, mô hình dữ liệu **đã tốt lên rõ rệt** ở phía Service Caterers: chuỗi *thực đơn → định mức → xuất kho FEFO → lô* nay **sinh ra tự động tại bước xuất kho** thay vì khai tay (`mock-data.ts:2756-2773`). Đây là thiết kế đúng và nên **giữ nguyên làm nền cho backend**.

Nhưng còn **4 điểm gãy** khiến truy xuất chưa khép kín:

| # | Điểm gãy | Hệ quả | Mức |
|---|---|---|---|
| **T1** | `dishBatchMap()` gộp mọi ngày — mất chiều `serviceDate`/`menuId` | Cách ly lô **thừa**: khoá cả lô của ngày khác | **P0** |
| **T2** | Phụ huynh xem thực đơn **global**, không theo khối con; dị ứng không gắn `studentId` | PH thấy sai thực đơn, sai cảnh báo dị ứng | **P0** |
| **T3** | Suất ăn của HS là **chuỗi blob**; suất thay thế chỉ tính runtime theo NHÓM, không lưu | Không trả lời được "HS nào đã ăn món nhiễm?" | **P0** |
| **T4** | Sự cố → HS bị ảnh hưởng chỉ có **số đếm** / **mảng tên** | Không mở được hồ sơ dị ứng đúng HS khi điều tra | **P1** |

Điểm chung của cả 4: **khóa ngoại bị thay bằng chuỗi hiển thị**, và **quan hệ N-N bị làm phẳng thành text**.

---

## 1. Chuỗi A — Thực đơn → Học sinh (câu hỏi "PH xem menu đã đúng con mình chưa?")

### 1.1. Hiện trạng: phía Caterers ĐÚNG, phía Phụ huynh SAI

Phía Service Caterers, mô hình **đã đủ** để suy ra suất của từng nhóm:

```
Menu(id, grade "Khối 3", menuDateIso, status)          ← đã theo NGÀY + KHỐI (tốt)
 └─ blocksData: MealBlock[]  { mealType, dishes[] }     (menu-form.tsx:125)
     └─ DishForm { name, ingredients[], manualAllergens[], substitute }  (menu-form.tsx:109)
         └─ SubstituteDish { name, ingredients[], portions, groupId }    (menu-form.tsx:98)
                                        groupId = "ALLERGY" | DietGroup.id
Student.class "3A" → gradeOfClass() → "Khối 3"          (mock-data.ts:2745)
DIET_GROUPS[].matchDiet ←→ Student.specialDiet          (mock-data.ts:dietGroupMembers)
```

⇒ Caterers **tính được** "Khối 3, nhóm Vegetarian gồm những HS nào, ăn món thay thế gì".

**Nhưng màn Phụ huynh không dùng một dòng nào của mô hình trên:**

- `parent/weekly-menu/page.tsx:6` — đọc `WEEKLY_MENU_DAYS` (thực đơn tuần **global, hardcode**), không đọc `MENUS`.
- `parent/school-meals/page.tsx:13-14` và `parent/dashboard/page.tsx:11,17` — cùng vấn đề.
- ⇒ **PH của con Khối 1 và Khối 5 thấy y hệt nhau**, dù dữ liệu đã tách theo khối.

### 1.2. Lỗi nặng: dị ứng không gắn học sinh

```ts
// mock-data.ts:2121 — KHÔNG có studentId
export const PARENT_ALLERGIES: AllergyDeclaration[] = [
  { id: "ALG-001", category: "Hải sản…", allergen: "Tôm", severity: "Nặng", status: "VERIFIED" },
  { id: "ALG-002", …allergen: "Cua"… },
];
```

Trong khi phụ huynh mẫu có **2 con** (`PARENT_CHILDREN`: HS-3A-001 Khang, HS-1B-007 Minh Anh — `mock-data.ts:2054`). Không có cách nào biết dị ứng Tôm là của đứa nào. `parent/weekly-menu` cũng **không gọi `useActiveChild()`** (13 màn Parent khác có gọi) → đổi con không đổi cảnh báo.

### 1.3. Hai mô hình dị ứng song song, không nối nhau

| Nguồn | Kiểu | Có `studentId`? |
|---|---|---|
| `Student.allergies` | `string[]` (`["Tôm","Cua"]`) | ✔ (nằm trong Student) |
| `PARENT_ALLERGIES` | `AllergyDeclaration[]` (có severity, proofFile, status VERIFIED) | ✘ |

Caterers dùng `Student.allergies` (`gradeAllergyStudents`), Parent dùng `PARENT_ALLERGIES`. **Hai nguồn sự thật cho cùng một khái niệm** → PH khai dị ứng mới, bếp không thấy.

### 1.4. Suất thay thế gắn NHÓM, không gắn HỌC SINH

`SubstituteDish.groupId` = `"ALLERGY"` hoặc `DietGroup.id`. Danh sách HS của nhóm được tính **runtime** (`dietGroupMembersInGrade`), **không lưu**. Hệ quả:

- Không có bản ghi "ngày 03/07 HS Khang được suất Thịt rim thay Tôm rim thịt". Khi HS đổi chế độ ăn tháng sau, **lịch sử bị viết lại** — sai về mặt hồ sơ pháp lý khi điều tra.
- Nhóm `"ALLERGY"` gộp mọi allergen: HS dị ứng Tôm và HS dị ứng Sữa **cùng một nhóm**, nhưng cần 2 món thay thế khác nhau.

---

## 2. Chuỗi B — Món → Lô nguyên liệu (truy xuất khi có sự cố)

### 2.1. Nền tảng ĐÚNG — giữ nguyên cho backend

```
Thực đơn (BOM: món → nguyên liệu/suất) × số suất → nhu cầu
                                          ↓ allocateFEFO()  (BR-075)
Phiếu xuất kho InventoryTransaction { batchId, ingredientId, menuId, mealType, dishName, serviceDate }
        ├──► Sổ chế biến (batchesUsed)
        └──► Bản đồ truy xuất (dishBatchMap / batchDishMap)
```

Lý do chọn điểm bind là lúc xuất kho được ghi rõ tại `mock-data.ts:2771-2773` và **hoàn toàn hợp lý**: sớm hơn thì lô chưa nhập kho, muộn hơn thì liên kết là hồi tố theo trí nhớ đầu bếp. **Khuyến nghị giữ đúng nguyên tắc này ở backend.**

### 2.2. T1 (P0) — Bản đồ truy xuất mất chiều thời gian

```ts
// mock-data.ts:2988 — không có tham số ngày/menu
export function dishBatchMap(transactions, preparations): Record<string, string[]> {
  for (const t of transactions)
    if (t.type === "Xuất kho (FEFO)" && t.dishName) add(t.dishName, t.batchId);
  …
}
export const DISH_BATCHES = dishBatchMap();   // :3021
```

`InventoryTransaction` **có** `serviceDate` và `menuId`, nhưng map **vứt bỏ cả hai**, gộp mọi lần nấu của cùng tên món vào 1 danh sách lô.

Nơi dùng để **khoá lô** khi điều tra (`medical-staff/incidents/[id]/page.tsx:414, 430, 480`):
```ts
const batches = [...new Set(nextDishes.flatMap((d) => DISH_BATCHES[d] ?? []))];
```
⇒ Điều tra sự cố ngày 30/06 sẽ **khoá luôn lô của ngày 06/07**.

> **Hiện chưa lộ** vì seed mỗi món chỉ phục vụ đúng 1 ngày (đã kiểm: `Gà rang gừng` 30/06, `Thịt kho trứng cút` 06/07, `Cá basa` 07/07…). Nhưng thực tế **"Cơm trắng" ngày nào cũng có** → lỗi chắc chắn bung khi có dữ liệu thật. Đây là lỗi **thiết kế**, không phải lỗi dữ liệu.

**Đề xuất:** khoá theo `(dishId, serviceDate, mealType)` hoặc trực tiếp theo `menuDishId`; chữ ký hàm nên là `dishBatchMap(scope: { serviceDate, menuId?, mealType? })`.

> 📌 Liên quan: `review_medicalstaff_plan.md` mục **D** đã chốt "khoá cách ly ở **CẤP LÔ**, không theo món" — Medical Staff tự tick từng lô. Việc này **giảm nhẹ** T1 (người dùng thấy và chọn lô), nhưng **không xoá gốc**: bảng lô gợi ý vẫn dựng từ `DISH_BATCHES` nên vẫn liệt kê lô sai ngày. Cần sửa cả hàm map, không chỉ đổi UI.

### 2.3. Nguyên liệu trong BOM không có khóa ngoại

`IngredientRow { name: string; quantity: string; unit: string }` (`menu-form.tsx:88`) — nguyên liệu của món khai bằng **tên**, tra ngược qua `ingredientIdOfName()`. Chính code đã cảnh báo: *"nguyên liệu không định danh được = không truy xuất được (BR-076)"* (`mock-data.ts:2780-2786`).

Ngoài ra `quantity` là **string** → phải `parseQuantity()` với heuristic phân biệt dấu nghìn VN (`"1.200"`) và thập phân (`"7.6"`) (`mock-data.ts`). Ở backend nên là `numeric` + `unit` riêng.

### 2.4. Mẫu lưu không nối được lô

`FoodSample` (`mock-data.ts:679`) có `dish: string` — **không** `dishId`, **không** `batchIds`. Muốn từ mẫu xét nghiệm dương tính truy về lô phải đi vòng qua tên món → `DISH_BATCHES` (và dính luôn lỗi T1).

---

## 3. Chuỗi C — Học sinh nào đã ăn món nào (T3, P0)

```ts
// mock-data.ts:2329
export interface StudentDistribution {
  studentId: string;           // ✔ có khoá ngoại
  assignedMeal: string;        // ✘ "Cơm trắng · Thịt rim · Canh khoai tây · Rau muống xào tỏi"
  specialMeal?: string;        // ✘ "Thịt rim (thay Tôm rim thịt — dị ứng Tôm)"
  allergens: string[];         // ✘ chuỗi tên
  allergyWarning?: string;     // ✘ câu văn
}
```

Suất ăn của từng HS là **một chuỗi ghép bằng dấu `·`**. Hệ quả trực tiếp cho điều tra sự cố:

- Không truy được **ngược**: "lô BATCH-MP-8820 nhiễm khuẩn → những HS nào đã ăn món dùng lô đó?" — phải parse chuỗi.
- Không **thống kê được số suất theo món** (phục vụ đặt hàng nguyên liệu).
- Quan hệ "HS ↔ món" là **N-N**, đang bị nén thành 1 text.

---

## 4. Chuỗi D — Sự cố → Học sinh bị ảnh hưởng (T4, P1)

| Field | Kiểu hiện tại | Vấn đề |
|---|---|---|
| `Incident.affectedStudents` | `number` (số đếm) | Không biết là ai |
| `IncidentSourceReport.affectedStudents` | `string[]` **tên HS** (`mock-data.ts:794`) | Trùng tên → sai người |

Trong khi `Student.id` tồn tại đầy đủ. Hệ quả: từ sự cố **không click sang được** hồ sơ dị ứng/bệnh nền của đúng HS — thông tin sống còn khi điều tra ngộ độc/dị ứng. Đây cũng là lý do màn "Đóng sự cố" phải sinh checklist hồi phục bằng **học sinh ảo** `#1..#N` (`school-admin/incidents/[id]/close/page.tsx`).

Ngoài ra `SpecialMealRequest` (`mock-data.ts:1909`) dùng `student: string`, `class: string`, `dietaryType: string` — **không** `studentId`, **không** `menuId`/`dishId`. Duyệt xong không tự áp suất đặc biệt cho đúng HS.

---

## 4b. CẬP NHẬT 21/07/2026 — phần mô hình đã hiện thực ở FE

> Các đợt A/A2/B/E/C (xem `plan_menu_dish_library_and_special_meal_variants.md`) đã kéo mô hình FE **về gần đúng đề xuất Mục 5** ở những chỗ dưới đây. Phần còn lại của tài liệu giữ nguyên làm căn cứ cho backend.

| Đề xuất ở Mục 5 | Trạng thái FE |
|---|---|
| `Dish` master + `DishIngredient(qty_per_serving, ingredient_id)` (5.1) | ✅ **Có** — `Dish` / `DishIngredient` / `DISH_LIBRARY` trong `mock-data`; `allergens` **bỏ hẳn**, dẫn xuất bằng `dishLibraryAllergens()`. `DishForm.dishId` nối thực đơn về master. |
| `MenuDishSubstitute(..., portions)` (5.1) | ✅ **Có, dạng nhúng** — `DishForm.substitutes[]`, một món phục vụ **nhiều nhóm** cùng lúc; truy cập độc quyền qua `lib/menu-substitutes`. Ô đơn `substitute` đã **xoá hẳn** (Đợt D). |
| `target_type` tách allergen cụ thể khỏi nhóm chế độ ăn (gap 1.4) | ⚠️ **Chưa** — vẫn là `groupId = "ALLERGY" \| DietGroup.id`, tức nhóm ALLERGY còn gộp mọi allergen. Giữ nguyên khuyến nghị cho backend. |
| — (chưa có trong Mục 5) | 🆕 **`MenuVariant` / `MenuVariantDish`** (mode `PARTIAL`/`FULL`) — nhóm ăn **trọn bộ** món khác hẳn. Mục 5.1 nên bổ sung bảng này; `MenuDishSubstitute` chỉ tả được PARTIAL. |
| `PurchaseRequest` không có nguồn phát sinh | ✅ **Đã thêm** `sourceMenuId` · `sourceMealType` · `serviceDate` — trả lời được "đơn này đặt vì thực đơn nào". |
| **T2** — Phụ huynh xem thực đơn global | ✅ **Đã sửa** — `parent/weekly-menu` đọc `MENUS` theo khối của con và chiếu qua `personalizeBlock` (kể cả variant FULL). |
| **T3** — `ServingDish` thay blob `assignedMeal` | ⛔ **Chưa** — vẫn là dẫn xuất lúc xem, không phải bản ghi lịch sử. Nguyên trạng, thuộc đợt schema riêng. |
| **T4** — `IncidentAffectedStudent` | ⛔ **Chưa** — nguyên trạng. |

**Điểm cần backend lưu ý thêm:** BOM của **suất thay thế** nay bắt buộc có định lượng + `ingredient_id` (trước đây rỗng nên nhu cầu xuất kho của suất đặc biệt = 0). Ràng buộc này nên đưa thành `NOT NULL` ở `DishIngredient.qty_per_serving`.

---

## 5. Mô hình đề xuất cho Backend

Nguyên tắc: **ID là khóa, tên chỉ để hiển thị**; mọi quan hệ N-N có bảng nối; mọi bản ghi lịch sử phải **bất biến theo thời điểm** (không tính lại từ trạng thái hiện tại).

### 5.1. Danh mục & thực đơn

```
Dish(id, name, category, …)                       -- master món, thay tên chuỗi
Ingredient(id, name, unit_base)                    -- đã có INGREDIENT_CATALOG
DishIngredient(dish_id, ingredient_id, qty_per_serving NUMERIC, unit)   -- BOM, thay IngredientRow.name
Menu(id, school_id, grade, menu_date, status, approved_by, approval_signature, reject_reason)
MenuMeal(id, menu_id, meal_type)                   -- BREAKFAST/LUNCH/SNACK
MenuDish(id, menu_meal_id, dish_id, planned_servings)
MenuDishSubstitute(id, menu_dish_id, substitute_dish_id,
                   target_type ENUM('ALLERGEN','DIET_GROUP'),
                   allergen_id NULL, diet_group_id NULL, portions)
```
> `target_type` tách **allergen cụ thể** khỏi nhóm chế độ ăn — sửa gap 1.4 (nhóm "ALLERGY" hiện gộp mọi allergen).

### 5.2. Hồ sơ học sinh (gộp 2 mô hình dị ứng)

```
Student(id, class_id, …)
AllergyProfile(id, student_id, allergen_id, severity, status, proof_file, verified_by, verified_at)
StudentDiet(student_id, diet_group_id, effective_from, effective_to)
```
> **Bỏ** `PARENT_ALLERGIES` global và `Student.allergies: string[]` — cả hai thay bằng `AllergyProfile` có `student_id`. Đây là điều kiện tiên quyết để màn Phụ huynh đúng.

### 5.3. Suất ăn thực tế (thay blob)

```
ServingRecord(id, student_id, menu_id, serving_date, meal_type, status, served_at)
ServingDish(serving_record_id, dish_id, is_substitute,
            substituted_for_dish_id NULL, reason)     -- N-N thay assignedMeal
```
> Ghi **tại thời điểm phát suất**, không tính lại → lịch sử bất biến (1.4), truy ngược HS↔món được (T3), thống kê suất/món được.

### 5.4. Truy xuất lô — có chiều thời gian

```
InventoryIssue(id, batch_id, ingredient_id, menu_id, meal_type,
               menu_dish_id,          -- ★ thay dishName chuỗi
               service_date, qty NUMERIC, issued_by, issued_at)
```
Bản đồ truy xuất truy vấn theo **`menu_dish_id`** (đã tự mang ngày + bữa + khối), không theo tên món ⇒ xoá gốc T1.

```
FoodSample(id, menu_dish_id, batch_ids[], collected_by, retention_deadline, …)
```

### 5.5. Sự cố

```
Incident(id, …)                                    -- bỏ affectedStudents: number
IncidentAffectedStudent(incident_id, student_id, recovery_status, note)
IncidentSuspectedDish(incident_id, menu_dish_id)   -- thay suspectedDishIds tên món
IncidentQuarantineBatch(incident_id, batch_id, locked_at, released_at, release_reason)
IncidentSample(incident_id, sample_id)
```
> `IncidentAffectedStudent.recovery_status` cấp thẳng checklist BR-155, xoá "học sinh ảo".

### 5.6. Kiểu dữ liệu cần chuẩn hóa ngay

| Hiện tại | Backend |
|---|---|
| Ngày `"30/06/2026"` (string) | `DATE` / `TIMESTAMPTZ` |
| Lượng `"45 kg"`, `"1.200 quả"` | `qty NUMERIC` + `unit` |
| Tiền `"9.800.000 ₫"` | `amount NUMERIC(14,2)` + `currency` |
| `status` union phẳng dùng chung mọi entity | enum riêng từng bảng |

---

## 6. Nhận xét tài liệu `review_medicalstaff_plan.md`

**Điểm mạnh:** kế hoạch viết tốt — có phân tích phụ thuộc kèm lý do thứ tự (Mục 2), quy ước nghiệm thu rõ (`tsc` sạch, ghi status note ngược nguồn), và **Mục 3 tự nêu 3 câu hỏi chưa chốt thay vì tự quyết** — đúng nguyên tắc dự án.

**Cần lưu ý khi thực thi:**

1. **Rủi ro đứt mạch CAPA là thật, và tài liệu đã tự nhận** (Mục 1.2 + Mục 5.1). Câu hỏi #2 Mục 3 nên chốt là **sửa cả 3 file cùng đợt**: enum `CorrectiveAction.status` nằm trong `mock-data.ts` **dùng chung**, nên đổi phía Medical Staff sẽ làm `school-admin/corrective-actions` (duyệt CAPA) và gate "Đóng sự cố" (`capaDone` so `=== "COMPLETED"`) sai ngay lập tức. Tách đợt sẽ vỡ chéo.
2. **Câu hỏi #3 (ánh xạ Incident ↔ CAPA)**: đề xuất trong tài liệu hợp lý; riêng gate đóng sự cố nên chốt rõ **`COMPLETED`** là điều kiện duy nhất, vì cả `incident-detail-view.tsx` lẫn `incidents/[id]/close` đang dựa vào đúng giá trị này.
3. **Mục D (khoá cấp lô) chưa xử lý gốc T1** — như đã nêu ở §2.2: đổi UI sang tick từng lô là đúng hướng, nhưng danh sách lô gợi ý vẫn dựng từ `DISH_BATCHES` không lọc ngày. Đề nghị bổ sung vào Đợt 3: `dishBatchMap` nhận `serviceDate`.
4. **Mục 2 mô tả `DISH_BATCHES` là "cần mở rộng mock (kg còn lại, ngày nhập…)"** — thực ra dữ liệu đó **đã có sẵn**: `batchRemaining()`, `InventoryBatch.receivedDate`, `INGREDIENT_CATALOG`. Nên **join từ nguồn** thay vì mở rộng map, tránh đẻ thêm bản sao dữ liệu dẫn xuất.

---

## 7. Thứ tự đề xuất để chốt DB

1. **Chốt định danh trước** (chặn mọi thứ khác): `Dish.id`, `Ingredient.id`, `AllergyProfile.student_id`. Không có bước này thì mọi bảng nối phía sau đều vô nghĩa.
2. **Chốt `MenuDish` làm đơn vị truy xuất trung tâm** — nó là điểm giao của cả 4 chuỗi (thực đơn, xuất kho, suất ăn HS, mẫu lưu, sự cố).
3. Dựng `ServingDish` + `IncidentAffectedStudent` → mở khoá điều tra 2 chiều.
4. Chuẩn hóa kiểu ngày/lượng/tiền.
5. Sau cùng mới nối màn Phụ huynh vào `Menu` theo khối + `AllergyProfile` theo con.

---

## 8. Giới hạn của rà soát này

- Dựa trên mock tĩnh + code Web sau merge `a9610cd`; **chưa** đối chiếu `class_diagram_description.md` và Screen Spec — có thể có quyết định mới hơn ở đó.
- Các số hiệu BR/UC trích theo comment trong code, chưa verify ngược `business_rules.md`.
- Chưa rà actor Supplier (chuỗi NCC → phiếu giao → nhận hàng) ở mức chi tiết như 4 chuỗi trên.
