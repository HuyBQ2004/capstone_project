# Trợ lý AI + Gợi ý thực đơn bằng AI — kết quả ĐO thật (09/08/2026)

**Người đo:** Nam · **Cách đo:** gọi API thật qua nginx (`http://localhost/api/v1/…`) + đọc log
`backend` / `rag-service`. Không suy từ code.

Xuất phát từ ba câu hỏi của Nam:
1. Trợ lý AI nên là **popup** thay vì trang riêng.
2. **Sao nó không chat được?**
3. Gợi ý thực đơn bằng AI **đã chạy chưa, bấm thế nào?** — và tiếp theo: *"ingredients nó đã lấy
   từ danh mục các nhà cung cấp hợp tác với trường đó chưa hay lấy linh tinh tự do sáng tạo?"*

---

## 1. "Sao không chat được" — BA lỗi chồng nhau, không phải một

Cả ba đều tự nó đủ làm hỏng tính năng, nên sửa một cái vẫn thấy hỏng.

| # | Lỗi | Bằng chứng đo được | Đã sửa |
|---|---|---|---|
| 1 | `GeminiClient` có **constructor không tham số** cắm cứng `localhost:8001`. Spring có nhiều constructor và không cái nào `@Autowired` ⇒ chọn cái không tham số ⇒ `@Value("${sfms.rag.url}")` **không bao giờ được áp dụng** | mọi câu trả lời là chuỗi mẫu `"Đây là phản hồi mẫu (stub) cho…"`; log backend không hề có dòng gọi rag-service | ✅ gộp còn MỘT constructor, mặc định `http://rag-service:8001` |
| 2 | **axios timeout 10s** dùng chung cho mọi request (`lib/api/client.ts`) | một lượt sinh đo được **11.5s** (câu ngắn) → **105–130s** (prompt thực đơn). Tức mọi câu hỏi từ UI đều bị huỷ trước khi có trả lời, hiện "Không kết nối được trợ lý AI" — **trong khi backend vẫn sinh xong và ghi log THÀNH CÔNG** | ✅ `SLOW_AI_PATHS` (`/ai/`, `/ai-suggest`) → 180s; phần còn lại giữ 10s |
| 3 | **nginx `proxy_read_timeout` mặc định 60s** cho `/api/v1/` | prompt gợi ý thực đơn trả **504 Gateway Time-out (HTML của nginx)** trong khi log backend vẫn báo thành công | ✅ thêm `location /api/v1/ai/` riêng, 180s |

> **Lỗi #2 và #3 là đúng cái bẫy "sửa một đầu của cặp".** Nới nginx mà quên axios thì trình
> duyệt vẫn bỏ cuộc ở 10s; nới axios mà quên nginx thì vẫn nhận 504. Phải nới **cả hai**.

Sau khi sửa cả ba — đo lại: `POST /ai/chat` **HTTP 200**, `model=gemma-4-26b-a4b-it`,
`redacted=true`, không còn chuỗi stub.

**Giới hạn phạm vi (thiết kế, không phải lỗi):** hỏi ngoài chuyên môn thì RAG trả
*"Tôi là trợ lý chuyên biệt về an toàn thực phẩm học đường…"* — đúng BR-157.

## 2. Popup thay vì trang riêng — đã đổi

`components/app/ai-chat-widget.tsx` gắn trong `AppShell` ⇒ dùng được ở **mọi màn**.
**Cố ý KHÔNG có lớp phủ toàn màn**: trợ lý là công cụ tra cứu *kèm theo* công việc, người dùng
phải đọc được số liệu ở màn phía sau trong lúc hỏi. Esc để đóng. Trang `/ai-chat` giữ nguyên,
là đích của nút "Mở toàn màn hình"; hai nơi dùng **chung một API**.

## 3. "AI lấy nguyên liệu linh tinh hay theo danh mục?" — CÂU TRẢ LỜI: trước 09/08 là **linh tinh**

**Đo trước khi sửa.** Prompt cũ trong `menu-form.tsx` chỉ truyền allergen + lô sắp hết hạn,
**không truyền danh mục nào**. AI trả về `"Gạo"`, `"Nước"`, `"Thịt lợn"`, `"tôm"` — trong khi
danh mục hệ thống là `Gạo tẻ`, `Thịt heo nạc`… và `"Nước"`, `"tôm"` **không tồn tại**.

