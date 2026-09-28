# Rà soát Data Linkage & Cấu hình động — phục vụ điều tra sự cố

> **Phạm vi:** toàn bộ FE web `03_Design/08_FrontEnd/Web/src` (mock database `src/lib/mock-data.ts` + các màn).
> **Ngày:** 09/07/2026 · **Người rà:** Nam (School Admin / System Admin).
> **Mục tiêu:** tìm (A) các chỗ dữ liệu **chưa liên kết** khiến truy xuất khi điều tra sự cố phải làm thủ công, (B) chỗ **nhập tay** đáng lẽ chọn từ danh sách có sẵn, (C) chỗ **fix cứng** nên cấu hình động. Mục (D) là **chuyên đề Menu ↔ Món ↔ Thống kê suất** — xương sống cho màn Phụ huynh (hiển thị theo khối), món thay thế dị ứng và thống kê số suất/từng món. Mục (E) là **các vấn đề khác về mô hình & tính nhất quán** (mô hình trùng, ngày/tiền/lượng là chuỗi, field dẫn xuất lưu cứng, ID dễ trùng…).
>
> Mọi phát hiện đều trỏ tới `file:line` thật. Đây là báo cáo phân tích — **chưa sửa code**.

---

## 0. Bối cảnh: chuỗi truy xuất khi điều tra một sự cố

Khi có sự cố ngộ độc, người điều tra cần đi được mạch:

```
Sự cố (INC) → HS bị ảnh hưởng + lớp
            → món nghi vấn → thực đơn (Menu) ngày đó
                           → lô nguyên liệu (Batch) của món
                                          → nhà cung cấp (Supplier) + phiên giao (Delivery)
                                          → phiên nhận hàng (Receiving) + kiểm thực 3 bước (Inspection)
                           → mẫu thức ăn lưu (FoodSample) của món → phòng lab
```

Hiện tại **gần như mọi mắt xích trong chuỗi này nối với nhau bằng chuỗi TÊN tự do, không bằng ID**, và màn "Báo cáo Truy xuất nguồn gốc" chỉ là một bảng phẳng — nên người điều tra phải tự dò chéo giữa các màn. Đây chính là điểm nghẽn "truy xuất thủ công mất thời gian".

---

## A. Traceability gaps — chưa liên kết, phải truy xuất thủ công

### A1. Chuỗi món → lô → NCC nối bằng TÊN món, không có `dishId`
- `Incident.suspectedDishIds` thực chất là **mảng tên món** (`["Gà rang gừng"]`), không phải ID — `mock-data.ts:455`.
- `DISH_BATCHES` (bản đồ món → lô, BR-151) đánh khóa bằng **tên món** — `mock-data.ts:464`.
- `FoodSample.dish` cũng là tên món — `mock-data.ts:333`.
- **Hệ quả:** đổi tên món, hoặc 2 món trùng tên khác công thức → đứt mạch truy xuất. Không join tự động được sang bảng lô/NCC.
- **Đề xuất:** thêm thực thể `Dish { id, name, ... }`; `suspectedDishIds`, `DISH_BATCHES`, `FoodSample.dishId` tham chiếu `Dish.id`. Tên chỉ để hiển thị.

### A2. Học sinh bị ảnh hưởng KHÔNG link tới hồ sơ học sinh
- `Incident.affectedStudents` chỉ là **con số** — `mock-data.ts:422`.
- `IncidentSourceReport.affectedStudents` là **mảng tên HS** (`["Lý Gia Bảo", ...]`) — `mock-data.ts:390`.
- Trong khi `Student` có `id` đầy đủ — `mock-data.ts:481-484`.
- **Hệ quả:** từ sự cố không click được sang hồ sơ dị ứng / bệnh nền / lịch sử sức khỏe của đúng HS bị ảnh hưởng — trong khi đây là thông tin sống còn khi điều tra dị ứng/ngộ độc.
- **Đề xuất:** `affectedStudentIds: string[]` tham chiếu `Student.id`; hiển thị tên qua lookup.

