"""Lớp gọi API dùng chung cho script seed demo.

CHỈ dùng HTTP API thật — không chèn SQL. Mọi bản ghi sinh ra đi qua đúng endpoint mà
người dùng thật sẽ bấm, nên dữ liệu seed có đủ audit log, sinh mã, và các gate BR.
"""
from __future__ import annotations

import io
import json
import os
import sys
import urllib.error
import urllib.request
import uuid as _uuid

sys.stdout = io.TextIOWrapper(sys.stdout.buffer, encoding="utf-8", errors="replace")

BASE = os.environ.get("SFMS_API", "http://localhost/api/v1")
MAILPIT = os.environ.get("SFMS_MAILPIT", "http://localhost:8026")

_HERE = os.path.dirname(os.path.abspath(__file__))


def _load_env_file() -> None:
    """Đọc `.env.seed` cạnh script (đã bị .gitignore bắt qua mẫu `.env.*`)."""
    path = os.path.join(_HERE, ".env.seed")
    if not os.path.exists(path):
        return
    with open(path, encoding="utf-8") as fh:
        for line in fh:
            line = line.strip()
            if not line or line.startswith("#") or "=" not in line:
                continue
            k, v = line.split("=", 1)
            os.environ.setdefault(k.strip(), v.strip())


_load_env_file()


def account(env_key: str, default_email: str = "") -> tuple[str, str]:
    raw = os.environ.get(env_key, "")
    if not raw:
        raise SystemExit(
            f"Thiếu biến {env_key}. Tạo file 07_implement/seed/.env.seed theo mẫu "
            f".env.seed.example (định dạng: {env_key}=email|matkhau)."
        )
    email, password = raw.split("|", 1)
    return email.strip(), password.strip()


# ───────────────────────────── HTTP ─────────────────────────────

class ApiError(RuntimeError):
    def __init__(self, method: str, path: str, status: int, body):
        super().__init__(f"{method} {path} -> {status}: {json.dumps(body, ensure_ascii=False)[:400]}")
        self.status = status
        self.body = body


def call(method: str, path: str, token: str | None = None, body=None):
    data = json.dumps(body).encode("utf-8") if body is not None else None
    req = urllib.request.Request(BASE + path, data=data, method=method)
    if data:
        req.add_header("Content-Type", "application/json")
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read().decode("utf-8", "replace")
            return r.status, (json.loads(raw) if raw.strip() else None)
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "replace")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw


def call_multipart(method: str, path: str, token=None, parts=None, files=None):
    boundary = "----sfms" + _uuid.uuid4().hex
    payload = b""
    for name, value in (parts or {}).items():
        payload += (f"--{boundary}\r\nContent-Disposition: form-data; name=\"{name}\"\r\n"
                    f"Content-Type: application/json\r\n\r\n").encode()
        payload += json.dumps(value).encode("utf-8") + b"\r\n"
    for name, (fn, blob, ct) in (files or {}).items():
        payload += (f"--{boundary}\r\nContent-Disposition: form-data; name=\"{name}\"; "
                    f"filename=\"{fn}\"\r\nContent-Type: {ct}\r\n\r\n").encode()
        payload += blob + b"\r\n"
    payload += f"--{boundary}--\r\n".encode()
    req = urllib.request.Request(BASE + path, data=payload, method=method)
    req.add_header("Content-Type", "multipart/form-data; boundary=" + boundary)
    if token:
        req.add_header("Authorization", "Bearer " + token)
    try:
        with urllib.request.urlopen(req) as r:
            raw = r.read().decode("utf-8", "replace")
            return r.status, (json.loads(raw) if raw.strip() else None)
    except urllib.error.HTTPError as e:
        raw = e.read().decode("utf-8", "replace")
        try:
            return e.code, json.loads(raw)
        except Exception:
            return e.code, raw


# ───────────────────────────── tiện ích ─────────────────────────────

OK_CREATED = (200, 201)


def step(label: str, status: int, body, ok=OK_CREATED, tolerate=()):
    """In một dòng kết quả. `tolerate` = các mã coi như 'đã có sẵn', không phải lỗi."""
    if status in ok:
        mark = "OK  "
    elif status in tolerate:
        mark = "SKIP"
    else:
        mark = "FAIL"
    detail = ""
    if mark != "OK  ":
        detail = json.dumps(body, ensure_ascii=False)[:220] if not isinstance(body, str) else body[:220]
    print(f"  {mark} [{status}] {label} {detail}")
    if mark == "FAIL":
        raise ApiError("", label, status, body)
    return body


def login(email: str, password: str) -> str:
    st, b = call("POST", "/auth/login", body={"email": email, "password": password})
    if st != 200:
        raise ApiError("POST", "/auth/login " + email, st, b)
    return b["accessToken"]


def activation_token(email: str) -> str | None:
    """Lấy token kích hoạt từ Mailpit (backend phải trỏ SMTP về mailpit)."""
    import re
    msgs = json.load(urllib.request.urlopen(MAILPIT + "/api/v1/messages?limit=50"))["messages"]
    for m in msgs:
        if m["To"][0]["Address"].lower() != email.lower():
            continue
        body = json.load(urllib.request.urlopen(MAILPIT + "/api/v1/message/" + m["ID"]))
        txt = (body.get("HTML") or "") + (body.get("Text") or "")
        found = re.findall(r"token=([A-Za-z0-9_\-.]+)", txt)
        if found:
            return found[0]
    return None
