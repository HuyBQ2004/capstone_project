/**
 * Đổi tên 10/08/2026 (từ `mock-data.ts`) — file KHÔNG còn chứa dữ liệu mock (3 hằng số
 * giả cuối cùng — `CURRENT_SCHOOL`/`DISH_LIBRARY`/`TEACHER_CLASS` — đã xoá cùng đợt, xác
 * nhận 0 nơi còn đọc). Nội dung còn lại là type/hàm tính toán thuần phục vụ dữ liệu THẬT
 * (menu, kho, dị ứng, chi phí, kiểm thực...) lấy qua API, cộng vài bảng vocabulary cố định
 * (ALLERGEN_CATEGORIES, STANDARD_DIETS...) mà hệ thống cần bất kể backend trả gì.
 * Bám theo Business Rules (02_Requirements/00_BusinessRules/business_rules.md) và enum
 * trạng thái trong class diagram (MenuStatus, InspectionStatus, BatchStatus...).
 */

import type { BadgeVariant } from "@/components/ui/badge";
import type { DietGroup } from "@/lib/diet-groups";
import type { SupplierProduct } from "@/lib/supplier-product-adapter";
import { formatVnDateTime } from "@/lib/format";

// ===== Trạng thái chung =====

export const STATUS_BADGE: Record<string, { label: string; variant: BadgeVariant }> = {
  // Menu (BR-068)
  DRAFT: { label: "Nháp", variant: "info" },
  PENDING_APPROVAL: { label: "Chờ duyệt", variant: "warning" },
  APPROVED: { label: "Đã duyệt", variant: "success" },
  REJECTED: { label: "Từ chối", variant: "danger" },
  PUBLISHED: { label: "Đã công bố", variant: "success" },
  // Inspection (BR-079..BR-098)
  PASSED: { label: "Đạt", variant: "success" },
  FAILED: { label: "Không đạt", variant: "danger" },
  IN_PROGRESS: { label: "Đang thực hiện", variant: "info" },
  PENDING: { label: "Chờ xử lý", variant: "warning" },
  // Batch / Inventory (BR-205)
  RELEASED: { label: "Đã xuất", variant: "success" },
  QUARANTINED: { label: "Cách ly", variant: "critical" },
  EXPIRED: { label: "Hết hạn", variant: "danger" },
  EXPIRING_SOON: { label: "Sắp hết hạn", variant: "warning" },
  // `ShelfStatus.IN_STOCK` (backend thật, ADR-M06-2) — mock trước đây dùng "ACTIVE".
  IN_STOCK: { label: "Đang sử dụng", variant: "success" },
  RECEIVED: { label: "Đã nhận", variant: "success" },
  // Phiên nhận có ÍT NHẤT một lô bị từ chối trả về NCC (Minh yêu cầu 25/07/2026)
  RECEIVED_PARTIAL: { label: "Có lô bị trả về", variant: "warning" },
  // Sample retention (BR-111/112)
  RETAINED: { label: "Đang lưu", variant: "info" },
  DISPOSED: { label: "Đã hủy", variant: "neutral" },
  /** @deprecated 19/07/2026 (review foodsamples Vòng 2): Medical Staff KHÔNG tự thu hồi
   *  nữa — giữ nhãn để đọc được dữ liệu cũ, bản ghi mới dùng 3 trạng thái bên dưới. */
  RETRIEVED: { label: "Đã thu hồi (dữ liệu cũ)", variant: "critical" },
  /**
   * @deprecated Vòng 5 (26/07/2026, Mục 9.2): hai trạng thái trung gian này sinh ra để
   * mô tả việc BÀN GIAO cho Service Caterers (Vòng 2, BR-215). BR-215 nay Deprecated —
   * Medical Staff tự thu hồi và tự vận chuyển mẫu (chống tráo mẫu / chain of custody),
   * nên chuỗi rút còn `RETAINED` → `SENT_TO_LAB`, không còn bước chờ bên khác xác nhận.
   * Giữ nhãn để đọc được dữ liệu cũ; KHÔNG sinh bản ghi mới ở 2 trạng thái này.
   */
  RETRIEVAL_REQUESTED: { label: "Chờ bếp lấy & gửi lab (dữ liệu cũ)", variant: "warning" },
  PENDING_LAB_DELIVERY: { label: "Đang mang tới lab (dữ liệu cũ)", variant: "warning" },
  /**
   * Vòng 1 (19/07/2026) — SENT_TO_LAB là trạng thái CUỐI tại trường, không hoàn tác kể
   * cả khi Incident đóng. Vòng 5 (26/07/2026) — nay là trạng thái kế tiếp TRỰC TIẾP của
   * RETAINED, do chính Medical Staff đặt sau khi tải "Biên bản giao nhận mẫu" (BR-113/BR-212).
   */
  SENT_TO_LAB: { label: "Đã gửi phòng kiểm nghiệm", variant: "critical" },
  /**
   * Thêm 08/08/2026 (backlog B3). Y tế "giữ mẫu tại trường" để điều tra: mẫu vẫn nằm
   * trong tủ của bếp nhưng bếp KHÔNG được hủy (backend chặn bằng SMP-112, khác với
   * SMP-111 "chưa tới hạn"). Trước đó bảng nhãn không có khoá này nên màn Lưu mẫu của
   * Service Caterers in ra chuỗi enum trần `HOLD` — đúng ở ô quan trọng nhất của màn.
   */
  HOLD: { label: "Đang giữ để điều tra", variant: "critical" },
  // Incident
  OPEN: { label: "Đang mở", variant: "danger" },
  INVESTIGATING: { label: "Đang điều tra", variant: "warning" },
  CLOSED: { label: "Đã đóng", variant: "neutral" },
  // Supplier / Partnership (BR-041)
  ACTIVE: { label: "Hoạt động", variant: "success" },
  INACTIVE: { label: "Ngừng hoạt động", variant: "neutral" },
  SUSPENDED: { label: "Tạm đình chỉ", variant: "critical" },
  /**
   * MF01 `iam.domain.UserStatus` — trạng thái KHỞI TẠO của mọi tài khoản mới
   * (BR-010: phải xác minh email trước khi đăng nhập được). Backend đặt giá trị
   * này, FE không tự sinh. Trước 02/08/2026 màn Nhân viên trường dùng `PENDING`
   * + cột `emailVerified` riêng để diễn đạt cùng ý; backend gộp làm một.
   */
  PENDING_VERIFICATION: { label: "Chờ xác minh email", variant: "warning" },
  // Documents (BR-047)
  VALID: { label: "Còn hiệu lực", variant: "success" },
  EXPIRING: { label: "Sắp hết hạn", variant: "warning" },
  URGENT: { label: "Khẩn cấp", variant: "danger" },
  // Delivery (Screen Spec 20/23 — UC-039/UC-092)
  SCHEDULED: { label: "Đã lên lịch", variant: "info" },
  DISPATCHED: { label: "Đã xuất kho", variant: "info" },
  IN_TRANSIT: { label: "Đang giao", variant: "warning" },
  DELAYED: { label: "Trễ lịch", variant: "danger" },
  DELIVERED: { label: "Đã giao", variant: "success" },
  CANCELLED: { label: "Đã hủy", variant: "neutral" },
  // Partnership application (BR-041/BR-048)
  ACTION_REQUIRED: { label: "Cần bổ sung", variant: "warning" },
  // Severity
  LOW: { label: "Thấp", variant: "info" },
  MEDIUM: { label: "Trung bình", variant: "warning" },
  HIGH: { label: "Cao", variant: "danger" },
  CRITICAL: { label: "Nghiêm trọng", variant: "critical" },
  // CAPA — vòng đời BR-216, xem `CorrectiveAction.status`.
  // Bỏ 22/07/2026: ASSIGNED · SUBMITTED · OVERDUE. Chú thích cũ nói "giữ để đọc dữ
  // liệu cũ", nhưng đây là mock DB — không có dữ liệu cũ nào, và không bản ghi nào
  // dùng. Giữ lại chỉ để lại đúng loại "trạng thái không có đường vào" mà đợt rà
  // 22/07 vừa dọn (SFMS-FLOW-REV-001 §4.1).
  COMPLETED: { label: "Hoàn thành", variant: "success" },
  // Health review
  VERIFIED: { label: "Đã xác minh", variant: "success" },
  /*
   * "Chưa khai báo" TÁCH khỏi "Đã xác minh" (08/08/2026). Backend trước đây suy
   * `healthStatus = VERIFIED` khi học sinh không có khai báo nào (luật kế thừa từ seed mock),
   * nên học sinh vừa tạo đã mang nhãn "Đã xác minh" ở cột Hồ sơ sức khỏe — trong khi chưa ai
   * khai gì và Y tế chưa xem. Cùng lớp lỗi "chưa khai ≠ đã xác minh" đã bỏ ở NUTRITION_TARGET.
   */
  NO_DECLARATION: { label: "Chưa khai báo", variant: "neutral" },
  NEEDS_INFO: { label: "Cần bổ sung", variant: "warning" },
  // Violation Management (§18, BR-183..BR-190 — enum ViolationStatus class diagram)
  REPORTED: { label: "Đã ghi nhận", variant: "danger" },
  UNDER_REVIEW: { label: "Đang xem xét", variant: "warning" },
  RESOLVED: { label: "Đã xử lý", variant: "success" },
  ESCALATED: { label: "Chờ BGH phê duyệt", variant: "critical" },
  // Incident (enum IncidentStatus class diagram)
  VALIDATED: { label: "Y tế đã xác nhận", variant: "success" },
  // Incident — state machine mới (review_medicalstaff_incidentmanagement.md Mục 7.5/9.5, chốt 09/07/2026)
  SEVERITY_PENDING: { label: "Chờ đánh giá mức độ", variant: "warning" },
  TREATMENT: { label: "Đang xử lý sơ cứu", variant: "info" },
  CAPA_PENDING: { label: "Chờ duyệt CAPA", variant: "warning" },
  CAPA_APPROVED: { label: "Đang triển khai CAPA", variant: "info" },
  // IncidentSourceReport (RPT-xxx — báo cáo thô Teacher/Parent, Mục 9.1)
  PENDING_TRIAGE: { label: "Chờ tiếp nhận", variant: "warning" },
  MERGED: { label: "Đã gộp vào phiên điều tra", variant: "info" },
  CLOSED_NOT_FOOD_SAFETY: { label: "Đã đóng — Không liên quan ATTP", variant: "neutral" },
  // Mục 10.3 (15/07/2026): trạng thái biên bản sự cố
  DRAFTING: { label: "Đang soạn biên bản (nháp)", variant: "warning" },
  DRAFTED: { label: "Đã hoàn tất biên bản", variant: "info" },
  OPENED: { label: "Đã mở phiên điều tra", variant: "success" },
  // Tiêu thụ bữa ăn (BR-121)
  FULLY: { label: "Ăn hết", variant: "success" },
  PARTIALLY: { label: "Ăn một phần", variant: "warning" },
  NOT_CONSUMED: { label: "Không ăn", variant: "danger" },
  // Phân phát theo học sinh (BR-120)
  SERVED: { label: "Đã phục vụ", variant: "success" },
  // --- Vòng đời CAPA chuẩn BR-216 (21/07/2026) ---
  // DRAFT / IN_PROGRESS / COMPLETED tái dùng key sẵn có ở trên.
  PENDING_ADMIN_APPROVAL: { label: "Chờ BGH duyệt kế hoạch", variant: "warning" },
  APPROVED_FOR_IMPLEMENTATION: { label: "Đã duyệt — chờ thực hiện", variant: "info" },
  SUBMITTED_EVIDENCE: { label: "Đã nộp bằng chứng", variant: "warning" }
};

/*
 * `SchoolConfig`/`SCHOOL_CONFIG` — ĐÃ BỎ 06/08/2026. Nối thật qua `GET|PATCH
 * /schools/{code}/config` (`SchoolConfigController`, 1 object không phân trang) —
 * `school-admin/config/page.tsx` và `menu-form.tsx` (SERVICE_CATERERS, mở thêm
 * @PreAuthorize GET-only vì costCeiling chỉ tham chiếu, không nhạy cảm) đều đã đổi sang
 * `useSubDetail("schools", schoolId, "config")`.
 */

// ===== Thực đơn (MF Menu, BR-061..BR-082) =====
// Rework 11/07/2026 (voice review "Thạch Bàn 2"): thực đơn theo NGÀY — mỗi
// bản ghi = 1 ngày × 1 khối; bỏ mô hình tuần/range. `week` giữ tên field để
// không vỡ các trang khác nhưng ngữ nghĩa mới = NHÃN NGÀY ("Thứ 2 — 13/07/2026").

/**
 * Thực đơn đã có hiệu lực phục vụ.
 *
 * Đến 22/07/2026 vòng đời thực đơn có CẢ `APPROVED` lẫn `PUBLISHED`, và mọi nơi
 * tiêu thụ đều lọc `["PUBLISHED", "APPROVED"]` như nhau — tức hai tên cho cùng một
 * trạng thái. Màn duyệt của Ban giám hiệu chỉ sinh ra `PUBLISHED` và không có bước
 * công bố riêng nào sau khi duyệt, nên `APPROVED` chỉ tồn tại trong seed. Gộp về
 * `PUBLISHED`; backend cũng chỉ nên có một trạng thái này.
 */
export function isMenuLive(menu: { status: string }): boolean {
  return menu.status === "PUBLISHED";
}

export interface MenuItem {
  id: string;
  /** Nhãn ngày áp dụng hiển thị — "Thứ 2 — 13/07/2026" (tên field giữ nguyên vì nhiều trang dùng làm label). */
  week: string;
  name: string;
  /** Thực đơn quản lý theo từng khối lớp — "Khối 1".."Khối 5". */
  grade: string;
  /** Ngày áp dụng ISO (yyyy-mm-dd) — thực đơn theo NGÀY, dùng để sort/filter. */
  menuDateIso?: string;
  status: keyof typeof STATUS_BADGE;
  dishes: number;
  allergyConflicts: number;
  estimatedCost: number;
  costCeiling: number;
  submittedAt?: string;
  /** Nhãn hiển thị đơn vị lập thực đơn; khoá thật là `createdBySupplierId`. */
  createdBy: string;
  createdBySupplierId?: string;
  /** BR-066: người duyệt + chữ ký điện tử, ghi khi School Administration phê duyệt. */
  approvedBy?: string;
  approvalSignature?: string;
  /** BR-048: lý do School Administration từ chối — Service Caterers đọc để sửa & nộp lại. */
  rejectReason?: string;
  /**
   * ĐỐI TƯỢNG PHỤC VỤ của thực đơn (22/07/2026 — gộp `MenuVariant` vào `Menu`).
   *
   *   "ALL"          → thực đơn chung của khối (mọi học sinh).
   *   <DietGroup.id> → THỰC ĐƠN RIÊNG, ĐẦY ĐỦ cho một nhóm chế độ ăn (chay, Halal,
   *                    tiểu đường…) — tự thân trọn vẹn như một thực đơn thường,
   *                    KHÔNG phải "lấy thực đơn chuẩn rồi thay từng món".
   *
   * Backend: chỉ một bảng `Menu`, phân biệt bằng cột này. Không còn khái niệm
   * variant/delta ở tầng chế độ ăn. Dị ứng vẫn thay TỪNG MÓN ngay trong thực đơn
   * (`DishForm.substitutes`), và chỉ phát sinh khi khối có HS đã xác minh dị ứng.
   */
  audience?: MenuAudience;
  /** Nhóm chế độ ăn của thực đơn riêng — tiện đọc, = `audience` khi khác "ALL". */
  dietGroupId?: string;
  /** Bắt buộc khi thực đơn riêng được thêm SAU khi thực đơn chung đã công bố. */
  reason?: string;
  approvedAt?: string;
  createdAt?: string;
}

/** "ALL" (khối) hoặc một `DietGroup.id` (thực đơn riêng của nhóm chế độ ăn). */
export type MenuAudience = "ALL" | string;

/** Đối tượng phục vụ của thực đơn; record cũ chưa có trường thì coi là "ALL". */
export function menuAudienceOf(m: { audience?: MenuAudience }): MenuAudience {
  return m.audience ?? "ALL";
}

/** Thực đơn riêng cho nhóm chế độ ăn (audience khác "ALL"). */
export function isSpecialMenu(m: { audience?: MenuAudience }): boolean {
  return menuAudienceOf(m) !== "ALL";
}

// ---- Seed chi tiết buổi ăn/món — shape khớp MealBlock/DishForm của menu-form.tsx
// (định nghĩa cục bộ để tránh import vòng; db.ts vốn erase type khi clone) ----

interface SeedIngredientRow {
  name: string;
  quantity: string;
  unit: string;
  /** Khoá về danh mục — nối ở vòng backfill, không khai tay trong seed. */
  ingredientId?: string;
}

interface SeedSubstituteDish {
  /** Khoá về THƯ VIỆN MÓN — backfill cùng lúc với món chính. */
  dishId?: string;
  name: string;
  ingredients: SeedIngredientRow[];
  manualAllergens: string[];
  kcal: string;
  portions: string;
  /** Tác nhân mà suất riêng này tránh — nối ở vòng backfill. */
  allergenIds?: string[];
  note: string;
}

interface SeedDish {
  /** Khoá về THƯ VIỆN MÓN — backfill sau khi DISH_LIBRARY khai báo (xem cuối mục đó). */
  dishId?: string;
  name: string;
  ingredients: SeedIngredientRow[];
  manualAllergens: string[];
  kcal: string;
  protein: string;
  carb: string;
  fat: string;
  isSpecialDish: boolean;
  /** Nhiều suất theo nhóm — một món có thể phục vụ vừa suất dị ứng vừa suất chay. */
  substitutes?: SeedSubstituteDish[];
}

/** Suất đặc biệt của 1 món seed. Ô đơn `substitute` đã gỡ hẳn ở Đợt D. */
function seedSubsOf(d: SeedDish): SeedSubstituteDish[] {
  return d.substitutes ?? [];
}

export type MealType = "BREAKFAST" | "LUNCH" | "SNACK";

/** Nhãn tiếng Việt của buổi ăn — dùng chung cho mọi màn thay vì hardcode rải rác. */
export const MEAL_TYPE_LABEL: Record<MealType, string> = {
  BREAKFAST: "Bữa sáng",
  LUNCH: "Bữa trưa",
  // 25/07/2026 (Minh): 3 bữa thống nhất gọi Sáng / Trưa / Chiều.
  SNACK: "Bữa chiều",
};

export interface SeedMealBlock {
  mealType: MealType;
  dishes: SeedDish[];
}

/**
 * Thực đơn kèm khối dữ liệu bữa — dạng các màn lập/duyệt thực đơn làm việc.
 * Có tên riêng để nơi khác lấy được KIỂU mà không phải import cả mảng seed.
 */
export type SeedMenu = MenuItem & { blocksData?: SeedMealBlock[]; servings?: number };

// ===== Nhận nguyên liệu (MF06, BR-070..BR-078) =====

/**
 * `ReceivingLine`/`ReceivingSession` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0
 * nơi dùng): `service-caterers/receiving/page.tsx` đọc thẳng `ReceivingSessionDto`
 * (= `Api<"ReceivingSessionResponse">`, có `lines?: ReceivingLineResponse[]`).
 */

// ===== Kho nguyên liệu (BR-075 FEFO, BR-202, BR-205) =====

export interface InventoryBatch {
  id: string;
  /** Khóa join về INGREDIENT_CATALOG — BR-076 (xem ghi chú tại CatalogIngredient). */
  ingredientId: string;
  /** Nhãn hiển thị của lô; có thể khác tên catalog ("… (bổ sung)", "… đông lạnh"). */
  ingredient: string;
  supplier: string;
  quantity: string;
  storageZone: "Dry" | "Chilled" | "Frozen";
  receivedDate: string;
  /**
   * Giờ nhận hàng THẬT (ISO Instant, `InventoryBatchResponse.receivedAt` — thêm 10/08/2026) —
   * lấy từ `receiving_sessions.received_at` (BR-073, server ghi). `undefined` với lô không gắn
   * phiên nhận nào (dữ liệu cũ/điều chỉnh tay) — Bước 1 kiểm thực khi đó vẫn phải cho gõ tay.
   */
  receivedAtIso?: string;
  expiryDate: string;
  /**
   * Trạng thái HẠN DÙNG — ACTIVE / EXPIRING_SOON / EXPIRED, suy từ `expiryDate`
   * (xem `batchShelfStatus`). Seed giữ giá trị cố định vì mốc thời gian của seed
   * neo quanh 06–07/2026; backend nên tính tại chỗ thay vì lưu cột này.
   *
   * Trước 22/07/2026 cột này còn mang cả giá trị QUARANTINED, tức một cột gánh hai
   * khái niệm độc lập: hạn dùng là dữ kiện, cách ly là QUYẾT ĐỊNH của người điều
   * tra. Cách ly một lô đang EXPIRING_SOON là ghi đè mất thông tin hạn, và nhả
   * cách ly xong thì không biết trả về trạng thái nào.
   */
  status: keyof typeof STATUS_BADGE;
  /**
   * Cách ly phục vụ điều tra (BR-149) — độc lập với hạn dùng.
   *
   * Nhận `null` chứ không chỉ `undefined`: gỡ cách ly phải gửi `quarantine: null`
   * qua API. `JSON.stringify` XOÁ HẲN các khoá mang giá trị `undefined`, nên
   * `{ quarantine: undefined }` lên tới server thành body rỗng — lệnh nhả chạy
   * xong, toast báo thành công, mà lô vẫn khoá nguyên. Mọi chỗ đọc đều so
   * `== null` nên `null` hoạt động đúng cho cả hai nghĩa.
   */
  quarantine?: {
    at: string;
    by: string;
    reason: string;
    incidentId?: string;
  } | null;
}

/** Trạng thái hạn dùng của lô — dẫn xuất từ hạn, không khai tay. */
export function batchShelfStatus(expiryDate: string, today = new Date()): "ACTIVE" | "EXPIRING_SOON" | "EXPIRED" {
  const t = parseVnDate(expiryDate);
  if (!Number.isFinite(t)) return "ACTIVE";
  const days = (t - today.getTime()) / 86400000;
  if (days < 0) return "EXPIRED";
  if (days <= 2) return "EXPIRING_SOON";
  return "ACTIVE";
}

/** Nhãn hiển thị: lô bị cách ly thì cách ly lấn át hạn dùng. */
export function batchDisplayStatus(b: Pick<InventoryBatch, "status" | "quarantine">): string {
  return b.quarantine ? "QUARANTINED" : b.status;
}

// ===== Kiểm thực ba bước (MF07, BR-079..BR-099, QĐ 1246/QĐ-BYT) =====

/**
 * Các trường chi tiết theo ĐÚNG Mẫu số 1/2/3 — Phụ lục 1, QĐ 1246/QĐ-BYT
 * (00_Policies/1246_QD-BYT...md). Đây là biểu mẫu luật — không được thiếu cột;
 * bản in (print) phải render đúng format bảng của Bộ Y tế.
 */

/** Mẫu số 1 — phần I: thực phẩm tươi sống, đông lạnh (15 cột). */
export interface Step1FreshRow {
  name: string; // (2) Tên thực phẩm
  /**
   * D1 — MÃ LÔ kho đã xuất cho bữa này (`InventoryBatch.id`, VD "SB-THIT-HEO").
   *
   * Nối 08/08/2026 (backlog A1-FE). Backend `Step1FreshRowDto`/`Step1DryRowDto` có field này
   * và dùng nó cho BR-092/093 (đối chiếu hạn dùng lô lúc kiểm thực) + truy xuất nguồn gốc khi
   * có sự cố. FE trước đây KHÔNG gửi ⇒ cột `step1_*_rows.batch_id` luôn NULL ⇒ hai business
   * rule đó chưa từng chạy, và biên bản Mẫu 1 không truy ngược được về lô.
   */
  batchId?: string;

  receivedAt: string; // (3) Thời gian nhập (ngày, giờ)
  /** Giờ ở `receivedAt` là THẬT (từ `InventoryBatch.receivedAtIso`, BR-073) — khoá luôn ô giờ, không cho gõ. */
  receivedAtIsReal?: boolean;
  quantity: string; // (4) Khối lượng (kg/lít…)
  supplierName: string; // (5) Nơi cung cấp — tên cơ sở
  supplierContact: string; // (6) — địa chỉ, điện thoại
  deliverer: string; // (7) — tên người giao hàng
  hasInvoice: boolean; // (8) Chứng từ, hóa đơn
  hasVetCert: boolean; // (9) Giấy ĐK VS thú y
  hasQuarantineCert: boolean; // (10) Giấy kiểm dịch
  sensoryPass: boolean; // (11)/(12) Kiểm tra cảm quan Đạt / Không đạt
  quickTest: "PASS" | "FAIL" | "NONE"; // (13)/(14) Xét nghiệm nhanh (nếu có)
  action: string; // (15) Biện pháp xử lý / Ghi chú
  /** Hệ thống (không thuộc mẫu 1246): dòng Không đạt bắt buộc ảnh bằng chứng — BR-083. */
  photoTaken?: boolean;
  /** Số ảnh thực đã đính kèm — chụp/không đạt nhiều ảnh (không chỉ 1). */
  photoCount?: number;
}

