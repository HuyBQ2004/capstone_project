# Trợ lý AI của SFMS — cách nó thật sự hoạt động

> Cập nhật 09/08/2026. Mọi con số trong tài liệu này đều **đo bằng API thật**, không suy từ code.
> Bối cảnh đầy đủ: [`07_implement/12_AI_Assistant_Findings.md`](../../../07_implement/12_AI_Assistant_Findings.md).

## 1. Truy hồi: pgvector + từ khoá, chạy song song

Trước 09/08/2026 cái tên "RAG service" gây hiểu nhầm — **không có truy hồi nào cả**: Postgres là
`postgres:15-alpine` (không cài được extension `vector`), không migration nào tạo bảng embedding,
và `langchain`/`pgvector`/`psycopg2` nằm trong `requirements.txt` mà **không dòng nào import**.
Backend gửi nguyên văn tài liệu sang, service chỉ ghép vào prompt.

Nay đã có thật:

| Thành phần | Chỗ đặt |
|---|---|
| Extension `vector` + bảng `ai_knowledge_chunks(embedding vector(768))` + index HNSW | migration `V59`, image `pgvector/pgvector:pg15` |
| Sinh vector nhúng | `POST /api/v1/rag/embed` của service này (biết đang dùng Gemini hay mô hình local) |
| Cắt đoạn + nạp vector lúc thêm tài liệu | `KnowledgeDocumentServiceImpl.reindex` |
| Tìm hàng xóm gần nhất `<=>` | `KnowledgeChunkRepository.searchNearest` |
| Truy hồi từ khoá (IDF + cụm từ) | `KnowledgeContextSelector` |

**Vì sao giữ CẢ HAI đường (hybrid) thay vì bỏ từ khoá đi.** Đo trên chính kho tri thức thật, mỗi
đường thắng một loại câu hỏi:

| Câu hỏi | Chỉ vector | Chỉ từ khoá |
|---|---|---|
| "Kiểm thực ba bước gồm những bước nào?" | ❌ trả nội dung kiểm tra hàng **nhập khẩu** | ✓ trích đúng Điều 3 QĐ 1246 |
| "Cả lớp đau bụng sau bữa trưa thì làm gì?" | ✓ hiểu là ngộ độc tập thể | ❌ không có từ nào trùng luật |

Nên `contextFor` chia đôi ngân sách: nửa cho vector, nửa cho từ khoá, loại đoạn trùng. Nhánh từ
khoá còn là **đường sống** khi mô hình embedding tắt, khi tài liệu vừa nạp chưa kịp sinh vector,
và khi chạy hệ thống không có nhà cung cấp AI nào.

`langchain`, `langchain-core`, `langchain-google-genai`, `python-dotenv` vẫn **không dùng** — đã
gỡ khỏi `requirements.txt`, image giảm **364MB → 281MB**.

### Tiền tố nhiệm vụ của nomic-embed — đừng bỏ

`nomic-embed-text-v1.5` được huấn luyện với tiền tố: câu hỏi phải mang `search_query:`, đoạn tài
liệu phải mang `search_document:`. Thiếu nó thì truy hồi kém rõ rệt (đó là một phần lý do câu
"kiểm thực ba bước" hỏng ở lần đo đầu). Xem `_apply_task_prefix` — Gemini `text-embedding-004`
không dùng quy ước này nên được bỏ qua.

### Đổi mô hình embedding = phải reindex

Vector của hai mô hình khác nhau **không so sánh được với nhau**; để lẫn thì kết quả tìm kiếm sai
mà không có lỗi nào báo ra. Sinh lại bằng `POST /ai/knowledge-documents/{code}/reindex`. Cả hai mô
hình đang dùng đều **768 chiều** (`text-embedding-004`, `nomic-embed-text-v1.5`) nên schema không
phải đổi; dùng mô hình khác chiều thì phải migration lại cột.

## 2. Luồng một câu hỏi

