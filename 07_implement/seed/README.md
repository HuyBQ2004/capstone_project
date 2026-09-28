# Seed dữ liệu demo — chỉ bằng API thật

`seed_demo.py` dựng đủ dữ liệu để chạy **cả hai luồng nghiệp vụ chính** (bếp và sự cố) trên
một DB trắng, **không dùng một câu SQL nào**: mọi bản ghi đi qua đúng endpoint mà người dùng
thật sẽ bấm. Nhờ vậy dữ liệu seed có đủ audit log, mã sinh theo `code_sequences`, và **đi qua
mọi gate BR** — nếu một luật chặn, script dừng ngay tại đó thay vì đẻ ra dữ liệu không thể
tồn tại qua UI.

## Chạy

```bash
cd 07_implement/seed
cp .env.seed.example .env.seed        # rồi điền mật khẩu 4 tài khoản gốc
python seed_demo.py                   # chạy hết 8 pha
python seed_demo.py incident          # chạy lại một pha (các pha trước chỉ lấy token)
```

## Điều kiện trước khi chạy

| # | Việc | Vì sao |
|---|---|---|
| 1 | `docker compose build backend && docker compose up -d backend` | Stack compose chạy **jar đã build** — sửa code mà không build lại thì vẫn là bản cũ (runbook mục 9) |
| 2 | `docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend` | Script lấy **token kích hoạt** tài khoản y tế/giáo viên từ Mailpit. Mặc định backend gửi Gmail thật ⇒ email đi ra ngoài và script không đọc được |
| 3 | `MENU_LEAD_TIME_DAYS=0` | compose đã đặt sẵn — không có thì BR-061 chặn tạo thực đơn trong ngày |
| 4 | ~~Đã có sẵn: 1 trường, 1 năm học, khối, lớp `1A`, 4 tài khoản gốc~~ **KHÔNG CÒN CẦN** (08/08/2026) | Pha `bootstrap` tự dựng trường + 3 tài khoản gốc + năm học + khối + lớp, đi ĐÚNG luồng thật (đăng ký công khai → duyệt → kích hoạt qua email). Chỉ cần **DB trắng + tài khoản System Admin** có sẵn từ migration |

Quay lại gửi Gmail thật: `docker compose up -d backend` (không truyền file override).

## 8 pha

| Pha | Vai | Sinh ra |
|---|---|---|
| `accounts` | School Admin | tài khoản **Y tế** + **Giáo viên** (tạo → email Mailpit → kích hoạt → đăng nhập) |
| `catalog` | System Admin · Đơn vị suất ăn | 6 nguyên liệu · 4 món (nguyên liệu chọn từ danh mục) |
| `students` | School Admin | 3 học sinh + phụ huynh (BR bắt buộc mỗi HS có ≥1 phụ huynh) |
| `supply` | NCC · Đơn vị suất ăn | hồ sơ hợp tác → duyệt → 4 mặt hàng → 4 lô → đơn mua → báo giá → chốt giá → lịch giao → **nhận hàng ⇒ 4 lô kho** |
| `menu` | Đơn vị suất ăn · School Admin | thực đơn trưa hôm nay → nộp → duyệt → **công bố ⇒ 3 suất phân phát** |
| `inspection` | Đơn vị suất ăn | kiểm thực 3 bước (PASSED) + **2 mẫu lưu** |
| `serve` | School Admin · Giáo viên | gán GVCN lớp 1A → cổng BR-094 mở → **phát 3 suất** |
| `incident` | Giáo viên · Y tế · School Admin · Đơn vị suất ăn | báo cáo → hồ sơ → điều tra → khoanh vùng → giữ mẫu → cách ly lô → CAPA (duyệt/nhận/bằng chứng/nghiệm thu) → xử lý lô → **đóng sự cố** |

## Bẫy đã mắc khi viết script (giữ lại để khỏi mắc lại)

- **Trường kiểu nguyên thuỷ (`boolean`/`int`) trong DTO record mà vắng mặt ⇒ 422 "malformed
  request body"**, không nói field nào. Dính ở `ReceivingLineRequest.rejected`,
  `Step1FreshRowDto.photoCount/hasVetCert`, `SubmitEvidenceRequest.evidencePhotos`.
  ⇒ **Gửi đủ mọi field**, kể cả field OpenAPI không đánh dấu `required`.
- **Ba endpoint là `multipart/form-data`**, không phải JSON: `POST /partnership-applications`
  (part `meta`), `POST /samples` (part `payload`, **bắt buộc ≥1 ảnh** — SMP-107),
  `POST /corrective-actions/{code}/evidence-photos` (part **`files`**, số nhiều).
- **Giá trị bị DB CHECK ràng buộc**: `quickTest ∈ {PASS,FAIL,NONE}`,
  `storage ∈ {THUONG,LANH,DONG}`, `samples.unit ∈ {G,ML}` (viết HOA),
  `rootCauseStatus ∈ {PRELIMINARY,FINAL}`.
