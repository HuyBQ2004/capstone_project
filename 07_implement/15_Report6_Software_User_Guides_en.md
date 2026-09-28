# Capstone Project Report

## Report 6 – Software User Guides

**Project:** SFMS — School Food Management System
**Group:** SEP490_G8

> **Note for Nam:** this draft is written in Markdown so it's easy to read/edit in Claude Code; copy the content + paste the screenshots into the original Word file `Report6_Software User Guides.docx` afterward. The outline (I / II.1 / II.2 / II.3.x) **keeps the exact same numbering** as the Word template — don't rename sections, so the Table of Contents doesn't shift when pasted back. Every spot that needs a screenshot is marked:
>
> 📸 **[SCREENSHOT NEEDED — description]**
>
> All routes/URLs in this document are taken from the real code (`04_Development/Frontend/src/app/(app)/`) and the real deployment topology (`docker-compose.yml` at the repository root + `nginx/nginx.conf`) as of 2026-08-12 — if the FE routes or the compose stack change later, update this file accordingly.
>
> ⚠️ `03_Design/08_FrontEnd/Web/` is an **old copy** of the Frontend and is *not* what gets built/deployed — the compose file builds `./04_Development/Frontend`. Always take screenshots from the running compose stack, not from that folder.

---

## I. Record of Changes

| Date | A\*M, D | In charge | Change Description |
|---|---|---|---|
| 2026-08-12 | A | Nam | Initial draft of Report 6 following the template, outlined 13 workflows mapped to the 13 SFMS modules |
| 2026-08-12 | M | Nam | Aligned section 2 (Installation) with the real deployment stack in the root `docker-compose.yml` (Nginx → Frontend/Backend, 2 Python AI microservices, pgvector Postgres, Mailpit, optional Cloudflare Tunnel); corrected Frontend source path to `04_Development/Frontend`; corrected routes that do not exist on the FE; rewrote section 3.13 (MF12) with the real AI screens |
| | | | |

*A - Added, M - Modified, D - Deleted*

---

## II. Release Package & User Guides

### 1. Deliverable Package

