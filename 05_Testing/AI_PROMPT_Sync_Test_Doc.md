# Prompt chuẩn — Đồng bộ tài liệu kiểm thử với code test thật (dùng cho AI)

> Mỗi người phụ trách một MF, chạy độc lập, không đụng file của nhau.
> **Cách dùng:** copy toàn bộ khối trong mục 1, thay `<<MF>>` bằng số MF của bạn (`03`, `04`, …), dán vào Claude Code mở tại thư mục gốc repo.
> Đọc mục 2 (chạy song song) và mục 3 (nghiệm thu) trước khi bắt đầu.

---

## 1. Prompt để copy

```
Đồng bộ tài liệu kiểm thử của MF<<MF>> với code test thật.

BỐI CẢNH
- Code test: 04_Development/Backend/src/test/java/com/sfms/mf<<MF>>/
  (riêng MF01 thì package là com/sfms/iam/, không phải mf01)
- Doc unit test:        05_Testing/01_UI/MF<<MF>>_*/
- Doc integration test: 05_Testing/02_IT/MF<<MF>>_*/
- Format bắt buộc:      05_Testing/01_UI/sample.md và 05_Testing/02_IT/sample.md

NGUYÊN TẮC SỐ 1 — CODE LÀ NGUỒN SỰ THẬT
Tài liệu hiện tại nhiều chỗ đã lỗi thời so với code. Khi doc và code lệch
nhau thì SỬA DOC THEO CODE, không sửa code theo doc. Không được suy đoán
kết quả test: phải chạy thật rồi lấy số thật.

CÁC BƯỚC

Bước 1 — Đọc toàn bộ code test của MF<<MF>>, liệt kê từng class và từng
method. Ghi lại @DisplayName, điều kiện đầu vào, và oracle (mã lỗi, message,
trạng thái DB được assert).

Bước 2 — PHÂN LOẠI UNIT vs INTEGRATION THEO NỘI DUNG, KHÔNG THEO TÊN FILE.
  - Unit test  = thuần Mockito, không @SpringBootTest, không đụng DB.
  - Integration = có @SpringBootTest / kế thừa AbstractIamIntegrationTest /
    dùng jdbc / cần Docker.
  Trong repo này có 13 file tên "*Test.java" nhưng thực chất là integration
  test (nhiều nhất ở MF05, MF09, MF12). Đừng tin đuôi file. Ngược lại,
  "AbstractIamIntegrationTest.java" khớp mẫu "*Test.java" nhưng KHÔNG phải
  test class — đừng đếm nó.

Bước 3 — Chạy test lấy kết quả thật, chạy từ 04_Development/Backend:
  Unit:
    mvn -o test -Dtest='com.sfms.mf<<MF>>.**.*Test' -DfailIfNoTests=false
  Integration (cần Docker Desktop đang chạy):
    mvn -o verify -Dit.test='com.sfms.mf<<MF>>.**.*IT' \
        -Dfailsafe.forkCount=1 -Dtest='<một unit test bất kỳ của bạn>' \
        -Dsurefire.failIfNoSpecifiedTests=false \
        -Dfailsafe.failIfNoSpecifiedTests=false
  KHÔNG chạy `mvn verify` trần — sẽ chạy toàn bộ 13 module, rất lâu.
  Lấy số liệu chính xác từ file XML, KHÔNG đọc dòng tóm tắt của console
  (class có @Nested luôn hiện "Tests run: 0" ở console, sai):
    target/surefire-reports/TEST-*.xml  và  target/failsafe-reports/TEST-*.xml
  Đọc thuộc tính tests= / failures= / errors= và từng <testcase>.

Bước 4 — Nếu test ĐỎ, phân biệt 3 loại rồi xử lý khác nhau:
  (a) Lỗi hạ tầng — "Could not find a valid Docker environment",
      NoClassDefFoundError, ExceptionInInitializerError khi khởi động
      container: chạy lại với -Dfailsafe.forkCount=1. Không phải lỗi test.
  (b) Lỗi test — assertion sai/lỗi thời: sửa TEST rồi chạy lại cho xanh.
  (c) Lỗi nghiệp vụ thật: KHÔNG tự sửa code production. Ghi "Failed" vào
      doc kèm mã defect và báo lại cho người phụ trách.

Bước 5 — Viết lại doc theo ĐÚNG format sample.md.
  01_UI (unit):
    - Mỗi file .md = một test class. Đặt tên file theo tên class bỏ đuôi Test.
    - Mỗi UTCID = một @Test method (parameterized: mỗi bộ tham số một UTCID).
    - Đủ 4 mục: General Information / Result Summary / Condition-Confirmation
      Matrix / Execution Result.
    - Hàng "Passed/Failed" điền chữ CÁI: P hoặc F (không viết "Passed").
    - "Lines of code" lấy từ file service production tương ứng, không phải
      LOC của file test.
    - Bất biến phải đúng: Passed+Failed+Untested = N+A+B = Total Test Cases
      = số cột UTCID = số test đã chạy.
  02_IT (integration):
    - Gom theo Feature; mỗi file một Feature. Nếu một Feature quá dài
      (trên ~50 ca) thì tách thành 2 file theo nhóm nghiệp vụ.
    - Mỗi dòng = một @Test method. Với @ParameterizedTest lặp cùng một kịch
      bản chỉ khác vai trò/endpoint thì được gộp thành 1 dòng, và PHẢI ghi rõ
      số tổ hợp đã phủ ở cột Note.
    - Cột Note ghi tên method Java (VD: `AuthControllerIT.loginReturnsExactShape`)
      để truy ngược code.
    - Round 1 = lần chạy thật hôm nay (điền ngày + tên bạn).
      Round 2, Round 3 để "Pending".
    - "Number of TCs" và bảng Testing Round Summary phải khớp đúng số dòng.

Bước 6 — NGÔN NGỮ: toàn bộ nội dung trong file .md viết bằng TIẾNG ANH
(để convert sang Excel nộp báo cáo). Không chèn thêm mục nào ngoài 4 mục của
sample.md — không thêm blockquote, không thêm ghi chú cuối file.

Bước 7 — Được phép GIẢM số test case trong doc nếu quá nhiều hoặc trùng lặp,
miễn là mỗi dòng doc vẫn map được về code thật và tổng số khớp. Tuyệt đối
không được bịa ca không tồn tại trong code.

Bước 8 — Tự kiểm tra trước khi báo xong, in ra bảng đối chiếu:
  số UTCID trong doc  vs  số unit test đã chạy
  số dòng TC trong doc vs số integration test đã chạy
  các con số trong Result Summary / Round Summary có tự nhất quán không
Nếu lệch thì sửa cho khớp rồi mới báo.

PHẠM VI
- Chỉ đụng file trong 05_Testing/01_UI/MF<<MF>>_*/ và 05_Testing/02_IT/MF<<MF>>_*/
- Được sửa file test trong com/sfms/mf<<MF>>/ nếu là lỗi loại (b) ở Bước 4.
- KHÔNG sửa code production (src/main).
- KHÔNG sửa file .xlsx.
- KHÔNG đụng thư mục MF khác — người khác đang làm song song.
- Không commit, không push. Báo cáo xong để tôi tự review.

BÁO CÁO CUỐI
Liệt kê: số file doc đã viết, tổng unit test / integration test và số
pass-fail thật, những chỗ doc cũ sai lệch so với code, và mọi defect phát
hiện được.
```