```
Web/Mobile ──POST /api/v1/ai/chat──▶ Backend Java
                                      │ 1. redact mã học sinh (BR-173/181)
                                      │ 2. truy hồi LAI, tổng ≤6000 ký tự:
                                      │      a. embed câu hỏi ──▶ rag-service /embed
                                      │      b. pgvector `<=>` trên ai_knowledge_chunks   (½ ngân sách)
                                      │      c. từ khoá IDF trên tài liệu đang bật        (½ ngân sách)
                                      │ 3. ghi ai_requests (BR-157, cùng transaction)
                                      ▼
                                    rag-service:8001  ──▶ Gemini API  hoặc  LM Studio/Ollama
```

Nạp tài liệu thì đi đường khác: cắt đoạn → `/embed` (task=`document`) → lưu `ai_knowledge_chunks`.
**Cắt sẵn lúc nạp**, không cắt lại mỗi câu hỏi.

**Ngân sách 6000 ký tự là bắt buộc, không phải tối ưu.** Kho tri thức thật (`00_Policies/`) nặng
**442KB** — riêng Thông tư 13/2016 đã 168KB. Trước 09/08/2026 code nhét nguyên **8 tài liệu đầu
tiên** vào mỗi lượt hỏi (giới hạn theo *số tài liệu*, không theo dung lượng). Nạp đủ tài liệu vào
là làm hỏng chính tính năng mà tài liệu đó phục vụ: mô hình local chạy `n_ctx = 4096` sẽ vỡ ngữ
cảnh ngay.

## 3. Chọn nhà cung cấp mô hình

| Biến | Mặc định | Ý nghĩa |
|---|---|---|
| `SFMS_AI_PROVIDER` | `gemini` | `openai` = bất kỳ máy chủ nào nói giao thức OpenAI `/v1/chat/completions` |
| `SFMS_AI_BASE_URL` | — | vd. `http://host.docker.internal:1234/v1` |
| `SFMS_AI_MODEL` | — | vd. `google/gemma-4-e4b` |
| `SFMS_AI_TIMEOUT_SECONDS` | `180` | khớp nginx `/api/v1/ai/` và axios của web |
| `GEMINI_API_KEY` | — | chỉ dùng cho provider `gemini` |

**Chạy mô hình local (LM Studio):**

```bash
# LM Studio → tab Developer → Status: Running (cổng 1234), nạp sẵn model
docker compose -f docker-compose.yml -f docker-compose.localai.yml up -d --build rag-service
```

Muốn máy mình mặc định dùng local thì thêm vào `.env` (đã gitignore):

```
COMPOSE_FILE=docker-compose.yml;docker-compose.localai.yml   # Windows dùng ';', Linux/macOS ':'
SFMS_AI_MODEL=google/gemma-4-e4b
```

⚠️ `host.docker.internal` **chứ không phải** `127.0.0.1` — trong container, `127.0.0.1` là chính
container đó.

**Khi local hỏng, hệ thống KHÔNG tự chuyển sang Gemini.** Đã chọn chạy local thì phải thấy nó
hỏng, chứ không âm thầm đốt quota. Câu trả lời khi đó ghi rõ *"PHẢN HỒI DỰ PHÒNG, không phải câu
trả lời của mô hình"* kèm lý do, và log ở mức ERROR.

### Chọn model nào

| Model | Chat | Gợi ý thực đơn | Ghi chú |
|---|---|---|---|
| Gemini `gemini-2.0-flash` | 13s (qua Cloudflare) | 79–130s | **hết quota free tier** (429), phải rơi xuống model dự phòng |
| LM Studio `gemma-4-e2b` | **0.7s** | ❌ sai khuôn JSON (tự đặt khoá `mo_ten`) | không dùng được cho gợi ý thực đơn |
| LM Studio `gemma-4-e4b` | 11–25s | 67.7s · 8/8 nguyên liệu đúng danh mục | **đang dùng** |

## 4. Kho tri thức

System Admin quản tại `/system-admin/ai-knowledge`. Hai cách nạp:

* **Gõ/dán** — `POST /ai/knowledge-documents` `{title, category, content}`;
* **Upload file** — `POST /ai/knowledge-documents/upload` (multipart), nhận `.pdf` `.docx` `.txt`
  `.md` `.csv` ≤10MB. Server bóc văn bản bằng **PDFBox/POI** (đã có sẵn từ MF08, không thêm thư
  viện). **Không lưu file gốc** — kho tri thức cần văn bản cho ngữ cảnh, không cần bản scan.
  PDF chỉ chứa ảnh scan bị từ chối với lý do rõ ràng (`AIS-415`) thay vì tạo tài liệu rỗng.