| No. | Deliverable Item | Description |
|---|---|---|
| 1 | Project Schedule/Tracking | `07_implement/00_Team_Work_Assignment_Plan.md` |
| 2 | Project Backlog | `02_Requirements/` (Use Cases, Business Rules, Traceability Matrix) |
| 3 | Source Codes | `04_Development/Backend/` (Spring Boot 4.1, Java 25) + `04_Development/Frontend/` (Next.js 15) + `04_Development/AiServices/` (2 Python/FastAPI microservices: `frs_service`, `rag_service`) + `04_Development/Mobile/` |
| 4 | Deployment Package | Root `docker-compose.yml` (+ overrides `docker-compose.prod.yml`, `docker-compose.mailpit.yml`, `docker-compose.localai.yml`), `nginx/nginx.conf`, one `Dockerfile` per service, `.env.example` at root/Backend/Frontend |
| 5 | Database Script(s) | Flyway migrations under `04_Development/Backend/src/main/resources/db/migration/` (V1–V70, including seed scripts) — run automatically by the Backend on startup |
| 6 | Final Report Document | *(fill in per the course's common template)* |
| 7 | Test Cases Document | `07_implement/MFxx_*/TestSpec-MFxx_*.md` (13 files, 1 per module) + `05_Testing/02_IT/MFxx_*/*.md` |
| 8 | Defects List | `07_implement/14_TC_Completion_Status.md` ("Real gaps/bugs found in production code" section, per module) |
| 9 | Issues List | *(fill in if a separate tracker exists — Jira/Trello/GitLab Issues)* |
| 10 | Slide | *(fill in once the presentation slide deck is ready)* |

---

### 2. Installation Guides

**Deployment architecture (what actually runs)** — *unnumbered lead-in; paste it as body text under heading "2. Installation Guides" in Word so the template's 2.1/2.2 numbering doesn't shift.*

The whole system runs as a **single Docker Compose stack** defined by `docker-compose.yml` at the repository root. Nothing but Nginx is published to the host: the browser only ever talks to Nginx, which reverse-proxies to the Frontend and Backend.

```
Browser (or Cloudflare Tunnel → domain sfsm.io.vn)
        │  http://localhost  (host port ${NGINX_HOST_PORT:-80})
        ▼
   ┌─────────┐   /api/v1/…, /media/, /ws/, /oauth2/…, /swagger-ui   ┌──────────────┐
   │  nginx  │ ────────────────────────────────────────────────────▶│ backend:8080 │ Spring Boot 4.1 / Java 25
   │ :80     │                                                       └──────┬───────┘
   └────┬────┘   everything else (/)                                        │
        │        ┌────────────────┐                                         │
        └───────▶│ frontend:3000  │ Next.js 15 (standalone)                 │
                 └────────────────┘                                         │
                                                     ┌──────────────────────┼───────────────────────┐
                                                     ▼                      ▼                       ▼
                                            ┌────────────────┐   ┌────────────────────┐   ┌──────────────────┐
                                            │ postgres:5432  │   │ frs-service:8000   │   │ rag-service:8001 │
                                            │ pgvector/pg15  │◀──│ face recognition   │   │ Gemini RAG (MF12)│
                                            └────────────────┘   │ (Python/FastAPI)   │   │ (Python/FastAPI) │
                                                                 └────────────────────┘   └──────────────────┘
                        ┌──────────────┐                         ┌───────────────────────────────┐
                        │ mailpit      │ fake SMTP, UI on :8026  │ cloudflared (profile "tunnel")│ optional
                        └──────────────┘                         └───────────────────────────────┘
```

| Service | Image / build context | Ports | Role |
|---|---|---|---|
| `nginx` | `nginx:alpine` + `nginx/nginx.conf` | **`${NGINX_HOST_PORT:-80}` → 80** (the only host-published port) | Reverse proxy; 180s read timeout on `/api/v1/ai/`, long-lived `/ws/` for STOMP notifications |
| `frontend` | build `./04_Development/Frontend` | 3000 (internal only) | Next.js 15, standalone output; reaches the API via `SFMS_BACKEND_URL=http://backend:8080` |
| `backend` | build `./04_Development/Backend` | 8080 (internal only) | Spring Boot 4.1 / Java 25; runs with `SPRING_PROFILES_ACTIVE=prod` so the real FRS adapter is wired instead of the dev stub |
| `postgres` | `pgvector/pgvector:pg15` | internal only | PostgreSQL 15 **with the `vector` extension** (needed for real RAG vector retrieval, V59) — DB/user/password all `sfms` |
| `frs-service` | build `./04_Development/AiServices/frs_service` | 8000 (internal) | Face recognition; persists embeddings into its own `driver_face_embeddings` table (created by the service itself, not by Flyway) |
| `rag-service` | build `./04_Development/AiServices/rag_service` | 8001 (internal) | RAG chatbot; uses `GEMINI_API_KEY` from the root `.env` |
| `mailpit` | `axllent/mailpit:latest` | **8026 → 8025** (web UI) | Fake SMTP for dev/test; only receives mail when the Mailpit override is applied (see 2.2 Step 5) |
| `cloudflared` | `cloudflare/cloudflared:latest` | none | Publishes the stack on the real domain; only started with `--profile tunnel` |

Compose overrides (never loaded automatically — must be passed with `-f`):

| File | When to use |
|---|---|
| `docker-compose.prod.yml` | On the VPS — replaces `build:` with pre-built GitLab CI images (`docker compose … pull`) |
| `docker-compose.mailpit.yml` | Local/test — redirects the Backend's SMTP to Mailpit instead of real Gmail |
| `docker-compose.localai.yml` | Dev only — points `rag-service` at a local OpenAI-compatible model server (LM Studio/Ollama) instead of Gemini |

#### 2.1 System Requirements

| Component | Requirement |
|---|---|
| OS | Windows 10/11, or any Linux/macOS that supports Docker |
| Docker Desktop / Docker Engine | **Required — this is the only mandatory dependency.** Needs Compose v2 (`docker compose`, not `docker-compose`) |
| Java / Maven | **Not needed for the Docker run** (the Backend image builds with `maven:3.9-eclipse-temurin-25` and runs on `eclipse-temurin:25-jre-alpine`). Only install JDK 25 + Maven 3.9.x if running the Backend outside Docker (see the "running outside Docker" block at the end of 2.2) |
| Node.js | **Not needed for the Docker run** (the Frontend image builds on `node:20-alpine`). Only install Node 20 LTS if running the Frontend outside Docker |
| PostgreSQL | 15 with the `vector` extension — supplied by the `pgvector/pgvector:pg15` container; no standalone install needed |
| Browser | Latest Chrome/Edge |
| RAM | 8GB minimum; **12–16GB recommended** — six containers run concurrently and `frs-service` loads the DeepFace model |
| Disk | ≈ 10GB free for images + volumes (`pgdata`, `backend-uploads`, `backend-backups`, `frs-data`, `frs-cache`) |
| Network | Internet access for the first `--build` (pulls base images and dependencies); a `GEMINI_API_KEY` if the AI assistant must answer for real |

📸 **[SCREENSHOT NEEDED — optional]:** Output of `docker -v` and `docker compose version` on the real machine, proving the environment is set up correctly.

#### 2.2 Installation Instruction — Docker (the real deployment path)

**Step 1 — Clone the source code**

```bash
git clone https://gitlab.com/manhnc2/su26_sep490_g8.git
cd su26_sep490_g8
```

**Step 2 — Create the three `.env` files**

Compose reads one `.env` at the root and, via `env_file:`, one per application. Copy each `.example` and fill it in:

```bash
cp .env.example .env                                        # GEMINI_API_KEY, TUNNEL_TOKEN, NGINX_HOST_PORT
cp 04_Development/Backend/.env.example  04_Development/Backend/.env
cp 04_Development/Frontend/.env.example 04_Development/Frontend/.env
```

Minimum set to fill in for a working demo:

| File | Variable | Note |
|---|---|---|
| `.env` (root) | `GEMINI_API_KEY` | Only needed for the AI assistant (MF12). From <https://aistudio.google.com/apikey> |
| `.env` (root) | `NGINX_HOST_PORT` | Leave empty = port 80. Set it only if port 80 is already taken on the machine |
| `Backend/.env` | `JWT_SECRET` | Required — generate with `openssl rand -base64 48` |
| `Backend/.env` | `MF04_NATIONAL_ID_KEY`, `SFMS_CRED_MASTER_KEY` | Required — 32 bytes base64, `openssl rand -base64 32` |
| `Backend/.env` | `SMTP_USER`, `SMTP_PASSWORD` | Gmail App Password, for real activation/reset emails. Skip it and use Mailpit instead (Step 5) |
| `Backend/.env` | `DB_URL` | Leave as-is — Compose overrides it to `jdbc:postgresql://postgres:5432/sfms` inside the network |

> ⚠️ `.env` files are gitignored and never committed (ADR-000 §2) — they must be created by hand on each machine.

**Step 3 — Bring the whole stack up**

```bash
docker compose up --build -d
```

The first build takes a while (Maven dependencies + Next.js build + DeepFace model). Flyway runs migrations `V1 … V70` automatically when the Backend starts, including the seed scripts (`V2` roles/permissions, `V5` MF03, `V25` System Admin, `V39` bulk test data …).

```bash
docker compose ps        # all services should be "running"/"healthy"
docker compose logs -f backend
```

📸 **[SCREENSHOT NEEDED]:** `docker compose ps` showing all 7 services (`nginx`, `frontend`, `backend`, `postgres`, `frs-service`, `rag-service`, `mailpit`) up.

📸 **[SCREENSHOT NEEDED]:** The Backend log line `Started SfmsApplication in ... seconds`, preceded by the Flyway "Successfully applied … migrations" line.

**Step 4 — Open the application**

| What | URL |
|---|---|
| Web application (login) | `http://localhost/login` |
| REST API | `http://localhost/api/v1/…` |
| Swagger UI | `http://localhost/swagger-ui/index.html` |
| Uploaded files | `http://localhost/media/…` |
| Mailpit inbox | `http://localhost:8026` |

> Note: `http://localhost:3000` and `http://localhost:8080` do **not** work in this setup — the Frontend and Backend are not published to the host, everything goes through Nginx on port 80.

📸 **[SCREENSHOT NEEDED]:** The login page at `http://localhost/login`.

📸 **[SCREENSHOT NEEDED]:** The Swagger UI page showing the full list of tags for all 13 modules (MF01–MF13).

**Step 5 — (Test only) Capture outgoing email in Mailpit**

By default the Backend sends through the real Gmail SMTP configured in `Backend/.env`. To capture activation/reset emails locally instead:

```bash
docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend
```

Then read the mail at `http://localhost:8026`. Go back to real Gmail with `docker compose up -d backend`.

📸 **[SCREENSHOT NEEDED]:** The Mailpit inbox showing an account-activation email.

**Step 6 — (Optional) Publish on the real domain via Cloudflare Tunnel**

Put `TUNNEL_TOKEN` in the root `.env` (Cloudflare Zero Trust → Networks → Tunnels), then:

```bash
docker compose --profile tunnel up --build -d
```

The stack becomes reachable at the real domain (`sfsm.io.vn`) without opening any inbound port — `cloudflared` dials out to Cloudflare.

**Step 7 — (Optional) Production deployment on the VPS**

Images are built and pushed by GitLab CI; the VPS only pulls them:

```bash
docker compose -f docker-compose.yml -f docker-compose.prod.yml pull backend frontend frs-service rag-service
docker compose -f docker-compose.yml -f docker-compose.prod.yml up -d
```

`REGISTRY_IMAGE` / `IMAGE_TAG` come from the root `.env`. Pin `IMAGE_TAG` to a specific commit SHA to roll back without rebuilding.

**Useful operations**

```bash
docker compose logs -f backend frontend      # follow logs
docker compose restart backend               # restart one service after an .env change
docker compose down                          # stop (volumes are KEPT)
docker compose down -v                       # stop and WIPE the database + uploads + backups
```

**Alternative — running outside Docker (development only)** — *unnumbered appendix to 2.2; keep it as body text so the Word ToC keeps only 2.1/2.2.*

Useful when debugging with an IDE. Requires JDK 25, Maven 3.9.x and Node 20 installed locally.

```bash
# 1) Infrastructure only (Postgres 5432 + Mailpit 1025/8025 published to the host)
cd 04_Development/Backend && docker compose up -d

# 2) Backend — reads 04_Development/Backend/.env (DB_URL default localhost:5432)
mvn clean install -DskipTests
mvn spring-boot:run                          # http://localhost:8080

# 3) Frontend
cd ../../04_Development/Frontend
npm install
npm run dev                                  # http://localhost:3000, API at http://localhost:8080/api/v1
```

> In this mode the AI features (chatbot MF12, face recognition) fall back to their dev stubs — `SFMS_RAG_ENABLED`/`SFMS_FRS_ENABLED` default to `false` and the `prod` profile is not active, so `frs-service`/`rag-service` are not called. Use the Docker path (2.2) for any screenshot involving AI.

---

### 3. User Manual

#### 3.1 Overview

SFMS (School Food Management System) is a school meal / food-safety management system for primary schools, supporting **7 user roles** and **13 business modules**:

| # | Module | Primary role(s) |
|---|---|---|
| MF01 | User Management & Access Control | System Admin, all roles (login) |
| MF02 | School, Class & Academic Year Management | System Admin, School Admin |
| MF03 | Student & Parent Management | School Admin, Teacher, Parent |
| MF04 | Supplier & Partner Management | School Admin, Supplier, Service Caterers |
| MF05 | Menu & Diet Management | Service Caterers, School Admin, Medical Staff |
| MF06 | Ingredient & Inventory Management | Service Caterers, Supplier |
| MF07 | Three-Step Food Safety Inspection | Medical Staff, Service Caterers |
| MF08 | Food Sample Retention | Medical Staff, Service Caterers |
| MF09 | Meal Serving & Distribution | Teacher, Parent |
| MF10 | Feedback & Announcement Management | All roles |
| MF11 | Incident & Emergency Management | Medical Staff, School Admin |
| MF12 | AI Supporting Services & System Administration | All roles (AI assistant), System Admin (knowledge base, integrations, monitoring) |
| MF13 | Reporting & Analytics | School Admin, System Admin, Medical Staff |

**High-level business flow (food-safety chain):**

```
Supplier delivers ingredients (MF04/MF06)
      ↓
Menu planning & approval (MF05)
      ↓
Three-step inspection before cooking/serving (MF07)
      ↓
24h food sample retention (MF08)
      ↓
Meal distribution to students (MF09)
      ↓
Consumption recording + parent feedback (MF09/MF10)
      ↓
If an incident occurs → investigate using the retained data (MF11)
      ↓
Consolidated reporting for management/authorities (MF13)
```

📸 **[SCREENSHOT NEEDED]:** Redraw the diagram above with draw.io/Figma for a cleaner look, and insert it here instead of the plain text block.

**Logging in (shared by every workflow below):**

1. Open `http://localhost/login` (Docker stack — port 80 through Nginx; `http://localhost:3000/login` only in the non-Docker dev mode described at the end of 2.2).
2. Enter the email + password of an account with the role you want to demo (School Admin / System Admin / Teacher / Parent / Medical Staff / Service Caterers / Supplier). Accounts come from the Flyway seed scripts — e.g. the System Admin seeded by `V25__seed_system_admin.sql`: `dangnh.sysadmin.synthetic@example.test` / `P@ssw0rd123`.
3. Click "Login" → the system redirects to that role's dashboard (`/<role>/dashboard`). "Sign in with Google" is also available (Spring Security OAuth2, proxied through `/oauth2/…`) when `GOOGLE_CLIENT_ID`/`GOOGLE_CLIENT_SECRET` are configured.

New schools/suppliers/caterers can also self-register at `/register` → `/register/school`, `/register/supplier`, `/register/caterer`.

📸 **[SCREENSHOT NEEDED]:** The empty `/login` screen, and the dashboard after a successful login (capture one per role if the report needs all 7 roles illustrated).

---

#### 3.2 Workflow 1 — User Management & Access Control (MF01)

**Purpose:** System Admin/School Admin create staff accounts and assign roles, manage login sessions; every user self-manages their profile and password.

**Roles:** System Admin, School Admin (create/manage accounts); all roles (self-service profile/password).

**Steps:**

1. **System Admin creates a system-level account:** log in as System Admin → go to `/system-admin/users` → click "Add..." → choose a role; if it's a school-level role, "Linked School" becomes required → fill in the details → "Create Account".
   📸 **[SCREENSHOT]:** The account-creation form at `/system-admin/users`, and the new user row appearing in the table with a "pending verification" status.
2. **School Admin creates a school staff member:** log in as School Admin → `/school-admin/staff` → "Add Staff" → the role dropdown only allows school-level roles (Teacher/Medical Staff/Service Caterers).
   📸 **[SCREENSHOT]:** The `/school-admin/staff` form, noting the role dropdown excludes System/School Admin.
3. **Account activation:** the new user opens the invitation email (via the Mailpit UI at `http://localhost:8026` in local testing — see 2.2 Step 5) → follows the `/activate-account?token=...` link → sets a password → activates.
   📸 **[SCREENSHOT]:** The Mailpit inbox showing the invitation email, and the `/activate-account` screen after successful activation.
4. **Login / forgot password:** `/login` → "Forgot password?" → `/forgot-password` → `/verify-otp` → `/reset-password`.
   📸 **[SCREENSHOT]:** All 3 screens — forgot-password, verify-otp, reset-password.
5. **Managing custom roles (System Admin):** `/system-admin/roles` → create/edit a role, assign fine-grained permissions.
   📸 **[SCREENSHOT]:** The `/system-admin/roles` screen with the permission list.
6. **Self-service profile:** click the avatar in the topbar (any role) → "Profile" / "Change Password".
   📸 **[SCREENSHOT]:** The avatar menu open + the change-password dialog.

---

#### 3.3 Workflow 2 — School, Class & Academic Year Management (MF02)

**Purpose:** System Admin manages the list of schools in the system; School Admin manages classes and academic years for their own school.

**Roles:** System Admin (school list), School Admin (classes/academic years).

**Steps:**

1. System Admin → `/system-admin/schools` → view the school list, click into one school (`/system-admin/schools/[id]`) to see its details/configuration.
   📸 **[SCREENSHOT]:** The school list + one school's detail page.
2. School Admin → `/school-admin/academic-years` → create a new academic year, or `/school-admin/academic-years/promote` to bulk-promote students at year-end.
   📸 **[SCREENSHOT]:** The academic-year list screen + the "promote" (bulk class promotion) screen.
3. School Admin → `/school-admin/classrooms` → create a class, assign a homeroom teacher, view a class's detail page (`/school-admin/classrooms/[id]`).
   📸 **[SCREENSHOT]:** The class list + one class's detail page (headcount, students in the class).
4. School Admin → `/school-admin/config` → general school configuration (report-recipient emails, alert thresholds, etc.).
   📸 **[SCREENSHOT]:** The school configuration page.

---

#### 3.4 Workflow 3 — Student & Parent Management (MF03)

**Purpose:** School Admin manages student records, declares allergies/chronic conditions; parents view their own children's information.

**Roles:** School Admin, Teacher (view), Parent (declare/view own children).

**Steps:**

1. School Admin → `/school-admin/students` → "Add Student" → fill in the record. Bulk Excel import is triggered from this same list screen (there is no separate `/students/import` route).
   📸 **[SCREENSHOT]:** The student list + the "add student" form + the Excel import dialog.
2. View a student's detail page: `/school-admin/students/[id]` — profile, class, linked parents, allergies.
   📸 **[SCREENSHOT]:** The student detail page.
3. Manage a parent: open a parent from the student's detail page → `/school-admin/parents/[id]` (a parent's detail page, reached from the student record — there is no standalone parent list route).
   📸 **[SCREENSHOT]:** A parent's detail page showing linked children.
