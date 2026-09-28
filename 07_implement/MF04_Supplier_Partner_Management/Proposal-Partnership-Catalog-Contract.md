# PROPOSAL — Danh mục sản phẩm & giá theo hợp đồng partner × school (MF04)

| Field | Value |
|-------|-------|
| **Status** | **`Proposed — CHƯA duyệt`**, không phải TDS chính thức. Không sửa `TDS-MF04_Backend.md` (Document Owner: Nguyễn Đức Mạnh, Approved 2026-08-02) — tài liệu này là đề xuất độc lập, chờ BA/PM bổ sung UC/BR tương ứng vào `business_rules.md`/UC file trước khi được ghép vào TDS thật. |
| **Tác giả** | Đề xuất theo yêu cầu Nam (namnvhe180863), từ trao đổi thiết kế 2026-08-07 — không đại diện quyết định chính thức của module owner. |
| **Vì sao cần** | `SupplierProduct` (MF06, `supplier_products`) hiện là danh mục + giá **toàn cục theo NCC**, dùng chung cho mọi trường đang hợp tác. Thực tế mỗi trường chỉ ký hợp đồng với một số sản phẩm/giá nhất định (không phải "muốn bán gì thì bán, muốn đổi giá thì đổi"); thay đổi phải qua trường duyệt. Không UC/BR nào trong `02_Requirements` hiện mô tả ràng buộc này — đây là **khoảng trống yêu cầu (requirement gap)**, không chỉ khoảng trống thiết kế. |

## 1. Đề xuất bổ sung yêu cầu (để BA/PM xác nhận, cấp mã BR/UC thật)

- **BR-đề xuất-1**: Nhà cung cấp chỉ được giao (và lập yêu cầu mua hàng) các sản phẩm/giá đã được trường phê duyệt trong hợp đồng hợp tác (`supplier_partnerships`); mọi thay đổi sản phẩm/giá phải qua yêu cầu và được trường duyệt trước khi có hiệu lực.
- **UC-đề xuất-1** (actor Raw Food Supplier): Đề xuất thêm/sửa sản phẩm-giá cho một trường đang hợp tác.
- **UC-đề xuất-2** (actor School Administration): Duyệt/từ chối đề xuất thay đổi danh mục sản phẩm-giá của NCC.

*(Mã BR/UC thật — nếu được chấp nhận — do Huy/Lead Traceability cấp theo đúng `00_Common_Conventions.md` §2c, KHÔNG tự đặt số ở đây.)*

## 2. Contract kỹ thuật đề xuất (tham khảo khi review chính thức)

**Entity mới (package `com.sfms.mf04.domain`, extends `BaseEntity`, theo đúng khuôn D1/D9):**

- `PartnershipCatalogItem` (`partnership_catalog_items`): `code`, `partnershipId` (FK `supplier_partnerships`), `partnerId`, `schoolId` (denormalized — cùng khuôn `supplier_partnerships` mang thẳng cả hai khóa tenant, tránh join khi lọc CF-M04-6), `ingredientId`, `unit`, `approvedUnitPrice`, `packaging`, `effectiveFrom`, `effectiveTo` (nullable), `status` (`ACTIVE`/`INACTIVE`).
- `CatalogChangeRequest` (`catalog_change_requests`): `code`, `partnershipId`, `partnerId`, `schoolId`, `catalogItemId` (nullable — null = đề xuất sản phẩm mới), `ingredientId`, `requestedUnit`, `requestedUnitPrice`, `requestedPackaging`, `reason`, `status` (`PENDING`/`APPROVED`/`REJECTED`), `decidedByUserId`, `decidedAt`, `decisionNote`.

**API đề xuất:**
- `GET /api/v1/partnerships/{partnershipCode}/catalog-items` — SUPPLIER (chính mình) / SCHOOL_ADMIN (trường mình) / SYSTEM_ADMIN.
- `POST /api/v1/partnerships/{partnershipCode}/catalog-change-requests` — SUPPLIER tạo đề xuất, trạng thái PENDING.
- `GET /api/v1/catalog-change-requests` — liệt kê theo scope actor (SUPPLIER: `partnerId`; SCHOOL_ADMIN: `schoolId`).
- `POST /api/v1/catalog-change-requests/{code}/decision` — SCHOOL_ADMIN duyệt/từ chối; duyệt ⇒ upsert `PartnershipCatalogItem` tương ứng.

**Lỗi dùng lại** (không cấp mã SUP-xxx mới, theo tiền lệ "3 mã dùng-chung-mọi-module" ở CHANGELOG 2026-08-02 mục 6): `SupExceptions.notFound()`, `businessConflict()`, `validationFailed()`, `reasonRequired()`.

## 3. Mã nguồn liên quan (đã triển khai theo đề xuất này, chờ review chính thức)

