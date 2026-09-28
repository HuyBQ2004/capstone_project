import os
import logging
from typing import List, Dict, Any, Optional

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("rag_chain")

# 13/08/2026 (theo Nam) — gemini-2.0-flash đã bị Google loại bỏ (retired), nên MỌI lượt hỏi
# đều fail ngay ở lần thử đầu và rơi xuống model dự phòng "gemma-4-26b-a4b-it" — đúng nhánh
# đã đo chậm 79-130s/lượt (xem ghi chú ở generate_rag_answer). Đổi sang gemini-3.5-flash
# (bản ổn định hiện tại, https://ai.google.dev/gemini-api/docs/models) để lần thử đầu thành
# công thẳng, không phải rơi xuống fallback chậm nữa.
DEFAULT_GEMINI_MODEL = "gemini-3.5-flash"

# ─────────────────────────── Chọn nhà cung cấp mô hình ───────────────────────────
#
# `gemini` (MẶC ĐỊNH, dùng cho production) — gọi Google GenAI.
# `openai`  — gọi BẤT KỲ máy chủ nào nói giao thức OpenAI `/chat/completions`:
#             LM Studio, Ollama, vLLM, hoặc chính OpenAI.
#
# Vì sao cần: quota free tier của Gemini cạn rất nhanh khi dev (đo 09/08/2026:
# `gemini-2.0-flash` trả 429 RESOURCE_EXHAUSTED, phải rơi xuống model dự phòng, mỗi lượt
# 79–130s). Chạy mô hình local lúc phát triển thì không tốn quota, không cần mạng, và
# phản hồi nhanh hơn — production vẫn dùng Gemini, KHÔNG đổi mặc định.
#
# Đổi bằng biến môi trường, KHÔNG sửa code:
#   SFMS_AI_PROVIDER=openai
#   SFMS_AI_BASE_URL=http://host.docker.internal:1234/v1   # LM Studio trên máy host
#   SFMS_AI_MODEL=google/gemma-4-e2b
# (xem `docker-compose.localai.yml` — đã dựng sẵn đúng ba biến này)
AI_PROVIDER = os.getenv("SFMS_AI_PROVIDER", "gemini").strip().lower()
AI_BASE_URL = os.getenv("SFMS_AI_BASE_URL", "").strip().rstrip("/")
AI_MODEL = os.getenv("SFMS_AI_MODEL", "").strip()
AI_API_KEY = os.getenv("SFMS_AI_API_KEY", "lm-studio").strip()
# Mô hình local chạy trên CPU/GPU máy dev có thể mất hàng chục giây cho prompt dài
# (prompt gợi ý thực đơn kèm danh mục nguyên liệu đo được 79–130s ở phía Gemini).
AI_TIMEOUT_SECONDS = int(os.getenv("SFMS_AI_TIMEOUT_SECONDS", "180"))