/** Mẫu số 1 — phần II: thực phẩm khô, bao gói sẵn, phụ gia (15 cột). */
export interface Step1DryRow {
  name: string; // (2) Tên thực phẩm
  /**
   * D1 — MÃ LÔ kho đã xuất cho bữa này (`InventoryBatch.id`, VD "SB-THIT-HEO").
   *
   * Nối 08/08/2026 (backlog A1-FE). Backend `Step1FreshRowDto`/`Step1DryRowDto` có field này
   * và dùng nó cho BR-092/093 (đối chiếu hạn dùng lô lúc kiểm thực) + truy xuất nguồn gốc khi
   * có sự cố. FE trước đây KHÔNG gửi ⇒ cột `step1_*_rows.batch_id` luôn NULL ⇒ hai business
   * rule đó chưa từng chạy, và biên bản Mẫu 1 không truy ngược được về lô.
   */
  batchId?: string;

  producer: string; // (3) Tên cơ sở sản xuất
  producerAddress: string; // (4) Địa chỉ sản xuất
  receivedAt: string; // (5) Thời gian nhập
  /** Giờ ở `receivedAt` là THẬT (từ `InventoryBatch.receivedAtIso`, BR-073) — khoá luôn ô giờ, không cho gõ. */
  receivedAtIsReal?: boolean;
  quantity: string; // (6) Khối lượng
  supplierName: string; // (7) Nơi cung cấp — tên cơ sở
  deliverer: string; // (8) — tên chủ giao hàng
  supplierContact: string; // (9) — địa chỉ, điện thoại
  expiry: string; // (10) Hạn sử dụng
  storage: "Thường" | "Lạnh" | "Đông"; // (11) Điều kiện bảo quản
  hasInvoice: boolean; // (12) Chứng từ, hóa đơn
  sensoryPass: boolean; // (13)/(14) Cảm quan Đạt / Không đạt
  action: string; // (15) Biện pháp xử lý / Ghi chú
  /** Hệ thống: dòng Không đạt bắt buộc ảnh bằng chứng — BR-083. */
  photoTaken?: boolean;
  /** Số ảnh thực đã đính kèm — chụp/không đạt nhiều ảnh (không chỉ 1). */
  photoCount?: number;
}

/** Mẫu số 2 — kiểm tra khi chế biến (13 cột). */
export interface Step2Row {
  shift: string; // (2) Ca/bữa ăn (bữa, giờ ăn)
  /**
   * Mã món (`dishes.code`) — KHÓA nối sang bước lưu mẫu.
   *
   * Thêm 07/08/2026. Backend `Step2RowDto`/`Step3RowDto` VẪN LUÔN có `dishId`, nhưng
   * interface FE này thiếu nó và `step2RowsOfMeal()` không set ⇒ `step2_rows.dish_id`
   * luôn NULL ⇒ `dishChoicesOf()` (màn Lưu mẫu) lọc `r.dishId` ra MẢNG RỖNG ⇒ dialog
   * "Đăng ký lưu mẫu" không có món nào để chọn, mà `RegisterSampleRequest.dishId` là
   * `@NotBlank`. Tức là **không lưu mẫu thực phẩm được** (QĐ 1246 Mẫu 4/5, BR-110/111).
   *
   * Lỗi lọt được vì nơi nộp ép kiểu `as unknown as SubmitStepDto["step2Rows"]` — đã bỏ ép
   * kiểu đó, đừng thêm lại.
   */
  dishId: string;
  dishName: string; // (3) Tên món ăn
  mainIngredients: string; // (4) Nguyên liệu chính (tên, số lượng)
  servings: string; // (5) Số lượng/số suất ăn
  prepDoneAt: string; // (6) Thời gian sơ chế xong (ngày, giờ)
  cookDoneAt: string; // (7) Thời gian chế biến xong (ngày, giờ)
  hygieneStaff: boolean; // (8) VS người tham gia chế biến
  hygieneEquipment: boolean; // (9) VS trang thiết bị dụng cụ
  hygieneArea: boolean; // (10) VS khu vực chế biến và phụ trợ
  sensoryPass: boolean; // (11)/(12) Cảm quan thức ăn Đạt / Không đạt
  action: string; // (13) Biện pháp xử lý / Ghi chú
  /** Hệ thống: món Không đạt bắt buộc ảnh bằng chứng — BR-083. */
  photoTaken?: boolean;
  /** Số ảnh thực đã đính kèm — chụp/không đạt nhiều ảnh (không chỉ 1). */
  photoCount?: number;
}

/** Mẫu số 3 — kiểm tra trước khi ăn (10 cột). */
export interface Step3Row {
  shift: string; // (2) Ca/bữa ăn
  /** Mã món — cùng lý do như `Step2Row.dishId`, xem Javadoc ở đó. */
  dishId: string;
  dishName: string; // (3) Tên món ăn
  servings: string; // (4) Số lượng suất ăn
  dividedDoneAt: string; // (5) Thời gian chia món ăn xong (ngày, giờ)
  eatStartAt: string; // (6) Thời gian bắt đầu ăn (ngày, giờ)
  /** (7) Dụng cụ chia, chứa đựng, che đậy ĐẠT yêu cầu vệ sinh — V56, 08/08/2026. */
  utensilsPass: boolean;
  /** (7) Ghi chú kèm theo — loại dụng cụ, hoặc lý do khi Không đạt (trước đây là ô CHÍNH). */
  utensils: string;
  sensoryPass: boolean; // (8)/(9) Cảm quan món ăn Đạt / Không đạt
  action: string; // (10) Biện pháp xử lý / Ghi chú
  /** Hệ thống: món Không đạt bắt buộc ảnh bằng chứng — BR-083. */
  photoTaken?: boolean;
  /** Số ảnh thực đã đính kèm — chụp/không đạt nhiều ảnh (không chỉ 1). */
  photoCount?: number;
}

/** Header chung mỗi mẫu biểu: Tên cơ sở / Người kiểm tra / Thời gian / Địa điểm. */
export interface ThreeStepFormHeader {
  facility: string;
  inspector: string;
  checkedAt: string;
  location: string;
}

export interface InspectionSession {
  id: string;
  date: string;
  meal: "Bữa sáng" | "Bữa trưa" | "Bữa chiều";
  step1: keyof typeof STATUS_BADGE;
  step2: keyof typeof STATUS_BADGE;
  step3: keyof typeof STATUS_BADGE;
  overall: keyof typeof STATUS_BADGE;
  coreTemp?: number; // BR-090: >= 75°C (yêu cầu HỆ THỐNG, không thuộc mẫu 1246)
  inspector: string;
  coSigner?: string;
  /** Chi tiết theo mẫu biểu QĐ 1246 — điền trong workspace, dùng cho bản in. */
  formHeader?: ThreeStepFormHeader;
  step1Fresh?: Step1FreshRow[];
  step1Dry?: Step1DryRow[];
  step2Rows?: Step2Row[];
  step3Rows?: Step3Row[];
  /** UC-112 field #3 (filter theo thực đơn/món) + field #6 (chi tiết): ảnh, GPS, ghi chú */
  dish?: string;
  /** Khoá về THƯ VIỆN MÓN — nối ở vòng backfill. */
  dishId?: string;
  evidencePhotos?: number; // ảnh bằng chứng kèm timestamp (BR-108)
  gps?: string; // vị trí GPS lúc ghi nhận
  staffNotes?: string; // ghi chú của nhân viên thực hiện
  /**
   * `review_medicalstaff_compliance_kitcheninspection.md` Mục 8.5 điểm 1 (26/07/2026):
   * đánh giá RIÊNG của Medical Staff khi xem lại phiên kiểm thực — TÁCH BIỆT với
   * `overall` (vốn tự tính theo BR-090/093/094 từ số liệu Service Caterers nhập).
   * KHÔNG ghi đè `overall`; hiển thị song song để phân biệt "kết quả tự tính theo
   * bước" và "nhận định bổ sung của Y tế". Lý do bắt buộc khi đánh giá Không đạt.
   */
  medicalReviewResult?: "PASSED" | "FAILED";
  medicalReviewReason?: string;
  medicalReviewBy?: string;
  medicalReviewAt?: string;
}

// ===== Catalog AUTO-FILL cho mẫu biểu kiểm thực (nhận xét Minh 13/07/2026) =====
// Trong form kiểm thực, chọn thực phẩm/món từ dropdown → MỌI cột tự điền từ
// dữ liệu nhập kho/thực đơn (mock), người dùng không phải gõ tay từng món.
// Quy ước: các field ngày-giờ trong catalog CHỈ giữ "HH:mm" — trang phiên
// kiểm thực sẽ tự ghép với ngày của phiên.

/** Mẫu 1 — phần I: thực phẩm tươi sống/đông lạnh nhập trong ngày (từ phiên nhận hàng). */

/** Mẫu 1 — phần II: thực phẩm khô/bao gói sẵn đang có trong kho. */

/** Mẫu 2 — món ăn theo thực đơn đã duyệt (nguyên liệu, suất, giờ sơ chế/chế biến mock). */

/** Mẫu 3 — món đã chia suất, chờ giờ ăn (dụng cụ chia/che đậy mock). */

// ===== Lưu mẫu thực phẩm (MF08, BR-105..BR-114, BR-203) =====

/**
 * Lưu mẫu thức ăn theo QĐ 1246/QĐ-BYT Chương III + Phụ lục 2:
 * Mẫu số 4 (nhãn: bữa ăn, tên mẫu, thời gian lấy, người lấy + chữ ký) và
 * Mẫu số 5 (sổ theo dõi 12 cột: TT, tên mẫu, bữa ăn/giờ ăn, số suất, khối
 * lượng/thể tích, dụng cụ chứa, nhiệt độ bảo quản, TG lấy, TG hủy, ghi chú,
 * người lưu, người hủy). Điều 6: dụng cụ nắp kín + tiệt trùng · Điều 7: mỗi
 * món 1 dụng cụ riêng, niêm phong, ≥100g đặc/≥150ml lỏng · Điều 8: 2–8°C,
 * lưu ≥24h.
 *
 * BR-111 = 24 giờ (sửa 26/07/2026, review foodsamples Mục 9.1). Trước đó hệ thống dùng
 * 72 giờ với lý giải "buffer an toàn cao hơn luật"; lý giải đó bỏ vì mẫu bảo quản ở ngăn
 * MÁT 2–8°C (Điều 8.1/BR-110), không phải cấp đông — giữ quá lâu thì mẫu tự xuống cấp và
 * mất giá trị xét nghiệm, tức kéo dài KHÔNG còn là an toàn hơn.
 */
/**
 * `FoodSample`/`SampleLabRequest` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi
 * dùng làm kiểu resource, chỉ còn nhắc tới trong comment): `samples`/`sample-lab-requests`
 * nối thật TRỌN VẸN 05/08/2026 — shape lệch đủ sâu (nhiều field tự do, xem
 * `contract-check.ts` mục MF08) nên mọi màn tiêu thụ đã đổi hẳn sang đọc `FoodSampleDto`/
 * `SampleLabRequestDto` (= `Api<"FoodSampleResponse">`/`Api<"SampleLabRequestResponse">`)
 * trực tiếp; `sample-law-print.tsx` tự khai shape cục bộ tối thiểu để in Mẫu 4/5.
 */

// ===== Sự cố (MF11, BR-146..BR-155) =====

/**
 * IncidentSourceReport (mã RPT-xxx) — báo cáo thô do Teacher (UC-074)/Parent (UC-073) tạo.
 * Chốt 09/07/2026 (review_medicalstaff_incidentmanagement.md Mục 7.3/9.1): quan hệ N-1 —
 * nhiều RPT được Medical Staff chủ động gộp (multi-select, KHÔNG tự động) thành 1 INC-xxx;
 * "Đóng — Không liên quan ATTP" áp dụng ngay ở mức RPT đơn lẻ, không cần tạo INC (E3, Mục 7.8).
 * severityLevel chỉ đọc — populate SAU khi Medical Staff hoàn tất Step 1 (BR-147, Mục 7.2).
 */
/**
 * Học sinh bị ảnh hưởng trong một sự cố.
 *
 * Trước 22/07/2026 báo cáo thô lưu MẢNG TÊN, còn sự cố chỉ lưu MỘT CON SỐ đếm.
 * Hai hệ quả: (1) tên không nối được về hồ sơ nào — seed từng ghi "Vũ Hải Đăng"
 * là học sinh không hề tồn tại mà không ai phát hiện; (2) biết "4 học sinh" mà
 * không biết là em nào thì không thể đối chiếu với sổ phát suất, không gọi được
 * phụ huynh, và Y tế không lần ra em nào đã ăn món nghi vấn.
 */
export interface IncidentAffectedStudent {
  studentId: string;
  /** Chụp lại lúc ghi nhận — tên hiển thị không đổi kể cả hồ sơ đổi sau này. */
  studentName: string;
  className: string;
}

export interface IncidentSourceReport {
  id: string; // = code RPT-YYYY-NNN
  reportedBy: string; // Teacher/Parent + vai trò
  reportedAt: string;
  className: string;
  affectedStudents: IncidentAffectedStudent[];
  symptoms: string[];
  /** Thông tin mô tả thô — KHÔNG liên kết tự động BR-151 (Mục 7.2) */
  suspectedMealText: string;
  mealDate: string;
  description: string;
  evidencePhotos: number;
  severityLevel?: "LOW" | "MEDIUM" | "HIGH"; // read-only, sau Step 1
  /**
   * Mục 10.3.5 (15/07/2026): thêm DRAFTING (đang soạn biên bản nháp) và DRAFTED
   * (đã hoàn tất biên bản, chờ quyết định đóng/mở phiên điều tra) — chen giữa
   * PENDING_TRIAGE và MERGED/CLOSED_NOT_FOOD_SAFETY.
   */
  status: "PENDING_TRIAGE" | "DRAFTING" | "DRAFTED" | "MERGED" | "CLOSED_NOT_FOOD_SAFETY";
  mergedIntoIncidentId?: string;
  draftRecordId?: string; // biên bản (IncidentRecordDraft) đang gắn với RPT này
}

/**
 * Biên bản sự cố (Mục 10.3, spec 33_CreateIncidentRecordDialog — 15/07/2026, UC-114 mở rộng).
 * Bước giữa mới: Medical Staff soạn văn bản chính thức từ 1+ RPT trước khi quyết định
 * đóng/mở phiên điều tra (đảo ngược quyết định "gộp 1 bước" 09/07/2026 — Mục 7.8).
 * Cấu trúc rút từ mẫu 00_Policies/specialreport2173_1.md, cùng phương pháp BR-211.
 */
export interface IncidentRecordDraft {
  id: string; // REC-xxx
  sourceReportIds: string[];
  content: string; // Nội dung biên bản — Medical Staff tự viết (BR-154)
  status: "DRAFT" | "COMPLETED" | "OPENED" | "CLOSED_NOT_FOOD_SAFETY";
  createdBy: string;
  createdAt: string;
  updatedAt?: string;
  incidentId?: string; // caseCode INC-xxx sau khi "Mở phiên điều tra" (Mục 10.3.4)
  /**
   * Design pattern vòng 2 (18/07/2026, Mục 5.3 QĐ 4): 2 field mới theo cấu trúc
   * specialreport2173_1.md — dùng tự sinh câu mở đầu "Vào hồi [giờ] ngày [ngày],
   * tại [địa điểm]..." trong PDF. Nằm trong phạm vi BR-154, không cấp BR mới.
   */
  location?: string; // Địa điểm xảy ra sự cố
  occurredAt?: string; // Thời điểm xảy ra sự cố (khác "Thời điểm báo cáo" của RPT)
}

/**
 * Mục 8.3 bug 2 (design pattern vòng 3, 19/07/2026): MỌI Incident hợp lệ đều phải có
 * biên bản gốc (luồng 2 bước — BR-146 chỉ là tín hiệu, KHÔNG bỏ qua bước soạn biên bản).
 * Seed đủ 1 REC/1 INC — trước đây [] khiến Section A hiện "không có biên bản gốc".
 */


/** UC-041 (#21, review §7.1a): 1 mục bằng chứng điều tra — lưu thật vào bản ghi sự cố */
export interface InvestigationEvidence {
  type: string;
  description: string;
  files: number;
}

/**
 * Gộp học sinh bị ảnh hưởng từ nhiều báo cáo về một sự cố, khử trùng theo mã HS.
 * `new Set(...)` không dùng được nữa vì phần tử là object — hai báo cáo cùng nhắc
 * một em sẽ cho hai tham chiếu khác nhau và đếm thành hai người.
 */
export function uniqueAffected(list: IncidentAffectedStudent[]): IncidentAffectedStudent[] {
  const seen = new Map<string, IncidentAffectedStudent>();
  for (const s of list) if (!seen.has(s.studentId)) seen.set(s.studentId, s);
  return [...seen.values()];
}

export interface Incident {
  id: string;
  title: string;
  reportedAt: string;
  /**
   * Thời điểm XẢY RA sự cố — khác `reportedAt` (thời điểm lập biên bản/tiếp nhận).
   *
   * Thêm 27/07/2026 (`review_medicalstaff_foodsamples.md` Mục 13.4). Đính chính so với
   * mô tả trong review: field `occurredAt` sẵn có ở dòng ~1323 thuộc `IncidentRecordDraft`
   * (biên bản gốc), KHÔNG phải `Incident` — nên ở đây là field mới thật sự.
   *
   * Dùng làm mốc gốc số 1 để tính cửa sổ điều tra 24h (BR-149 sửa 27/07/2026). Khi thiếu,
   * `incidents/[id]/page.tsx` lùi về `occurredAt` của biên bản gốc → `mealDate` →
   * `reportedAt`. Định dạng: `dd/MM/yyyy HH:mm` (khác biên bản gốc dùng `yyyy-MM-ddTHH:mm`
   * của `<input type="datetime-local">` — hàm đọc mốc xử lý được cả hai).
   */
  occurredAt?: string;
  reportedBy: string;
  /** Danh sách thật, không phải số đếm — số học sinh ảnh hưởng là `.length`. */
  affectedStudents: IncidentAffectedStudent[];
  severity: keyof typeof STATUS_BADGE;
  status: keyof typeof STATUS_BADGE;
  symptoms: string[];
  /** BR-146: món ăn nghi vấn + thời điểm ăn — hiển thị ở incident-detail-view dùng chung (School Admin/Teacher) */
  suspectedDish?: string;
  mealTime?: string;
  /** Sơ cứu đã thực hiện (hợp nhất từ bản Medical Staff — 26_IncidentDetail) */
  firstAid?: string;
  /**
   * #21 (06/07/2026, review §7.1a): dữ liệu Investigation Workspace (UC-041) persist
   * vào bản ghi sự cố — trước đây chỉ useState nội bộ, F5 mất toàn bộ.
   */
  investigationEvidence?: InvestigationEvidence[];
  investigationLabResult?: string;
  /** Số file kết quả xét nghiệm đã tải lên (mock — cùng cơ chế đếm với InvestigationEvidence.files). */
  investigationLabResultFiles?: number;
  /** #23: nguồn pre-fill duy nhất cho "Root cause determination" ở UC-033 Step 3 (BR-150) */
  investigationRootCause?: string;
  /** Mục 11.9.1 (đợt 2): root cause 1 bản ghi trạng thái Sơ bộ → Chính thức (thay 2 Textarea). */
  investigationRootCauseStatus?: "PRELIMINARY" | "FINAL";
  /**
   * Redesign 09/07/2026 (review_medicalstaff_incidentmanagement.md Mục 9.1) — progressive
   * disclosure theo state machine Mục 7.5. status dùng thêm các giá trị mới:
   * SEVERITY_PENDING → TREATMENT → INVESTIGATING → CAPA_PENDING → CAPA_APPROVED →
   * (VALIDATED) → CLOSED.
   */
  sourceReportIds?: string[]; // N-1: các RPT-xxx gốc đã gộp (Mục 7.3)
  suspectedMeal?: string; // free-text kế thừa từ RPT gốc — CHỈ tham khảo, không binding (Mục 7.7 đợt 5)
  mealDate?: string; // "Ngày ăn" đã xác nhận (dd/MM/yyyy) — nguồn để lấy danh sách món của bữa
  firstAidNotes?: string; // E5: nhập ở trạng thái TREATMENT, bắt buộc trước khi điều tra
  /**
   * Mục 12.8.6 (15/07/2026, đợt 3) — "Lưu bản nháp" cho khối đánh giá gộp: giữ lại
   * "Đánh giá rủi ro lâm sàng & khuyến nghị" (UC-009) khi lưu dở, KHÔNG đổi status,
   * KHÔNG khóa lô. Trước đây field này chỉ validate mà không persist.
   */
  clinicalRiskAssessment?: string;
  /**
   * Quarantine 1 LỚP DUY NHẤT (Mục 7.7 — sửa 09/07/2026 đợt 5, thay mô hình 2 lớp cũ):
   * Medical Staff multi-select "món ăn nghi vấn" từ Menu đúng Ngày ăn (structured — KHÔNG
   * suy diễn từ suspectedMealText) → hệ thống khóa đúng lô nguyên liệu của các món đó
   * (BR-149). Lý do bỏ khóa tự động toàn bữa: chi phí xét nghiệm ~5-10 triệu VNĐ/mẫu.
   */
  suspectedDishIds?: string[];
  quarantineBatches?: string[]; // lô bị khóa — đúng theo suspectedDishIds
  traceabilityMapStatus?: "COMPILING" | "READY"; // BR-151: đang biên soạn (ETA 4h) / đã đầy đủ
  /**
   * review_medicalstaff_design_pattern.md Mục 3 QĐ 2+5 (18/07/2026): cờ "Đã xuất
   * báo cáo — Read-only". Bật khi "Export & Submit Incident Reports" (Section D)
   * chạy thành công → toàn bộ Detail chuyển display-only với MỌI actor, không có
   * cơ chế mở khoá lại. Chỉ là gate UI — không đổi quyền truy cập dữ liệu, không
   * cần BR mới; case vẫn do School Administration đóng chính thức (BR-155/UC-013).
   */
  reportExported?: boolean;
  /**
   * Design pattern vòng 2 (18/07/2026, Mục 5.3 QĐ 7): cặp nút "Lưu nháp"/"Chốt kết
   * luận" thống nhất cho Section B/C/D — khái quát hoá mô hình "Sơ bộ → Chính thức"
   * (Mục 11.9.1) sang toàn bộ. "Lưu nháp" đủ mở section kế tiếp (không khoá sequential
   * cứng); "Chốt kết luận" mới khoá section thành read-only.
   */
  /**
   * @deprecated Vòng 4 (Mục 10.3 QĐ 1, 19/07/2026 — vá P0-1): cờ này KHÔNG còn mở sớm
   * Section C nữa (Section C chỉ hiện khi state ≥ 3, tức Section B đã CHỐT qua validate
   * BR-147/149). "Lưu nháp" Section B nay chỉ persist form dở dang. Field giữ lại để
   * không phá dữ liệu cũ.
   */
  assessmentDraftSaved?: boolean;
  investigationEvidenceFinal?: boolean; // C2 Gathering Evidence đã Chốt kết luận
  investigationMeasuresDraft?: string; // C4 Remedial Measures — nháp (Chốt = Establish Measures/CAPA thật)
  officialClinicalNotes?: string; // D Block B — nháp Clinical notes (Chốt = Export & Submit)
  /**
   * Vòng 4 (Mục 10.3 QĐ 5, 19/07/2026): nhả quarantine (bỏ tick món) phải qua dialog
   * xác nhận + lý do bắt buộc — lý do ghi audit log (BR-170). Tick (khoá lô) vẫn tức
   * thì (QĐ 6 vòng 2 giữ nguyên) — bất đối xứng có chủ đích.
   */
  quarantineReleaseLog?: { dish: string; batches: string[]; reason: string; at: string }[];
}

// Bản đồ món → lô nguyên liệu (BR-151) được DẪN XUẤT từ phiếu xuất kho + sổ chế
// biến — xem `dishBatchMap()` cuối file. Hằng `DISH_BATCHES` bọc sẵn kết quả đó
// cũng đã bỏ (21/07/2026) vì không nơi nào dùng: mọi màn gọi thẳng `dishBatchMap()`
// để lấy dữ liệu tại thời điểm xem, không dính giá trị chốt lúc nạp module.

// ===== Học sinh (MF03/MF04) =====

export interface Student {
  id: string;
  name: string;
  class: string;
  allergies: string[];
  /**
   * @deprecated 27/07/2026 — `review_medicalstaff_healthrecord.md` Mục 4.6 điểm 4:
   * "chế độ ăn đặc biệt" chỉ có MỘT nguồn là `SpecialMealRequest` (có luồng duyệt
   * UC-078/BR-066). Toàn bộ màn Medical Staff đã bỏ đọc/ghi field này (dùng
   * `latestApprovedMeal()` trong `lib/health-docs.ts`). Field còn lại để các actor
   * khác (Parent/School Admin/Service Caterers, các helper thực đơn) đọc tạm cho tới
   * lượt sweep riêng của họ — KHÔNG dùng cho code mới.
   */
  specialDiet?: string;
  healthStatus: keyof typeof STATUS_BADGE;
  parent: string;
  /** UC-051: clinical file — Medical conditions / Medications / Emergency contacts */
  medicalConditions?: string;
  medications?: string;
  emergencyContact?: string;
  /** BR-031: tài liệu y tế đính kèm (bắt buộc khi dị ứng mức Severe) */
  medicalDocuments?: string[];
  allergySeverity?: "Mild" | "Moderate" | "Severe";
}

