# 04 — Thiết kế: Suất ăn thay thế theo dị ứng, xuyên MF05→MF06→MF07→MF09

**Ngày lập:** 2026-08-05 · **Trạng thái:** ✅ Phase 1 XONG, ✅ Phase 2 mục (1)+(2)+(3)+(4) XONG
(05/08/2026, đã được chủ MF06/MF07 duyệt) — chỉ còn mục (5) (phụ huynh xem trước thực đơn) chưa
làm, xem §4 bảng trạng thái + §5/§6 nhật ký triển khai
**Người đề xuất:** Nam (qua trao đổi khi nối FE MF09) · **Cần chủ MF05/MF06/MF07/MF09 duyệt
trước khi merge Phase 2** (đúng quy trình `02_Module_Completion_Workflow.md` §2.1 mục B — thay
đổi xuyên module).

> **Tóm tắt 1 câu:** hệ thống hiện đã có thực đơn theo nhóm chế độ ăn (MF05) và món thay thế
> theo dị ứng từng học sinh tại thời điểm phát suất (MF09), nhưng CHƯA có cách "xem trước" số
> liệu đó để phục vụ bếp chuẩn bị đúng số suất từng món — đây là khoảng trống cần vá.

---

## 1. Hiện trạng đã xác nhận qua đọc code (không suy đoán)

- **Thực đơn theo nhóm ăn đã có** (`Menu.audience`/`dietGroupId`, ADR-M05-01): `audience="ALL"`
  = thực đơn chung; `audience=<mã DietGroup>` = thực đơn riêng (chay, tiểu đường…).
