# Manual Web E2E Test — MF12 AI & Supporting Services

> Manual testing on a real browser, no direct API calls. Same format as `02_IT/sample.md`.

---

## Feature: AI Chat Assistant (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | AI Chat Assistant (Manual Web) |
| Test requirement | Ask the AI assistant a question and receive a grounded, in-scope response on `/ai-chat`, verify out-of-scope refusal, multi-turn context, role-aware greeting, and error handling. |
| Number of TCs | 5 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 5 | 0 |
| Round 2 | 0 | 0 | 5 | 0 |
| Round 3 | 0 | 0 | 5 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Ask an in-scope question** | | | | | | | | | | | | | | |
| MF12-WEB-001 | Ask the AI assistant a food-safety/nutrition question and get a grounded response. | 1. Log in (any role with `/ai-chat` access), open `/ai-chat`.<br>2. Type a question such as "quy định lưu mẫu thực phẩm 24h là gì?" in the input field.<br>3. Click "Gửi". | User message appears in the chat bubble on the right. A loading indicator ("Đang truy vấn…") shows briefly. An AI reply bubble appears on the left, containing an answer grounded in the active knowledge base documents, with a timestamp. | Logged in, at least one ACTIVE knowledge document exists covering the topic asked. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-002 | Ask a question outside the food-safety/nutrition school scope. | 1. On `/ai-chat`, type an off-topic question (e.g. "hôm nay thời tiết thế nào?").<br>2. Click "Gửi". | The AI politely declines/redirects, stating it only supports food safety and school nutrition topics — no crash, no generic off-topic answer. | Logged in. | Pending | | | Pending | | | Pending | | | Refusal is enforced by the AI's own scope, not a UI-side filter |
| **Conversation continuity** | | | | | | | | | | | | | | |
| MF12-WEB-003 | Ask a follow-up question that depends on the previous answer (multi-turn). | 1. Ask a first question, wait for the reply.<br>2. Ask a short follow-up referring back to it (e.g. "vậy còn với sữa thì sao?") without repeating context. | The second reply reflects the context of the first exchange (same conversation), rather than starting a fresh, disconnected answer. | Logged in, step 1 already answered successfully. | Pending | | | Pending | | | Pending | | | Continuity is carried by `conversationId`, not resent by the client each time |
| **Role-aware greeting** | | | | | | | | | | | | | | |
| MF12-WEB-004 | Open the chat as a Parent and confirm the greeting mentions their child. | 1. Log in as a Parent account.<br>2. Open `/ai-chat`. | The first AI message in the thread differs from other roles — it explicitly mentions being able to help with allergies or the child's recent meals, in addition to general food-safety rules. | Logged in as Parent. | Pending | | | Pending | | | Pending | | | |
| **Error handling** | | | | | | | | | | | | | | |
| MF12-WEB-005 | Send a message while the AI backend is unreachable/errors out. | 1. Simulate/observe a backend failure (e.g. stop the backend, or ask during an outage window).<br>2. Type a question, click "Gửi". | The user's message still appears in the thread. An AI-side error bubble appears with a clear "cannot connect to the assistant" message — the page does not crash or hang indefinitely. | Backend unreachable or forced to error. | Pending | | | Pending | | | Pending | | | |

---

