import io
import os
import uuid
import logging
import numpy as np
import psycopg2
from psycopg2.pool import SimpleConnectionPool
from typing import Optional
from fastapi import FastAPI, UploadFile, File, Form, HTTPException, status
from fastapi.middleware.cors import CORSMiddleware
from PIL import Image

logging.basicConfig(level=logging.INFO, format="%(asctime)s [%(levelname)s] %(name)s: %(message)s")
logger = logging.getLogger("frs_service")

app = FastAPI(
    title="SFMS Driver Face Recognition Service",
    description="Microservice cho xác thực sinh trắc học khuôn mặt tài xế (PDPD Compliant - DeepFace Engine)",
    version="1.0.0"
)

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Persist vào Postgres (10/08/2026) — trước đây `FACE_EMBEDDINGS_DB: Dict[str, np.ndarray] = {}`
# chỉ giữ trong RAM tiến trình: MỌI lần container restart xoá sạch đăng ký, verify() 404 cho
# tới khi đăng ký lại tài xế. Chỉ lưu VECTOR (float32[]) — KHÔNG lưu ảnh gốc (PDPD Compliance,
# giữ nguyên tinh thần cũ). Bảng do CHÍNH service này sở hữu/tạo (KHÔNG qua Flyway của backend
# Java) — Java chỉ gọi HTTP API của service này, không bao giờ đọc thẳng bảng này.
DATABASE_URL = os.environ.get("DATABASE_URL", "")
_pool: Optional[SimpleConnectionPool] = None


def _get_pool() -> SimpleConnectionPool:
    global _pool
    if _pool is None:
        if not DATABASE_URL:
            raise RuntimeError("DATABASE_URL chưa cấu hình — không lưu được dữ liệu khuôn mặt bền vững.")
        _pool = SimpleConnectionPool(1, 5, DATABASE_URL)
    return _pool


def _ensure_table() -> None:
    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn, conn.cursor() as cur:
            cur.execute("""
                CREATE TABLE IF NOT EXISTS driver_face_embeddings (
                    face_id     TEXT PRIMARY KEY,
                    embedding   REAL[] NOT NULL,
                    created_at  TIMESTAMPTZ NOT NULL DEFAULT now()
                )
            """)
    finally:
        pool.putconn(conn)


def _save_embedding(face_id: str, vector: np.ndarray) -> None:
    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn, conn.cursor() as cur:
            cur.execute(
                "INSERT INTO driver_face_embeddings (face_id, embedding) VALUES (%s, %s)",
                (face_id, vector.astype(np.float32).tolist()),
            )
    finally:
        pool.putconn(conn)


def _load_embedding(face_id: str) -> Optional[np.ndarray]:
    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn, conn.cursor() as cur:
            cur.execute("SELECT embedding FROM driver_face_embeddings WHERE face_id = %s", (face_id,))
            row = cur.fetchone()
            return np.array(row[0], dtype=np.float32) if row else None
    finally:
        pool.putconn(conn)


def _delete_embedding(face_id: str) -> bool:
    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn, conn.cursor() as cur:
            cur.execute("DELETE FROM driver_face_embeddings WHERE face_id = %s", (face_id,))
            return cur.rowcount > 0
    finally:
        pool.putconn(conn)


def _count_embeddings() -> int:
    pool = _get_pool()
    conn = pool.getconn()
    try:
        with conn, conn.cursor() as cur:
            cur.execute("SELECT count(*) FROM driver_face_embeddings")
            return cur.fetchone()[0]
    finally:
        pool.putconn(conn)


@app.on_event("startup")
def on_startup():
    _ensure_table()
    logger.info("driver_face_embeddings table ready (Postgres-backed, survives restarts).")

# Try importing DeepFace
DEEPFACE_AVAILABLE = False
try:
    from deepface import DeepFace
    DEEPFACE_AVAILABLE = True
    logger.info("DeepFace engine loaded successfully.")
except Exception as e:
    logger.warning(f"DeepFace failed to load (will use fallback feature extractor): {e}")


class NoFaceDetectedError(Exception):
    """Ảnh không có khuôn mặt nào — phải TỪ CHỐI, không âm thầm nhúng ảnh rác (BR-050/051)."""
    pass