/**
 * `STUDENTS` (seed) XOÁ 07/08/2026 — cùng đợt với `ALLERGY_DECLARATIONS` và vòng backfill
 * `Student.allergies` đọc cả hai (đã dead, xem comment gần `AllergyDeclaration` phía dưới).
 * Dữ liệu thật qua `usePagedList<StudentDto>("students")` + `studentDtoToMock()`.
 */

// ===== Nhà cung cấp / Đối tác (MF05, BR-040..BR-060) =====

/*
 * `Partner` + `PartnerDocument` (interface khai tay) XOÁ 07/08/2026 — migrate sang 2 DTO THẬT,
 * đúng chỗ nào dùng chỗ nấy:
 *   `GET /partners`      → `PartnerDto`       (= `Api<"PartnerResponse">`)       — 9 điểm gọi
 *   `GET /partners/{id}` → `PartnerDetailDto` (= `Api<"PartnerDetailResponse">`) — 2 điểm gọi
 *                          (`partnerships/[id]`, `supplier/dashboard` qua `/partners/me`)
 * Mock gộp CẢ HAI làm một nên `taxId`/`documents`/`email`/`phone`/`description` (chỉ có ở
 * DETAIL) trông như đọc được từ danh sách — `_partnersContract` cũ tự ghi rõ nó KHÔNG bắt được
 * lỗi đó vì so với bản đầy đủ hơn. Tách type là cách vá đúng lỗ hổng ấy.
 * `PartnerDocument` ↔ `PartnerDocumentSummary` (lồng trong DETAIL).
 */

/**
 * `PARTNERS` (seed) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng thật): mọi tham số
 * `partners: ... = PARTNERS` chỉ còn là default KHÔNG BAO GIỜ trúng (mọi nơi gọi thật đã
 * truyền `partners` thật từ `usePagedList<Partner>("partners")` — kiểm phiên 5, xem Handoff),
 * và lần đọc trực tiếp cuối cùng (`PARTNERS.find(...)` trong `step1RowsOfIssue()`, tra tên
 * NCC cho Mẫu số 1 kiểm thực 3 bước) là BUG thật — đã vá bằng tham số `partners` mới phiên 5.
 */

// ===== Đơn mua hàng & giao hàng (Supplier) =====

/**
 * `PurchaseItem`/`PurchaseRequest` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi
 * dùng): `purchase-requests` đã nối thật từ trước (`PurchaseRequestDto`/
 * `PurchaseRequestItemDto` = `Api<"PurchaseRequestResponse">`/`Api<"ItemResponse">`,
 * `lib/api/types.ts`), mọi trang `supplier/purchase-requests`,
 * `service-caterers/orders` đọc thẳng DTO thật.
 */

export interface Delivery {
  id: string;
  school: string;
  scheduledAt: string;
  driver: string;
  vehiclePlate: string;
  batches: number;
  status: keyof typeof STATUS_BADGE;
  /** Đơn mua hàng nguồn — UC-083 (Submit Delivery Information). */
  purchaseRequestId?: string;
  /** Mã lô nguyên liệu gắn kèm — BR-071 (Linked Batches). */
  linkedBatches?: string[];
  /** Lô gán theo từng nguyên liệu (NCC chọn khi xác nhận đơn): tên nguyên liệu → batchId. */
  batchAssignments?: Record<string, string>;
  /** Thời điểm cập nhật trạng thái gần nhất — UC-092 (audit trail). */
  statusUpdatedAt?: string;
}

// ===== Raw Food Supplier (MF06) — lô xuất đi, nhân viên giao, tài liệu tuân thủ, hợp tác trường =====

/**
 * Lô nguyên liệu do NHÀ CUNG CẤP đăng ký trước khi giao (Screen Spec 12–17, UC-043/081/100/107).
 * Khác với InventoryBatch (kho của Service Caterer): lô supplier khai theo BR-071 —
 * số lô, NSX, ngày đóng gói, HSD, số lượng và chứng chỉ liên kết.
 */
/*
 * `CURRENT_SUPPLIER_ID` — ĐÃ BỎ 06/08/2026. Tất cả màn Supplier đã đổi sang
 * `useAuthStore((s) => s.user?.partnerId)` (phiên thật) — không còn nơi nào đọc hằng số này.
 */

export interface SupplierBatch {
  id: string;
  /**
   * Lô thuộc về SẢN PHẨM nào của NCC. Trước 22/07/2026 chỉ có `productName` là
   * chuỗi tự do: hai lô cùng mặt hàng chỉ nhận ra nhau bằng trùng tên, và khâu
   * nhận hàng của trường phải dò tên để đoán nguyên liệu trong danh mục.
   */
  productId: string;
  /** Nhãn hiển thị — nối từ sản phẩm ở vòng backfill, không khai tay. */
  productName?: string;
  lotNumber: string;
  manufacturingDate: string;
  packagingDate: string;
  expirationDate: string;
  quantity: string;
  /** Mã tài liệu trong SUPPLIER_DOCUMENTS liên kết với lô — BR-071/BR-046. */
  certificates: string[];
  status: keyof typeof STATUS_BADGE;
}

/**
 * `SupplierDeliveryPerson` (khai tay) XOÁ 07/08/2026 — `supplier/delivery-staff/page.tsx`
 * đọc thẳng `DeliveryPersonDto` (= `Api<"DeliveryPersonResponse">`). `biometricStatus` giữ
 * union cục bộ TẠI FILE ĐÓ (không phải interface) vì DTO thật khai field này là `string`
 * trần — springdoc không sinh literal union dù có 3 giá trị cố định thật.
 */

/** Tài liệu tuân thủ của supplier — ComplianceDocument (class diagram), BR-046/047/048, UC-101/102/091/167. */
/**
 * `SupplierDocument` (khai tay) XOÁ 07/08/2026 — dùng thẳng `DocumentDto` (=
 * `Api<"DocumentResponse">`, `lib/api/types.ts`). `documentType` literal union giữ
 * qua `UploadDocumentDto["documentType"]` (`Api<"UploadDocumentRequest">`, OpenAPI
 * sinh sẵn D6 enum) tại nơi dùng, không khai lại tay.
 */

/**
 * `SupplierPartnership` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE từ trước (0 nơi
 * dùng): `supplier/partnerships/page.tsx` đọc thẳng `PartnershipDto` (=
 * `Api<"PartnershipResponse">`) vì `since` (mock) khác tên `startDate` (DTO thật).
 *
 * `PartnershipApplication` (khai tay) CŨNG XOÁ 07/08/2026 — cùng trang đọc thẳng
 * `ApplicationDto` (= `Api<"ApplicationResponse">`), field khớp 1:1.
 */


// ===== Tài khoản người dùng (System Admin) =====

/**
 * `UserAccount` (khai tay) XOÁ 07/08/2026 — dùng thẳng `UserDto` (=
 * `Api<"UserResponse">`, `lib/api/types.ts`) cho `GET|POST /users`,
 * `PATCH /users/{code}/status`. `role`/`status` sinh sẵn literal union
 * (kebab-case D6) từ enum OpenAPI, khớp `RoleKey` — không mất type-safety.
 */

/**
 * `SchoolRecord` (khai tay) XOÁ 07/08/2026 — dùng thẳng `SchoolDto` (=
 * `Api<"SchoolResponse">`, `lib/api/types.ts`), field khớp 1:1.
 */


/**
 * `AuditLog` (khai tay) XOÁ 07/08/2026 — `system-admin/audit-logs/page.tsx` đọc thẳng
 * `AuditLogDto` (= `Api<"AuditLogResponse">`), field khớp 1:1.
 */

// ===== Phân phát bữa ăn (Teacher, BR-120..BR-135) =====

/**
 * Tổng hợp phát suất theo NGÀY × LỚP.
 *
 * Trước 21/07/2026 đây là mảng seed `DISTRIBUTIONS` khai tay, song song với
 * `student-distributions` mà giáo viên thực sự ghi khi phát suất: 3 trong 4 dòng
 * của seed là ngày không có một bản ghi phát suất nào bên dưới, và vì hai màn
 * tiêu thụ đọc seed nên bếp lẫn dashboard không bao giờ thấy việc GV vừa làm.
 * Nay là số liệu DẪN XUẤT — xem `distributionSummaries`.
 */
export interface DistributionRecord {
  id: string;
  date: string;
  class: string;
  planned: number;
  distributed: number;
  specialMeals: number;
  status: keyof typeof STATUS_BADGE;
}


/**
 * `CorrectiveAction` (khai tay) + `incidentStatusFromCapa()`/`CAPA_ACTIVE_STATUSES`
 * XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng ngoài chính chúng): `corrective-actions`
 * đã nối thật từ trước, mọi trang (`medical-staff/school-admin/service-caterers
 * /corrective-actions`) đọc thẳng `CorrectiveActionDto` (= `Api<"CorrectiveActionResponse">`).
 */

/**
 * Sự kiện của một sự cố — sổ CHỈ THÊM (append-only).
 *
 * Trước 22/07/2026 dòng thời gian điều tra là mảng hardcode diễn biến của riêng
 * INC-2026-014, hiển thị y hệt cho mọi sự cố. Sau đó chuyển sang dẫn xuất từ các
 * cột trạng thái của `Incident`, nhưng cách đó **mất mốc thời gian**: mô hình chỉ
 * lưu trạng thái cuối, không lưu lúc nào chuyển. Nên phải có bảng sự kiện riêng.
 *
 * Ghi TẠI THỜI ĐIỂM hành động xảy ra, không dựng lại từ trạng thái hiện tại.
 */
export interface IncidentEvent {
  id: string;
  incidentId: string;
  at: string;
  type:
  | "REPORTED"
  | "SEVERITY_SET"
  | "QUARANTINE"
  | "RELEASE"
  | "SAMPLE_SENT"
  | "TRACE_READY"
  | "CAPA_SUBMITTED"
  | "CAPA_APPROVED"
  | "CAPA_EVIDENCE"
  | "CAPA_VALIDATED"
  | "REPORT_SENT"
  | "CLOSED";
  description: string;
  actor: string;
}

// ===== Thông báo trường học (BR-138/BR-144) =====

/**
 * `Announcement` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng làm kiểu
 * resource, chỉ còn tên "Announcement" trong docstring UC): shape lệch mock đủ sâu
 * (list item KHÔNG có `content`, `audienceCodes` mới, `revisionCount` thay
 * `revisions[]`) nên mọi trang đã đổi hẳn sang đọc `AnnouncementDto`/
 * `AnnouncementListItemDto` (`lib/api/types.ts`) từ trước — xem `contract-check.ts`
 * mục MF10.
 */



// ===== Checklist kiểm tra bếp ĐỘNG (UC-186, BR-210 — mới 06/07/2026) =====
// Medical Staff tự CRUD nhóm/hạng mục/chú thích căn cứ pháp lý, không qua System Admin
// (BR-210 thay điều khoản "System Admin cấu hình" trong BR-207 gốc). Dữ liệu dưới đây là
// SEED KHỞI TẠO MẶC ĐỊNH — 6 nhóm theo MedicalStaff_KitchenInspectionChecklist_Proposal.md
// (bản 06/07/2026); nội dung chú thích là bản nháp tham khảo, mục chưa có căn cứ trong
// 00_Policies/ được giữ nguyên đánh dấu [cần xác nhận thêm] (không suy diễn nguồn pháp lý).

/**
 * `ChecklistItem`/`ChecklistGroup` (khai tay) XOÁ 07/08/2026 —
 * `medical-staff/conduct-inspection/checklist/page.tsx` đọc thẳng
 * `ChecklistItemDto`/`ChecklistGroupDto` (`lib/api/types.ts`), field khớp 1:1.
 */

/**
 * ===== Báo cáo ATTP đã gửi (UC-187–190) — NỐI THẬT 05/08/2026 =====
 *
 * `SentReport`/`SENT_REPORTS` và các type con (`ComplianceSentReport`/`ViolationSentReport`/
 * `IncidentSummarySentReport`) đã XOÁ — resource `sent-reports` nối thật hoàn toàn (xem
 * `07_implement/06_Sent_Reports_Redesign_Design.md`), thay bằng `SentReportDto`
 * (`lib/api/types.ts`, alias `Api<"SentReportResponse">`) + `ReportDocumentData`
 * (`components/app/report-document.tsx`, đúng shape `payload` JSONB server thật sự trả về).
 */

/*
 * ===== Nhật ký kiểm soát côn trùng — ĐÃ BỎ HẲN 27/07/2026 =====
 *
 * `PestControlLogEntry` + `PEST_CONTROL_LOG` xoá theo Vòng 9 Mục 17
 * (`review_medicalstaff_compliance_kitcheninspection.md`), cùng với việc bỏ đăng ký
 * resource `pest-control-log` trong `db.ts`.
 *
 * Đường đi của quyết định này (ghi lại để không dựng lại lần nữa):
 *  · Mục 1 (09/07): đổi BR-102 từ "chứng nhận có hiệu lực" sang mô hình "nhật ký sự kiện".
 *  · Mục 11.3 (26/07): giữ hiển thị nhưng xoá seed giả, để trạng thái rỗng trung thực —
 *    vì grep toàn dự án cho `useCreate` vào resource này ra 0 kết quả ở MỌI actor.
 *  · Mục 17 (27/07): bỏ hẳn. Duy trì một chỗ hiển thị cho loại dữ liệu không có nguồn
 *    nhập thật là gánh nặng không đổi lấy gì; và bản thân mô hình "nhật ký côn trùng"
 *    độc lập chưa từng có căn cứ luật nào trong dự án.
 *
 * Nơi côn trùng thực sự được ghi nhận, có căn cứ pháp lý rõ ràng, và đã tồn tại sẵn từ
 * trước: **2 mục của checklist UC-014** trong `CHECKLIST_GROUPS_SEED` —
 *  · `CLI-3-8` "Không có dấu vết của côn trùng, động vật gây hại" (Luật ATTP Điều 19.1.c,
 *    20.1.b);
 *  · `CLI-4-6` "Khu chế biến có phương tiện phòng chống côn trùng và động vật gây hại"
 *    (Luật ATTP Điều 28.5; TTLT 13/2016 Phụ lục 04 Mục IV.4.1).
 * Phát hiện côn trùng = đánh "Không đạt" ở 2 mục đó kèm lý do + ảnh qua `itemFindings`
 * (cơ chế BR-207 đã có). Không phát hiện gì thì 2 mục "Đạt" và không có gì để hiển thị ở
 * chỗ nào khác — không dòng riêng, không card, không tab, không trạng thái rỗng.
 */

// ===== Kiểm tra vệ sinh bếp Định kỳ / Đột xuất (UC-014, BR-100..BR-104, BR-207/BR-208) =====

/** BR-207: bằng chứng theo từng hạng mục Không đạt (ảnh + lý do) */
export interface InspectionItemFinding {
  item: string;
  photos: number;
  reason: string;
}

export interface KitchenInspection {
  id: string;
  date: string;
  /**
   * @deprecated BR-208 deprecated 09/07/2026 (review_medicalstaff_compliance_kitcheninspection.md
   * Mục 3) — không còn hiển thị/nhập ở bất kỳ đâu; giữ field optional để không vỡ dữ liệu cũ (#16).
   */
  type?: "Định kỳ tháng" | "Đột xuất";
  /**
   * % — <60% → result "Failed" + tự động cảnh báo School Administration (MSG-093).
   * Descope 05/07/2026: KHÔNG khóa vận hành bếp, KHÔNG chặn phân phát bữa ăn
   * (bỏ cơ chế Quarantine/MSG-024 — sai module, thuộc Incident Management/BR-149).
   * Xem business_rules.md §12, ghi chú sau BR-208.
   */
  score: number;
  inspector: string;
  /**
   * @deprecated Vòng 9 (27/07/2026, Mục 17.2) — cờ tóm tắt "chứng nhận côn trùng còn hiệu
   * lực" không còn là một chỉ số độc lập ở phía Medical Staff: côn trùng nay chỉ phản ánh
   * qua 2 mục `CLI-3-8`/`CLI-4-6` của chính phiên kiểm tra.
   *
   * KHÔNG xoá field vì `school-admin/compliance/page.tsx` và
   * `school-admin/reports/[slug]/page.tsx` vẫn đang đọc trực tiếp — xoá sẽ làm gãy màn của
   * actor khác, nằm ngoài phạm vi đợt Medical Staff. Mọi điểm hiển thị phía Medical Staff
   * đã gỡ; POST `/kitchen-inspections` vẫn nhận giá trị để dữ liệu cũ và màn School Admin
   * không đứt mạch.
   */
  pestControlValid: boolean;
  staffHealthValid: boolean;
  result: keyof typeof STATUS_BADGE;
  photosCount?: number;
  failedItems?: string[];
  /** BR-207: chi tiết bằng chứng per-item cho các hạng mục Không đạt */
  itemFindings?: InspectionItemFinding[];
  /**
   * `review_medicalstaff_compliance_kitcheninspection.md` Mục 8.4 (chốt 26/07/2026):
   * phiên KHÔNG đóng ngay khi Medical Staff bấm "Hoàn thành" — chuyển
   * `AWAITING_CATERERS_CONFIRM`, chờ Service Caterers xác nhận kết quả → `CONFIRMED`,
   * lúc đó mới mở khoá "Tạo biên bản sự việc". Màn xác nhận phía Service Caterers
   * ghi nhận ở `review_medicalstaff_to_servicecaterers.md`, CHƯA code (ngoài phạm vi).
   */
  confirmStatus?: "AWAITING_CATERERS_CONFIRM" | "CONFIRMED";
  confirmedBy?: string;
  confirmedAt?: string;
  /** Snapshot đánh giá từng hạng mục (phục vụ màn xem lại chỉ đọc — Mục 8.4 điểm 1) */
  itemResults?: { item: string; passed: boolean }[];
  /**
   * Biên bản kiểm tra bếp — tái dùng đúng cơ chế BR-211 của Sổ vi phạm: xuất PDF → in →
   * ký tay → tải ảnh/scan bản đã ký. KHÁC Sổ vi phạm đúng 1 điểm: chỉ 2 bên ký (Y tế
   * trường học + đại diện Service Caterers), BỎ HẲN "Người làm chứng".
   *
   * ═══ ĐẢO NGƯỢC 27/07/2026 (Vòng 11 Mục 22) ═══
   * Vòng 3 Mục 9 (26/07) từng đổi sang 2 tick điện tử, với lý do DUY NHẤT là "đồng bộ theo
   * BR-211 vì BR-211 tự thân đã đổi". Nay quyết định đó bên Violations đã bị đảo ngược
   * (`review_medicalstaff_violations.md` Mục 12 — 2 nút không thể cùng một màn vì khác
   * role/layout, và đại diện Nhà thầu chỉ được CHỌN TÊN chứ không tự xác thực), nên lý do
   * đổi không còn. Quay lại cơ chế cũ để không tồn tại hai quy ước ký khác nhau trong cùng
   * một hệ thống.
   */
  recordCaterersRep?: string;
  /**
   * @deprecated Vòng 11 (27/07/2026) — cơ chế 2 tick điện tử đã bỏ. KHÔNG xoá 3 field:
   * biên bản lập trong khoảng cơ chế đó còn hiệu lực (26-27/07/2026) vẫn giữ giá trị lịch
   * sử — cùng nguyên tắc đã áp bên Violations.
   */
  recordConfirmedByMedicalStaffAt?: string;
  /** @deprecated cùng lý do — xem `recordConfirmedByMedicalStaffAt` */
  recordConfirmedByMedicalStaffBy?: string;
  /** @deprecated cùng lý do — xem `recordConfirmedByMedicalStaffAt` */
  recordConfirmedByCaterersAt?: string;
  /** Xuất bản in — **bắt buộc** trước khi tải bản ký (BR-211) */
  recordPdfExportedAt?: string;
  recordPdfVersion?: number;
  /** Gate khoá biên bản: ảnh/scan bản đã ký 2 bên (BR-211) */
  recordSignedFiles?: UploadedFile[];
}


// ===== Lớp học & Năm học (MF02, BR-020..BR-028, BR-191..BR-195) =====

/*
 * `Classroom` (interface khai tay) XOÁ 07/08/2026 — migrate sang `ClassroomDto`
 * (= `Api<"ClassroomResponse">`, `lib/api/types.ts`) ở cả 16 nơi dùng. DTO có ĐỦ mọi field
 * mock khai TRỪ `lunchTime` (backend chưa từng có; từ 06/08 `useTeacherClass()` đọc API thật
 * nên field đó luôn `undefined` ⇒ 2 màn Teacher hiện "Bữa trưa dự kiến —" vĩnh viễn, phiên 6
 * gỡ hẳn đoạn UI chết đó). Xem Javadoc `ClassroomDto` cho 2 cạm bẫy còn lại (`grade` là NHÃN
 * chứ không phải số/UUID).
 */

/**
 * MF02 — khớp `AcademicYearDtos.AcademicYearResponse` của backend thật.
 *
 * ⚠️ Đổi tên 02/08/2026: `start`/`end` → `startDate`/`endDate`, và giá trị nay là
 * ISO `yyyy-MM-dd` (D4) chứ không phải `dd/MM/yyyy`. Backend là nguồn sự thật
 * (§0-BIS) — không sửa DTO backend để chiều FE cũ. Hiển thị đi qua
 * `formatVnDate()` ở tầng render.
 */
/**
 * `AcademicYear` (khai tay) XOÁ 07/08/2026 — dùng thẳng `AcademicYearDto` (=
 * `Api<"AcademicYearResponse">`, `lib/api/types.ts`), field khớp 1:1 (`status` DTO
 * thật còn thêm `ARCHIVED`, superset — không lệch nguy hiểm).
 */

// ===== Vi phạm & Xử lý sai phạm (§18, BR-183..BR-190) =====

export interface ViolationRecord {
  id: string;
  type: "Quy trình" | "Nhân sự";
  subject: string;
  recordedBy: string;
  date: string;
  severity: keyof typeof STATUS_BADGE; // LOW | MEDIUM | HIGH
  resolution: keyof typeof STATUS_BADGE; // PENDING_APPROVAL | APPROVED | REJECTED
  status: keyof typeof STATUS_BADGE; // OPEN | CLOSED
  /**
   * @deprecated Vòng 4/5 (26-27/07/2026, Mục 9.1 + Mục 10) — Medical Staff **không lập biên
   * bản nhắm cá nhân** dưới bất kỳ hình thức nào: chủ thể duy nhất là ĐƠN VỊ Nhà thầu, đại
   * diện là `caterersRepresentative`. Kỷ luật nhân viên cụ thể là việc nội bộ của Nhà thầu,
   * ngoài phạm vi SFMS. BR-184 đã Deprecated (gộp vào BR-183).
   *
   * KHÔNG xoá field: 2 bản ghi seed cũ và mọi biên bản đã lập trước 27/07/2026 vẫn còn giá
   * trị; xoá đi là mất dữ liệu lịch sử. Nhưng **không còn form nào nhập, không màn nào hiển
   * thị, và `type` không còn suy ra từ nó**.
   */
  staffName?: string;
  /** @deprecated cùng lý do với `staffName` */
  staffPosition?: string;
  violationType?: string; // HYGIENE_VIOLATION | PROCEDURE_SKIP | OVERRIDE_ABUSE | UNAUTHORIZED_ACTION | OTHER
  description?: string;
  occurredAt?: string;
  witness?: string; // bắt buộc khi ≥ MEDIUM với vi phạm quy trình (BR-183)
  evidencePhotos?: number;
  /** UC-180 — ViolationResolutionRecord */
  actionTaken?: string;
  disciplinaryLevel?: string; // VERBAL_WARNING | WRITTEN_WARNING | SUSPENSION | TERMINATION_RECOMMENDATION (BR-185)
  handledBy?: string;
  /**
   * BR-189: vi phạm do ĐƠN VỊ SUẤT ĂN tự ghi nhận (khác với do Y tế/BGH lập).
   * Trước 21/07/2026 nhóm này nằm ở bảng riêng `CATERER_VIOLATIONS` — cùng khái
   * niệm, cùng trường, và 3/4 bản ghi trùng đúng sự việc đã có trong `VIOLATIONS`
   * ⇒ báo cáo tổng hợp BR-214 đếm trùng và gửi kèm mã vi phạm không có trong bảng.
   */
  selfReported?: boolean;
  /** BR-187: nghi ngờ tái diễn trong 90 ngày */
  repeatSuspect?: boolean;
  /** BR-211 (06/07/2026) — biên bản chuẩn theo mẫu 00_Policies/specialreport2173_1.md */
  locationOfIncident?: string; // "Hôm nay, tại: ___" — bắt buộc khi lập mới (UC-178/179)
  caterersRepresentative?: string; // đại diện Nhà thầu ký biên bản — tài khoản Service Caterers, không nhập tay
  /**
   * @deprecated ĐẢO NGƯỢC 27/07/2026 (Vòng 6 Mục 12) — cơ chế 2 tick điện tử thêm ở Mục 9.2
   * (26/07) đã bị bỏ: hai nút không thể cùng nằm một màn vì thuộc hai role/layout khác nhau,
   * và `caterersRepresentative` vốn chỉ là "Y tế chọn tên từ dropdown" nên xác nhận của bên
   * thứ hai chưa từng là hành động độc lập. Quay lại PDF + ký tay + ảnh (BR-211 nguyên bản).
   *
   * KHÔNG xoá 3 field: biên bản lập trong khoảng cơ chế 2-tick còn hiệu lực (26-27/07/2026)
   * vẫn giữ giá trị lịch sử — cùng nguyên tắc đã áp cho `staffName`.
   */
  confirmedByMedicalStaffAt?: string;
  /** @deprecated cùng lý do — xem `confirmedByMedicalStaffAt` */
  confirmedByMedicalStaffBy?: string;
  /** @deprecated cùng lý do — xem `confirmedByMedicalStaffAt` */
  confirmedByCaterersAt?: string;
  /** BR-211 — đã bấm "Xuất & in biên bản (PDF)"; **bắt buộc** trước khi tải bản ký */
  pdfExported?: boolean;
  /** Gate chặn cứng (MSG-095): chưa có ảnh biên bản đã ký → không được đóng/gửi duyệt */
  signedRecordPhoto?: boolean;
  /** Mục 11.5.1 (đợt 2): liên kết chéo tới FoodSafetyIncident (INC-xxx) khi vi phạm phát sinh
   * từ điều tra sự cố — optional, pre-fill từ Investigation Workspace. BR-186 độc lập với BR-153. */
  relatedIncidentId?: string;
  /**
   * ===== review_medicalstaff_violations.md (19/07/2026) — Master–Detail redesign =====
   * Mục 2.3/QĐ 3: "Ảnh bằng chứng" và "ảnh biên bản ký" chuyển từ counter/boolean giả sang
   * FILE THẬT (mock: tên file + thời điểm tải + kích thước) — có preview/xoá trước khi lưu.
   * `evidencePhotos`/`signedRecordPhoto` giữ lại để tương thích dữ liệu cũ (dẫn xuất).
   */
  evidenceFiles?: UploadedFile[];
  signedRecordFiles?: UploadedFile[]; // ảnh/scan biên bản đã ký 3 bên — nhiều file/PDF nhiều trang
  /** Mục 2.2/2.3: PDF là TÀI LIỆU SINH RA (version + thời điểm), không phải field nhập */
  pdfGeneratedAt?: string;
  pdfVersion?: number;
  /**
   * Mục 2.4/QĐ 9 — traceability: VIO nguồn gốc quy trình (BR-183) tham chiếu ngược bản ghi
   * checklist/kiểm thực 3 bước đã "nâng cấp" thành biên bản này. Không đổi nghiệp vụ.
   */
  sourceInspectionId?: string;
}

