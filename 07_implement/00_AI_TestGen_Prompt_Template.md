# Prompt chung — Sinh test case còn thiếu cho một module MF (dùng lại cho MF khác)

> Nguồn gốc: đúc kết từ đợt viết test cho MF13 (2026-08-11) — viết 15 file mới, chạy full suite
> thật với Docker lần đầu ra 10/148 fail, sửa xong 148/148 pass. Toàn bộ 10 lỗi đều là **lỗi
> trong chính test mới viết** (đoán sai hành vi thật của code), không phải lỗi ở code chính.
> Phần "Rút kinh nghiệm" ở cuối là để AI lần sau không lặp lại đúng 10 kiểu lỗi đó.

---

## Cách dùng

Thay `MFxx` bằng module cần làm (VD MF05, MF07...), thay đường dẫn TestSpec tương ứng, rồi đưa
nguyên văn phần "PROMPT" bên dưới cho AI (Claude Code hoặc tương đương, có quyền đọc/ghi file +
chạy `mvn`).

---

## PROMPT

```
Repo: c:\SEP490_G8\SEP490_G8\04_Development\Backend (Spring Boot, Maven — `mvn` trên PATH).

Mục tiêu: code production của MFxx ĐÃ XONG và KHÔNG được sửa — chỉ viết các file JUnit 5 còn
thiếu để nâng coverage lên đúng những gì `07_implement/MFxx_<TenModule>/TestSpec-MFxx_*.md` mô
tả. Đọc toàn bộ TestSpec đó trước (đọc theo từng đoạn ~400 dòng nếu file dài) để lấy oracle
chính xác (số BR/UC/MSG, giá trị expected, ý định của từng test case ID).

QUY TẮC QUAN TRỌNG NHẤT: TestSpec là Ý ĐỊNH THIẾT KẾ, CODE THẬT MỚI LÀ CHUẨN. Nếu chỗ nào code
triển khai thực tế khác giả định của spec (biến cấu hình bị hardcode, tầng validate nằm khác chỗ
spec đoán, tính năng chưa được implement...), viết test đúng theo NHỮNG GÌ CODE THẬT LÀM, không
bịa theo tài liệu. Nếu cả một tính năng spec giả định mà code hoàn toàn không có, BỎ QUA và ghi
rõ trong báo cáo cuối — không tự chế test cho tính năng không tồn tại.

KHÔNG được sửa bất kỳ file nào dưới src/main/java. Chỉ thêm/sửa file dưới src/test/java.

RIÊNG NHÓM E2E: KHÔNG viết code tự động (không JUnit, không Playwright/Selenium). E2E là kịch bản
TEST TAY — viết file Markdown theo đúng mẫu bảng của `05_Testing/02_IT/sample.md` (cột Test Case
ID/Description/Procedure/Expected Results/Pre-conditions/Round 1-2-3/Note, trạng thái mặc định
`Pending`), đặt tại **`05_Testing/02_IT/MFxx_<TenModule>/`** (ĐÚNG chỗ team đã dùng sẵn — KHÔNG
đặt ở `03_Web`, thư mục đó không phải quy ước của team, đang trống). Có 2 loại file, phân biệt
bằng tiền tố ID và nội dung Procedure — kiểm tra xem MFxx đã có file nào chưa trước khi tạo mới:

- **`End_to_End_Scenarios.md`** (ID `MFxx-TC-E2E-NNN`) — luồng nhiều API nối tiếp qua nhiều
  module, Procedure ghi bước gọi API (`POST /...`, `GET /...`) + dữ liệu cần kiểm ở DB/response.
  Loại này ánh xạ 1-1 với nhóm "E2E" trong TestSpec — nếu TestSpec ghi "N TC E2E" thì file này
  phải có đúng N dòng.
- **`Manual_Web_Flows.md`** (ID `MFxx-WEB-NNN`) — luồng click tay trên UI thật (mở trang, điền
  form, bấm nút), Procedure ghi thao tác trình duyệt, KHÔNG phải lệnh API. Đây là lớp bổ sung
  ngoài TestSpec (TestSpec không có mục riêng cho luồng UI) — chỉ viết nếu được yêu cầu rõ, đừng
  tự ý tạo thêm nếu prompt gốc không nhắc tới.

Trước khi tạo file mới, LUÔN kiểm tra `05_Testing/02_IT/MFxx_.../` xem đã có `End_to_End_Scenarios.md`
chưa — nếu có rồi và số dòng khớp số TC E2E của TestSpec thì coi như ĐÃ XONG, chỉ cần đọc lại xem
nội dung còn khớp hành vi code thật không, không viết trùng. Không tự đánh dấu Passed/Failed — giữ
nguyên `Pending` vì AI không chạy tay được, để QA/tester điền khi thực thi.

⚠️ **`05_Testing/` là read-only với AI, coi ngang `src/main/java`.** Toàn bộ thư mục này sẽ được
convert sang Excel nộp cho trường — KHÔNG được thêm/sửa BẤT KỲ nội dung nào vào file đã có trong
đó (kể cả ghi chú "đối chiếu với code thật", cột Note, hay sửa dòng có sẵn), dù phát hiện nội dung
lệch với hành vi code hiện tại. Nếu phát hiện lệch, ghi vào `07_implement/14_TC_Completion_Status.md`
(mục Gap/bug hoặc một mục riêng "Lệch giữa kịch bản thủ công và code thật"), KHÔNG BAO GIỜ sửa
trực tiếp file trong `05_Testing/`. Việc TẠO MỚI file trong `05_Testing/` (khi thật sự chưa có,
theo đúng mục "Cách dùng" ở trên) chỉ được làm khi prompt gốc yêu cầu rõ, và:
- **PHẢI viết bằng tiếng Anh** — mọi nội dung dưới `05_Testing/` dùng để nộp trường, chuẩn ngôn
  ngữ là tiếng Anh (xem `sample.md`, `Password_Management.md`, `End_to_End_Scenarios.md` — đều
  tiếng Anh). Quy tắc này CHỈ áp dụng cho `05_Testing/`; các file theo dõi nội bộ khác của team
  (VD `07_implement/14_TC_Completion_Status.md`) vẫn giữ tiếng Việt theo quy ước sẵn có, không đổi.

## Việc phải làm (Unit/Security/Integration/Contract — code tự động)
1. Đọc kỹ TestSpec, đọc kỹ MỌI class production sẽ động tới trước khi viết test.
2. Đọc 2-3 file test đã có trong module (hoặc `AbstractIamIntegrationTest` nếu là IT/Security/
   Contract) để bắt đúng style, helper sẵn có (auth header, parse response, seed data...).
3. Viết các test case còn thiếu, bám sát style/package convention hiện có.
4. BẮT BUỘC verify bằng cách chạy thật `mvn -Dtest="com.sfms.mfxx.**" test` với Docker đang bật
   — KHÔNG dừng lại ở `mvn test-compile`. Compile sạch KHÔNG có nghĩa là test đúng hay pass.
5. Nếu test fail, tự sửa TEST (không sửa code chính) cho tới khi pass, hoặc nếu phát hiện đó là
   gap/bug thật của code chính thì DỪNG LẠI, không tự sửa code, báo cáo lại cho người review.

## Việc phải làm (E2E — kịch bản tay, xem quy tắc riêng ở trên)
E2E KHÔNG chạy `mvn test` — chỉ tạo/bổ sung file Markdown ở `05_Testing/03_Web/`. Vẫn phải đọc
TestSpec để lấy đúng oracle (BR/UC/luồng nghiệp vụ) cho từng kịch bản trước khi viết.

## Báo cáo cuối
Liệt kê file code đã tạo (map theo TC ID) + kết quả chạy `mvn test` thật (số pass/fail, không
phải compile) cho 4 nhóm code tự động; liệt kê riêng file Markdown E2E đã tạo/bổ sung ở
`05_Testing/03_Web/`; liệt kê TC ID (mọi nhóm) không viết được kèm lý do (tính năng chưa tồn tại).

## RÚT KINH NGHIỆM — 10 lỗi đã gặp ở đợt làm MF13, tránh lặp lại

1. **Đừng suy đoán mã lỗi/HTTP status từ tài liệu — luôn chạy thử để xem THẬT.** Enum sai giá trị
   (VD gửi `"format":"CSV"` khi enum chỉ nhận PDF/XLSX) bị Jackson chặn TRƯỚC khi vào
   controller/service → trả mã lỗi GENERIC theo handler `HttpMessageNotReadableException`
   (`<PREFIX>-422`), KHÁC với mã validate thủ công ở tầng service (VD `RPT-138`). Đừng giả định
   "field sai định dạng" và "field thiếu/null" trả cùng một mã lỗi — kiểm tra riêng từng nhánh.

2. **Không kiểm tag/schema OpenAPI ở cấp "toàn cục" (`spec.tags`, `/components/schemas` quét hết)
   — dễ dính false-positive/false-negative từ MODULE KHÁC.** `@OpenAPIDefinition(tags = {...})`
   chỉ đăng ký thủ công một vài tag ở top-level, KHÔNG tự động gom `@Tag` của mọi controller. Kiểm
   tra sự tồn tại của tag phải làm ở CẤP OPERATION (`op.tags` trên từng path/method), không phải
   `spec.tags`. Tương tự, khi kiểm "schema không được tên X*", chỉ quét các schema mà CHÍNH module
   đang xét thực sự tham chiếu (đi từ operation có tag của module đó, walk `$ref` bắc cầu), không
   quét toàn bộ `/components/schemas` của cả app — module khác có thể có schema trùng tiền tố tên
   một cách hợp lệ.

3. **Trước khi viết assertion trên `/v3/api-docs`, tải bản dump THẬT về và đọc, đừng đoán cấu
   trúc.** Hai lỗi hay gặp:
   - content-type của response KHÔNG chắc là `application/json` — nếu controller không khai
     `produces` tường minh, Spring/springdoc dùng `*/*`. JSON Pointer phải trỏ đúng key đó.
   - một enum/DTO có thể KHÔNG có schema riêng trong `components/schemas` — nó có thể bị "inline"
     thẳng vào property dùng nó (`type: string, enum: [...]`) nếu bản thân enum không có
     `@Schema(name=...)`. Đừng giả định mọi enum/DTO đều có schema top-level riêng.
   - envelope lỗi có thể KHÔNG phẳng — `ErrorResponse.properties.error` có thể là `$ref` trỏ tới
     một schema con (VD `ErrorBody`) chứa `code/message/details`, không nằm thẳng trên
     `ErrorResponse`. Luôn resolve `$ref` trước khi assert property.

   Cách làm nhanh: viết một test tạm gọi `GET /v3/api-docs`, `Files.writeString(...)` ra file JSON,
   `node -e "..."` hoặc tương đương để soi cấu trúc thật, RỒI xóa test tạm đó đi sau khi xong.

4. **GreenMail lưu MỘT BẢN SAO MAIL PER-RECIPIENT.** Gửi 1 email tới N người nhận →
   `greenMail.getReceivedMessages()` trả về N phần tử (mỗi mailbox một bản sao), KHÔNG phải 1.
   Đừng assert `hasSize(1)` khi có nhiều hơn 1 recipient — assert theo đúng số recipient, hoặc
   assert trên tập hợp gộp nếu không quan tâm thứ tự.

5. **Nếu có `MutableClock`/test clock cố định (kiểm tra `AbstractIamIntegrationTest` hoặc tương
   đương của module), MỌI dữ liệu seed liên quan tới khoảng thời gian phải tính theo đúng "hiện
   tại" của clock đó, không phải ngày hôm nay thật hay ngày bất kỳ.** Test lịch/báo cáo tính kỳ
   theo (`frequency`, `today`) — nếu seed dữ liệu ngoài kỳ được tính ra, kết quả sẽ rỗng một cách
   "đúng" (không phải bug) nhưng làm assertion của bạn sai. Tính ngược từ giá trị clock cố định ra
   kỳ thật trước khi chọn ngày seed.

6. **Khi test một cơ chế "clamp"/edge-case phân trang, đừng giả định clamp áp dụng cho CẢ dữ liệu
   lẫn metadata hiển thị.** Có thể chỉ metadata (`page`) được clamp để hiển thị đẹp, còn dữ liệu
   (`items`) vẫn lấy từ query gốc chưa clamp (VD `page=99` → Spring Data trả `content` rỗng dù
   `page` trong response đã sửa về `pageCount`). Chạy thử để biết code THẬT làm gì trước khi viết
   assertion cứng, đừng suy luận "logic" từ tên hàm.

7. **Không tự thêm field/tham số cấu hình vào production code để "làm cho test viết được dễ hơn".**
   Nếu spec giả định một giá trị có thể cấu hình (VD `report.retention-months`) nhưng code thật
   hardcode, viết test khẳng định giá trị hardcode đó — không sửa code chính để khớp spec.

8. **`mvn test-compile` PASS không có nghĩa là test ĐÚNG.** Nếu môi trường không có Docker lúc viết
   test (Testcontainers không chạy được), phải nói rõ trong báo cáo là "chỉ compile, chưa chạy
   thật" và yêu cầu chạy lại `mvn test` full khi có Docker — đừng báo "PASS" khi thực chất chỉ mới
   compile sạch.

9. **Ghi lại GAP thật một cách tường minh thay vì che giấu bằng assertion giả.** Nếu phát hiện một
   phần hành vi spec mô tả nhưng code chưa làm (VD tham số `search` bị bỏ qua hoàn toàn dù entity
   có sẵn hạ tầng filter), viết test xác nhận ĐÚNG hiện trạng ("gọi có `search` vẫn ra kết quả y
   hệt không truyền — không lỗi 500") kèm comment giải thích, thay vì hoặc (a) giả vờ filter hoạt
   động, hoặc (b) im lặng bỏ qua không viết test cho endpoint đó.

10. **Warning kiểu "method X deprecated" từ IDE diagnostics sau khi Edit không phải lúc nào cũng
    cần sửa** — nếu đó là API cũ nhưng vẫn đang được cả codebase dùng nhất quán (VD `JsonNode.
    fields()` ở dự án này), giữ nguyên theo convention hiện có thay vì tự ý đổi sang API khác chỉ
    vì IDE cảnh báo, trừ khi được yêu cầu dọn dẹp riêng.
```
