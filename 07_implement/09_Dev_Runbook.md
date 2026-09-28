# 09 — Runbook: dựng môi trường, chạy, xác minh

> Viết cho người **mới nhận việc** sau khi pull git. Mọi lệnh dưới đây đã chạy thật
> 07–08/08/2026 trên Windows 11 + Git Bash.

## 0. Yêu cầu

| Thứ | Bản | Ghi chú |
|---|---|---|
| JDK | **25** | ⚠️ `JAVA_HOME` hệ thống trên máy Nam đang trỏ JDK 21 — **phải set tay** mỗi lệnh `mvn` |
| Node | 20+ | |
| Docker | | chạy Postgres |

## 1. Postgres

```bash
docker start sfms-postgres          # container đã có sẵn
docker ps --filter name=sfms-postgres --format "{{.Status}}"
```

Backend đọc `jdbc:postgresql://localhost:5432/sfms` (user/pass `sfms`/`sfms`), override bằng
`DB_URL`/`DB_USER`/`DB_PASSWORD`.

> ⚠️ Container **tự tắt** sau một thời gian không dùng. Triệu chứng: backend chết với
> `Connection refused: getsockopt`. Bật lại rồi chạy tiếp.

## 2. Backend

```bash
cd 04_Development/Backend
JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn spring-boot:run
```

**Để test luồng thực đơn trong ngày** (bỏ khoá BR-061 "nộp trước 3 ngày"):

```bash
MENU_LEAD_TIME_DAYS=0 JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn spring-boot:run
```

Mặc định trong `application.yml` vẫn là **3** — production không đổi hành vi. Nhớ **bỏ biến
này khi demo cho khách** nếu muốn thể hiện đúng BR-061.

> ⚠️ **BẪY hay mắc:** kill tiến trình `mvn` **KHÔNG giết JVM con** — cổng 8080 vẫn bị giữ,
> lần chạy sau chết với *"Port 8080 was already in use"* mà nhìn log dễ tưởng chạy được:
> ```bash
> PID=$(netstat -ano | grep ":8080" | grep LISTENING | head -1 | awk '{print $NF}')
> powershell -Command "Stop-Process -Id $PID -Force"
> ```

## 2-BIS. Docker phải BẬT trước khi `mvn test`

`mvn test` dùng **Testcontainers** (`application-test.yml` chạy Flyway trên container
ephemeral). Docker Desktop chưa chạy ⇒ test chết, và triệu chứng ở `docker ps` là:

```
dial ... dockerDesktopLinuxEngine: The system cannot find the file specified
```

Bật Docker Desktop rồi chờ daemon lên (`until docker info >/dev/null 2>&1; do sleep 5; done`).
`sfms-postgres`/`sfms-mailpit` tự bật theo.

> ⏱ Một lần `mvn test` đầy đủ mất **~20 phút** trên máy này. Đừng tưởng nó treo: surefire chỉ
> ghi `target/surefire-reports/` khi xong TỪNG CLASS, nên có quãng 10+ phút không ra file mới.

## 3. Frontend

```bash
cd 04_Development/Frontend
npm install     # ⚠️ BẮT BUỘC chạy lại sau khi pull: commit 598b1f6 thêm `exceljs`,
                #    thiếu nó thì `npx tsc --noEmit` chết 4 lỗi ở staff/upload/page.tsx
npm run dev            # proxy /api/v1 sang localhost:8080, same-origin nên không cần CORS
```

**Sinh lại type từ OpenAPI** (bắt buộc mỗi khi đổi DTO backend — backend phải đang chạy):

```bash
npm run gen:api        # openapi-typescript http://localhost:8080/v3/api-docs -> src/lib/api/schema.d.ts
```

## 4. Tài khoản

Sau khi xoá seed nghiệp vụ (08/08/2026, mục 11) **chỉ còn MỘT tài khoản** trong DB trắng:

| Vai | Email | Mật khẩu |
|---|---|---|
| System Admin | `dangnh.sysadmin.synthetic@example.test` | `P@ssw0rd123` |

