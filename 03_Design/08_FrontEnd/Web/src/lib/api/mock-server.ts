/**
 * Mock API server — axios-mock-adapter chặn mọi request của `api`
 * (client.ts) và phục vụ dữ liệu in-memory (db.ts).
 *
 * Quy ước REST:
 *   GET    /:resource            → danh sách phân trang {items,total,page,pageSize,pageCount}
 *                                  query: page, pageSize, search, <field>=<value> (lọc chính xác)
 *   GET    /:resource/:id        → chi tiết
 *   POST   /:resource            → tạo mới (trả bản ghi đã tạo)
 *   PUT    /:resource/:id        → cập nhật (merge)
 *   DELETE /:resource/:id        → xóa (có guard riêng cho roles — BR-008)
 *
 * Endpoint nghiệp vụ riêng:
 *   POST /announcements          → tự tạo revision "Công bố lần đầu" (BR-144)
 *   PUT  /announcements/:id      → tự append revision history (BR-144)
 *   POST /kitchen-inspections    → tính điểm; <60% → FAILED + cảnh báo School Administration (UC-014, MSG-093 — descope khóa bếp 05/07/2026)
 *   POST /academic-years/:id/activate → enforce BR-191 (1 năm học Active/trường, MSG-080)
 *   GET  /compliance-report      → tổng hợp báo cáo tuân thủ (UC-115/UC-171, BR-100..BR-104)
 */

import MockAdapter from "axios-mock-adapter";
import { api } from "./client";
import { db, type Row } from "./db";

const DELAY_MS = 350; // giả lập độ trễ mạng

function paginate(rows: Row[], params: URLSearchParams) {
  const page = Math.max(1, Number(params.get("page") ?? 1));
  const pageSize = Math.max(1, Number(params.get("pageSize") ?? 8));
  const search = (params.get("search") ?? "").trim().toLowerCase();

  let filtered = rows;

  // Lọc chính xác theo field bất kỳ (ngoài các key điều khiển);
  // key dạng "not:<field>" = lọc phủ định (VD tab "Chờ xử lý" UC-077 = not:healthStatus=VERIFIED)
  const control = new Set(["page", "pageSize", "search", "dateFrom", "dateTo", "step", "stepStatus", "mealPrefix"]);
  for (const [key, value] of params.entries()) {
    if (control.has(key) || value === "") continue;
    if (key.startsWith("not:")) {
      const field = key.slice(4);
      filtered = filtered.filter((r) => String(r[field]) !== value);
    } else {
      filtered = filtered.filter((r) => String(r[key]) === value);
    }
  }

  // Lọc khoảng ngày (UC-112 field #1, UC-118 field #1) — so trên field `date` hoặc `collectedAt`
  // (dd/MM/yyyy...); dateFrom/dateTo theo định dạng yyyy-mm-dd của <input type="date">
  const dateFrom = params.get("dateFrom");
  const dateTo = params.get("dateTo");
  if (dateFrom || dateTo) {
    const rowTime = (r: Row): number | null => {
      const m = String(r.date ?? r.collectedAt ?? r.sentAt ?? r.deliveryDate ?? "").match(/^(\d{2})\/(\d{2})\/(\d{4})/);
      return m ? Date.UTC(Number(m[3]), Number(m[2]) - 1, Number(m[1])) : null;
    };
    filtered = filtered.filter((r) => {
      const t = rowTime(r);
      if (t == null) return false;
      if (dateFrom && t < Date.parse(dateFrom)) return false;
      if (dateTo && t > Date.parse(dateTo)) return false;
      return true;
    });
  }

  // Lọc theo bước kiểm thực (UC-112 field #2, BR-079): step=1|2|3 + trạng thái của bước đó
  const step = params.get("step");
  const stepStatus = params.get("stepStatus");
  if (step && stepStatus) {
    filtered = filtered.filter((r) => String(r[`step${step}`]) === stepStatus);
  }

  // Lọc bữa theo tiền tố (UC-118 — field `meal` dạng "Trưa 02/07")
  const mealPrefix = params.get("mealPrefix");
  if (mealPrefix) {
    filtered = filtered.filter((r) => String(r.meal ?? "").startsWith(mealPrefix));
  }

  // Tìm kiếm mờ trên mọi trường chuỗi
  if (search) {
    filtered = filtered.filter((r) =>
      Object.values(r).some(
        (v) => typeof v === "string" && v.toLowerCase().includes(search)
      )
    );
  }

  const total = filtered.length;
  const pageCount = Math.max(1, Math.ceil(total / pageSize));
  const safePage = Math.min(page, pageCount);
  const items = filtered.slice((safePage - 1) * pageSize, safePage * pageSize);
  return { items, total, page: safePage, pageSize, pageCount };
}

