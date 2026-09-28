# Rà soát NCC · Bếp · Thực đơn — tổng hợp gap từ phiên trao đổi 25/07/2026

> **Phạm vi:** tổng hợp toàn bộ gap được phát hiện/xác nhận trong phiên trao đổi ngày 25/07/2026, đối chiếu trực tiếp với code `03_Design/08_FrontEnd/Web/src/`.
> **Không lặp lại** nội dung đã có trong `review_data_model_traceability_for_backend.md` (T1–T4, chuỗi truy xuất Thực đơn↔Học sinh↔Lô↔Sự cố) — file đó vẫn là nguồn chính cho 4 điểm gãy đó. Tài liệu này bổ sung các gap **mới** phát sinh từ câu hỏi thực tế của Nam, tập trung vào 3 mảng: quan hệ với NCC, vận hành bếp, thực đơn.
> **Người rà:** Nam (School Admin / System Admin).

---

## 0. Tóm tắt cho người quyết

| # | Gap | Mảng | Mức | Đã có hướng sửa? |
|---|---|---|---|---|
| G1 | Service Caterer không có màn tra cứu độc lập "Danh sách NCC" / "NCC này bán gì" | NCC | P2 | ✅ (mục 1.1) |
| G2 | Hồ sơ giấy tờ NCC không theo checklist cố định, không tách cấp mặt hàng | NCC | P1 | ✅ (mục 1.2) |
| G3 | Giá nguyên liệu (`SupplierProduct.unitPrice`) & trạng thái duyệt NCC (`Partner.status`) dùng chung toàn hệ thống, không phân theo trường | NCC | **P0** | ✅ (mục 1.3) — còn 1 câu hỏi mở |
| G4 | Thiếu loại giấy tờ "theo lô" (phiếu kiểm nghiệm/kiểm dịch) — model hiện chỉ có giấy tờ cấp cơ sở | NCC | P1 | ✅ (mục 1.4) |
| G5 | Khai mặt hàng cung ứng không gate theo hợp đồng cụ thể | NCC | P2 | ⚠️ nêu vấn đề, chưa thiết kế |
| G6 | Danh mục nguyên liệu chung (`INGREDIENT_CATALOG`) không có workflow đề xuất thêm mới | NCC/Bếp | P1 | ⚠️ nêu hướng, chưa chốt ai duyệt |
| G7 | Kiểm thực 3 bước chưa tự động điền sẵn từ dữ liệu thật — vẫn phải chọn tay từng dòng | Bếp | P1 | ✅ (mục 2.1) |
| G8 | "Phiên kiểm thực mới" không cho chọn ngày/buổi trước khi mở | Bếp | P1 | ✅ (mục 2.2) |
| G9 | Chưa có Menu mẫu (template) — mỗi thực đơn phải khai lại từ đầu | Thực đơn | P1 | ✅ (mục 3.1) |
| G10 | Món thay thế dị ứng (`substitutes`) gắn ở cấp thực đơn, không gắn ở Thư viện món — phải khai lại mỗi lần dùng | Thực đơn | **P0** | ✅ (mục 3.2) — chặn G9 phát huy hết tác dụng |
| G11 | Thực phẩm "dùng ngay" (sữa hộp, bánh mì, hoa quả tráng miệng...) bị ép qua quy trình nhập-kho-rồi-xuất-kho 2 màn hình riêng, dù không có quyết định FEFO nào cần đưa ra | Bếp | P2 | ✅ (mục 2.3) |

**Ưu tiên đề xuất:** G10 nên làm cùng lúc hoặc trước G9 (menu mẫu vô nghĩa nếu substitute vẫn phải gõ lại tay mỗi tuần). G3 là quyết định kiến trúc ảnh hưởng rộng (đa trường) — nên chốt sớm vì mọi entity NCC khác đều phụ thuộc vào quyết định này.

---

## 1. Quan hệ với Nhà cung cấp

### 1.1. G1 — Service Caterer không có màn tra cứu NCC độc lập