4. Review allergy declarations: `/school-admin/allergy-review` — School Admin/Medical Staff confirm allergy information declared by parents.
   📸 **[SCREENSHOT]:** The allergy-review screen.
5. Parent declares allergies/chronic conditions: log in as Parent → `/parent/allergies`, `/parent/chronic-conditions`.
   📸 **[SCREENSHOT]:** Both declaration screens on the Parent side.

---

#### 3.5 Workflow 4 — Supplier & Partner Management (MF04)

**Purpose:** School Admin forms partnerships with suppliers/catering providers; suppliers self-manage their profile, products, and certificates.

**Roles:** School Admin, Supplier, Service Caterers.

**Steps:**

1. School Admin → `/school-admin/partnerships` → view the partner list, one partner's detail page (`/school-admin/partnerships/[id]`).
   📸 **[SCREENSHOT]:** The partner list + a detail page.
2. Supplier logs in → `/supplier/partnerships` → view partnership invitations from schools, accept one.
   📸 **[SCREENSHOT]:** The partnership-invitation screen on the Supplier side.
3. Supplier manages products: `/supplier/products` — add/edit the catalog of supplied products.
   📸 **[SCREENSHOT]:** The Supplier's product list.
4. Supplier manages food-safety certificates: `/supplier/certificates` — upload, track expiry.
   📸 **[SCREENSHOT]:** The certificates screen, noting the near-expiry warning.