## Feature: AI Knowledge Base Management (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | AI Knowledge Base Management (Manual Web) |
| Test requirement | System Admin manages the AI knowledge base at `/system-admin/ai-knowledge`: add documents by typing content or uploading a file, toggle active/inactive, delete, and view long content. |
| Number of TCs | 7 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 7 | 0 |
| Round 2 | 0 | 0 | 7 | 0 |
| Round 3 | 0 | 0 | 7 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Add document (typed content)** | | | | | | | | | | | | | | |
| MF12-WEB-006 | Add a knowledge document by typing the title, category, and content directly. | 1. Log in as System Admin, go to `/system-admin/ai-knowledge`.<br>2. Click "Thêm tài liệu".<br>3. Fill in Title, pick a Category (Quy định ATTP / Tiêu chuẩn dinh dưỡng / Chính sách trường học / Khác), fill in Content, leave the file field empty.<br>4. Click "Lưu tài liệu". | Dialog closes, a success toast confirms the document is saved and will be used by the AI from the next question. The new document appears in the list, marked "Đang dùng" (active) by default. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-007 | Try to save with Title or Content left empty. | 1. Open "Thêm tài liệu".<br>2. Leave Title or Content blank, click "Lưu tài liệu". | Inline error "Tiêu đề và nội dung là bắt buộc." — dialog stays open, nothing is saved. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |
| **Add document (file upload)** | | | | | | | | | | | | | | |
| MF12-WEB-008 | Upload a valid document file instead of typing content. | 1. Open "Thêm tài liệu".<br>2. Pick a `.pdf`/`.docx`/`.txt`/`.md`/`.csv` file under 10MB in the "Nạp từ file" field — note the Content textarea disappears once a file is chosen.<br>3. Click "Nạp "<filename>"". | Button shows "Đang bóc văn bản…" while processing, then a success toast confirms the file was loaded and the AI will use it from the next question. The document appears in the list with the extracted text as its content. | Logged in as System Admin, a sample text-bearing PDF/DOCX file available. | Pending | | | Pending | | | Pending | | | Server extracts text (PDFBox/POI) — not parsed in the browser |
| MF12-WEB-009 | Upload a scanned (image-only) PDF that has no extractable text. | 1. Open "Thêm tài liệu".<br>2. Pick a scanned/image-only PDF.<br>3. Click to submit. | Rejected with a clear error that text could not be extracted from the file — no document is created. | Logged in as System Admin, a scanned/image-only PDF available. | Pending | | | Pending | | | Pending | | | |
| **Manage existing documents** | | | | | | | | | | | | | | |
| MF12-WEB-010 | Toggle a document between active and inactive. | 1. On the list, find an active document, click "Tắt".<br>2. Observe the status badge and toast.<br>3. Click "Bật" to re-enable it. | After step 1: badge changes to "Inactive", toast confirms the AI will no longer use this document. After step 3: badge returns to "Active", toast confirms the AI will use it again from the next question. | At least one document exists. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-011 | Delete a document with the confirmation dialog. | 1. Click the trash icon on a document.<br>2. Confirm the document title shown in the confirmation dialog matches, click "Xoá". | A confirmation dialog appears, warning the action cannot be undone. After confirming, the document disappears from the list and a success toast shows. | At least one document exists that is safe to delete. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-012 | View a long document's content — preview truncation and expand/collapse. | 1. Open a document whose content is longer than 600 characters.<br>2. Observe the initial preview and character count.<br>3. Click "Xem toàn văn", then "Thu gọn". | Initially shows the first ~600 characters plus "…" and the total extracted character count. "Xem toàn văn" expands to the full content in a scrollable box; "Thu gọn" collapses it back. | A document with content longer than 600 characters exists. | Pending | | | Pending | | | Pending | | | |

---