Hậu quả không chỉ là tên xấu:
* tên không khớp ⇒ `ingredientId` rỗng ⇒ món **không truy xuất được lô** (BR-076);
* nguyên liệu ngoài **danh mục hợp đồng đã duyệt** thì bếp **không mua được** — chặn bằng
  `ING-077` từ 08/08. AI có thể vẽ ra món *không thể nấu*.

### 3.1 Hai mức danh mục — và mức đúng là mức hẹp hơn

| Nguồn | Số mục (dữ liệu demo, SCH-0001) | Nghĩa |
|---|---|---|
| `GET /ingredients` — danh mục nguyên liệu **toàn hệ thống** | 6 | Cà chua, Cá basa phi lê, Gạo tẻ, Rau cải ngọt, Thịt heo nạc, Trứng gà |
| `partnership_catalog_items` — **hợp đồng đã duyệt của trường** | **4** | Thịt heo nạc, Cá basa phi lê, Gạo tẻ, Rau cải ngọt |

Ép AI theo 6 mục vẫn sai: món dùng **Cà chua/Trứng gà** hợp lệ trên màn thực đơn nhưng vỡ ở
`ING-077` lúc đặt mua — rất lâu sau, khi kế hoạch đã lập xong. Ràng buộc đúng là thứ bếp **mua
được**.

### 3.2 Vướng: người lập thực đơn không đọc được danh mục hợp đồng

Đo: `GET /partnerships/PTN-201/catalog-items` với tài khoản đơn vị suất ăn → **403 IAM-004**
(`@PreAuthorize` chỉ có SYSTEM_ADMIN/SCHOOL_ADMIN/SUPPLIER), và endpoint đó **cần mã hợp đồng**
mà vai này không đứng tên.

**Thêm `GET /api/v1/catalog-items`** (MF04, vẫn thuộc nhóm PROPOSED): danh mục hiệu lực của
**chính trường**, gộp mọi hợp đồng, không cần mã hợp đồng. Điều kiện hiệu lực đặt **trùng khít**
với `ReferenceResolver.isIngredientInApprovedCatalog` (`ACTIVE` + `effective_from <= hôm nay`) —
lệch nhau thì UI lại hứa thứ mà cổng mua hàng từ chối. Quyền: SYSTEM_ADMIN, SCHOOL_ADMIN,
**SERVICE_CATERERS**; trường lấy qua `ActorScope` (tài khoản đối tác không có `schoolId`).

Đo sau khi triển khai:

| Vai | Kết quả |
|---|---|
| Đơn vị suất ăn | `200`, total=4 — đúng danh mục hợp đồng |
| School Admin | `200`, total=4 |
| Nhà cung cấp | `403` — **cố ý**: NCC phục vụ nhiều trường, không suy ra được trường nào |

### 3.3 Ràng buộc + kiểm lại đầu ra

Prompt nay kèm câu **BẮT BUỘC** liệt kê danh mục hợp đồng và yêu cầu ghi tên chính xác. Nhưng
**không tin model đã tuân thủ**: sau khi parse, mọi nguyên liệu không có trong danh mục bị
**loại bỏ** và màn hình hiện rõ *"Đã loại N nguyên liệu AI đề xuất nhưng KHÔNG có trong danh mục
của trường"*. Ràng buộc trong prompt là lời đề nghị, không phải bảo đảm.

Màn hình cũng nói rõ đang ràng buộc theo danh mục nào: hoặc *"AI chỉ được dùng 4 mặt hàng trong
danh mục hợp đồng đã duyệt"*, hoặc — nếu trường chưa duyệt mặt hàng nào — cảnh báo vàng rằng
đang rơi về danh mục chung và món đề xuất **có thể không mua được**. Im lặng dùng danh mục rộng
hơn chính là cái bẫy vừa gỡ.

### 3.4 Lỗi thứ tư — chỉ lộ ra khi chạy trên TRÌNH DUYỆT, không lộ khi gọi API