---

#### 3.6 Workflow 5 — Menu & Diet Management (MF05)

**Purpose:** Service Caterers plan the weekly menu, School Admin/Medical Staff approve it; the system supports special meals for students with allergies.

**Roles:** Service Caterers (planning), School Admin (approval), Medical Staff (nutrition review), Parent (view/request special meals).

**Steps:**

1. Service Caterers → `/service-caterers/menus` → "Create Menu" (`/service-caterers/menus/create`) → pick dishes for each day/meal of the week.
   📸 **[SCREENSHOT]:** The menu-creation form laid out as a day × meal grid.
2. A saved template can be reused: `/service-caterers/menu-templates`.
   📸 **[SCREENSHOT]:** The menu-template list.
3. Manage the dish catalog: `/service-caterers/dishes`.
   📸 **[SCREENSHOT]:** The dish list + each dish's nutrition info.
4. School Admin approves the menu: `/school-admin/menu-approval` → pick one pending menu (`/school-admin/menu-approval/[id]`) → Approve/Reject.
   📸 **[SCREENSHOT]:** The pending-approval list + the approval detail screen.
5. Parents view the weekly menu: Parent → `/parent/weekly-menu`.
   📸 **[SCREENSHOT]:** The weekly-menu screen on the Parent side.
6. Parents request a special meal (allergy/religious diet, etc.): `/parent/special-meal-requests` → approved by School Admin at `/school-admin/special-meals`.
   📸 **[SCREENSHOT]:** The Parent's request form + the School Admin's approval screen.