/** Mock file đã tải lên (review_medicalstaff_violations.md Mục 2.3 — thay counter giả) */
export interface UploadedFile {
  name: string;
  uploadedAt: string;
  size: string;
}

// ===== Phản hồi phụ huynh (BR-139..BR-145) =====

/**
 * `Feedback`/`hasIllnessKeyword()`/`ILLNESS_KEYWORDS` (khai tay) XOÁ 07/08/2026 — đã
 * là DEAD CODE (0 nơi dùng): `feedbacks` nối thật, `oneOf` theo vai (D11, BR-145) nên
 * mọi trang đọc thẳng `FeedbackOwnerListItemDto`/`FeedbackAnonymizedListItemDto`.
 * BR-140/BR-142 (từ khoá bệnh lý → thông báo Y tế) nay tính Ở SERVER
 * (`illnessKeywordDetector`), FE không tự dò từ khoá nữa — xem `lib/api/types.ts`
 * mục MF10.
 */

// ===== Danh mục Permission (UC-129 — cố định trong code, chỉ đọc) =====
//
// `Permission`/`RoleRecord` (khai tay) XOÁ 07/08/2026 — dùng thẳng `PermissionDto`/
// `RoleRecordDto` (= `Api<"PermissionResponse">`/`Api<"RoleResponse">`, `lib/api/types.ts`),
// field khớp 1:1 (đã contract-check trước đó).

// ===== Nhân viên trường (UC-144/024/002 — School Administration) =====

/**
 * MF01 — `GET /api/v1/staff` trả `UserDtos.UserResponse` (cùng DTO với `/users`),
 * KHÔNG phải một shape "staff" riêng. Đồng bộ với backend thật 02/08/2026:
 *
 *  - `role` là RoleKey kebab-case (D6): `teacher` | `medical-staff` |
 *    `service-caterers`. Nhãn hiển thị tra `ROLE_LABEL` ở `lib/labels.ts` —
 *    nhãn KHÔNG bao giờ là giá trị lưu trữ, và backend từ chối `"Teacher"` bằng 422.
 *  - `emailVerified` và `invitedAt` KHÔNG tồn tại trong DTO. Trạng thái xác minh
 *    email (BR-010) nay nằm gọn trong `status`: `PENDING_VERIFICATION` = đã mời,
 *    chưa xác minh. Không có mốc thời gian mời nào được expose.
 *  - `phone` có thể null (chưa khai báo).
 */
/**
 * `StaffMember` (khai tay) XOÁ 07/08/2026 — dùng thẳng `StaffDto` (=
 * `Api<"UserResponse">`, CÙNG DTO với `/users`, `lib/api/types.ts`). `role` mock hẹp
 * hơn (3/7 giá trị) chỉ để tài liệu hoá — DTO thật (7 giá trị RoleKey) là superset,
 * không lệch nguy hiểm (server đã chặn tạo staff role ngoài 3 giá trị, BR-013).
 */

// ===== Nhật ký lỗi hệ thống (UC-111 — System Admin) =====

/**
 * `ErrorLog` (khai tay) XOÁ 07/08/2026 — `system-admin/{error-logs,dashboard}/page.tsx` đọc
 * thẳng `ErrorLogDto` (= `Api<"ErrorLogSummary">`), field khớp 1:1.
 */

/*
 * ===== Báo cáo lạm dụng manual override hàng tuần (UC-183, BR-190) — ĐÃ BỎ 06/08/2026 =====
 *
 * `OverrideAbuseRow`/`OVERRIDE_ABUSE_REPORT` (mock, theo NHÂN VIÊN) đã xoá — nối thật qua
 * `GET /violations/override-abuse-report` (`ViolationController`, ADR-M11-6, dẫn xuất runtime
 * từ `ViolationRecord.violationType=OVERRIDE_ABUSE`). Model vi phạm thật đã chuyển sang cấp
 * ĐƠN VỊ nhà thầu — xem `lib/api/types.ts` (`OverrideAbuseRowDto`) và
 * `school-admin/violations/override-report/page.tsx`.
 */

// ===== Yêu cầu bữa ăn đặc biệt (UC-036/078, BR-066) =====
/** Danh mục chế độ ăn đặc biệt chuẩn (BR-066) */
export const STANDARD_DIETS = ["Halal", "Vegetarian", "Gluten-Free", "Nut-Free", "Lactose-Free"];

/*
 * `SpecialMealRequest` (interface khai tay) XOÁ 07/08/2026 — migrate sang
 * `SpecialMealRequestDto` (= `Api<"SpecialMealRequestResponse">`, `lib/api/types.ts`) ở cả 11
 * nơi dùng. Đối chiếu field-by-field: DTO có ĐỦ 15 field mock khai, cộng thêm `requestedAt`/
 * `reviewedAt`; khác biệt DUY NHẤT là `status` — mock khai `keyof typeof STATUS_BADGE`, nhưng
 * `STATUS_BADGE` là `Record<string, …>` nên union đó THỰC CHẤT chỉ là `string`, không chặt hơn
 * DTO ⇒ không cần Draft type kiểu `MenuTemplate`, thay thẳng được.
 * Lưu ý khi đọc code cũ: `date` mock là dd/MM/yyyy, DTO thật là ISO `yyyy-MM-dd` (D4) — mọi
 * nơi render đã đi qua `formatVnDate()` từ trước.
 */

/**
 * `HealthHistoryEvent`/`HEALTH_HISTORY` (khai tay) XOÁ 07/08/2026 — DEAD CODE (0 nơi
 * dùng, sót lại từ Giai đoạn 1 — `health-history` đã nối thật 05/08/2026, mọi trang
 * đọc thẳng `HealthHistoryEventDto` = `Api<"HealthHistoryEventResponse">`,
 * `lib/api/types.ts`).
 */

// ===== Thông báo cá nhân (Notification Center — Report 3 §3.3.2.1.13, BR-158/159) =====

/**
 * `NotificationAudience`/`UserNotification` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng):
 * `notification-bell.tsx` đọc thẳng `NotificationDto` (= `Api<"NotificationResponse">`)
 * từ trước (taxonomy 7 giá trị D6, khác 4 giá trị mock cũ).
 */

// ===== Cấu hình hệ thống do System Admin quản (UC-184/UC-183) =====

/**
 * Chính sách bảo mật (BR-002/004/006).
 *
 * Trước 22/07/2026 màn "Chính sách bảo mật" giữ toàn bộ tham số trong `useState`
 * rồi `setTimeout` báo lưu thành công. Và cũng như cấu hình trường, nó không tới
 * được nơi dùng: màn đặt lại mật khẩu HARDCODE đúng những quy tắc mà màn này khai
 * là cấu hình được ("Tối thiểu 8 ký tự", "Có ký tự đặc biệt"), còn quy tắc khoá
 * tài khoản sau N lần sai thì chỉ nằm trong một dòng chú thích ở màn đăng nhập.
 * Quản trị viên đặt độ dài tối thiểu 12 mà màn đổi mật khẩu vẫn cho qua ở 8.
 */
/**
 * `SecurityPolicy` (khai tay) XOÁ 07/08/2026 — `system-admin/security-policies/page.tsx`
 * và `(auth)/reset-password/page.tsx` đọc thẳng `SecurityPolicyDto` (=
 * `Api<"SecurityPolicyResponse">`). DTO thật KHÔNG có `updatedAt`/`updatedBy` (dẫn
 * xuất từ `BaseEntity`, D12 không expose) — form không hiển thị 2 dòng đó, không
 * phải nợ đang chờ quyết định.
 */

/**
 * `ExternalIntegration` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng
 * ngoài `contract-check.ts`): `system-admin/{integrations,system-status}/page.tsx`
 * đọc thẳng `IntegrationDto` (= `Api<"IntegrationResponse">`) từ trước.
 */

// ===== Cơ quan tiếp nhận mẫu lưu (BR-113) =====

/**
 * Nơi mẫu lưu được bàn giao tới.
 *
 * KHÔNG chỉ là phòng kiểm nghiệm. Thực tế có hai loại tiếp nhận khác hẳn nhau:
 *   - XÉT NGHIỆM: trường chủ động gửi mẫu, nêu chỉ tiêu, và CHỜ KẾT QUẢ trả về để
 *     kết luận nguyên nhân.
 *   - THU GIỮ: công an hoặc thanh tra tới lấy mẫu làm VẬT CHỨNG trong vụ việc của
 *     họ. Không có chỉ tiêu do trường đặt, và kết quả có thể không bao giờ quay
 *     lại trường — trường chỉ giữ biên bản thu giữ.
 * Gộp hai loại vào một khái niệm "phòng kiểm nghiệm" sẽ bắt người dùng điền chỉ
 * tiêu xét nghiệm cho một cuộc thu giữ, và để hồ sơ treo mãi ở trạng thái "chờ kết
 * quả" cho thứ không bao giờ có kết quả.
 *
 * Danh sách trước 22/07/2026 gõ cứng trong JSX, và hai màn gõ hai bản KHÁC NHAU:
 * "Viện Kiểm nghiệm ATVSTP Quốc gia" so với "Viện Kiểm nghiệm ATTP Quốc gia (NIFC)"
 * — cùng một cơ quan, hai chuỗi, nên thống kê theo nơi nhận không gộp được.
 */
/**
 * `ReceivingAuthority` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng ngoài
 * `contract-check.ts`): `medical-staff/samples/page.tsx` đọc thẳng `ReceivingAuthorityDto`
 * (= `Api<"ReceivingAuthorityResponse">`) từ trước.
 */

// ===== Ký xác nhận an toàn thực phẩm hằng ngày (UC-027 bước 5, BR-091/100/104) =====

/**
 * Chữ ký xác nhận trạng thái an toàn thực phẩm trong ngày của Ban giám hiệu.
 *
 * Trước 22/07/2026 nút "Ký xác nhận" chỉ đặt `useState(true)`: tải lại trang là
 * mất, không vai nào khác thấy, và khi thanh tra hỏi "hôm đó ai xác nhận" thì
 * không có gì để đưa ra. Đây là bản ghi tuân thủ, không phải trạng thái giao diện.
 */
/**
 * `SafetySignoff` (khai tay) XOÁ 07/08/2026 — `school-admin/compliance/page.tsx` đọc thẳng
 * `SafetySignoffDto` (= `Api<"SafetySignoffResponse">`), field khớp 1:1.
 */


/*
 * ===== Chính sách trường học (UC-137, BR-022) — ĐÃ BỎ 06/08/2026 =====
 *
 * `SchoolPolicy`/`SCHOOL_POLICIES` (thư viện văn bản chính sách) đã xoá cùng màn
 * `medical-staff/policies` — tính năng CHƯA từng có backend (khác `GET /schools/me/policy`
 * thật, trả tham số vận hành geofence/lat/lng, không phải văn bản), không màn nào khác phụ
 * thuộc. Quyết định của người dùng: bỏ hẳn thay vì viết module mới cho một màn không ai cần.
 */

/*
 * ===== Nhật ký nhiệt độ tủ lạnh — ĐÃ BỎ 26/07/2026 =====
 *
 * `FridgeTempLog` + `FRIDGE_TEMP_LOGS` đã xoá theo Vòng 5 Mục 11.2
 * (`review_medicalstaff_compliance_kitcheninspection.md`). Hai lý do:
 *
 * 1. Đây là mảng HARDCODE, không đăng ký qua `db.ts` — tức không phải resource API thật,
 *    không actor nào ghi vào được. Grep toàn dự án: không có `useCreate` nào trỏ tới nó.
 * 2. Nó hiển thị nhiều "Refrigerator ID" (tủ mát nguyên liệu, tủ đông…) như thể hệ thống
 *    giám sát mọi tủ trong bếp. BR-110 trích Điều 8.1 QĐ 1246/QĐ-BYT, mà điều đó chỉ nói
 *    về **tủ lưu mẫu** ở dải 2–8°C — nên "tủ đông -18.5°C: PASSED" trong seed cũ vừa
 *    ngoài phạm vi BR vừa mâu thuẫn với chính ngưỡng của BR đó. Cùng loại scope creep đã
 *    khiến BR-089 bị Deprecated.
 *
 * Nguồn thay thế: `FoodSample.fridgeTemp` — nhiệt độ THẬT do Service Caterers nhập ở form
 * lưu mẫu (`service-caterers/samples`, validate 2–8°C), mỗi bản ghi lưu mẫu là một phép
 * đo có người chịu trách nhiệm và có mốc thời gian. Xem `medical-staff/compliance/page.tsx`
 * và `medical-staff/compliance/history/page.tsx`.
 */

// ===== Nhật ký kiểm soát côn trùng (UC-117, BR-102) =====


// ===== Học sinh của phụ huynh đang đăng nhập (mock session Parent) =====
// Một phụ huynh có thể có nhiều con học cùng trường — chọn con đang xem qua Child Switcher ở Topbar.
export interface ParentChild {
  studentId: string;
  name: string;
  class: string;
  dob: string;
  age: number;
  school: string;
}

// ===== Hồ sơ sức khỏe học sinh (07/08/09 — UC-050/084/093, BR-031) =====

/**
 * `HealthRecord`/`HEALTH_RECORDS`/`healthRecordOfStudent()` (khai tay) XOÁ 07/08/2026
 * — đã là DEAD CODE (0 nơi dùng): `parent/health/page.tsx` đọc thẳng `HealthRecordDto`
 * (= `Api<"HealthRecordResponse">`, `GET /students/{code}/health-record`).
 */

// ===== Khai báo dị ứng (05/06 — BR-031) =====

export const ALLERGEN_CATEGORIES = [
  "Hải sản (tôm, cua, sò, ốc)",
  "Cá",
  "Đậu phộng & các loại hạt",
  "Sữa & chế phẩm từ sữa",
  "Trứng",
  "Đậu nành",
  "Lúa mì (gluten)",
  "Khác",
];

/**
 * `AllergyDeclaration`/`ALLERGY_DECLARATIONS` (khai tay) XOÁ 07/08/2026 — DEAD CODE
 * (0 nơi dùng thật: import duy nhất ở `menu-personalization.ts` không ai đọc — tham
 * số `declarations` dùng shape nội bộ `{studentId?; allergen?; status?}[]` rồi). Dữ liệu
 * thật qua `usePagedList<AllergyDeclarationDto>("allergy-declarations")`. Kiểm phiên 5.
 */

/**
 * `STUDENTS`/`ALLERGY_DECLARATIONS` (seed) + vòng backfill `Student.allergies` XOÁ
 * 07/08/2026 — đã là DEAD CODE (backfill chỉ ghi đè seed, không ai đọc STUDENTS/
 * ALLERGY_DECLARATIONS làm nguồn thật nữa — xem `studentDtoToMock()`/`useDietGroups()`-style
 * adapter, `StudentResponse.allergies` đã là field DẪN XUẤT ADR-M03-3 từ backend). Kiểm phiên 5.
 */

/**
 * Tác nhân đã được Y tế xác minh của 1 học sinh — căn cứ cảnh báo/chặn (BR-031).
 *
 * 07/08/2026: nhận thẳng `student.allergies` (DTO thật `StudentResponse` — field DẪN XUẤT,
 * CHỈ gồm allergen ĐÃ XÁC MINH, ADR-M03-3) thay vì tự tra hằng seed `ALLERGY_DECLARATIONS`
 * theo `studentId`. `allergiesOfStudent()` (đọc cùng hằng, không còn nơi nào gọi) đã bỏ cùng
 * đợt.
 */
export function verifiedAllergensOfStudent(allergies: string[] | undefined): string[] {
  return allergies ?? [];
}

// ===== Khai báo bệnh lý nền (Phương án A — UC-084/093, BR-030/031/172/173) =====
// Danh mục bệnh lý nền do phụ huynh khai báo; hiệu lực sau khi Y tế trường xác minh (BR-031).
// Dữ liệu tách khỏi hồ sơ y tế append-only student_health_records (BR-172).

export const CHRONIC_CONDITION_CATEGORIES = [
  "Hen phế quản / Hen suyễn",
  "Tiểu đường (Type 1)",
  "Động kinh / Co giật",
  "Tim mạch bẩm sinh",
  "Rối loạn đông máu",
  "Dị ứng thuốc mạn tính",
  "Bệnh thận mạn",
  "Khác",
];

/**
 * `ChronicConditionDeclaration` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi
 * dùng, không có seed array kèm theo): `chronic-conditions` đã nối thật, mọi trang
 * đọc thẳng `ChronicConditionDto` (= `Api<"ChronicConditionResponse">`).
 */


// ===== THƯ VIỆN MÓN — dish master (03_NutritionDetail — BR-069, BR-076) =====
//
// Trước 21/07/2026 bảng này chỉ phục vụ màn dinh dưỡng của Phụ huynh: `ingredients`
// là `string[]` (chỉ TÊN, không định lượng) nên không dùng lại được khi lập thực
// đơn — mỗi lần lập lại phải gõ tay thành phần + kcal, món nối với nhau bằng tên
// chuỗi tự do. Nay nâng thành THƯ VIỆN MÓN dùng chung:
//   - `ingredients` là BOM thật (định lượng/suất + `ingredientId` — khoá join BR-076)
//   - `allergens` KHÔNG lưu nữa mà DẪN XUẤT qua `dishLibraryAllergens()`, hết phụ
//     thuộc kỷ luật nhập liệu (thành phần có tôm mà quên khai "Tôm" là mất cảnh báo)
//   - `isActive` để ngừng dùng món mà không xoá lịch sử thực đơn đã tham chiếu

export interface DishIngredient {
  /** Khoá join catalog (BR-076); thiếu = nguyên liệu ngoài danh mục, không truy được lô. */
  ingredientId?: string;
  name: string;
  /** Định lượng cho MỘT suất. */
  quantity: string;
  unit: string;
}

export interface Dish {
  id: string;
  name: string;
  category: "MON_CHINH" | "MON_MAN" | "CANH" | "RAU" | "BUA_PHU" | "TRANG_MIENG";
  // `supplier`/`kitchen` gõ tay đã bỏ 23/07/2026: nguồn gốc suy từ nguyên liệu
  // (nay là `GET /supplier-products/sourcing`, server tự chọn NCC rẻ nhất còn hợp tác),
  // còn bếp luôn là đơn vị suất ăn đang vận hành.
  kcal: number;
  protein: number;
  fat: number;
  carb: number;
  ingredients: DishIngredient[];
  /** Tác nhân khai thêm bằng tay — cho thành phần ngoài catalog. */
  manualAllergens: string[];
  isActive: boolean;
  /**
   * Món thay thế MẶC ĐỊNH khi món này chạm dị ứng (G10 — review 25/07/2026).
   * Trước đây suất thay thế chỉ nằm trong TỪNG thực đơn (`DishForm.substitutes`)
   * nên "Gà rang gừng → Thịt rim" tuần nào cũng phải khai lại tay. Khai một lần
   * ở thư viện: lập thực đơn (tay hoặc từ Thực đơn mẫu) hệ thống tự điền sẵn,
   * chỉ ghi đè khi tuần đó khác (vd hết nguyên liệu món mặc định).
   * Backend: bảng `DishSubstituteDefault(dish_id, substitute_dish_id)`.
   */
  defaultSubstituteId?: string;
  /** NỐI THẬT 05/08/2026 — URL công khai ảnh minh hoạ món (upload qua PUT-multipart riêng). */
  imageUrl?: string;
  /** NỐI THẬT 05/08/2026 — mô tả món (nguyên liệu chính, hương vị...), không bắt buộc. */
  description?: string;
  /** NỐI THẬT 05/08/2026 — hướng dẫn chế biến, không bắt buộc. */
  cookingInstructions?: string;
}

/**
 * 6 giá trị THẬT của enum Java `DishCategory` (backend) — `DishMapper.toResponse()`
 * trả `d.getCategory().name()`, `DishServiceImpl` gọi `DishCategory.valueOf(...)`
 * trên create/update, nên đây PHẢI là tên enum thô, không phải nhãn tiếng Việt
 * (khác quy ước cũ, đã gây lỗi 400/500 khi backend enforce enum — sửa 06/08/2026).
 */
export const DISH_CATEGORIES: { value: Dish["category"]; label: string }[] = [
  { value: "MON_CHINH", label: "Món chính" },
  { value: "MON_MAN", label: "Món mặn" },
  { value: "CANH", label: "Canh" },
  { value: "RAU", label: "Rau" },
  { value: "BUA_PHU", label: "Bữa phụ" },
  { value: "TRANG_MIENG", label: "Tráng miệng" },
];

/** Nhãn tiếng Việt cho 1 giá trị `Dish["category"]` — trả nguyên giá trị nếu không khớp. */
export function categoryLabel(value: string): string {
  return DISH_CATEGORIES.find((c) => c.value === value)?.label ?? value;
}

/**
 * Sinh `Dish.id` từ tên: "Cá basa sốt cà chua" → "ca-basa-sot-ca-chua". Id đọc
 * được để link `/parent/nutrition/[dishId]` còn ý nghĩa, và trùng đúng quy ước
 * của 16 món seed. `taken` để né trùng khi hai món khác nhau ra cùng slug.
 */
export function dishIdOfName(name: string, taken: Iterable<string> = []): string {
  const base =
    name
      .normalize("NFD")
      // ̀-ͯ = dấu thanh tách ra sau NFD ("ế" → "e" + dấu sắc + dấu mũ)
      .replace(/[̀-ͯ]/g, "")
      .replace(/đ/g, "d")
      .replace(/Đ/g, "D")
      .toLowerCase()
      .replace(/[^a-z0-9]+/g, "-")
      .replace(/^-+|-+$/g, "") || "mon-moi";
  const used = new Set(taken);
  let id = base;
  for (let i = 2; used.has(id); i += 1) id = `${base}-${i}`;
  return id;
}

/** Allergen của 1 món trong thư viện = khai tay ∪ suy từ thành phần (BR-063/069). */
export function dishLibraryAllergens(
  dish: Pick<Dish, "ingredients" | "manualAllergens">
): string[] {
  const set = new Set<string>(dish.manualAllergens);
  for (const ing of dish.ingredients) {
    resolveIngredientAllergens(ing).allergens.forEach((a) => set.add(a));
  }
  return [...set];
}

// `DISH_LIBRARY` (mock, ~33 món) và helper `bom()` — đã xoá 10/08/2026, 0 nơi
// còn đọc (thư viện món thật qua `usePagedList<Dish>("dishes")`).