---

## 2. Chạy song song — điều kiện và rủi ro

**Chạy song song được**, vì mỗi người đụng một thư mục con riêng của `05_Testing/` và một package riêng của `src/test/`. Không có file dùng chung nào bị ghi đồng thời.

Nhưng có 4 điểm phải thống nhất trước:

| Rủi ro | Xử lý |
|---|---|
| **Hai người trên cùng một máy** | Không được. Testcontainers mở Postgres riêng cho từng lần chạy, hai tiến trình Maven cùng lúc sẽ tranh `target/` và cổng Docker. Mỗi người một máy, một clone riêng. |
| **File `Report5_*.xlsx`** | Đây là file dùng chung, sẽ conflict nếu nhiều người cùng sửa. Quy ước: **giai đoạn này không ai đụng .xlsx**, chỉ sửa `.md`. Cuối cùng cử một người chạy `populate_report.py` / `populate_ut_report.py` để gộp một lượt. |
| **Nhánh Git** | Mỗi người một nhánh riêng, gộp về sau. Vì đường dẫn tách bạch theo MF nên merge gần như không conflict. |
| **Người làm MF01** | Package là `com/sfms/iam/`, không phải `com/sfms/mf01/`. Prompt đã ghi chú, nhắc lại kẻo chạy lệnh không ra test nào. |

## 3. Nghiệm thu — kiểm 4 điểm này là biết làm ẩu hay không

1. **Có chạy test thật không?** Đòi xem đường dẫn `target/failsafe-reports/TEST-*.xml`. Không có file XML nghĩa là chưa chạy, số liệu là bịa.
2. **Tổng có khớp không?** Số UTCID trong doc phải bằng đúng số unit test đã chạy. Lệch một ca cũng là dấu hiệu doc chưa bám code.
3. **Cột Note của file IT có tên method Java không?** Không có thì không truy ngược được, coi như chưa đạt.
4. **Hàng `Passed/Failed` trong file 01_UI có đúng chữ `P`/`F` không?** Viết đủ chữ "Passed" là chưa đọc kỹ `sample.md`.

## 4. Bẫy đã gặp thật khi làm MF01 — nhắc mọi người

**Đồng hồ test bị ghim, đừng dùng `now()` của Postgres trong assertion.**
`MutableClock` ghim ở `2026-07-30T02:00:00Z`, còn `now()` là giờ máy thật. Trộn hai nguồn thời gian trong một phép so sánh tạo bom hẹn giờ: test xanh hôm nay, đỏ vào một ngày bất kỳ sau này — hoặc tệ hơn, **xanh rỗng** (mệnh đề lọc sạch mọi dòng nên assertion không còn kiểm được gì, không bắt được regression nữa).

Đã xảy ra thật ngày 2026-08-14: 2 test đỏ và 1 test xanh-giả ở MF01, thêm 1 chỗ mong manh ở MF08.

Cách viết đúng: dùng `Timestamp.from(clock.instant())` truyền vào làm tham số, hoặc literal `TIMESTAMPTZ` cố định. Xem `ReportExportGuardIT` của MF13 làm mẫu — có comment giải thích đầy đủ.

`now()` ở vế INSERT cho cột dấu vết (`approved_at`, `collected_at`, …) thì vô hại, không cần đổi. Chỉ cấm ở vế so sánh trong assertion.