---

#### 3.7 Workflow 6 — Ingredient & Inventory Management (MF06)

**Purpose:** Suppliers deliver ingredients; Service Caterers receive them, verify documentation, and manage inventory.

**Roles:** Supplier (delivering), Service Caterers (receiving, issuing stock).

**Steps:**

1. Supplier creates a delivery: `/supplier/deliveries` → view its detail page (`/supplier/deliveries/[id]`); manage product batches at `/supplier/batches`.
   📸 **[SCREENSHOT]:** The delivery list on the Supplier side.
2. Service Caterers receive the delivery: `/service-caterers/receiving` → "New Receiving" (`/service-caterers/receiving/new`) → cross-check documentation, inspect quality, confirm stock-in.
   📸 **[SCREENSHOT]:** The receiving form, including the section for photographing documents/goods.
3. Issue stock to the kitchen: `/service-caterers/inventory/issue`.
   📸 **[SCREENSHOT]:** The stock-issue form based on the day's menu.
4. View inventory transaction history: `/service-caterers/inventory-transactions`; current stock levels: `/service-caterers/inventory`.
   📸 **[SCREENSHOT]:** The stock-level table + the in/out transaction history.

---

#### 3.8 Workflow 7 — Three-Step Food Safety Inspection (MF07)

**Purpose:** Mandatory three-step inspection (raw ingredients → cooking → before serving) required before meals can be served (BR-094 gate).