/* ============================================================================
 * THỰC ĐƠN MẪU — menu template (G9, review 25/07/2026 + gợi ý Minh)
 *
 * Một mẫu = MỘT BỘ MÓN CỦA MỘT BỮA, đặt tên gọi được ("Thực đơn tiêu chuẩn A"),
 * KHÔNG gắn ngày/thứ và KHÔNG gắn buổi — mẫu A xếp cho bữa Sáng hay Trưa là
 * quyết định lúc xếp lịch, mẫu chỉ giữ nội dung. Trên Lịch thực đơn tuần, ô đã
 * áp mẫu hiện TÊN MẪU thay vì liệt kê món; bấm vào mới sổ ra toàn bộ bữa ăn.
 *
 * `audience` khớp mô hình Menu.audience của Nam: "ALL" = thực đơn tiêu chuẩn,
 * DietGroup.id = mẫu cho nhóm chế độ ăn (vd "Thực đơn ăn chay A1" chỉ được mời
 * chọn khi đang xếp dòng nhóm chay của lịch).
 *
 * Món tham chiếu bằng `dishId` về DISH_LIBRARY — mẫu KHÔNG chép nội dung món:
 * sửa BOM/kcal ở thư viện là mọi mẫu dùng món đó cập nhật theo; suất thay thế
 * dị ứng cũng tự đổ từ `Dish.defaultSubstituteId` (G10) lúc áp mẫu.
 *
 * Backend: MenuTemplate(id, name, audience, is_active)
 *          + MenuTemplateDish(template_id, dish_id, sort_order).
 * ==========================================================================*/

/** Thư viện món PHẢI là dữ liệu THẬT (`usePagedList<Dish>("dishes")`, `GET /api/v1/dishes`). */
export function findDishByName(name: string, library: Pick<Dish, "id" | "name">[]): Pick<Dish, "id" | "name"> | undefined {
  return library.find((d) => d.name === name);
}

// ===== Suất ăn TRỌN BỘ theo nhóm — MenuVariant (Đợt C, 21/07/2026) =====
//
// Mô hình cũ giả định "suất đặc biệt = thay từng món lẻ" (`DishForm.substitutes[]`).
// Đúng với ăn chay (thay 2–3 món), SAI với tiểu đường: đổi cơm trắng sang gạo lứt,
// giảm khẩu phần tinh bột, bỏ món ngọt — gần như một thực đơn khác. Và vì suất chỉ
// là các mẩu rời rạc nên KHÔNG kiểm được tổng kcal/đạm của chính suất đó (BR-062).
//
//   PARTIAL — chỉ ghi phần khác biệt. Tương đương `substitutes[]` sẵn có, KHÔNG
//             chuyển đổi lại; giữ nguyên luồng của màn Bữa ăn đặc biệt.
//   FULL    — ghi trọn bộ món của buổi ăn cho nhóm đó (`replacesDishId` bỏ trống).
//
// Ở backend đây là 2 bảng (MenuVariant / MenuVariantDish); trong mock nhúng luôn
// `dishes[]` vào variant cho gọn — quan hệ vẫn là 1-n, không mất thông tin.

export interface MenuVariantDish {
  /** Khoá về THƯ VIỆN MÓN — dinh dưỡng của variant lấy từ đây, không nhập tay. */
  dishId: string;
  /**
   * Tên món để HIỂN THỊ khi `dishId` không tra được trong thư viện — thực đơn
   * riêng có thể chứa món gõ tay giữ lại từ bữa gốc, khoá của nó chỉ là slug sinh
   * từ tên. Trước 23/07/2026 projection vứt tên đi và mọi màn fallback về chính
   * `dishId`, nên lịch tuần bày ra "mon-moi" (slug của món chưa đặt tên).
   * KHÔNG dùng thay `dishId`: dinh dưỡng và truy xuất lô vẫn đi bằng khoá.
   */
  name?: string;
  /** Món gốc bị thay (chỉ dùng ở PARTIAL); FULL để trống vì thay trọn bộ. */
  replacesDishId?: string;
}

/** Tên hiển thị của 1 món trong suất riêng: thư viện → tên đã chiếu → khoá. */
export function variantDishName(
  vd: MenuVariantDish,
  library: Pick<Dish, "id" | "name">[] = []
): string {
  return library.find((d) => d.id === vd.dishId)?.name ?? vd.name ?? vd.dishId;
}

export interface MenuVariant {
  id: string;
  menuId: string;
  mealType: MealType;
  /**
   * `DietGroup.id`. Trước 22/07/2026 trường này tên `groupId` — cùng tên với
   * `SubstituteDish.groupId`, nơi giá trị có thể là "ALLERGY". Cùng một tên
   * mang hai miền giá trị khác nhau, đọc code không biết được cái nào chứa gì.
   * Variant luôn thuộc một nhóm chế độ ăn nên gọi thẳng tên là xong.
   */
  dietGroupId: string;
  mode: "PARTIAL" | "FULL";
  portions: number;
  dishes: MenuVariantDish[];
  note?: string;
  /**
   * Vòng đời của suất riêng — 22/07/2026.
   *
   * Trước đó suất riêng không có trạng thái: khai lúc nào cũng được, kể cả gắn
   * vào thực đơn ĐÃ CÔNG BỐ, và không ai duyệt. Nghịch lý là sửa MỘT MÓN của
   * thực đơn đã duyệt thì bị chặn, mà thêm hẳn một thực đơn riêng cho cả bữa
   * thì không — Ban giám hiệu duyệt A, bếp nấu A + B.
   *
   * Nay:
   *   · khai lúc thực đơn còn DRAFT/REJECTED → `PLANNED`, đi kèm thực đơn đi
   *     duyệt, không cần duyệt riêng;
   *   · thêm vào thực đơn đã công bố → `PENDING_APPROVAL` + bắt buộc `reason`,
   *     chờ Ban giám hiệu duyệt thành `APPROVED` mới được nấu.
   *
   * Vì sao vẫn cho thêm sau khi công bố: học sinh nhập học giữa kỳ hoặc Y tế
   * vừa xác minh xong hồ sơ dị ứng là chuyện thường. Cấm hẳn thì bếp sẽ lách
   * bằng đường khác và hệ thống mất dấu hoàn toàn.
   */
  status?: "PLANNED" | "PENDING_APPROVAL" | "APPROVED" | "REJECTED";
  /** Bắt buộc khi thêm vào thực đơn đã công bố — vì sao phát sinh muộn. */
  reason?: string;
  createdAt?: string;
  approvedBy?: string;
  approvedAt?: string;
  rejectReason?: string;
}

/** Suất riêng đã được phép nấu: theo kế hoạch, hoặc bổ sung đã duyệt. */
export function isVariantServable(v: MenuVariant): boolean {
  return v.status == null || v.status === "PLANNED" || v.status === "APPROVED";
}

/**
 * Tổng dinh dưỡng của 1 variant, cộng từ thư viện món (BR-062).
 * `library` PHẢI là dữ liệu THẬT (`usePagedList<Dish>("dishes")`) — hằng seed
 * `DISH_LIBRARY` đã ngừng dùng làm nguồn 07/08/2026.
 */
export function variantNutrition(
  variant: Pick<MenuVariant, "dishes">,
  library: Pick<Dish, "id" | "kcal" | "protein" | "fat" | "carb">[] = []
): { kcal: number; protein: number; fat: number; carb: number } {
  return variant.dishes.reduce(
    (sum, d) => {
      const dish = library.find((x) => x.id === d.dishId);
      if (!dish) return sum;
      return {
        kcal: sum.kcal + dish.kcal,
        protein: sum.protein + dish.protein,
        fat: sum.fat + dish.fat,
        carb: sum.carb + dish.carb,
      };
    },
    { kcal: 0, protein: 0, fat: 0, carb: 0 }
  );
}

/** Thực đơn CHUNG của khối (audience = "ALL") — bỏ các thực đơn riêng. */
export function standardMenus<T extends { audience?: MenuAudience }>(menus: T[]): T[] {
  return menus.filter((m) => menuAudienceOf(m) === "ALL");
}

/**
 * Map trạng thái `Menu` riêng → trạng thái "suất riêng" mà UI/`isVariantServable`/
 * `dietCoverageGaps` hiểu. Phân biệt hai vòng đời:
 *   · ĐI KÈM thực đơn chung (không `reason`) — được duyệt CÙNG thực đơn chung nên
 *     luôn coi là ĐÃ CÓ KẾ HOẠCH (PLANNED/APPROVED), kể cả khi chung đang chờ duyệt.
 *   · BỔ SUNG muộn (có `reason`) — vòng đời riêng: chờ duyệt thì CHƯA được nấu.
 */
function menuMealStatus(m: { status?: string; reason?: string }): MenuVariant["status"] {
  if (m.status === "REJECTED") return "REJECTED";
  const approved = m.status === "APPROVED" || m.status === "PUBLISHED";
  if (m.reason) return approved ? "APPROVED" : "PENDING_APPROVAL";
  return approved ? "APPROVED" : "PLANNED";
}

/** Đầu vào tối thiểu để chiếu thực đơn riêng thành các buổi — nhận SeedMenu/PersistedMenu/db Row. */
interface MenuMealsInput {
  id: string;
  grade: string;
  menuDateIso?: string;
  audience?: MenuAudience;
  servings?: number;
  status?: string;
  reason?: string;
  approvedBy?: string;
  approvedAt?: string;
  createdAt?: string;
  rejectReason?: string;
  blocksData?: { mealType: MealType; dishes: { dishId?: string; name: string }[]; note?: string; portions?: number }[];
}

/**
 * PROJECTION (chỉ FE): tách mỗi THỰC ĐƠN RIÊNG (audience != "ALL") thành các "suất
 * riêng theo buổi" — đúng shape `MenuVariant` cũ để các màn render không phải đổi
 * logic. Backend KHÔNG có bảng này; nó chỉ là cách nhìn per-meal của một `Menu` riêng.
 * `menuId` trỏ về thực đơn CHUNG cùng khối+ngày để bộ lọc cũ `v.menuId === menu.id`
 * còn khớp.
 */
/**
 * Khôi phục id THỰC ĐƠN RIÊNG (`Menu`) từ id một buổi đã chiếu (`specialMenuMeals`).
 * Projection đặt id = `${menuId}:${mealType}` nên bỏ phần `:mealType` là ra menu.
 * Dùng khi duyệt/sửa: thao tác trên cả THỰC ĐƠN riêng, không phải từng buổi.
 */
export function specialMenuIdOfMeal(mealRowId: string): string {
  return mealRowId.replace(/:[^:]+$/, "");
}

export function specialMenuMeals(menus: MenuMealsInput[]): MenuVariant[] {
  const standards = menus.filter((m) => menuAudienceOf(m) === "ALL");
  const out: MenuVariant[] = [];
  for (const m of menus) {
    const group = menuAudienceOf(m);
    if (group === "ALL") continue;
    const std = standards.find((s) => s.grade === m.grade && s.menuDateIso === m.menuDateIso);
    const menuId = std?.id ?? m.id.replace(/--[^-].*$/, "");
    for (const b of m.blocksData ?? []) {
      out.push({
        id: `${m.id}:${b.mealType}`,
        menuId,
        mealType: b.mealType,
        dietGroupId: group,
        mode: "FULL",
        portions: b.portions ?? m.servings ?? 1,
        // Giữ TÊN khi chiếu: `blocksData` của thực đơn riêng có sẵn tên món, mà
        // `dishId` của món gõ tay chỉ là slug — vứt tên đi là màn đọc lại không
        // còn gì để hiện ngoài cái slug đó.
        dishes: b.dishes.map((d) => ({
          dishId: d.dishId ?? dishIdOfName(d.name),
          name: d.name || undefined,
        })),
        note: b.note,
        status: menuMealStatus(m),
        reason: m.reason,
        createdAt: m.createdAt,
        approvedBy: m.approvedBy,
        approvedAt: m.approvedAt,
        rejectReason: m.rejectReason,
      });
    }
  }
  return out;
}

// ===== Lịch sử dinh dưỡng (04_NutritionHistory — BR-062, BR-133) =====

/*
 * `NUTRITION_TARGET` XOÁ 07/08/2026 — chuẩn dinh dưỡng nay là DỮ LIỆU THẬT theo
 * TRƯỜNG × KHỐI: bảng `nutrition_targets` (migration V54), đọc qua
 * `lib/nutrition-target.ts useNutritionTarget(gradeLabel)`.
 *
 * Hằng cũ là bản sao thủ công của một `private static final` bên backend, và vì
 * `menus` đã gắn `grade_id` từ V44 nên nó khiến thực đơn Khối 1 và Khối 5 bị chấm
 * cùng một thước ±10%. ĐỪNG khai lại hằng ở FE: giá trị mặc định là quyết định của
 * server (`ReferenceResolver.DEFAULT_NUTRITION_TARGET`), đoán lại ở FE là tái lập
 * đúng lỗi hai-bản-sao vừa bỏ.
 */

/**
 * `NutritionDay` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng):
 * `parent/nutrition-history/page.tsx` đã nối thật `GET .../nutrition-history`
 * 06/08/2026 và tự khai type cục bộ khớp `NutritionDayResponse`/`NutritionTargetResponse`
 * — KHÔNG động vào (liên quan `NUTRITION_TARGET`, ngoài phạm vi phiên này, quyết định
 * riêng của Nam).
 */

// ===== Lịch sử bữa ăn chi tiết (12/13_MealHistory — BR-065, BR-069, BR-121) =====

/**
 * `MealHistoryEntry` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng):
 * `MealHistoryEntryDto` (= `Api<"MealHistoryEntryResponse">`) là projection dẫn xuất
 * (ADR-M09-2, KHÔNG bảng riêng) — chưa từng có mock type tương đương thật sự dùng,
 * xem `contract-check.ts` mục MF09.
 */


// ===== Bữa ăn đặc biệt (10/11 — UC-141/142, BR-065, BR-123) =====


// ===== Thực đơn tuần kế tiếp (14_SchoolMealsDashboard — BR-070) =====


// ===== Báo cáo an toàn thực phẩm khẩn cấp (18 — UC-073, BR-146..BR-155) =====

export const CONCERN_CATEGORIES = [
  "Nghi ngờ ngộ độc thực phẩm",
  "Dị vật trong thức ăn",
  "Thức ăn có mùi vị/màu sắc bất thường",
  "Phản ứng dị ứng sau bữa ăn",
  "Vệ sinh dụng cụ/khu vực ăn",
  "Khác",
];

// `CONCERN_SYMPTOMS` — ĐÃ BỎ 06/08/2026, zero usage (khác `CONCERN_CATEGORIES` phía trên, vẫn dùng).

// `TEACHER_CLASS` (mock session Teacher) — đã xoá 10/08/2026, 0 nơi còn đọc
// (dùng `useTeacherClass()` ở `lib/stores/auth-store.ts` thay thế).

// ===== Phân phát theo từng học sinh (08/09/10 — UC-016, BR-120/123/125/130) =====

/**
 * `ServingDish`/`StudentDistribution` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE
 * (0 nơi dùng thật): `student-distributions` đã nối thật 05/08/2026, mọi màn tiêu
 * thụ đọc thẳng `DistributionDto`/`DistributionCatererDto` (=
 * `Api<"DistributionResponse">`/`Api<"DistributionCatererResponse">`) + endpoint
 * `GET /planned-dishes` — xem `contract-check.ts` mục MF09.
 */


/**
 * Gộp bản ghi phát suất từng học sinh thành dòng tổng hợp NGÀY × LỚP.
 * Đây là nguồn của widget "Phân phát bữa ăn" (Giáo viên) và cột "Phân phát gần
 * nhất" (đơn vị suất ăn) — hai màn trước đây đọc mảng seed riêng nên không thấy
 * việc giáo viên vừa phát.
 */
/** Nới kiểu tham số — nhận cả `StudentDistribution` (mock) lẫn `DistributionDto` (thật, MF09
 *  nối 05/08/2026), chỉ cần 4 field dùng thật sự bên dưới (cùng nguyên tắc widen-tại-chỗ-gọi
 *  đã dùng cho `kitchen-confirm-badge.tsx`/`inspection-adapter.ts`). */
export function distributionSummaries(
  rows: { date: string; class: string; status: string; isSpecial?: string }[]
): DistributionRecord[] {
  const byKey = new Map<string, DistributionRecord>();
  for (const r of rows) {
    const key = `${r.date}|${r.class}`;
    const cur = byKey.get(key) ?? {
      id: `DST-${r.date.split("/").reverse().join("")}-${r.class}`,
      date: r.date,
      class: r.class,
      planned: 0,
      distributed: 0,
      specialMeals: 0,
      status: "PENDING" as keyof typeof STATUS_BADGE,
    };
    cur.planned += 1;
    if (r.status === "SERVED") cur.distributed += 1;
    if (r.isSpecial === "yes") cur.specialMeals += 1;
    byKey.set(key, cur);
  }
  return [...byKey.values()]
    .map((d) => ({
      ...d,
      status: (d.distributed === 0
        ? "PENDING"
        : d.distributed < d.planned
          ? "IN_PROGRESS"
          : "COMPLETED") as keyof typeof STATUS_BADGE,
    }))
    // Mới nhất lên đầu — chuỗi dd/mm/yyyy không sort trực tiếp được
    .sort((a, b) => parseVnDate(b.date) - parseVnDate(a.date));
}

/**
 * Sổ phát suất của giáo viên.
 *
 * Trước 22/07/2026 seed này ghi ngày 03/07 — NGÀY KHÔNG CÓ THỰC ĐƠN NÀO. Mọi nhãn
 * suất ("Tôm rim thịt", "Canh khoai tây cà rốt") là chuỗi gõ tay không khớp món
 * nào trong hệ thống: 10/10 dòng tra ngược không ra thực đơn. Hệ quả nặng nhất là
 * màn phát suất tra thực đơn theo (ngày, khối) không ra gì, nên bấm "Xác nhận đã
 * phục vụ" sẽ đóng băng một danh sách RỖNG — đúng thứ `servedDishes` sinh ra để tránh.
 *
 * Nay neo vào MENU-2026-27 (06/07/2026, Khối 3, PUBLISHED); mọi dòng sinh từ
 * `personalizeBlock` trên chính thực đơn đó.
 */


// ===== Ghi nhận tiêu thụ bữa ăn (07_MealRecords — UC-056/UC-080, BR-121/125) =====

/**
 * `TeacherMealRecord` (khai tay) XOÁ 07/08/2026 — `teacher/{meal-records,students/[id]}/page.tsx`
 * đọc thẳng `MealRecordDto` (= `Api<"MealRecordResponse">`), field khớp 1:1 (DTO thật còn
 * thừa `class`/`mealType`, chưa dùng).
 */

// ===== Thông tin sức khỏe/dị ứng học sinh — Teacher chỉ đọc (04/05 — UC-145, BR-030/031/172) =====

/**
 * `TeacherStudentHealth` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng):
 * `teacher/students/[id]/page.tsx` đọc thẳng `StudentDto`/`HealthFileDto`/
 * `AllergyDeclarationDto` (chiều cao/cân nặng/BMI/nhóm máu không có trên backend
 * thật — đã bỏ card đó, xem handoff "Chi tiết mục 5").
 */


/*
 * `SpecialDietRequirement`/`SPECIAL_DIET_REQUIREMENTS` — ĐÃ BỎ 06/08/2026. `teacher/dashboard`
 * (chỗ dùng cuối cùng) đã đổi sang đọc `special-meals` thật lọc theo lớp + status=APPROVED,
 * cùng cách `teacher/special-meals` đã làm từ trước.
 */

// ===== Giao dịch kho (BR-075 FEFO, BR-204 điều chỉnh có ký) =====

export interface InventoryTransaction {
  id: string;
  timestamp: string;
  type: "Nhập kho" | "Xuất kho (FEFO)" | "Điều chỉnh";
  batchId: string;
  ingredient: string;
  /** Có dấu: "+45 kg" nhập, "-12 kg" xuất/điều chỉnh giảm. */
  quantity: string;
  /** BR-204: bắt buộc với Điều chỉnh (spoilage, count-correction, damage…). */
  reasonCode?: string;
  actor: string;
  /** BR-204: chữ ký xác nhận quản lý cho điều chỉnh thủ công. */
  managerSignature?: string;

  // --- Truy xuất nguồn gốc (BR-076) — chỉ có ở giao dịch "Xuất kho (FEFO)" ---
  // Xuất kho là thời điểm DUY NHẤT một lô vật lý bị cam kết cho một mục đích cụ
  // thể, nên liên kết món ↔ lô được sinh ở đây rồi chảy xuống sổ chế biến, kiểm
  // thực và lưu mẫu. Nhập kho/Điều chỉnh không có món đích nên bỏ trống.
  /** Khóa join về INGREDIENT_CATALOG (batch.ingredientId tại thời điểm xuất). */
  ingredientId?: string;
  menuId?: string;
  mealType?: MealType;
  /** Tên món trong thực đơn — chọn từ blocksData, không nhập tay. */
  dishName?: string;
  /**
   * Khoá về THƯ VIỆN MÓN. Bản đồ truy xuất từng nối món↔lô bằng TÊN: phiếu xuất ghi
   * "Su su xào tỏi" còn thực đơn ghi "Su su xào" là mất liên kết, màn điều tra sự cố
   * hiện bảng lô rỗng mà không báo gì.
   */
  dishId?: string;
  /** Ngày phục vụ dd/mm/yyyy (khác `timestamp` = lúc thao tác xuất kho). */
  serviceDate?: string;
}


/*
 * ===== Phiếu xuất kho (IssueSlip) — ĐÃ BỎ 06/08/2026 =====
 *
 * `IssueSlip`/`ISSUE_SLIPS`/`issueSlipOfTransaction` — zero usage thật (kiểm tra lại: biến
 * `createIssueSlip` ở `inventory/issue/page.tsx` là tên trùng ngẫu nhiên, KHÔNG dùng type
 * này, gọi thẳng `POST /inventory/issue-slips` thật). Xoá cùng đợt dọn dead code sau khi nối
 * `supplier-products`/`parents`/`override-abuse-report`.
 */

// ===== Kế hoạch bữa ăn đặc biệt (SpecialMealPlan, BR-063/065/066/071) =====



// ===== Vi phạm quy trình & nhân sự (UC-178, BR-183/184/186) =====


// ===== Sổ chế biến (dữ liệu dẫn xuất nội bộ — nguồn cho bản đồ truy xuất món→lô
// BR-149/BR-151, xem `dishBatchMap()`/`dishBatchesForIncident()` cuối file; KHÔNG
// còn màn "Sổ chế biến" riêng nào tiêu thụ trực tiếp, đã bỏ 06/08/2026 vì trùng
// nội dung Bước 2 Kiểm thực ba bước và không có căn cứ pháp lý — nhưng cụm dữ
// liệu này vẫn là nguồn của `medical-staff/incidents/[id]` nên GIỮ LẠI) =====

/**
 * `PreparationRecord` (khai tay) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng ngoài
 * `mock-data.ts`): tính năng trùng Bước 2 Kiểm thực ba bước (không có căn cứ pháp lý
 * riêng, bỏ 06/08/2026), KHÔNG có endpoint backend nào tương ứng — xem
 * `contract-check.ts` mục MF06.
 */

// Seed khớp thực đơn thật: mỗi bản ghi neo vào (menuId, mealType) và các lô
// trùng với phiếu xuất FEFO cùng món trong INVENTORY_TRANSACTIONS bên dưới.
// (Sửa 21/07/2026: bản cũ gán BATCH-GF-4410 = Gạo tám thơm cho món "Cá basa sốt
// cà chua" và BATCH-GF-4412 = Rau cải ngọt cho "Canh chua cá" — lô không hề nằm
// trong thành phần món, bản đồ truy xuất BR-151 vì thế trỏ sai nguyên liệu.)
// ===== Khối lớp, tác nhân dị ứng & catalog nguyên liệu (đề xuất Minh 07/2026) =====

/** Thực đơn quản lý theo từng khối — Tiểu học 5 khối. */
/**
 * Các khối THẬT SỰ có lớp — dẫn xuất từ cấu hình lớp học của Ban giám hiệu.
 *
 * Trước 22/07/2026 đây là hằng số `["Khối 1".."Khối 5"]`. Hai chiều đều sai:
 * Ban giám hiệu tạo được lớp ở Khối 1–12 (BR-024) nhưng màn lập thực đơn chỉ liệt
 * kê tới Khối 5, nên tạo lớp Khối 6 là không ai lập được thực đơn cho khối đó mà
 * không có cảnh báo nào; ngược lại trường chưa mở Khối 2 thì màn vẫn mời chọn.
 *
 * `classrooms` BẮT BUỘC là dữ liệu THẬT do nơi gọi truyền vào (`usePagedList("classrooms")`)
 * — hằng seed `CLASSROOMS` (luôn rỗng) đã xoá 07/08/2026.
 */
export function gradeLevelsOf(classrooms: { grade?: string }[] = []): string[] {
  const source = classrooms ?? [];
  // `grade` ĐÃ là nhãn đầy đủ ("Khối 3") từ V44 — không tự thêm tiền tố nữa (xem Javadoc
  // `ClassroomDto` ở `lib/api/types.ts`). Sắp xếp theo số trong nhãn, KHÔNG theo thứ
  // tự chữ cái (tránh "Khối 10" đứng trước "Khối 2").
  // Tham số khai shape TỐI THIỂU (không `Pick<ClassroomDto,…>`) để `mock-data.ts` không phải
  // phụ thuộc `lib/api/types` — nơi gọi truyền thẳng `ClassroomDto[]` vẫn khớp cấu trúc.
  return [...new Set(source.map((c) => c.grade).filter((g): g is string => Boolean(g)))].sort(
    (a, b) => (Number(a.replace(/\D/g, "")) || 0) - (Number(b.replace(/\D/g, "")) || 0)
  );
}