Nguồn tài liệu thật của dự án: [`00_Policies/`](../../../00_Policies) — 7 văn bản (QĐ 1246, NĐ
15/2018, Luật ATTP 2010, TTLT 13/2016…), tổng 333.000 ký tự sau khi bóc.

**Kho tri thức rỗng làm hỏng trợ lý:** `SYSTEM_PROMPT` buộc chỉ trả lời dựa trên tài liệu tham
chiếu, nên khi kho trống, mô hình nhỏ **từ chối trả lời** câu nghiệp vụ. Đo trước/sau khi nạp
`00_Policies`:

| Câu hỏi | Kho rỗng | Sau khi nạp |
|---|---|---|
| "Mẫu thực phẩm lưu tối thiểu bao nhiêu giờ, nhiệt độ nào?" | *"chưa có thông tin trong tài liệu tham chiếu"* | **24 giờ, 2–8°C** ✓ |
| "Kiểm thực ba bước gồm những bước nào?" | từ chối | 3 bước, trích Điều 3 QĐ 1246 ✓ |

## 5. Bẫy đã mắc — đừng mắc lại

1. **`GeminiClient` có constructor không tham số cắm cứng `localhost:8001`** ⇒ Spring bỏ qua
   `@Value`, mọi câu trả lời là chuỗi stub. Sống sót rất lâu vì stub trông như câu trả lời thật
   và log ghi mức INFO. Nay nhánh dự phòng nói thẳng nó là dự phòng và log ERROR.
2. **axios timeout 10s + nginx `proxy_read_timeout` 60s** trong khi một lượt sinh mất 11–130s.
   Sửa một đầu không đủ: nới nginx mà quên axios thì trình duyệt vẫn bỏ cuộc.
3. **Trần multipart mặc định của Spring là 1MB** và không ai khai đè ⇒ mọi giới hạn 10MB/5MB khai
   trong code đều là lời hứa suông, request chết thành **500 chưa xử lý**. Nay khai ở
   `application.yml` + handler trả **413**.
4. **Chấm điểm truy hồi bằng "đếm số từ khớp"** ⇒ câu *"Kiểm thực ba bước"* lấy về đoạn nói về
   thu hồi sản phẩm và ghi nhãn, vì "kiểm"/"thực" có mặt ở gần như mọi đoạn của mọi văn bản luật.
   Nay chấm theo **độ hiếm (IDF)** + thưởng cụm từ.
5. **Prompt liệt kê danh mục dạng `Tên (kg)`** ⇒ model chép cả đuôi `(kg)`, khiến bộ lọc loại
   sạch nguyên liệu hợp lệ. Chỉ lộ ra khi chạy trên trình duyệt, không lộ khi gọi API bằng script.

## 6. Còn nợ

1. **Xếp hạng lại (rerank)**: hiện hai đường truy hồi chia đôi ngân sách một cách cứng nhắc. Chuẩn
   hơn là gộp điểm (RRF — reciprocal rank fusion) rồi cắt theo ngân sách chung. Chưa làm vì cần đo
   trên tập câu hỏi thật đủ lớn mới biết có hơn không.
2. **Có nên dùng LangChain?** Vẫn **không** — kể cả sau khi đã có pgvector. Toàn bộ phần truy hồi
   là ~150 dòng Java có test, cộng một endpoint `/embed` 30 dòng. LangChain sẽ thêm tầng trừu
   tượng lên thứ đã chạy, cộng phụ thuộc nặng và churn phiên bản, đổi lại không thêm năng lực nào.
   Đáng cân nhắc lại nếu sau này cần nhiều retriever/reranker/agent — không phải bây giờ.
3. `menu-form.tsx` vẫn gọi `/ai/chat` thay vì endpoint chuyên dụng `POST /menus/{code}/ai-suggest`
   (có rate limit, lưu lịch sử) — endpoint đó cần mã thực đơn đã tồn tại, cần chốt.
4. **Chưa có nút "Nạp lại vector" trên UI** — mới có API `POST /ai/knowledge-documents/{code}/reindex`.
   Cần khi đổi mô hình embedding hoặc khi tài liệu nạp lúc mô hình đang tắt.