Đây là **cửa vào duy nhất**. Sáu vai còn lại (School Admin, Y tế, Giáo viên, Đơn vị suất ăn,
NCC, Phụ huynh) **phải tạo bằng tay** — thứ tự ở cuối mục 11.

> Các doc cũ còn liệt kê `tranth.admin`, `ngoth.medical`, `quynhdt.teacher`, `duchm.caterer`,
> `sonlt.supplier`, `binhnv.parent` — **những tài khoản đó không còn tồn tại**. Chúng đến từ
> seed đã xoá.

## 5. Xác minh trước khi commit — CHẠY ĐỦ 4 LỆNH

```bash
# Backend
cd 04_Development/Backend
JAVA_HOME="/c/Program Files/Java/jdk-25.0.3" mvn test        # kỳ vọng: 374 pass, BUILD SUCCESS

# Frontend
cd 04_Development/Frontend
npx tsc --noEmit                                             # kỳ vọng: không output
npx next lint                                                # kỳ vọng: ≤8 warning, 0 ERROR
npm run build                                                # kỳ vọng: 181 route
```

> **Baseline cập nhật 08/08/2026:** route đo lại được **181** (doc cũ ghi 136 — số đó có từ
> trước lần merge `ac03d2e`). Lint đo được **7 warning**.

### Lệnh phụ (09/08/2026)

| Lệnh | Dùng khi |
|---|---|
| `npm run check:routes` | Soi **trang chết** (có `page.tsx` mà không ai dẫn tới) và **link gãy**. Baseline: 0/0, exit 0. Xem `13_Dead_Pages_Check.md` |
| `npm run audit` | Nay chỉ gọi `check:routes` |
| `npm run gen:api` | Sinh `schema.d.ts` qua **nginx** (`http://localhost/v3/api-docs`) — cách chạy mặc định |
| `npm run gen:api:local` | Khi chạy backend bằng `mvn spring-boot:run` (cổng 8080 mở trên host) |

> ⚠️ **Hai bẫy đã mắc, nay đã vá trong `package.json`:**
> 1. `gen:api` từng trỏ cứng `localhost:8080` — compose **không publish** cổng đó ra host (chỉ
>    nginx :80), nên lệnh luôn `fetch failed`. Cùng lớp bẫy với `curl http://localhost:8080/...`
>    bị "connection refused".
> 2. `npm run audit` **hỏng từ lâu mà không ai biết**: `audit:data` đọc `mock-data.INVENTORY` và
>    `audit:seed` đọc `src/lib/api/db.ts` — **cả hai đều đã bị xoá** ở đợt gỡ mock 06/08/2026.
>    Hai script đó nay **xoá hẳn**: một lệnh kiểm tra luôn đỏ vì lý do không liên quan sẽ dạy cả
>    đội bỏ qua nó.

**Baseline lint = 8 warning.** Nhiều hơn là do thay đổi của bạn — sửa, đừng bỏ qua. **0
Error** là bắt buộc: `react-hooks/rules-of-hooks` báo Error chứ không phải Warning.

> ⚠️ **Bẫy đã mắc:** thêm hook vào giữa component mà quên phía trên đã có `if (...) return`
> ⇒ vi phạm rules-of-hooks. Hook phải đứng trước MỌI early return.

## 6. Gọi API thật để kiểm (đừng suy từ code)

```bash
T=$(curl -s -X POST http://localhost:8080/api/v1/auth/login \
     -H "Content-Type: application/json" \
     -d '{"email":"duchm.caterer.synthetic@example.test","password":"P@ssw0rd123"}' \
   | python -c "import sys,json;print(json.load(sys.stdin)['accessToken'])")

curl -s -H "Authorization: Bearer $T" http://localhost:8080/api/v1/grades
```

> ⚠️ Đừng so chuỗi tiếng Việt trong `python -c` chạy qua Git Bash — encoding cp1252 làm hỏng
> (`'Khối 3'` không khớp). Lọc theo `sortOrder`/index, hoặc bọc
> `sys.stdout=io.TextIOWrapper(sys.stdout.buffer,encoding='utf-8')` khi cần in tiếng Việt.
>
> ⚠️ `/tmp/x.json` trong Git Bash và `python.exe` **không cùng đường dẫn**. Ghi file tạm vào
> thư mục cụ thể, đừng dùng `/tmp`.

