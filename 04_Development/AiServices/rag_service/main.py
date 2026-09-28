import time
import logging
from typing import List, Optional
from fastapi import FastAPI, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from pydantic import BaseModel, Field
from rag_chain import generate_rag_answer, embed_texts, DEFAULT_GEMINI_MODEL

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("rag_service")

app = FastAPI(
    title="SFMS RAG Service (Gemini 2.0 Flash & LangChain)",
    description="Microservice Trợ lý AI & RAG cho Hệ thống Quản lý An toàn Thực phẩm Học đường SFMS",
    version="2.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# In-memory Knowledge Base Store (Pre-loaded regulations & guidelines)
KNOWLEDGE_BASE: List[str] = [
    "Quy định lưu mẫu thực phẩm (Điều 8.2): Thực phẩm phục vụ từ 30 suất ăn trở lên phải lưu mẫu ít nhất 24 giờ. Dung lượng mẫu tối thiểu 100g với thức ăn khô, 150ml với thức ăn lỏng.",
    "Quy trình xử lý sự cố ngộ độc thực phẩm: Lập tức cách ly lô thực phẩm nghi ngờ, báo cáo Ban Giám hiệu và Trạm Y tế phường/xã, thực hiện niêm phong mẫu thực phẩm trong vòng 1 giờ.",
    "Quy định về dị ứng thực phẩm học sinh (BR-123): Khi quét mã ID học sinh nhận suất ăn, hệ thống bắt buộc cảnh báo thời gian thực nếu thực đơn chứa dị ứng của học sinh.",
    "Tiêu chuẩn vệ sinh bếp ăn (BR-104): Điểm đánh giá kiểm tra bếp ăn hàng ngày phải đạt từ 80/100 trở lên mới được phép vận hành nấu ăn."
]


class ChatRequest(BaseModel):
    conversation_id: Optional[str] = Field(None, description="UUID của hội thoại")
    user_id: Optional[str] = Field(None, description="ID người dùng")
    module: str = Field("MF12_CHAT", description="Module gọi: MF12_CHAT hoặc MF05_MENU")
    prompt: str = Field(..., description="Câu hỏi hoặc yêu cầu từ người dùng")
    school_id: Optional[str] = Field(None, description="ID Trường học")
    # Kho tri thức bền vững (Postgres, System Admin quản trị qua `POST /api/v1/ai/knowledge-documents`)
    # do backend Java gửi kèm — ưu tiên hơn khớp từ khoá trong KNOWLEDGE_BASE tĩnh nội bộ khi có.
    context_docs: Optional[List[str]] = Field(None, description="Tài liệu tri thức do caller cung cấp sẵn")


class IngestRequest(BaseModel):
    category: str = Field(..., description="Phân loại tri thức: ATTP_REGULATION | NUTRITION_STANDARD")
    content: str = Field(..., description="Nội dung tri thức")


@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "SFMS RAG Service",
        "model": DEFAULT_GEMINI_MODEL,
        "sdk": "google-genai",
        "knowledge_chunks_count": len(KNOWLEDGE_BASE)
    }


class EmbedRequest(BaseModel):
    texts: List[str] = Field(..., description="Các đoạn văn bản cần sinh vector nhúng")
    # "query" cho câu hỏi, "document" cho đoạn tài liệu — nomic-embed cần tiền tố khác nhau cho
    # hai loại này, dùng sai thì truy hồi kém hẳn (xem _apply_task_prefix).
    task: str = Field("document", description='"query" | "document"')


@app.post("/api/v1/rag/embed")
def rag_embed(req: EmbedRequest):
    """
    Sinh vector nhúng cho backend Java lưu vào `ai_knowledge_chunks.embedding` (pgvector).

    <p>Đặt ở đây chứ không ở Java vì service này đã là nơi DUY NHẤT biết đang dùng nhà cung cấp
    nào (Gemini hay OpenAI-compatible) — tách ra hai nơi là chắc chắn có ngày lệch cấu hình.
    """
    if not req.texts:
        return {"vectors": [], "model": "", "dimensions": 0}
    try:
        result = embed_texts(req.texts, req.task)
    except Exception as e:
        # 503 chứ không phải 500: đây là phụ thuộc ngoài đang không dùng được, không phải lỗi
        # dữ liệu của caller. Java dựa vào đó để quyết định vẫn lưu tài liệu (embedding NULL).
        logger.error("Sinh embedding HỎNG: %s", e)
        raise HTTPException(status_code=status.HTTP_503_SERVICE_UNAVAILABLE, detail=str(e))
    return {
        "vectors": result["vectors"],
        "model": result["model"],
        "dimensions": len(result["vectors"][0]) if result["vectors"] else 0,
    }


@app.post("/api/v1/rag/chat")
def rag_chat(req: ChatRequest):
    """
    Endpoint RAG Chatbot & Gợi ý thực đơn (Gemini 2.0 Flash).
    """
    if not req.prompt or not req.prompt.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Prompt không được để trống."
        )

    start_time = time.time()

    if req.context_docs:
        # Caller (Java backend) đã gửi sẵn tài liệu tri thức bền vững — ưu tiên tuyệt đối,
        # không tự khớp từ khoá trong KNOWLEDGE_BASE tĩnh nội bộ nữa.
        context_docs = req.context_docs
    else:
        # Fallback: chưa có kho tri thức nào active (System Admin chưa thêm gì) — dùng
        # KNOWLEDGE_BASE tĩnh + khớp từ khoá đơn giản như trước.
        prompt_lower = req.prompt.lower()
        matching_docs = [doc for doc in KNOWLEDGE_BASE if any(kw in doc.lower() for kw in prompt_lower.split() if len(kw) > 3)]
        context_docs = matching_docs if matching_docs else KNOWLEDGE_BASE[:2]

    rag_result = generate_rag_answer(
        prompt=req.prompt,
        module=req.module,
        context_docs=context_docs
    )
    
    latency_ms = int((time.time() - start_time) * 1000)
    
    logger.info(f"Processed RAG request for module={req.module} in {latency_ms}ms")
    return {
        "response_text": rag_result["response_text"],
        "model": rag_result["model"],
        "sources": rag_result["sources"],
        "latency_ms": latency_ms
    }


@app.post("/api/v1/rag/ingest")
def ingest_knowledge(req: IngestRequest):
    """
    Endpoint Nạp Tri thức mới vào Knowledge Base.
    """
    if not req.content or not req.content.strip():
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Nội dung tri thức không được để trống."
        )
    
    KNOWLEDGE_BASE.append(req.content.strip())
    logger.info(f"Ingested new knowledge chunk under category={req.category}")
    return {
        "success": True,
        "message": f"Nạp tri thức thành công vào danh mục {req.category}.",
        "total_chunks": len(KNOWLEDGE_BASE)
    }