def extract_face_vector(image_bytes: bytes) -> np.ndarray:
    """Trích xuất Vector đặc trưng (Embedding) từ ảnh mặt — raise NoFaceDetectedError nếu ảnh
    không chứa khuôn mặt (DeepFace thật đang chạy)."""
    if DEEPFACE_AVAILABLE:
        # detector_backend="opencv" (mặc định) LUÔN lỗi — bản opencv-python cài qua pip thiếu
        # file haarcascade_frontalface_default.xml ở path DeepFace mong đợi (lỗi đóng gói phổ
        # biến của package). "mtcnn"/"retinaface" dùng neural network, không phụ thuộc file đó,
        # và ĐÚNG NGHĨA: raise lỗi rõ khi ảnh không có mặt (enforce_detection=True) — không dùng
        # detector_backend="skip" (bản trước) vì "skip" nhúng thẳng ảnh bất kể có mặt hay không,
        # verify() thành vô nghĩa với ảnh rác.
        img = Image.open(io.BytesIO(image_bytes)).convert("RGB")
        img_np = np.array(img)
        try:
            results = DeepFace.represent(img_np, model_name="VGG-Face", detector_backend="mtcnn",
                                          enforce_detection=True)
        except ValueError as err:
            raise NoFaceDetectedError(str(err)) from err
        embedding = np.array(results[0]["embedding"], dtype=np.float32)
        norm = np.linalg.norm(embedding)
        if not np.isfinite(norm) or norm == 0:
            raise NoFaceDetectedError("Embedding không hợp lệ (norm=0/inf).")
        return embedding / norm

    # DeepFace KHÔNG load được (import lỗi, xem log khởi động) — dev/test fallback, KHÔNG phải
    # sinh trắc học thật. Bug đã fix: đọc thẳng byte hash làm bit pattern float32
    # (`np.frombuffer(..., dtype=np.float32)`) ra giá trị cực lớn (~1e38), bình phương để tính
    # norm TRÀN SỐ thành `inf` → vec / inf = toàn 0 → verify luôn "không khớp" bất kể ảnh nào.
    # Sửa: đưa byte về [0,1] trước (uint8 / 255) rồi mới thành vector, không bao giờ tràn số.
    import hashlib
    hash_digest = hashlib.sha256(image_bytes).digest()
    raw = np.frombuffer((hash_digest * 4)[:128], dtype=np.uint8).astype(np.float32) / 255.0
    norm = np.linalg.norm(raw)
    return raw / (norm + 1e-10)


@app.get("/health")
def health_check():
    return {
        "status": "UP",
        "service": "SFMS Face Recognition Service",
        "deepface_available": DEEPFACE_AVAILABLE,
        "registered_faces_count": _count_embeddings()
    }


@app.post("/api/v1/frs/enroll")
async def enroll_face(file: UploadFile = File(...)):
    """
    Đăng ký sinh trắc học khuôn mặt tài xế.
    Chỉ lưu Vector đặc trưng, KHÔNG lưu ảnh gốc (PDPD Compliance).
    """
    if not file.content_type or not file.content_type.startswith("image/"):
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="File phải ở định dạng ảnh (JPEG/PNG)."
        )
    
    image_bytes = await file.read()
    if len(image_bytes) == 0:
        raise HTTPException(
            status_code=status.HTTP_400_BAD_REQUEST,
            detail="Dữ liệu ảnh rỗng."
        )

    try:
        vector = extract_face_vector(image_bytes)
    except NoFaceDetectedError:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Không phát hiện khuôn mặt trong ảnh — vui lòng chụp lại, đảm bảo khuôn mặt rõ trong khung hình."
        )
    except Exception as e:
        logger.error(f"Enrollment failed: {e}")
        raise HTTPException(
            status_code=status.HTTP_500_INTERNAL_SERVER_ERROR,
            detail=f"Lỗi đăng ký khuôn mặt: {str(e)}"
        )

    face_id = f"face-{uuid.uuid4()}"
    _save_embedding(face_id, vector)
    logger.info(f"Enrolled face successfully: face_id={face_id}")
    return {
        "face_id": face_id,
        "status": "REGISTERED",
        "message": "Delivery staff face biometrics registered successfully."
    }


@app.post("/api/v1/frs/verify")
async def verify_face(
    captured_image: UploadFile = File(...),
    expected_face_id: str = Form(...)
):
    """
    Xác thực sinh trắc học khuôn mặt tài xế tại cổng trường khi giao nhận thực phẩm.
    """
    # ⚠️ Bản trước có nhánh "stub fallback": id không thấy trong DB nhưng bắt đầu bằng
    # "face-"/"mock-" thì TỰ ĐỘNG trả matched=true — lỗ hổng an ninh thật, đã xoá hẳn (không
    # thấy id ⇒ luôn 404, không có đường tự pass). Nay dữ liệu bền vững qua restart
    # (Postgres, `driver_face_embeddings`) nên 404 chỉ còn nghĩa đúng của nó: chưa từng đăng
    # ký / đã bị xoá (BR-051), không còn do "service vừa restart".
    stored_vector = _load_embedding(expected_face_id)
    if stored_vector is None:
        raise HTTPException(
            status_code=status.HTTP_404_NOT_FOUND,
            detail="Không tìm thấy dữ liệu khuôn mặt đã đăng ký — cần đăng ký lại."
        )

    image_bytes = await captured_image.read()
    try:
        captured_vector = extract_face_vector(image_bytes)
    except NoFaceDetectedError:
        raise HTTPException(
            status_code=status.HTTP_422_UNPROCESSABLE_ENTITY,
            detail="Không phát hiện khuôn mặt trong ảnh chụp — vui lòng thử lại."
        )

    # Cosine Similarity
    similarity = float(np.dot(captured_vector, stored_vector))
    distance = float(1.0 - similarity)
    threshold = 0.40  # Cosine distance threshold (lower is more similar)

    matched = distance <= threshold
    confidence = float(max(0.0, min(1.0, 1.0 - distance)))

    logger.info(f"Verify face_id={expected_face_id}: matched={matched}, confidence={confidence:.3f}")
    return {
        "matched": matched,
        "confidence": confidence,
        "distance": distance,
        "expected_face_id": expected_face_id
    }


@app.delete("/api/v1/frs/faces/{face_id}")
def delete_face(face_id: str):
    """
    Xóa dữ liệu sinh trắc học của tài xế (BR-051).
    """
    if _delete_embedding(face_id):
        logger.info(f"Deleted face data for face_id={face_id}")
    return {
        "success": True,
        "message": f"Biometric data for face_id {face_id} has been permanently deleted."
    }