### A3. Lớp và người báo cáo là chuỗi tự do
- `IncidentSourceReport.className` = `"3B"` (string) — `mock-data.ts:389`; trong khi `Classroom.id` tồn tại (vd `"3A"`) — `mock-data.ts:1096+`.
- `IncidentSourceReport.reportedBy` / `Incident.reportedBy` = tên + vai trò dạng text — `mock-data.ts:387, 421` (thậm chí nhét cả mã RPT vào text: `"Ngô Thị Hà (Y tế) — gộp từ RPT-2026-030"`, `mock-data.ts:474`).
- **Đề xuất:** `classroomId`, `reportedById` (→ `UserAccount`/`StaffMember`/`ParentChild`). Quan hệ gộp RPT→INC đã có `sourceReportIds` (tốt) — bỏ phần nhét mã vào `reportedBy`.

### A4. Sự cố → mẫu thức ăn chỉ có link NGƯỢC
- `FoodSample.incidentId` link ngược mẫu → sự cố (tốt) — `mock-data.ts:363`.
- Nhưng chiều xuôi (sự cố → các mẫu đã thu) chỉ là **free-text** trong `investigationEvidence[].description` = `"Mẫu SMP-20260630-03 gửi CDC Hà Nội"` — `mock-data.ts:474`.
- **Đề xuất:** `Incident.retrievedSampleIds: string[]` để card Investigation query trực tiếp thay vì parse chữ.

### A5. Mẫu thức ăn không có `batchId`
- `FoodSample` (`mock-data.ts:331-366`) không lưu lô nguyên liệu gốc. Muốn truy mẫu → lô phải đi vòng qua tên món → `DISH_BATCHES`.
- **Đề xuất:** thêm `FoodSample.batchIds?: string[]` gán lúc lưu mẫu để truy xuất trực tiếp mẫu ↔ lô.

### A6. Người/đơn vị tham chiếu bằng tên dù đã có bảng ID
| Field | Vị trí | Bảng ID có sẵn |
|---|---|---|
| `Delivery.driver` (tên) | `mock-data.ts:630` | `SupplierDeliveryPerson.id` (682) |
| `PurchaseRequest.supplier` / `requestedBy` (tên) | `mock-data.ts:570, 572` | `Partner.id` (528) |
| `InspectionSession.inspector` / `coSigner` (tên) | `mock-data.ts:277, 278` | `StaffMember.id` (1248) |
| `FoodSample.collectedBy` / `disposedBy` (tên) | `mock-data.ts:348, 355` | `StaffMember.id` (1248) |
| `CorrectiveAction.assignee` (tên) | `mock-data.ts:872` | `Partner.id` (528) |

- **Hệ quả:** lọc/nhóm/thống kê theo người hay đơn vị đều dựa trên so khớp chuỗi — sai khi trùng tên, đổi tên, sai chính tả.
- **Đề xuất:** chuyển sang `*Id`, giữ tên hiển thị qua lookup. (Lưu ý: `ViolationRecord.staffName` cố ý **không** phải FK theo BR-184 — giữ nguyên.)

### A7. Màn "Báo cáo Truy xuất nguồn gốc" chưa phải chuỗi truy xuất
- `school-admin/reports/[slug]/page.tsx:200-201` — slug `traceability` chỉ render **một `DataTable` phẳng của `inventory`** (lô ← NCC ← ngày nhận), không nhận `incidentId`, không dựng mạch món→lô→NCC→giao→mẫu.
- **Hệ quả:** đúng như mô tả — điều tra vẫn phải mở nhiều màn và tự nối. Đây là ứng viên số 1 để làm thành **màn "Bản đồ truy xuất theo sự cố"** một khi các khóa ID ở A1–A6 sẵn sàng.

---

## B. Nhập tay đáng lẽ chọn từ danh sách có sẵn

### B1. "Món nghi vấn" của Giáo viên hardcoded, không lấy từ thực đơn thật
- `teacher/incidents/page.tsx:41` — `SUSPECTED_MEALS` là **3 chuỗi cứng** (`"Trưa 03/07 — Tôm rim thịt"`…), không khớp `MENUS`/`MEAL_HISTORY`.
- **Đối chiếu pattern đúng:** `parent/safety-concern/page.tsx` sinh danh sách món **động** từ `MEAL_HISTORY.flatMap(...)`. Giáo viên nên dùng đúng cách này.
- **Hệ quả:** món GV chọn không map được sang món thật/`dishId` → gãy ngay từ đầu chuỗi điều tra (liên quan A1).

