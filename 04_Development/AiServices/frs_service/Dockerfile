# 11/08/2026 — chạm file để kích hoạt lần build CI/CD đầu tiên thật sự.
FROM python:3.11-slim

WORKDIR /app

RUN apt-get update && apt-get install -y \
    libgl1 \
    libglib2.0-0 \
    && rm -rf /var/lib/apt/lists/*

COPY requirements.txt .
# tensorflow (kéo theo bởi deepface) là wheel ~570MB — timeout mặc định của pip (~15s không có
# dữ liệu mới) hay bị kích hoạt trên mạng chậm/không ổn định. Tăng timeout + retry, không phải
# giảm dung lượng cài — dlib/deepface cần đúng bản này.
RUN pip install --no-cache-dir --default-timeout=180 --retries 10 -r requirements.txt

COPY . .

EXPOSE 8000

CMD ["uvicorn", "main:app", "--host", "0.0.0.0", "--port", "8000"]