**Roles:** Medical Staff (performs the inspection), Service Caterers (views results).

**Steps:**

1. Medical Staff → `/medical-staff/conduct-inspection` → select the meal to inspect → work through the 3-step checklist (`/medical-staff/conduct-inspection/checklist`).
   📸 **[SCREENSHOT]:** The 3-step checklist, each step with a PASS/FAIL result field + supporting photo.
2. Record detailed results: `/medical-staff/conduct-inspection/[id]/record`.
   📸 **[SCREENSHOT]:** The per-step result recording screen.
3. View inspection history: `/medical-staff/conduct-inspection/history`, or the consolidated list `/medical-staff/inspections` → detail page `/medical-staff/inspections/[id]`.
   📸 **[SCREENSHOT]:** The inspection history + one session's detail page.
4. Service Caterers view how the inspection result affects the kitchen: `/service-caterers/inspections` → detail + printable slip (`/service-caterers/inspections/[id]/print`).
   📸 **[SCREENSHOT]:** The printable inspection slip.
5. **Illustrating the gate:** attempt to serve a meal while the inspection is not yet PASSED → the system blocks it (see Workflow 9, the "confirm serving" step).
   📸 **[SCREENSHOT]:** The error message blocking meal serving when the inspection hasn't passed.

---

#### 3.9 Workflow 8 — Food Sample Retention (MF08)

**Purpose:** Retain food samples for a minimum of 24 hours as required by regulation, to support traceability if a food-poisoning incident occurs.

**Roles:** Medical Staff / Service Caterers.

**Steps:**

1. Log in as Medical Staff → `/medical-staff/samples` → "New Sample" → select the dish, weight, collection time, retention deadline.
   📸 **[SCREENSHOT]:** The sample-registration form.
2. Service Caterers perform the same action at `/service-caterers/samples` → view detail/print label (`/service-caterers/samples/[id]`, `/service-caterers/samples/[id]/print`, or bulk print `/service-caterers/samples/print`).
   📸 **[SCREENSHOT]:** The printed sample label (sample code, dish, collection time, disposal deadline).
3. Dispose of a sample once past its retention window (action performed on the same sample-detail screen).
   📸 **[SCREENSHOT]:** The sample status changing to "Disposed" with the disposal timestamp.

---

#### 3.10 Workflow 9 — Meal Serving & Distribution (MF09)

**Purpose:** Teachers confirm meal serving per class, record consumption; parents review their child's meal history.

**Roles:** Teacher, Parent.

**Steps:**

1. Teacher logs in → `/teacher/distribution` → view the list of students to be served in their class today.
   📸 **[SCREENSHOT]:** The class serving list, PENDING status.
2. Click "Confirm Serving" for the whole class/individual students → view one serving record's detail (`/teacher/distribution/[id]`).
   📸 **[SCREENSHOT]:** The confirm-serving action + status changing to SERVED. If the inspection (Workflow 7) hasn't passed, also capture the blocked screen to illustrate the BR-094 gate.
3. Record consumption after the meal: `/teacher/meal-records` → select FULLY/PARTIALLY/NOT_CONSUMED per student.
   📸 **[SCREENSHOT]:** The consumption-recording form.
4. Parents view their child's meal history: `/parent/meal-history` → one day's detail (`/parent/meal-history/[id]`); view dish nutrition info: `/parent/nutrition-history`, `/parent/nutrition/[dishId]`.
   📸 **[SCREENSHOT]:** The meal-history screen on the Parent side (dish name, consumption level, allergy warning if any).

---

#### 3.11 Workflow 10 — Feedback & Announcement Management (MF10)

**Purpose:** The school sends announcements to parents/staff; parents submit feedback about meals.