**Hiện trạng:** Menu điều hướng của Service Caterers (`src/lib/roles.ts:238-295`) không có mục "Nhà cung cấp". NCC chỉ xuất hiện gián tiếp:
- Nhúng trong `service-caterers/orders/new/page.tsx:124-128` — chọn NCC trong dropdown khi tạo đơn thì mới thấy `supplier-products` lọc theo `supplierId`.
- Màn "Danh sách NCC" đầy đủ chỉ có ở **School Admin** (`/school-admin/partnerships`), phục vụ mục đích duyệt hồ sơ (BR-040/041), không phải màn tra cứu cho bếp.

**Đề xuất:** thêm route `/service-caterers/suppliers` (list, đọc-only) + `/service-caterers/suppliers/[id]` (chi tiết + danh sách mặt hàng NCC đó cung ứng), dựng theo pattern có sẵn của `school-admin/partnerships`.

### 1.2. G2 — Hồ sơ giấy tờ NCC: checklist cố định + tách cấp mặt hàng

**Đầu vào thực tế:** Excel trường đang dùng có 6 loại giấy tờ cố định (Hợp đồng, Giấy ĐKKD, Chứng nhận ATTP, Hồ sơ công bố chất lượng, Phiếu kiểm nghiệm, Chứng nhận nguồn gốc xuất xứ) và theo dõi **theo từng mặt hàng** — cùng 1 NCC nhưng mặt hàng A đủ giấy, mặt hàng B thiếu, khác nhau dòng.

**Hiện trạng model** (`mock-data.ts:1421-1462`, `4471-4496`): `Partner.documents: PartnerDocument[]` là danh sách tự do (`name` là string, không phải enum), gắn **duy nhất ở cấp NCC**. `SupplierProduct` không có field giấy tờ nào.

**Đề xuất:**

```
DocumentType ENUM: CONTRACT | BUSINESS_LICENSE | FOOD_SAFETY_CERT
                  | QUALITY_DECLARATION | TEST_REPORT | ORIGIN_CERT

PartnerDocument            -- cấp NCC (pháp nhân), áp cho mọi mặt hàng
  id, partner_id, doc_type ENUM(6 loại), doc_number, issued_at, expiry_date NULL,
  status ENUM(VALID|EXPIRING|URGENT|MISSING), file_url NULL, note TEXT NULL
  UNIQUE (partner_id, doc_type)

SupplierProductDocument    -- cấp mặt hàng — bảng MỚI
  id, supplier_product_id, doc_type ENUM(6 loại),
  status ENUM(OK|MISSING|EXPIRED), doc_number NULL, expiry_date NULL,
  note TEXT NULL, checked_by, checked_at

SupplierProduct.complianceStatus  -- derived: OK nếu đủ mọi doc_type bắt buộc
```

**Chốt chặn mới cho backend:** API tạo PO chỉ hiện `SupplierProduct` có `complianceStatus = OK` — mở rộng BR-041 xuống cấp mặt hàng (hiện chỉ chặn ở cấp NCC qua `partner.status === APPROVED`).

### 1.3. G3 (P0) — Giá & trạng thái duyệt NCC chưa phân theo trường

**Phát hiện:** `SupplierProduct.unitPrice` (`data_model.puml:227-236`) là **1 giá duy nhất** cho mỗi cặp `(supplierId, ingredientId)`, dùng chung toàn hệ thống. Không có `schoolId` ở bất kỳ đâu trong `Partner`, `SupplierProduct`, `CatalogIngredient`. `PurchaseRequest.school` / `Delivery.school` có field `school` nhưng là **String tự do**, không phải FK.

**Hệ quả nếu không sửa:** trường A duyệt/đình chỉ 1 NCC thì toàn hệ thống (kể cả trường B chưa từng làm việc với NCC đó) thấy cùng trạng thái — mâu thuẫn BR-176 ("dữ liệu giới hạn theo phạm vi trường"). Giá cũng vậy — không phản ánh đúng thực tế đàm phán hợp đồng riêng từng trường (đã xác nhận qua trao đổi: giá NÊN khác theo từng trường vì phụ thuộc sản lượng, khoảng cách giao hàng, đàm phán riêng).

**Đề xuất:**

```
SupplierProduct(id, supplier_id, ingredient_id, unit, packaging, min_order_qty)  -- KHÔNG còn unitPrice
SchoolSupplierPrice(id, school_id, supplier_product_id, unit_price, effective_from, effective_to)
  UNIQUE (school_id, supplier_product_id, effective_from)
```