### B2. Tạo sự cố KHÔNG lưu món đã chọn
- `teacher/incidents/page.tsx` (hàm `submit`, ~`151-160`): `create.mutate` chỉ gửi `title` (text), `symptoms`, `severity`, `status` — **không** lưu `suspectedDish`/`mealDate`/`suspectedDishIds`. Món chỉ nằm trong tiêu đề dạng chữ.
- **Đề xuất:** lưu `mealDate` + `suspectedDishIds` có cấu trúc ngay khi tạo để Medical Staff không phải nhập lại và để khóa lô đúng (BR-149).

### B3. Danh sách phòng lab hardcoded inline
- `medical-staff/samples/page.tsx:341-344` — 3 `<option>` phòng lab gõ cứng trong JSX; `FoodSample.labDestination` lưu bằng tên.
- **Đề xuất:** đưa danh sách lab vào mock-data/config (`LAB_DESTINATIONS`), `labId` tham chiếu.

### B4. Các danh mục phân loại bị lặp giữa các actor
- `VIOLATION_TYPES`, `DISCIPLINARY_LEVELS` khai 2 lần: `medical-staff/violations/page.tsx:86,94` **và** `service-caterers/violations/page.tsx:33,41`.
- `STANDARD_DIETS` (`medical-staff/special-meals/page.tsx:33`) — danh mục chế độ ăn nên dùng chung với `DietGroup` (`mock-data.ts:2020`).
- **Đề xuất:** gom về một nguồn danh mục chung, mọi màn cùng import.

---

## C. Fix cứng nên cấu hình động

### C1. Ngưỡng nghiệp vụ (BR) rải rác, gõ cứng ở nhiều nơi
| Ngưỡng | Giá trị | Vị trí (một số) |
|---|---|---|
| Nhiệt độ lõi tối thiểu (BR-090) | `75°C` | `service-caterers/inspections/[id]/page.tsx:389`; `components/app/send-compliance-report-dialog.tsx:120` |
| Bán kính geofence (BR-073/074) | `200m` | `service-caterers/receiving/new/page.tsx:35`; `receiving-history/page.tsx:76` |
| Thời hạn lưu mẫu (BR-111) | `72 giờ` | `service-caterers/samples/[id]/page.tsx:187`; nhiều mô tả |
| Cửa sổ phản hồi (BR-139) | `24 giờ` | `parent/feedback/*`, `parent/meal-history/page.tsx:24` |
| Dung lượng file tối đa (BR-013) | `10 MB` | `teacher/incidents/page.tsx:54` |
| Khối lượng mẫu tối thiểu (BR-203) | `100g/150ml` | `service-caterers/samples/page.tsx:125` |
- **Hệ quả:** đổi 1 ngưỡng theo quy định → phải sửa nhiều file, dễ sót → dữ liệu/luật không nhất quán.
- **Đề xuất:** một `BUSINESS_RULES` tập trung (vd `lib/business-rules.ts`) chứa các ngưỡng, mọi màn import từ đó.

### C2. Đơn vị số lượng lặp & KHÔNG nhất quán
- `service-caterers/inspections/[id]/page.tsx:101` — `QTY_UNITS = ["kg","lít","gam",...]`.
- `supplier/batches/page.tsx:32` — `QUANTITY_UNITS = ["kg","g","lít",...]`.
- Lệch nhau: **"gam" vs "g"** → cùng đơn vị hai cách viết.
- **Đề xuất:** `UNITS` dùng chung một nguồn.

### C3. "Bữa ăn / ca" hardcoded & khác nhau giữa màn
- `service-caterers/inspections/[id]/page.tsx:152` — `SHIFT_MEALS = ["Bữa sáng","Bữa trưa","Bữa phụ","Bữa chiều"]`.
- `service-caterers/samples/page.tsx:37` — `MEAL_OPTIONS = ["Bữa sáng","Bữa trưa","Bữa tối"]`.
- `parent/feedback/quality/page.tsx:19` — `MEAL_SESSIONS` lại khác.
- **Hệ quả:** cùng khái niệm "bữa" nhưng 3 tập giá trị khác nhau → dữ liệu khó đối chiếu (liên quan việc lọc mẫu/kiểm thực theo bữa khi điều tra).
- **Đề xuất:** `MEAL_SHIFTS` một nguồn chung.