## Feature: Audit Log Viewing & Filtering (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | Audit Log Viewing & Filtering (Manual Web) |
| Test requirement | System Admin views and filters the immutable audit trail at `/system-admin/audit-logs` — no edit/delete controls exist anywhere on the page. |
| Number of TCs | 6 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 6 | 0 |
| Round 2 | 0 | 0 | 6 | 0 |
| Round 3 | 0 | 0 | 6 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **View audit log list** | | | | | | | | | | | | | | |
| MF12-WEB-013 | View the audit log table with all expected columns. | 1. Log in as System Admin, go to `/system-admin/audit-logs`. | Table shows columns: Mã, Thời điểm, Người thực hiện, Địa chỉ IP, Hành động, Tài nguyên, Kết quả (badge "Thành công"/"Bị chặn"). No row has an edit/delete action — the table is read-only. | Logged in as System Admin, audit rows exist. | Pending | | | Pending | | | Pending | | | Confirms UI-level immutability (BR-162) — no edit/delete control anywhere on the page |
| **Filtering** | | | | | | | | | | | | | | |
| MF12-WEB-014 | Filter by date range (From/To). | 1. Set "Từ ngày" and "Đến ngày" to a known range containing at least one entry.<br>2. Observe the table. | Only rows whose Thời điểm falls within the selected range (inclusive of the whole "Đến ngày" day) are shown. | Audit rows spanning multiple dates exist. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-015 | Filter by IP address. | 1. Enter a known IP in "Địa chỉ IP".<br>2. Observe the table. | Only rows matching that exact IP are shown. | An audit row with a known IP exists. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-016 | Filter by Result (Tất cả / SUCCESS). | 1. Change "Kết quả" from "Tất cả" to "SUCCESS".<br>2. Observe the table. | Only rows with a "Thành công" badge remain; switching back to "Tất cả" restores the full list. | Audit rows with both SUCCESS and BLOCKED results exist. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-017 | Free-text search by actor, action, or resource keyword. | 1. Type a keyword (e.g. a known actor's name or "LOGIN") into the table's search box.<br>2. Observe the filtered results. | Only rows where the actor, action, or resource contains the keyword (case-insensitive) are shown. | Audit rows containing the searched keyword exist. | Pending | | | Pending | | | Pending | | | |
| **Export** | | | | | | | | | | | | | | |
| MF12-WEB-018 | Click "Xuất nhật ký" (export). | 1. On `/system-admin/audit-logs`, click "Xuất nhật ký". | An informational toast about the Log Retention Policy is shown; no destructive or editing action is triggered by this button. | Logged in as System Admin. | Pending | | | Pending | | | Pending | | | |

---

## Feature: System Monitoring Dashboard (Web)

### 1. General Information

| Field | Value |
|---|---|
| Feature | System Monitoring Dashboard (Manual Web) |
| Test requirement | System Admin views real-time and historical system metrics (CPU, heap, DB connections, uptime) at `/system-admin/monitoring`. |
| Number of TCs | 3 |

### 2. Testing Round Summary

| Testing Round | Passed | Failed | Pending | N/A |
|---|---|---|---|---|
| Round 1 | 0 | 0 | 3 | 0 |
| Round 2 | 0 | 0 | 3 | 0 |
| Round 3 | 0 | 0 | 3 | 0 |

### 3. Test Cases

| Test Case ID | Test Case Description | Test Case Procedure | Expected Results | Pre-conditions | Round 1 | Test date | Tester | Round 2 | Test date | Tester | Round 3 | Test date | Tester | Note |
|---|---|---|---|---|---|---|---|---|---|---|---|---|---|---|
| **Current metrics** | | | | | | | | | | | | | | |
| MF12-WEB-019 | View the current real-time metric stat cards. | 1. Log in as System Admin, go to `/system-admin/monitoring`. | Three stat cards show: "CPU hiện tại" (percentage), "Heap JVM" (used/max MB), "Uptime backend" (formatted as days/hours or hours/minutes) — all populated with non-placeholder values shortly after the page loads. | Logged in as System Admin, backend running. | Pending | | | Pending | | | Pending | | | |
| **Historical charts** | | | | | | | | | | | | | | |
| MF12-WEB-020 | View the 7-day history mini bar charts. | 1. On `/system-admin/monitoring`, observe the three chart cards: "Tải CPU (%)", "Heap dùng (%)", "Kết nối DB đang mở". | Each card renders one bar per 15-minute snapshot over the last 7 days, with a hover tooltip showing the value, and a caption noting the 15-minute snapshot interval. | Backend has been running long enough to have accumulated snapshot history. | Pending | | | Pending | | | Pending | | | |
| MF12-WEB-021 | View the dashboard on a freshly started backend with no snapshot history yet. | 1. Open `/system-admin/monitoring` right after the backend starts (before the first 15-minute snapshot). | The current-metric stat cards still populate immediately. The three history charts show an empty-state message noting no data yet and that the first snapshot needs at least 15 minutes after backend startup. | Backend started less than 15 minutes ago. | Pending | | | Pending | | | Pending | | | |