Gọi API bằng script cho **3/3 nguyên liệu đúng danh mục**. Nhưng mở đúng màn thật
(*Thực đơn → AI Assist (UC-075)*) thì màn hình hiện:

> *"Đã loại 3 nguyên liệu AI đề xuất nhưng KHÔNG có trong danh mục của trường — Không còn
> nguyên liệu nào hợp lệ."*

…trong khi cả ba nguyên liệu đều nằm trong danh mục. Bắt response thật của trình duyệt
(Playwright network) thấy nguyên nhân:

```json
{"name":"Thịt heo nạc (kg)","quantity":"50","unit":"g"}
```

Prompt liệt kê danh mục dạng `Tên (đơn vị)` và dặn *"ghi tên CHÍNH XÁC như liệt kê"* — model
làm **đúng y lời**: chép cả đuôi `(kg)`. Tên đó không khớp danh mục ⇒ bộ lọc loại sạch. **Lỗi
của prompt, không phải của model.** Script gọi API không bắt được vì lần đó model tình cờ trả
tên trần.

Sửa hai đầu (một đầu là chưa đủ):
1. **Prompt**: liệt kê `Tên — đơn vị kg`, và nói rõ `"name"` là phần **trước dấu "—"**, không
   kèm đơn vị.
2. **So khớp**: `normalizeIngredientName()` — bỏ phần trong ngoặc, gom khoảng trắng, chuẩn hoá
   Unicode **NFC** (tiếng Việt có hai cách mã hoá dấu; chuỗi từ model không nhất thiết cùng dạng
   với chuỗi từ Postgres). **Cố ý KHÔNG bỏ dấu** — "cà" và "cá" là hai nguyên liệu khác nhau,
   khớp nhầm còn tệ hơn không khớp. Nguyên liệu giữ lại còn được **ghi đè về đúng chính tả trong
   danh mục** để tra ra `ingredientId`.

> **Bài học lặp lại lần nữa:** đo bằng API là cần, **chưa đủ**. Màn hình mới là nơi lỗi này sống.

### 3.5 Kết quả đo cuối

| Lần đo | Cách đo | Kết quả |
|---|---|---|
| Trước khi sửa | API | bịa `Nước`, `tôm`, `Gạo`, `Thịt lợn` |
| Ép theo `/ingredients` (6 mục) | API | **8/8** nguyên liệu đúng danh mục, 105s |
| Ép theo `/catalog-items` (4 mục) | API | **3/3** đúng danh mục hợp đồng, 130s |
| Ép theo `/catalog-items`, prompt dạng `Tên (kg)` | **trình duyệt** | **0/3** — model chép cả `(kg)`, loại sạch (mục 3.4) |
| Sau khi sửa prompt + `normalizeIngredientName` | **trình duyệt** | **3 món, 8/8 nguyên liệu giữ nguyên**, 79s, không dòng "Đã loại" nào |

Chạy trọn trên trình duyệt (đơn vị suất ăn → *Thực đơn* → *AI Assist (UC-075)*): 3 món có tên
tử tế (*"Cơm thịt heo nạc xào rau cải ngọt"*, *"Cơm cá basa phi lê"*…). Bấm **Áp dụng vào thực
đơn** → dòng thành phần hiện **"Thịt heo nạc · Công ty Thực phẩm Nguyễn Nam · 145.000₫/kg"** ⇒
`ingredientId` đã tra ra, món **nối được về lô** và **mua được** theo hợp đồng. Đây là điều
kiện mà toàn bộ mục 3 nhắm tới.

> **Quan sát kèm về hạ tầng:** log `rag-service` cho thấy chuỗi model dự phòng —
> `gemini-2.0-flash` trả **429 RESOURCE_EXHAUSTED** (hết quota free tier) rồi mới rơi xuống
> `gemma-4-26b-a4b-it` thành công. Đây là một phần lý do mỗi lượt mất 79–130s. Nếu cần nhanh:
> bỏ model đã hết quota khỏi danh sách, hoặc nâng quota.