- Migration: `V48__mf04_partnership_catalog_proposed.sql`, `V49__mf04_mf06_packaging_conversion_proposed.sql`, `V50__mf04_document_scope_proposed.sql` (hậu tố `_proposed` để không lẫn với migration đã qua review chuẩn — đổi tên khi được duyệt).
- Entity/Repository/Service/Controller: xem javadoc đầu file, mỗi file dẫn ngược về tài liệu này thay vì một BR/UC thật.

## 4. Phase 2 — Quy đổi số lượng theo quy cách đóng gói (đã triển khai)

- `PartnershipCatalogItem` thêm `packagingSize`/`allowPartialPackage`; `PurchaseItem` (mf06) thêm `requestedQuantity` (số thực cần, trước quy đổi) — `quantity` giữ ngữ nghĩa cũ (số đã quy đổi/thực đặt).
- `PackagingCalculator` (mf06/service) — hàm thuần `roundedQuantity()`.
- `ReferenceResolver.partnershipCatalogPackaging()` — mf06 tra cứu thuần bảng `partnership_catalog_items` của mf04, không import entity/repository chéo module.
- **Mặc định an toàn**: khi hợp đồng (partner, school, ingredient) chưa có dòng danh mục nào (trạng thái hiện tại của MỌI hợp đồng, vì đây là bảng mới chưa có dữ liệu) → hành vi giữ nguyên như trước (không quy đổi gì) — không có rủi ro breaking change cho luồng đang chạy.

## 5. Phase 3 — Chứng từ theo lô & hồ sơ cá nhân (đã triển khai, có điều chỉnh sau rà soát)

**Phát hiện quan trọng (07/08/2026):** "chứng từ theo lô hàng" đã có cơ chế THẬT từ trước — UC-100/BR-071, `POST /supplier-batches/{code}/certificates` (`SupplierBatchService.uploadCertificate()`), dùng bảng nối `batch_certificates` (batch_id, document_id) trỏ vào `partner_documents` loại `PRODUCT_ORIGIN`. Bản đầu của migration V50 có thêm cột `partner_documents.supplier_batch_id` — **đã bỏ** để tránh 2 cơ chế song song cho cùng một việc (vi phạm chính nguyên tắc "additive, không viết lại hạ tầng có sẵn" mà quy trình của nhóm đề ra, AP-AI-016 tương tự). Kết luận: nhu cầu ban đầu ("chứng từ nguồn gốc phải theo từng lô, không phải văn bản dùng chung mãi mãi") **đã được đáp ứng bởi tính năng có sẵn** — không cần thêm gì ở lớp dữ liệu cho phần này.

Phần thực sự MỚI (chưa có cơ chế nào trước đó): **hồ sơ CÁ NHÂN nhân viên giao hàng** — `partner_documents.delivery_person_id` (nullable), 2 `DocumentType` mới (`HEALTH_CERTIFICATE`, `FOOD_SAFETY_KNOWLEDGE_CERT`), tái dùng nguyên `DocumentUploadSupport`/`ComplianceDocumentService`/`DocumentStatusCalculator` (cùng ngưỡng cảnh báo EXPIRING/URGENT như hồ sơ công ty).

**Chưa làm (cân nhắc rủi ro breaking change):** không bắt buộc (block) tạo `SupplierBatch` phải kèm chứng từ nguồn gốc — dữ liệu/lô hiện có (kể cả seed) đều chưa có chứng từ, ép buộc ngay sẽ chặn toàn bộ luồng đang chạy. Nếu muốn siết, nên làm theo hướng cảnh báo mềm trước (đếm `batch_certificates` rỗng → badge cảnh báo trên `SupplierBatchResponse`), rồi mới cân nhắc chặn sau khi có dữ liệu thật.

## 6. Phase 4 — Đơn vị đo lường xuyên suốt hệ thống (đã triển khai đầy đủ Phase A+B — 08/08/2026)

Thiết kế đầy đủ (mô hình dữ liệu + kịch bản) lưu ở `C:\Users\namng\.claude\plans\snazzy-churning-sonnet.md` tại thời điểm triển khai — nên chép lại vào đây nếu đề xuất được duyệt chính thức, vì file plan cá nhân không phải nơi lưu trữ lâu dài.

**Nguyên tắc cốt lõi (chốt sau góp ý của Nam):** quy đổi "1 bó = bao nhiêu gam" hay "1 thùng = bao nhiêu hộp" **không phải thuộc tính của nguyên liệu** — mỗi NHÀ CUNG CẤP có thể đóng gói khác nhau cho cùng một nguyên liệu. Chỉ `catalog_ingredients.unit` (baseUnit) được khóa cứng dùng xuyên suốt nội bộ (thực đơn/tồn kho/báo cáo); mọi quy đổi theo cách đóng gói của NCC sống ở `supplier_products`/`partnership_catalog_items` (`conversionToBase`).