**Câu hỏi còn mở (cần Nam chốt):** `Partner.status` (Approved/Suspended) có nên tách theo trường luôn không (`SchoolPartner(school_id, partner_id, status, ...)`), hay giữ 1 trạng thái duyệt dùng chung, chỉ tách phần giá? Lý lẽ tách: trường A đình chỉ NCC vì sự cố ở trường A không có nghĩa trường B cũng gặp vấn đề tương tự — cùng logic với giá.

### 1.4. G4 — Thiếu loại giấy tờ "theo lô", khác giấy tờ "theo cơ sở"

**Phát hiện:** Model hiện tại đã có `SupplierDocument` (`mock-data.ts:1653-1669`) với `documentType: FOOD_SAFETY_CERT | PRODUCT_ORIGIN | BUSINESS_LICENSE | SELF_DECLARATION`, và `SupplierBatch.certificates: string[]` (`data_model.puml:237-248`) tham chiếu lại các document này — **đúng thực tế cho giấy tờ cấp cơ sở** (ATTP, ĐKKD... cấp 1 lần, dùng lại nhiều lô, đúng BR-071 chỉ yêu cầu "kèm chứng nhận" mà không nói phải mới mỗi lần).

**Nhưng thiếu hẳn `TEST_REPORT`** (phiếu kiểm nghiệm) — loại giấy tờ mà thực tế **phải làm lại mỗi lô** (đặc biệt hàng tươi sống: chất lượng lô này có thể khác lô kia dù cùng NCC, cùng nguyên liệu). Đây đúng là cột "Phiếu kiểm nghiệm" trong Excel trường đang dùng, hiện chưa có chỗ mô hình hoá.

**Đề xuất:** thêm `TEST_REPORT` vào `documentType`; khi loại này được tạo, **bắt buộc gắn `batch_id`** và **không được join lại cho lô khác** (khác hẳn 4 loại kia vốn tái sử dụng qua nhiều lô).

### 1.5. G5 — Khai mặt hàng cung ứng không gate theo hợp đồng cụ thể

**Hiện trạng:** NCC tự khai mặt hàng (`supplier/products/page.tsx`), lưu ngay không qua duyệt (chỉ cần `Partner.status = APPROVED` từ trước). Không có liên kết cấu trúc giữa `PartnerDocument` (loại `CONTRACT`) và các `SupplierProduct` cụ thể mà hợp đồng đó cho phép bán.

**Rủi ro:** nếu thực tế trường ký hợp đồng riêng theo nhóm mặt hàng (VD hợp đồng rau củ riêng, hợp đồng thịt riêng), hệ thống hiện không biết "hợp đồng nào cho phép bán những gì" — NCC có thể tự thêm mặt hàng ngoài phạm vi hợp đồng đã ký mà không ai chặn.

**Trạng thái:** mới nêu vấn đề, **chưa thiết kế bảng cụ thể** — cần Nam xác nhận trường có thực sự ký hợp đồng theo nhóm mặt hàng hay theo cả gói trước khi thiết kế thêm.

### 1.6. G6 — Danh mục nguyên liệu chung chưa có workflow đề xuất thêm mới

**Hiện trạng:** `INGREDIENT_CATALOG` hoàn toàn tĩnh — không có API (`mock-server.ts` không có resource `ingredients`), không màn hình CRUD ở bất kỳ role nào. Nơi duy nhất chạm tới nguyên liệu ngoài danh mục là `service-caterers/dishes/page.tsx:487` — chỉ gắn badge cảnh báo "X ngoài danh mục" khi đầu bếp gõ tên tự do, không có nút đề xuất chính thức.

**Đề xuất hướng (chưa chốt người duyệt):** Service Caterer đề xuất (vì họ là người gõ tên nguyên liệu ngoài danh mục khi tạo món) → School Admin hoặc System Admin duyệt, theo pattern PENDING→APPROVED/REJECTED giống `Partnerships`.

```
IngredientProposal(id, proposed_name, unit, category, allergens[], proposed_by,
                   status ENUM(PENDING|APPROVED|REJECTED), reject_reason NULL,
                   reviewed_by NULL, reviewed_at NULL)
```

---

## 2. Vận hành bếp

### 2.1. G7 — Kiểm thực 3 bước chưa tự động điền sẵn từ dữ liệu thật