function parseUrl(url: string, requestParams?: Record<string, unknown>) {
  const [path, qs] = url.split("?");
  const segments = path.replace(/^\//, "").split("/").filter(Boolean);
  const params = new URLSearchParams(qs ?? "");
  // axios đặt query trong config.params (không nối vào url) — merge vào đây
  if (requestParams) {
    for (const [key, value] of Object.entries(requestParams)) {
      if (value !== undefined && value !== null) params.set(key, String(value));
    }
  }
  return { segments, params };
}

function nowStamp() {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
}

function todayStamp() {
  const d = new Date();
  const pad = (n: number) => String(n).padStart(2, "0");
  return `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()}`;
}

let seq = 100;
function nextId(prefix: string) {
  seq += 1;
  return `${prefix}-${seq}`;
}

function audit(actor: string, action: string, resource: string) {
  db["audit-logs"].unshift({
    id: nextId("AUD"),
    timestamp: nowStamp(),
    actor,
    ip: "10.20.0.1", // UC-106: mọi bản ghi audit kèm IP Address
    action,
    resource,
    result: "SUCCESS",
  });
}

export function setupMockServer() {
  const mock = new MockAdapter(api, { delayResponse: DELAY_MS });

  // ---- Nghiệp vụ riêng: Announcement (BR-144, MSG-047/074) ----

  mock.onPost("/announcements").reply((config) => {
    const body = JSON.parse(config.data ?? "{}");
    const record: Row = {
      id: nextId("ANN"),
      title: body.title ?? "",
      audience: body.audience ?? "Toàn trường",
      publishedAt: body.status === "PUBLISHED" ? todayStamp() : "",
      status: body.status ?? "DRAFT",
      author: body.author ?? "Phạm Quốc Toản (BGH)",
      urgent: Boolean(body.urgent),
      content: body.content ?? "",
      revisions: [
        { at: nowStamp(), by: body.author ?? "Phạm Quốc Toản (BGH)", note: "Công bố lần đầu" },
      ],
    };
    db.announcements.unshift(record);
    audit(String(record.author), `CREATE Announcement ${record.id} (MSG-047)`, "Announcement");
    return [201, record];
  });

  mock.onPut(/\/announcements\/[^/]+$/).reply((config) => {
    const { segments } = parseUrl(config.url ?? "");
    const id = segments[1];
    const row = db.announcements.find((r) => r.id === id);
    if (!row) return [404, { message: "Announcement Not Found" }];
    const body = JSON.parse(config.data ?? "{}");
    const { revisionNote, editor, ...fields } = body;
    Object.assign(row, fields);
    // BR-144: mỗi lần cập nhật tạo 1 mục lịch sử sửa đổi
    (row.revisions as { at: string; by: string; note: string }[]).push({
      at: nowStamp(),
      by: editor ?? "Phạm Quốc Toản (BGH)",
      note: revisionNote || "Cập nhật nội dung",
    });
    audit(String(editor ?? "BGH"), `UPDATE Announcement ${id} (BR-144, MSG-074)`, "Announcement");
    return [200, row];
  });

  // ---- Nghiệp vụ riêng: Kitchen Inspection (UC-014) ----

  mock.onPost("/kitchen-inspections").reply((config) => {
    const body = JSON.parse(config.data ?? "{}");
    const score = Number(body.score ?? 0);
    // <60% → Failed + cảnh báo School Administration (MSG-093) — KHÔNG khóa bếp (descope 05/07/2026)
    const passed = score >= 60;
    const record: Row = {
      id: nextId("KIN-2026"),
      date: todayStamp(),
      // BR-208 deprecated 09/07/2026 — không còn field `type` (Loại kiểm tra)
      score,
      inspector: body.inspector ?? "Ngô Thị Hà",
      pestControlValid: Boolean(body.pestControlValid),
      staffHealthValid: Boolean(body.staffHealthValid),
      result: passed ? "PASSED" : "FAILED",
      failedItems: body.failedItems ?? [],
      itemFindings: body.itemFindings ?? [],
      photosCount: Number(body.photosCount ?? 0),
      /*
       * BUG FIX 27/07/2026 — `review_medicalstaff_compliance_kitcheninspection.md` Mục 19.4.
       *
       * Triệu chứng: phiên vừa tạo qua wizard (VD `KIN-2026-101`) lại hiện badge "Phiên cũ
       * (trước cơ chế xác nhận)" và "Hạng mục đã đánh giá (0)" — đúng nhánh dành cho dữ liệu
       * CŨ HƠN cơ chế xác nhận (Mục 8.4).
       *
       * Nguyên nhân: handler này dựng `record` theo DANH SÁCH FIELD CỐ ĐỊNH, và khi Mục 8.4
       * thêm `confirmStatus`/`itemResults` vào payload phía form thì đây không được cập nhật
       * theo. Form gửi đúng (grep `conduct-inspection/page.tsx` xác nhận), nhưng tầng lưu trữ
       * lặng lẽ bỏ 2 field — không lỗi, không cảnh báo, chỉ mất dữ liệu.
       *
       * Đây là giả thuyết (b) trong 2 khả năng Mục 19.4 nêu; xác định bằng đọc code chứ không
       * cần chạy thử UI. Giữ nguyên kiểu whitelist (không spread `...body`) để không nhận bừa
       * field lạ, chỉ bổ sung đúng 2 field đã có trong đặc tả.
       */
      confirmStatus: body.confirmStatus ?? "AWAITING_CATERERS_CONFIRM",
      itemResults: body.itemResults ?? [],
    };
    db["kitchen-inspections"].unshift(record);
    audit(
      String(record.inspector),
      `SUBMIT KitchenInspection ${record.id} score=${score}%${passed ? "" : " → ALERT School Administration (MSG-093), bếp vận hành bình thường"}`,
      "Food Safety Inspection"
    );
    return [201, record];
  });

  // ---- Nghiệp vụ riêng: Academic Year (BR-191, MSG-080/082/083) ----

  mock.onPost(/\/academic-years\/[^/]+\/activate$/).reply((config) => {
    const { segments } = parseUrl(config.url ?? "");
    const id = segments[1];
    const row = db["academic-years"].find((r) => r.id === id);
    if (!row) return [404, { message: "Academic year not found" }];
    const active = db["academic-years"].find(
      (r) => r.status === "ACTIVE" && r.id !== id
    );
    if (active) {
      // MSG-080
      return [
        409,
        {
          message:
            "Cannot create Active academic year: an Active academic year already exists for this school. Please close it first. (MSG-080, BR-191)",
        },
      ];
    }
    row.status = "ACTIVE";
    audit("toanpq@dtd.edu.vn", `ACTIVATE AcademicYear ${id} (MSG-083)`, "Academic Year");
    return [200, row];
  });

  mock.onPost(/\/academic-years\/[^/]+\/close$/).reply((config) => {
    const { segments } = parseUrl(config.url ?? "");
    const id = segments[1];
    const row = db["academic-years"].find((r) => r.id === id);
    if (!row) return [404, { message: "Academic year not found" }];
    row.status = "CLOSED"; // BR-193: Classroom của năm này chuyển read-only
    audit("toanpq@dtd.edu.vn", `CLOSE AcademicYear ${id} (BR-193, MSG-083)`, "Academic Year");
    return [200, row];
  });

  // ---- Nghiệp vụ riêng: Báo cáo tuân thủ (UC-115, BR-100..BR-104) ----

  mock.onGet("/compliance-report").reply(() => {
    const kitchen = db["kitchen-inspections"];
    const threeStep = db.inspections;
    const samples = db.samples;
    const receiving = db.receiving;
    const partners = db.partners;

    const rate = (ok: number, total: number) =>
      total === 0 ? 100 : Math.round((ok / total) * 100);

    const summary = {
      generatedAt: nowStamp(),
      period: "30 ngày gần nhất",
      modules: [
        {
          slug: "three-step",
          title: "Kiểm thực ba bước",
          rate: rate(threeStep.filter((r) => r.overall === "PASSED").length, threeStep.length),
          total: threeStep.length,
          alerts: threeStep.filter((r) => r.overall === "FAILED").length,
        },
        {
          slug: "sample-retention",
          title: "Lưu mẫu thực phẩm",
          rate: 100,
          total: samples.length,
          alerts: 0,
        },
        {
          slug: "receiving",
          title: "Nhận nguyên liệu",
          rate: rate(receiving.filter((r) => r.status !== "REJECTED").length, receiving.length),
          total: receiving.length,
          alerts: receiving.filter((r) => r.status === "REJECTED").length,
        },
        {
          slug: "supplier-certificates",
          title: "Chứng nhận nhà cung cấp",
          rate: rate(partners.filter((r) => r.certStatus === "VALID").length, partners.length),
          total: partners.length,
          alerts: partners.filter((r) => r.certStatus === "URGENT").length,
        },
        {
          slug: "hygiene",
          title: "Vệ sinh bếp (UC-014)",
          rate: rate(kitchen.filter((r) => r.result === "PASSED").length, kitchen.length),
          total: kitchen.length,
          alerts: kitchen.filter((r) => r.result === "FAILED").length,
        },
      ],
    };
    return [200, summary];
  });

  // ---- Generic REST cho mọi resource trong db ----

  mock.onGet(/.*/).reply((config) => {
    const { segments, params } = parseUrl(
      config.url ?? "",
      config.params as Record<string, unknown> | undefined
    );
    const [resource, id] = segments;
    const rows = db[resource];
    if (!rows) return [404, { message: `Unknown resource: ${resource}` }];
    if (id) {
      const row = rows.find((r) => r.id === id);
      return row ? [200, row] : [404, { message: "Not found" }];
    }
    return [200, paginate(rows, params)];
  });

  mock.onPost(/.*/).reply((config) => {
    const { segments } = parseUrl(config.url ?? "");
    const [resource] = segments;
    const rows = db[resource];
    if (!rows) return [404, { message: `Unknown resource: ${resource}` }];
    const body = JSON.parse(config.data ?? "{}");
    const record: Row = { id: body.id ?? nextId(resource.toUpperCase().slice(0, 3)), ...body };
    rows.unshift(record);
    audit("current-user", `CREATE ${resource} ${record.id}`, resource);
    return [201, record];
  });

  mock.onPut(/.*/).reply((config) => {
    const { segments } = parseUrl(config.url ?? "");
    const [resource, id] = segments;
    const rows = db[resource];
    if (!rows) return [404, { message: `Unknown resource: ${resource}` }];
    const row = rows.find((r) => r.id === id);
    if (!row) return [404, { message: "Not found" }];
    Object.assign(row, JSON.parse(config.data ?? "{}"));
    audit("current-user", `UPDATE ${resource} ${id}`, resource);
    return [200, row];
  });

  // DELETE — dùng cho xóa role tùy chỉnh (UC-029): chặn role hệ thống/còn user gán (BR-008)
  mock.onDelete(/.*/).reply((config) => {
    const { segments } = parseUrl(config.url ?? "");
    const [resource, id] = segments;
    const rows = db[resource];
    if (!rows) return [404, { message: `Unknown resource: ${resource}` }];
    const idx = rows.findIndex((r) => r.id === id);
    if (idx === -1) return [404, { message: "Not found" }];
    const row = rows[idx];
    if (resource === "roles") {
      if (row.system) return [403, { message: "Không thể xóa vai trò hệ thống mặc định (BR-008)." }];
      if (Number(row.users) > 0)
        return [409, { message: `Không thể xóa: còn ${row.users} người dùng (kể cả đã vô hiệu hóa) đang gán vai trò này — cần reassign trước (BR-008, AF1 UC-029).` }];
    }
    rows.splice(idx, 1);
    audit("current-user", `DELETE ${resource} ${id}`, resource);
    return [200, { ok: true }];
  });

  return mock;
}