> **Còn lại về chất lượng model:** tên món nó trả về hiện chỉ là chuỗi nguyên liệu nối lại
> (`"Cá basa phi lê, Cà chua, Gạo tẻ, Rau cải ngọt"`) chứ không phải tên món tiếng Việt.
> Đây là chất lượng sinh của `gemma-4-26b-a4b-it`, không phải lỗi ràng buộc — sửa được bằng
> few-shot ví dụ trong prompt. **Chưa làm.**

## 4. Bấm ở đâu — và lỗi thứ năm: MÀN SOẠN THỰC ĐƠN KHÔNG CÓ ĐƯỜNG VÀO

Nam hỏi: *"sao không có cách nào để vào trang này: `/service-caterers/menus/create`"*. Đo:
`grep -rn "menus/create" --include="*.tsx"` → **0 kết quả**. Route tồn tại, được viết để mở từ
lưới tuần kèm `?grade=&date=&meal=&ai=1` (đúng như Javadoc của chính nó), nhưng **link gọi nó đã
mất trong một lần refactor**. Chỉ vào được bằng cách gõ tay URL.

Hệ quả không nhỏ: lịch tuần chỉ ghép được **thực đơn MẪU** (`menu-templates`). Toàn bộ nhánh
soạn món chi tiết — thành phần, định lượng, dinh dưỡng, **và AI Assist UC-075 vừa sửa ở mục 3**
— không có ai vào được. Sửa xong tính năng mà không có nút bấm thì vẫn là chưa xong.

**Đã nối lại hai lối vào** (`app/(app)/service-caterers/menus/page.tsx`):
* nút **"Soạn thực đơn chi tiết"** ở đầu trang, mang theo khối đang xem;
* nút **"Soạn chi tiết"** trong TỪNG ô (ngày × bữa) còn sửa được, mang theo `grade+date+meal`.

Đo lại trên trình duyệt: bấm ô *Trưa 17/08* → form mở với **Khối 1**, ngày **2026-08-17**, khối
**Bữa trưa · Thứ 2 — 17/08/2026** điền sẵn.

* **Trợ lý AI:** nút tròn ✨ góc phải dưới, có ở **mọi màn** sau khi đăng nhập.
* **Gợi ý thực đơn:** Đơn vị suất ăn → *Thực đơn* → **Soạn chi tiết** (hoặc *Soạn thực đơn chi
  tiết*) → nút **"AI Assist (UC-075)"** ở đầu form. Chọn khối trước để AI biết hồ sơ dị ứng.
  Chờ **~1–2 phút** — chậm là bình thường, không phải treo.

## 5. Xác minh

`mvn test` **374 pass** (371 + 3 test mới `SchoolCatalogItemsScopeTest`) · `tsc --noEmit` sạch ·
`next lint` **7 warning / 0 error** (đúng baseline) · `npm run build` thành công.

## 6. Chạy mô hình LOCAL khi dev, Gemini khi production (09/08/2026)

Nam hỏi: *"tính năng AI có thể cấu hình tạm để gọi local AI không? tương lai đưa lên production
thì dùng gemini?"* — **được**, và nay đã có công tắc. Sau đó Nam chốt: *"chưa mua gemini cứ dùng
tạm local đi"*.

**Lý do thực tế:** log `rag-service` cho thấy `gemini-2.0-flash` trả **429 RESOURCE_EXHAUSTED**
(hết quota free tier) rồi mới rơi xuống model dự phòng — đó là phần lớn thời gian chờ 79–130s.

### 6.1 Công tắc

`rag_chain.py` trước đây cắm cứng `google-genai`. Nay chọn bằng **biến môi trường**, không sửa code:

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `SFMS_AI_PROVIDER` | `gemini` | `openai` = gọi bất kỳ máy chủ nào nói giao thức OpenAI `/v1/chat/completions` (LM Studio, Ollama, vLLM, hoặc chính OpenAI) |
| `SFMS_AI_BASE_URL` | — | vd. `http://host.docker.internal:1234/v1` |
| `SFMS_AI_MODEL` | — | vd. `google/gemma-4-e4b` |
| `SFMS_AI_TIMEOUT_SECONDS` | `180` | khớp với nginx `/api/v1/ai/` và axios |