**Đã đúng hướng, còn thiếu 1 nấc tự động hoá.** Code hiện tại (`inspections/[id]/page.tsx:867-882`) đã tính đúng dữ liệu nguồn:
- Bước 1 ← `step1RowsOfDate(session.date, liveInventory)` — lô đã nhập kho đúng ngày.
- Bước 2/3 ← `step2RowsOfMeal`/`step3RowsOfMeal` — thực đơn đúng bữa (mọi khối) + BOM.

Nhưng dữ liệu này chỉ nằm trong dropdown `CatalogSelect` để **chọn từng dòng** — form khởi tạo với 1 dòng trống (`emptyFresh()`/`emptyStep2()`/`emptyStep3()`), bếp phải bấm "+ Thêm" và chọn tay từng dòng, chưa tự render sẵn toàn bộ danh sách.

**Đề xuất:** đổi state khởi tạo từ 1 dòng trống thành map sẵn toàn bộ `step1Catalog`/`step2Catalog`/`step3Catalog` thành rows ngay khi mở phiên — hạ tầng dữ liệu đã đủ, chỉ cần đổi cách khởi tạo state, không phải xây lại liên kết.

### 2.2. G8 — "Phiên kiểm thực mới" không cho chọn ngày/buổi

**Hiện trạng:** `inspections/page.tsx:46-68` — nút "Phiên kiểm thực mới" tự gán `date: hôm nay`, `meal: "Bữa trưa"` cứng, không hỏi ngày/buổi nào.

**Rủi ro:** nếu không sửa trước khi làm G7, phiên kiểm thực buổi sáng có thể vô tình lấy nhầm dữ liệu buổi trưa khi tự động điền.

**Đề xuất:** thêm bước chọn ngày + buổi khi mở phiên (giống UX trang xuất kho `inventory/issue/page.tsx`), làm **trước** G7.

### 2.3. G11 — Thực phẩm "dùng ngay" (direct-use / cross-docking)

**Bối cảnh (Nam đưa ra, đã trao đổi trước với 1 AI khác):** một số thực phẩm không nằm chờ trong kho — sữa hộp/sữa chua tiệt trùng, bánh mì tươi, hoa quả tráng miệng, hoặc suất ăn bếp trung tâm chở tới — nhận về là dùng hết ngay trong bữa đó, không có quyết định FEFO nào cần đưa ra (chỉ có đúng 1 lô, dùng 100%). Nhưng vẫn **bắt buộc phải có dữ liệu lô/NCC** để truy xuất khi có sự cố (đúng BR-071/076).

**Đánh giá đề xuất của AI kia:** hướng đúng (không bỏ qua truy xuất, chỉ gộp thao tác), nhưng có 2 chỗ **không khớp với model hiện tại**, nếu làm theo nguyên văn sẽ tạo ra khái niệm thừa:

1. Họ đề xuất thêm `InventoryBatch.status = EXHAUSTED/RELEASED` — **không cần thiết**. Tồn kho còn lại của 1 lô trong hệ thống hiện tại là giá trị **suy diễn**, không phải cột lưu tay:
   ```ts
   // mock-data.ts:5218-5229
   batchRemaining(batchId) = quantity ban đầu − tổng mọi giao dịch xuất đã ghi cho lô đó
   ```
   Chỉ cần tự động sinh 1 giao dịch xuất bằng đúng số lượng vừa nhập, `batchRemaining()` tự trả về 0 — không đụng gì tới `status`. Và `isBatchLocked()` (`mock-data.ts:5205-5215`) xét cách ly (`quarantine != null`) **độc lập với tồn còn lại** — nghĩa là 1 lô đã dùng hết 100% vẫn cách ly được bình thường khi điều tra sự cố, không cần trạng thái đặc biệt nào cho lô "đã xuất hết".

2. Họ vẽ "Kiểm thực Bước 1" = luôn cả bước **nhận hàng**. Trong hệ thống hiện tại đây là **2 bản ghi/2 màn hình khác nhau**: `ReceivingSession`/`InventoryBatch` (nhận hàng, `receiving/new/page.tsx`) và `InspectionSession.step1` (Kiểm thực 3 bước theo QĐ 1246 — Mẫu 1, `inspections/[id]/page.tsx`, đã bàn ở G7). Luồng rút gọn chỉ nên áp cho **bước nhận hàng → nhập/xuất kho**; Kiểm thực 3 bước theo luật vẫn phải làm đủ tuần tự (BR-079) như bình thường — không có "phiên bản rút gọn" cho food-safety inspection, chỉ rút gọn thao tác kho.