**Roles:** School Admin (sends announcements), Parent (feedback + receiving announcements).

**Steps:**

1. School Admin → `/school-admin/announcements` → "Create Announcement" (`/school-admin/announcements/create`) → choose the target audience, content.
   📸 **[SCREENSHOT]:** The announcement-creation form + the sent-announcements list.
2. Other roles view their announcements at `/<role>/announcements` (e.g. `/parent/announcements`, `/teacher/announcements`, ...) → detail `[id]`.
   📸 **[SCREENSHOT]:** The announcement list on the recipient side (capture one representative role, e.g. Parent).
3. Parents submit meal feedback: `/parent/feedback/meal`, or general quality feedback: `/parent/feedback/quality`; view previously submitted feedback at `/parent/feedback`.
   📸 **[SCREENSHOT]:** The feedback submission form + star rating (if applicable).
4. School Admin views consolidated feedback: `/school-admin/feedback`.
   📸 **[SCREENSHOT]:** The consolidated feedback table on the school side.
5. Parents report a food-safety concern (a separate, more urgent channel than regular feedback): `/parent/safety-concern`.
   📸 **[SCREENSHOT]:** The safety-concern report form.

---

#### 3.12 Workflow 11 — Incident & Emergency Management (MF11)

**Purpose:** Record, investigate, and resolve food-safety incidents (food poisoning, foreign objects, etc.), tracing back to ingredients/dishes via data from MF06–MF09.

**Roles:** Medical Staff (investigation), School Admin (tracking/closing incidents), Teacher (initial report).

**Steps:**

1. Initial incident report: Teacher → `/teacher/incidents` (quick report when a student shows unusual symptoms).
   📸 **[SCREENSHOT]:** The Teacher's quick-report form.
2. Medical Staff handles the full workflow: `/medical-staff/incidents` → detail page (`/medical-staff/incidents/[id]`) → add evidence (`/medical-staff/incidents/[id]/evidence`) → open an investigation (`/medical-staff/incidents/[id]/investigation`).
   📸 **[SCREENSHOT]:** The investigation page — note the trace-back-by-`dish_id` feature linking back to MF09 (which students ate the suspect dish).
3. Record related medical records: opened from the incident detail page → `/medical-staff/incidents/records/[id]` (per-record page; the list lives inside the incident detail, not at a separate `/records` route). Broader student health data: `/medical-staff/health-records` → `/medical-staff/health-records/[id]` (+ `/history`).
   📸 **[SCREENSHOT]:** A student's medical record related to the incident.
4. Issue corrective actions (CAPA): `/medical-staff/corrective-actions` → detail `[id]`; School Admin views CAPA at `/school-admin/corrective-actions`.
   📸 **[SCREENSHOT]:** The CAPA list + resolution status.
5. Record a violation (if a third party is at fault): `/medical-staff/violations` → create new (`/medical-staff/violations/new`) → detail (`/medical-staff/violations/[id]`); School Admin views/closes it (`/school-admin/violations`, `/school-admin/violations/[id]`).
   📸 **[SCREENSHOT]:** The violation-creation form + its detail page.
6. School Admin closes the incident after resolution: `/school-admin/incidents/[id]/close`.
   📸 **[SCREENSHOT]:** The close-incident screen (requiring a final conclusion to be filled in).

---

#### 3.13 Workflow 12 — AI Supporting Services & System Administration (MF12)

**Purpose:** Two Python microservices (`rag-service` — Gemini RAG chatbot; `frs-service` — face recognition) plus the System Admin's operations screens. Both microservices are called by the Backend, but each has a real user-facing screen.

**Roles:** All roles (AI assistant), Supplier + Service Caterers (face recognition in the delivery flow), System Admin (knowledge base, integrations, monitoring, backup).

**Steps:**

1. **AI assistant (chatbot):** any logged-in role → `/ai-chat` → ask a question about school food safety / nutrition (UC-012/UC-119). The FE calls `POST /api/v1/ai/…`, the Backend forwards to `rag-service:8001`, which calls Gemini. Out-of-scope questions are politely refused by the model itself (scope is enforced in `rag_chain.py`, not in the UI).
   📸 **[SCREENSHOT]:** The `/ai-chat` screen with a real question and answer.
   > Requires the Docker stack with `SFMS_RAG_ENABLED=true` (already set in `docker-compose.yml`) and a valid `GEMINI_API_KEY` in the root `.env`. Without them the Backend returns the stub answer (ADR-000).
2. **AI knowledge base:** System Admin → `/system-admin/ai-knowledge` → add/remove the documents the RAG assistant retrieves from (stored as vectors in the `pgvector` Postgres).
   📸 **[SCREENSHOT]:** The knowledge-document list + the "add document" dialog.
3. **Face recognition (FRS):** Supplier enrolls delivery staff faces at `/supplier/delivery-staff`; on receiving, Service Caterers verify the delivery person via the face-scan dialog inside `/service-caterers/receiving/new`. The Backend calls `frs-service:8000`, which stores embeddings in its own `driver_face_embeddings` table.
   📸 **[SCREENSHOT]:** The face-scan dialog on the receiving screen with the match/no-match result.
   > Requires `SPRING_PROFILES_ACTIVE=prod` + `SFMS_FRS_ENABLED=true` (both set in `docker-compose.yml`); outside Docker the stub always returns PASS.