Dùng `requests` (đã có sẵn) thay vì thêm SDK — thân request chỉ vài trường.

**Mặc định của repo KHÔNG đổi:** thiếu biến ⇒ vẫn Gemini, tức nhánh production nguyên vẹn.
Bật local bằng lớp phủ `docker-compose.localai.yml`; máy nào muốn mặc định local thì thêm
`COMPOSE_FILE=docker-compose.yml;docker-compose.localai.yml` vào `.env` (xem `.env.example`).

> ⚠️ `host.docker.internal` **chứ không phải** `127.0.0.1`: trong container, `127.0.0.1` là chính
> container đó. Đây là cùng lớp bẫy với `localhost:8080` (cổng không publish) đã mắc hai lần.

**Không tự chuyển sang Gemini khi local hỏng** — đã chọn chạy local thì phải thấy nó hỏng, chứ
không âm thầm đốt quota. Nhánh dự phòng nay log mức **ERROR** kèm lý do và nói thẳng trong câu
trả lời rằng *"PHẢN HỒI DỰ PHÒNG, không phải câu trả lời của mô hình"* — trước đây nó log mức
INFO và trả chuỗi mẫu trông y như thật, chính là thứ giúp lỗi `GeminiClient` sống sót rất lâu.

### 6.2 Đo thật (qua đúng API `POST /ai/chat` mà web + mobile gọi)

| Cấu hình | Chat ngắn | Gợi ý thực đơn | Nguyên liệu đúng danh mục |
|---|---|---|---|
| Gemini (chuỗi dự phòng sau 429) | 13s (qua Cloudflare) · 32s (local) | 79–130s | 8/8 |
| LM Studio `gemma-4-e2b` (2B, Q4) | **0.7s** | ❌ **sai khuôn JSON** — tự đặt khoá `mo_ten`, trả object thay vì mảng | — |
| LM Studio `gemma-4-e4b` (4B) | 46.6s | 67.7s | **8/8** |

**Chọn `e4b`**: `e2b` nhanh gấp bội nhưng không dùng được cho gợi ý thực đơn.

**Hạn chế đã đo, cần biết trước khi tin:** với câu *"Kiểm thực ba bước gồm những bước nào?"*,
`e4b` **từ chối trả lời** ("chưa có thông tin trong tài liệu tham chiếu") trong khi Gemini trả
lời đúng — kho tri thức RAG đang rỗng nên model nhỏ bám sát ràng buộc "chỉ dựa trên tài liệu
tham chiếu", còn model lớn dùng kiến thức nền. Tức **chất lượng hỏi–đáp giảm rõ**; phần gợi ý
thực đơn (có đủ dữ liệu trong prompt) thì không bị.

### 6.3 Ràng buộc vận hành

Máy dev này **cũng đang chạy `cloudflared`**, tức phục vụ luôn `sfsm.io.vn`. Nên khi bật local:
**LM Studio phải luôn mở** (Developer → Status: Running, cổng 1234), tắt là trợ lý AI trên domain
thật cũng ngừng — nhưng sẽ báo rõ "PHẢN HỒI DỰ PHÒNG" chứ không im lặng. Lên production thật thì
bỏ `COMPOSE_FILE` trong `.env` và nạp `GEMINI_API_KEY` của gói trả phí.

## 7. Còn nợ

1. `menu-form.tsx` vẫn gọi `POST /ai/chat` chứ **không** gọi endpoint chuyên dụng
   `POST /menus/{code}/ai-suggest` (có rate limit, lưu lịch sử, MENU-073). Nên chuyển — nhưng
   endpoint đó cần mã thực đơn **đã tồn tại**, còn màn này gợi ý lúc **đang soạn**. Cần chốt.
2. `npm run gen:api` trỏ `http://localhost:8080` — cổng này **không publish** khi chạy Docker
   Compose (chỉ nginx :80). Phải chạy `npx openapi-typescript http://localhost/v3/api-docs`.
   Nên sửa script hoặc ghi rõ trong runbook.
3. Cân nhắc bỏ mục `/ai-chat` khỏi sidebar 7 vai vì đã có popup.
4. Few-shot ví dụ để model đặt **tên món** tử tế.