**Đề xuất sửa lại cho khớp model hiện tại:**

```
CatalogIngredient.consumptionType ENUM('RAW_MATERIAL' | 'READY_TO_EAT')
  -- cờ đặt ở NGUYÊN LIỆU (dùng chung), không đặt ở SupplierProduct — vì "dùng ngay
  -- hay không" là bản chất của nguyên liệu (sữa hộp luôn dùng ngay dù mua từ NCC nào)
```

Màn `receiving/new/page.tsx` khi dòng lô có `ingredient.consumptionType = READY_TO_EAT`, hiện thêm ô **"Dùng ngay cho bữa nào"** (chọn `menuId` + `mealType` trong ngày, tái dùng danh sách `issuableMenus()` đã có sẵn ở `inventory/issue/page.tsx`). Bấm "Chấp nhận hàng" thì ngoài `createBatch(...)` như hiện tại (dòng 250 file đó), tạo thêm luôn **2 `InventoryTransaction`** trong cùng transaction: 1 dòng thể hiện đã nhập, 1 dòng xuất thẳng 100% cho `menuId/mealType` đã chọn (đúng schema `InventoryTransaction` sẵn có — không cần field mới) — thay cho việc thủ kho phải mở riêng trang "Xuất kho theo bữa" chỉ để xuất nốt đúng 1 lô vừa nhập.

**Không đổi:** Kiểm thực 3 bước, lưu mẫu (`FoodSample`, BR-105) vẫn bắt buộc như mọi thực phẩm khác — item `READY_TO_EAT` vẫn lên đúng `step1Catalog` (vì vẫn là 1 `InventoryBatch` bình thường, chỉ tự động xuất kèm) nên không phá vỡ luồng G7 đã bàn.

---

## 3. Thực đơn

### 3.1. G9 — Chưa có Menu mẫu (template)

**Hiện trạng:** `menus/create/page.tsx:9,20-21` chỉ prefill khối + ngày từ URL params (khi bấm ô lưới tuần); toàn bộ món ăn từng buổi phải chọn lại từ đầu mỗi lần, không có "Sao chép từ tuần trước" hay "Áp dụng mẫu".

**Đề xuất (đã thống nhất qua trao đổi — mẫu KHÔNG gắn ngày/thứ, chỉ gắn khối + đối tượng):**

```
MenuTemplate(id, name*, grade, audience, is_active)
  -- name bắt buộc, người dùng tự gõ (không tự sinh được vì không có ngày để ghép),
  -- dùng để tìm kiếm/lọc trong danh sách mẫu (search theo name, filter theo grade/audience)
MenuTemplateMeal(id, template_id, meal_type)              -- BREAKFAST/LUNCH/SNACK, không có day_of_week
MenuTemplateDish(id, template_meal_id, dish_id, planned_servings)
```

Khi lên thực đơn tuần thật: `MenuMeal(menu_id, meal_type, template_id NULL)` — ghi lại mẫu đã áp (nếu có) để đối chiếu sau. UI: mỗi ô (ngày × buổi) trong lưới tuần thêm lựa chọn "Áp dụng mẫu" bên cạnh khai tay hiện có; chọn mẫu → tự đổ món + substitute (xem G10) → School Admin/Service Caterer chỉnh tay nếu cần khác biệt → gửi duyệt như luồng hiện có (không đổi `Menu.status` workflow).

### 3.2. G10 (P0) — Món thay thế dị ứng gắn sai cấp, chặn G9 phát huy tác dụng

**Phát hiện:** `Dish` (thư viện món dùng chung, `mock-data.ts:3045-3059`) **không có field `substitutes`**. Trường `substitutes` chỉ tồn tại ở `DishForm`/`MenuDish` — tức **khai lại mỗi lần đưa 1 món vào 1 thực đơn cụ thể** (`mock-data.ts:3516,3525`).