/** Sĩ số của một khối, cộng từ các lớp thuộc khối đó. */
export function gradeEnrollmentOf(
  grade: string,
  classrooms: { grade?: string; students?: number }[] = []
): number {
  // `grade` tham số và `c.grade` đều là nhãn đầy đủ ("Khối 3") từ V44 — so thẳng
  // chuỗi, không còn tách số ra so với number nữa (xem Javadoc `ClassroomDto`).
  // `students` là field DẪN XUẤT (COUNT runtime) và optional trên DTO — lớp chưa có học sinh
  // nào cộng vào 0, không phải NaN.
  return classrooms.filter((c) => c.grade === grade).reduce((sum, c) => sum + (c.students ?? 0), 0);
}

/** Nguồn combobox tác nhân dị ứng (chuẩn hóa để BR-063 đối chiếu được với AllergyProfile). */
export const ALLERGENS = [
  "Tôm",
  "Cua",
  "Cá",
  "Hải sản",
  "Trứng",
  "Sữa bò",
  "Đậu phộng",
  "Đậu nành",
  "Lúa mì (Gluten)",
  "Mè (vừng)",
] as const;

/** Từ khóa nhận diện allergen trong tên thành phần — auto-detect khi nhập nguyên liệu món ăn. */
const ALLERGEN_KEYWORDS: Record<string, string[]> = {
  "Tôm": ["tôm"],
  "Cua": ["cua", "ghẹ"],
  "Cá": ["cá "],
  "Hải sản": ["mực", "ngao", "sò", "ốc", "hàu", "hải sản"],
  "Trứng": ["trứng"],
  "Sữa bò": ["sữa bò", "sữa tươi", "phô mai", "bơ động vật", "sữa chua", "kem sữa"],
  "Đậu phộng": ["đậu phộng", "lạc", "dầu lạc"],
  "Đậu nành": ["đậu nành", "đậu phụ", "đậu hũ", "nước tương", "tào phớ", "sữa đậu nành"],
  "Lúa mì (Gluten)": ["bột mì", "bánh mì", "mì sợi", "mì ống", "nui", "lúa mì"],
  "Mè (vừng)": ["mè", "vừng", "dầu mè"],
};

/** Nhận diện tác nhân dị ứng từ chuỗi tên thành phần (BR-063 auto-check phía UI). */
export function detectAllergens(ingredientText: string): string[] {
  const lower = ` ${ingredientText.toLowerCase()} `;
  return Object.entries(ALLERGEN_KEYWORDS)
    .filter(([, keywords]) => keywords.some((k) => lower.includes(k)))
    .map(([allergen]) => allergen);
}

// ---------------------------------------------------------------------------
// Quan hệ cha–con giữa các tác nhân dị ứng (BR-063)
// ---------------------------------------------------------------------------
// "Hải sản" là tác nhân Ô (umbrella): HS khai dị ứng "Hải sản" phải được cảnh báo
// với mọi món chứa Tôm/Cua/…, và ngược lại HS khai "Tôm" phải được cảnh báo với
// món chỉ tag chung "Hải sản". Trước đây quan hệ này chỉ áp cho MÓN THAY THẾ
// (menu-form) nên món GỐC lọt lưới — an toàn phụ thuộc vào việc người nhập liệu
// nhớ tag đủ cả 2 nhãn. Nay tập trung tại đây để mọi nơi dùng chung.
//
// Backend: nên thay bằng bảng Allergen(id, parent_id) — xem review_data_model_*.

/** Tác nhân con → tác nhân Ô chứa nó. */
const ALLERGEN_UMBRELLA: Record<string, string> = {
  "Tôm": "Hải sản",
  "Cua": "Hải sản",
};

/**
 * Tác nhân của HỌC SINH có "chạm" tác nhân của MÓN không.
 * Xét 2 chiều qua tác nhân Ô: HS "Hải sản" ↔ món "Tôm", và HS "Tôm" ↔ món "Hải sản".
 * Lưu ý KHÔNG bắc cầu giữa 2 con: HS "Tôm" KHÔNG chạm món chỉ có "Cua".
 */
export function allergenTouches(studentAllergen: string, dishAllergen: string): boolean {
  if (studentAllergen === dishAllergen) return true;
  const parentOfStudent = ALLERGEN_UMBRELLA[studentAllergen];
  const parentOfDish = ALLERGEN_UMBRELLA[dishAllergen];
  // Chỉ chạm khi một bên là tác nhân Ô của bên kia.
  return parentOfStudent === dishAllergen || parentOfDish === studentAllergen;
}

/** Món có tác nhân nào chạm tới tác nhân của học sinh không. */
export function dishTouchesAllergen(dishAllergens: string[], studentAllergen: string): boolean {
  return dishAllergens.some((da) => allergenTouches(studentAllergen, da));
}

/**
 * BR-031/BR-065: chỉ dị ứng ĐÃ được Y tế xác minh mới có hiệu lực pháp lý
 * (chặn duyệt thực đơn, bắt buộc suất riêng). Khai báo chưa xác minh vẫn phải
 * hiển thị để cảnh báo, nhưng không dùng làm căn cứ CHẶN.
 */
export function isAllergyVerified(s: Student): boolean {
  return s.healthStatus === "VERIFIED";
}

/**
 * Tác nhân CỤ THỂ này của em đã được Y tế xác minh chưa (BR-031).
 *
 * `isAllergyVerified` xét `healthStatus` ở mức HỌC SINH, nên em có 3 tác nhân mà
 * mới xác minh 1 thì cả 3 bị coi cùng trạng thái — sai với BR-031 vốn xác minh
 * từng khai báo kèm giấy tờ riêng. Mọi chỗ chặn/cảnh báo theo tác nhân phải dùng
 * hàm này; `isAllergyVerified` chỉ còn để hiển thị trạng thái hồ sơ chung.
 *
 * `declarations` PHẢI là dữ liệu THẬT (`usePagedList<AllergyDeclarationDto>
 * ("allergy-declarations")`) — hằng seed `ALLERGY_DECLARATIONS` đã ngừng dùng làm nguồn
 * 07/08/2026. ⚠️ `GET /api/v1/allergy-declarations` chỉ cấp quyền
 * SCHOOL_ADMIN/MEDICAL_STAFF/PARENT/TEACHER (`AllergyDeclarationController`) — KHÔNG có
 * SERVICE_CATERERS, nên các màn Service Caterers (lập/duyệt thực đơn) KHÔNG gọi được API
 * này (403). Bỏ trống `declarations` ở các màn đó — xem `gradeAllergyStudents()`.
 */
export function isAllergenVerified(
  studentId: string,
  allergen: string,
  declarations: { studentId?: string; allergen?: string; status?: string }[] = []
): boolean {
  return declarations.some(
    (a) => a.studentId === studentId && a.allergen === allergen && a.status === "VERIFIED"
  );
}

/**
 * Tra tác nhân dị ứng của 1 dòng thành phần món — CHỈ còn dò từ khoá trên tên
 * (`detectAllergens`, kém tin cậy hơn catalog — chỉ bắt được chữ có dấu).
 *
 * 07/08/2026 — NỢ KỸ THUẬT BACKEND: trước đây hàm này ưu tiên tra `ingredientId`/tên
 * trong hằng seed `INGREDIENT_CATALOG.allergens` (chính xác tuyệt đối, BR-076) rồi mới lùi
 * về heuristic. `GET /api/v1/ingredients` (`IngredientResponse`, MF06, nối thật 06/08/2026)
 * KHÔNG có field `allergens` — bảng nguyên liệu chuẩn ở backend chưa lưu tác nhân dị ứng
 * theo nguyên liệu, nên đường tra catalog KHÔNG có dữ liệu thật để thay — bỏ hẳn hai bước
 * đó thay vì giữ đọc `INGREDIENT_CATALOG` (seed, không phản ánh nguyên liệu mới khai qua
 * MF06 thật). Cần MF06 bổ sung field/bảng allergens cho `Ingredient` nếu muốn chính xác lại
 * bằng catalog thay vì đoán từ khoá tên. Xem `IngredientDto` (`lib/api/types.ts`).
 */
export function resolveIngredientAllergens(row: {
  name: string;
  ingredientId?: string;
}): { allergens: string[]; source: "keyword" | "unknown" } {
  const name = row.name.trim();
  if (!name) return { allergens: [], source: "unknown" };

  const guessed = detectAllergens(name);
  return { allergens: guessed, source: guessed.length > 0 ? "keyword" : "unknown" };
}

/**
 * Catalog nguyên liệu — nguồn combobox thành phần món ăn + dòng đơn đặt hàng.
 *
 * `id` là KHÓA JOIN chuẩn của toàn hệ (BR-076): thành phần món trong thực đơn,
 * lô trong kho (`InventoryBatch.ingredientId`) và phiếu xuất kho đều trỏ về đây.
 * Trước 21/07/2026 các bảng này join bằng `name` dạng chuỗi tự do nên lệch âm
 * thầm ("Tôm sú đông lạnh" ≠ "Tôm sú", "Thịt lợn nạc vai (bổ sung)" ≠ "Thịt lợn
 * nạc vai") — bản đồ truy xuất BR-151 vì thế bỏ sót lô. Tên chỉ còn để hiển thị.
 *
 * `unit` là ĐƠN VỊ GỐC dùng để quy đổi định lượng BOM (g/ml mỗi suất) sang tồn kho.
 */
export interface CatalogIngredient {
  id: string;
  name: string;
  unit: string;
  allergens: string[];
  /**
   * HỆ SỐ THẢI BỎ — phần bỏ đi khi sơ chế, tính trên khối lượng mua (0–1).
   *
   * Định lượng trong công thức món là phần ĂN ĐƯỢC, nhưng bếp phải mua/xuất nhiều
   * hơn: gọt vỏ bí, nhặt rau, bỏ đầu vỏ tôm, lọc xương. Đến 22/07/2026 mô hình chỉ
   * có MỘT con số nên phiếu xuất kho xuất đúng bằng lượng ăn được — bếp nấu xong
   * sẽ thiếu. Tỉ lệ tham chiếu Bảng thành phần thực phẩm Việt Nam (Viện Dinh dưỡng).
   *
   * Lượng thực mua = lượng ăn được ÷ (1 − refuseFactor).
   */
  refuseFactor?: number;
}


/**
 * SẢN PHẨM CỦA NHÀ CUNG CẤP — (NCC × nguyên liệu), mang giá và là cha của các LÔ.
 *
 * Một sản phẩm có NHIỀU lô hàng: "Gạo tám thơm" của GreenFarm về hai đợt khác nhau
 * với hạn dùng khác nhau. Trước 22/07/2026 phía NCC không có tầng sản phẩm nào —
 * `SupplierBatch.productName` là chuỗi tự do, nên hai lô gạo chỉ liên hệ nhau bằng
 * TRÙNG TÊN, và khâu nhận hàng phải dò tên để đoán ra nguyên liệu nào trong danh
 * mục. Cùng một lỗi với phía trường, chỉ ở đầu bên kia của chuỗi truy xuất.
 *
 * Vì sao là bảng riêng chứ không phải cột `price` trên `CatalogIngredient`:
 *   - Cùng một nguyên liệu có nhiều NCC bán với giá khác nhau; gắn giá lên nguyên
 *     liệu là mất khả năng so sánh và mất luôn NCC nào đang bán.
 *   - Giá đổi theo thời gian. Thực đơn đã duyệt phải giữ chi phí tại thời điểm
 *     duyệt, nên cần biết giá NÀO có hiệu lực ngày nào.
 * Và vì sao món ăn KHÔNG trỏ thẳng vào bảng này: món ăn là công thức, sống lâu hơn
 * hợp đồng. Đình chỉ một NCC (BR-070) mà món trỏ thẳng vào họ là gãy cả thực đơn.
 * Món → danh mục chuẩn → (bảng này) → nhà cung cấp.
 */
/**
 * `SupplierProduct` (khai tay) XOÁ 07/08/2026 — dẫn xuất `Pick<SupplierProductDto,...>` ở
 * `lib/supplier-product-adapter.ts` (Giai đoạn 3, phiên 5). Mọi nơi dùng đổi import sang đó.
 */

/**
 * `SUPPLIER_PRODUCTS` (seed) XOÁ 07/08/2026 — đã là DEAD CODE (0 nơi dùng thật): mọi tham số
 * `prices: ... = SUPPLIER_PRODUCTS` chỉ còn là default KHÔNG BAO GIỜ trúng (mọi nơi gọi thật
 * đã truyền `prices` thật từ `usePagedList<SupplierProductDto>("supplier-products")` — kiểm
 * phiên 5, xem Handoff). `productLabel()` đã ngừng đọc hằng này từ 07/08/2026 (đợt trước).
 */

/**
 * Nhãn hiển thị của một sản phẩm NCC. `products` PHẢI là dữ liệu THẬT
 * (`usePagedList<SupplierProduct>("supplier-products")`) — hằng seed `SUPPLIER_PRODUCTS`
 * đã ngừng dùng làm nguồn 07/08/2026; `ingredientIdOfProduct()` (đọc cùng hằng, không còn
 * nơi nào gọi) đã bỏ cùng đợt.
 */
export function productLabel(productId: string, products: SupplierProduct[] = []): string {
  const prod = products.find((p) => p.id === productId);
  if (!prod) return productId;
  return prod.label ?? prod.ingredientId ?? productId;
}

/**
 * Giá tốt nhất đang có của một nguyên liệu — chỉ xét NCC còn hợp tác.
 * Nhà cung cấp bị đình chỉ (BR-070) thì báo giá của họ không dùng để ước tính nữa.
 */
export function bestPriceOf(
  ingredientId: string | undefined,
  prices: SupplierProduct[] = [],
  partners: { id?: string; status?: string }[] = []
): SupplierProduct | undefined {
  if (!ingredientId) return undefined;
  const usable = prices.filter(
    (p) =>
      p.ingredientId === ingredientId &&
      partners.find((s) => s.id === p.supplierId)?.status === "APPROVED"
  );
  return usable.sort((a, b) => a.unitPrice - b.unitPrice)[0];
}

/*
 * `dishSourcing()` (suy NCC nguồn gốc của món từ nguyên liệu, BR-076) đã XOÁ 07/08/2026:
 * nơi gọi duy nhất là `parent/nutrition/[dishId]/page.tsx`, nay đọc thẳng endpoint
 * `GET /supplier-products/sourcing` — logic "giá rẻ nhất + đối tác APPROVED" tính Ở SERVER
 * (`SupplierProductService.sourcing()`), không trả `unitPrice`. Dựng endpoint riêng thay vì
 * mở quyền PARENT cho `/partners` + `/supplier-products` (list có giá — dữ liệu thương mại).
 * `bestPriceOf()` ở trên GIỮ NGUYÊN: các màn nội bộ (`menu-form.tsx`,
 * `service-caterers/orders/new`) cần chính con SỐ giá để ước tính chi phí, và có đủ quyền
 * gọi 2 endpoint kia.
 */

/**
 * Viết lại 10/08/2026 — trước đây `toPurchaseQuantity`/`dishCostPerServing`/
 * `substituteCostDelta`/`estimateMenuCostPerServing`/`mealIngredientNeeds` tra hệ số thải bỏ
 * qua `INGREDIENT_CATALOG` (mảng mock, đã xoá cùng đợt dọn seed). 4 hàm ĐẦU không phải mock —
 * `menu-form.tsx` gọi với dữ liệu THẬT (`GET /ingredients`, `costSupplierProducts`,
 * `costPartners`) để hiện chi phí ước tính lúc lập thực đơn (BR-080); xoá `INGREDIENT_CATALOG`
 * làm gãy build chứ không phải dọn được tính năng này. Nay nhận danh mục nguyên liệu THẬT
 * (`GET /ingredients`, đã có sẵn `refuseFactor` — `IngredientResponse.refuseFactor`, mf06) qua
 * tham số thay vì đọc hằng nội bộ.
 */
export type IngredientCatalogRef = { id?: string; name?: string; refuseFactor?: number };

/** `ingredientId` có sẵn thì ưu tiên tra theo ID (chuẩn); thiếu thì lùi về so TÊN (dữ liệu cũ). */
function resolveIngredientRef(
  ing: { name: string; ingredientId?: string },
  catalog: IngredientCatalogRef[]
): IngredientCatalogRef | undefined {
  if (ing.ingredientId) {
    return catalog.find((c) => c.id === ing.ingredientId) ?? { id: ing.ingredientId };
  }
  return catalog.find((c) => c.name === ing.name);
}

/**
 * Lượng THỰC MUA từ lượng ĂN ĐƯỢC, theo hệ số thải bỏ của nguyên liệu.
 *
 * Công thức món ghi phần ăn được; bếp phải xuất nhiều hơn vì còn gọt vỏ, nhặt rau,
 * bỏ đầu vỏ tôm. Trước 22/07/2026 hai lượng này là MỘT, nên phiếu xuất kho xuất
 * đúng bằng lượng ăn được và bếp nấu xong sẽ thiếu — 45% với tôm sú, 35% với thịt gà.
 */
export function toPurchaseQuantity(edible: number, refuseFactor: number | undefined, baseUnit: string): number {
  // Chỉ áp cho khối lượng/thể tích. Đơn vị ĐẾM thì công thức đã ghi đúng thứ phải
  // mua: "3 quả trứng cút" là mua 3 quả cả vỏ, không phải 3.66 quả vì vỏ chiếm 18%.
  if (baseUnit !== "kg" && baseUnit !== "lít") return Number(edible.toFixed(3));
  const rf = refuseFactor ?? 0;
  if (rf <= 0 || rf >= 1) return Number(edible.toFixed(3));
  return Number((edible / (1 - rf)).toFixed(3));
}

/**
 * Chi phí nguyên liệu của MỘT SUẤT một món.
 *
 * Dùng để so món gốc với suất thay thế: suất riêng cho nhóm dị ứng/chế độ ăn có
 * thể đắt hơn đáng kể (thay thịt lợn bằng thịt gà Halal, thay tôm bằng thịt băm),
 * và người lập thực đơn nên biết TRƯỚC khi nộp chứ không phải sau khi bếp đi chợ.
 */
export function dishCostPerServing(
  dish: { ingredients: { name: string; quantity: string; unit: string; ingredientId?: string }[] },
  ingredientCatalog: IngredientCatalogRef[] = [],
  prices: SupplierProduct[] = [],
  partners: { id?: string; status?: string }[] = []
): { cost: number; missingPrices: string[] } {
  let cost = 0;
  const missing: string[] = [];
  for (const ing of dish.ingredients) {
    const ref = resolveIngredientRef(ing, ingredientCatalog);
    const base = toBaseQuantity(Number(ing.quantity) || 0, ing.unit);
    const price = bestPriceOf(ref?.id, prices, partners);
    if (!price) {
      if (ing.name.trim() && !missing.includes(ing.name)) missing.push(ing.name);
      continue;
    }
    cost += toPurchaseQuantity(base.qty, ref?.refuseFactor, base.unit) * price.unitPrice;
  }
  return { cost: Math.round(cost), missingPrices: missing };
}

/**
 * So chi phí suất thay thế với món gốc.
 *
 * `extraTotal` tính trên ĐÚNG số suất thay thế, không nhân với sĩ số cả khối:
 * suất chay đắt hơn 5.000₫ cho 3 em là 15.000₫, không phải 5.000₫ × 380. Nói sai
 * chỗ này thì con số nghe đáng sợ hơn thực tế và người dùng học cách bỏ qua cảnh báo.
 */
export function substituteCostDelta(
  original: { ingredients: { name: string; quantity: string; unit: string; ingredientId?: string }[] },
  substitute: { ingredients: { name: string; quantity: string; unit: string; ingredientId?: string }[]; portions?: string },
  ingredientCatalog: IngredientCatalogRef[] = [],
  prices: SupplierProduct[] = [],
  partners: { id?: string; status?: string }[] = []
): { originalCost: number; substituteCost: number; delta: number; portions: number; extraTotal: number; comparable: boolean } {
  const a = dishCostPerServing(original, ingredientCatalog, prices, partners);
  const b = dishCostPerServing(substitute, ingredientCatalog, prices, partners);
  const portions = Number(substitute.portions) || 0;
  return {
    originalCost: a.cost,
    substituteCost: b.cost,
    delta: b.cost - a.cost,
    portions,
    extraTotal: (b.cost - a.cost) * portions,
    // Thiếu báo giá ở một trong hai bên thì phép so là vô nghĩa — phải nói ra,
    // không hiển thị một con số chênh lệch dựng trên dữ liệu khuyết.
    comparable: a.missingPrices.length === 0 && b.missingPrices.length === 0,
  };
}

/**
 * Nhu cầu nguyên liệu của 1 buổi ăn thuộc 1 thực đơn — KHÔNG phân bổ lô, chỉ phục vụ ước tính
 * chi phí (`estimateMealCostPerServing`). Xuất kho THẬT tính server-side qua
 * `GET /inventory/issue-plan` (MF06 — không còn mô phỏng FEFO phía client).
 */
function mealIngredientNeeds(
  menu: SeedMenu,
  mealType: MealType,
  servings: number,
  ingredientCatalog: IngredientCatalogRef[] = []
): { dishName: string; ingredientId?: string; ingredientName: string; perServing: string; edible: number; needed: number; unit: string }[] {
  const block = menu.blocksData?.find((b) => b.mealType === mealType);
  if (!block) return [];
  const rows: { dishName: string; ingredientId?: string; ingredientName: string; perServing: string; edible: number; needed: number; unit: string }[] = [];
  const add = (
    dishName: string,
    ing: { name: string; quantity: string; unit: string; ingredientId?: string },
    portions: number
  ) => {
    const base = toBaseQuantity(Number(ing.quantity) || 0, ing.unit);
    const ingredientId = ing.ingredientId ?? resolveIngredientRef(ing, ingredientCatalog)?.id;
    const edible = Number((base.qty * portions).toFixed(3));
    rows.push({
      dishName,
      ingredientId,
      ingredientName: ing.name,
      perServing: `${ing.quantity} ${ing.unit}`,
      edible,
      // Chi phí ước tính tính THẲNG theo định lượng công thức × số suất, KHÔNG cộng hệ số
      // thải bỏ (yêu cầu 22/07/2026 — Minh): khớp đúng định lượng đã khai trong thực đơn.
      needed: edible,
      unit: base.unit,
    });
  };
  for (const d of block.dishes) {
    for (const ing of d.ingredients) add(d.name, ing, servings);
    for (const sub of seedSubsOf(d)) {
      const portions = Number(sub.portions) || 0;
      for (const ing of sub.ingredients) add(`${sub.name} (suất thay thế)`, ing, portions);
    }
  }
  return rows;
}

/**
 * ƯỚC TÍNH CHI PHÍ MỘT SUẤT của một bữa trong thực đơn (BR-080).
 *
 * Trước 22/07/2026 `menu-form` ghi thẳng `estimatedCost: 0` cho MỌI thực đơn tạo
 * mới, trong khi màn duyệt của Ban giám hiệu so `estimatedCost` với `costCeiling`
 * — nên thực đơn mới luôn hiện 0₫/30.000₫ và LUÔN ĐẠT trần. Seed thì có số thật
 * (26.400–31.500₫) nên nhìn seed không phát hiện ra.
 *
 * Tính trên lượng THỰC MUA (đã gồm hệ số thải bỏ) × đơn giá tốt nhất đang có —
 * đúng số tiền phải trả, không phải số tiền của phần ăn được.
 *
 * Trả về cả `missingPrices` để màn hình nói thẳng phần chưa có báo giá thay vì âm
 * thầm tính thiếu rồi báo một con số đẹp.
 */
export function estimateMealCostPerServing(
  menu: SeedMenu,
  mealType: MealType,
  servings: number,
  ingredientCatalog: IngredientCatalogRef[] = [],
  prices: SupplierProduct[] = [],
  partners: { id?: string; status?: string }[] = []
): { perServing: number; total: number; missingPrices: string[] } {
  if (servings <= 0) return { perServing: 0, total: 0, missingPrices: [] };
  let total = 0;
  const missing: string[] = [];
  for (const r of mealIngredientNeeds(menu, mealType, servings, ingredientCatalog)) {
    const price = bestPriceOf(r.ingredientId, prices, partners);
    if (!price) {
      if (!missing.includes(r.ingredientName)) missing.push(r.ingredientName);
      continue;
    }
    total += r.needed * price.unitPrice;
  }
  return {
    perServing: Math.round(total / servings),
    total: Math.round(total),
    missingPrices: missing,
  };
}