- **Giáo viên chỉ thấy suất của lớp mình chủ nhiệm** — chưa `PATCH
  /classrooms/{code}/homeroom-teacher` thì danh sách phân phát **rỗng**, không báo lỗi gì.
- **Hồ sơ sự cố phải `finalize` (COMPLETED)** rồi mới `open-investigation` được.

Chi tiết kết quả đo và các lỗi backend đã sửa nhờ đợt seed này:
[`../10_E2E_API_Run_Report.md`](../10_E2E_API_Run_Report.md).

---

## Dựng lại demo từ DB TRẮNG (08/08/2026)

Trước đây script cần "Nam đã dựng tay qua UI" nên không dựng lại được ở máy khác. Nay chạy
được từ số 0 — **đã chạy thật: 105 bước, 0 lỗi, sự cố đóng được**.

```bash
# 1. DB trắng
docker compose exec -T postgres sh -c 'dropdb -U sfms sfms && createdb -U sfms sfms'

# 2. SMTP → Mailpit (BẮT BUỘC: 5 tài khoản kích hoạt bằng token trong email)
docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend

# 3. Chạy
cd 07_implement/seed && python seed_demo.py

# 4. TRẢ backend về Gmail — quên thì mọi email sau đó rơi vào Mailpit im lặng
docker compose up -d backend
```

### Bẫy đã mắc, ghi lại để khỏi mò

- **Rate limit 5 lần/phút/IP** ở `/schools/register` và `/partners/register/*`. Chạy lại liên
  tiếp gặp **429** — chờ một phút, không phải lỗi code.
- **Đơn vị suất ăn PHẢI có hợp tác đã duyệt** trước khi đặt hàng. Tài khoản đối tác không mang
  `schoolId` trong JWT; backend suy "trường của tôi" từ hợp tác đang hiệu lực. Thiếu bước này
  thì `POST /purchase-requests` trả **403 ING-005** — không phải lỗi phân quyền, mà là *chưa ký
  hợp đồng*.
- **Boolean NGUYÊN THUỶ thiếu ⇒ 422 "malformed request body"** không nói field nào. Đã dính hai
  lần: `rejected` (phiếu nhận) và `utensilsPass` (Mẫu 3). Thêm field boolean mới vào DTO là
  phải cập nhật script.
- **Năm học**: backend chặn kích hoạt năm ĐÃ kết thúc. Chạy vào tháng 8 (nghỉ hè) thì năm đúng
  là năm SẮP khai giảng — script tự chọn, dashboard ghi "Active — chưa bắt đầu" (đúng sự thật).

### Backup trước khi drop DB

`pg_dump` qua Git Bash **làm hỏng file nhị phân** (`> file.dump` cho ra file đủ dung lượng
nhưng `pg_restore` báo *"did not find magic string"*). Cách đúng:

```bash
docker compose exec -T postgres sh -c 'pg_dump -U sfms -d sfms -Fc -f /tmp/sfms.dump'
docker compose cp postgres:/tmp/sfms.dump _db_backup/sfms-$(date +%Y%m%d-%H%M%S).dump
# LUÔN kiểm khôi phục thử vào DB tạm rồi đối chiếu số bản ghi trước khi drop
```

`_db_backup/` đã vào `.gitignore` — **bản dump chứa PII** (tên học sinh, email phụ huynh, hash
mật khẩu), không được commit.

## Tài khoản sau khi chạy seed (đã kiểm bằng đăng nhập thật 08/08/2026)

| Vai | Email | Mật khẩu | Nguồn |
|---|---|---|---|
| Quản trị hệ thống | `dangnh.sysadmin.synthetic@example.test` | `P@ssw0rd123` | migration (có sẵn trên DB trắng) |
| Ban giám hiệu | `nam14092004@gmail.com` | `n6UTg.u!w:DeUtE` | `.env.seed` |
| Đơn vị suất ăn | `namnguyen14092004@gmail.com` | `J!etATmYTq_66rc` | `.env.seed` |
| Nhà cung cấp | `namnvhe180863@fpt.edu.vn` | `yW3VApB:AAnT5NT` | `.env.seed` |
| Nhân viên y tế | `medical.e2e@example.test` | `MedE2e@2026x` | `PWD` trong `seed_demo.py` |
| Giáo viên | `teacher.e2e@example.test` | `TeachE2e@2026x` | `PWD` trong `seed_demo.py` |
| Phụ huynh | `parent1.e2e@example.test` | `ParentE2e@2026x` | `PWD` — kích hoạt ở pha `students` |

Ba mật khẩu đầu nằm trong `.env.seed` (đã gitignore) — **đổi ở đó thì đổi luôn tài khoản
thật**, script chỉ đọc chứ không tự đặt lại. Bốn tài khoản còn lại do script tạo và tự đặt mật
khẩu từ hằng `PWD`.

> **`parent2`/`parent3` cố ý KHÔNG kích hoạt** — giữ lại để test đúng trạng thái
> `PENDING_VERIFICATION` (BR-010: chưa xác minh email thì không đăng nhập được).