### C4. Chuẩn dinh dưỡng theo khối gõ cứng
- `school-admin/menu-approval/[id]/page.tsx:50` — `NUTRITION_BY_AGE` (kcal/protein/fat theo nhóm tuổi) gõ cứng trong màn duyệt thực đơn.
- **Đề xuất:** đưa vào config/mock-data để BGH/quy định thay đổi được mà không sửa code.

### C5. Danh sách filter trạng thái khai thủ công, có thể derive
- Nhiều màn tự liệt kê lại status: `academic-years/page.tsx:111` (`AY_STATUS`), `violations/page.tsx:156` (`RESOLUTIONS`), `partnerships/page.tsx:146`, `staff/page.tsx:199`…
- **Đề xuất:** derive từ `STATUS_BADGE` như đã làm cho `school-admin/incidents` (`INCIDENT_STATUS_KEYS`) để nhãn/giá trị luôn đồng bộ khi thêm status mới.

---

## D. Chuyên đề: Menu ↔ Món ↔ Món thay thế ↔ Thống kê suất

> Đây là mảng chạm cùng lúc cả 3 nhóm A/B/C và là xương sống cho màn Phụ huynh + thống kê đặt hàng/truy xuất. Nền tảng dữ liệu **đã có** nhưng **tầng liên kết menu chưa dựng** → hiện chưa handle đầy đủ.

### D1. Menu KHÔNG chứa danh sách món
- `MenuItem.dishes` chỉ là **con số** (số món), menu không có danh sách món — `mock-data.ts:113`.
- Món thật của tuần nằm ở `WEEKLY_MENU_DAYS`: **một tuần global hardcoded**, tên món là chuỗi, **không có `menuId`, không theo khối** — `mock-data.ts:131-137`.
- **Hệ quả:** dù `MenuItem` đã có `grade` và data đã tách theo khối (MENU-2026-28/28B/28C), "khối nào ăn món gì" **không tồn tại về mặt dữ liệu**. Menu-per-grade và món-theo-ngày là hai thế giới rời nhau.

### D2. Dish master có `id` nhưng chỉ được tra theo TÊN
- `DishNutrition` là dish master tốt: `id` kebab-case (`"tom-rim-thit"`), category, supplier, kcal, `allergens`, `ingredients` — `mock-data.ts:1530-1561`.
- Nhưng mọi nơi tra bằng `findDishByName(name)` — `mock-data.ts:1563`; `WEEKLY_MENU_DAYS`, `DISH_BATCHES`, `StudentDistribution.assignedMeal`, substitute records… đều dùng **tên**, không `dishId`.
- **Đề xuất:** dùng `DishNutrition.id` làm khóa món xuyên hệ thống (đây là hiện thực cụ thể của gap **A1**).

### D3. Món thay thế khi dị ứng — rời rạc theo tên, không suy tự động
- Có `SpecialMealPlan`/`SpecialMealRecord` với `originalDish`/`substituteDish` là **tên chuỗi** — `mock-data.ts:1621, 1635`; `StudentDistribution.specialMeal` mô tả thay thế bằng **free-text** (`"Thịt rim (thay Tôm rim thịt — dị ứng Tôm)"`) — `mock-data.ts:1707`.
- **Không** có liên kết cấu trúc "món X trong menu → món thay thế Y cho HS có allergen Z"; việc thay thế nhập/mô tả thủ công, **không suy ra tự động** từ `DishNutrition.allergens` ∩ khai báo dị ứng của HS.

### D4. Yêu cầu bữa ăn đặc biệt không link tới HS/menu/món
- `SpecialMealRequest` dùng `student`/`class`/`requestedBy` là **tên chuỗi**, `dietaryType` free-text — **không** `studentId`, **không** `menuId`/`dishId` — `mock-data.ts:1316-1331`.
- **Hệ quả:** phê duyệt xong không tự áp suất đặc biệt vào menu hiển thị cho đúng HS đó.