**Hệ quả:** tuần này khai "Gà rang gừng → thay bằng Thịt rim (dị ứng Tôm)", tuần sau đưa lại đúng món "Gà rang gừng" vào thực đơn khác thì phải khai lại y hệt — hệ thống không nhớ ở cấp thư viện. Nếu làm G9 (Menu mẫu) mà không sửa gap này, mẫu vẫn phải khai lại substitute mỗi khi áp dụng, không đạt được mục tiêu "chọn sẵn không cần nhập lại".

**Đề xuất:**

```
DishSubstituteDefault(id, dish_id,
                      target_type ENUM(ALLERGEN|DIET_GROUP),   -- xem thêm gap 1.4 T-target_type ở review cũ
                      allergen_id NULL, diet_group_id NULL,
                      substitute_dish_id, portions)
```

Gắn vào `Dish` (thư viện) thay vì `DishForm` (thực đơn) → dùng lại được ở **mọi** thực đơn, kể cả khi áp dụng từ `MenuTemplate`. Khi khai thực đơn (tay hoặc từ mẫu), hệ thống tự suy substitute từ `DishSubstituteDefault` của từng `dish_id`; School Admin/Caterer chỉ cần ghi đè nếu tuần đó có khác biệt (VD hết nguyên liệu món thay thế mặc định).

---

## 4. Điểm đã xác nhận ĐÚNG — không cần sửa

Ghi lại để tránh làm lại việc đã tốt:

- **Xuất kho theo bữa** (`inventory/issue/page.tsx`) — gộp nhu cầu mọi khối theo ngày+buổi, FEFO tự phân bổ lô, cho sửa số suất, đúng 100% với đề xuất "dựa vào menu → xuất kho, cho chỉnh nếu cần".
- **Multi-supplier cho 1 nguyên liệu** — hệ thống đã hỗ trợ đúng qua `shortageBySupplier`/`suggestedSupplierName` (`orders/new/page.tsx`), không ép "1 nguyên liệu = 1 NCC" (giả định ban đầu của Nam là sai so với thực tế, và may là code cũng không ép theo giả định đó).
- **Vòng báo giá theo từng đơn** (PENDING → QUOTED → School Admin duyệt giá, `orders/[id]/page.tsx`) — bù đắp một phần cho việc NCC tự do khai giá "niêm yết" (G3), vì giá thực áp dụng cho từng đơn vẫn qua xác nhận.
- **Giấy tờ cấp cơ sở tái sử dụng qua nhiều lô** (`SupplierDocument` + `SupplierBatch.certificates[]`) — đúng thực tế cho ATTP/ĐKKD/tự công bố, chỉ thiếu loại theo-lô (G4).

---

## 5. Thứ tự đề xuất triển khai

1. **G10** trước (hoặc cùng lúc) **G9** — sửa gốc `DishSubstituteDefault` rồi mới xây `MenuTemplate`, tránh xây mẫu xong vẫn phải khai tay substitute.
2. **G3** — quyết định kiến trúc đa trường (giá + có tách `Partner.status` theo trường hay không) — ảnh hưởng schema của mọi entity NCC khác, nên chốt sớm.
3. **G2 + G4** — cùng một đợt sửa `PartnerDocument`/`SupplierDocument`/`SupplierProductDocument`, vì đụng chung khái niệm "giấy tờ NCC".
4. **G6** — thêm `IngredientProposal`, chốt ai duyệt trước khi code.
5. **G8 rồi G7** — bếp: cho chọn ngày/buổi trước, rồi mới tự động điền sẵn form kiểm thực.
6. **G1, G5, G11** — mức P2, làm sau cùng hoặc gộp chung đợt UI khi rảnh. G11 nên làm sau G7/G8 vì cùng đụng màn kiểm thực/kho, gộp 1 đợt cho đỡ phải test lại 2 lần.

---

## 6. Giới hạn của tài liệu này

- Dựa trên trao đổi hỏi-đáp một phiên, chưa qua review chéo với Mạnh (DB)/Huy (Traceability) như quy trình 13 module ở `07_implement/00_Team_Work_Assignment_Plan.md`.
- Các câu hỏi "còn mở" (G3 phần `Partner.status`, G5) cần Nam xác nhận thực tế nghiệp vụ trước khi chốt schema — không tự quyết thay.
- Chưa đối chiếu ngược với `business_rules.md` xem có BR nào cần thêm số hiệu mới cho các gap G1–G10 — cần làm khi đưa vào TDS chính thức.