def _generate_openai_compatible(formatted_prompt: str) -> Optional[Dict[str, Any]]:
    """
    Gọi máy chủ nói giao thức OpenAI. Dùng `requests` (đã có sẵn trong requirements)
    thay vì thêm SDK — thân request chỉ có vài trường, không đáng thêm phụ thuộc.

    Trả `None` khi gọi hỏng, để lớp trên rơi về nhánh dự phòng CHUNG (và ghi log rõ lý do).
    """
    if not AI_BASE_URL or not AI_MODEL:
        logger.error(
            "SFMS_AI_PROVIDER=openai nhưng THIẾU cấu hình: SFMS_AI_BASE_URL=%r, SFMS_AI_MODEL=%r",
            AI_BASE_URL, AI_MODEL,
        )
        return None
    import requests
    try:
        resp = requests.post(
            f"{AI_BASE_URL}/chat/completions",
            headers={"Authorization": f"Bearer {AI_API_KEY}", "Content-Type": "application/json"},
            json={"model": AI_MODEL, "messages": [{"role": "user", "content": formatted_prompt}]},
            timeout=AI_TIMEOUT_SECONDS,
        )
        if resp.status_code >= 400:
            # In THÂN lỗi chứ không chỉ mã: LM Studio trả lý do cụ thể (vượt ngữ cảnh, sai tên
            # model, model chưa nạp) trong body. Chỉ log "400 Bad Request" là bắt người sửa
            # phải đoán — đúng kiểu log đã làm mất thời gian ở lỗi GeminiClient.
            logger.error("Máy chủ %s trả %s: %s", AI_BASE_URL, resp.status_code, resp.text[:500])
            return None
        text = (resp.json()["choices"][0]["message"].get("content") or "").strip()
        if not text:
            # Model suy luận xong nhưng trả rỗng — báo rõ, đừng để rơi vào stub im lặng.
            logger.error("Máy chủ %s trả nội dung RỖNG cho model %s", AI_BASE_URL, AI_MODEL)
            return None
        logger.info("RAG answer generated successfully using model %s (%s)", AI_MODEL, AI_BASE_URL)
        return {"response_text": text, "model": AI_MODEL, "sources": []}
    except Exception as e:
        # Ghi ĐỦ để sửa được ngay: sai URL, sai tên model, LM Studio chưa bật đều lộ ở đây.
        logger.error("Gọi %s/chat/completions (model=%s) HỎNG: %s", AI_BASE_URL, AI_MODEL, e)
        return None

SYSTEM_PROMPT = """Bạn là Trợ lý AI Chuyên gia An toàn Thực phẩm & Dinh dưỡng Học đường (SFMS AI Assistant).
Nhiệm vụ của bạn là hỗ trợ Ban giám hiệu, Nhân viên Y tế, Đơn vị Bếp ăn, Giáo viên và Phụ huynh trong phạm vi
an toàn thực phẩm, dinh dưỡng học đường, kiểm thực/lưu mẫu, dị ứng thực phẩm, và vận hành bếp ăn trường học.

RÀNG BUỘC BẮT BUỘC:
1. Tuân thủ nghiêm ngặt Luật An toàn Thực phẩm 2010, Nghị định 15/2018/NĐ-CP, và Quy định lưu mẫu thực phẩm tối thiểu 24 giờ (Điều 8.2).
2. Bảo vệ dữ liệu cá nhân học sinh (PDPD): Tuyệt đối KHÔNG thu thập hoặc hiển thị thông tin nhận dạng cá nhân của học sinh ngoài phạm vi đã được cung cấp sẵn trong TÀI LIỆU THAM CHIẾU (nếu có — VD dữ liệu con của chính phụ huynh đang hỏi).
3. Khi đề xuất thực đơn (MF05), bắt buộc kiểm tra các dị ứng đã khai báo và đảm bảo cân bằng calo theo tiêu chuẩn Bộ GD&ĐT.
4. Trả lời ngắn gọn, chuyên nghiệp, chính xác dựa trên tài liệu tri thức tham chiếu được cung cấp.
5. GIỚI HẠN PHẠM VI (bắt buộc, không có ngoại lệ): CHỈ trả lời câu hỏi thuộc phạm vi an toàn thực phẩm, dinh
   dưỡng học đường, kiểm thực ba bước, lưu mẫu, dị ứng/bệnh lý liên quan tới ăn uống của học sinh, hoặc vận
   hành bếp ăn/nhà cung cấp của trường. Nếu câu hỏi NẰM NGOÀI phạm vi này (VD: hỏi bài tập về nhà, lập trình,
   thời sự, chuyện riêng tư không liên quan bữa ăn...), TỪ CHỐI lịch sự bằng đúng 1-2 câu, nói rõ đây là trợ
   lý chuyên biệt về an toàn thực phẩm học đường và không trả lời câu hỏi ngoài phạm vi đó — KHÔNG cố trả lời
   một phần, KHÔNG suy đoán, KHÔNG dùng kiến thức nền ngoài phạm vi để trả lời cho vui.

TÀI LIỆU TRI THỨC THAM CHIẾU:
{context}

CÂU HỎI / YÊU CẦU NGƯỜI DÙNG:
{user_prompt}
"""