### D5. Màn Phụ huynh hiển thị menu KHÔNG theo khối con
- `parent/dashboard`, `parent/school-meals`, `parent/weekly-menu` đều đọc `WEEKLY_MENU_DAYS` (global) — `parent/school-meals/page.tsx:14,63`; `parent/weekly-menu/page.tsx:6,25`; `parent/dashboard/page.tsx:11,17`.
- **Hệ quả:** phụ huynh con Khối 1 và Khối 5 thấy **cùng một thực đơn**; cảnh báo dị ứng cũng chỉ so tên món (`findDishByName` + `PARENT_ALLERGIES`), không qua menu-theo-khối. Menu-per-grade (28/28B/28C) hoàn toàn vô hình với phụ huynh.

### D6. Thống kê số suất chỉ ở cấp LỚP, không theo MÓN
- `DistributionRecord` đếm theo **lớp**: `planned`/`distributed`/`specialMeals` — `mock-data.ts:848-856`.
- `StudentDistribution` theo từng HS (có `studentId` — tốt) nhưng `assignedMeal` là **chuỗi blob** (`"Cơm trắng · Thịt rim · Canh…"`) — `mock-data.ts:1707`.
- **Hệ quả:** **không đếm được** "bao nhiêu suất Tôm rim thịt" nếu không parse chuỗi → thống kê theo món/nguyên liệu (đặt hàng, truy xuất) **không làm được** với cấu trúc hiện tại.

### D7. Mô hình đề xuất (mở khóa cả 3 nhu cầu: hiển thị theo khối, thay thế tự động, thống kê theo món)

```
Menu(id, grade, week, status)                       // MenuItem + đã có grade
 └─ MenuDay(menuId, date, shift)                     // thay WEEKLY_MENU_DAYS global
     └─ MenuDish(menuDayId, dishId, plannedServings) // dishId → DishNutrition.id
         └─ substituteFor?: { dishId, forAllergen }  // món thay thế gắn theo dishId + allergen
StudentDistribution.dishServings: { dishId, isSubstitute }[]   // thay blob assignedMeal
SpecialMealRequest: + studentId, (khi duyệt) sinh MenuDish thay thế cho HS
```

Khi có tầng này:
- **Phụ huynh:** query `Menu` theo `grade` của con → hiển thị đúng thực đơn khối; tự suy món thay thế theo `child.allergies` ∩ `dish.allergens` (D3/D5).
- **Thống kê:** group `StudentDistribution.dishServings` theo `dishId` → ra **số suất / từng món**, phục vụ đặt hàng nguyên liệu và nối thẳng vào chuỗi truy xuất **A1/A5** (D6).

---

## E. Các vấn đề khác — mô hình & tính nhất quán dữ liệu

### E1. Mô hình TRÙNG khái niệm (nhiều nguồn cho cùng một thứ)
- **Vi phạm:** `ViolationRecord` (`mock-data.ts:1138`) và `CatererViolation` (`mock-data.ts:1848`) — hai mô hình song song; `service-caterers/violations/page.tsx` dùng **cả hai**.
- **Bữa đặc biệt:** năm interface chồng lấn — `SpecialMealRequest` (1316), `SpecialMealPlan` (1615), `SpecialMealRecord` (1631), `SpecialMealPlanRecord` (1816), `SpecialDietRequirement` (1770).
- **Kiểm tra:** `InspectionSession` (268) vs `KitchenInspection` (1065).
- **Hệ quả:** cùng thực thể nhưng nhiều nguồn sự thật → dữ liệu dễ lệch, khó đồng bộ status, tăng chi phí bảo trì.
- **Đề xuất:** hợp nhất về 1 mô hình chuẩn cho mỗi khái niệm (violation / special-meal / inspection), phần khác nhau tách bằng `type`/`view`.

### E2. Ngày lưu dạng chuỗi `"dd/MM/yyyy"` — không sort/so sánh/tính được
- 20+ field ngày là `string` (`submittedAt`, `reportedAt`, `collectedAt`, `deadline`, `expiryDate`…).
- Bằng chứng hệ quả: mock-server **không** sort theo ngày; nhiều nơi phải tự viết `parseVnDate`/`parseDate` để so hạn dùng (BR-071). Sort cột ngày trong bảng sẽ **sai** (so sánh chuỗi từ điển).
- **Đề xuất:** lưu ISO (`2026-07-02`) hoặc timestamp; chỉ format khi hiển thị.