- **`Mf05Events.MenuPublished` CHỈ phát cho `audience="ALL"`** (Javadoc `Mf05Events.java` ghi rõ:
  "Chỉ phát cho thực đơn `audience = "ALL"` — thực đơn riêng theo nhóm chế độ ăn không có
  lớp/khối cố định để MF09 tự sinh phiên phát suất theo lớp"). Nghĩa là **mọi học sinh trong
  khối, kể cả có chế độ ăn riêng, đều được gán CÙNG một `menuId` (thực đơn ALL)** khi
  `DistributionPlanHandler.onMenuPublished()` sinh phiên `PENDING` — đây là THIẾT KẾ CHỦ Ý,
  KHÔNG phải bug (đã kiểm tra kỹ, rút lại nghi vấn "race condition" nêu trước đó trong hội
  thoại — Javadoc xác nhận đây là hành vi mong muốn).
- **Món thay thế theo dị ứng CHỈ được tính tại `StudentDistributionServiceImpl.doServe()`**
  (dòng ~157-187): so `verifiedAllergensOfStudent(studentId)` với dị ứng của từng món trong
  thực đơn ALL đã gán (`menuQueryPort.dishesOfMenu`/`allergensOfMenu`), món trùng dị ứng thì
  thay bằng chính món đó nhưng đánh dấu `isSubstitute=true` + lý do — ghi thành `ServingDish`
  (ADR-M09-1: **sự kiện đóng băng**, không tính lại sau, không được ghi đè).
- **Phía phụ huynh đã hiện đúng món con mình ăn** (không phải món chung cả khối) — do
  `MealHistoryServiceImpl`/`GET /students/{code}/meal-history` đọc thẳng `ServingDish` theo
  `distributionId` của CHÍNH học sinh đó. Đã nối FE 05/08/2026 (`03_FE_Full_Wiring_Master_Plan.md`
  mục MF09) — **không cần sửa gì thêm ở đây**.
- **CHƯA có** bất kỳ chỗ nào trong `mf06` (kho/sơ chế) hay `mf05` đọc `ServingDish`/dị ứng để
  đếm số suất từng món cần nấu — xác nhận qua agent tìm kiếm toàn bộ `com.sfms.mf06`, 0 kết quả
  khớp `portions|serving_dish|ServingDish|StudentDistribution`.
- **CHƯA có** khái niệm "giao suất bếp→lớp theo món" (khác hẳn giao NCC→bếp đã có ở MF06) —
  không có bảng/entity nào biểu diễn việc này.
- **CHƯA có** endpoint "xem trước" (preview) suất ăn dự kiến của một học sinh/lớp TRƯỚC khi
  giáo viên bấm phát — logic thay món chỉ chạy (và LƯU) đúng lúc `serve()`/`confirmBatch()`.

## 2. Vì sao KHÔNG làm trọn vẹn 1 lần (Phase 2 ghi tồn đọng, không code trong đợt này)

Làm trọn ý tưởng ban đầu (đếm suất bếp theo món có tính thay thế → tự điền kiểm thực bước 1/2 →
màn giao nhận bếp→lớp theo món có check-off) đòi hỏi:
- Thêm khái niệm domain MỚI ở MF06 (giao suất bếp→lớp) — chưa tồn tại, cần bàn thiết kế bảng +
  luồng với chủ MF06 (khác hẳn `deliveries` hiện có, vốn là NCC→bếp).
- Sửa 3 bước kiểm thực MF07 để hiển thị thêm dòng món thay thế — đụng vào form đã ổn định.
- Đây là các module có CHỦ RIÊNG theo `00_Team_Work_Assignment_Plan.md` — sửa lõi nghiệp vụ của
  module người khác cần thống nhất trước khi merge (`02_Module_Completion_Workflow.md` §2.1B),
  không tự quyết một mình khi đang ở giữa việc nối FE MF09.
- Rủi ro cao hơn lợi ích nếu làm vội trong 1 phiên — đây là tính năng mới, không phải sửa lỗi.

**→ Phase 1 dưới đây là phần có thể làm NGAY, rủi ro thấp, không đổi schema, không đụng module
người khác, và giải quyết đúng nhu cầu cấp thiết nhất: giáo viên (và sau này là bếp) cần "xem
trước" chính xác suất ăn dự kiến — kể cả món thay thế — mà KHÔNG đoán sai so với lúc `serve()`
thật sự chạy.**

## 3. Phase 1 — Preview suất ăn dự kiến (KHÔNG đổi schema, dùng lại đúng logic `doServe()`)

### Nguyên tắc thiết kế
Tách phần **tính toán** (thuần, không side-effect) ra khỏi phần **ghi** trong `doServe()`, rồi
expose phần tính toán qua 1 endpoint GET mới. Vì đây là hàm THUẦN dùng lại y nguyên logic đã có
(không viết lại luật thay thế ở FE hay ở một chỗ khác trong BE), preview KHÔNG BAO GIỜ lệch với
kết quả `serve()` thật — đúng lo ngại đã nêu khi thảo luận với người dùng.

### Backend — `mf09`
- `StudentDistributionServiceImpl`: extract khối tính `substitute`/`ServingDish` (dòng ~161-183)
  thành method thuần `private List<PlannedDishRow> planDishes(StudentDistribution dist)` — dùng
  lại ở CẢ `doServe()` (ghi) LẪN method preview mới (không ghi).
- Method mới `planForClass(LocalDate date, String className, MealType mealType, AuthUser actor)`:
  lấy mọi `StudentDistribution` PENDING của (date, class, mealType) trong lớp actor (Teacher) hoặc
  toàn trường (School Admin/Caterers), chạy `planDishes()` cho từng em (KHÔNG lưu), rồi:
  - `perStudent`: `[{studentId, dishId, dishName, isSubstitute, substitutedForDishName, reason}]`
  - `byDish`: gộp theo `dishId` → `{dishId, dishName, count, substituteCount}` (đây là con số
    bếp cần, đặt nền cho Phase 2 — MF06 CÓ THỂ gọi lại đúng method này qua
    `ReferenceResolver`/port mới sau này thay vì tự viết lại).
- Endpoint mới: `GET /api/v1/student-distributions/planned-dishes?date=&class=&mealType=`
  (`@PreAuthorize("hasAnyRole('TEACHER','SCHOOL_ADMIN','SERVICE_CATERERS')")`) — response
  `PlannedDishesResponse{perStudent, byDish}`. GET-only, không side-effect, an toàn tuyệt đối.

### Frontend — `teacher/distribution/page.tsx`
- Cột "Suất ăn được phân công (BR-125)" cho học sinh PENDING đọc từ `planned-dishes` (đúng món
  + đúng cờ thay thế của CHÍNH học sinh đó, không phải đoán/không phải thực đơn chung cả lớp).
- Cột "Cảnh báo dị ứng (BR-123)" GIỮ NGUYÊN, đã đúng từ trước (field `allergyWarning` có sẵn
  trên `DistributionResponse`, không cần sửa).
- `POST /confirm` (batch) đổi từ vòng lặp PUT sang gọi đúng 1 lần với `distributionIds` — server
  tự tính `servedDishes` (không cần FE gửi payload món).

## 4. Phase 2 — người dùng đã xác nhận chủ MF06/MF07 đồng ý, triển khai 05/08/2026

| # | Việc | Module | Trạng thái |
|---|---|---|---|
| 1 | Bàn thiết kế bảng "giao suất bếp→lớp theo món" (khác `deliveries` NCC→bếp hiện có) | MF06 | ✅ Xong 05/08 — xem §6 |
| 2 | Dùng `byDish` để tính xuất kho theo món thay vì chỉ theo nguyên liệu thô | MF06 | ✅ Xong 05/08 — xem §5 |
| 3 | Kiểm thực bước 1/2 hiện thêm dòng món thay thế (số suất riêng) | MF07 | ✅ Xong 05/08 — xem §5 |
| 4 | Màn bếp giao suất theo lớp có check-off theo món | MF06 + FE service-caterers | ✅ Xong 05/08 — xem §6 |
| 5 | Phụ huynh xem "dự kiến" thực đơn/món thay thế TRƯỚC ngày ăn (không chỉ sau khi phát) | MF09 FE | ⏳ Chưa làm |

### 4.1. Phát hiện QUAN TRỌNG khi bắt tay làm (3) — bug an toàn thực phẩm ở lõi BR-065

Khi đọc `doServe()`/`planDishes()` (MF09) để lấy dữ liệu món thay thế cho (2)/(3), phát hiện
**`ServingDish` KHÔNG BAO GIỜ thực sự đổi món** — chỉ đánh cờ `isSubstitute=true` rồi vẫn ghi
`dishName`/`dishId` = ĐÚNG món gây dị ứng. `Dish.defaultSubstituteId` (BR-065/CF-6, đã có sẵn
từ V8 + dữ liệu mẫu `ca-basa-sot-ca-chua`→`dau-phu-sot-ca-chua`) CHƯA BAO GIỜ được tra tới.
Đối chiếu `TestSpec-MF09_Meal_Serving_Distribution.md` (ca FX-009) xác nhận đây là **sai lệch so
với thiết kế gốc đã viết**, không phải quyết định thiết kế mới. Đã hỏi & được người dùng xác
nhận: (a) sửa để tra `defaultSubstituteId` thật; (b) món KHÔNG có sẵn thay thế cấu hình thì
**CHẶN phát** (lỗi mới `SRV-026`), không âm thầm phát món gây dị ứng. Đã sửa + verify qua curl
thật (ca chặn) + verify qua SQL trực tiếp (ca thay thế thành công, JOIN đúng dữ liệu mẫu có sẵn)
— xem §5 mục đầu tiên.

**Việc kế tiếp**: mục (1)+(4) — bàn thiết kế bảng giao suất bếp→lớp, domain mới, làm ở đợt sau
trong cùng phiên nếu còn thời gian.

---

## 5. Phase 1 — Nhật ký triển khai (05/08/2026, XONG)

- **Backend (`mf09`)**: tách `doServe()` thành hàm thuần `planDishes(StudentDistribution)` (dùng
  chung cho `doServe()` VÀ preview, không viết 2 bản luật); thêm `plannedDishes()` +
  `GET /student-distributions/planned-dishes?date=&class=&mealType=` (`PlannedDishRow`/
  `PlannedDishSummary`/`PlannedDishesResponse`, 3 DTO mới trong `DistributionDtos.java`).
  `mvn compile` sạch.
- **Frontend**: `teacher/distribution/page.tsx` viết lại — "Bắt đầu phân phát" đổi từ vòng lặp
  PUT sang đúng 1 lời gọi `POST /confirm`; cột "Suất ăn được phân công" đọc `planned-dishes`
  (PENDING) hoặc `servedDishes` (SERVED); nút "Hủy phát" riêng gọi `/revoke`.
  `teacher/distribution/[id]/page.tsx` viết lại — xoá TOÀN BỘ khối tính suất thay thế ở client
  (`personalizeBlock`/`dietGroupIdOf`/`verifiedAllergensOfStudent`/JOIN `menus`+`dishes`, logic
  viết cho mock khi chưa có backend) — dùng `serve()`/`planned-dishes` thật; thêm dialog "Báo
  lỗi phân phát sai" (trước đó chỉ toast giả, nay gọi thật `POST /error-report`).
  `teacher/dashboard/page.tsx` — đổi type, `today` tính ISO thay vì đọc `vnOfDate()` (VN format).
  `service-caterers/meal-service/page.tsx` — BỎ HẲN cột "Suất phân công gần nhất" (PII, D11).
  `usePlainList`/`useSingle` (`hooks.ts`) thêm `opts?: {enabled}` để gọi `planned-dishes` có
  điều kiện. 8 type alias mới trong `types.ts` (`DistributionDto` và 7 cái khác).
- **2 bug backend KHÔNG liên quan MF09, chặn cứng lúc verify, vá cùng đợt** (chi tiết + lý do
  xem comment `real-resources.ts` khối MF09):
  1. `mf04` — app không boot được từ HEAD (2 bean `FaceRecognitionPort` không phân biệt được,
     `InsightFaceServiceAdapter` thiếu bean `RestTemplate`) — `@Primary` cho stub +
     `@Profile("prod")` cho adapter thật.
  2. `ReferenceResolver.specialDietOfStudent()` — native query sai tên cột (`created_at` thay vì
     `requested_at` trên bảng `special_meal_requests`, bảng không extends `BaseEntity`) — LUÔN
     500 cho MỌI lời gọi `GET /student-distributions` (chạy trên từng dòng kết quả).
  3. `DistributionSpec` — filter `date` qua `SpecSupport.applyExactFilters` (ép kiểu chuỗi) gây
     lỗi Postgres `operator does not exist: date = character varying` — tách xử lý riêng bằng
     `LocalDate.parse()`.
- **Verify qua curl thật** (tài khoản `lethihuong.teacher.synthetic@example.test`, lớp 3A):
  `GET /planned-dishes` trả đúng 2 món của HS-3A-005 (Cơm trắng, Canh bí đỏ chay), gộp đúng theo
  món ở `byDish`; `GET /student-distributions?date=&class=&status=` trả đúng sau khi vá 2 bug
  cột/kiểu dữ liệu ở trên; `POST /error-report` chạy đúng, không đổi status. **`POST /confirm`/
  `serve()` CHƯA verify được end-to-end** — dữ liệu seed cố định ở 06–07/07/2026, đồng hồ hệ
  thống thật đang ở 05/08/2026, constraint `ck_dist_served_same_day` (đúng thiết kế) chặn phát
  suất cho ngày quá khứ xa — đã xác nhận transaction rollback sạch (không để lại trạng thái
  hỏng) khi constraint chặn, nhưng chưa có seed "hôm nay" để chạy trọn happy path.
- `tsc --noEmit`/`eslint`/`npm run audit` sạch toàn dự án. Đã thêm `"student-distributions"` vào
  `REAL_RESOURCES` — **MF09 giờ 5/5, TRỌN VẸN**.
- Việc kế tiếp: theo kế hoạch mục 1 của `03_FE_Full_Wiring_Master_Plan.md` — MF10
  (`feedbacks`, `notifications`).

---

## 5. Phase 2 — Nhật ký triển khai (05/08/2026, mục (2)+(3)+bugfix BR-065 XONG)

- **Bug BR-065 (an toàn thực phẩm, §4.1) — vá xong:**
  - `MenuQueryPort.DishView` thêm 3 field `defaultSubstituteId`/`defaultSubstituteCode`/
    `defaultSubstituteName`; `MenuQueryPortImpl.dishesOfMenu()` thêm `LEFT JOIN dishes s ON
    s.id = d.default_substitute_id`.
  - `StudentDistributionServiceImpl.planDishes()` viết lại: còn dị ứng trùng món → có
    `defaultSubstituteId` thì `dishId`/`dishCode`/`dishName` trả về là CỦA MÓN THAY THẾ,
    `substitutedForDishName` giữ tên món GỐC; KHÔNG có thì đánh dấu `blocked=true`.
  - `doServe()` thêm chặn: có bất kỳ `PlannedDish.blocked=true` nào → ném `SRV-026` (422, MSG-142
    mới), KHÔNG lưu gì (an toàn — học sinh dị ứng không bị ghi nhận đã ăn nhầm món).
  - DTO `PlannedDishRow`/`PlannedDishSummary` thêm field `blocked`/`blockedCount` để FE hiện rõ
    ca cần xử lý tay TRƯỚC khi phát (không chỉ phát hiện lúc `serve()` báo lỗi).
  - **Verify qua curl thật + SQL trực tiếp**: dựng tạm 1 dị ứng "Tôm" đã xác minh cho HS-3A-002
    (món "Canh bí đỏ nấu tôm" KHÔNG có `defaultSubstituteId` cấu hình) — `planned-dishes` trả
    đúng `blocked:true`, `POST /serve` trả đúng `SRV-026` với thông điệp rõ ràng, KHÔNG lưu gì
    (dọn dữ liệu test ngay sau). Verify nhánh THAY THẾ THÀNH CÔNG bằng SQL trực tiếp (JOIN
    `ca-basa-sot-ca-chua`→`dau-phu-sot-ca-chua` đúng dữ liệu mẫu có sẵn) — không có học sinh nào
    trong seed có dị ứng cá đã xác minh nên không dựng được ca HTTP đầy đủ cho nhánh này, nhưng
    logic đối xứng với nhánh đã verify qua HTTP.
  - FE (`teacher/distribution/page.tsx` + `[id]/page.tsx`): thêm hiển thị/chặn theo `blocked` —
    không cho tick chọn học sinh bị chặn vào lô xác nhận (tránh cả lô `POST /confirm` thất bại
    vì 1 transaction, ADR-M09-1), nút "Xác nhận đã phục vụ" tự disable + đổi nhãn khi bị chặn.
    Thêm nhãn lỗi `SRV-026` vào `messages.ts`.

- **Mục (2) — MF06 xuất kho theo suất THẬT (kể cả món thay thế) — xong:**
  - Backend: `GET /student-distributions/planned-dishes` cho phép bỏ `class` (School Admin/
    Service Caterers) để gộp TOÀN TRƯỜNG theo món; Teacher vẫn BẮT BUỘC truyền `class` (lỗi
    `SRV-001` nếu thiếu). Thêm field `className` vào `PlannedDishRow` (dùng khi cần bóc lại theo
    khối ở FE).
  - FE (`service-caterers/inventory/issue/page.tsx`) viết lại `demand` — TRƯỚC đọc
    `blocksData` × số suất GÕ TAY từng khối; NAY đọc `planned-dishes` (school-wide) × ingredients
    thật của TỪNG món (kể cả món thay thế, đã đúng nhờ bugfix BR-065 ở trên) — bỏ hẳn ô "Số suất
    theo khối" (không còn ảnh hưởng gì tới nhu cầu nữa).
  - **Bug đơn vị phát hiện khi verify qua curl** (không liên quan trực tiếp BR-065, phát hiện khi
    kiểm chuỗi tính toán mới): công thức món (`dishes[].ingredients[].unit`) LUÔN là "g"/"ml"
    trong khi danh mục kho (`catalog_ingredients.unit`) LUÔN là "kg" — gửi thẳng số gam làm
    demand khiến server hiểu nhầm gấp 1000 lần (VD 100g gạo bị hiểu thành 100kg). Bug CÓ TỪ
    TRƯỚC (code cũ trước Phase 2 cũng mắc lỗi này), không phải do đổi nguồn dữ liệu — vá bằng
    hàm `toBulkUnit()` (g/ml → kg/L) ở FE. Verify qua curl thật: gửi `100` (chưa quy đổi) →
    server hiểu 100kg; gửi `0.1` (đã quy đổi) → đúng 0,1kg = 100g.

- **Mục (3) — MF07 kiểm thực Bước 2/3 hiện dòng món thay thế — xong:**
  - `service-caterers/inspections/[id]/page.tsx` — thêm 1 lời gọi `planned-dishes` (school-wide,
    theo ngày/bữa của phiên đang xem), lọc `byDish` có `substituteCount > 0`, tạo thêm dòng
    catalog gợi ý riêng (tên món kèm nhãn "(suất thay thế — BR-065, N suất)") nối vào
    `step2Catalog`/`step3Catalog` hiện có — cơ chế "còn thiếu dòng nào" (`missingStep2Dishes`/
    `onAddAll`) tự động nhận diện đúng vì so khớp theo `dishName`, không cần sửa thêm.
  - Không đổi `Step2RowDto`/`Step3RowDto` (giữ nguyên shape, không thêm field `isSubstitute` mới
    — nhãn text trong `dishName` là đủ, tránh đổi hợp đồng API của form nhập tay đã ổn định).

- `tsc --noEmit`/`eslint`/`npm run audit` sạch toàn dự án sau mỗi bước. Backend `mvn compile`
  sạch, đã rebuild + restart + `npm run gen:api` lại sau các thay đổi DTO.

## 6. Phase 2 — Nhật ký triển khai mục (1)+(4) (05/08/2026, XONG — domain mới `class_meal_deliveries`)

- **Domain mới, KHÔNG tái dùng `deliveries` (NCC→bếp)** — khác object hoàn toàn: theo dõi
  TRẠNG THÁI giao/nhận theo (ngày, bữa, LỚP), không có breakdown món/suất trong response (đọc
  riêng `planned-dishes` cho việc đó, tránh duplicate nguồn sự thật).
  - Migration `V32__mf06_class_meal_deliveries.sql` — bảng `class_meal_deliveries` (status
    `DELIVERED`/`RECEIVED`), `UNIQUE(school_id, date, meal_type, classroom_id)` (idempotent theo
    thiết kế, không phải chỉ ở service layer).
  - `ClassMealDeliveryService`/`Impl` — `deliver()` (SERVICE_CATERERS, 409 nếu đã tồn tại bản ghi
    cho đúng (ngày, bữa, lớp) — sửa/giao lại dùng note, không tạo bản ghi mới); `receive()`
    (TEACHER, phải đúng GVCN lớp đó qua `isHomeroomTeacher()`, idempotent — gọi lại khi đã
    RECEIVED thì trả nguyên trạng, không lỗi); `list()` (Teacher chỉ thấy lớp mình, School
    Admin/Service Caterers thấy toàn trường).
  - `ClassMealDeliveryController` — `GET/POST /api/v1/class-meal-deliveries`,
    `POST /{code}/receive`.
  - Verify qua curl thật (`duchm.caterer.synthetic`, `lethihuong.teacher.synthetic`, lớp 3A,
    2026-07-07 LUNCH — ngày có sẵn `student_distributions` thật trong seed): deliver → 201 tạo
    `CMDLV-2026-0002`; teacher list đúng lọc theo lớp 3A; receive → 200 chuyển RECEIVED đúng tên
    người nhận. Đã xoá bản ghi test khỏi DB sau khi verify xong (không để lại dữ liệu rác).

- **FE — 2 màn mới, không mở rộng màn cũ** (đúng luồng Minh mô tả: bếp giao → giáo viên nhận →
  giáo viên mới chia cho học sinh, 2 bước tách bạch):
  - `service-caterers/class-deliveries/page.tsx` — chọn ngày/bữa, danh sách LỚP + số suất từng
    món (kể cả suất thay thế/suất bị chặn BR-065, đọc từ `planned-dishes`) + nút "Xác nhận đã
    giao" (`POST /class-meal-deliveries`). **Quyết định kỹ thuật**: danh sách lớp lấy từ
    `planned.perStudent[].className` (gộp duy nhất), KHÔNG gọi `usePagedList("classrooms")` —
    `ClassroomController` hiện `@PreAuthorize("hasRole('SCHOOL_ADMIN')")` nên Service Caterers gọi
    vào sẽ 403 (gap đã ghi nhận sẵn ở `real-resources.ts` dòng ~207, cùng lý do `meal-service.tsx`
    cũng đang gặp — "quyết định của Nam (chủ MF02), không tự mở rộng ở đây"). Đọc từ
    `planned-dishes` vừa né gap đó vừa đúng nhu cầu hơn: bếp chỉ cần biết lớp NÀO có suất phải
    giao hôm nay, lớp chưa có thực đơn thì không có gì để giao — không cần liệt kê MỌI lớp trong
    trường.
  - `teacher/class-deliveries/page.tsx` — giáo viên xem suất bếp đã giao cho lớp mình (theo món),
    xác nhận "Đã nhận" (`POST /class-meal-deliveries/{code}/receive`), sau khi RECEIVED hiện nút
    điều hướng sang `teacher/distribution` để chia cho từng học sinh.
  - Thêm alias `ClassMealDeliveryDto`/`CreateDeliveryDto` (`lib/api/types.ts`), thêm
    `"class-meal-deliveries"` vào `REAL_RESOURCES`, thêm link sidebar 2 vai (`lib/roles.ts`):
    "Giao suất tới lớp" (Service Caterers, nhóm "Hàng ngày") và "Nhận suất từ bếp" (Teacher, nhóm
    "Bữa ăn hôm nay", đặt TRƯỚC "Phân phát bữa ăn" đúng thứ tự quy trình).

- `tsc --noEmit`/`eslint`/`npm run audit` sạch toàn dự án. Backend đã build + migrate + verify
  qua curl thật như trên.

**Còn lại của Phase 2**: chỉ mục (5) — phụ huynh xem thực đơn/món thay thế TRƯỚC ngày ăn — chưa
làm, chưa có yêu cầu ưu tiên rõ từ người dùng cho mục này.