/** Chi phí ước tính một suất cho CẢ NGÀY của thực đơn (cộng mọi bữa). */
export function estimateMenuCostPerServing(
  menu: SeedMenu,
  servings: number,
  ingredientCatalog: IngredientCatalogRef[] = [],
  prices: SupplierProduct[] = [],
  partners: { id?: string; status?: string }[] = []
): { perServing: number; missingPrices: string[] } {
  const blocks = menu.blocksData ?? [];
  let per = 0;
  const missing: string[] = [];
  for (const b of blocks) {
    const r = estimateMealCostPerServing(menu, b.mealType, servings, ingredientCatalog, prices, partners);
    per += r.perServing;
    for (const nm of r.missingPrices) if (!missing.includes(nm)) missing.push(nm);
  }
  return { perServing: per, missingPrices: missing };
}

/** Suy khối từ tên lớp: "3A" → "Khối 3". */
export function gradeOfClass(className: string): string {
  const digit = className.match(/^\d/)?.[0];
  return digit ? `Khối ${digit}` : "Khối ?";
}

/**
 * Hồ sơ dị ứng theo khối (BR-063): allergen → số học sinh của khối bị dị ứng.
 *
 * `students` BẮT BUỘC là dữ liệu THẬT (`usePagedList<StudentDto>("students")`) — KHÔNG còn
 * default về hằng seed `STUDENTS` (đã ngừng dùng làm nguồn 07/08/2026); cố tình bắt buộc
 * (không cho `= []` ngầm) để `tsc` bắt lỗi tại mọi nơi gọi thiếu tham số thay vì âm thầm trả
 * về rỗng — đây là cảnh báo AN TOÀN THỰC PHẨM (BR-063), im lặng sai là nguy hiểm hơn vỡ build.
 *
 * LƯU Ý MÔ HÌNH: `StudentResponse.allergies` (DTO thật) là field DẪN XUẤT — CHỈ gồm allergen
 * ĐÃ XÁC MINH (ADR-M03-3), khác `Student.allergies` (mock) từng gồm cả chưa xác minh. Vì vậy:
 *   · `opts.verifiedOnly = true` (mặc định khi có `students` thật, không cần `declarations`):
 *     đọc thẳng `students[].allergies` là đủ chính xác.
 *   · `opts.verifiedOnly = false` (muốn đếm CẢ chưa xác minh) cần truyền thêm `declarations`
 *     (`GET /allergy-declarations` — CHỈ SCHOOL_ADMIN/MEDICAL_STAFF/PARENT/TEACHER gọi được,
 *     xem Javadoc `isAllergenVerified()`); không có thì hàm coi `students[].allergies` như
 *     đã đủ (không đếm thêm được phần chưa xác minh — nợ kỹ thuật ở màn Service Caterers).
 */
export function gradeAllergySummary(
  grade: string,
  opts: { verifiedOnly?: boolean } = {},
  students: Pick<Student, "id" | "class" | "allergies">[],
  declarations: { studentId?: string; allergen?: string; status?: string }[] = []
): Record<string, number> {
  const summary: Record<string, number> = {};
  for (const s of students) {
    if (gradeOfClass(s.class) !== grade) continue;
    for (const a of s.allergies) {
      if (opts.verifiedOnly && declarations.length > 0 && !isAllergenVerified(s.id, a, declarations)) continue;
      summary[a] = (summary[a] ?? 0) + 1;
    }
  }
  return summary;
}

/**
 * Danh sách học sinh của khối bị dị ứng với 1 tác nhân cụ thể — dùng cho
 * cảnh báo chi tiết khi tạo thực đơn (BR-063): số lượng, lớp nào, mức độ.
 *
 * Dùng `allergenTouches` chứ KHÔNG so khớp chuỗi chính xác, để HS khai "Hải sản"
 * vẫn được cảnh báo với món tag "Tôm" và ngược lại.
 * `verifiedOnly`: chỉ lấy HS đã được Y tế xác minh (căn cứ CHẶN duyệt — BR-031/065).
 */
/** BẮT BUỘC `students` thật — xem ghi chú mô hình dữ liệu ở `gradeAllergySummary()`. */
export function gradeAllergyStudents<T extends Pick<Student, "id" | "class" | "allergies">>(
  grade: string,
  allergen: string,
  opts: { verifiedOnly?: boolean } = {},
  students: T[],
  declarations: { studentId?: string; allergen?: string; status?: string }[] = []
): T[] {
  return students.filter(
    (s) =>
      gradeOfClass(s.class) === grade &&
      s.allergies.some(
        (sa) =>
          allergenTouches(sa, allergen) &&
          (!opts.verifiedOnly || declarations.length === 0 || isAllergenVerified(s.id, sa, declarations))
      )
  );
}


/** Nhóm chế độ ăn cho thực đơn đặc biệt (BR-066) — chỉ tạo được khi nhóm có học sinh. */
/**
 * Nhóm chế độ ăn.
 *
 * Trước 22/07/2026 nhóm chỉ có `id` + nhãn — không mang một chút ngữ nghĩa nào
 * về việc nhóm đó KIÊNG GÌ. Hệ quả: không màn nào trả lời được câu hỏi cơ bản
 * nhất — "bữa này em ăn chay có ăn được không?" — nên cũng không ai kiểm được
 * thực đơn đã phủ hết các nhóm chưa. Người lập thực đơn phải tự nhớ.
 *
 * Hai cơ chế vì hai loại kiêng khác nhau:
 *   · `excludeAllergens` — kiêng theo TÁC NHÂN đã có sẵn trong hệ thống
 *     (không lactose = tránh "Sữa bò"), dùng chung bộ nhận diện với dị ứng;
 *   · `excludeIngredientKeywords` — kiêng theo LOẠI THỰC PHẨM, thứ không phải
 *     tác nhân dị ứng (chay tránh thịt, Halal tránh thịt lợn).
 */
/**
 * `DietGroup` (khai tay) XOÁ 07/08/2026 — dẫn xuất `Pick<DietGroupDto,...>` ở
 * `lib/diet-groups.ts` (Giai đoạn 3, phiên 5). Mọi nơi dùng đổi import sang đó.
 */

/**
 * Học sinh thuộc 1 nhóm chế độ ăn (đã Y tế xác minh mới đủ điều kiện — BR-065).
 *
 * Danh mục nhóm chế độ ăn truyền vào PHẢI là dữ liệu THẬT (`useDietGroups()`,
 * `GET /diet-groups`) — hằng `DIET_GROUPS` (seed) đã xoá 07/08/2026.
 *
 * `students` BẮT BUỘC là dữ liệu THẬT (`usePagedList<StudentDto>("students")` qua
 * `studentDtoToMock()`) — hằng `STUDENTS` (seed) ngừng dùng làm nguồn 07/08/2026, cùng
 * đợt với `gradeAllergyStudents()` (xem handoff mục "Chi tiết mục 5").
 */
export function dietGroupMembers(
  groupId: string,
  dietGroups: DietGroup[],
  students: Student[]
): Student[] {
  const group = dietGroups.find((g) => g.id === groupId);
  if (!group?.matchDiet) return [];
  return students.filter((s) => s.specialDiet === group.matchDiet);
}

/**
 * Thành viên nhóm chế độ ăn GIỚI HẠN trong 1 khối — dùng khi kế hoạch bữa ăn
 * đặc biệt neo vào thực đơn theo khối (hướng A, 07/07/2026): nhóm trải nhiều
 * khối thì mỗi khối lập kế hoạch riêng theo thực đơn của khối đó.
 */
export function dietGroupMembersInGrade(
  groupId: string,
  grade: string,
  dietGroups: DietGroup[],
  students: Student[]
): Student[] {
  return dietGroupMembers(groupId, dietGroups, students).filter(
    (s) => gradeOfClass(s.class) === grade
  );
}

/**
 * Nhóm chế độ ăn CÓ HỌC SINH trong khối — thứ một thực đơn của khối phải phủ.
 *
 * Danh mục nhóm chế độ ăn truyền vào PHẢI là dữ liệu THẬT (`useDietGroups()`,
 * `GET /diet-groups`) — hằng `DIET_GROUPS` (seed) đã xoá 07/08/2026.
 */
export function dietGroupsNeededByGrade(
  grade: string,
  dietGroups: DietGroup[],
  students: Student[]
): DietGroup[] {
  return dietGroups.filter((g) =>
    students.some((s) => gradeOfClass(s.class) === grade && s.specialDiet === g.matchDiet)
  );
}

/** Một món có hợp với nhóm chế độ ăn không — và nếu không thì vướng ở đâu. */
export function dishConflictsWithDiet(
  dish: { name?: string; ingredients?: { name: string }[]; manualAllergens?: string[] },
  group: DietGroup
): string[] {
  const reasons: string[] = [];
  const text = [dish.name ?? "", ...(dish.ingredients ?? []).map((i) => i.name)]
    .join(" ")
    .toLowerCase();
  for (const kw of group.excludeIngredientKeywords ?? []) {
    if (text.includes(kw)) reasons.push(kw);
  }
  if (group.excludeAllergens?.length) {
    const found = new Set<string>(dish.manualAllergens ?? []);
    for (const ing of dish.ingredients ?? []) {
      for (const a of detectAllergens(ing.name)) found.add(a);
    }
    for (const a of group.excludeAllergens) {
      if ([...found].some((f) => allergenTouches(a, f))) reasons.push(a);
    }
  }
  return [...new Set(reasons)];
}

/**
 * Nhóm chế độ ăn có học sinh nhưng CHƯA ĂN ĐƯỢC bữa này.
 *
 * Điều kiện nộp duyệt thứ 8 (BR-065/BR-125). Trước 22/07/2026 không có kiểm tra
 * nào như vậy, nên thực đơn công bố xong vẫn hở nhóm — đến bữa các em thuộc
 * nhóm đó không có gì ăn mà không màn nào báo.
 *
 * "Chưa ăn được" chứ không phải "chưa có suất riêng": bữa gốc đã hợp với nhóm
 * thì các em ăn chung, không cần suất riêng. Bắt phải có suất riêng cho mọi
 * nhóm sẽ đẻ ra một đống suất trùng hệt bữa chính, vừa tốn công bếp vừa làm dữ
 * liệu nói dối về khối lượng nấu thật.
 *
 * Chỉ tính suất ĐƯỢC PHÉP NẤU: suất bổ sung còn chờ Ban giám hiệu duyệt thì
 * chưa lấp được chỗ trống.
 */
export interface DietGap {
  mealType: MealType;
  group: DietGroup;
  students: number;
  /** Món của bữa gốc khiến nhóm không ăn được, kèm lý do vướng. */
  blockedBy: { dish: string; reasons: string[] }[];
}

export function dietCoverageGaps(
  grade: string,
  menuId: string,
  blocks: { mealType: MealType; dishes: { name?: string; ingredients?: { name: string }[]; manualAllergens?: string[] }[] }[],
  variants: MenuVariant[],
  dietGroups: DietGroup[],
  students: Student[]
): DietGap[] {
  const needed = dietGroupsNeededByGrade(grade, dietGroups, students);
  const gaps: DietGap[] = [];
  for (const block of blocks) {
    for (const group of needed) {
      const hasVariant = variants.some(
        (v) =>
          v.menuId === menuId &&
          v.mealType === block.mealType &&
          v.dietGroupId === group.id &&
          isVariantServable(v)
      );
      if (hasVariant) continue;
      const blockedBy = block.dishes
        .map((d) => ({ dish: d.name ?? "(chưa đặt tên)", reasons: dishConflictsWithDiet(d, group) }))
        .filter((x) => x.reasons.length > 0);
      if (blockedBy.length === 0) continue; // bữa gốc đã hợp — ăn chung được
      gaps.push({
        mealType: block.mealType,
        group,
        students: students.filter(
          (s) => gradeOfClass(s.class) === grade && s.specialDiet === group.matchDiet
        ).length,
        blockedBy,
      });
    }
  }
  return gaps;
}

// ============================================================================
// Bảng nấu ăn của bếp — "hôm nay nấu món gì, mỗi món mấy suất" (BR-082)
// ============================================================================
//
// Chỗ trống trước 23/07/2026: có màn xuất kho (nguyên liệu × số lượng), có màn
// phục vụ (từng học sinh), có sổ chế biến (ghi SAU khi nấu), nhưng KHÔNG có bảng
// nói cho bếp trước giờ nấu: món này bao nhiêu suất. Nhiều khối cùng ăn một bữa
// và dùng chung món — "Cơm trắng" ngày 06/07 là 1030 suất gộp ba khối — mà bếp
// phải mở từng thực đơn khối rồi tự cộng, dễ nấu thiếu.
//
// Ba loại việc nấu, tách bạch vì bếp thao tác khác nhau:
//   · MAIN        — nồi lớn, gộp mọi khối dùng chung món;
//   · ALLERGY_SUB — suất thay MỘT món cho em dị ứng, nấu nồi nhỏ riêng (BR-082);
//   · DIET_VARIANT— cả bữa riêng cho nhóm chế độ ăn, nấu và chia riêng.

export interface CookLine {
  dish: string;
  portions: number;
  kind: "MAIN" | "ALLERGY_SUB" | "DIET_VARIANT";
  /** Khối (món chính) hoặc nhóm chế độ ăn (suất riêng) đóng góp vào dòng này. */
  sources: string[];
  note?: string;
}

export interface CookSheet {
  dateIso: string;
  mealType: MealType;
  main: CookLine[];
  special: CookLine[];
  totalMain: number;
  totalSpecial: number;
  grades: string[];
}

/**
 * Bảng nấu của một NGÀY + BUỔI, gộp mọi khối đang phục vụ.
 *
 * Số suất món chính đã TRỪ các em ăn suất riêng (dị ứng thay món đó, hoặc theo
 * cả một thực đơn chế độ ăn) — nấu đúng số nồi lớn cần, phần riêng nằm ở mục
 * special. Nếu để nguyên sĩ số thì cộng suất riêng vào là nấu dư.
 */
export function cookSheet(
  dateIso: string,
  mealType: MealType,
  menus: SeedMenu[],
  variants: MenuVariant[] = specialMenuMeals(menus),
  /** Danh mục nhóm chế độ ăn THẬT (`useDietGroups()`, `GET /diet-groups`) — chỉ dùng để
   *  hiện nhãn "Suất <nhóm>: …" ở mục special, KHÔNG còn đọc hằng `DIET_GROUPS` (seed). */
  dietGroups: DietGroup[] = [],
  /** Thư viện món THẬT (`usePagedList<Dish>("dishes")`) — để tra tên món của suất TRỌN BỘ,
   *  KHÔNG còn đọc hằng `DISH_LIBRARY` (seed) như trước 07/08/2026. */
  dishLibrary: Pick<Dish, "id" | "name">[] = []
): CookSheet {
  // Chỉ duyệt thực đơn CHUNG cho "món chính"; thực đơn riêng (audience != "ALL")
  // vào mục `special` qua `variants` (projection), không đếm lại như suất chính.
  const live = menus.filter(
    (m) => menuAudienceOf(m) === "ALL" && isMenuLive(m) && m.menuDateIso === dateIso
  );
  const mainByDish = new Map<string, CookLine>();
  const special: CookLine[] = [];
  const grades = new Set<string>();

  for (const menu of live) {
    const block = (menu.blocksData ?? []).find((b) => b.mealType === mealType);
    if (!block) continue;
    grades.add(menu.grade);
    const servings = menu.servings ?? 0;

    // Các em ăn cả một thực đơn chế độ ăn riêng thì không ăn món chính nào của bữa.
    const fullVariants = variants.filter(
      (v) => v.menuId === menu.id && v.mealType === mealType && v.mode === "FULL" && isVariantServable(v)
    );
    const fullVariantPortions = fullVariants.reduce((s, v) => s + (v.portions || 0), 0);

    for (const d of block.dishes) {
      const subs = d.substitutes ?? [];
      const subPortions = subs.reduce((s, x) => s + (Number(x.portions) || 0), 0);
      const mainPortions = Math.max(0, servings - fullVariantPortions - subPortions);
      if (mainPortions > 0) {
        const line = mainByDish.get(d.name) ?? {
          dish: d.name,
          portions: 0,
          kind: "MAIN" as const,
          sources: [] as string[],
        };
        line.portions += mainPortions;
        line.sources.push(`${menu.grade} (${mainPortions})`);
        mainByDish.set(d.name, line);
      }
      // Suất thay thế dị ứng — mỗi cái một nồi nhỏ riêng.
      for (const sub of subs) {
        const p = Number(sub.portions) || 0;
        if (p <= 0) continue;
        special.push({
          dish: `${sub.name} (thay ${d.name})`,
          portions: p,
          kind: "ALLERGY_SUB",
          sources: [menu.grade],
          note: sub.note,
        });
      }
    }

    // Thực đơn riêng theo chế độ ăn — cả bữa nấu riêng.
    for (const v of fullVariants) {
      const group = dietGroups.find((g) => g.id === v.dietGroupId);
      const dishes = v.dishes
        .map((x) => dishLibrary.find((y) => y.id === x.dishId)?.name ?? x.dishId)
        .join(", ");
      special.push({
        dish: `Suất ${group?.label ?? v.dietGroupId}: ${dishes}`,
        portions: v.portions || 0,
        kind: "DIET_VARIANT",
        sources: [menu.grade],
        note: v.note,
      });
    }
  }

  const main = [...mainByDish.values()].sort((a, b) => b.portions - a.portions);
  return {
    dateIso,
    mealType,
    main,
    special,
    totalMain: main.reduce((s, l) => s + l.portions, 0),
    totalSpecial: special.reduce((s, l) => s + l.portions, 0),
    grades: [...grades].sort(),
  };
}

// ============================================================================
// Truy xuất nguồn gốc: thực đơn → món → nguyên liệu → lô  (BR-075/076/077/205)
// ============================================================================
//
// Chuỗi liên kết được SINH RA ở bước xuất kho, không khai báo tay:
//
//   Thực đơn (BOM: món → nguyên liệu mỗi suất)
//        × số suất  ─────────────────────────►  nhu cầu theo nguyên liệu
//                                                      │  allocateFEFO()
//                                                      ▼
//   Phiếu xuất kho { batchId, ingredientId, menuId, mealType, dishName }
//        │                                    ← ĐÂY là nơi món ↔ lô ra đời
//        ├──► Sổ chế biến (batchesUsed prefill)
//        └──► Bản đồ truy xuất khi có sự cố (dishBatchMap / batchDishMap)
//
// Xuất kho là thời điểm duy nhất một lô vật lý bị cam kết và bị trừ tồn, nên đây
// là điểm bind đúng: sớm hơn (lúc duyệt thực đơn) thì lô chưa nhập kho, muộn hơn
// (chỉ ghi ở sổ chế biến) thì liên kết là hồi tố và phụ thuộc trí nhớ đầu bếp.


/**
 * Bảng hệ số DỰ PHÒNG — chỉ dùng khi danh mục thật từ server chưa về.
 *
 * ⚠️ ĐỪNG THÊM ĐƠN VỊ MỚI VÀO ĐÂY. Nguồn sự thật là bảng `measure_units` ở DB, đọc qua
 * `GET /measure-units` (xem `lib/measure-units.ts`). Bảng này giữ lại vì `toBaseQuantity()` là
 * hàm THUẦN, được gọi từ trong chính `mock-data.ts` (3 chỗ) lẫn từ component (3 chỗ) nên không
 * gọi hook được; nó chỉ phục vụ nhịp render đầu tiên trước khi danh mục tải xong.
 */
const UNIT_FACTOR_FALLBACK: Record<string, { base: string; factor: number }> = {
  g: { base: "kg", factor: 0.001 },
  kg: { base: "kg", factor: 1 },
  ml: { base: "lít", factor: 0.001 },
  "lít": { base: "lít", factor: 1 },
};

/**
 * Bảng hệ số THẬT, nạp một lần từ `GET /measure-units` (xem `setUnitFactors`).
 *
 * Vì sao dùng biến cấp module thay vì truyền tham số xuống: `toBaseQuantity()` nằm ở đáy ba
 * chuỗi hàm thuần (`step2RowsOfMeal`, `dishCostPerServing`, `mealIngredientNeeds`…), thêm
 * tham số phải sửa xuyên suốt cả chuỗi lẫn mọi nơi gọi. Đây là danh mục THAM CHIẾU chỉ-đọc,
 * nạp một lần mỗi phiên và chỉ làm kết quả ĐÚNG HƠN, nên đánh đổi này chấp nhận được.
 *
 * Rủi ro nó bịt: DB có 13 đơn vị, bảng dự phòng có 4. Hôm nay trùng nhau về mặt kết quả vì 9
 * đơn vị còn lại đều là ĐẾM (không quy đổi). Nhưng thêm MỘT đơn vị khối lượng/thể tích mới
 * (VD "tấn") vào `measure_units` là FE tính sai IM LẶNG — đây là lớp lỗi "hằng số nhân đôi"
 * đã vấp 4 lần trong dự án.
 */
let serverUnitFactors: Record<string, { base: string; factor: number }> | null = null;

/** Gọi từ `useMeasureUnits()` khi danh mục đơn vị thật về. Chỉ ghi, không đọc ngược. */
export function setUnitFactors(table: Record<string, { base: string; factor: number }>): void {
  serverUnitFactors = table;
}

export function toBaseQuantity(quantity: number, unit: string): { qty: number; unit: string } {
  const conv = (serverUnitFactors ?? UNIT_FACTOR_FALLBACK)[unit];
  return conv ? { qty: quantity * conv.factor, unit: conv.base } : { qty: quantity, unit };
}

/** Phần số của chuỗi tồn kho: "45 kg" → 45; "1.200 quả" → 1200; "-7.6 kg" → 7.6. */
export function parseQuantity(v: string): number {
  const cleaned = v.replace(/[^\d,.]/g, "");
  // "1.200" là dấu nghìn kiểu VN nhưng "7.6" là thập phân → chỉ coi dấu chấm là
  // dấu nghìn khi theo sau đúng 3 chữ số và chuỗi không có dấu phẩy thập phân.
  const normalized = cleaned.includes(",")
    ? cleaned.replaceAll(".", "").replace(",", ".")
    : cleaned.replace(/\.(?=\d{3}\b)/g, "");
  return Number(normalized) || 0;
}

/**
 * Số do MÁY sinh (`${delta} ${unit}` từ DTO) — dấu chấm LUÔN là thập phân, KHÔNG bao giờ là
 * dấu nghìn, vì đó là cách `Number.prototype.toString()` của JS in ra.
 *
 * <p>13/08/2026 (Nam) — tách khỏi {@link parseQuantity} sau khi phát hiện sổ kiểm thực ba bước
 * (Mẫu số 1, QĐ 1246/QĐ-BYT) in khối lượng SAI GẤP 1000 LẦN: cột `delta` là `NUMERIC(12,3)` nên
 * lượng xuất kho thật 9.235 kg bị `parseQuantity` hiểu "9.235" là chín nghìn hai trăm ba lăm và
 * in ra "9235 kg". Chỉ số có ĐÚNG 3 chữ số thập phân dính (khớp mẫu dấu nghìn), nên 2.7 và
 * 12.84 vẫn đúng — kiểu sai lặng lẽ, nhìn bảng không ngờ.
 *
 * <p>{@link parseQuantity} viết cho chuỗi NGƯỜI GÕ (nơi "1.200 quả" đúng là 1200) — giữ nguyên
 * cho những chỗ đó; đừng dùng nó cho giá trị lấy thẳng từ API.
 *
 * <p>Trả trị TUYỆT ĐỐI: giao dịch xuất kho có `delta` âm, nhưng cột "Khối lượng" của biểu mẫu
 * ghi lượng đã xuất (số dương).
 */
export function parseMachineQuantity(v: string): number {
  return Math.abs(Number(v.replace(/[^\d.-]/g, ""))) || 0;
}

/** Đơn vị của chuỗi tồn kho: "45 kg" → "kg". */
export function parseUnit(v: string): string {
  return v.replace(/^[\d.,\s+-]+/, "").trim();
}

/** "dd/mm/yyyy" → timestamp, để sắp FEFO. */
export function parseVnDate(v: string): number {
  const [d, m, y] = v.split("/").map(Number);
  return new Date(y, m - 1, d).getTime();
}

/**
 * "30/06/2026 14:20" → timestamp. `parseVnDate` bỏ phần giờ nên nhiều sự kiện
 * trong cùng một ngày sẽ sắp xếp không ổn định — sổ sự kiện cần đúng thứ tự phút.
 */
export function parseVnDateTime(v: string): number {
  const [datePart, timePart] = v.trim().split(" ");
  const [d, m, y] = datePart.split("/").map(Number);
  const [hh, mm] = (timePart ?? "0:0").split(":").map(Number);
  return new Date(y, m - 1, d, hh || 0, mm || 0).getTime();
}

/** Lô bị khóa, không được xuất/dùng: hết hạn (BR-205) hoặc đang cách ly (BR-149). */
export function isBatchLocked(batch: InventoryBatch, today = new Date()): boolean {
  // Hết hạn theo NGÀY CHẠY, không chỉ theo cột `status` tĩnh. Seed neo quanh
  // 06–07/2026 nên cột `status` đóng băng ở thời điểm đó; lô hạn 04/07 vẫn ghi
  // ACTIVE. Nếu chỉ đọc cột này, FEFO sẽ phân bổ lô đã quá hạn khi chạy vào ngày
  // khác — đúng thứ kịch bản "hôm nay" làm lộ ra. Suy từ `expiryDate` là nguồn thật.
  return (
    batch.status === "EXPIRED" ||
    batchShelfStatus(batch.expiryDate, today) === "EXPIRED" ||
    batch.quarantine != null
  );
}