# Dynamic client initialization using official google-genai SDK
genai_client = None

def get_genai_client():
    global genai_client
    if genai_client is None:
        api_key = os.getenv("GEMINI_API_KEY", "")
        if api_key:
            try:
                from google import genai
                genai_client = genai.Client(api_key=api_key)
                logger.info(f"Successfully initialized Google GenAI Client with model {DEFAULT_GEMINI_MODEL}")
            except Exception as e:
                logger.warning(f"Failed to initialize google-genai client: {e}")
    return genai_client


DEFAULT_GEMINI_EMBED_MODEL = "text-embedding-004"
AI_EMBED_MODEL = os.getenv("SFMS_AI_EMBED_MODEL", "").strip()

# 768 chiều cho CẢ HAI nhà cung cấp đang dùng (Gemini text-embedding-004, LM Studio
# nomic-embed-text-v1.5) — khớp `vector(768)` ở migration V59. Đổi sang mô hình khác chiều thì
# phải migration lại cột VÀ sinh lại toàn bộ embedding, nên kiểm ngay ở đây thay vì để Postgres
# từ chối từng dòng lúc ghi.
EMBED_DIMENSIONS = 768


def _apply_task_prefix(texts: List[str], model: str, task: str) -> List[str]:
    """
    `nomic-embed-text` được HUẤN LUYỆN với tiền tố nhiệm vụ và sẽ cho vector kém hơn hẳn nếu
    thiếu: câu hỏi phải mang `search_query:`, đoạn tài liệu phải mang `search_document:`.

    <p>Đo 09/08/2026: không có tiền tố, câu "Kiểm thực ba bước gồm những bước nào?" truy hồi ra
    đoạn nói về kiểm tra hàng NHẬP KHẨU — tệ hơn cả truy hồi bằng từ khoá.

    <p>Mô hình khác (Gemini `text-embedding-004`) không dùng quy ước này nên bỏ qua.
    """
    if "nomic" not in model.lower():
        return texts
    prefix = "search_query: " if task == "query" else "search_document: "
    return [prefix + t for t in texts]


def embed_texts(texts: List[str], task: str = "document") -> Dict[str, Any]:
    """
    Sinh vector nhúng cho một loạt đoạn văn bản.

    <p>Trả `{"vectors": [[...], ...], "model": "..."}`; ném RuntimeError khi không gọi được —
    caller (Java) tự quyết định nạp tài liệu mà chưa có embedding hay báo lỗi.
    """
    if not texts:
        return {"vectors": [], "model": ""}

    if AI_PROVIDER == "openai":
        if not AI_BASE_URL:
            raise RuntimeError("SFMS_AI_PROVIDER=openai nhưng thiếu SFMS_AI_BASE_URL")
        model = AI_EMBED_MODEL or "text-embedding-nomic-embed-text-v1.5"
        import requests
        resp = requests.post(
            f"{AI_BASE_URL}/embeddings",
            headers={"Authorization": f"Bearer {AI_API_KEY}", "Content-Type": "application/json"},
            json={"model": model, "input": _apply_task_prefix(texts, model, task)},
            timeout=AI_TIMEOUT_SECONDS,
        )
        if resp.status_code >= 400:
            raise RuntimeError(f"{AI_BASE_URL}/embeddings trả {resp.status_code}: {resp.text[:300]}")
        # LM Studio trả đúng khuôn OpenAI nhưng KHÔNG bảo đảm thứ tự — sắp lại theo `index`,
        # lệch thứ tự là gán nhầm vector cho đoạn, một lỗi im lặng rất khó thấy về sau.
        data = sorted(resp.json()["data"], key=lambda d: d.get("index", 0))
        vectors = [d["embedding"] for d in data]
    else:
        client = get_genai_client()
        if client is None:
            raise RuntimeError("Chưa có GEMINI_API_KEY để sinh embedding")
        model = AI_EMBED_MODEL or DEFAULT_GEMINI_EMBED_MODEL
        result = client.models.embed_content(model=model, contents=texts)
        vectors = [list(e.values) for e in result.embeddings]

    for v in vectors:
        if len(v) != EMBED_DIMENSIONS:
            raise RuntimeError(
                f"Mô hình {model} trả vector {len(v)} chiều, schema yêu cầu {EMBED_DIMENSIONS} "
                f"(vector(768) ở V59). Đổi mô hình hoặc migration lại cột."
            )
    logger.info("Đã sinh %d vector bằng %s", len(vectors), model)
    return {"vectors": vectors, "model": model}