### E3. Số lượng & tiền không đồng nhất, nhúng đơn vị vào chuỗi
- **Lượng:** `InventoryBatch.quantity` = `"45 kg"` / `"40 lít"` / `"1.200 quả"` (string) → không cộng/quy đổi → không tổng tồn kho, không tính định mức. `servings: "1.841"` cũng là chuỗi.
- **Tiền:** `MenuItem.estimatedCost: number` (28500) nhưng `PurchaseRequest.totalValue: string` = `"9.800.000 ₫"` (`mock-data.ts:576`); `PurchaseItem.unitPrice/lineTotal` cũng chuỗi "₫" → **không tổng chi tiêu được**.
- **Đề xuất:** tách `value: number` + `unit`/`currency` riêng; format ở tầng hiển thị.

### E4. Field DẪN XUẤT bị lưu cứng (dễ lệch nguồn thật)
- `MenuItem.dishes` (số món) & `allergyConflicts` (số xung đột) — lưu số cứng, sẽ lệch khi menu có danh sách món thật (nối **D1**) và khi khai báo dị ứng thay đổi.
- `FoodSample.hoursRemaining: number` (`mock-data.ts:351`) — lưu cứng, hiển thị nguyên (`còn X giờ`, `samples/[id]:187`) dù đáng lẽ = `retentionDeadline − now`; mở lại hôm sau vẫn hiện số cũ → **sai hạn hủy mẫu** (rủi ro pháp lý BR-111).
- `Delivery.batches` (số lô) tương tự.
- **Đề xuất:** tính runtime từ nguồn thật, không seed số dẫn xuất.

### E5. Sinh ID phía client bằng `Date.now().slice(-n)` → nguy cơ TRÙNG
- `PR-2026-${Date.now().slice(-4)}` (`service-caterers/orders/new/page.tsx:106`), `INSP-…${slice(-2)}` (`inspections/page.tsx:48`), `RCV-…${slice(-2)}` (`receiving/new/page.tsx:176`), `TXN-${slice(-6)}` (`inventory/*`), `STF-${slice(-4)}` (`staff/page.tsx:132`), `HS-${cls}-${slice(-3)}` (`students/page.tsx:96`)…
- Hai thao tác trong cùng mili-giây, hoặc hậu tố ngắn `slice(-2)/(-3)`, dễ **trùng id** → ghi đè bản ghi.
- **Đề xuất:** để mock-server `nextId()` (đã có, `mock-server.ts:114`) cấp phát tập trung thay vì mỗi màn tự chế.

### E6. `status` dùng chung một union phẳng cho MỌI thực thể
- Mọi entity khai `status: keyof typeof STATUS_BADGE` — một union phẳng gộp status của menu, sự cố, mẫu, vi phạm, giao hàng… → TypeScript **không chặn** việc gán nhầm status của domain này cho domain khác.
- **Đề xuất:** union status riêng theo từng thực thể (vd `MenuStatus`, `IncidentStatus`), `STATUS_BADGE` vẫn là nơi tra nhãn/màu.

---

## F. Bảng tổng hợp & ưu tiên