## 7. Script rà soát

`07_implement/audit-scripts/` — chạy lại là ra đúng số liệu trong các doc:

| Script | Việc |
|---|---|
| `scan_endpoints.py` → `check_exists.py` → `scan2.py` | resource FE gọi vs `paths` trong OpenAPI, gắn theo vai |
| `probe2.py` → `locate.py` | curl từng cặp (vai × endpoint), lọc 401/403/5xx, **truy ngược file chứa lời gọi** |
| `ux_scan.py` | màn thiếu loading state |
| `scan_mut_handled.py` | chấm TỪNG lời gọi factory-mutation: có tự xử lý lỗi không |
| `apply_handled_meta.py` | codemod gắn `{ handledError: true }` |

> **Bẫy của chính đợt rà:** gán resource của file dùng chung (`lib/`, `components/`) cho MỌI
> vai ⇒ dương tính giả hàng loạt. **Phải truy ngược file chứa lời gọi** (`locate.py`) rồi mới
> kết luận. Chiều ngược lại cũng lọt: `useDietGroups()` nằm trong `lib/` nên gap 403 của
> Parent/Teacher **bị bỏ sót** ở đợt trước.

## 8. Quy ước code hay quên

- **PATCH, không PUT** cho resource đã có backend thật.
- **Mutation tự toast lỗi ⇒ BẮT BUỘC khai `{ handledError: true }`**, nếu không sẽ ra **2
  toast** (TanStack v5 gọi cả handler toàn cục lẫn callback của từng `mutate`). Màn KHÔNG tự
  xử lý thì **đừng thêm gì** — để `MutationCache.onError` ở `providers.tsx` lo.
- **Khối lớp**: nhãn ("Khối 3") là khoá HIỂN THỊ/URL; **UUID chỉ dùng ở ranh giới gửi
  request** qua `useGradeId(label)` (`lib/grades.ts`). `ClassroomResponse.gradeId` là `null` —
  không lấy UUID khối từ `classrooms` được.
- **Đừng khai lại hằng số mà server đã có.** Ba lần dính cùng một lỗi này:
  `NUTRITION_TARGET`, `menuLeadTimeDays`, danh mục `STANDARD_DIETS`. Đọc từ API.
- **Không module nào inject service của module khác** — cross-module read đi qua
  `common/reference/ReferenceResolver` bằng native query.
- **Không giả lập.** Backend chưa có thì ghi rõ "chưa hỗ trợ" (tiền lệ:
  `medical-staff/conduct-inspection/[id]/record`), đừng bịa dữ liệu.

## 9. HAI môi trường — đừng nhầm

Repo có **hai** cách chạy, DB tách hẳn nhau:

| | Stack compose (gốc repo) | Chạy tay |
|---|---|---|
| Lệnh | `docker compose up -d` | `mvn spring-boot:run` |
| Container | `sep490_g8-*` (8 cái: backend, frontend, postgres, nginx, cloudflared, mailpit, frs, rag) | `sfms-postgres`, `sfms-mailpit` |
| Backend | **jar đã build** trong image | source đang sửa |
| Vào bằng | `http://localhost` (qua nginx) | `http://localhost:8080` |

⚠️ **Sửa migration/code xong mà chỉ chạy tay thì stack compose VẪN dùng jar cũ.** Phải
`docker compose build backend` rồi `docker compose up -d backend`.

Đã dính đúng lỗi này 08/08/2026: một migration vá đã bị xoá khỏi source nhưng còn trong jar,
làm `sep490_g8-backend-1` crash-loop.

## 10. Trạng thái dữ liệu đã biết

Sau đợt xoá seed (mục 11), DB trắng chỉ còn cấu hình hệ thống:

| Còn | Rỗng |
|---|---|
| 7 role · 16 permission · 1 tài khoản System Admin | trường · lớp · học sinh · phụ huynh |
| 6 nhóm chế độ ăn · 13 đơn vị đo · 10 từ khoá bệnh lý · 6 cơ quan tiếp nhận · 4 tích hợp | **danh mục nguyên liệu** · món · thực đơn · đối tác |
| bộ đếm sinh mã (`code_sequences`) | kho · kiểm thực · mẫu lưu · phân phát · sự cố · vi phạm |

- **Không có `student_distributions`** ⇒ chưa có suất PENDING để bấm thử cổng BR-094. Muốn
  có: tạo thực đơn → nộp duyệt → BGH duyệt → **công bố**; event `MenuPublished` tự sinh phân
  phát cho từng học sinh.
- Khi dựng danh mục nguyên liệu, đọc `08_E2E_Findings_Backlog.md` mục **D1** trước — quy tắc
  độ mịn (tách khi đổi thứ này sang thứ kia làm đổi món / đổi dị nguyên / đổi chế độ ăn).


---

## 11. Xoá seed nghiệp vụ (08/08/2026) — dựng dữ liệu bằng tay

**Quyết định của Nam:** không vá seed nữa, xoá thẳng dữ liệu nghiệp vụ **trong chính file
migration** (không insert-rồi-delete), rồi tạo tay qua UI.

**Lý do:** seed khai món bằng TÊN tự do cho nguyên liệu chưa có trong danh mục —
`dish_ingredients.ingredient_code` NULL ở **10/14 dòng**. Cả hai màn tính nhu cầu đều BỎ QUA
dòng không có mã, nên nguyên liệu đó **không đặt mua, không xuất kho, không truy xuất được
lô** mà không có cảnh báo nào. Đo theo món: "Thịt kho hành" mất 2/2, "Cá basa sốt cà chua"
mất 2/3, tổng **8/9 món** hụt ít nhất một nguyên liệu.

### Cắt gì, giữ gì

Đã cắt **497 `INSERT`** khỏi 13 file (V5, V7, V9, V11, V13, V15, V17, V19, V21, V30, V38,
V39, V47). **Giữ** những thứ mà thiếu là không chạy được app, hoặc **không có endpoint ghi
để tạo lại**:

| Giữ | Ở đâu | Vì sao |
|---|---|---|
| roles · permissions · security_policies | V2 | không có thì không ai đăng nhập |
| `code_sequences` | V2, V6, V11, V16, V19, V21, V30, V48 | bộ đếm sinh mã, hạ tầng |
| **tài khoản System Admin** | V25 | `school_id = NULL`, chỉ phụ thuộc `roles` — **cửa vào duy nhất để dựng tay** |
| `diet_groups` (6) | V9 | `DietGroupController` có **0 endpoint ghi** — xoá là mất hẳn chế độ ăn đặc biệt |
| `receiving_authorities` (6) | V15 | 0 endpoint ghi |
| `illness_keywords` (10) | V19 | không có controller |
| `measure_units` (13) | V51 | không có controller |
| `external_integrations` (4) | V24 | cấu hình hệ thống |
| backfill `grades` | V44, V47 | `SELECT … FROM schools` ⇒ 0 trường thì 0 dòng, vô hại |

**Đã xoá** (đều có UI để tạo lại): `catalog_ingredients` (màn `system-admin/ingredients`),
`checklist_groups`/`items` (màn `medical-staff/conduct-inspection/checklist`), và toàn bộ
trường/lớp/học sinh/phụ huynh/đối tác/món/thực đơn/kho/kiểm thực/mẫu lưu/sự cố/vi phạm.

### Hệ quả bắt buộc

Sửa file migration ⇒ **checksum Flyway đổi** ⇒ **mọi DB đang có phải drop + create lại**:

```bash
docker exec <postgres> psql -U sfms -d postgres -c "DROP DATABASE IF EXISTS sfms;"
docker exec <postgres> psql -U sfms -d postgres -c "CREATE DATABASE sfms OWNER sfms;"
```

Làm cho **cả hai** môi trường (mục 9). Với stack compose nhớ `docker compose build backend`
trước, nếu không jar cũ vẫn chạy migration cũ.

