# Manual Web E2E Test — MF06 Ingredient & Inventory Management

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: Ingredient Catalog (System Admin, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Ingredient Catalog Management (Manual Web) |
| Test requirement | Create/import/deactivate the system-wide ingredient catalog at `/system-admin/ingredients` — suppliers can only list products against ingredients in this catalog. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-001 | Add a new ingredient with a valid unit and storage zone. | 1. Log in as System Admin, go to `/system-admin/ingredients`.<br>2. Click "Thêm nguyên liệu".<br>3. Enter Name, pick Unit from the searchable dropdown, pick Storage zone (Khô/Lạnh/Đông lạnh), set Hao hụt sơ chế %, click "Thêm vào danh mục". | New ingredient appears in the list with a system-generated code (if left blank), correct unit/zone badge, and refuse-factor percentage shown. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-002 | Reject a duplicate ingredient name. | 1. On "Thêm nguyên liệu", enter a Name that already exists in the catalog (any case), fill the rest, submit. | Blocked with an error toast — name already exists in the system; no duplicate row created. | At least one ingredient already exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-003 | Deactivate (soft-delete) an ingredient. | 1. On the ingredient list, click the trash icon on a row.<br>2. Confirm the browser confirm dialog. | Ingredient disappears from the active list; existing batches/menus referencing it keep their link (soft-delete, not a hard delete). | At least one ingredient exists and is not referenced critically for this test. | Pending | | | Pending | | | Pending | | | Soft-delete — no data loss for historical batches/menus |

---

## Feature: Supplier Product Catalog (Supplier, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Supplier Product Catalog (Manual Web) |
| Test requirement | Supplier declares which system ingredients it sells and at what price/unit/packaging at `/supplier/products` (BR-080). |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-004 | Declare a new product selling at the ingredient's standard unit. | 1. Log in as Supplier, go to `/supplier/products`.<br>2. Click "Thêm mặt hàng".<br>3. Pick an ingredient not yet declared, leave Unit = catalog unit, enter Đơn giá and Hiệu lực từ, save. | Product saved with conversion factor 1:1 (locked, since sell unit = base unit); appears in the product list with correct price/unit. | Logged in as Supplier; at least one ingredient in the catalog not yet declared by this supplier. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-005 | Declare a product with a sell unit different from the ingredient's base unit. | 1. On "Thêm mặt hàng", pick an ingredient, change Đơn vị bán to a different unit than the catalog unit.<br>2. Leave "Hệ số quy đổi" empty, click save. | Blocked — conversion factor (1 sell-unit = ? base-unit) is mandatory when sell unit differs from base unit. Filling in a valid factor (e.g. 25) then saves successfully. | Logged in as Supplier. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-006 | Reject declaring the same ingredient a second time. | 1. Click "Thêm mặt hàng", pick an ingredient already declared by this supplier, fill the rest, submit. | Blocked with an error naming the existing product code — must edit that row ("Sửa giá") instead of creating a duplicate. | Supplier already has at least one declared product. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Purchase Request Creation (Service Caterer, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Purchase Request Creation (Manual Web) |
| Test requirement | Caterer creates purchase requests at `/service-caterers/orders/new`, either manually or auto-filled from published menu shortage (FEFO), with a ≥24h delivery-date lead time (BR-071). |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 5 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-007 | Reject a requested delivery date earlier than tomorrow. | 1. Log in as Service Caterer, go to `/service-caterers/orders/new`.<br>2. Add one ingredient row with a supplier, set "Ngày giao yêu cầu" to today, click "Gửi đơn tới NCC". | Blocked — delivery date must be from tomorrow onward (BR-071, supplier needs ≥24h to confirm). | Logged in as Service Caterer. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-008 | Auto-fill order rows from a published menu's ingredient shortage. | 1. On `/service-caterers/orders/new`, in "1 · Chọn buổi cần đặt nguyên liệu" click one or more upcoming (date × meal) chips that have a published menu.<br>2. Wait for "Đang tính nhu cầu…" to finish. | Rows are pre-filled with the shortfall quantity per ingredient (demand minus available FEFO stock), each row auto-assigned the cheapest APPROVED supplier, with a note showing which dishes it is for. | At least one PUBLISHED menu exists for a future date with ingredients not fully covered by current stock. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-009 | Editing a pre-filled quantity requires a reason. | 1. From a row auto-filled by step 008, change "Số lượng" to a different value than "Đề xuất".<br>2. Try to submit without filling "Lý do điều chỉnh". | Blocked — the note field becomes "Lý do điều chỉnh *" and submission is refused until filled; filling it in allows submission. | At least one auto-filled row exists (from MF06-WEB-008). | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-010 | Reject two rows with the same ingredient and same supplier. | 1. Add two rows, pick the same ingredient and same supplier on both (leave quantities different).<br>2. Submit. | Blocked with an error naming the duplicated ingredient — must merge into one row and sum quantities instead. | Logged in as Service Caterer; at least one ingredient has a supplier. | Pending | | | Pending | | | Pending | | | Same ingredient with a DIFFERENT supplier on two rows is allowed |
| MF06-WEB-011 | Rows across different suppliers are split into separate purchase requests on submit. | 1. Add two rows with ingredients sold by two different suppliers.<br>2. Set a valid delivery date, submit "Gửi đơn tới NCC". | An info banner beforehand states "Sẽ tách thành 2 đơn". After submit, a success toast confirms 2 requests were sent; both appear in `/service-caterers/orders` with status PENDING, each linked to one supplier only. | At least two different APPROVED suppliers each sell a different ingredient. | Pending | | | Pending | | | Pending | | | Requests are sent sequentially, not in parallel |

---

## Feature: Purchase Request Management (Service Caterer, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Purchase Request Management (Manual Web) |
| Test requirement | View request list/detail and cancel a PENDING request at `/service-caterers/orders` and `/service-caterers/orders/[id]`. |
| Number of TCs | 2 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 2 | 0 |
| Round 2 | 0 | 0 | 2 | 0 |
| Round 3 | 0 | 0 | 2 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-012 | View the request list and open a detail page. | 1. Go to `/service-caterers/orders`.<br>2. Click "Xem chi tiết" on any row. | Detail page shows supplier, dates, item lines with contract unit price/line total, and current status badge (PENDING/APPROVED/CANCELLED/REJECTED/COMPLETED). | At least one purchase request exists for this caterer. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-013 | Cancel a PENDING request with a mandatory reason. | 1. Open a PENDING request's detail page.<br>2. Click "Huỷ đơn", leave the reason empty, click "Huỷ đơn" in the dialog.<br>3. Fill in a reason, submit again. | Step 2 is blocked — reason is required. Step 3 succeeds — status becomes CANCELLED, the cancel reason is shown on the detail page, and the "Huỷ đơn" button disappears. | A PENDING request exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Purchase Request Acceptance & Batch Assignment (Supplier, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Purchase Request Acceptance & Batch Assignment (Manual Web) |
| Test requirement | Supplier accepts/rejects a PENDING request and assigns a registered batch to every ingredient line at `/supplier/purchase-requests/[id]` (BR-071). |
| Number of TCs | 4 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 4 | 0 |
| Round 2 | 0 | 0 | 4 | 0 |
| Round 3 | 0 | 0 | 4 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-014 | Accept a PENDING request and assign one registered batch per ingredient. | 1. Log in as Supplier, open a PENDING request at `/supplier/purchase-requests/[id]`.<br>2. Click "Chấp nhận đơn".<br>3. For every ingredient row, pick a batch from "— Chọn lô đã đăng ký —" and click "Chấp nhận đơn". | Request status becomes APPROVED, each item row shows its assigned batch code (green badge) on the detail page. Blocking without assigning every line — submit shows "BR-071: chưa gán lô hàng cho..." naming the missing ingredient(s). | Request is PENDING; at least one ACTIVE batch with remaining quantity exists for each requested ingredient. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-015 | The batch dropdown for a line only offers batches of the same ingredient. | 1. Open "Chấp nhận đơn & gán lô hàng" for a request with ≥2 different ingredient lines.<br>2. Inspect the dropdown options for each line. | Each ingredient's dropdown lists only ACTIVE batches with remaining quantity of that exact ingredient — no cross-ingredient batches offered. | At least two ingredients each with their own registered batch exist. | Pending | | | Pending | | | Pending | | | Regression guard for PR-2026-0101 (wrong-ingredient batch assignment) |
| MF06-WEB-016 | Reject a PENDING request with a mandatory reason. | 1. Open a PENDING request, click "Từ chối".<br>2. Leave reason empty, click "Từ chối đơn" — blocked.<br>3. Fill a reason, submit again. | Step 2 blocked with "Lý do từ chối không được để trống." Step 3 succeeds — request status becomes CANCELLED/REJECTED and the caterer's detail page shows the reason. | A PENDING request exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-017 | Edit the batch assignment of an APPROVED request that has no delivery yet. | 1. Open an APPROVED request with no delivery created yet.<br>2. Click "Sửa lô hàng đã gán", change one line's batch, save. | Dialog allows changing assignments and saves successfully (Lưu lô hàng); once a delivery exists for this request, the edit button/action is no longer usable for changing assignments. | Request is APPROVED and has zero linked deliveries. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Supplier Batch Registration (Supplier, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Supplier Batch Registration (Manual Web) |
| Test requirement | Register ingredient batches with lot number/dates/quantity and attach certificates at `/supplier/batches` and `/supplier/batches/[id]` (BR-071/BR-046). |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 5 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-018 | Register a new batch with valid manufacturing/packaging/expiration dates. | 1. Log in as Supplier, go to `/supplier/batches`.<br>2. Click "Tạo lô hàng mới".<br>3. Pick a declared product, enter Số lô, dates (NSX < đóng gói < HSD), quantity, submit. | Batch created, appears in the list with status ACTIVE and "còn lại" equal to the full quantity. | Supplier has at least one declared product (`/supplier/products`). | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-019 | Reject an expiration date not after the packaging/manufacturing dates. | 1. On "Nộp thông tin lô nguyên liệu", set Hạn sử dụng equal to or earlier than Ngày đóng gói.<br>2. Submit. | Blocked — "Hạn sử dụng phải sau ngày sản xuất và ngày đóng gói (UC-081)." No batch created. | Supplier has at least one declared product. | Pending | | | Pending | | | Pending | | | Same rule blocks Ngày đóng gói ≤ Ngày sản xuất |
| MF06-WEB-020 | Upload a valid certificate file to a batch. | 1. Open a batch detail page, click "Tải chứng chỉ lô (UC-100)".<br>2. Choose a PDF/JPG/PNG file ≤10MB, click "Lưu chứng chỉ". | Upload succeeds, the certificate code appears under "Chứng chỉ liên kết"; clicking it previews the file. | At least one batch exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-021 | Reject an oversized or wrong-format certificate file. | 1. On the certificate upload dialog, pick a file >10MB, or a file with an unsupported extension.<br>2. Submit. | Blocked with MSG-008 (too large) or MSG-009 (unsupported format) — no certificate attached. | At least one batch exists; a test file >10MB or with an unsupported extension is available. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-022 | A single batch is shared across two different purchase requests until its remaining quantity is exhausted. | 1. Register one batch with a large quantity.<br>2. Assign that same batch to an ingredient line on two separate PENDING requests (accept both, BR-071).<br>3. Open the batch detail page and check "Còn lại" after both requests are accepted. | Both requests accept successfully without a "batch already used" block; the batch's remaining quantity is NOT reduced merely by assignment (only decreases after actual receiving) — it stays selectable for further requests as long as remaining quantity is > 0. | One batch has enough quantity to cover two separate small requests for the same ingredient. | Pending | | | Pending | | | Pending | | | Confirms BR-071 batch-sharing behavior (fixed after PR-2026-0107 regression) |

---

## Feature: Delivery Scheduling & Dispatch (Supplier, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Delivery Scheduling & Dispatch (Manual Web) |
| Test requirement | Create/dispatch/track deliveries at `/supplier/deliveries` and `/supplier/deliveries/[id]` — driver must have registered biometrics (BR-050) and delivery must be submitted ≥24h ahead (BR-071). |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 6 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-023 | Create a delivery for an APPROVED request with a valid driver, plate, and schedule. | 1. Log in as Supplier, go to `/supplier/deliveries`, click "Tạo chuyến giao".<br>2. Pick the APPROVED request (batches already assigned), pick a date/time ≥24h from now, enter Biển số xe, pick a driver with "(đã có sinh trắc học)".<br>3. Click "Tạo chuyến giao". | Delivery created with status SCHEDULED, appears in the list linked to the correct request and showing the assigned batch count; request no longer appears in the "chưa có chuyến giao" picker. | An APPROVED request exists with no delivery yet, and at least one driver has REGISTERED biometric status. | Pending | | | Pending | | | Pending | | | This screen is the only place a delivery is created — no longer created from the purchase-request screen |
| MF06-WEB-024 | Reject scheduling a delivery less than 24 hours from now. | 1. On "Tạo chuyến giao", pick an APPROVED request, set the date/time to less than 24h from the current time.<br>2. Submit. | Blocked — "BR-071: thông tin giao hàng phải nộp tối thiểu 24 giờ trước thời điểm giao dự kiến," with the earliest allowed time shown. | An APPROVED request with assigned batches exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-025 | Block assigning a driver without registered biometrics. | 1. On "Tạo chuyến giao", fill all required fields, but pick a driver whose option shows "(CHƯA có sinh trắc học)".<br>2. Submit. | Blocked — "BR-050: nhân viên ... chưa đăng ký sinh trắc học khuôn mặt — không thể phân công giao hàng." | At least one delivery-person exists without REGISTERED biometric status. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-026 | Advance a delivery through its status lifecycle, including the delay warning. | 1. Open a SCHEDULED delivery's detail page, click "Cập nhật trạng thái (UC-092)".<br>2. Set status to "Đã xuất kho (Dispatched)", save.<br>3. Repeat, moving to "Đang giao", then "Trễ lịch (Delayed)", then "Đã giao (Delivered)". | Each update shows "MSG-029: cập nhật trạng thái thành công". Selecting "Delayed" additionally triggers a warning toast "MSG-065" (caterer is notified, BR-135). Once DELIVERED, the "Cập nhật trạng thái" button disappears. | A SCHEDULED delivery exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-027 | Cancel a SCHEDULED delivery and confirm its batches return to Active. | 1. Open a SCHEDULED delivery's detail page, click "Hủy chuyến".<br>2. Confirm. | Delivery status becomes CANCELLED; every linked batch's status returns to ACTIVE (visible on `/supplier/batches`). "Hủy chuyến" is only available while status is SCHEDULED. | A SCHEDULED delivery with ≥1 linked batch exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-028 | A request without batch assignment cannot be turned into a delivery. | 1. On "Tạo chuyến giao", pick a request that is APPROVED but was somehow never assigned batches (or check the "Lô nguyên liệu liên kết" panel shows "Đơn này chưa được gán lô hàng").<br>2. Submit. | Blocked — "Đơn chưa được gán lô hàng ở bước Chấp nhận đơn (BR-071) — quay lại Đơn mua hàng để gán lô trước." | An APPROVED request exists with no batch assignment on any line (edge case). | Pending | | | Pending | | | Pending | | | Edge case — normally batch assignment happens during acceptance |

---

## Feature: Ingredient Receiving (Service Caterer, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Ingredient Receiving (Manual Web) |
| Test requirement | Open a receiving session from a DELIVERED delivery, verify driver/GPS/photos, accept or reject each batch line, and cap received quantity at the batch's remaining quantity, at `/service-caterers/receiving` and `/service-caterers/receiving/new` (BR-070/071/072/073/074). |
| Number of TCs | 7 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 7 | 0 |
| Round 2 | 0 | 0 | 7 | 0 |
| Round 3 | 0 | 0 | 7 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-029 | Only DELIVERED deliveries can be opened as a receiving session. | 1. Log in as Service Caterer, go to `/service-caterers/receiving`, tab "Sắp nhận hàng".<br>2. For a delivery whose status is not DELIVERED (e.g. SCHEDULED/IN_TRANSIT), confirm the row shows "Chờ hàng tới" with no action button.<br>3. For a DELIVERED delivery, click "Mở phiên nhận". | Non-DELIVERED rows have no way to open a session. DELIVERED row navigates to `/service-caterers/receiving/new?delivery=...` with batch lines pre-filled from the request's assigned batches. | At least one delivery each in a non-DELIVERED and a DELIVERED state exists for this school. | Pending | | | Pending | | | Pending | | | Receiving sessions can never be entered manually — only opened from a delivery |
| MF06-WEB-030 | Complete a full accept-only receiving session (checklist + GPS + photos). | 1. On the receiving workspace, verify the driver via "Xác thực khuôn mặt tài xế".<br>2. For each batch line, enter a valid received quantity and storage zone.<br>3. Attach at least 1 general evidence photo, click "Ghi vị trí hiện tại", tick all 4 checklist items, click "Hoàn tất phiên nhận hàng". | Session completes successfully, toast confirms items moved to inventory; redirected to `/service-caterers/receiving`; the new session appears under "Lịch sử & báo cáo" with status RECEIVED. | A DELIVERED delivery with ≥1 assigned batch is open in the workspace. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-031 | Reject received quantity exceeding the batch's remaining quantity. | 1. On a batch line, enter "Số lượng thực nhận" greater than the batch's displayed "còn lại" quantity.<br>2. Complete the rest of the form and submit. | Blocked — "Lô ... chỉ còn X ... — không thể nhận Y ... (vượt số lượng còn lại, BR-071)." | A batch line whose remaining quantity is a known, finite number (e.g. partially consumed by another school/delivery). | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-032 | Reject a batch line with a mandatory reason and photo evidence. | 1. On a batch line, click "Từ chối lô".<br>2. Leave "Lý do từ chối" empty and submit — blocked.<br>3. Fill the reason but do not take a photo, submit — blocked.<br>4. Click "Chụp ảnh bằng chứng", capture a photo, submit again. | Step 2 and 3 are both blocked with their respective messages (reason required; photo required, camera capture only — no file upload accepted). Step 4 succeeds; the session completes with that line marked "Trả về NCC". | A DELIVERED delivery with ≥1 batch line is open. | Pending | | | Pending | | | Pending | | | Camera-only capture — no upload input for this evidence type |
| MF06-WEB-033 | An already-expired batch is pre-flagged for rejection and cannot be force-accepted. | 1. Open a receiving session containing a batch whose expiration date is in the past.<br>2. Observe the line's default state; try to un-reject it and enter a received quantity. | The line defaults to rejected with reason "Lô đã hết hạn ..." and an "Hết hạn" badge. If un-rejected and a quantity is entered, submit is blocked: "Lô ... đã hết hạn ... phải từ chối lô này (BR-071)." | A supplier batch assigned to this delivery has an expiration date before today. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-034 | Block receiving when the supplier is no longer Approved. | 1. Open a receiving session for a delivery whose supplier's partnership status is not APPROVED (e.g. SUSPENDED).<br>2. Observe the supplier badge and try to submit. | A critical alert "NCC bị chặn (BR-070 — MSG-015)" is shown; the submit button stays disabled and the session cannot be completed. | A delivery exists whose supplier is not currently APPROVED. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-035 | View a completed session's detail and export the receiving history report. | 1. Go to `/service-caterers/receiving`, tab "Lịch sử & báo cáo".<br>2. Click "Xem chi tiết" on a completed session.<br>3. Set a From/To date range, click "Xuất báo cáo". | Detail dialog shows supplier, timestamp, evidence counts, GPS badge, and per-line batch results (accepted/rejected with reasons). Export downloads an `.xlsx` file filtered to the chosen date range. | At least one completed (RECEIVED or RECEIVED_PARTIAL) session exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Inventory & Stock Management (Service Caterer, Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Inventory & Stock Management (Manual Web) |
| Test requirement | View inventory batches/transaction ledger, adjust stock manually (BR-204), and issue stock FEFO for the day's meal (BR-075) at `/service-caterers/inventory`, `/service-caterers/inventory-transactions`, `/service-caterers/inventory/issue`. |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 5 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| MF06-WEB-036 | View the inventory batch list with storage zone and FEFO expiry columns. | 1. Log in as Service Caterer, go to `/service-caterers/inventory`, tab "Danh sách lô". | Table lists each in-stock batch with remaining quantity, storage-zone badge (Dry/Chilled/Frozen), received date, expiry date, and status; clicking "Xem chi tiết" opens the full batch info. | At least one batch has been received into inventory. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-037 | A quarantined batch is blocked from manual adjustment and shows its incident link. | 1. With a batch under medical quarantine (BR-149), open `/service-caterers/inventory` — check the red banner at the top lists it.<br>2. Click "Điều chỉnh (BR-204)" and check the batch dropdown does not offer this batch.<br>3. Click "Xem chi tiết" on the batch. | Quarantined batch is excluded from the adjustment dropdown. Detail dialog shows a "Lô đang bị cách ly (BR-149)" panel with reason, who locked it, and a link to the related incident if any. | At least one inventory batch is under an active medical quarantine. | Pending | | | Pending | | | Pending | | | Only Medical Staff can lift the quarantine — kitchen role cannot self-unlock |
| MF06-WEB-038 | Manual stock adjustment requires a reason code and manager signature. | 1. Click "Điều chỉnh (BR-204)".<br>2. Pick a batch, enter a delta quantity, leave Mã lý do and Quản lý xác nhận empty, submit — blocked.<br>3. Pick a reason code and enter a manager name, submit again. | Step 2 blocked twice (reason code required, then signature required). Step 3 succeeds — a new ADJUSTMENT entry appears in "Lịch sử giao dịch kho" showing the reason code and signature. | At least one adjustable (non-quarantined) batch exists. | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-039 | The inventory transaction ledger is a read-only append log. | 1. Go to `/service-caterers/inventory-transactions`.<br>2. Confirm every row (RECEIPT/ISSUE/ADJUSTMENT) has no edit/delete action, and ADJUSTMENT rows show the manager signature while RECEIPT/ISSUE rows show "— không cần". | No edit/delete controls exist anywhere on this page; ADJUSTMENT rows without a signature show a "Thiếu — bị chặn" badge (cannot happen via the UI flow, sanity check only). | At least one transaction of each type exists (RECEIPT from receiving, ISSUE from FEFO issue, ADJUSTMENT from MF06-WEB-038). | Pending | | | Pending | | | Pending | | | |
| MF06-WEB-040 | Issue stock FEFO for today's meal and follow the shortage link to a new order. | 1. Go to `/service-caterers/inventory/issue`.<br>2. Pick today's meal (only today is selectable).<br>3. Review the allocation table — if a shortage banner appears, click "Tạo đơn đặt hàng bổ sung"; otherwise click "Xác nhận xuất kho". | Allocation table shows the nearest-expiry batch consumed first per ingredient (FEFO). If short, the shortage banner links to `/service-caterers/orders/new?date=...&meal=...` with rows pre-filled. If sufficient, submitting creates ISSUE transactions and redirects to `/service-caterers/inventory-transactions`. | A PUBLISHED menu exists for today with at least one meal slot and pending student servings. | Pending | | | Pending | | | Pending | | | Issuing is locked to today only — no past/future date picker |