| # | Vấn đề | Nhóm | Tác động điều tra | Công sức | Ưu tiên |
|---|---|---|---|---|---|
| A1 | Món nối bằng tên, thiếu `dishId` | A | Cao | Vừa–Cao | **P0** |
| A2 | HS bị ảnh hưởng không link `Student.id` | A | Cao | Vừa | **P0** |
| A7 | Màn truy xuất chỉ là bảng phẳng | A | Cao | Cao | **P1** (sau A1–A6) |
| B1/B2 | Món nghi vấn hardcoded + không lưu khi tạo | B | Cao | Thấp | **P0** |
| D1/D5 | Menu không có danh sách món; Phụ huynh không xem theo khối | A+D | Cao | Cao | **P1** |
| D6 | Không thống kê suất theo món (`assignedMeal` là blob) | A+D | Cao | Vừa | **P1** |
| D3/D4 | Món thay thế & bữa đặc biệt rời rạc, không link `dishId`/`studentId` | A+D | Trung bình | Vừa | P1–P2 |
| A4/A5 | Sự cố→mẫu chỉ link ngược; mẫu thiếu `batchId` | A | Trung bình | Vừa | P1 |
| A3/A6 | Lớp/người/NCC bằng tên | A | Trung bình | Vừa | P1 |
| C1 | Ngưỡng BR gõ cứng nhiều nơi | C | Trung bình (nhất quán dữ liệu) | Thấp–Vừa | P1 |
| B3/B4 | Lab + danh mục lặp/nhập tay | B | Thấp–TB | Thấp | P2 |
| C2/C3 | Units & bữa ăn lệch chuẩn | C | Thấp–TB | Thấp | P2 |
| C4/C5 | Dinh dưỡng & filter status hardcoded | C | Thấp | Thấp | P2 |
| E4 | Field dẫn xuất lưu cứng (`hoursRemaining` sai hạn hủy mẫu) | E | Trung bình (pháp lý) | Thấp | **P1** |
| E5 | ID client `Date.now().slice` dễ trùng | E | Trung bình (mất dữ liệu) | Thấp | **P1** |
| E2/E3 | Ngày/tiền/lượng là chuỗi — không sort/tổng hợp | E | Trung bình | Vừa | P2 |
| E1 | Mô hình trùng (violation/special-meal/inspection) | E | Thấp–TB | Cao | P2 |
| E6 | `status` union phẳng dùng chung | E | Thấp | Vừa | P3 |

---

## G. Lộ trình đề xuất

1. **Đợt 1 (P0 — mở đường cho điều tra + quick win an toàn):**
   - Chuẩn hóa **món theo `DishNutrition.id`** (đã có sẵn id) + chuyển `suspectedDishIds`/`DISH_BATCHES`/`FoodSample` sang `dishId` (A1/D2).
   - `Incident.affectedStudentIds` → `Student.id` (A2).
   - Giáo viên chọn món **động từ thực đơn** và lưu `suspectedDishIds`+`mealDate` khi tạo (B1/B2).
   - **Quick win:** tính `hoursRemaining` runtime (E4 — tránh sai hạn hủy mẫu); chuyển sinh ID về `nextId()` tập trung (E5).
2. **Đợt 2 (P1 — hoàn thiện mạch & nhất quán):**
   - Dựng tầng **Menu → MenuDay → MenuDish(dishId, plannedServings)** thay `WEEKLY_MENU_DAYS` global; màn Phụ huynh query theo `grade` của con; suy món thay thế theo `allergens` ∩ khai báo (D1/D3/D5).
   - `StudentDistribution.dishServings[]` thay blob → thống kê **suất theo món** (D6); `SpecialMealRequest.studentId` (D4).
   - `classroomId`/`reportedById` (A3), chuyển người/NCC sang `*Id` (A6), `Incident.retrievedSampleIds` + `FoodSample.batchIds` (A4/A5).
   - Gom `BUSINESS_RULES` tập trung (C1).
   - Dựng màn **Bản đồ truy xuất theo sự cố** dùng các khóa ID vừa có (A7).
3. **Đợt 3 (P2/P3 — dọn cấu hình & chuẩn hóa kiểu):** `LAB_DESTINATIONS`, danh mục vi phạm/diet dùng chung (B3/B4), `UNITS`/`MEAL_SHIFTS` (C2/C3), `NUTRITION_BY_AGE` config + derive filter status từ `STATUS_BADGE` (C4/C5); chuẩn hóa ngày ISO + tách `value/unit/currency` (E2/E3); hợp nhất mô hình trùng (E1); union status theo thực thể (E6).

> **Ghi chú phối hợp:** A1–A6 chạm mock database dùng chung (`mock-data.ts`) và nhiều actor (Medical Staff, Service Caterers, Supplier, Teacher) → cần thống nhất với cả nhóm trước khi đổi khóa, tránh vỡ chéo. Nguyên tắc đổi mock: giữ tên hiển thị, thêm trường ID mới (không xóa field cũ đột ngột).