/* ===== Dựng dòng biểu mẫu kiểm thực TỪ DỮ LIỆU THẬT (22/07/2026) =====
 *
 * Trước đây 3 mảng `STEP1_FRESH_CATALOG` / `STEP2_DISH_CATALOG` / `STEP3_DISH_CATALOG`
 * đóng vai "gợi ý điền nhanh", nhưng đó là dữ liệu KHAI TAY trùng với thứ hệ thống
 * đã có — và mâu thuẫn với chính nó: catalog ghi "Cơm trắng — Gạo tám thơm 150kg /
 * 1850 suất" trong khi BOM cho 80g × 1850 = 148kg; "Thịt kho trứng cút — thịt lợn
 * 45kg" trong khi BOM cho 92,5kg. Chọn từ catalog là ghi số bịa vào biểu mẫu pháp
 * lý (QĐ 1246).
 *
 * Nay dựng thẳng từ nguồn:
 *   Mẫu số 1 (trước chế biến)  ← lô đã XUẤT KHO cho đúng ngày+bữa đang kiểm
 *   Mẫu số 2/3 (chế biến / trước khi ăn) ← thực đơn của bữa + BOM
 * Cột thuộc về đánh giá tại chỗ (cảm quan, xét nghiệm nhanh, giờ sơ chế/chế biến,
 * biện pháp xử lý) vẫn để trống cho người kiểm thực điền — hệ thống không đoán hộ.
 */

/**
 * Dòng Mẫu số 1 dựng từ các lô ĐÃ XUẤT KHO cho đúng ngày+bữa đang kiểm; tách
 * tươi sống / khô theo khu lưu.
 *
 * Nhận xét Minh 26/07: trước đây lọc theo `batch.receivedDate === dateVn`
 * ("lô nhập TRONG NGÀY") — sai với thực tế vận hành, vì nguyên liệu (đặc biệt
 * đồ khô/gia vị) có thể nhập kho từ nhiều ngày trước, chỉ XUẤT KHO đúng ngày
 * nấu. Luồng thật là: Thực đơn duyệt → Xuất kho (sinh `InventoryTransaction`
 * với `serviceDate`/`mealType`/`batchId`) → Kiểm thực Bước 1 phải soi đúng
 * NHỮNG LÔ VỪA XUẤT cho bữa đó, không phải lô nhập kho hôm đó (có thể rỗng nếu
 * hôm đó bếp không nhập gì mới, dù vẫn xuất kho tồn cũ để nấu).
 */
export function step1RowsOfIssue(
  dateVn: string,
  mealType: MealType | undefined,
  transactions: InventoryTransaction[],
  batches: InventoryBatch[],
  /** `GET /api/v1/supplier-batches` — chỉ cần `id`+`certificates` (certificateIds). */
  supplierBatches: Pick<SupplierBatch, "id" | "certificates">[] = [],
  /** `GET /api/v1/deliveries` — chỉ cần `linkedBatches`+`driver` (tên đã tra qua `delivery-persons`). */
  deliveries: Pick<Delivery, "driver" | "linkedBatches">[] = [],
  /**
   * `GET /api/v1/supplier-documents?ids=<CSV mã>` — tra `documentType`/`name` của các mã
   * trong `SupplierBatchResponse.certificateIds` để phân loại cột (9)/(10) (giấy VS thú
   * y/kiểm dịch). Filter `ids` thêm 07/08/2026 riêng cho đường này (trước đó không có API
   * GET nào tra chứng từ theo mã, 2 cột luôn "chưa có"). `id` của `DocumentResponse` CHÍNH
   * LÀ mã chứng từ (`partner_documents.code`), khớp thẳng phần tử `certificates`.
   */
  documents: Pick<import("@/lib/api/types").DocumentDto, "id" | "documentType" | "name">[] = [],
  /**
   * `GET /api/v1/partners` — tra TÊN NCC theo tên lô cho cột producer (phiên 5, thay
   * `PARTNERS.find(...)` mock cũ luôn 0 khớp trên id/tên lô thật). `address`/`contact` cho
   * cột (6) "địa chỉ, điện thoại" đã có SẴN trên `PartnerResponse` từ 07/08/2026 (trước đó
   * chỉ `PartnerDetailResponse` mới có, cột (6) luôn trống) — KHÔNG cần gọi detail riêng
   * từng NCC (N+1).
   */
  partners: { name?: string; address?: string; contact?: string }[] = []
): { fresh: Step1FreshRow[]; dry: Step1DryRow[] } {
  const fresh: Step1FreshRow[] = [];
  const dry: Step1DryRow[] = [];

  // 13/08/2026 — điều chỉnh thủ công EXTRA_USAGE (BR-204, VD xuất bù sau kiểm thực Bước 1
  // false cần nguyên liệu mới) mang serviceDate/mealType riêng thì được gộp CHUNG với xuất
  // kho FEFO ở đây, nếu không phiếu kiểm thực Bước 1 sẽ không link được vào lô vừa bù.
  const issuedTx = transactions.filter(
    (t) =>
      (t.type === "Xuất kho (FEFO)" || (t.type === "Điều chỉnh" && t.reasonCode === "EXTRA_USAGE")) &&
      t.serviceDate === dateVn &&
      (!mealType || t.mealType === mealType)
  );
  // (6) Khối lượng của Mẫu số 1 = số THỰC XUẤT cho ĐÚNG bữa này, KHÔNG phải tổng
  // khối lượng còn lại/đã nhập của cả lô (`b.quantity`) — một lô thường dùng dần
  // qua nhiều bữa/nhiều ngày (FEFO), lấy nguyên `b.quantity` sẽ khai khống số liệu
  // (phát hiện 06/08/2026: lô Gạo tẻ chỉ xuất 0.1kg cho bữa này nhưng cột Khối
  // lượng lại hiện 70.1kg — bằng đúng số lượng còn tồn/đã nhập của cả lô).
  const issuedQtyByBatch = new Map<string, number>();
  const unitByBatch = new Map<string, string>();
  for (const t of issuedTx) {
    const unit = t.quantity.trim().split(/\s+/).slice(1).join(" ") || "kg";
    // parseMachineQuantity, KHÔNG parseQuantity: `t.quantity` ở đây do chính FE dựng từ
    // `${delta} ${unit}` của DTO (xem chỗ gọi trong inspections/[id]), nên dấu chấm luôn là
    // thập phân. Dùng nhầm bản "người gõ" làm 9.235 kg thành 9235 kg trên sổ pháp lý.
    issuedQtyByBatch.set(t.batchId, (issuedQtyByBatch.get(t.batchId) ?? 0) + parseMachineQuantity(t.quantity));
    unitByBatch.set(t.batchId, unit);
  }
  const issuedBatchIds = new Set(issuedTx.map((t) => t.batchId));

  /*
   * 14/08/2026 (Nam) — lô đã bị CÁCH LY (VD do chính Bước 1 fail cần nguyên liệu mới, người
   * dùng khoá lô hỏng qua BR-205) không còn được coi là bằng chứng hợp lệ cho BẤT KỲ phiên
   * kiểm thực nào (kể cả phiên hiện tại lẫn phiên tái kiểm tra mở sau đó) — vẫn đã THỰC XUẤT
   * đúng ngày/bữa nên khớp filter phía trên, nhưng đưa một lô đã biết là có vấn đề vào biên
   * bản đang chờ ký thì không đúng nữa. Khác lô HẾT HẠN: hết hạn không loại ở đây vì có thể
   * hết hạn SAU thời điểm xuất (đang xét việc đã xảy ra, không phải trạng thái hiện tại).
   */
  for (const b of batches.filter((x) => issuedBatchIds.has(x.id) && x.quarantine == null)) {
    const issuedQuantity = `${issuedQtyByBatch.get(b.id) ?? 0} ${unitByBatch.get(b.id) ?? "kg"}`;
    // `name` optional trên `PartnerResponse` — đối tác thiếu tên thì không so khớp được, bỏ
    // qua (trước đây `Partner.name` khai required nên chỗ này ngầm định luôn có).
    const partner = partners.find(
      (p) => !!p.name && (p.name.includes(b.supplier) || b.supplier.includes(p.name))
    );
    const supplierBatch = supplierBatches.find((sb) => sb.id === b.id);
    const delivery = deliveries.find((d) => (d.linkedBatches ?? []).includes(b.id));
    const certs = supplierBatch?.certificates ?? [];
    /*
     * Cột (9) giấy ĐK VS thú y và (10) giấy kiểm dịch. ĐỪNG sửa regex bên dưới để "chữa" việc
     * cột (9) không bao giờ bật — không phải lỗi regex, mà 2 cột này KHÁC CẤP dữ liệu (đối
     * chiếu QĐ 1246, `00_Policies/1246_QD-BYT_...md`, Mẫu số 1 Mục I + Điều 3.1.a):
     *
     *   (10) "Giấy kiểm dịch" = giấy chứng nhận kiểm dịch động vật, cấp theo TỪNG LÔ/chuyến —
     *        đúng là chứng từ LÔ ⇒ tra qua `certificateIds` như dưới đây. Đây cũng là giấy DUY
     *        NHẤT mà phần quy định (Điều 3.1.a) nêu đích danh cho thực phẩm tươi sống/đông lạnh.
     *   (9)  "Giấy ĐK VS thú y" = giấy đăng ký/chứng nhận điều kiện vệ sinh thú y, cấp cho CƠ SỞ
     *        (lò mổ, cơ sở sơ chế của NCC) theo thời hạn, dùng chung cho mọi lô ⇒ là hồ sơ CẤP
     *        CÔNG TY, cùng loại với ĐKKD/ATTP — KHÔNG phải chứng từ đính theo lô. Cột (9) chỉ
     *        xuất hiện ở Mẫu số 1 (biểu mẫu), không nằm trong phần quy định của Điều 3.
     *
     * ⇒ Nối cột (9) đúng cách là tra hồ sơ ĐỐI TÁC của NCC, không phải `certificateIds`. Hai
     * việc BE cần trước khi làm được (KHÔNG tự bịa): (a) `DocumentType` (D6, `mf04/domain`)
     * chưa có giá trị nào cho vệ sinh thú y; (b) `GET /supplier-documents` scope
     * SERVICE_CATERERS về ĐÚNG partnerId của chính nhà thầu, nên bếp KHÔNG đọc được hồ sơ cấp
     * công ty của NCC nguyên liệu — nhánh `ids` (thêm 08/2026) cố ý chỉ mở cho chứng từ LÔ.
     *
     * Trong lúc chờ: cột (9) để người kiểm TỰ TÍCH tay trên màn nhập (`CheckField` "Giấy ĐK VS
     * thú y (9)") — vẫn đúng quy định, vì QĐ 1246 yêu cầu GHI CHÉP kết quả kiểm tra chứ không
     * yêu cầu hệ thống tự suy. Lưu ý dòng rau/củ/quả tươi vốn KHÔNG có giấy thú y lẫn kiểm
     * dịch động vật — để trống ở các dòng đó là ĐÚNG, không phải thiếu dữ liệu.
     */
    const docNames = certs
      .map((c) => {
        const doc = documents.find((d) => d.id === c);
        return doc ? `${doc.documentType} ${doc.name}` : "";
      })
      .join(" ");
    // Cột (6) "địa chỉ, điện thoại" — 2 field này OPTIONAL trên `PartnerResponse` thật
    // (`usePagedList<Partner>` chỉ cast type, không đảm bảo có giá trị), nối chuỗi thẳng sẽ
    // ra "undefined — undefined" khi NCC chưa khai. Chỉ ghép phần thực sự có.
    const contact = [partner?.address, partner?.contact].filter(Boolean).join(" — ");
    // BR-073 — giờ nhận hàng THẬT (ghi lúc `ReceivingSessionService.create()`), thêm 10/08/2026.
    // Trước đây chỉ có `b.receivedDate` (LocalDate, không giờ) nên Bước 1 buộc người kiểm phải
    // tự chọn giờ dù hệ thống đã biết — có `receivedAtIso` thì dùng luôn, khoá ô giờ lại.
    const receivedAtReal = b.receivedAtIso ? formatVnDateTime(b.receivedAtIso) : undefined;

    if (b.storageZone === "Dry") {
      dry.push({
        name: b.ingredient,
        batchId: b.id,
        producer: partner?.name ?? b.supplier,
        producerAddress: partner?.address ?? "",
        receivedAt: receivedAtReal ?? b.receivedDate,
        receivedAtIsReal: Boolean(receivedAtReal),
        quantity: issuedQuantity,
        supplierName: partner?.name ?? b.supplier,
        deliverer: delivery?.driver ?? "",
        supplierContact: contact,
        expiry: b.expiryDate,
        storage: "Thường",
        // BUG tìm thấy 10/08/2026 — trước đây `certs.length > 0` (CÓ certificateIds bất kỳ,
        // không phân biệt loại giấy) bị dùng làm proxy cho "có chứng từ, hóa đơn", nên lô nào
        // gắn certificateIds (thường là giấy ATTP/kiểm dịch, KHÔNG phải hóa đơn) là tự tick
        // Đạt dù chưa ai xác nhận có hóa đơn thật. Cột này không có nguồn dữ liệu thật (hệ
        // thống không lưu "có hóa đơn hay không" ở đâu) — để trống, người kiểm tự tick khi thấy
        // hóa đơn thật tại chỗ (cùng tinh thần cột (9) bên dưới).
        hasInvoice: false,
        // Cảm quan là đánh giá TẠI CHỖ — để Đạt, người kiểm tự hạ khi thấy sai
        sensoryPass: true,
        action: "",
      });
    } else {
      fresh.push({
        name: b.ingredient,
        batchId: b.id,
        receivedAt: receivedAtReal ?? b.receivedDate,
        receivedAtIsReal: Boolean(receivedAtReal),
        quantity: issuedQuantity,
        supplierName: partner?.name ?? b.supplier,
        supplierContact: contact,
        deliverer: delivery?.driver ?? "",
        // BUG tìm thấy 10/08/2026 — cùng lỗi proxy sai như nhánh Dry ở trên: "có
        // certificateIds bất kỳ" KHÔNG phải "có hóa đơn". Để trống, người kiểm tự tick.
        hasInvoice: false,
        // Cột (9) — đúng như đoạn giải thích phía trên: KHÔNG có nguồn dữ liệu thật (hồ sơ
        // VS thú y là giấy CẤP CÔNG TY của NCC, bếp không đọc được qua certificateIds của
        // LÔ) — code trước đây vẫn lỡ suy qua regex trên `docNames` dù chính comment ngay
        // trên đã nói rõ phải để người kiểm tự tick tay. Sửa cho khớp với ý đã ghi.
        hasVetCert: false,
        hasQuarantineCert: /kiểm dịch|quarantine|PRODUCT_ORIGIN/i.test(docNames),
        sensoryPass: true,
        quickTest: "NONE",
        action: "",
      });
    }
  }
  return { fresh, dry };
}

/**
 * Dòng Mẫu số 2 dựng từ thực đơn của bữa: mỗi MÓN một dòng, nguyên liệu chính và
 * số suất cộng gộp mọi khối ăn bữa đó (bếp nấu chung một mẻ).
 */
export function step2RowsOfMeal(
  menus: SeedMenu[],
  mealType: MealType,
  servingsOf: (menu: SeedMenu) => number
): Step2Row[] {
  // Gom theo MÃ MÓN, không theo tên: hai món khác nhau có thể trùng tên hiển thị, và mã
  // mới là thứ bước lưu mẫu cần (`RegisterSampleRequest.dishId`).
  const byDish = new Map<
    string,
    { dishId: string; dishName: string; servings: number; ing: Map<string, { qty: number; unit: string }> }
  >();

  for (const menu of menus) {
    const block = menu.blocksData?.find((b) => b.mealType === mealType);
    if (!block) continue;
    const servings = servingsOf(menu);
    for (const d of block.dishes) {
      const key = d.dishId || d.name;
      const entry = byDish.get(key) ?? { dishId: d.dishId ?? "", dishName: d.name, servings: 0, ing: new Map() };
      entry.servings += servings;
      for (const ing of d.ingredients) {
        const base = toBaseQuantity(Number(ing.quantity) || 0, ing.unit);
        const cur = entry.ing.get(ing.name) ?? { qty: 0, unit: base.unit };
        cur.qty = Number((cur.qty + base.qty * servings).toFixed(2));
        entry.ing.set(ing.name, cur);
      }
      byDish.set(key, entry);
    }
  }

  return [...byDish.values()].map((v) => ({
    shift: MEAL_TYPE_LABEL[mealType],
    dishId: v.dishId,
    dishName: v.dishName,
    // Định lượng nổ từ BOM — không còn con số khai tay lệch với thực đơn
    mainIngredients: [...v.ing.entries()]
      .map(([name, q]) => `${name} ${q.qty}${q.unit}`)
      .join("; "),
    servings: String(v.servings),
    prepDoneAt: "",
    cookDoneAt: "",
    hygieneStaff: true,
    hygieneEquipment: true,
    hygieneArea: true,
    sensoryPass: true,
    action: "",
  }));
}

/** Dòng Mẫu số 3 — cùng danh sách món và số suất, bỏ phần nguyên liệu. */
export function step3RowsOfMeal(
  menus: SeedMenu[],
  mealType: MealType,
  servingsOf: (menu: SeedMenu) => number
): Step3Row[] {
  return step2RowsOfMeal(menus, mealType, servingsOf).map((r) => ({
    shift: r.shift,
    dishId: r.dishId,
    dishName: r.dishName,
    servings: r.servings,
    dividedDoneAt: "",
    eatStartAt: "",
    utensilsPass: true,
    utensils: "",
    sensoryPass: true,
    action: "",
  }));
}

/* ===== Nối PHIÊN KIỂM THỰC ↔ THỰC ĐƠN (22/07/2026) =====
 *
 * Kiểm thực ba bước làm MỘT phiên cho cả trường mỗi bữa — đúng mẫu QĐ 1246 (Mẫu số
 * 1/2/3 ghi theo "ca/bữa ăn", `Step2Row.servings` là số suất toàn trường). Nên phiên
 * kiểm thực KHÔNG có, và không cần có, khoá tới từng thực đơn khối.
 *
 * Cái thiếu là đường nối: hai bên mã hoá cùng hai chiều (ngày, bữa) theo hai kiểu
 *   InspectionSession : date "02/07/2026"      · meal "Bữa trưa"
 *   PersistedMenu     : menuDateIso "2026-07-02" · mealType "LUNCH"
 * nên muốn trả lời "Bước 2 không đạt thì ảnh hưởng khối nào, lô nào" phải quy đổi
 * thủ công ở từng chỗ dùng. Gom về đây để chỉ quy đổi một lần.
 */

/** "02/07/2026" → "2026-07-02". Trả chuỗi rỗng nếu không đúng dạng. */
export function isoOfVnDate(d: string): string {
  const m = d.match(/^(\d{2})\/(\d{2})\/(\d{4})/);
  return m ? `${m[3]}-${m[2]}-${m[1]}` : "";
}

/**
 * Date → "dd/MM/yyyy" CÓ đệm số 0.
 *
 * `toLocaleDateString("vi-VN")` trả "24/7/2026" — không đệm — trong khi mọi ngày
 * trong seed và `isoOfVnDate` đều đòi hai chữ số. Bản ghi tạo bằng ngày không đệm
 * trông vẫn đúng trên màn hình nhưng không khớp được với lô nhập kho hay thực đơn
 * nào, nên mọi dropdown tự điền của nó đều rỗng.
 */
export function vnOfDate(d: Date = new Date()): string {
  const p = (x: number) => String(x).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

/** "Bữa trưa" → "LUNCH" — ánh xạ ngược của MEAL_TYPE_LABEL. */
export function mealTypeOfLabel(label: string): MealType | undefined {
  return (Object.keys(MEAL_TYPE_LABEL) as MealType[]).find(
    (k) => MEAL_TYPE_LABEL[k] === label
  );
}

/**
 * Các thực đơn KHỐI mà một phiên kiểm thực đang kiểm — tức mọi thực đơn phục vụ
 * cùng ngày, cùng bữa. Đây là mắt nối để đi tiếp tới phiếu xuất và lô nguyên liệu.
 */
export function menusOfInspection(
  session: Pick<InspectionSession, "date" | "meal">,
  menus: SeedMenu[] = []
): SeedMenu[] {
  const iso = isoOfVnDate(session.date);
  const mealType = mealTypeOfLabel(session.meal);
  if (!iso || !mealType) return [];
  return menus.filter(
    (m) => m.menuDateIso === iso && (m.blocksData ?? []).some((b) => b.mealType === mealType)
  );
}

/**
 * Phiên kiểm thực của một bữa cụ thể (chiều ngược lại).
 *
 * 13/08/2026 (Nam) — từ khi có nút "Tái kiểm tra" (BR-096, `POST /reinspect`), MỘT bữa có
 * thể tồn tại NHIỀU phiên: phiên gốc FAILED (bất biến, giữ nguyên) + phiên tái kiểm mới hơn.
 * `GET /inspections` không trả `createdAt` và server sort `Sort.unsorted()` (không đảm bảo
 * thứ tự) — trước đây hàm này lấy `.find()` (bản KHỚP ĐẦU TIÊN trong mảng), có thể trúng
 * đúng phiên FAILED cũ thay vì phiên đang hoạt động, khiến nút "Mở phiên kiểm thực" ở trang
 * danh sách đưa nhầm người dùng vào phiên đã chốt thay vì phiên mới đang làm dở.
 *
 * Sinh mã phiên (`generateSessionCode`, backend) luôn tăng dần hậu tố `-2`, `-3`... khi trùng
 * mã trong cùng ngày/buổi — dùng chính hậu tố đó để suy ra phiên MỚI NHẤT, không cần
 * `createdAt`. Không ảnh hưởng cổng phục vụ thật (`checkGate()` phía server tự
 * `ORDER BY created_at DESC` riêng, không qua hàm này) — đây chỉ sửa phần hiển thị/điều
 * hướng phía client.
 */
function sessionSuffix(id: string): number {
  const m = id.match(/-(\d+)$/);
  return m ? Number(m[1]) : 1;
}

export function inspectionForMeal(
  dateIso: string,
  mealType: MealType,
  sessions: InspectionSession[] = []
): InspectionSession | undefined {
  const matches = sessions.filter(
    (s) => isoOfVnDate(s.date) === dateIso && mealTypeOfLabel(s.meal) === mealType
  );
  if (matches.length <= 1) return matches[0];
  return matches.reduce((latest, s) => (sessionSuffix(s.id) > sessionSuffix(latest.id) ? s : latest));
}

/**
 * BR-094: "Bữa ăn không được phép phục vụ nếu bất kỳ bước nào trong ba bước kiểm
 * tra thất bại."
 *
 * Lưu ý cách đọc: quy định chặn khi có bước THẤT BẠI, không chặn khi CHƯA có phiên
 * kiểm thực — hai trạng thái đó khác nhau, nên trả về riêng để màn hình chặn một
 * đằng và chỉ cảnh báo một nẻo.
 */
export function mealServingGate(
  dateIso: string,
  mealType: MealType,
  sessions: InspectionSession[] = []
): { session?: InspectionSession; blocked: boolean; failedSteps: string[]; noSession: boolean } {
  const session = inspectionForMeal(dateIso, mealType, sessions);
  if (!session) return { blocked: false, failedSteps: [], noSession: true };
  const failedSteps = (
    [
      ["step1", "Bước 1 — trước chế biến"],
      ["step2", "Bước 2 — trong chế biến"],
      ["step3", "Bước 3 — trước khi ăn"],
    ] as const
  )
    .filter(([k]) => session[k] === "FAILED")
    .map(([, label]) => label);
  return {
    session,
    blocked: failedSteps.length > 0 || session.overall === "FAILED",
    failedSteps,
    noSession: false,
  };
}




/**
 * Kế hoạch xuất kho cho MỘT BỮA của cả trường.
 *
 * @param menus  mọi thực đơn (khối) phục vụ bữa đó trong ngày
 * @param servingsOf  số suất thực tế theo từng thực đơn
 */

/** Phần thiếu của CẢ BỮA (mọi khối), gộp theo nguyên liệu — nguồn của đơn đặt hàng. */

/** Các lô đã xuất cho đúng một món của một buổi ăn — nguồn prefill sổ chế biến. */

/**
 * Các phiếu xuất đã ghi cho một buổi ăn — dùng để chặn xuất trùng. Xuất lại một
 * buổi đã xuất sẽ trừ tồn lần hai và làm sai cả tồn kho lẫn bản đồ truy xuất.
 *
 * KHÔNG còn tham số mặc định `= INVENTORY_TRANSACTIONS` (mảng seed đã xoá cùng
 * đợt dọn mock-server/db.ts) — `transactions` BẮT BUỘC là dữ liệu thật do nơi gọi
 * truyền vào (`service-caterers/inspections/[id]/page.tsx` đã đọc qua
 * `usePagedList<InventoryTransactionDto>("inventory-transactions")` từ trước).
 */
export function issuedLinesForMeal(
  menuId: string,
  mealType: MealType,
  transactions: InventoryTransaction[]
): InventoryTransaction[] {
  return transactions.filter(
    (t) => t.type === "Xuất kho (FEFO)" && t.menuId === menuId && t.mealType === mealType
  );
}