def generate_rag_answer(prompt: str, module: str = "MF12_CHAT", context_docs: Optional[List[str]] = None) -> Dict[str, Any]:
    """
    Sinh câu trả lời RAG sử dụng Gemini 2.0 Flash + Context Tri thức.
    """
    context_str = "\n---\n".join(context_docs) if context_docs else "Không có tài liệu tham chiếu bổ sung."
    formatted_prompt = SYSTEM_PROMPT.format(context=context_str, user_prompt=prompt)
    
    # Nhà cung cấp local/OpenAI-compatible: thử TRƯỚC và không rơi sang Gemini khi hỏng —
    # đã chọn chạy local thì phải thấy nó hỏng, chứ không phải âm thầm đốt quota Gemini.
    if AI_PROVIDER == "openai":
        result = _generate_openai_compatible(formatted_prompt)
        if result:
            return result
        logger.error("Provider 'openai' hỏng — trả phản hồi dự phòng, KHÔNG tự chuyển sang Gemini.")
        return _fallback(prompt, context_docs, reason=f"khong goi duoc {AI_BASE_URL}")

    client = get_genai_client()
    if client:
        # Danh sách model ưu tiên: DEFAULT_GEMINI_MODEL (gemini-3.5-flash) -> gemma-4-26b-a4b-it (free fallback) -> gemma-4-31b-it
        models_to_try = [DEFAULT_GEMINI_MODEL, "gemma-4-26b-a4b-it", "gemma-4-31b-it"]
        for target_model in models_to_try:
            try:
                response = client.models.generate_content(
                    model=target_model,
                    contents=formatted_prompt
                )
                if response and response.text:
                    logger.info(f"RAG answer generated successfully using model {target_model}")
                    return {
                        "response_text": response.text,
                        "model": target_model,
                        "sources": context_docs or []
                    }
            except Exception as e:
                logger.warning(f"API call for model {target_model} failed: {e}. Trying next model...")

    return _fallback(prompt, context_docs, reason="khong co GEMINI_API_KEY hoac moi model deu hong")


def _fallback(prompt: str, context_docs: Optional[List[str]], reason: str) -> Dict[str, Any]:
    """
    Phản hồi dự phòng (ADR-000 Stub) — KHÔNG phải câu trả lời thật.

    <p>Nói rõ LÝ DO ngay trong `model` và log ở mức ERROR. Trước 09/08/2026 nhánh này log ở
    mức INFO và trả một chuỗi mẫu trông như câu trả lời thật, nên việc `GeminiClient` cắm cứng
    sai cổng đã sống sót rất lâu: nhìn màn hình thấy "có trả lời", nhìn log thấy "bình thường".
    """
    logger.error("Dùng phản hồi DỰ PHÒNG (không phải trả lời thật) — lý do: %s", reason)
    fallback_text = (
        f"[SFMS AI Assistant — PHẢN HỒI DỰ PHÒNG, không phải câu trả lời của mô hình] "
        f"Không gọi được mô hình ({reason}). Yêu cầu đã nhận: '{prompt}'.\n"
        f"Thông tin nền: theo Điều 8.2, mẫu thực phẩm phải lưu ít nhất 24 giờ ở 0–4°C kèm niêm phong."
    )
    return {
        "response_text": fallback_text,
        "model": f"{AI_MODEL or DEFAULT_GEMINI_MODEL}-stub",
        "sources": context_docs or ["Điều 8.2 - Quy định lưu mẫu thực phẩm 24h"]
    }