**Đã xác minh 08/08/2026:** 53 migration chạy sạch trên DB trắng ở cả hai môi trường ·
đăng nhập `dangnh.sysadmin` **200** (qua `localhost:8080` và qua nginx `localhost`) ·
`GET /schools`, `/ingredients`, `/users`, `/diet-groups` đều 200.

### 11-BIS. Dựng dữ liệu bằng SCRIPT API (08/08/2026) — nhanh hơn dựng tay

`07_implement/seed/seed_demo.py` dựng **cả hai luồng** (bếp + sự cố) **chỉ bằng HTTP API**,
không một câu SQL. Dùng khi cần dữ liệu demo hoặc khi phải đo lại một đường nghiệp vụ.

```bash
docker compose build backend                                        # bắt buộc — mục 9
docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend
cd 07_implement/seed && cp .env.seed.example .env.seed              # điền mật khẩu
python seed_demo.py
```

> ⚠️ **Backend đang nối GMAIL THẬT** (`SMTP_HOST=smtp.gmail.com` trong
> `04_Development/Backend/.env`). Tài khoản tạo qua `POST /staff` ở trạng thái
> `PENDING_VERIFICATION` và **chỉ kích hoạt được bằng token trong email** — gửi ra Gmail thì
> không lấy được. `docker-compose.mailpit.yml` (gốc repo, **không tự nạp**, phải truyền `-f`)
> chuyển SMTP về Mailpit; đọc thư ở `http://localhost:8026`. Bỏ `-f` là quay lại Gmail.

Kết quả và các bẫy khi gọi API: `07_implement/10_E2E_API_Run_Report.md` và `seed/README.md`.

### Thứ tự dựng tay để chạy được luồng bếp

System Admin: danh mục nguyên liệu → trường → tài khoản School Admin. School Admin: năm học →
khối → lớp → học sinh/phụ huynh → đối tác (NCC + đơn vị suất ăn) → chuẩn dinh dưỡng theo khối.
Y tế: nhóm checklist kiểm tra bếp. Đơn vị suất ăn: món (nguyên liệu **phải chọn từ danh mục**)
→ mẫu thực đơn → lịch thực đơn → …

> ⚠️ **Việc còn nợ:** backend **chưa** validate `ingredient_code` — `DishServiceImpl
> .applyIngredients()` gán thẳng `i.ingredientId()` không kiểm gì, nên gọi API trực tiếp vẫn
> tạo được món có nguyên liệu ngoài danh mục. FE **đã** chặn
> (`service-caterers/dishes/page.tsx`) và DB có FK `fk_dish_ing_catalog`, nhưng cột vẫn
> NULLABLE. Xem `08_E2E_Findings_Backlog.md` mục D1.

### 11-TER. Kích hoạt tài khoản test khi email không gửi được (08/08/2026)

Tài khoản sinh tự động (VD Phụ huynh, sinh kèm khi tạo học sinh) ở trạng thái
`PENDING_VERIFICATION`, và `account_activation_tokens` **chỉ lưu hash** — không truy ngược
được token từ DB. Địa chỉ `@example.test` thì Gmail không gửi tới đâu cả.

```bash
# 1. Bắt email về Mailpit thay vì Gmail thật
docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend

# 2. Yêu cầu gửi lại email kích hoạt
curl -s -X POST http://localhost/api/v1/auth/activate/resend   -H "Content-Type: application/json" -d '{"email":"parent1.e2e@example.test"}'

# 3. Lấy token từ Mailpit (http://localhost:8026 để xem bằng mắt)
curl -s "http://localhost:8026/api/v1/messages?limit=1"          # lấy message ID
curl -s "http://localhost:8026/api/v1/message/<ID>"              # token nằm trong link ?token=

# 4. Đặt mật khẩu
curl -s -X POST http://localhost/api/v1/auth/activate   -H "Content-Type: application/json" -d '{"token":"<TOKEN>","newPassword":"..."}'

# 5. TRẢ LẠI Gmail thật — nếu quên, mọi email sau đó rơi vào Mailpit im lặng
docker compose up -d backend
```

**Tài khoản Phụ huynh đã dựng sẵn:** `parent1.e2e@example.test` / `ParentE2e@2026x`.