4. **External integrations:** System Admin → `/system-admin/integrations` → enter credentials for Gemini / Firebase Cloud Messaging / GPS provider. These keys are **not** in `.env` — they are encrypted with `SFMS_CRED_MASTER_KEY` and stored in the database.
   📸 **[SCREENSHOT]:** The integrations screen (credentials masked).
5. **System operations:** `/system-admin/monitoring`, `/system-admin/system-status`, `/system-admin/error-logs`, `/system-admin/audit-logs`, `/system-admin/backup` (BR-160/161 — scheduled `pg_dump` into the `backend-backups` volume), `/system-admin/security-policies`, `/system-admin/permissions`.
   📸 **[SCREENSHOT]:** The system-status/monitoring screen + the backup screen showing the scheduled backup list.

---

#### 3.14 Workflow 13 — Reporting & Analytics (MF13)

**Purpose:** View the consolidated compliance dashboard, export PDF/XLSX reports, and schedule automated recurring report delivery.

**Roles:** School Admin, System Admin, Medical Staff (view-only).

**Steps:**

1. Log in as School Admin/System Admin → `/school-admin/reports` (or the System Admin equivalent) → view the consolidated dashboard across the 5 compliance groups (sample retention, three-step inspection, receiving, supplier certificates, kitchen hygiene).
   📸 **[SCREENSHOT]:** The consolidated dashboard with compliance-rate charts per group.
2. View the detailed report for one category: `/school-admin/reports/[slug]` (e.g. `sample-retention`, `three-step`, `receiving`, ...).
   📸 **[SCREENSHOT]:** The detailed data table for one report category.
3. Export a report file: click "Export Report" on the detail page → choose PDF/XLSX → download.
   📸 **[SCREENSHOT]:** The format-selection dialog + the downloaded PDF/XLSX file opened for viewing (note the "Confidential" watermark).
4. School-wide compliance view (Medical Staff, School Admin): `/medical-staff/compliance`, `/medical-staff/compliance/report`, `/medical-staff/compliance/history`, or the report center (`/medical-staff/report-center`, `/medical-staff/report-center/compliance`, `/medical-staff/report-center/history`).
   📸 **[SCREENSHOT]:** One or two representative screens on the Medical Staff side.
5. System Admin views school-wide system usage statistics: `/system-admin/usage-statistics`.
   📸 **[SCREENSHOT]:** The usage-statistics screen.
6. (If wired up on the FE) Schedule automated recurring report delivery (daily/weekly/monthly) — check the School Admin's report configuration section.
   📸 **[SCREENSHOT]:** The report-schedule creation form, if this screen exists on the FE.

---

## Notes for Nam before moving this into Word

1. **Screenshots:** run the full stack with `docker compose up --build -d` per section 2.2 and browse at `http://localhost` (not `:3000`/`:8080` — those ports are not published). Log in as each role in turn using the accounts seeded by the Flyway migrations (`V25__seed_system_admin.sql` for System Admin, `V5`/`V39` for the school-level roles; shared dev password `P@ssw0rd123`) so screenshots show real data rather than staged mock-ups.
   ⚠️ `V900__test_fixtures.sql` is **test-only** (`src/test/resources/db/testseed/`) — it is never loaded into the dev/prod database, so those accounts (e.g. `U-9001`) will not exist in the running system.
2. **AI screenshots (section 3.13)** only work on the Docker path: the `prod` profile plus `SFMS_RAG_ENABLED`/`SFMS_FRS_ENABLED` are set in `docker-compose.yml`, and `GEMINI_API_KEY` must be in the root `.env`. Running the Backend with `mvn spring-boot:run` (the non-Docker block at the end of 2.2) gives you the stubs instead.
3. Feel free to **merge some workflows** if the report has a page-count limit (e.g. merge MF07+MF08 into a single "inspection → sample-retention chain", fold MF10 into the end of other workflows) — the 13-workflow structure above is the most complete version (1:1 with the 13 modules); trim as needed per the actual course requirements.
4. All routes in this document have been cross-checked against the real `04_Development/Frontend/src/app/(app)/` folder on 2026-08-12 (three routes cited in the first draft did not exist and have been corrected: `/school-admin/students/import`, `/school-admin/parents`, `/medical-staff/incidents/records`). Do **not** take screenshots from `03_Design/08_FrontEnd/Web/` — that copy is not the deployed Frontend.
5. Screens that exist on the FE but are not yet covered by a workflow above, in case the report needs them: `/school-admin/grades`, `/school-admin/compliance`, `/school-admin/violations/override-report`, `/service-caterers/orders`, `/service-caterers/cook-sheet`, `/service-caterers/kitchen-inspections`, `/service-caterers/class-deliveries`, `/supplier/purchase-requests`, `/medical-staff/health-reviews`, `/parent/health`, `/parent/school-meals`, `/teacher/students`.