**Đã triển khai (Phase A — rủi ro thấp, chỉ thêm validate, không đổi field cũ):**
- `measure_units` (migration V51) — bảng tra cứu đơn vị hợp lệ + hệ số quy đổi CHÍNH XÁC (kg↔g, lít↔ml). `com.sfms.common.unit.MeasureUnit(Repository|Service)` — dùng chung mọi module.
- `IngredientServiceImpl` validate `unit` phải nằm trong `measure_units` khi tạo/sửa nguyên liệu.
- `InventoryTransactionService.adjust()` + `InventoryService.createIssueSlip()`: chặn nếu `unit` gửi lên lệch với `batch.getUnit()` (trước đây ghi thẳng vào sổ cái không kiểm gì).
- `SupplierBatchService.create()`: chặn nếu `unit` lô hàng lệch với `SupplierProduct.unit` đã báo giá.
- `PurchaseRequestDtos.QuoteItem` thêm field `unit` bắt buộc; `PurchaseRequestService.quote()` validate khớp với `PurchaseItem.unit` trước khi nhân giá — trước đây nhân mù không kiểm đơn vị.

**Đã triển khai (Phase B — đổi field, cần UI mới, migration V52):**
- `SupplierProduct`, `PartnershipCatalogItem`, `CatalogChangeRequest` thêm `conversionToBase` (mặc định 1) + `conversionNote`. Bắt buộc khai tường minh (không cho mặc định ngầm =1) khi `unit`/`requestedUnit` khác `baseUnit` của nguyên liệu — validate ở `SupplierProductService`/`CatalogChangeRequestServiceImpl`.
- `ReferenceResolver.ingredientUnit()` (mới) + `CatalogPackaging` record mở rộng thêm `conversionToBase`/`purchaseUnitFamily` — cho mf06 tra cứu thuần sang mf04 mà không import entity chéo module.
- Frontend: form đề xuất đơn lẻ + hàng loạt (`/supplier/partnerships/[id]/catalog`) và màn duyệt (`school-admin/partnerships`) đã cập nhật hiển thị/nhập `conversionToBase`.

**Đã triển khai (hoàn thiện nốt — 08/2026, migration V53):**
- `DishIngredient.quantity`/`MenuDtos.MenuIngredient.quantity` đổi từ `String` sang `BigDecimal` (`NUMERIC(12,3)`, nullable — nguyên liệu ngoài danh mục/gia vị có thể chưa định lượng). Rà toàn bộ chỗ dùng chung: `DishServiceImpl`, `DishMapper`, `MenuMapper` (không cần sửa, đã type-match trực tiếp qua tham số/getter). Frontend: `schema.d.ts` đổi kiểu tương ứng; các màn đọc trực tiếp (`service-caterers/dishes/page.tsx`, `menu-form.tsx` — 3 chỗ, `special-meal-dialog.tsx`) bọc `String(quantity ?? "")` phòng thủ khi đưa vào input dạng text; `menus.blocks_data` không có cột JSONB riêng — được tính động qua `MenuMapper.toBlocksData()` từ đúng bảng `dish_ingredients` (single source of truth), nên không phát sinh migrate dữ liệu JSONB cũ.
- Luồng tự động "nhu cầu công thức (baseUnit) → số lượng đặt hàng (purchaseUnit của đúng NCC, qua `conversionToBase`)" — nối ở **frontend**, `service-caterers/orders/new/page.tsx`: thêm `OrderRow.baseQuantity` lưu nhu cầu gốc theo baseUnit song song với `quantity` (số lượng đặt thực tế theo purchaseUnit); hàm `toSupplierOrderQuantity(baseQuantity, conversionToBase, purchaseUnit)` áp đúng thứ tự baseUnit→purchaseUnit (chia `conversionToBase`, mặc định 1 nếu thiếu) → làm tròn (đơn vị liên tục kg/g/lít/ml giữ 3 chữ số thập phân, đơn vị đếm `Math.ceil` lên nguyên đơn vị) → sau đó `packagingSize` (đã có từ Phase 2) làm tròn kiện. Áp dụng cả khi prefill từ thiếu hụt tồn kho (`shortageBySupplier`) lẫn khi người dùng đổi NCC giữa chừng (tính lại từ `baseQuantity` đã lưu, không cộng dồn sai số qua nhiều lần đổi). Backend `PurchaseRequestService.create()` giữ nguyên không tự chia `conversionToBase` — vì frontend đã gửi lên `quantity`+`unit` là số cuối cùng theo đúng `purchaseUnit` của sản phẩm NCC đã chọn; chia thêm lần nữa ở backend sẽ tính trùng (double-convert).
