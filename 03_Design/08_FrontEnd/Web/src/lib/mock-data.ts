/**
 * Mock data cho SFMS Web Frontend (chưa nối backend).
 * Dữ liệu bám theo Business Rules (02_Requirements/00_BusinessRules/business_rules.md)
 * và enum trạng thái trong class diagram (MenuStatus, InspectionStatus, BatchStatus...).
 * Khối dữ liệu Teacher (phân phát/ghi nhận bữa ăn, sức khỏe học sinh) ở cuối file.
 */

import type { RoleKey } from "@/lib/roles";
import type { BadgeVariant } from "@/components/ui/badge";

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
  // Incident
  OPEN: { label: "Đang mở", variant: "danger" },
  INVESTIGATING: { label: "Đang điều tra", variant: "warning" },
  CLOSED: { label: "Đã đóng", variant: "neutral" },
  // Supplier / Partnership (BR-041)
  ACTIVE: { label: "Hoạt động", variant: "success" },
  INACTIVE: { label: "Ngừng hoạt động", variant: "neutral" },
  SUSPENDED: { label: "Tạm đình chỉ", variant: "critical" },
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
  // Duyệt giá đơn mua hàng — giá theo hợp đồng, 1 vòng (review 25/07/2026).
  // PRICE_ACCEPTED xanh DƯƠNG chứ không xanh lá: đơn mới chốt giá, chưa xong
  // việc — xanh lá (success) dành cho COMPLETED. Nhãn theo vai đặt tại màn
  // (SC: "— chờ giao", Supplier: "— cần giao") qua prop label của StatusBadge.
  QUOTED: { label: "Đã gửi giá — chờ duyệt", variant: "info" },
  PRICE_ACCEPTED: { label: "Đã chốt giá", variant: "info" },
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

// ===== Người dùng hiện tại (mock session) =====

export const CURRENT_SCHOOL = {
  name: "Trường Tiểu học Đoàn Thị Điểm",
  /** Tên rút gọn dùng trong các bản ghi mock (PurchaseRequest.school, Delivery.school). */
  shortName: "TH Đoàn Thị Điểm",
  code: "SCH-HN-0042",
  address: "Mỹ Đình 2, Nam Từ Liêm, Hà Nội",
  mealsPerDay: 1850,
  geofenceRadius: 200, // BR-028 — xem SCHOOL_CONFIG, giá trị này chỉ là mặc định ban đầu
};

/**
 * Tham số vận hành cấp trường do Ban giám hiệu đặt (UC-095).
 *
 * Trước 22/07/2026 màn "Cấu hình trường" giữ ba tham số này trong `useState` rồi
 * `setTimeout` báo lưu thành công — không ghi đi đâu. Tệ hơn: hai trong ba tham
 * số không hề tới được nơi dùng. Trần chi phí bị SAO CHÉP vào từng thực đơn
 * (`Menu.costCeiling`, hardcode 30000 lúc tạo), nên Ban giám hiệu đổi trần thì
 * không thực đơn nào đổi theo; bán kính geofence thì nơi dùng đọc thẳng hằng số
 * `CURRENT_SCHOOL`. Màn cấu hình đẹp nhưng vô tác dụng.
 *
 * Backend: đây là bảng một dòng cho mỗi trường, không phải cột lặp trên thực đơn.
 */
export interface SchoolConfig {
  id: string;
  /** BR-080 — trần chi phí mỗi suất, áp cho thực đơn ĐANG SOẠN. */
  costCeiling: number;
  /** BR-028 — 50–500m. */
  geofenceRadius: number;
  /** BR-179 — nơi nhận báo cáo định kỳ. */
  reportEmails: string[];
  updatedAt?: string;
  updatedBy?: string;
}

export const SCHOOL_CONFIG: SchoolConfig[] = [
  {
    id: CURRENT_SCHOOL.code,
    costCeiling: 30000,
    geofenceRadius: 200,
    reportEmails: ["toanpq@dtd.edu.vn", "hant@dtd.edu.vn"],
    updatedAt: "01/07/2026 08:00",
    updatedBy: "Phạm Quốc Toản (BGH)",
  },
];

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

function ing(name: string, quantity: number, unit: string): SeedIngredientRow {
  // `ingredientId` nối sau (INGREDIENT_CATALOG khai sau MENUS trong file này) — xem
  // vòng backfill "Nối nguyên liệu thực đơn về danh mục" phía dưới.
  return { name, quantity: String(quantity), unit };
}

interface SeedSubstituteInput {
  name: string;
  kcal: number;
  portions: number;
  note: string;
  ingredients: SeedIngredientRow[];
}

/**
 * Một món CÓ THỂ cần nhiều suất riêng cùng lúc — món thịt vừa phải có suất chay,
 * vừa phải có suất Halal. Tham số nhận cả một suất lẻ lẫn mảng để không phải sửa
 * mọi lời gọi cũ.
 */
function dish(
  name: string,
  kcal: number,
  ingredients: SeedIngredientRow[],
  substitute?: SeedSubstituteInput | SeedSubstituteInput[]
): SeedDish {
  const subs = substitute == null ? [] : Array.isArray(substitute) ? substitute : [substitute];
  return {
    name,
    ingredients,
    manualAllergens: [],
    kcal: String(kcal),
    protein: "",
    carb: "",
    fat: "",
    isSpecialDish: subs.length > 0,
    substitutes: subs.map((sub) => ({
      name: sub.name,
      ingredients: sub.ingredients,
      manualAllergens: [],
      kcal: String(sub.kcal),
      portions: String(sub.portions),
      note: sub.note,
    })),
  };
}

/**
 * Ngày ISO của (hôm nay + n). Thực đơn "sắp phục vụ" phải neo vào ngày chạy chứ
 * không hardcode: cảnh báo sớm thiếu nguyên liệu (3 ngày tới) mà seed cố định thì
 * chỉ đúng đến hết tuần demo rồi im lặng mãi mãi.
 */
function isoInDays(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() + n);
  const p = (x: number) => String(x).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

const UPCOMING_ISO = isoInDays(2);
const UPCOMING_LABEL = (() => {
  const d = new Date(`${UPCOMING_ISO}T00:00:00`);
  const p = (x: number) => String(x).padStart(2, "0");
  const names = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
  return `${names[d.getDay()]} — ${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
})();

/** Ngày dd/mm/yyyy của (hôm nay + n) — cho các trường ngày kiểu VN trong seed. */
function vnInDays(n: number): string {
  const d = new Date();
  d.setDate(d.getDate() + n);
  const p = (x: number) => String(x).padStart(2, "0");
  return `${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
}

/**
 * KỊCH BẢN "HÔM NAY" — neo vào ngày chạy để chạy thử trọn luồng vận hành bếp
 * (xuất kho → kiểm thực → lưu mẫu → phát suất) mà không vướng BR-061 (thực đơn
 * phải tạo trước 3 ngày, nên không tự tạo cho hôm nay qua giao diện được).
 *
 * Cố tình là THỰC ĐƠN CHAY: mọi món gốc mọi nhóm chế độ ăn của Khối 3 đều ăn
 * được và không chạm dị ứng nào của lớp 3A, nên không cần suất riêng — kịch bản
 * sạch để test, không phải bịa cho đủ.
 */
const TODAY_ISO = isoInDays(0);
const TODAY_LABEL = (() => {
  const d = new Date();
  const p = (x: number) => String(x).padStart(2, "0");
  const names = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
  return `${names[d.getDay()]} — ${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;
})();

/**
 * Thực đơn kèm khối dữ liệu bữa — dạng các màn lập/duyệt thực đơn làm việc.
 * Có tên riêng để nơi khác lấy được KIỂU mà không phải import cả mảng seed.
 */
export type SeedMenu = MenuItem & { blocksData?: SeedMealBlock[]; servings?: number };

export const MENUS: SeedMenu[] = [
  {
    // Thực đơn đã duyệt của 2 ngày tới — nguồn của cảnh báo sớm thiếu nguyên liệu
    // (tôm sú đang cách ly, dầu ăn đã hết hạn nên FEFO không phân bổ được gì).
    id: "MENU-2026-30", week: UPCOMING_LABEL, name: `Thực đơn ${UPCOMING_LABEL} — Khối 3`, grade: "Khối 3", menuDateIso: UPCOMING_ISO, status: "PUBLISHED", dishes: 4, allergyConflicts: 0, estimatedCost: 28800, costCeiling: 30000, submittedAt: "18/07/2026", createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 380,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          // Chạm dị ứng Tôm của 2 HS Khối 3 → BẮT BUỘC có suất riêng, nếu không
          // BGH không ký được (BR-063). Thực đơn này ở trạng thái đã duyệt nên
          // suất riêng phải có sẵn, đúng như luồng thật.
          dish("Canh bí đỏ nấu tôm", 90, [ing("Bí đỏ", 60, "g"), ing("Tôm sú", 8, "g"), ing("Hành khô", 2, "g")], [
            {
            name: "Canh bí đỏ thịt băm", kcal: 85, portions: 2, note: "Nấu nồi riêng, không dùng chung muôi (BR-082)",
            ingredients: [ing("Bí đỏ", 60, "g"), ing("Thịt lợn nạc vai", 20, "g"), ing("Hành khô", 2, "g")],
            },
          ]),
          dish("Su su xào", 65, [ing("Su su", 70, "g"), ing("Tỏi", 3, "g"), ing("Dầu ăn thực vật", 5, "ml")]),
          dish("Gà rang gừng", 220, [ing("Thịt gà ta", 70, "g"), ing("Gừng", 3, "g"), ing("Nước mắm truyền thống", 4, "ml")]),
        ],
      },
    ],
  },
  {
    // ===== KỊCH BẢN HÔM NAY (neo ngày chạy) — Khối 3, thực đơn chay =====
    id: "MENU-TD-3", week: TODAY_LABEL, name: `Thực đơn ${TODAY_LABEL} — Khối 3`, grade: "Khối 3", menuDateIso: TODAY_ISO, status: "PUBLISHED", dishes: 6, allergyConflicts: 0, estimatedCost: 24200, costCeiling: 30000, submittedAt: vnInDays(-3), createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 380,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
          dish("Đậu phụ sốt cà chua", 195, [ing("Đậu phụ", 80, "g"), ing("Cà chua", 40, "g")]),
          dish("Canh bí đỏ chay", 70, [ing("Bí đỏ", 60, "g"), ing("Hành khô", 2, "g")]),
          dish("Su su xào", 70, [ing("Su su", 70, "g"), ing("Dầu ăn thực vật", 4, "ml"), ing("Tỏi", 2, "g")]),
        ],
      },
      {
        mealType: "SNACK",
        dishes: [
          dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")]),
          dish("Dưa hấu", 50, [ing("Dưa hấu", 100, "g")]),
        ],
      },
    ],
  },
  {
    id: "MENU-2026-27", week: "Thứ 2 — 06/07/2026", name: "Thực đơn Thứ 2 06/07 — Khối 3", grade: "Khối 3", menuDateIso: "2026-07-06", status: "PUBLISHED", dishes: 6, allergyConflicts: 0, estimatedCost: 28500, costCeiling: 30000, submittedAt: "01/07/2026", createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 380,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          dish("Thịt kho trứng cút", 225, [ing("Thịt lợn nạc vai", 60, "g"), ing("Trứng cút", 3, "quả"), ing("Nước mắm truyền thống", 4, "ml")], [
            {
              name: "Thịt kho tàu", kcal: 215, portions: 1,
              note: "Suất riêng cho HS dị ứng Trứng — kho cùng nồi nhưng KHÔNG có trứng cút",
              ingredients: [ing("Thịt lợn nạc vai", 70, "g"), ing("Nước mắm truyền thống", 4, "ml")],
            },
          ]),
          dish("Canh bí đỏ nấu tôm", 90, [ing("Bí đỏ", 60, "g"), ing("Tôm sú", 8, "g"), ing("Hành khô", 2, "g")], [
            {
              name: "Canh bí đỏ thịt băm", kcal: 85, portions: 2, note: "Nấu nồi riêng khu B, không dùng chung muôi với món gốc (BR-082)",
              ingredients: [ing("Bí đỏ", 60, "g"), ing("Thịt lợn nạc vai", 20, "g"), ing("Hành khô", 2, "g")],
            },
          ]),
          dish("Su su xào", 65, [ing("Su su", 70, "g"), ing("Tỏi", 3, "g"), ing("Dầu ăn thực vật", 5, "ml")]),
        ],
      },
      {
        mealType: "SNACK",
        dishes: [
          dish("Sữa chua", 100, [ing("Sữa chua", 1, "hộp")]),
          dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")]),
        ],
      },
    ],
  },
  {
    id: "MENU-2026-27B", week: "Thứ 3 — 07/07/2026", name: "Thực đơn Thứ 3 07/07 — Khối 3", grade: "Khối 3", menuDateIso: "2026-07-07", status: "PUBLISHED", dishes: 5, allergyConflicts: 0, estimatedCost: 27800, costCeiling: 30000, submittedAt: "01/07/2026", createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 380,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          dish("Cá basa sốt cà chua", 210, [ing("Cá basa phi lê", 70, "g"), ing("Cà chua", 40, "g"), ing("Dầu ăn thực vật", 5, "ml")]),
          dish("Canh rau ngót thịt băm", 80, [ing("Rau ngót", 50, "g"), ing("Thịt lợn nạc vai", 20, "g")], [
          ]),
          dish("Giá xào", 60, [ing("Giá đỗ", 60, "g"), ing("Dầu ăn thực vật", 5, "ml")]),
        ],
      },
      { mealType: "SNACK", dishes: [dish("Chè đậu xanh", 150, [ing("Đường trắng", 15, "g")])] },
    ],
  },
  {
    id: "MENU-2026-28", week: "Thứ 2 — 20/07/2026", name: "Thực đơn Thứ 2 20/07 — Khối 3", grade: "Khối 3", menuDateIso: "2026-07-20", status: "PENDING_APPROVAL", dishes: 6, allergyConflicts: 1, estimatedCost: 29200, costCeiling: 30000, submittedAt: "10/07/2026", createdBy: "Cty TNHH Suất ăn An Bình",
    servings: 380,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          // Còn 1 xung đột dị ứng chưa lập món thay thế (khớp allergyConflicts = 1)
          dish("Tôm rim thịt", 230, [ing("Tôm sú", 40, "g"), ing("Thịt lợn nạc vai", 30, "g")]),
          dish("Canh khoai tây cà rốt", 85, [ing("Khoai tây", 50, "g"), ing("Cà rốt", 30, "g")]),
          dish("Rau muống xào tỏi", 60, [ing("Rau muống", 70, "g"), ing("Tỏi", 3, "g")]),
        ],
      },
      {
        mealType: "SNACK",
        dishes: [
          dish("Bánh flan", 145, [ing("Trứng gà", 1, "quả"), ing("Sữa tươi tiệt trùng", 40, "ml")]),
          dish("Dưa hấu", 50, [ing("Dưa hấu", 100, "g")]),
        ],
      },
    ],
  },
  {
    id: "MENU-2026-28B", week: "Thứ 2 — 20/07/2026", name: "Thực đơn Thứ 2 20/07 — Khối 4", grade: "Khối 4", menuDateIso: "2026-07-20", status: "PENDING_APPROVAL", dishes: 5, allergyConflicts: 0, estimatedCost: 28900, costCeiling: 30000, submittedAt: "10/07/2026", createdBy: "Cty TNHH Suất ăn An Bình",
    servings: 395,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          dish("Thịt bò xào rau củ", 235, [ing("Thịt bò thăn", 60, "g"), ing("Su su", 40, "g"), ing("Cà rốt", 20, "g")]),
          dish("Canh cải ngọt", 70, [ing("Rau cải ngọt", 60, "g")]),
        ],
      },
      {
        mealType: "SNACK",
        dishes: [
          dish("Bánh mì phô mai", 180, [ing("Bánh mì sandwich", 1, "ổ")]),
          dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")]),
        ],
      },
    ],
  },
  /*
   * Hai thực đơn dưới đây bổ sung 22/07/2026. Trước đó Khối 1 chỉ có thực đơn
   * DRAFT và Khối 5 chỉ có thực đơn REJECTED, nên 4 học sinh của hai khối này
   * không có bữa nào đã công bố: mọi màn Phụ huynh của các em rỗng trắng và
   * không luồng nào (phát suất, xuất kho, kiểm thực) chạy hết được cho hai khối.
   * Cùng ngày 06/07 với Khối 3 để cả ba khối dùng chung một phiên kiểm thực.
   */
  {
    id: "MENU-2026-27C", week: "Thứ 2 — 06/07/2026", name: "Thực đơn Thứ 2 06/07 — Khối 1", grade: "Khối 1", menuDateIso: "2026-07-06", status: "PUBLISHED", dishes: 4, allergyConflicts: 0, estimatedCost: 26900, costCeiling: 30000, submittedAt: "01/07/2026", createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 350,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 180, [ing("Gạo tám thơm", 70, "g")]),
          dish("Thịt kho tàu", 215, [ing("Thịt lợn nạc vai", 70, "g"), ing("Nước mắm truyền thống", 4, "ml")]),
          dish("Canh mồng tơi", 65, [ing("Mồng tơi", 50, "g")]),
        ],
      },
      { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
    ],
  },
  {
    id: "MENU-2026-27D", week: "Thứ 2 — 06/07/2026", name: "Thực đơn Thứ 2 06/07 — Khối 5", grade: "Khối 5", menuDateIso: "2026-07-06", status: "PUBLISHED", dishes: 4, allergyConflicts: 0, estimatedCost: 29400, costCeiling: 30000, submittedAt: "01/07/2026", createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 300,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 210, [ing("Gạo tám thơm", 85, "g")]),
          dish("Gà rang gừng", 225, [ing("Thịt gà ta", 65, "g"), ing("Gừng", 3, "g")]),
          dish("Su su xào", 70, [ing("Su su", 60, "g")]),
        ],
      },
      { mealType: "SNACK", dishes: [dish("Sữa chua", 100, [ing("Sữa chua", 1, "hộp")])] },
    ],
  },
  {
    id: "MENU-2026-28C", week: "Thứ 3 — 21/07/2026", name: "Thực đơn Thứ 3 21/07 — Khối 1", grade: "Khối 1", menuDateIso: "2026-07-21", status: "DRAFT", dishes: 4, allergyConflicts: 0, estimatedCost: 26400, costCeiling: 30000, createdBy: "Cty TNHH Suất ăn An Bình",
    servings: 350,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 180, [ing("Gạo tám thơm", 70, "g")]),
          dish("Gà rang gừng", 215, [ing("Thịt gà ta", 60, "g"), ing("Gừng", 3, "g")]),
          dish("Canh mồng tơi", 65, [ing("Mồng tơi", 50, "g")]),
        ],
      },
      { mealType: "SNACK", dishes: [dish("Sữa chua", 100, [ing("Sữa chua", 1, "hộp")])] },
    ],
  },
  {
    id: "MENU-2026-29", week: "Thứ 3 — 21/07/2026", name: "Thực đơn Thứ 3 21/07 — Khối 3", grade: "Khối 3", menuDateIso: "2026-07-21", status: "DRAFT", dishes: 5, allergyConflicts: 0, estimatedCost: 27100, costCeiling: 30000, createdBy: "Cty TNHH Suất ăn An Bình",
    servings: 380,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          dish("Gà rang gừng", 215, [ing("Thịt gà ta", 60, "g"), ing("Gừng", 3, "g")]),
          dish("Canh chua cá", 95, [ing("Cá basa phi lê", 30, "g"), ing("Cà chua", 30, "g")]),
          dish("Bắp cải luộc", 55, [ing("Bắp cải", 70, "g")]),
        ],
      },
      { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
    ],
  },
  {
    id: "MENU-2026-26", week: "Thứ 6 — 10/07/2026", name: "Thực đơn Thứ 6 10/07 — Khối 4", grade: "Khối 4", menuDateIso: "2026-07-10", status: "PUBLISHED", dishes: 6, allergyConflicts: 0, estimatedCost: 28000, costCeiling: 30000, submittedAt: "06/07/2026", createdBy: "Cty TNHH Suất ăn An Bình", approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
    servings: 395,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          dish("Thịt kho trứng cút", 225, [ing("Thịt lợn nạc vai", 60, "g"), ing("Trứng cút", 3, "quả")], {
            name: "Thịt kho tàu", kcal: 215, portions: 1,
            note: "Suất riêng cho HS dị ứng Trứng — kho cùng nồi nhưng KHÔNG có trứng cút",
            ingredients: [ing("Thịt lợn nạc vai", 70, "g"), ing("Nước mắm truyền thống", 4, "ml")],
          }),
          dish("Canh khoai tây cà rốt", 85, [ing("Khoai tây", 50, "g"), ing("Cà rốt", 30, "g")]),
          dish("Rau muống xào tỏi", 60, [ing("Rau muống", 70, "g"), ing("Tỏi", 3, "g")]),
        ],
      },
      {
        mealType: "SNACK",
        dishes: [
          dish("Sữa đậu nành", 110, [ing("Sữa đậu nành", 180, "ml")]),
          dish("Dưa hấu", 50, [ing("Dưa hấu", 100, "g")]),
        ],
      },
    ],
  },
  {
    id: "MENU-2026-25", week: "Thứ 4 — 08/07/2026", name: "Thực đơn Thứ 4 08/07 — Khối 5", grade: "Khối 5", menuDateIso: "2026-07-08", status: "REJECTED", dishes: 6, allergyConflicts: 3, estimatedCost: 31500, costCeiling: 30000, submittedAt: "03/07/2026", createdBy: "Cty TNHH Suất ăn An Bình",
    servings: 410,
    blocksData: [
      {
        mealType: "LUNCH",
        dishes: [
          dish("Cơm trắng", 200, [ing("Gạo tám thơm", 80, "g")]),
          dish("Tôm rim thịt", 230, [ing("Tôm sú", 40, "g"), ing("Thịt lợn nạc vai", 30, "g")]),
          dish("Canh mồng tơi nấu cua", 90, [ing("Cua đồng xay", 30, "g"), ing("Mồng tơi", 50, "g")]),
          dish("Lạc rang muối", 160, [ing("Đậu phộng (lạc)", 25, "g")]),
        ],
      },
      {
        mealType: "SNACK",
        dishes: [
          dish("Sữa tươi", 120, [ing("Sữa tươi tiệt trùng", 180, "ml")]),
          dish("Bánh quy", 130, [ing("Bột mì", 25, "g")]),
        ],
      },
    ],
  },
];


// ===== Nhận nguyên liệu (MF06, BR-070..BR-078) =====

/** Một dòng lô trong phiên nhận — nguyên liệu nào, lô nào, có bị trả về không. */
export interface ReceivingLine {
  ingredient: string;
  batchId: string;
  /** Lô bị từ chối trả về NCC — kèm lý do bắt buộc. */
  rejected?: boolean;
  rejectReason?: string;
}

export interface ReceivingSession {
  id: string;
  /** Nhãn hiển thị; khoá thật là `supplierId`. */
  supplier: string;
  supplierId?: string;
  supplierStatus: "APPROVED" | "SUSPENDED";
  deliveryDate: string;
  items: number;
  batches: string[];
  status: keyof typeof STATUS_BADGE;
  gpsVerified: boolean;
  photosCount: number;
  receivedBy: string;
  /**
   * Chi tiết từng dòng lô (Minh yêu cầu 25/07/2026) — màn lịch sử phải trả lời
   * được "nguyên liệu gì — lô nào — lô nào bị trả về, vì sao". Phiên seed cũ
   * không có thì màn hình fallback về danh sách `batches` trần.
   */
  lines?: ReceivingLine[];
}

export const RECEIVING_SESSIONS: ReceivingSession[] = [
  /*
   * Bổ sung 22/07/2026 — hai phiên nhận hàng đầu tuần 06/07.
   *
   * Thiếu hai phiên này thì bữa 06/07 và 07/07 không có lô nào còn hạn để nấu:
   * phiếu xuất đang trỏ về lô thịt lợn hết hạn 03/07 và lô su su hết hạn 04/07.
   * Khoá trỏ đúng bảng, đúng dòng có thật, nhưng SAI NGỮ NGHĨA — nấu bằng hàng
   * đã quá hạn. Đây là loại lỗi khoá ngoại không bắt được.
   */
  /*
   * Bổ sung 22/07/2026 — phiên nhận cho 14 lô trước đây "tự xuất hiện trong kho".
   *
   * Lô có mã, có nhà cung cấp, có ngày nhập — mọi khoá đều hợp lệ — nhưng không
   * phiên nhận nào ghi chúng. Truy xuất ngược theo BR-076 dừng ở kho: không biết
   * ai giao, phiếu nào, có kiểm tra tiếp nhận không. Đúng loại lỗi "dữ liệu bị
   * cô lập ở mức bản ghi", khoá ngoại không phát hiện được vì không có khoá nào sai.
   */
  { id: "RCV-20260702-02", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "02/07/2026 06:00", items: 6, batches: ["BATCH-VN-2210"], status: "RECEIVED", gpsVerified: true, photosCount: 8, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260701-03", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: "01/07/2026 06:20", items: 3, batches: ["BATCH-GF-4420"], status: "RECEIVED", gpsVerified: true, photosCount: 6, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260629-01", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "29/06/2026 05:55", items: 5, batches: ["BATCH-VN-2195"], status: "RECEIVED", gpsVerified: true, photosCount: 7, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260629-02", supplier: "Cty TNHH Thịt sạch Minh Phát", supplierStatus: "APPROVED", deliveryDate: "29/06/2026 05:30", items: 4, batches: ["BATCH-MP-8825", "BATCH-MP-8820"], status: "RECEIVED", gpsVerified: true, photosCount: 9, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260628-01", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "28/06/2026 06:05", items: 6, batches: ["BATCH-VN-2188", "BATCH-VN-2211"], status: "RECEIVED", gpsVerified: true, photosCount: 8, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260627-01", supplier: "Cty TNHH Thịt sạch Minh Phát", supplierStatus: "APPROVED", deliveryDate: "27/06/2026 05:40", items: 3, batches: ["BATCH-MP-8819"], status: "RECEIVED", gpsVerified: true, photosCount: 7, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260625-01", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: "25/06/2026 06:15", items: 2, batches: ["BATCH-GF-4410"], status: "RECEIVED", gpsVerified: true, photosCount: 5, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260622-01", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: "22/06/2026 06:10", items: 2, batches: ["BATCH-GF-4401"], status: "RECEIVED", gpsVerified: true, photosCount: 4, receivedBy: "Trần Văn Hùng" },
  // Cá basa đông lạnh nhận 21/06 — TRƯỚC khi chứng nhận ATTP của Đại Dương hết
  // hiệu lực 22/06. Trước đây lô ghi ngày nhập 30/06, tức nhận hàng của nhà cung
  // cấp đã mất chứng nhận và đúng ngày phiếu RCV-20260630-02 bị từ chối (BR-070).
  { id: "RCV-20260621-01", supplier: "Cty CP Hải sản Đại Dương", supplierStatus: "APPROVED", deliveryDate: "21/06/2026 06:00", items: 4, batches: ["BATCH-DD-1095", "BATCH-DD-1080"], status: "RECEIVED", gpsVerified: true, photosCount: 6, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260620-01", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: "20/06/2026 06:00", items: 4, batches: ["BATCH-GF-4388", "BATCH-GF-4395"], status: "RECEIVED", gpsVerified: true, photosCount: 6, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260618-01", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "18/06/2026 06:05", items: 3, batches: ["BATCH-VN-2170"], status: "RECEIVED", gpsVerified: true, photosCount: 5, receivedBy: "Trần Văn Hùng" },
  // Phiên nhận cho kịch bản HÔM NAY — mọi lô BATCH-TD-* phải có phiếu nhận, nếu
  // không truy xuất BR-076 đứt và audit LO_KHONG_CO_PHIEU_NHAN báo lỗi.
  { id: "RCV-TD-01", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: `${vnInDays(0)} 05:30`, items: 5, batches: ["BATCH-TD-01", "BATCH-TD-02", "BATCH-TD-03", "BATCH-TD-04", "BATCH-TD-05"], status: "RECEIVED", gpsVerified: true, photosCount: 10, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-TD-02", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: `${vnInDays(0)} 05:50`, items: 6, batches: ["BATCH-TD-06", "BATCH-TD-07", "BATCH-TD-08", "BATCH-TD-09", "BATCH-TD-10", "BATCH-TD-11"], status: "RECEIVED", gpsVerified: true, photosCount: 12, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260705-01", supplier: "Cty TNHH Thịt sạch Minh Phát", supplierStatus: "APPROVED", deliveryDate: "05/07/2026 05:35", items: 4, batches: ["BATCH-MP-8842"], status: "RECEIVED", gpsVerified: true, photosCount: 9, receivedBy: "Lê Thị Mai" },
  // 25/07/2026: lô Sữa chua/Chuối tây của bữa phụ 06/07 (phục vụ phiên kiểm thực
  // INSP-20260706-S) trước đây KHÔNG có phiên nhận nào — đứt truy xuất BR-076,
  // audit LO_KHONG_CO_PHIEU_NHAN báo đúng. Mỗi NCC một phiên vì phiếu nhận gắn 1 NCC.
  { id: "RCV-20260706-01", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: "06/07/2026 05:40", items: 1, batches: ["BATCH-GF-4430"], status: "RECEIVED", gpsVerified: true, photosCount: 4, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260706-02", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "06/07/2026 06:00", items: 1, batches: ["BATCH-VN-2225"], status: "RECEIVED", gpsVerified: true, photosCount: 3, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260705-02", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "05/07/2026 06:05", items: 7, batches: ["BATCH-VN-2220"], status: "RECEIVED", gpsVerified: true, photosCount: 11, receivedBy: "Lê Thị Mai" },
  // Lô tôm sú của bữa 06/07 trước đây không có phiên nhận nào — màn truy xuất
  // hiện "—" ở cột phiếu nhận. Phiên 28/06 là phiên có thật đã nhận lô này.

  { id: "RCV-20260702-01", supplier: "Cty CP Thực phẩm sạch GreenFarm", supplierStatus: "APPROVED", deliveryDate: "02/07/2026 05:45", items: 12, batches: ["BATCH-GF-4411", "BATCH-GF-4412"], status: "IN_PROGRESS", gpsVerified: true, photosCount: 8, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260701-02", supplier: "HTX Rau an toàn Vân Nội", supplierStatus: "APPROVED", deliveryDate: "01/07/2026 06:10", items: 8, batches: ["BATCH-VN-2201"], status: "RECEIVED", gpsVerified: true, photosCount: 12, receivedBy: "Trần Văn Hùng" },
  { id: "RCV-20260701-01", supplier: "Cty TNHH Thịt sạch Minh Phát", supplierStatus: "APPROVED", deliveryDate: "01/07/2026 05:30", items: 5, batches: ["BATCH-MP-8830", "BATCH-MP-8831"], status: "RECEIVED", gpsVerified: true, photosCount: 10, receivedBy: "Lê Thị Mai" },
  { id: "RCV-20260630-02", supplier: "Cty CP Hải sản Đại Dương", supplierStatus: "SUSPENDED", deliveryDate: "30/06/2026 06:00", items: 4, batches: ["BATCH-DD-1092"], status: "REJECTED", gpsVerified: true, photosCount: 6, receivedBy: "Trần Văn Hùng" },
];

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

export const INVENTORY: InventoryBatch[] = [
  // Hàng nhập 05/07 phục vụ tuần 06–07/07 (RCV-20260705-01/02).
  { id: "BATCH-MP-8842", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai", supplier: "Minh Phát", quantity: "40 kg", storageZone: "Chilled", receivedDate: "05/07/2026", expiryDate: "08/07/2026", status: "ACTIVE" },
  { id: "BATCH-VN-2220", ingredientId: "ING-SU-SU", ingredient: "Su su", supplier: "Vân Nội", quantity: "32 kg", storageZone: "Chilled", receivedDate: "05/07/2026", expiryDate: "10/07/2026", status: "ACTIVE" },
  { id: "BATCH-MP-8830", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai", supplier: "Minh Phát", quantity: "45 kg", storageZone: "Chilled", receivedDate: "01/07/2026", expiryDate: "03/07/2026", status: "EXPIRING_SOON" },
  { id: "BATCH-MP-8831", ingredientId: "ING-THIT-GA", ingredient: "Thịt gà ta", supplier: "Minh Phát", quantity: "30 kg", storageZone: "Chilled", receivedDate: "01/07/2026", expiryDate: "04/07/2026", status: "ACTIVE" },
  { id: "BATCH-VN-2201", ingredientId: "ING-RAU-MUONG", ingredient: "Rau muống", supplier: "Vân Nội", quantity: "25 kg", storageZone: "Chilled", receivedDate: "01/07/2026", expiryDate: "02/07/2026", status: "EXPIRING_SOON" },
  { id: "BATCH-GF-4410", ingredientId: "ING-GAO-TAM", ingredient: "Gạo tám thơm", supplier: "GreenFarm", quantity: "200 kg", storageZone: "Dry", receivedDate: "25/06/2026", expiryDate: "25/12/2026", status: "ACTIVE" },
  { id: "BATCH-GF-4388", ingredientId: "ING-DAU-AN", ingredient: "Dầu ăn thực vật", supplier: "GreenFarm", quantity: "40 lít", storageZone: "Dry", receivedDate: "20/06/2026", expiryDate: "28/06/2026", status: "EXPIRED" },
  { id: "BATCH-DD-1080", ingredientId: "ING-TOM-SU", ingredient: "Tôm sú đông lạnh", supplier: "Đại Dương", quantity: "18 kg", storageZone: "Frozen", receivedDate: "21/06/2026", expiryDate: "28/09/2026", status: "ACTIVE", quarantine: { at: "30/06/2026 07:10", by: "Trần Văn Hùng (bếp)", reason: "Nhà cung cấp Đại Dương bị đình chỉ — giữ lô chờ kết luận." } },
  { id: "BATCH-GF-4412", ingredientId: "ING-RAU-CAI-NGOT", ingredient: "Rau cải ngọt", supplier: "GreenFarm", quantity: "30 kg", storageZone: "Chilled", receivedDate: "02/07/2026", expiryDate: "05/07/2026", status: "ACTIVE" },
  { id: "BATCH-GF-4411", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai (bổ sung)", supplier: "GreenFarm", quantity: "45 kg", storageZone: "Chilled", receivedDate: "02/07/2026", expiryDate: "05/07/2026", status: "ACTIVE" },
  { id: "BATCH-VN-2195", ingredientId: "ING-BI-DO", ingredient: "Bí đỏ", supplier: "Vân Nội", quantity: "35 kg", storageZone: "Dry", receivedDate: "29/06/2026", expiryDate: "09/07/2026", status: "ACTIVE" },
  { id: "BATCH-VN-2188", ingredientId: "ING-SU-SU", ingredient: "Su su", supplier: "Vân Nội", quantity: "28 kg", storageZone: "Chilled", receivedDate: "28/06/2026", expiryDate: "04/07/2026", status: "ACTIVE" },
  { id: "BATCH-MP-8825", ingredientId: "ING-TRUNG-CUT", ingredient: "Trứng cút", supplier: "Minh Phát", quantity: "1.200 quả", storageZone: "Chilled", receivedDate: "29/06/2026", expiryDate: "13/07/2026", status: "ACTIVE" },
  { id: "BATCH-MP-8819", ingredientId: "ING-THIT-BO", ingredient: "Thịt bò thăn", supplier: "Minh Phát", quantity: "22 kg", storageZone: "Frozen", receivedDate: "27/06/2026", expiryDate: "27/08/2026", status: "ACTIVE" },
  { id: "BATCH-GF-4401", ingredientId: "ING-NUOC-MAM", ingredient: "Nước mắm truyền thống", supplier: "GreenFarm", quantity: "24 lít", storageZone: "Dry", receivedDate: "22/06/2026", expiryDate: "22/06/2027", status: "ACTIVE" },
  { id: "BATCH-GF-4395", ingredientId: "ING-MUOI", ingredient: "Muối i-ốt", supplier: "GreenFarm", quantity: "50 kg", storageZone: "Dry", receivedDate: "20/06/2026", expiryDate: "20/06/2028", status: "ACTIVE" },
  { id: "BATCH-VN-2170", ingredientId: "ING-HANH-KHO", ingredient: "Hành khô", supplier: "Vân Nội", quantity: "15 kg", storageZone: "Dry", receivedDate: "18/06/2026", expiryDate: "18/08/2026", status: "ACTIVE" },
  // Bổ sung 21/07/2026: thực đơn có món dùng cà chua/tỏi/gạo nhưng kho không có lô
  // → mọi phiếu xuất theo BOM đều báo thiếu. Thêm lô để luồng menu-driven chạy đủ.
  { id: "BATCH-VN-2210", ingredientId: "ING-CA-CHUA", ingredient: "Cà chua", supplier: "Vân Nội", quantity: "40 kg", storageZone: "Chilled", receivedDate: "02/07/2026", expiryDate: "08/07/2026", status: "ACTIVE" },
  { id: "BATCH-VN-2211", ingredientId: "ING-TOI", ingredient: "Tỏi", supplier: "Vân Nội", quantity: "12 kg", storageZone: "Dry", receivedDate: "28/06/2026", expiryDate: "28/08/2026", status: "ACTIVE" },
  { id: "BATCH-DD-1095", ingredientId: "ING-CA-BASA", ingredient: "Cá basa phi lê", supplier: "Đại Dương", quantity: "35 kg", storageZone: "Frozen", receivedDate: "21/06/2026", expiryDate: "30/09/2026", status: "ACTIVE" },
  // Lô dầu ăn còn hạn — BATCH-GF-4388 giữ nguyên trạng thái EXPIRED để demo BR-205
  // (bị khóa, FEFO bỏ qua) nhưng vẫn phải có lô dùng được, nếu không mọi món rán/xào
  // đều báo thiếu và không kiểm chứng được luồng bình thường.
  { id: "BATCH-GF-4420", ingredientId: "ING-DAU-AN", ingredient: "Dầu ăn thực vật", supplier: "GreenFarm", quantity: "36 lít", storageZone: "Dry", receivedDate: "01/07/2026", expiryDate: "01/01/2027", status: "ACTIVE" },
  // Lô thịt gà của bữa trưa 30/06 — đã bị hệ thống tự cách ly khi báo sự cố
  // INC-2026-014 (BR-149). Giữ trong kho để bản đồ truy xuất của sự cố tra được
  // ngược từ món "Gà rang gừng" về đúng lô và nhà cung cấp.
  { id: "BATCH-MP-8820", ingredientId: "ING-THIT-GA", ingredient: "Thịt gà ta", supplier: "Minh Phát", quantity: "25 kg", storageZone: "Chilled", receivedDate: "29/06/2026", expiryDate: "02/07/2026", status: "EXPIRING_SOON", quarantine: { at: "30/06/2026 14:35", by: "Ngô Thị Hà (Y tế)", reason: "Nghi lô gà không đạt nhiệt độ lõi khi chế biến — phục vụ điều tra INC-2026-014 (BR-149).", incidentId: "INC-2026-014" } },

  // Bữa phụ 06/07 (MENU-2026-27) gồm sữa chua + chuối tây nhưng kho không có lô
  // nào nhập ngày đó, nên Mẫu số 1 của phiên kiểm thực bữa phụ 06/07 không chọn
  // được thực phẩm nào — dropdown "lô đã nhập trong ngày" rỗng.
  { id: "BATCH-GF-4430", ingredientId: "ING-SUA-CHUA", ingredient: "Sữa chua", supplier: "GreenFarm", quantity: "400 hộp", storageZone: "Chilled", receivedDate: "06/07/2026", expiryDate: "27/07/2026", status: "EXPIRING_SOON" },
  { id: "BATCH-VN-2225", ingredientId: "ING-CHUOI", ingredient: "Chuối tây", supplier: "Vân Nội", quantity: "45 kg", storageZone: "Chilled", receivedDate: "06/07/2026", expiryDate: "11/07/2026", status: "EXPIRED" },

  // ===== Lô tươi cho kịch bản HÔM NAY (MENU-TD-3) — nhập hôm nay, còn hạn =====
  // Đủ cho ~380 suất mỗi món, để bếp xuất kho được ngay trong ngày chạy thử.
  { id: "BATCH-TD-01", ingredientId: "ING-GAO-TAM", ingredient: "Gạo tám thơm", supplier: "GreenFarm", quantity: "200 kg", storageZone: "Dry", receivedDate: vnInDays(0), expiryDate: vnInDays(150), status: "ACTIVE" },
  { id: "BATCH-TD-02", ingredientId: "ING-DAU-PHU", ingredient: "Đậu phụ", supplier: "GreenFarm", quantity: "40 kg", storageZone: "Chilled", receivedDate: vnInDays(0), expiryDate: vnInDays(3), status: "ACTIVE" },
  { id: "BATCH-TD-03", ingredientId: "ING-CA-CHUA", ingredient: "Cà chua", supplier: "GreenFarm", quantity: "25 kg", storageZone: "Chilled", receivedDate: vnInDays(0), expiryDate: vnInDays(7), status: "ACTIVE" },
  { id: "BATCH-TD-04", ingredientId: "ING-DAU-AN", ingredient: "Dầu ăn thực vật", supplier: "GreenFarm", quantity: "36 lít", storageZone: "Dry", receivedDate: vnInDays(0), expiryDate: vnInDays(180), status: "ACTIVE" },
  { id: "BATCH-TD-05", ingredientId: "ING-MUOI", ingredient: "Muối i-ốt", supplier: "GreenFarm", quantity: "20 kg", storageZone: "Dry", receivedDate: vnInDays(0), expiryDate: vnInDays(365), status: "ACTIVE" },
  { id: "BATCH-TD-06", ingredientId: "ING-BI-DO", ingredient: "Bí đỏ", supplier: "Vân Nội", quantity: "35 kg", storageZone: "Dry", receivedDate: vnInDays(0), expiryDate: vnInDays(12), status: "ACTIVE" },
  { id: "BATCH-TD-07", ingredientId: "ING-HANH-KHO", ingredient: "Hành khô", supplier: "Vân Nội", quantity: "15 kg", storageZone: "Dry", receivedDate: vnInDays(0), expiryDate: vnInDays(45), status: "ACTIVE" },
  { id: "BATCH-TD-08", ingredientId: "ING-SU-SU", ingredient: "Su su", supplier: "Vân Nội", quantity: "32 kg", storageZone: "Chilled", receivedDate: vnInDays(0), expiryDate: vnInDays(7), status: "ACTIVE" },
  { id: "BATCH-TD-09", ingredientId: "ING-TOI", ingredient: "Tỏi", supplier: "Vân Nội", quantity: "12 kg", storageZone: "Dry", receivedDate: vnInDays(0), expiryDate: vnInDays(60), status: "ACTIVE" },
  { id: "BATCH-TD-10", ingredientId: "ING-CHUOI", ingredient: "Chuối tây", supplier: "Vân Nội", quantity: "45 kg", storageZone: "Chilled", receivedDate: vnInDays(0), expiryDate: vnInDays(5), status: "ACTIVE" },
  { id: "BATCH-TD-11", ingredientId: "ING-DUA-HAU", ingredient: "Dưa hấu", supplier: "Vân Nội", quantity: "45 kg", storageZone: "Chilled", receivedDate: vnInDays(0), expiryDate: vnInDays(7), status: "ACTIVE" },
];

// ===== Kiểm thực ba bước (MF07, BR-079..BR-099, QĐ 1246/QĐ-BYT) =====

/**
 * Các trường chi tiết theo ĐÚNG Mẫu số 1/2/3 — Phụ lục 1, QĐ 1246/QĐ-BYT
 * (00_Policies/1246_QD-BYT...md). Đây là biểu mẫu luật — không được thiếu cột;
 * bản in (print) phải render đúng format bảng của Bộ Y tế.
 */

/** Mẫu số 1 — phần I: thực phẩm tươi sống, đông lạnh (15 cột). */
export interface Step1FreshRow {
  name: string; // (2) Tên thực phẩm
  receivedAt: string; // (3) Thời gian nhập (ngày, giờ)
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
  producer: string; // (3) Tên cơ sở sản xuất
  producerAddress: string; // (4) Địa chỉ sản xuất
  receivedAt: string; // (5) Thời gian nhập
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
  dishName: string; // (3) Tên món ăn
  servings: string; // (4) Số lượng suất ăn
  dividedDoneAt: string; // (5) Thời gian chia món ăn xong (ngày, giờ)
  eatStartAt: string; // (6) Thời gian bắt đầu ăn (ngày, giờ)
  utensils: string; // (7) Dụng cụ chia, chứa đựng, che đậy, bảo quản
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

export const INSPECTIONS: InspectionSession[] = [
  /*
   * Bổ sung 22/07/2026: mỗi bữa ĐÃ CÔNG BỐ phải có phiên kiểm thực của đúng ngày +
   * bữa đó, nếu không cổng BR-094 rơi vào nhánh "không tìm thấy phiếu" — cảnh báo
   * suông thay vì kết luận được. Trước đó chỉ 06/07 có phiên, còn 07/07 · 10/07 ·
   * 24/07 đều trống dù thực đơn đã công bố.
   *
   * 07/07 để Bước 2 KHÔNG ĐẠT — nhánh chặn phục vụ của BR-094 vốn không bữa nào
   * đã công bố demo được, nên quy tắc chưa từng thực sự chạy trên dữ liệu thật.
   */
  /*
   * Bổ sung 24/07/2026: MỌI phiên trong seed đều đã `step1: "PASSED"`, mà bước đã
   * chốt thì form chuyển sang chỉ-xem (fieldset disabled) — nên không có lấy một
   * phiên nào thao tác được Bước 1. Đường còn lại là nút "Phiên kiểm thực mới",
   * nhưng phiên tạo mới lại rỗng catalog vì lệch định dạng ngày (xem
   * `vnOfDate`), nghĩa là Bước 1 chưa từng chạy thử được trên dữ liệu thật.
   *
   * Hai phiên dưới đây để CẢ BA BƯỚC ở PENDING, đặt đúng ngày có lô nhập kho và
   * có thực đơn công bố, nên vào là chọn được thực phẩm/món từ dropdown ở cả 3 mẫu.
   */
  // Hôm nay — neo theo ngày chạy để luôn khớp lô BATCH-TD-01…11 (cùng dùng vnInDays(0)).
  // Bữa chiều, vì bữa trưa hôm nay đã có INSP-20260724-L; một ngày + một bữa chỉ nên
  // có một phiên, nếu không `inspectionForMeal` lấy nhầm phiên cho cổng BR-094.
  { id: "INSP-TD-S", date: vnInDays(0), meal: "Bữa chiều", step1: "PENDING", step2: "PENDING", step3: "PENDING", overall: "IN_PROGRESS", inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Chuối tây", evidencePhotos: 0, gps: "Khu chia suất — 21.0363, 105.7626 (±12m)", staffNotes: "Phiên MỞ để chạy thử: Bước 1 chọn được 11 lô nhập hôm nay (5 lô tươi + 6 lô khô), Bước 2/3 lấy món bữa chiều của MENU-TD-3 (Chuối tây, Dưa hấu)." },
  // 06/07 — bữa chiều MENU-2026-27 (Sữa chua, Chuối tây), lô nhập cùng ngày ở INVENTORY.
  { id: "INSP-20260706-S", date: "06/07/2026", meal: "Bữa chiều", step1: "PENDING", step2: "PENDING", step3: "PENDING", overall: "IN_PROGRESS", inspector: "Lê Thị Mai", coSigner: "Ngô Thị Hà (Y tế)", dish: "Sữa chua", evidencePhotos: 0, gps: "Khu chia suất — 21.0363, 105.7626 (±14m)", staffNotes: "Phiên MỞ để chạy thử trên ngày cũ: Bước 1 chọn được lô Sữa chua BATCH-GF-4430 và Chuối tây BATCH-VN-2225 nhập 06/07." },
  { id: "INSP-20260724-L", date: "24/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 82, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Gà rang gừng", evidencePhotos: 3, gps: "Bếp trung tâm — 21.0362, 105.7627 (±10m)", staffNotes: "Bữa trưa 24/07 — thực đơn MENU-2026-30 (Khối 3). Đủ 3 bước Đạt." },
  { id: "INSP-20260710-L", date: "10/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 80, inspector: "Lê Thị Mai", coSigner: "Ngô Thị Hà (Y tế)", dish: "Thịt kho trứng cút", evidencePhotos: 3, gps: "Bếp trung tâm — 21.0362, 105.7627 (±12m)", staffNotes: "Bữa trưa 10/07 — thực đơn MENU-2026-26 (Khối 4). Đủ 3 bước Đạt." },
  { id: "INSP-20260707-L", date: "07/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "FAILED", step3: "PENDING", overall: "FAILED", coreTemp: 68, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Cá basa sốt cà chua", evidencePhotos: 5, gps: "Bếp trung tâm — 21.0362, 105.7627 (±11m)", staffNotes: "Nhiệt độ lõi cá 68°C < 75°C (BR-090) — Bước 2 KHÔNG ĐẠT, dừng phục vụ bữa trưa 07/07 cho tới khi nấu lại và tái kiểm." },
  // Thêm 22/07/2026 — trước đó không phiên kiểm thực nào trùng ngày+bữa với một
  // thực đơn nào cả (kiểm thực 29/06–02/07, thực đơn từ 06/07), nên chuỗi truy xuất
  // "kiểm thực → thực đơn khối → phiếu xuất → lô" luôn rỗng dù mã hoá có quy đổi.
  { id: "INSP-20260706-L", date: "06/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 83, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Thịt kho trứng cút", evidencePhotos: 3, gps: "Bếp trung tâm — 21.0362, 105.7627 (±10m)", staffNotes: "Bữa trưa 06/07 — khớp thực đơn MENU-2026-27 (Khối 3)." },
  // Ngày giáo viên đang phát suất (STUDENT_DISTRIBUTIONS) — cổng BR-094 cần phiên
  // của đúng bữa đó để biết được phép phục vụ hay không.
  { id: "INSP-20260703-L", date: "03/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 81, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Tôm rim thịt", evidencePhotos: 3, gps: "Bếp trung tâm — 21.0362, 105.7627 (±11m)", staffNotes: "Đủ 3 bước Đạt — bữa trưa được phép phục vụ (BR-094)." },
  { id: "INSP-20260702-L", date: "02/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "IN_PROGRESS", step3: "PENDING", overall: "IN_PROGRESS", coreTemp: 82, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Thịt kho trứng cút", evidencePhotos: 4, gps: "Bếp trung tâm — 21.0362, 105.7627 (±12m)", staffNotes: "Nguyên liệu đạt cảm quan; nhiệt lõi đo tại nồi số 2." },
  {
    id: "INSP-20260701-L", date: "01/07/2026", meal: "Bữa trưa", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 85, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Cá basa sốt cà chua", evidencePhotos: 6, gps: "Bếp trung tâm — 21.0362, 105.7627 (±10m)", staffNotes: "Đủ 3 bước, không phát hiện bất thường.",
    // Phiên mẫu đã điền ĐẦY ĐỦ theo Mẫu 1/2/3 QĐ 1246 — dùng demo bản in
    formHeader: { facility: "Bếp ăn TH Đoàn Thị Điểm — Cty TNHH Suất ăn An Bình", inspector: "Trần Văn Hùng", checkedAt: "01/07/2026", location: "Khu bếp chính, TH Đoàn Thị Điểm, Mỹ Đình 2, Nam Từ Liêm, Hà Nội" },
    step1Fresh: [
      { name: "Thịt lợn nạc vai", receivedAt: "01/07/2026 05:30", quantity: "45 kg", supplierName: "Cty TNHH Thịt sạch Minh Phát", supplierContact: "Thanh Trì, Hà Nội — 0908 666 777", deliverer: "Đỗ Văn Tám", hasInvoice: true, hasVetCert: true, hasQuarantineCert: true, sensoryPass: true, quickTest: "NONE", action: "" },
      { name: "Rau muống", receivedAt: "01/07/2026 06:10", quantity: "25 kg", supplierName: "HTX Rau an toàn Vân Nội", supplierContact: "Đông Anh, Hà Nội — 0977 444 555", deliverer: "Nguyễn Thị Lụa", hasInvoice: true, hasVetCert: false, hasQuarantineCert: false, sensoryPass: true, quickTest: "PASS", action: "" },
      { name: "Cá basa phi lê", receivedAt: "01/07/2026 05:45", quantity: "30 kg", supplierName: "Cty CP Thực phẩm sạch GreenFarm", supplierContact: "Chương Mỹ, Hà Nội — 0913 222 333", deliverer: "Đỗ Văn Tám", hasInvoice: true, hasVetCert: true, hasQuarantineCert: true, sensoryPass: true, quickTest: "NONE", action: "" },
    ],
    step1Dry: [
      { name: "Nước mắm truyền thống", producer: "Cty CP Thủy sản 584 Nha Trang", producerAddress: "Nha Trang, Khánh Hòa", receivedAt: "01/07/2026 06:00", quantity: "12 lít", supplierName: "Cty CP Thực phẩm sạch GreenFarm", deliverer: "Đỗ Văn Tám", supplierContact: "Chương Mỹ, Hà Nội — 0913 222 333", expiry: "22/06/2027", storage: "Thường", hasInvoice: true, sensoryPass: true, action: "" },
    ],
    step2Rows: [
      { shift: "Bữa trưa — 10:45", dishName: "Cá basa sốt cà chua", mainIngredients: "Cá basa 30kg; cà chua 8kg; hành khô 1kg", servings: "1.841", prepDoneAt: "01/07/2026 08:15", cookDoneAt: "01/07/2026 09:50", hygieneStaff: true, hygieneEquipment: true, hygieneArea: true, sensoryPass: true, action: "" },
      { shift: "Bữa trưa — 10:45", dishName: "Canh rau ngót thịt băm", mainIngredients: "Rau ngót 15kg; thịt lợn 8kg", servings: "1.841", prepDoneAt: "01/07/2026 08:30", cookDoneAt: "01/07/2026 09:40", hygieneStaff: true, hygieneEquipment: true, hygieneArea: true, sensoryPass: true, action: "" },
    ],
    step3Rows: [
      { shift: "Bữa trưa — 10:45", dishName: "Cá basa sốt cà chua", servings: "1.841", dividedDoneAt: "01/07/2026 10:20", eatStartAt: "01/07/2026 10:45", utensils: "Khay inox có nắp, xe đẩy giữ nhiệt", sensoryPass: true, action: "" },
      { shift: "Bữa trưa — 10:45", dishName: "Canh rau ngót thịt băm", servings: "1.841", dividedDoneAt: "01/07/2026 10:25", eatStartAt: "01/07/2026 10:45", utensils: "Nồi inox có nắp đậy, muôi riêng", sensoryPass: true, action: "" },
    ],
  },
  { id: "INSP-20260630-L", date: "30/06/2026", meal: "Bữa trưa", step1: "PASSED", step2: "FAILED", step3: "PENDING", overall: "FAILED", coreTemp: 68, inspector: "Lê Thị Mai", coSigner: "Ngô Thị Hà (Y tế)", dish: "Gà rang gừng", evidencePhotos: 5, gps: "Bếp trung tâm — 21.0361, 105.7629 (±15m)", staffNotes: "Nhiệt lõi 68°C <75°C (BR-090) — dừng Bước 3, CAPA tự tạo (BR-095), chờ tái chế biến." },
  { id: "INSP-20260630-S", date: "30/06/2026", meal: "Bữa chiều", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 79, inspector: "Lê Thị Mai", dish: "Sữa chua", evidencePhotos: 3, gps: "Khu chia suất — 21.0363, 105.7626 (±14m)", staffNotes: "Bữa phụ đóng gói sẵn, kiểm tra hạn dùng đạt." },
  { id: "INSP-20260629-L", date: "29/06/2026", meal: "Bữa trưa", step1: "PASSED", step2: "PASSED", step3: "PASSED", overall: "PASSED", coreTemp: 88, inspector: "Trần Văn Hùng", coSigner: "Ngô Thị Hà (Y tế)", dish: "Thịt bò xào rau củ", evidencePhotos: 6, gps: "Bếp trung tâm — 21.0362, 105.7627 (±11m)", staffNotes: "Đạt toàn bộ; ảnh chụp đủ 3 bước." },
];

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
export interface FoodSample {
  id: string;
  dish: string; // (2) Tên mẫu thức ăn
  /** Khoá về THƯ VIỆN MÓN — nối ở vòng backfill, xem "Nối chuỗi truy xuất món". */
  dishId?: string;
  meal: string;
  /** (3) Bữa ăn (giờ ăn...) — VD "Bữa trưa — 10:45". */
  mealShift?: string;
  /** (4) Số lượng suất ăn — Điều 1.2: từ 30 suất trở lên bắt buộc lưu mẫu. */
  servings?: string;
  weight: string; // (5) BR-203/Điều 7.2: >=100g đặc / >=150ml lỏng
  /** (6) Dụng cụ chứa mẫu thức ăn lưu. */
  container?: string;
  /** Điều 6.2: dụng cụ rửa sạch, tiệt trùng trước khi dùng. */
  sanitized?: boolean;
  /** Điều 7.1: mỗi món 1 dụng cụ riêng và được niêm phong. */
  sealed?: boolean;
  fridgeTemp: number; // (7) BR-110/Điều 8.1: 2–8°C
  collectedAt: string; // (8) Thời gian lấy mẫu (giờ, ngày, tháng, năm)
  collectedBy: string; // (11) Người lưu mẫu + nhãn Mẫu 4
  storageLocation?: string;
  retentionDeadline: string; // BR-111: 24 giờ (Điều 8.2 — sửa 26/07/2026, Vòng 5 Mục 9.1)
  hoursRemaining: number;
  /** (9) Thời gian hủy mẫu — khi status DISPOSED. */
  disposedAt?: string;
  /** (12) Người hủy mẫu (ký và ghi rõ họ tên). */
  disposedBy?: string;
  /** (10) Ghi chú (chất lượng mẫu thức ăn lưu...). */
  disposalNote?: string;
  status: keyof typeof STATUS_BADGE;
  /**
   * Bug 2 (review_medicalstaff_incidentmanagement.md Mục 9.10, 09/07/2026): khóa liên kết
   * để Investigation Workspace card 1 query đúng mẫu của INC-xxx hiện tại.
   */
  incidentId?: string; // set khi Authorize Retrieval / liên kết Disposed (UC-076, BR-114)
  labDestination?: string;
  retrievedAt?: string;
  /**
   * BR-212 (sửa 19/07/2026 — TT 14/2011/TT-BYT Điều 5 khoản 2, thay trích dẫn Phụ lục V):
   * hệ thống KHÔNG tự sinh "Biên bản bàn giao mẫu"; bằng chứng là ảnh/scan phiếu nhận
   * mẫu do chính phòng lab cấp.
   * Vòng 5 (26/07/2026, Mục 9.2): người tải lên quay lại là MEDICAL STAFF — cơ chế giữ
   * nguyên, chỉ đổi actor.
   */
  labRepresentative?: string; // đại diện bên nhận, Medical Staff ghi lúc bàn giao
  testCriteria?: string; // chỉ tiêu xét nghiệm — Medical Staff chỉ định
  /** Vòng 2 — liên kết tới phiếu thu hồi & gửi xét nghiệm gộp nhiều mẫu (`SampleLabRequest`) */
  labRequestId?: string;
  /** Mốc chuyển SENT_TO_LAB — Vòng 5: do chính Medical Staff đặt khi bàn giao xong */
  sentToLabAt?: string;
  deliveredBy?: string; // người trực tiếp mang mẫu đi — Vòng 5: Medical Staff đăng nhập
  labReceiptFiles?: UploadedFile[]; // ảnh/scan phiếu nhận mẫu do lab cấp (BR-212)
  /**
   * Vòng 5 Mục 9.2 — "Biên bản giao nhận mẫu" (ảnh chụp/PDF) BẮT BUỘC trước khi mẫu
   * chuyển `SENT_TO_LAB`. Tách khỏi `labReceiptFiles` vì đây là bằng chứng của CHUỖI
   * GIÁM SÁT (ai giao, ai nhận, lúc nào) — thứ được thêm chính vì rủi ro tráo mẫu; còn
   * `labReceiptFiles` là phiếu do lab cấp, có thể về sau mới có.
   */
  handoverRecordFiles?: UploadedFile[];
  /** Ảnh niêm phong mẫu tại tủ lưu, chụp ngay trước khi mang đi (Vòng 6 Mục 11.3) */
  sealPhotoFiles?: UploadedFile[];
}

/**
 * `SampleLabRequest` — review_medicalstaff_foodsamples.md Mục 3.5 (19/07/2026),
 * ĐỔI NGHĨA ở Vòng 5 (26/07/2026, Mục 9.2).
 *
 * Trước: task GIAO VIỆC cho Service Caterers (Y tế chỉ định → bếp lấy mẫu, mang đi, xác nhận).
 * Nay: **phiếu thu hồi & gửi xét nghiệm do chính Medical Staff thực hiện** — một lần bàn
 * giao thực địa, gộp 1 Incident · 1 nơi nhận · nhiều mẫu. Entity được GIỮ LẠI (không xoá)
 * vì vẫn đúng vai trò gom nhóm: tránh lặp nơi nhận/chỉ tiêu/bằng chứng trên từng mẫu lẻ,
 * và giữ nguyên dữ liệu cũ đọc được. Chỉ khác: không còn khoảng chờ giữa 2 vai trò, phiếu
 * sinh ra là đã ở `SENT_TO_LAB`.
 *
 * Lý do đảo ngược (ghi lại để không đảo lần 3 mà thiếu căn cứ): Nhà thầu là bên có thể bị
 * quy trách nhiệm qua kết quả xét nghiệm — để họ tự tay mang mẫu đi gửi thì có động cơ và
 * điều kiện tráo mẫu, mất tính khách quan của chuỗi giám sát.
 */
export interface SampleLabRequest {
  id: string; // LABREQ-xxx
  incidentId: string;
  sampleIds: string[];
  /** Nhãn nơi nhận; khoá thật là `authorityId`. */
  labDestination: string;
  authorityId?: string;
  /**
   * LAB = gửi xét nghiệm theo chỉ tiêu, chờ kết quả về.
   * AUTHORITY = cơ quan chức năng thu giữ làm vật chứng — không có chỉ tiêu do
   * trường đặt, và có thể không bao giờ có kết quả trả về.
   */
  handoverType?: "LAB" | "AUTHORITY";
  /** Bắt buộc khi gửi xét nghiệm; để trống khi bị thu giữ. */
  testCriteria: string;
  requestedBy: string; // Medical Staff — tự động
  requestedAt: string;
  /**
   * SENT_TO_LAB | CANCELLED (bản ghi mới).
   * RETRIEVAL_REQUESTED/PENDING_LAB_DELIVERY chỉ còn ở dữ liệu cũ — xem STATUS_BADGE.
   */
  status: keyof typeof STATUS_BADGE;
  note?: string;
  /**
   * @deprecated Vòng 5 (26/07/2026) — huỷ phiếu chỉ có nghĩa khi còn khoảng chờ giữa
   * "chỉ định" và "thực hiện". Nay Medical Staff bàn giao xong mới sinh phiếu, nên phiếu
   * ra đời đã ở `SENT_TO_LAB` — trạng thái cuối, không hoàn tác (Vòng 1 Mục 2.1). Giữ
   * field để đọc dữ liệu cũ đã huỷ; không sinh mới.
   */
  cancelledAt?: string;
  cancelledBy?: string;
  cancelReason?: string;
  /** Vòng 5: người trực tiếp mang mẫu đi = Medical Staff đăng nhập (không còn phía bếp) */
  deliveredBy?: string;
  sentToLabAt?: string;
  labRepresentative?: string;
  labReceiptFiles?: UploadedFile[];
  /** Vòng 5 Mục 9.2 — "Biên bản giao nhận mẫu", BẮT BUỘC ≥1 file trước khi SENT_TO_LAB */
  handoverRecordFiles?: UploadedFile[];
  /** Ảnh niêm phong tại tủ lưu, chụp trước khi mang đi (Vòng 6 Mục 11.3) */
  sealPhotoFiles?: UploadedFile[];
}

export const SAMPLE_LAB_REQUESTS: SampleLabRequest[] = [
  {
    id: "LABREQ-2026-001",
    incidentId: "INC-2026-014",
    sampleIds: ["SMP-20260630-03"],
    labDestination: "CDC Hà Nội",
    testCriteria: "Salmonella, S.aureus, tổng vi sinh vật hiếu khí",
    requestedBy: "Ngô Thị Hà (Y tế)",
    requestedAt: "30/06/2026 14:35",
    status: "SENT_TO_LAB",
    // Vòng 5 (26/07/2026): người mang mẫu đi đổi từ đại diện bếp về chính cán bộ y tế.
    deliveredBy: "Ngô Thị Hà (Y tế)",
    sentToLabAt: "30/06/2026 15:00",
    labRepresentative: "Nguyễn Thị Vân (CDC Hà Nội)",
    handoverRecordFiles: [{ name: "bien_ban_giao_nhan_mau_cdc.pdf", uploadedAt: "30/06/2026 15:05", size: "640 KB" }],
    labReceiptFiles: [{ name: "phieu_nhan_mau_cdc.jpg", uploadedAt: "30/06/2026 15:10", size: "1.8 MB" }],
  },
  /*
   * LABREQ-2026-002 (phiếu ở trạng thái `RETRIEVAL_REQUESTED`, chờ bếp thực hiện) đã BỎ
   * khỏi seed ở Vòng 5 (26/07/2026): trạng thái đó nay không sinh ra được nữa, giữ lại
   * chỉ khiến màn Sổ lưu mẫu hiện một đầu việc không ai thao tác được. Hai mẫu nó từng
   * trỏ tới (SMP-20260630-01/02) quay về `RETAINED` — đúng vai "mẫu còn hiệu lực của ca
   * INC-2026-014" để chạy thử luồng deep-link Thu hồi & gửi xét nghiệm (Vòng 6 Mục 11.3).
   * Nhãn trạng thái cũ vẫn còn trong STATUS_BADGE để đọc dữ liệu thật đã lưu trước đó.
   */
];

export const FOOD_SAMPLES: FoodSample[] = [
  /*
   * Mẫu lưu bữa trưa 06/07 — bữa DUY NHẤT có đủ chuỗi truy xuất (thực đơn đã công
   * bố cho cả 3 khối · phiên kiểm thực ba bước · phiếu xuất kho · sổ phát suất).
   * Trước 22/07/2026 seed mẫu lưu dừng ở 02/07 nên đúng bữa dùng để truy xuất lại
   * thì KHÔNG có mẫu nào — trong khi Điều 1.2 QĐ 1246 bắt buộc lưu mẫu mọi bữa từ
   * 30 suất trở lên. Mỗi món một dụng cụ riêng, niêm phong (Điều 7.1).
   *
   * 26/07/2026 (Vòng 5 Mục 9.1): `retentionDeadline` = `collectedAt` + **24 giờ** (BR-111
   * mới), không còn +72 giờ; `hoursRemaining` tính lại trên mốc 24 giờ.
   */
  { id: "SMP-20260706-01", dish: "Cơm trắng", meal: "Trưa 06/07", mealShift: "Bữa trưa — 10:45", servings: "1850", weight: "120 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "06/07/2026 10:15", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 1", retentionDeadline: "07/07/2026 10:15", hoursRemaining: 24, fridgeTemp: 4.2, status: "RETAINED" },
  { id: "SMP-20260706-02", dish: "Thịt kho trứng cút", meal: "Trưa 06/07", mealShift: "Bữa trưa — 10:45", servings: "1850", weight: "115 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "06/07/2026 10:17", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 1", retentionDeadline: "07/07/2026 10:17", hoursRemaining: 24, fridgeTemp: 4.2, status: "RETAINED" },
  { id: "SMP-20260706-03", dish: "Canh bí đỏ nấu tôm", meal: "Trưa 06/07", mealShift: "Bữa trưa — 10:45", servings: "1850", weight: "180 ml", container: "Hộp thủy tinh có nắp", sanitized: true, sealed: true, collectedAt: "06/07/2026 10:19", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 2", retentionDeadline: "07/07/2026 10:19", hoursRemaining: 24, fridgeTemp: 4.3, status: "RETAINED" },
  { id: "SMP-20260706-04", dish: "Su su xào", meal: "Trưa 06/07", mealShift: "Bữa trưa — 10:45", servings: "1850", weight: "110 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "06/07/2026 10:21", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 2", retentionDeadline: "07/07/2026 10:21", hoursRemaining: 24, fridgeTemp: 4.3, status: "RETAINED" },
  { id: "SMP-20260702-01", dish: "Thịt kho trứng cút", meal: "Trưa 02/07", mealShift: "Bữa trưa — 10:45", servings: "1850", weight: "120 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "02/07/2026 10:15", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 2", retentionDeadline: "03/07/2026 10:15", hoursRemaining: 20, fridgeTemp: 4.2, status: "RETAINED" },
  { id: "SMP-20260702-02", dish: "Canh bí đỏ nấu tôm", meal: "Trưa 02/07", mealShift: "Bữa trưa — 10:45", servings: "1850", weight: "180 ml", container: "Hộp thủy tinh có nắp", sanitized: true, sealed: true, collectedAt: "02/07/2026 10:18", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 2", retentionDeadline: "03/07/2026 10:18", hoursRemaining: 20, fridgeTemp: 4.2, status: "RETAINED" },
  { id: "SMP-20260701-01", dish: "Cá basa sốt cà chua", meal: "Trưa 01/07", mealShift: "Bữa trưa — 10:45", servings: "1841", weight: "110 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "01/07/2026 10:20", collectedBy: "Trần Văn Hùng", storageLocation: "Tủ lưu mẫu số 1 — ngăn 1", retentionDeadline: "02/07/2026 10:20", hoursRemaining: 6, fridgeTemp: 4.5, status: "RETAINED" },
  // 19/07/2026 (review foodsamples Vòng 1/2): mẫu đã gửi lab dùng SENT_TO_LAB — trạng thái
  // CUỐI tại trường, gắn labRequestId. 26/07/2026 (Vòng 5): người gửi + biên bản giao nhận
  // do chính Medical Staff ghi, không còn qua Service Caterers.
  // Hai món còn lại của bữa trưa 30/06 — bữa nào cũng phải lưu mẫu ĐỦ MÓN
  // (Điều 1.2 QĐ 1246). Seed cũ chỉ lưu món nghi ngờ, nên khi Y tế muốn loại trừ
  // các món khác thì không có mẫu nào để gửi. Hai mẫu này giữ `RETAINED` để chạy thử
  // luồng "Thu hồi & gửi xét nghiệm" vào từ deep-link của ca INC-2026-014.
  { id: "SMP-20260630-01", dish: "Cơm trắng", meal: "Trưa 30/06", mealShift: "Bữa trưa — 10:45", servings: "1820", weight: "120 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "30/06/2026 10:20", collectedBy: "Trần Văn Hùng", storageLocation: "Tủ lưu mẫu số 1 — ngăn 3", retentionDeadline: "01/07/2026 10:20", hoursRemaining: 4, fridgeTemp: 4.1, status: "RETAINED", incidentId: "INC-2026-014" },
  { id: "SMP-20260630-02", dish: "Su su xào", meal: "Trưa 30/06", mealShift: "Bữa trưa — 10:45", servings: "1820", weight: "180 ml", container: "Hộp thủy tinh có nắp", sanitized: true, sealed: true, collectedAt: "30/06/2026 10:22", collectedBy: "Trần Văn Hùng", storageLocation: "Tủ lưu mẫu số 1 — ngăn 3", retentionDeadline: "01/07/2026 10:22", hoursRemaining: 4, fridgeTemp: 4.1, status: "RETAINED", incidentId: "INC-2026-014" },
  { id: "SMP-20260630-03", dish: "Gà rang gừng", meal: "Trưa 30/06", mealShift: "Bữa trưa — 10:45", servings: "1820", weight: "105 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "30/06/2026 10:25", collectedBy: "Trần Văn Hùng", storageLocation: "Tủ lưu mẫu số 1 — ngăn 3", retentionDeadline: "01/07/2026 10:25", hoursRemaining: 4, fridgeTemp: 4.1, status: "SENT_TO_LAB", incidentId: "INC-2026-014", labRequestId: "LABREQ-2026-001", labDestination: "CDC Hà Nội", testCriteria: "Salmonella, S.aureus, tổng vi sinh vật hiếu khí", retrievedAt: "30/06/2026 15:00", sentToLabAt: "30/06/2026 15:00", deliveredBy: "Ngô Thị Hà (Y tế)", labRepresentative: "Nguyễn Thị Vân (CDC Hà Nội)", sealPhotoFiles: [{ name: "niem_phong_smp_20260630_03.jpg", uploadedAt: "30/06/2026 14:40", size: "2.1 MB" }], handoverRecordFiles: [{ name: "bien_ban_giao_nhan_mau_cdc.pdf", uploadedAt: "30/06/2026 15:05", size: "640 KB" }], labReceiptFiles: [{ name: "phieu_nhan_mau_cdc.jpg", uploadedAt: "30/06/2026 15:10", size: "1.8 MB" }], disposalNote: "Đã gửi CDC Hà Nội 30/06 15:00 — sự cố INC-2026-014" },
  { id: "SMP-20260628-01", dish: "Thịt bò xào rau củ", meal: "Trưa 28/06", mealShift: "Bữa trưa — 10:45", servings: "1835", weight: "115 g", container: "Hộp inox có nắp kín", sanitized: true, sealed: true, collectedAt: "28/06/2026 10:30", collectedBy: "Lê Thị Mai", storageLocation: "Tủ lưu mẫu số 1 — ngăn 1", retentionDeadline: "29/06/2026 10:30", hoursRemaining: 0, fridgeTemp: 4.3, status: "DISPOSED", disposedAt: "01/07/2026 11:00", disposedBy: "Lê Thị Mai", disposalNote: "Mẫu đạt, không phát sinh sự cố — hủy sau khi hết hạn lưu 24h (BR-111), nhân chứng Ngô Thị Hà" },
];

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
export const INCIDENT_RECORDS: IncidentRecordDraft[] = [
  { id: "REC-2026-021", sourceReportIds: ["RPT-2026-030"], content: "Theo báo cáo RPT-2026-030 của Phan Thị Hồng (GV 3B) lúc 30/06/2026 13:50: 2 HS lớp 3B có triệu chứng tương tự sau bữa trưa.<br>Ghi nhận thêm 2 HS lớp 3A cùng triệu chứng — nghi liên quan món Gà rang gừng bữa trưa 30/06.", status: "OPENED", createdBy: "Ngô Thị Hà (Y tế)", createdAt: "30/06/2026 14:10", incidentId: "INC-2026-014", location: "Nhà ăn tầng 1 — khu lớp 3", occurredAt: "2026-06-30T11:15" },
  { id: "REC-2026-019", sourceReportIds: ["RPT-2026-024"], content: "Theo báo cáo RPT-2026-024 của Nguyễn Thu Trang (GV 3A) lúc 24/06/2026 11:20: phát hiện dị vật kim loại nhỏ trong khay canh rau ngót lớp 3A; đã ngừng phục vụ phần canh.", status: "OPENED", createdBy: "Ngô Thị Hà (Y tế)", createdAt: "24/06/2026 11:40", incidentId: "INC-2026-013", location: "Nhà ăn tầng 1 — khu lớp 3", occurredAt: "2026-06-24T11:10" },
  { id: "REC-2026-018", sourceReportIds: ["RPT-2026-018"], content: "Theo báo cáo RPT-2026-018 của Phan Thị Hồng (GV 3B) lúc 18/06/2026 12:05: 2 HS nổi mẩn, ngứa sau bữa trưa có món chế biến từ tôm — nghi dị ứng.", status: "OPENED", createdBy: "Ngô Thị Hà (Y tế)", createdAt: "18/06/2026 12:20", incidentId: "INC-2026-012", location: "Phòng y tế", occurredAt: "2026-06-18T11:45" },
];

export const SOURCE_REPORTS: IncidentSourceReport[] = [
  { id: "RPT-2026-033", reportedBy: "Đào Văn Quân (GV 4A)", reportedAt: "02/07/2026 15:40", className: "4A", affectedStudents: [{ studentId: "HS-4A-002", studentName: "Tạ Đức Thịnh", className: "4A" }], symptoms: ["Đau bụng"], suspectedMealText: "Sữa chua bữa phụ", mealDate: "02/07/2026", description: "1 HS kêu đau bụng nhẹ sau bữa phụ, đã cho nghỉ tại lớp.", evidencePhotos: 0, status: "PENDING_TRIAGE" },
  // Mục 8.3 bug 2 — RPT lịch sử (MERGED) làm nguồn cho biên bản gốc của INC-2026-013/012
  { id: "RPT-2026-024", reportedBy: "Nguyễn Thu Trang (GV 3A)", reportedAt: "24/06/2026 11:20", className: "3A", affectedStudents: [], symptoms: [], suspectedMealText: "Canh rau ngót (dị vật)", mealDate: "24/06/2026", description: "Phát hiện dị vật kim loại nhỏ trong khay canh lớp 3A.", evidencePhotos: 1, status: "MERGED", mergedIntoIncidentId: "INC-2026-013" },
  { id: "RPT-2026-018", reportedBy: "Phan Thị Hồng (GV 3B)", reportedAt: "18/06/2026 12:05", className: "3B", affectedStudents: [{ studentId: "HS-3B-001", studentName: "Trịnh Bảo Ngọc", className: "3B" }, { studentId: "HS-3B-002", studentName: "Lý Gia Bảo", className: "3B" }], symptoms: ["Nổi mẩn", "Ngứa"], suspectedMealText: "Món chế biến từ tôm", mealDate: "18/06/2026", description: "2 HS nổi mẩn, ngứa sau bữa trưa — nghi dị ứng tôm.", evidencePhotos: 0, status: "MERGED", mergedIntoIncidentId: "INC-2026-012" },
  { id: "RPT-2026-032", reportedBy: "Trần Quốc Đạt (PH, 3B)", reportedAt: "30/06/2026 19:05", className: "3B", affectedStudents: [{ studentId: "HS-3B-001", studentName: "Trịnh Bảo Ngọc", className: "3B" }], symptoms: ["Buồn nôn"], suspectedMealText: "Gà rang gừng bữa trưa", mealDate: "30/06/2026", description: "Con về nhà kêu buồn nôn, nôn 1 lần sau bữa trưa ở trường.", evidencePhotos: 1, status: "PENDING_TRIAGE" },
  { id: "RPT-2026-031", reportedBy: "Nguyễn Thu Trang (GV 3A)", reportedAt: "30/06/2026 14:05", className: "3A", affectedStudents: [{ studentId: "HS-3A-001", studentName: "Nguyễn Minh Khang", className: "3A" }, { studentId: "HS-3A-004", studentName: "Phạm Đức Minh", className: "3A" }], symptoms: ["Đau bụng", "Buồn nôn"], suspectedMealText: "Gà rang gừng — bữa trưa", mealDate: "30/06/2026", description: "2 HS đau bụng, buồn nôn ~1 giờ sau bữa trưa; đã đưa xuống phòng y tế.", evidencePhotos: 2, severityLevel: "HIGH", status: "MERGED", mergedIntoIncidentId: "INC-2026-014" },
  { id: "RPT-2026-030", reportedBy: "Phan Thị Hồng (GV 3B)", reportedAt: "30/06/2026 13:50", className: "3B", affectedStudents: [{ studentId: "HS-3B-002", studentName: "Lý Gia Bảo", className: "3B" }, { studentId: "HS-3B-001", studentName: "Trịnh Bảo Ngọc", className: "3B" }], symptoms: ["Đau bụng", "Buồn nôn"], suspectedMealText: "Gà rang gừng — bữa trưa", mealDate: "30/06/2026", description: "2 HS lớp 3B có triệu chứng tương tự sau bữa trưa.", evidencePhotos: 1, severityLevel: "HIGH", status: "MERGED", mergedIntoIncidentId: "INC-2026-014" },
  { id: "RPT-2026-029", reportedBy: "Nguyễn Thu Trang (GV 3A)", reportedAt: "28/06/2026 09:30", className: "3A", affectedStudents: [{ studentId: "HS-3A-002", studentName: "Trần Bảo An", className: "3A" }], symptoms: ["Sốt"], suspectedMealText: "—", mealDate: "27/06/2026", description: "HS sốt từ tối hôm trước, phụ huynh xác nhận cả nhà cùng bị cúm.", evidencePhotos: 0, status: "CLOSED_NOT_FOOD_SAFETY" },
];

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

export const INCIDENTS: Incident[] = [
  // Resolve merge (21/07/2026): giữ linkage đầy đủ của nhánh medical-staff
  // (sourceReportIds → RPT, mealDate, investigationRootCause) + lấy quarantineBatches
  // đúng từ nhánh service-caterers: BATCH-MP-8820 = "Thịt gà ta" (QUARANTINED), khớp
  // chuỗi truy xuất TXN-20260630-01 (xuất kho, dishName "Gà rang gừng") →
  // PREP-20260630-01 (batchesUsed) → INSP-20260630-L (nhiệt lõi 68°C < 75°C).
  // BATCH-MP-8830 là "Thịt lợn nạc vai" (EXPIRING_SOON) — sai nguyên liệu cho món gà.
  // `occurredAt` (thêm 27/07/2026, Mục 13.4): suy từ chính `mealTime` đã ghi sẵn của mỗi
  // ca — không bịa số liệu mới, chỉ đưa mốc "lúc xảy ra" ra đúng field máy đọc được, thay
  // vì để cửa sổ điều tra 24h phải lùi về `reportedAt` (lúc lập biên bản).
  { id: "INC-2026-014", occurredAt: "30/06/2026 11:15", title: "Nghi ngộ độc sau bữa trưa 30/06", reportedAt: "30/06/2026 14:20", reportedBy: "Ngô Thị Hà (Y tế) — gộp từ RPT-2026-030", affectedStudents: [{ studentId: "HS-3B-002", studentName: "Lý Gia Bảo", className: "3B" }, { studentId: "HS-3B-001", studentName: "Trịnh Bảo Ngọc", className: "3B" }, { studentId: "HS-3A-001", studentName: "Nguyễn Minh Khang", className: "3A" }, { studentId: "HS-3A-004", studentName: "Phạm Đức Minh", className: "3A" }], severity: "HIGH", status: "CAPA_APPROVED", symptoms: ["Đau bụng", "Buồn nôn"], suspectedDish: "Gà rang gừng — bữa trưa 30/06/2026", mealTime: "Bữa trưa 30/06/2026, 11:15", firstAid: "Cho uống oresol, theo dõi tại phòng y tế 60 phút; 1 HS chuyển trạm y tế phường.", investigationEvidence: [{ type: "Kết quả thu hồi mẫu", description: "Mẫu SMP-20260630-03 gửi CDC Hà Nội (BR-113)", files: 1 }], investigationRootCause: "Sơ bộ: nghi lô gà (BATCH-MP-8820) không đạt nhiệt độ lõi khi chế biến (68°C < 75°C, INSP-20260630-L); chờ kết quả nuôi cấy CDC để xác nhận tác nhân.", investigationRootCauseStatus: "PRELIMINARY", sourceReportIds: ["RPT-2026-030", "RPT-2026-031"], suspectedMeal: "Gà rang gừng — bữa trưa 30/06/2026", mealDate: "30/06/2026", firstAidNotes: "Cho uống oresol, theo dõi tại phòng y tế 60 phút; 1 HS chuyển trạm y tế phường.", suspectedDishIds: ["Gà rang gừng"], quarantineBatches: ["BATCH-MP-8820"], traceabilityMapStatus: "READY" },
  { id: "INC-2026-013", occurredAt: "24/06/2026 11:10", title: "Phát hiện dị vật trong canh lớp 3A", reportedAt: "24/06/2026 11:45", reportedBy: "Ngô Thị Hà (Y tế) — biên bản REC-2026-019 (RPT-2026-024)", affectedStudents: [], severity: "MEDIUM", status: "CAPA_APPROVED", symptoms: [], suspectedDish: "Canh rau ngót (dị vật kim loại nhỏ)", mealTime: "Bữa trưa 24/06/2026, 11:10", firstAid: "Ngừng phục vụ phần canh, thu hồi khay còn lại; không HS bị thương", sourceReportIds: ["RPT-2026-024"], mealDate: "24/06/2026" },
  { id: "INC-2026-012", occurredAt: "18/06/2026 11:20", title: "2 học sinh nổi mẩn nghi dị ứng tôm", reportedAt: "18/06/2026 12:30", reportedBy: "Ngô Thị Hà (Y tế) — biên bản REC-2026-018 (RPT-2026-018)", affectedStudents: [{ studentId: "HS-3B-001", studentName: "Trịnh Bảo Ngọc", className: "3B" }, { studentId: "HS-3B-002", studentName: "Lý Gia Bảo", className: "3B" }], severity: "MEDIUM", status: "CLOSED", symptoms: ["Nổi mẩn", "Ngứa"], suspectedDish: "Món chế biến từ tôm", mealTime: "Bữa trưa 18/06/2026, 11:20", firstAid: "Dùng kháng histamin, theo dõi 1 giờ, hồi phục", sourceReportIds: ["RPT-2026-018"], mealDate: "18/06/2026" },
];

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

export const STUDENTS: Student[] = [
  { id: "HS-3A-001", name: "Nguyễn Minh Khang", class: "3A", allergies: ["Tôm", "Cua"], healthStatus: "VERIFIED", parent: "Nguyễn Văn Bình", allergySeverity: "Severe", medicalConditions: "Hen phế quản nhẹ, dị ứng hải sản có tiền sử nổi mề đay", medications: "Ventolin xịt (khi lên cơn hen); kháng histamin Loratadin 5mg khi dị ứng", emergencyContact: "Nguyễn Văn Bình (bố) — 0912 345 678", medicalDocuments: ["don-bac-si-di-ung-tom-cua.pdf", "ket-qua-test-lay-da.jpg"] },
  { id: "HS-3A-002", name: "Trần Bảo An", class: "3A", allergies: [], healthStatus: "VERIFIED", parent: "Trần Quốc Đạt", medicalConditions: "Khỏe mạnh, không bệnh nền", emergencyContact: "Trần Quốc Đạt (bố) — 0987 222 111" },
  { id: "HS-3A-003", name: "Lê Gia Hân", class: "3A", allergies: ["Đậu phộng"], specialDiet: "Nut-Free", healthStatus: "VERIFIED", parent: "Lê Hoàng Nam", allergySeverity: "Severe", medicalConditions: "Dị ứng đậu phộng mức phản vệ độ 1", medications: "EpiPen Jr. dự phòng tại phòng y tế", emergencyContact: "Lê Hoàng Nam (bố) — 0903 888 222", medicalDocuments: ["don-bac-si-nhi-trung-uong.pdf"] },
  { id: "HS-3A-004", name: "Phạm Đức Minh", class: "3A", allergies: [], specialDiet: "Vegetarian", healthStatus: "PENDING", parent: "Phạm Văn Cường", emergencyContact: "Phạm Văn Cường (bố) — 0977 665 544" },
  { id: "HS-3A-005", name: "Vũ Ngọc Diệp", class: "3A", allergies: ["Sữa bò"], specialDiet: "Lactose-Free", healthStatus: "NEEDS_INFO", parent: "Vũ Đình Phong", allergySeverity: "Moderate", medicalConditions: "Không dung nạp lactose — chờ giấy xác nhận của bác sĩ", emergencyContact: "Vũ Đình Phong (bố) — 0966 123 987" },
  // 22/07/2026: khai chế độ ăn tiểu đường — MV-001 (suất riêng nhóm DIABETIC của
  // Khối 3) vốn không có học sinh nào ăn, tức suất nấu cho người không tồn tại.
  { id: "HS-3A-006", name: "Hoàng Tuấn Kiệt", class: "3A", allergies: [], specialDiet: "Diabetic", healthStatus: "VERIFIED", parent: "Hoàng Văn Long", medicalConditions: "Tiểu đường type 1 — có giấy xác nhận của Bệnh viện Nhi Trung ương" },
  { id: "HS-3A-007", name: "Đặng Thùy Linh", class: "3A", allergies: [], healthStatus: "VERIFIED", parent: "Đặng Quốc Huy" },
  { id: "HS-3A-008", name: "Bùi Nhật Nam", class: "3A", allergies: [], healthStatus: "VERIFIED", parent: "Bùi Văn Thắng" },
  { id: "HS-3A-009", name: "Ngô Khánh Vy", class: "3A", allergies: ["Trứng"], healthStatus: "VERIFIED", parent: "Ngô Đức Anh" },
  { id: "HS-3A-010", name: "Đỗ Quang Huy", class: "3A", allergies: [], healthStatus: "VERIFIED", parent: "Đỗ Mạnh Hùng" },
  // Con thứ hai của phụ huynh Nguyễn Văn Bình (xem PARENT_CHILDREN). Trước
  // 22/07/2026 em này CHỈ tồn tại trong danh sách con của phụ huynh, không có hồ
  // sơ học sinh: phụ huynh đổi sang con thứ hai là mọi màn dẫn xuất (dị ứng, suất
  // riêng, lịch sử phát suất) rỗng trắng vì tra `studentId` không ra gì.
  { id: "HS-1B-007", name: "Nguyễn Minh Anh", class: "1B", allergies: [], healthStatus: "VERIFIED", parent: "Nguyễn Văn Bình", emergencyContact: "Nguyễn Văn Bình (bố) — 0912 345 678" },
  { id: "HS-3B-001", name: "Trịnh Bảo Ngọc", class: "3B", allergies: ["Hải sản"], healthStatus: "VERIFIED", parent: "Trịnh Văn Sơn" },
  { id: "HS-3B-002", name: "Lý Gia Bảo", class: "3B", allergies: [], healthStatus: "VERIFIED", parent: "Lý Thành Trung" },
  { id: "HS-3B-003", name: "Cao Minh Anh", class: "3B", allergies: [], specialDiet: "Halal", healthStatus: "VERIFIED", parent: "Cao Văn Đại" },
  { id: "HS-4A-001", name: "Vương Hà My", class: "4A", allergies: ["Đậu nành"], healthStatus: "PENDING", parent: "Vương Đình Khoa" },
  { id: "HS-4A-002", name: "Tạ Đức Thịnh", class: "4A", allergies: [], healthStatus: "VERIFIED", parent: "Tạ Văn Lâm" },
  { id: "HS-4A-003", name: "Mai Phương Thảo", class: "4A", allergies: ["Sữa bò", "Trứng"], specialDiet: "Lactose-Free", healthStatus: "VERIFIED", parent: "Mai Xuân Bắc" },
  { id: "HS-5A-001", name: "Hồ Tuấn Phong", class: "5A", allergies: [], healthStatus: "VERIFIED", parent: "Hồ Văn Tiến" },
  { id: "HS-5A-002", name: "Chu Ngọc Hân", class: "5A", allergies: ["Đậu phộng"], specialDiet: "Nut-Free", healthStatus: "VERIFIED", parent: "Chu Đức Long" },
];

// ===== Nhà cung cấp / Đối tác (MF05, BR-040..BR-060) =====

export interface PartnerDocument {
  name: string;
  number: string;
  expiry: string;
  status: keyof typeof STATUS_BADGE; // VALID | EXPIRING | URGENT (BR-047)
}

export interface Partner {
  id: string;
  name: string;
  type: "Raw Food Supplier" | "Service Caterer";
  status: keyof typeof STATUS_BADGE;
  certExpiry: string;
  certStatus: keyof typeof STATUS_BADGE;
  performanceScore: number;
  appliedAt: string;
  /** 04/05_Detail + 38_VerificationDocuments — hồ sơ xác minh (BR-047) */
  taxId: string;
  address: string;
  contact: string;
  /** Điểm khảo sát cơ sở — chỉ áp dụng Service Caterer (UC-108) */
  siteInspectionScore?: number;
  /** Lý do bị từ chối hồ sơ (status = REJECTED). */
  rejectReason?: string;
  /**
   * Lý do đình chỉ (status = SUSPENDED). Đình chỉ là quyết định có hệ quả — đối tác
   * mất quyền nhận đơn hàng — nên phải ghi vì sao, không để trạng thái trần.
   */
  suspendReason?: string;
  documents: PartnerDocument[];
}

export const PARTNERS: Partner[] = [
  { id: "SUP-001", name: "Cty CP Thực phẩm sạch GreenFarm", type: "Raw Food Supplier", status: "APPROVED", certExpiry: "15/03/2027", certStatus: "VALID", performanceScore: 94, appliedAt: "10/01/2026", taxId: "0107712345", address: "KCN Phú Nghĩa, Chương Mỹ, Hà Nội", contact: "Lý Thái Sơn — 0913 222 333", documents: [{ name: "Giấy chứng nhận ATTP", number: "ATTP-2025-1123", expiry: "15/03/2027", status: "VALID" }, { name: "Giấy ĐKKD", number: "0107712345", expiry: "—", status: "VALID" }, { name: "Chứng nhận VietGAP", number: "VG-2024-3341", expiry: "10/10/2026", status: "VALID" }] },
  { id: "SUP-002", name: "HTX Rau an toàn Vân Nội", type: "Raw Food Supplier", status: "APPROVED", certExpiry: "20/11/2026", certStatus: "EXPIRING", performanceScore: 88, appliedAt: "12/01/2026", taxId: "0106654321", address: "Vân Nội, Đông Anh, Hà Nội", contact: "Nguyễn Thị Lụa — 0977 444 555", documents: [{ name: "Giấy chứng nhận ATTP", number: "ATTP-2024-0871", expiry: "20/11/2026", status: "EXPIRING" }, { name: "Giấy ĐKKD", number: "0106654321", expiry: "—", status: "VALID" }] },
  { id: "SUP-003", name: "Cty TNHH Thịt sạch Minh Phát", type: "Raw Food Supplier", status: "APPROVED", certExpiry: "05/08/2026", certStatus: "EXPIRING", performanceScore: 91, appliedAt: "15/01/2026", taxId: "0105543210", address: "Thanh Trì, Hà Nội", contact: "Đỗ Minh Phát — 0908 666 777", documents: [{ name: "Giấy chứng nhận ATTP", number: "ATTP-2024-0512", expiry: "05/08/2026", status: "EXPIRING" }, { name: "Giấy ĐKKD", number: "0105543210", expiry: "—", status: "VALID" }, { name: "Chứng nhận thú y", number: "TY-2025-118", expiry: "01/02/2027", status: "VALID" }] },
  { id: "SUP-004", name: "Cty CP Hải sản Đại Dương", type: "Raw Food Supplier", status: "SUSPENDED", suspendReason: "Lô tôm BATCH-DD-1092 không đạt kiểm tra tiếp nhận 30/06 (phiếu RCV-20260630-02 bị từ chối); chứng nhận ATTP hết hiệu lực 22/06/2026.", certExpiry: "22/06/2026", certStatus: "URGENT", performanceScore: 62, appliedAt: "03/02/2026", taxId: "0104432109", address: "Hải An, Hải Phòng", contact: "Đặng Hải Yến — 0918 888 999", documents: [{ name: "Giấy chứng nhận ATTP", number: "ATTP-2023-0334", expiry: "22/06/2026", status: "URGENT" }, { name: "Giấy ĐKKD", number: "0104432109", expiry: "—", status: "VALID" }] },
  { id: "CAT-001", name: "Cty TNHH Suất ăn An Bình", type: "Service Caterer", status: "APPROVED", certExpiry: "30/01/2028", certStatus: "VALID", performanceScore: 96, appliedAt: "05/01/2026", taxId: "0103321098", address: "Nam Từ Liêm, Hà Nội", contact: "Trần Văn Hùng — 0904 123 456", siteInspectionScore: 92, documents: [{ name: "Giấy chứng nhận ATTP", number: "ATTP-2026-0089", expiry: "30/01/2028", status: "VALID" }, { name: "Giấy ĐKKD", number: "0103321098", expiry: "—", status: "VALID" }, { name: "Hồ sơ khám sức khỏe NV bếp", number: "KSK-2026-T6", expiry: "31/12/2026", status: "VALID" }] },
  { id: "SUP-005", name: "Cty TNHH Nông sản Tây Bắc", type: "Raw Food Supplier", status: "PENDING", certExpiry: "18/09/2027", certStatus: "VALID", performanceScore: 0, appliedAt: "28/06/2026", taxId: "0109210987", address: "Mộc Châu, Sơn La", contact: "Vàng A Chứ — 0966 010 203", documents: [{ name: "Giấy chứng nhận ATTP", number: "ATTP-2026-0412", expiry: "18/09/2027", status: "VALID" }, { name: "Giấy ĐKKD", number: "0109210987", expiry: "—", status: "VALID" }, { name: "Chứng nhận nguồn gốc nông sản", number: "NG-2026-77", expiry: "18/09/2027", status: "VALID" }] },
];

// ===== Đơn mua hàng & giao hàng (Supplier) =====

export interface PurchaseItem {
  name: string;
  quantity: string;
  note?: string;
  /** Đơn giá + thành tiền — có ở đơn seed (PURCHASE_REQUEST_ITEMS); đơn tạo mới (itemsDetail) có thể bỏ trống. */
  unitPrice?: string;
  lineTotal?: string;
}

export interface PurchaseRequest {
  id: string;
  school: string;
  /** Nhà cung cấp nhận đơn — SC chọn khi đặt hàng (nhãn hiển thị). */
  supplier?: string;
  /**
   * Khoá về `PARTNERS`. Trước 22/07/2026 đơn hàng chỉ giữ TÊN nhà cung cấp, nên
   * mọi màn phải dò `PARTNERS.find(p => p.name === pr.supplier)` — đổi tên đối tác
   * một chữ là đơn cũ mất liên kết, và không kiểm được đối tác còn hợp tác hay không.
   */
  supplierId?: string;
  /** Source of request — Screen Spec 10 (UC-139), VD "Bếp trưởng". */
  requestedBy?: string;
  requestedDate: string;
  deliveryDate: string;
  items: number;
  totalValue: string;
  status: keyof typeof STATUS_BADGE;
  /** Dòng nguyên liệu — đơn tạo mới lưu trực tiếp tại đây (đơn seed cũ dùng PURCHASE_REQUEST_ITEMS). */
  itemsDetail?: PurchaseItem[];
  /** NCC gán lô hàng cho từng nguyên liệu khi xác nhận đơn (BR-071): tên nguyên liệu → batchId. */
  batchAssignments?: Record<string, string>;
  /**
   * NGUỒN PHÁT SINH (Đợt E, 21/07/2026) — đơn tạo từ cảnh báo thiếu hàng của một
   * buổi ăn cụ thể. Có 3 field này mới trả lời được "đơn này đặt vì thực đơn nào,
   * phục vụ ngày nào"; đơn đặt chủ động để trống.
   */
  sourceMenuId?: string;
  sourceMealType?: MealType;
  /** Ngày phục vụ của thực đơn nguồn (yyyy-mm-dd). */
  serviceDate?: string;
  /** Thời điểm chấp nhận đơn — UC-015. */
  acceptedAt?: string;
  /** Ngày giờ giao đã xác nhận khi chấp nhận đơn — UC-015. */
  confirmedDeliveryAt?: string;
  /** Thời điểm từ chối đơn. */
  rejectedAt?: string;
  // ===== Duyệt giá 1 vòng (review 25/07/2026 — giá theo HỢP ĐỒNG, không đàm phán):
  // PENDING → QUOTED (NCC gửi giá tự tính từ bảng giá SUPPLIER_PRODUCTS) → SC
  // Chấp nhận (PRICE_ACCEPTED) hoặc Từ chối đơn (CANCELLED). Không có báo giá lại.
  /** Thời điểm NCC gửi giá theo hợp đồng. */
  quotedAt?: string;
  /** Ghi chú NCC kèm theo khi gửi giá. */
  quoteNote?: string;
  /** Thời điểm SC chấp nhận giá — mở bước NCC xác nhận lịch giao (UC-015). */
  priceAcceptedAt?: string;
  /** SC từ chối giá (trường hợp bất lợi) → đơn CANCELLED. Lý do bắt buộc. */
  cancelReason?: string;
  /** Thời điểm SC từ chối đơn. */
  cancelledAt?: string;
}

export const PURCHASE_REQUESTS: PurchaseRequest[] = [
  { id: "PR-2026-0815", school: "TH Đoàn Thị Điểm", supplier: "Cty CP Thực phẩm sạch GreenFarm", requestedBy: "Bếp trưởng — Cty Suất ăn An Bình", requestedDate: "01/07/2026", deliveryDate: "02/07/2026", items: 3, totalValue: "9.800.000 ₫", status: "APPROVED", itemsDetail: [{ name: "Thịt lợn nạc vai", quantity: "45 kg", note: "Giao trước 06:00" }, { name: "Rau cải ngọt", quantity: "30 kg" }, { name: "Gạo tám thơm", quantity: "100 kg" }], batchAssignments: { "Thịt lợn nạc vai": "BATCH-GF-4411", "Rau cải ngọt": "BATCH-GF-4412", "Gạo tám thơm": "BATCH-GF-4410" } },
  { id: "PR-2026-0812", school: "TH Đoàn Thị Điểm", supplier: "Cty CP Thực phẩm sạch GreenFarm", requestedBy: "Bếp trưởng — Cty Suất ăn An Bình", requestedDate: "01/07/2026", deliveryDate: "03/07/2026", items: 4, totalValue: "Chờ NCC gửi giá", status: "PENDING" },
  { id: "PR-2026-0809", school: "TH Đoàn Thị Điểm", supplier: "Cty CP Thực phẩm sạch GreenFarm", requestedBy: "Bếp trưởng — Cty Suất ăn An Bình", requestedDate: "29/06/2026", deliveryDate: "01/07/2026", items: 12, totalValue: "11.200.000 ₫", status: "APPROVED" },
  { id: "PR-2026-0805", school: "TH Nguyễn Siêu", supplier: "Cty CP Thực phẩm sạch GreenFarm", requestedBy: "Bếp trưởng — Cty Suất ăn Hoàn Cầu", requestedDate: "28/06/2026", deliveryDate: "30/06/2026", items: 9, totalValue: "8.750.000 ₫", status: "COMPLETED" },
  { id: "PR-2026-0801", school: "TH Đoàn Thị Điểm", supplier: "Cty TNHH Thịt sạch Minh Phát", requestedBy: "Bếp trưởng — Cty Suất ăn An Bình", requestedDate: "26/06/2026", deliveryDate: "28/06/2026", items: 15, totalValue: "13.100.000 ₫", status: "COMPLETED" },
];

/** Danh sách nguyên liệu của từng đơn — Screen Spec 11 (Purchase Request Detail, UC-139).
 *  Review 25/07/2026: đơn giá lấy TỰ ĐỘNG từ bảng giá hợp đồng (SUPPLIER_PRODUCTS)
 *  khi NCC gửi giá — đơn PENDING chưa có giá; đơn đã gửi giá/hoàn tất mới có. */
export const PURCHASE_REQUEST_ITEMS: Record<
  string,
  { name: string; quantity: string; unitPrice?: string; lineTotal?: string; note?: string }[]
> = {
  // PR-2026-0812 đang PENDING — chờ NCC báo giá từng nguyên liệu (chưa có đơn giá)
  "PR-2026-0812": [
    { name: "Thịt lợn nạc vai", quantity: "45 kg", note: "Giao trước 06:00" },
    { name: "Rau cải ngọt", quantity: "30 kg" },
    { name: "Gạo tám thơm", quantity: "100 kg" },
    { name: "Bí đỏ", quantity: "25 kg" },
  ],
  "PR-2026-0809": [
    { name: "Thịt gà ta", quantity: "30 kg", unitPrice: "145.000 ₫/kg", lineTotal: "4.350.000 ₫" },
    { name: "Rau muống", quantity: "25 kg", unitPrice: "15.000 ₫/kg", lineTotal: "375.000 ₫" },
    { name: "Dầu ăn thực vật", quantity: "20 lít", unitPrice: "52.000 ₫/lít", lineTotal: "1.040.000 ₫" },
  ],
  "PR-2026-0805": [
    { name: "Gạo tám thơm", quantity: "80 kg", unitPrice: "28.000 ₫/kg", lineTotal: "2.240.000 ₫" },
    { name: "Trứng gà", quantity: "600 quả", unitPrice: "3.800 ₫/quả", lineTotal: "2.280.000 ₫" },
  ],
  "PR-2026-0801": [
    { name: "Thịt lợn nạc vai", quantity: "40 kg", unitPrice: "125.000 ₫/kg", lineTotal: "5.000.000 ₫" },
    { name: "Rau cải ngọt", quantity: "28 kg", unitPrice: "22.000 ₫/kg", lineTotal: "616.000 ₫" },
    { name: "Cà rốt", quantity: "20 kg", unitPrice: "19.000 ₫/kg", lineTotal: "380.000 ₫" },
  ],
};

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

export const DELIVERIES: Delivery[] = [
  { id: "DLV-2026-1105", school: "TH Đoàn Thị Điểm", scheduledAt: "02/07/2026 05:40", driver: "Đỗ Văn Tám", vehiclePlate: "29C-123.45", batches: 3, status: "DELIVERED", purchaseRequestId: "PR-2026-0815", linkedBatches: ["BATCH-GF-4411", "BATCH-GF-4412", "BATCH-GF-4410"], batchAssignments: { "Thịt lợn nạc vai": "BATCH-GF-4411", "Rau cải ngọt": "BATCH-GF-4412", "Gạo tám thơm": "BATCH-GF-4410" } },
  { id: "DLV-2026-1104", school: "TH Đoàn Thị Điểm", scheduledAt: "03/07/2026 05:30", driver: "Đỗ Văn Tám", vehiclePlate: "29C-123.45", batches: 3, status: "SCHEDULED", purchaseRequestId: "PR-2026-0812", linkedBatches: ["BATCH-GF-4412", "BATCH-GF-4411", "BATCH-GF-4410"] },
  { id: "DLV-2026-1101", school: "TH Đoàn Thị Điểm", scheduledAt: "01/07/2026 05:30", driver: "Đỗ Văn Tám", vehiclePlate: "29C-123.45", batches: 2, status: "DELIVERED", purchaseRequestId: "PR-2026-0809", linkedBatches: ["BATCH-GF-4409", "BATCH-GF-4410"] },
  { id: "DLV-2026-1098", school: "TH Nguyễn Siêu", scheduledAt: "30/06/2026 06:00", driver: "Bùi Quang Sáng", vehiclePlate: "29D-567.89", batches: 2, status: "DELIVERED", purchaseRequestId: "PR-2026-0805", linkedBatches: ["BATCH-GF-4405", "BATCH-GF-4406"] },
];

// ===== Raw Food Supplier (MF06) — lô xuất đi, nhân viên giao, tài liệu tuân thủ, hợp tác trường =====

/**
 * Lô nguyên liệu do NHÀ CUNG CẤP đăng ký trước khi giao (Screen Spec 12–17, UC-043/081/100/107).
 * Khác với InventoryBatch (kho của Service Caterer): lô supplier khai theo BR-071 —
 * số lô, NSX, ngày đóng gói, HSD, số lượng và chứng chỉ liên kết.
 */
/**
 * Nhà cung cấp đang đăng nhập ở workspace Supplier.
 *
 * Trước 22/07/2026 danh tính này chỉ tồn tại ngầm qua tiền tố mã (`BATCH-GF-*`,
 * `DOC-GF-*`) — không màn nào biết mình là ai, nên không lọc được sản phẩm/lô của
 * chính mình. Khai thẳng như `CURRENT_SCHOOL` phía trường.
 */
export const CURRENT_SUPPLIER_ID = "SUP-001";

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

export const SUPPLIER_BATCHES: SupplierBatch[] = [
  { id: "BATCH-GF-4412", productId: "SIP-070", lotNumber: "GF-RC-260702", manufacturingDate: "01/07/2026", packagingDate: "02/07/2026", expirationDate: "05/07/2026", quantity: "30 kg", certificates: ["DOC-GF-04"], status: "ACTIVE" },
  { id: "BATCH-GF-4411", productId: "SIP-072", lotNumber: "GF-TL-260702", manufacturingDate: "02/07/2026", packagingDate: "02/07/2026", expirationDate: "05/07/2026", quantity: "45 kg", certificates: ["DOC-GF-01"], status: "ACTIVE" },
  { id: "BATCH-GF-4410", productId: "SIP-001", lotNumber: "GF-GT-260620", manufacturingDate: "15/06/2026", packagingDate: "20/06/2026", expirationDate: "25/12/2026", quantity: "200 kg", certificates: ["DOC-GF-01", "DOC-GF-04"], status: "ACTIVE" },
  { id: "BATCH-GF-4409", productId: "SIP-071", lotNumber: "GF-RM-260630", manufacturingDate: "29/06/2026", packagingDate: "30/06/2026", expirationDate: "02/07/2026", quantity: "25 kg", certificates: ["DOC-GF-04"], status: "EXPIRED" },
  { id: "BATCH-GF-4388", productId: "SIP-007", lotNumber: "GF-DA-260615", manufacturingDate: "10/06/2026", packagingDate: "15/06/2026", expirationDate: "28/06/2026", quantity: "40 lít", certificates: ["DOC-GF-03"], status: "EXPIRED" },
  { id: "BATCH-GF-4406", productId: "SIP-073", lotNumber: "GF-TG-260628", manufacturingDate: "27/06/2026", packagingDate: "28/06/2026", expirationDate: "12/07/2026", quantity: "600 quả", certificates: ["DOC-GF-01"], status: "ACTIVE" },
  { id: "BATCH-GF-4405", productId: "SIP-001", lotNumber: "GF-GT-260610", manufacturingDate: "05/06/2026", packagingDate: "10/06/2026", expirationDate: "10/12/2026", quantity: "80 kg", certificates: ["DOC-GF-01"], status: "ACTIVE" },
];

/** Nhân viên giao hàng của supplier — BR-050 (đăng ký CCCD + biển số), BR-051 (sinh trắc học). */
export interface SupplierDeliveryPerson {
  id: string;
  fullName: string;
  nationalId: string;
  phone: string;
  vehiclePlate: string;
  biometricStatus: "REGISTERED" | "PENDING_REGISTRATION" | "NOT_REGISTERED";
  /** Ngày đăng ký sinh trắc học khuôn mặt (BR-051) — Review 07/2026. */
  biometricRegisteredAt?: string;
}

export const DELIVERY_PERSONS: SupplierDeliveryPerson[] = [
  { id: "DP-01", fullName: "Đỗ Văn Tám", nationalId: "0010859xxxxx", phone: "0912 345 678", vehiclePlate: "29C-123.45", biometricStatus: "REGISTERED", biometricRegisteredAt: "12/03/2026" },
  { id: "DP-02", fullName: "Bùi Quang Sáng", nationalId: "0010761xxxxx", phone: "0987 654 321", vehiclePlate: "29D-567.89", biometricStatus: "REGISTERED", biometricRegisteredAt: "25/04/2026" },
  { id: "DP-03", fullName: "Lê Thị Hòa", nationalId: "0010992xxxxx", phone: "0905 112 233", vehiclePlate: "29E-246.80", biometricStatus: "PENDING_REGISTRATION" },
];

/** Tài liệu tuân thủ của supplier — ComplianceDocument (class diagram), BR-046/047/048, UC-101/102/091/167. */
export interface SupplierDocument {
  id: string;
  name: string;
  documentType: "FOOD_SAFETY_CERT" | "PRODUCT_ORIGIN" | "BUSINESS_LICENSE" | "SELF_DECLARATION";
  number: string;
  issuedBy: string;
  issuedDate: string;
  expiryDate: string;
  status: keyof typeof STATUS_BADGE;
}

export const SUPPLIER_DOCUMENTS: SupplierDocument[] = [
  { id: "DOC-GF-01", name: "Giấy chứng nhận cơ sở đủ điều kiện ATTP", documentType: "FOOD_SAFETY_CERT", number: "ATTP-HN-2024-1182", issuedBy: "Chi cục ATVSTP Hà Nội", issuedDate: "15/03/2024", expiryDate: "15/03/2027", status: "VALID" },
  { id: "DOC-GF-02", name: "Giấy phép kinh doanh", documentType: "BUSINESS_LICENSE", number: "0109988776", issuedBy: "Sở KH&ĐT Hà Nội", issuedDate: "10/02/2020", expiryDate: "Không thời hạn", status: "VALID" },
  { id: "DOC-GF-03", name: "Số tự công bố — Dầu ăn thực vật", documentType: "SELF_DECLARATION", number: "08/GreenFarm/2026", issuedBy: "Tự công bố", issuedDate: "05/01/2026", expiryDate: "05/01/2029", status: "VALID" },
  { id: "DOC-GF-04", name: "Chứng nhận VietGAP — Rau củ", documentType: "PRODUCT_ORIGIN", number: "VG-2024-3341", issuedBy: "TT Chứng nhận VietCert", issuedDate: "20/11/2024", expiryDate: "28/07/2026", status: "EXPIRING" },
];

/** Quan hệ hợp tác với trường — Screen Spec 03 (BR-040/041, BR-046/047). */
export interface SupplierPartnership {
  id: string;
  school: string;
  deliveryAddress: string;
  since: string;
  status: keyof typeof STATUS_BADGE;
  docStatus: keyof typeof STATUS_BADGE;
}

export const SUPPLIER_PARTNERSHIPS: SupplierPartnership[] = [
  { id: "PTN-01", school: "TH Đoàn Thị Điểm", deliveryAddress: "Cổng phụ khu bếp — Mỹ Đình 2, Nam Từ Liêm, Hà Nội", since: "20/01/2026", status: "APPROVED", docStatus: "EXPIRING" },
  { id: "PTN-02", school: "TH Nguyễn Siêu", deliveryAddress: "Cổng sau — Trung Hòa, Cầu Giấy, Hà Nội", since: "05/02/2026", status: "APPROVED", docStatus: "EXPIRING" },
  { id: "PTN-03", school: "TH Nguyễn Bỉnh Khiêm", deliveryAddress: "—", since: "—", status: "PENDING", docStatus: "VALID" },
];

/** Lịch sử nộp/duyệt đơn hợp tác — Screen Spec 08 (UC-169/170, BR-041/BR-048). */
export interface PartnershipApplication {
  id: string;
  school: string;
  submittedAt: string;
  status: keyof typeof STATUS_BADGE;
  /** Bình luận/lý do từ School Administration — hiển thị khi Rejected/Action Required (BR-048). */
  comment?: string;
}

export const PARTNERSHIP_APPLICATIONS: PartnershipApplication[] = [
  { id: "APP-2026-04", school: "TH Nguyễn Bỉnh Khiêm", submittedAt: "28/06/2026", status: "PENDING" },
  { id: "APP-2026-03", school: "TH Nguyễn Siêu", submittedAt: "02/02/2026", status: "APPROVED" },
  { id: "APP-2026-02", school: "TH Đoàn Thị Điểm", submittedAt: "18/01/2026", status: "APPROVED" },
  { id: "APP-2026-01", school: "TH Đoàn Thị Điểm", submittedAt: "05/01/2026", status: "REJECTED", comment: "Chứng nhận ATTP đính kèm đã hết hạn — bổ sung bản còn hiệu lực trước khi nộp lại (BR-048)." },
];

// ===== Tài khoản người dùng (System Admin) =====

export interface UserAccount {
  id: string;
  name: string;
  email: string;
  phone?: string;
  role: string;
  school?: string;
  /**
   * ACTIVE · SUSPENDED · INACTIVE. Phân biệt hai trạng thái ngừng:
   *   - SUSPENDED: khoá TẠM, đi theo quyết định đình chỉ đối tác (`partnerId`) —
   *     gỡ đình chỉ thì tài khoản mở lại.
   *   - INACTIVE: ngừng hẳn do quản trị, mở lại phải thao tác riêng.
   * Trước 22/07/2026 hai giá trị này cùng có trong dữ liệu mà không gì phân biệt,
   * và SUSPENDED không có đường vào từ giao diện.
   */
  status: keyof typeof STATUS_BADGE;
  /** Tài khoản thuộc về đối tác nào — khoá tài khoản đi theo đình chỉ đối tác. */
  partnerId?: string;
  lastLogin: string;
}

export const USER_ACCOUNTS: UserAccount[] = [
  { id: "U-0001", name: "Phạm Quốc Toản", email: "toanpq@dtd.edu.vn", role: "School Administration", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "02/07/2026 07:45" },
  { id: "U-0002", name: "Ngô Thị Hà", email: "hant@dtd.edu.vn", role: "Medical Staff", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "02/07/2026 06:30" },
  { id: "U-0003", name: "Trần Văn Hùng", email: "hungtv@anbinhcatering.vn", role: "Service Caterers", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "02/07/2026 05:15" },
  { id: "U-0004", name: "Nguyễn Thu Trang", email: "trangnt@dtd.edu.vn", role: "Teacher", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 16:20" },
  { id: "U-0005", name: "Nguyễn Văn Bình", email: "binhnv.parent@gmail.com", role: "Parent", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 20:05" },
  { id: "U-0006", name: "Lý Thái Sơn", email: "sonlt@greenfarm.vn", role: "Raw Food Supplier", status: "ACTIVE", partnerId: "SUP-001", lastLogin: "30/06/2026 09:10" },
  { id: "U-0007", name: "Đặng Hải Yến", email: "yendh@daiduong.vn", role: "Raw Food Supplier", status: "SUSPENDED", partnerId: "SUP-004", lastLogin: "22/06/2026 08:40" },
  { id: "U-0008", name: "Lê Thị Mai", email: "maile@anbinhcatering.vn", role: "Service Caterers", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "02/07/2026 05:20" },
  { id: "U-0009", name: "Phan Thị Hồng", email: "hongpt@dtd.edu.vn", role: "Teacher", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 17:05" },
  { id: "U-0010", name: "Đào Văn Quân", email: "quandv@dtd.edu.vn", role: "Teacher", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 16:55" },
  { id: "U-0011", name: "Lưu Thị Bích", email: "bichlt@dtd.edu.vn", role: "Teacher", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "30/06/2026 15:40" },
  { id: "U-0012", name: "Trịnh Minh Đức", email: "ductm@dtd.edu.vn", role: "Teacher", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 16:30" },
  { id: "U-0013", name: "Trần Quốc Đạt", email: "dattq.parent@gmail.com", role: "Parent", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 21:10" },
  { id: "U-0014", name: "Lê Hoàng Nam", email: "namlh.parent@gmail.com", role: "Parent", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "02/07/2026 06:50" },
  { id: "U-0015", name: "Phạm Văn Cường", email: "cuongpv.parent@gmail.com", role: "Parent", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "29/06/2026 20:30" },
  { id: "U-0016", name: "Vũ Đình Phong", email: "phongvd.parent@gmail.com", role: "Parent", school: "TH Đoàn Thị Điểm", status: "INACTIVE", lastLogin: "18/06/2026 19:15" },
  { id: "U-0017", name: "Hoàng Văn Long", email: "longhv.parent@gmail.com", role: "Parent", school: "TH Đoàn Thị Điểm", status: "ACTIVE", lastLogin: "01/07/2026 22:00" },
  { id: "U-0018", name: "Nguyễn Hải Đăng", email: "dangnh@nguyensieu.edu.vn", role: "School Administration", school: "TH Nguyễn Siêu", status: "ACTIVE", lastLogin: "02/07/2026 08:05" },
  { id: "U-0019", name: "Bùi Thu Hằng", email: "hangbt@nguyensieu.edu.vn", role: "Medical Staff", school: "TH Nguyễn Siêu", status: "ACTIVE", lastLogin: "02/07/2026 07:15" },
  { id: "U-0020", name: "Đỗ Văn Tám", email: "tamdv@greenfarm.vn", role: "Raw Food Supplier", status: "ACTIVE", partnerId: "SUP-001", lastLogin: "01/07/2026 05:00" },
];

export interface SchoolRecord {
  id: string;
  name: string;
  address: string;
  students: number;
  mealsPerDay: number;
  status: keyof typeof STATUS_BADGE;
  registeredAt: string;
  /** UC-122/UC-095 — hồ sơ đầy đủ của trường (System Admin) */
  taxId: string;
  email: string;
  lat: number;
  lng: number;
  /** BR-028: khóa tọa độ sau khi trường ACTIVE; chỉ System Admin mở khóa kèm lý do */
  coordinateLocked: boolean;
  operatingHours: string;
  emergencyContact: string;
  classes: number;
  staff: number;
}

export const SCHOOLS: SchoolRecord[] = [
  { id: "SCH-HN-0042", name: "TH Đoàn Thị Điểm", address: "Mỹ Đình 2, Nam Từ Liêm, Hà Nội", students: 1920, mealsPerDay: 1850, status: "ACTIVE", registeredAt: "05/01/2026", taxId: "0101245678", email: "contact@dtd.edu.vn", lat: 21.0312, lng: 105.7638, coordinateLocked: true, operatingHours: "07:00 – 17:30 (T2–T6)", emergencyContact: "Phạm Quốc Toản — 0912 345 678", classes: 28, staff: 86 },
  { id: "SCH-HN-0051", name: "TH Nguyễn Siêu", address: "Trung Hòa, Cầu Giấy, Hà Nội", students: 1450, mealsPerDay: 1400, status: "ACTIVE", registeredAt: "12/01/2026", taxId: "0102387654", email: "info@nguyensieu.edu.vn", lat: 21.0102, lng: 105.7996, coordinateLocked: true, operatingHours: "07:15 – 17:00 (T2–T6)", emergencyContact: "Nguyễn Hải Đăng — 0987 654 321", classes: 22, staff: 64 },
  { id: "SCH-HCM-0107", name: "TH Nguyễn Bỉnh Khiêm", address: "Quận 1, TP.HCM", students: 1680, mealsPerDay: 1600, status: "ACTIVE", registeredAt: "20/01/2026", taxId: "0312456789", email: "vp@nbk.edu.vn", lat: 10.7815, lng: 106.6992, coordinateLocked: true, operatingHours: "07:00 – 17:00 (T2–T6)", emergencyContact: "Trần Hữu Nghĩa — 0903 111 222", classes: 25, staff: 71 },
  { id: "SCH-HN-0063", name: "TH Vinschool Times City", address: "Hai Bà Trưng, Hà Nội", students: 2100, mealsPerDay: 2050, status: "PENDING", registeredAt: "28/06/2026", taxId: "0109988776", email: "admin@vinschool-tc.edu.vn", lat: 20.9955, lng: 105.8683, coordinateLocked: false, operatingHours: "07:30 – 17:30 (T2–T6)", emergencyContact: "Lê Minh Châu — 0936 555 888", classes: 0, staff: 0 },
];

export interface AuditLog {
  id: string;
  timestamp: string;
  actor: string;
  /** UC-106: cột IP Address bắt buộc theo spec Audit Logs */
  ip?: string;
  action: string;
  resource: string;
  result: "SUCCESS" | "BLOCKED";
}

export const AUDIT_LOGS: AuditLog[] = [
  { id: "AUD-99120", timestamp: "02/07/2026 10:18:44", actor: "hungtv@anbinhcatering.vn", ip: "10.20.1.44", action: "CREATE FoodSample SMP-20260702-02", resource: "Food Sample Retention", result: "SUCCESS" },
  { id: "AUD-99119", timestamp: "02/07/2026 10:02:11", actor: "hungtv@anbinhcatering.vn", ip: "10.20.1.44", action: "SUBMIT Inspection Step 2 INSP-20260702-L", resource: "Three-Step Inspection", result: "SUCCESS" },
  { id: "AUD-99101", timestamp: "02/07/2026 07:46:02", actor: "toanpq@dtd.edu.vn", ip: "10.20.3.12", action: "LOGIN", resource: "Authentication", result: "SUCCESS" },
  { id: "AUD-99087", timestamp: "01/07/2026 22:14:37", actor: "unknown@mail.com", ip: "203.113.99.87", action: "LOGIN (5 lần thất bại — khóa 15 phút, BR-002)", resource: "Authentication", result: "BLOCKED" },
  { id: "AUD-99080", timestamp: "01/07/2026 16:40:20", actor: "system", ip: "127.0.0.1", action: "AUTO-SUSPEND Supplier SUP-004 (chứng nhận ATTP hết hạn)", resource: "Supplier Management", result: "SUCCESS" },
  { id: "AUD-99031", timestamp: "30/06/2026 14:21:05", actor: "hant@dtd.edu.vn", ip: "10.20.2.18", action: "CREATE Incident INC-2026-014 + QUARANTINE 2 batches (BR-149)", resource: "Incident Management", result: "SUCCESS" },
  { id: "AUD-99027", timestamp: "30/06/2026 11:02:48", actor: "hungtv@anbinhcatering.vn", ip: "10.20.1.44", action: "SUBMIT Inspection Step 2 INSP-20260630-L → FAILED (68°C < 75°C)", resource: "Three-Step Inspection", result: "SUCCESS" },
  { id: "AUD-99025", timestamp: "30/06/2026 10:44:12", actor: "system", ip: "127.0.0.1", action: "AUTO-CREATE CorrectiveActionRequest CAPA-2026-031 (BR-095)", resource: "CAPA", result: "SUCCESS" },
  { id: "AUD-99018", timestamp: "29/06/2026 16:20:33", actor: "toanpq@dtd.edu.vn", ip: "10.20.3.12", action: "APPROVE Menu MENU-2026-27 → PUBLISHED (MSG-040)", resource: "Menu Management", result: "SUCCESS" },
  { id: "AUD-99010", timestamp: "29/06/2026 10:31:27", actor: "maile@anbinhcatering.vn", ip: "10.20.1.45", action: "CREATE FoodSample SMP-20260629-01", resource: "Food Sample Retention", result: "SUCCESS" },
  { id: "AUD-98994", timestamp: "28/06/2026 22:41:09", actor: "hungtv@anbinhcatering.vn", ip: "10.20.1.44", action: "DISPOSE FoodSample SMP-20260625-02 (hết hạn lưu tối thiểu, có nhân chứng)", resource: "Food Sample Retention", result: "SUCCESS" },
  { id: "AUD-98987", timestamp: "28/06/2026 14:05:51", actor: "trangnt@dtd.edu.vn", ip: "10.20.4.22", action: "CONFIRM Distribution DST-20260628-3A (MSG-045)", resource: "Meal Service", result: "SUCCESS" },
  { id: "AUD-98970", timestamp: "27/06/2026 09:12:40", actor: "sonlt@greenfarm.vn", ip: "118.70.12.34", action: "UPLOAD Certificate VG-2024-3341 (VietGAP renewal draft)", resource: "Supplier Management", result: "SUCCESS" },
  { id: "AUD-98955", timestamp: "26/06/2026 08:03:14", actor: "admin@sfms.vn", ip: "10.10.0.2", action: "UNLOCK coordinateLocked SCH-HN-0051 (lý do: hiệu chỉnh sai số GPS ±40m)", resource: "School Management", result: "SUCCESS" },
  { id: "AUD-98940", timestamp: "25/06/2026 13:47:22", actor: "hungtv@anbinhcatering.vn", ip: "10.20.1.44", action: "ADJUST Inventory BATCH-GF-4388 -2kg (mã lý do: spoilage, ký: Lê Thị Mai)", resource: "Inventory", result: "SUCCESS" },
  { id: "AUD-98931", timestamp: "25/06/2026 06:18:55", actor: "tamdv@greenfarm.vn", ip: "113.190.55.21", action: "FACE-VERIFY tại cổng trường (BR-051)", resource: "Delivery", result: "SUCCESS" },
  { id: "AUD-98902", timestamp: "23/06/2026 21:34:08", actor: "phongvd.parent@gmail.com", ip: "171.244.10.66", action: "SUBMIT Feedback ngoài cửa sổ 24h (MSG-022)", resource: "Feedback", result: "BLOCKED" },
];

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


// ===== Hành động khắc phục (CAPA, BR-095/BR-152) =====

export interface CorrectiveAction {
  id: string;
  /** Nhãn hiển thị nguồn phát sinh — CHỈ để đọc, không dùng làm khoá. */
  source: string;
  /**
   * Nguồn phát sinh dạng KHOÁ. CAPA có đúng 2 đường sinh:
   *   - sự cố (BR-152: sau mỗi sự cố phải có CAPA)
   *   - kiểm thực ba bước không đạt (BR-095: bước kiểm tra thất bại thì hệ thống
   *     tự tạo yêu cầu hành động khắc phục) — chỉ khi thất bại NGHIÊM TRỌNG mới
   *     đồng thời mở sự cố (BR-097), nên phần lớn CAPA loại này không có incidentId.
   * Trước 21/07/2026 màn duyệt CAPA của BGH phải regex `source.match(/INC-\d{4}-\d+/)`
   * để đoán ra mã sự cố — khoá ngoại bị thay bằng chuỗi hiển thị.
   */
  incidentId?: string;
  inspectionId?: string;
  description: string;
  /** Nhãn hiển thị người/đơn vị chịu trách nhiệm. */
  assignee: string;
  /** Khoá khi người chịu trách nhiệm là một ĐƠN VỊ đối tác (bếp, NCC). */
  assigneeSupplierId?: string;
  /**
   * @deprecated BR-216 (21/07/2026) bỏ SLA/hạn cứng 12h — không còn cảnh báo trễ.
   * Giữ optional để đọc dữ liệu cũ; thay bằng 2 timestamp thô bên dưới.
   */
  deadline?: string;
  /**
   * Vòng đời chuẩn BR-216:
   *   DRAFT → PENDING_ADMIN_APPROVAL → APPROVED_FOR_IMPLEMENTATION
   *         → IN_PROGRESS → SUBMITTED_EVIDENCE → COMPLETED
   *
   * Ai đổi trạng thái nào:
   *  - Medical Staff : soạn (DRAFT) → gửi duyệt (PENDING_ADMIN_APPROVAL);
   *                    đánh giá hiệu quả khi SUBMITTED_EVIDENCE →
   *                    đạt = COMPLETED, chưa đạt = trả về IN_PROGRESS (reworkReason)
   *  - School Admin  : duyệt KẾ HOẠCH (PENDING_ADMIN_APPROVAL →
   *                    APPROVED_FOR_IMPLEMENTATION); từ chối → về DRAFT (rejectReason)
   *  - Service Caterers: nhận việc (APPROVED_FOR_IMPLEMENTATION → IN_PROGRESS);
   *                    nộp bằng chứng (→ SUBMITTED_EVIDENCE). KHÔNG có nhánh từ chối.
   *
   * Các giá trị cũ (ASSIGNED / SUBMITTED / OVERDUE) đã gỡ hẳn 22/07/2026 — không
   * bản ghi nào dùng, giữ lại chỉ là trạng thái chết.
   */
  status: keyof typeof STATUS_BADGE;
  /** Mốc Medical Staff gửi kế hoạch cho BGH duyệt. */
  submittedForApprovalAt?: string;
  /** Mốc BGH duyệt kế hoạch. */
  approvedAt?: string;
  /** Mốc Service Caterers nộp bằng chứng khắc phục. */
  evidenceSubmittedAt?: string;
  /** Lý do BGH từ chối kế hoạch (bắt buộc) — CAPA quay về DRAFT để soạn lại. */
  rejectReason?: string;
  /** Ghi chú bằng chứng do Service Caterers nộp kèm ảnh. */
  evidenceNote?: string;
  evidencePhotos?: number;
  /**
   * UC-168 AF2 "Khắc phục chưa đạt yêu cầu" (design pattern Vòng 7 Mục 15.2 điểm 3 +
   * Mục 16 QĐ 1, 19/07/2026): Medical Staff trả CAPA về cho Service Caterers thực hiện
   * lại — lý do BẮT BUỘC, độc lập với EX1 (chặn theo hồi phục học sinh, giữ nguyên).
   * Gộp vào UC-168 như Alternative Flow, không cấp UC riêng; BR-152/153/155 giữ nguyên.
   */
  reworkReason?: string;
  reworkRequestedAt?: string;
}

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

export const INCIDENT_EVENTS: IncidentEvent[] = [
  // INC-2026-014 — chuyển từ mảng TIMELINE hardcode cũ, đã sửa cho khớp bản ghi
  // thật: lô cách ly là BATCH-MP-8820 chứ không phải 2 lô như timeline cũ ghi.
  { id: "IEV-014-01", incidentId: "INC-2026-014", at: "30/06/2026 12:15", type: "REPORTED", description: "4 học sinh khối 3 báo đau bụng, buồn nôn sau bữa trưa — gộp từ RPT-2026-030", actor: "GV chủ nhiệm 3A, 3B" },
  { id: "IEV-014-02", incidentId: "INC-2026-014", at: "30/06/2026 13:05", type: "QUARANTINE", description: "Cách ly lô nghi vấn BATCH-MP-8820 (BR-149)", actor: "Hệ thống" },
  { id: "IEV-014-03", incidentId: "INC-2026-014", at: "30/06/2026 14:20", type: "SEVERITY_SET", description: "Phân loại mức độ HIGH (BR-147); cảnh báo cơ quan y tế địa phương (BR-148)", actor: "Ngô Thị Hà (Y tế)" },
  { id: "IEV-014-04", incidentId: "INC-2026-014", at: "30/06/2026 15:00", type: "SAMPLE_SENT", description: "Thu hồi mẫu lưu SMP-20260630-03, gửi CDC Hà Nội xét nghiệm (BR-113)", actor: "Ngô Thị Hà (Y tế)" },
  { id: "IEV-014-05", incidentId: "INC-2026-014", at: "30/06/2026 16:20", type: "TRACE_READY", description: "Bản đồ truy xuất hoàn tất (BR-151)", actor: "Hệ thống" },
  { id: "IEV-014-06", incidentId: "INC-2026-014", at: "01/07/2026 09:00", type: "CAPA_SUBMITTED", description: "CAPA-2026-033 soạn xong, gửi Ban giám hiệu duyệt kế hoạch (BR-153)", actor: "Ngô Thị Hà (Y tế)" },

  // INC-2026-013 — đã đóng
  { id: "IEV-013-01", incidentId: "INC-2026-013", at: "24/06/2026 11:45", type: "REPORTED", description: "Phát hiện dị vật trong canh lớp 3A — gộp từ RPT-2026-024", actor: "Ngô Thị Hà (Y tế)" },
  { id: "IEV-013-02", incidentId: "INC-2026-013", at: "25/06/2026 09:05", type: "CAPA_SUBMITTED", description: "CAPA-2026-030 gửi Ban giám hiệu duyệt kế hoạch", actor: "Ngô Thị Hà (Y tế)" },
  { id: "IEV-013-03", incidentId: "INC-2026-013", at: "25/06/2026 14:25", type: "CAPA_APPROVED", description: "Ban giám hiệu duyệt kế hoạch CAPA-2026-030", actor: "Phạm Quốc Toản (BGH)" },
  { id: "IEV-013-04", incidentId: "INC-2026-013", at: "27/06/2026 16:30", type: "CAPA_EVIDENCE", description: "Đơn vị suất ăn nộp bằng chứng khắc phục CAPA-2026-030", actor: "Cty TNHH Suất ăn An Bình" },
  { id: "IEV-013-05", incidentId: "INC-2026-013", at: "28/06/2026 10:00", type: "CLOSED", description: "Đóng sự cố sau khi rà soát hồ sơ (BR-155)", actor: "Phạm Quốc Toản (BGH)" },

  // INC-2026-012 — đã đóng
  { id: "IEV-012-01", incidentId: "INC-2026-012", at: "18/06/2026 12:30", type: "REPORTED", description: "2 học sinh nổi mẩn nghi dị ứng tôm — gộp từ RPT-2026-018", actor: "Ngô Thị Hà (Y tế)" },
  { id: "IEV-012-02", incidentId: "INC-2026-012", at: "19/06/2026 15:10", type: "CAPA_APPROVED", description: "Ban giám hiệu duyệt kế hoạch CAPA-2026-028", actor: "Phạm Quốc Toản (BGH)" },
  { id: "IEV-012-03", incidentId: "INC-2026-012", at: "22/06/2026 17:00", type: "CAPA_EVIDENCE", description: "Đơn vị suất ăn nộp bằng chứng khắc phục", actor: "Cty TNHH Suất ăn An Bình" },
  { id: "IEV-012-04", incidentId: "INC-2026-012", at: "23/06/2026 09:30", type: "CAPA_VALIDATED", description: "Y tế đánh giá khắc phục đạt, chốt CAPA-2026-028 hoàn thành (BR-216)", actor: "Ngô Thị Hà (Y tế)" },
  { id: "IEV-012-05", incidentId: "INC-2026-012", at: "23/06/2026 15:00", type: "CLOSED", description: "Đóng sự cố (BR-155)", actor: "Phạm Quốc Toản (BGH)" },
];

export const CORRECTIVE_ACTIONS: CorrectiveAction[] = [
  /*
   * Sửa 22/07/2026 cho đúng BR-216: "1 Incident = 1 CAPA — có thể gộp nhiều hành
   * động khắc phục trong cùng 1 bản ghi, KHÔNG tách nhiều CAPA song song trong cùng
   * 1 Incident". Seed cũ có 3 CAPA cùng trỏ INC-2026-014 (CAPA-030/032/033), vừa
   * trái quy tắc vừa làm màn duyệt của BGH hiện 3 dòng cho một sự cố.
   *
   * Mỗi CAPA nay gắn đúng MỘT nguồn phát sinh, và mọi CAPA — kể cả loại sinh từ
   * kiểm thực (BR-095) — đều phải qua Ban giám hiệu duyệt kế hoạch trước khi thực
   * hiện (BR-153/BR-216), thể hiện bằng cặp mốc submittedForApprovalAt/approvedAt.
   */

  // ===== Nguồn SỰ CỐ (BR-152: sau mỗi sự cố phải có CAPA) — mỗi sự cố đúng 1 =====

  // INC-2026-014 (30/06, mới nhất): gộp 3 hành động của seed cũ vào 1 bản ghi
  { id: "CAPA-2026-033", source: "INC-2026-014 (nghi ngộ độc sau bữa trưa 30/06)", incidentId: "INC-2026-014", description: "Gộp 3 hành động: (1) vệ sinh khử khuẩn toàn bộ khu sơ chế, rà soát quy trình tách thớt sống/chín; (2) bổ sung quy trình kiểm tra chéo nguồn hải sản đông lạnh trước khi nhập kho; (3) tập huấn lại quy trình kiểm soát nhiệt độ lõi cho toàn bộ nhân sự bếp.", assignee: "Cty TNHH Suất ăn An Bình", status: "APPROVED_FOR_IMPLEMENTATION", submittedForApprovalAt: "01/07/2026 09:00" },

  // INC-2026-013 (24/06): đã làm xong, chờ Y tế đánh giá hiệu quả (UC-168)
  { id: "CAPA-2026-030", source: "INC-2026-013 (dị vật trong canh lớp 3A)", incidentId: "INC-2026-013", description: "Rà soát toàn bộ dụng cụ khu chia suất, thay rổ/vợt có nguy cơ bong tróc; bổ sung bước soi dị vật trước khi chia (BR-096).", assignee: "Cty TNHH Suất ăn An Bình", status: "SUBMITTED_EVIDENCE", submittedForApprovalAt: "25/06/2026 09:05", approvedAt: "25/06/2026 14:25", evidenceSubmittedAt: "27/06/2026 16:30", evidenceNote: "Đã thay 6 dụng cụ, bổ sung bước soi dị vật vào sổ Bước 3; kèm ảnh trước/sau.", evidencePhotos: 4 },

  // INC-2026-012 (18/06, cũ nhất): đã đánh giá đạt — trạng thái cuối của CAPA
  { id: "CAPA-2026-028", source: "INC-2026-012 (2 học sinh nổi mẩn nghi dị ứng tôm)", incidentId: "INC-2026-012", description: "Rà soát nhà cung cấp hải sản, bổ sung mốc thời gian và người chịu trách nhiệm từng bước theo yêu cầu của Ban giám hiệu.", assignee: "Cty TNHH Suất ăn An Bình", status: "COMPLETED", submittedForApprovalAt: "19/06/2026 08:40", approvedAt: "19/06/2026 15:10", evidenceSubmittedAt: "22/06/2026 17:00", evidenceNote: "Đã ký phụ lục hợp đồng với NCC hải sản kèm cam kết truy xuất lô.", evidencePhotos: 2 },

  // ===== Nguồn KIỂM TRA (BR-095: bước kiểm tra thất bại thì tự tạo CAR) =====
  // Loại này KHÔNG thuộc sự cố nào — chỉ thất bại nghiêm trọng mới mở sự cố (BR-097).

  // Kiểm thực ba bước 30/06: Bước 2 không đạt → đang thực hiện, chờ nộp bằng chứng
  { id: "CAPA-2026-031", source: "INSP-20260630-L (Bước 2 không đạt)", inspectionId: "INSP-20260630-L", description: "Nấu lại món gà đạt nhiệt độ lõi ≥75°C, tái kiểm tra Bước 2 trước khi phục vụ (BR-096).", assignee: "Trần Văn Hùng", status: "IN_PROGRESS", submittedForApprovalAt: "30/06/2026 09:10", approvedAt: "30/06/2026 10:05" },

  // Kiểm tra bếp 28/05 đạt 55% < 60% (KIN-2026-016) → BGH đã duyệt, chờ bếp nhận việc
  { id: "CAPA-2026-029", source: "KIN-2026-016 (kiểm tra bếp 28/05 — 55% < 60%)", inspectionId: "KIN-2026-016", description: "Khắc phục các hạng mục vệ sinh không đạt của phiên kiểm tra bếp; bổ sung nhật ký kiểm soát côn trùng khu kho khô (BR-102).", assignee: "Lê Thị Mai", status: "APPROVED_FOR_IMPLEMENTATION", submittedForApprovalAt: "29/05/2026 08:00", approvedAt: "29/05/2026 11:30" },
];

/**
 * Ánh xạ trạng thái Incident theo tiến độ CAPA (BR-216 + BR-152/153).
 * Trước đây `CAPA_APPROVED` không màn nào ghi nên sự cố kẹt mãi ở `CAPA_PENDING`.
 *
 *   có CAPA PENDING_ADMIN_APPROVAL          → CAPA_PENDING  (chờ BGH duyệt kế hoạch)
 *   có CAPA từ APPROVED_FOR_IMPLEMENTATION  → CAPA_APPROVED (đã duyệt, đang khắc phục)
 *   TẤT CẢ CAPA đã COMPLETED                → VALIDATED     (đủ điều kiện đóng, BR-155)
 */
const CAPA_ACTIVE_STATUSES = [
  "APPROVED_FOR_IMPLEMENTATION",
  "IN_PROGRESS",
  "SUBMITTED_EVIDENCE",
] as const;

export function incidentStatusFromCapa(
  capas: Pick<CorrectiveAction, "status">[]
): "CAPA_PENDING" | "CAPA_APPROVED" | "VALIDATED" | undefined {
  if (capas.length === 0) return undefined;
  if (capas.every((c) => c.status === "COMPLETED")) return "VALIDATED";
  if (capas.some((c) => (CAPA_ACTIVE_STATUSES as readonly string[]).includes(String(c.status))))
    return "CAPA_APPROVED";
  if (capas.some((c) => c.status === "PENDING_ADMIN_APPROVAL")) return "CAPA_PENDING";
  return undefined;
}

// ===== Thông báo trường học (BR-138/BR-144) =====

export interface Announcement {
  id: string;
  title: string;
  audience: string;
  publishedAt: string;
  /** DRAFT | PUBLISHED (UC-089 Publication Status) */
  status: "DRAFT" | "PUBLISHED";
  author: string;
  revisions: { at: string; by: string; note: string }[];
  urgent: boolean;
  content: string;
}

export const ANNOUNCEMENTS: Announcement[] = [
  {
    id: "ANN-112",
    title: "Thông báo thực đơn tuần 27 (29/06–03/07)",
    audience: "Phụ huynh toàn trường",
    publishedAt: "27/06/2026",
    status: "PUBLISHED",
    author: "Phạm Quốc Toản (BGH)",
    revisions: [{ at: "28/06/2026 08:10", by: "Phạm Quốc Toản", note: "Bổ sung ghi chú bữa phụ thứ 4" }],
    urgent: false,
    content:
      "Kính gửi Quý phụ huynh,\n\nThực đơn tuần 27 (29/06–03/07) đã được Ban giám hiệu phê duyệt và công bố trên hệ thống. Quý phụ huynh có thể xem chi tiết từng ngày tại mục Thực đơn tuần, bao gồm thành phần món ăn và thông tin dinh dưỡng.\n\nHọc sinh có hồ sơ dị ứng đã xác minh sẽ tự động được phục vụ suất ăn riêng theo kế hoạch bữa ăn đặc biệt.\n\nTrân trọng.",
  },
  {
    id: "ANN-111",
    title: "Cập nhật kết quả điều tra sự cố bữa trưa 30/06",
    audience: "Phụ huynh khối 3",
    publishedAt: "01/07/2026",
    status: "PUBLISHED",
    author: "Ngô Thị Hà (Y tế)",
    revisions: [
      { at: "01/07/2026 09:00", by: "Ngô Thị Hà", note: "Công bố lần đầu" },
      { at: "01/07/2026 15:30", by: "Ngô Thị Hà", note: "Bổ sung kết quả sơ bộ từ CDC" },
      { at: "02/07/2026 08:00", by: "Phạm Quốc Toản", note: "Cập nhật biện pháp khắc phục của đơn vị suất ăn" },
    ],
    urgent: true,
    content:
      "Kính gửi Quý phụ huynh khối 3,\n\nLiên quan đến việc 4 học sinh có biểu hiện đau bụng nhẹ sau bữa trưa ngày 30/06, nhà trường thông tin như sau:\n\n1. Y tế trường đã kích hoạt quy trình điều tra ngay trong chiều 30/06; mẫu lưu món ăn liên quan đã được gửi xét nghiệm tại CDC Hà Nội.\n2. Các lô nguyên liệu nghi vấn đã được cách ly, không sử dụng cho các bữa ăn tiếp theo.\n3. Sức khỏe của 4 học sinh đã ổn định, đi học bình thường từ 01/07.\n\nNhà trường sẽ tiếp tục cập nhật khi có kết quả xét nghiệm chính thức (dự kiến 04/07).",
  },
  {
    id: "ANN-110",
    title: "Kế hoạch khảo sát phản hồi bữa ăn tháng 7",
    audience: "Giáo viên + Phụ huynh",
    publishedAt: "25/06/2026",
    status: "PUBLISHED",
    author: "Phạm Quốc Toản (BGH)",
    revisions: [],
    urgent: false,
    content:
      "Nhà trường triển khai khảo sát phản hồi chất lượng bữa ăn bán trú tháng 7. Quý phụ huynh vui lòng gửi đánh giá qua mục Phản hồi trong vòng 24 giờ sau mỗi bữa ăn để dữ liệu được tổng hợp chính xác.\n\nKết quả tổng hợp ẩn danh sẽ được công bố vào tuần đầu tháng 8.",
  },
  {
    id: "ANN-109",
    title: "Nhắc nhở cập nhật hồ sơ dị ứng trước năm học mới",
    audience: "Phụ huynh toàn trường",
    publishedAt: "20/06/2026",
    status: "PUBLISHED",
    author: "Ngô Thị Hà (Y tế)",
    revisions: [],
    urgent: false,
    content:
      "Đề nghị Quý phụ huynh rà soát và cập nhật thông tin dị ứng, chế độ ăn đặc biệt của con trước ngày 15/08 để Y tế trường xác minh kịp thời cho năm học 2026–2027. Thông tin đã xác minh sẽ kích hoạt cảnh báo tự động trong giờ ăn.",
  },
];

// ===== Checklist kiểm tra bếp ĐỘNG (UC-186, BR-210 — mới 06/07/2026) =====
// Medical Staff tự CRUD nhóm/hạng mục/chú thích căn cứ pháp lý, không qua System Admin
// (BR-210 thay điều khoản "System Admin cấu hình" trong BR-207 gốc). Dữ liệu dưới đây là
// SEED KHỞI TẠO MẶC ĐỊNH — 6 nhóm theo MedicalStaff_KitchenInspectionChecklist_Proposal.md
// (bản 06/07/2026); nội dung chú thích là bản nháp tham khảo, mục chưa có căn cứ trong
// 00_Policies/ được giữ nguyên đánh dấu [cần xác nhận thêm] (không suy diễn nguồn pháp lý).

export interface ChecklistItem {
  id: string;
  name: string;
  /** BR-207/BR-210: chú thích căn cứ pháp lý (icon "ℹ"), text đơn giản, không versioning */
  legalNote?: string;
}

export interface ChecklistGroup {
  id: string;
  name: string;
  order: number;
  items: ChecklistItem[];
}

export const CHECKLIST_GROUPS_SEED: ChecklistGroup[] = [
  {
    id: "CLG-1",
    name: "Nhóm 1 — Hồ sơ",
    order: 1,
    items: [
      { id: "CLI-1-1", name: "Hợp đồng cung cấp hàng hóa, hồ sơ nguồn gốc nguyên liệu", legalNote: "Cơ sở phải lưu giữ hợp đồng cung cấp hàng hóa và hồ sơ chứng minh nguồn gốc nguyên liệu; nếu trường không tự nấu ăn, đơn vị cung cấp suất ăn phải có Giấy chứng nhận đủ điều kiện ATTP (Luật ATTP 55/2010/QH12 Điều 19.1.đ; TTLT 13/2016 Phụ lục 04 Mục IV.4.2)." },
      { id: "CLI-1-2", name: "Hóa đơn chứng từ chứng minh nguồn gốc nguyên liệu thực phẩm", legalNote: "Mỗi lô nguyên liệu nhập vào phải có hóa đơn/chứng từ chứng minh nguồn gốc, xuất xứ — kiểm tra ngay ở Bước 1 kiểm thực ba bước (QĐ 1246/QĐ-BYT, Điều 3.1.a)." },
    ],
  },
  {
    id: "CLG-2",
    name: "Nhóm 2 — Yêu cầu đối với người tiếp xúc trực tiếp sản phẩm",
    order: 2,
    items: [
      { id: "CLI-2-1", name: "Mặc đồ bảo hộ, khẩu trang trong quá trình chế biến, cung cấp đồ ăn", legalNote: "Người trực tiếp chế biến, phục vụ phải mặc trang phục bảo hộ riêng, đội mũ, đeo khẩu trang, đi găng tay chuyên dụng (TTLT 13/2016, Phụ lục 04, Mục IV.4.4; QĐ 1246/QĐ-BYT, Điều 4.1.a)." },
      { id: "CLI-2-2", name: "Đảm bảo vệ sinh cá nhân sạch sẽ, gọn gàng tuân thủ quy định VSATTP", legalNote: "Người trực tiếp chế biến, phục vụ phải tuân thủ quy định về sức khỏe, kiến thức và thực hành an toàn thực phẩm (Luật ATTP, Điều 29.4)." },
      { id: "CLI-2-3", name: "Không ăn uống, nhai kẹo cao su, hút thuốc lá trong khu vực nhà bếp", legalNote: "[cần xác nhận thêm] — chưa tìm thấy văn bản gốc trong 00_Policies/ (khả năng thuộc QCVN 01-1:2018/BYT, chưa có trong hồ sơ dự án)." },
      { id: "CLI-2-4", name: "Sử dụng găng tay nilon khi tiếp xúc với thực phẩm chín, lau đũa thìa", legalNote: "Phải đeo găng tay khi tiếp xúc trực tiếp với thực phẩm chín (QĐ 1246/QĐ-BYT, Điều 4.1.a). Chất liệu \"nilon\" cụ thể: [cần xác nhận thêm]." },
    ],
  },
  {
    id: "CLG-3",
    name: "Nhóm 3 — Điều kiện bảo quản thực phẩm",
    order: 3,
    items: [
      { id: "CLI-3-1", name: "Khu nhập hàng, kho bảo quản đảm bảo vệ sinh; thực phẩm cách nền, tường, trần theo quy định", legalNote: "Khu nhập hàng và kho phải sạch, có phương tiện phân loại/lưu giữ phù hợp (Luật ATTP Điều 20.1.a; TTLT 13/2016 Phụ lục 04 Mục IV.4.3). Khoảng cách cụ thể (cm): [cần xác nhận thêm]." },
      { id: "CLI-3-2", name: "Thực phẩm có đầy đủ thông tin ghi nhãn theo quy định", legalNote: "Thực phẩm bao gói sẵn phải ghi nhãn đầy đủ nội dung bắt buộc (NĐ 15/2018/NĐ-CP, Điều 24)." },
      { id: "CLI-3-3", name: "Bảo quản đúng cách, đúng nhiệt độ, ngăn ngừa lây nhiễm chéo (sống chín tách biệt)", legalNote: "Thực phẩm sống và chín phải bảo quản tách biệt, đúng nhiệt độ, tránh nhiễm chéo (Luật ATTP, Điều 28.1, Điều 29.1)." },
      { id: "CLI-3-4", name: "Dụng cụ bảo quản tách biệt giữa thực phẩm tươi sống và đã qua chế biến", legalNote: "Phải có dụng cụ, đồ chứa đựng riêng cho thực phẩm sống và chín (Luật ATTP, Điều 29.1)." },
      { id: "CLI-3-5", name: "Chế độ theo dõi nhiệt độ bảo quản phù hợp", legalNote: "Khu bảo quản phải có thiết bị điều chỉnh và theo dõi nhiệt độ phù hợp từng loại thực phẩm (Luật ATTP, Điều 20.1.b). Tủ lưu mẫu áp dụng thêm 2–8°C của BR-110 (kiểm tra chéo module Lưu mẫu)." },
      { id: "CLI-3-6", name: "Chế độ vệ sinh khu vực bảo quản định kỳ đảm bảo sạch", legalNote: "Khu bảo quản phải được vệ sinh định kỳ theo quy định (Luật ATTP, Điều 20.1.c)." },
      { id: "CLI-3-7", name: "Khu vực bảo quản sắp xếp gọn gàng, ngăn nắp", legalNote: "Khu bảo quản phải đủ diện tích, sắp xếp gọn gàng, xếp dỡ an toàn (Luật ATTP, Điều 20.1.a; TTLT 13/2016, Phụ lục 04, Mục IV.4.3)." },
      { id: "CLI-3-8", name: "Không có dấu vết của côn trùng, động vật gây hại", legalNote: "Phải có biện pháp ngăn ngừa côn trùng, động vật gây hại xâm nhập (Luật ATTP, Điều 19.1.c, Điều 20.1.b)." },
    ],
  },
  {
    id: "CLG-4",
    name: "Nhóm 4 — Điều kiện khu vực sơ chế, chế biến thực phẩm",
    order: 4,
    items: [
      { id: "CLI-4-1", name: "Bố trí sắp xếp khu vực bếp theo nguyên tắc 1 chiều", legalNote: "Bếp bố trí một hướng: tiếp nhận → sơ chế → nấu → chia soạn → ăn uống, không đi ngược/cắt ngang khu sạch, tránh nhiễm chéo sống–chín (TTLT 13/2016, Phụ lục 04, Mục IV.4.2)." },
      { id: "CLI-4-2", name: "Khu sơ chế có đầy đủ trang thiết bị đảm bảo an toàn sơ chế", legalNote: "Khu sơ chế phải có đủ trang thiết bị phù hợp để xử lý nguyên liệu an toàn (Luật ATTP, Điều 19.1.c)." },
      { id: "CLI-4-3", name: "Cống rãnh thông thoát nước, không ứ đọng và được che đậy", legalNote: "Cống rãnh khu bếp phải thông thoát, không ứ đọng (Luật ATTP, Điều 28.4). Yêu cầu \"có nắp che\": [cần xác nhận thêm]." },
      { id: "CLI-4-4", name: "Dụng cụ thu gom rác thải có nắp đậy", legalNote: "Phải có dụng cụ thu gom, chứa đựng rác thải bảo đảm vệ sinh (Luật ATTP, Điều 28.3). Chi tiết \"có nắp đậy\": [cần xác nhận thêm]." },
      { id: "CLI-4-5", name: "Dụng cụ sơ chế riêng biệt đối với thực phẩm tươi sống", legalNote: "Dụng cụ sơ chế thực phẩm tươi sống phải tách biệt, không dùng chung với thực phẩm chín (Luật ATTP, Điều 29.1)." },
      { id: "CLI-4-6", name: "Khu chế biến có phương tiện phòng chống côn trùng và động vật gây hại", legalNote: "Khu chế biến phải có phương tiện phòng, chống côn trùng (ví dụ lưới chắn cửa sổ) (Luật ATTP, Điều 28.5; TTLT 13/2016, Phụ lục 04, Mục IV.4.1)." },
      { id: "CLI-4-7", name: "Dụng cụ, thiết bị chế biến đảm bảo vệ sinh, dùng riêng cho thực phẩm đã qua chế biến", legalNote: "Dụng cụ cho thực phẩm chín phải bảo đảm vệ sinh, không dùng chung với thực phẩm sống (Luật ATTP, Điều 29.1, 29.2)." },
      { id: "CLI-4-8", name: "Chế biến trên bàn cao mặt đất tối thiểu 60 cm", legalNote: "Bàn/giá chế biến, bày thực phẩm phải cao hơn mặt đất (Luật ATTP, Điều 30.3). Con số \"60cm\": [cần xác nhận thêm]." },
      { id: "CLI-4-9", name: "Có đầy đủ phương tiện rửa tay, hóa chất diệt khuẩn cho nhân viên", legalNote: "Khu chế biến phải có đủ phương tiện rửa tay và dung dịch sát khuẩn (Luật ATTP, Điều 28.6; TTLT 13/2016, Phụ lục 04, Mục IV.4.1)." },
      { id: "CLI-4-10", name: "Chất tẩy rửa có nguồn gốc, không dùng sản phẩm trôi nổi", legalNote: "Chất tẩy rửa, khử trùng phải có nguồn gốc rõ ràng, an toàn (nguyên tắc chung Luật ATTP, Điều 23.1.đ). Điều khoản áp riêng dịch vụ ăn uống: [cần xác nhận thêm]." },
      { id: "CLI-4-11", name: "Đảm bảo các biện pháp phòng cháy, chữa cháy theo quy định", legalNote: "Kiểm tra chéo NGOÀI phạm vi Luật ATTP — thuộc pháp luật PCCC (chưa có trong 00_Policies/), trách nhiệm chuyên môn chính thuộc bộ phận PCCC/Facilities. [cần xác nhận thêm]" },
    ],
  },
  {
    id: "CLG-5",
    name: "Nhóm 5 — An toàn thực phẩm",
    order: 5,
    items: [
      { id: "CLI-5-1", name: "Nguyên liệu có nguồn gốc, kiểm định đúng quy định, rửa sạch trước khi chế biến", legalNote: "Nguyên liệu phải rõ nguồn gốc, còn hạn, kiểm tra cảm quan và rửa sạch trước chế biến — Bước 1 kiểm thực ba bước (Luật ATTP Điều 26.1; QĐ 1246/QĐ-BYT Điều 3)." },
      { id: "CLI-5-2", name: "Không dùng thực phẩm quá hạn, ôi, thiu, hỏng để chế biến", legalNote: "Không dùng nguyên liệu quá hạn, ôi thiu; phát hiện ở Bước 1 phải loại bỏ/trả lại (Luật ATTP Điều 26.1; QĐ 1246/QĐ-BYT Điều 3.1.c)." },
      { id: "CLI-5-3", name: "Thức ăn, đồ uống được nấu chín/đủ điều kiện chín trước khi cung cấp", legalNote: "Món ăn phải chế biến chín/an toàn trước khi phục vụ — đối chiếu Bước 3 kiểm thực ba bước (Luật ATTP Điều 30.2; QĐ 1246/QĐ-BYT Điều 5)." },
      { id: "CLI-5-4", name: "Không dùng phụ gia, phẩm màu, chất ngọt tổng hợp gây hại", legalNote: "Chỉ dùng phụ gia trong danh mục Bộ Y tế cho phép, đúng liều lượng, nguồn gốc rõ (Luật ATTP Điều 26.2; NĐ 15/2018/NĐ-CP Điều 33.2). Danh mục chất cấm cụ thể: [cần xác nhận thêm]." },
    ],
  },
  {
    id: "CLG-6",
    name: "Nhóm 6 — Sổ kiểm thực 3 bước và lưu mẫu (kiểm tra chéo)",
    order: 6,
    items: [
      { id: "CLI-6-1", name: "Lưu mẫu đúng quy định (số lượng, thời gian, dụng cụ, bảo quản) và ghi sổ 3 bước đúng quy định", legalNote: "Ghi chép đủ Sổ kiểm thực ba bước (Mẫu số 1–3, Phụ lục 1) và lưu mẫu: dụng cụ nắp kín, 2–8°C, tối thiểu 24 giờ (QĐ 1246/QĐ-BYT, Điều 3–8). Kiểm tra chéo — dữ liệu chính quản lý tại UC-079/UC-076/UC-118 (BR-105–114, BR-110)." },
      { id: "CLI-6-2", name: "Lưu đủ mẫu, đúng tên thực phẩm, đủ định lượng mẫu lưu", legalNote: "Bữa ăn từ 30 suất trở lên: lưu mẫu riêng từng món, đúng tên, tối thiểu 100g thức ăn đặc/150ml thức ăn lỏng (QĐ 1246/QĐ-BYT, Điều 1.2, Điều 7.2). Kiểm tra chéo module Lưu mẫu." },
    ],
  },
];

// ===== Báo cáo ATTP đã gửi (UC-187–190, BR-177/BR-209/BR-213/BR-214 — 13/07/2026) =====
// Lịch sử chung cho cả 3 loại báo cáo Medical Staff chủ động gửi School Administration;
// hiện thực hóa BR-177 (lưu trữ ≥12 tháng) ở phạm vi Medical Staff (32_SentReportsHistory.md).

/**
 * Phần CHUNG của mọi báo cáo đã gửi — 9 field vốn có từ 13/07/2026.
 *
 * Vòng 6 (27/07/2026, Mục 13.3 điểm 5): tách `SentReport` thành **discriminated union
 * theo `reportType`**. Trước đó type chỉ khai 9 field chung, trong khi lúc gửi thật
 * `compliance/report/page.tsx` và `violations/reports/page.tsx` đã đính kèm hàng loạt
 * field riêng theo từng loại — chúng chỉ "tồn tại" nhờ payload lỏng kiểu
 * `Record<string, unknown>`, không có gì kiểm tra. Detail view (Mục 13.3 điểm 4) phải
 * đọc lại đúng nội dung đã chốt tại thời điểm gửi (BR-178), nên phải chính thức hoá
 * shape TRƯỚC khi dựng, nếu không Detail sẽ đọc field không ai bảo đảm là có.
 */
interface SentReportBase {
  id: string;
  period: "Daily" | "Weekly";
  rangeLabel: string; // khoảng thời gian dữ liệu
  date: string; // ngày gửi dd/MM/yyyy (phục vụ filter khoảng ngày)
  sentAt: string; // ngày giờ gửi
  sentBy: string;
  recipient: string;
  notes?: string;
}

/** Snapshot 1 phiên kiểm thực đã chốt trong báo cáo tuân thủ (field 1b — Mục 8.3) */
export interface SentReportThreeStepRow {
  id: string;
  date: string;
  meal: string;
  dish: string;
  overall: string;
  failedStep: string;
  reason: string;
  inspector: string;
  location: string;
}

/** Phiên kiểm tra không đạt trong kỳ (field 4 — exception-only từ đầu) */
export interface SentReportFailedSession {
  id: string;
  date: string;
  reason: string;
}

/** UC-185/BR-209 — Báo cáo Giám sát tuân thủ */
export interface ComplianceSentReport extends SentReportBase {
  reportType: "COMPLIANCE";
  /** Điểm vệ sinh trung bình trong kỳ (BR-104); `null` khi kỳ không có phiên nào */
  avgScore?: number | null;
  failedSessions?: SentReportFailedSession[];
  threeStepSummary?: { done: number; total: number };
  threeStepRows?: SentReportThreeStepRow[];
  /**
   * @deprecated Vòng 9 (27/07/2026, Mục 17.3) — BR-209 bỏ dòng "tình trạng chứng nhận
   * kiểm soát côn trùng" khỏi nội dung tối thiểu của báo cáo tuân thủ. Báo cáo mới không
   * gửi kèm field này nữa; giữ khai báo để đọc lại báo cáo đã gửi trước ngày này.
   */
  latestPestControl?: { date: string; pestFound: string | null; nextDueDate: string } | null;
  staffHealthValid?: boolean | null;
  /** Field 1d — nhận định tổng hợp do người soạn viết */
  assessment?: string;
}

/** UC-189/BR-214 — Báo cáo Tổng hợp Vi phạm */
export interface ViolationSentReport extends SentReportBase {
  reportType: "VIOLATION_SUMMARY";
  summary?: string;
  staffInvolved?: string;
  counts?: {
    processCount: number;
    staffCount: number;
    bySeverity: Record<string, number>;
  };
  violationIds?: string[];
}

/**
 * @deprecated UC-188/BR-213 Deprecated 26/07/2026 (Mục 10.2) — không sinh bản ghi mới.
 * Giữ nhánh type để đọc báo cáo đã gửi trước đó.
 */
export interface IncidentSummarySentReport extends SentReportBase {
  reportType: "INCIDENT_SUMMARY";
  summary?: string;
  incidentIds?: string[];
}

export type SentReport = ComplianceSentReport | ViolationSentReport | IncidentSummarySentReport;

export const SENT_REPORTS: SentReport[] = [
  {
    id: "RPTS-2026-001",
    reportType: "COMPLIANCE",
    period: "Weekly",
    rangeLabel: "Thứ 2 06/07/2026 – Chủ nhật 12/07/2026",
    date: "12/07/2026",
    sentAt: "12/07/2026 16:40",
    sentBy: "Ngô Thị Hà (Y tế)",
    recipient: "School Administration",
    notes: "Điểm vệ sinh ổn định; đề nghị nhắc bếp bổ sung nhật ký côn trùng đúng hẹn.",
    // Snapshot đúng thứ đã chốt lúc gửi — Detail view đọc lại nguyên văn, KHÔNG tính lại
    // (BR-178). Số liệu khớp `KITCHEN_INSPECTIONS` của kỳ đó.
    avgScore: 90,
    threeStepSummary: { done: 5, total: 5 },
    failedSessions: [],
    staffHealthValid: true,
    assessment: "Không có phiên không đạt trong kỳ; điểm vệ sinh trung bình 90%.",
  },
];

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

export const KITCHEN_INSPECTIONS: KitchenInspection[] = [
  { id: "KIN-2026-018", date: "24/06/2026", type: "Đột xuất", score: 92, inspector: "Ngô Thị Hà", pestControlValid: true, staffHealthValid: true, result: "PASSED" },
  { id: "KIN-2026-017", date: "10/06/2026", type: "Định kỳ tháng", score: 88, inspector: "Ngô Thị Hà", pestControlValid: true, staffHealthValid: true, result: "PASSED" },
  { id: "KIN-2026-016", date: "28/05/2026", type: "Đột xuất", score: 55, inspector: "Ngô Thị Hà", pestControlValid: true, staffHealthValid: false, result: "FAILED", itemFindings: [{ item: "Sàn bếp khô ráo, không đọng nước", photos: 2, reason: "Nước đọng khu vực rửa, nguy cơ trơn trượt và nhiễm khuẩn chéo" }, { item: "Thùng rác có nắp, không quá đầy", photos: 1, reason: "2/4 thùng rác không có nắp đậy" }] },
  { id: "KIN-2026-015", date: "12/05/2026", type: "Định kỳ tháng", score: 90, inspector: "Ngô Thị Hà", pestControlValid: true, staffHealthValid: true, result: "PASSED" },
];

// ===== Lớp học & Năm học (MF02, BR-020..BR-028, BR-191..BR-195) =====

export interface Classroom {
  id: string;
  grade: number; // BR-024: 1–12
  teacher: string;
  /**
   * Khoá ngoại tới `UserAccount.id` của giáo viên chủ nhiệm (review/teacher —
   * `review_teacher_data_linkage_plan.md` Đợt 0). Trước đây chỉ có `teacher`
   * (chuỗi tên hiển thị) — nối phiên đăng nhập (`SessionUser`) với lớp chủ
   * nhiệm phải so tên, sai nếu 2 giáo viên trùng tên hoặc tên đổi sau này.
   * Chỉ 5 lớp khối 3-5 có tài khoản giáo viên tương ứng trong `USER_ACCOUNTS`
   * (seed hiện tại); 5 lớp còn lại để trống, không bịa thêm tài khoản.
   */
  teacherId?: string;
  students: number; // BR-025: <= 50
  allergies: number;
  academicYearId: string;
  /** BR-025: sức chứa cấu hình được (<=50); mặc định 50 nếu không đặt */
  capacity?: number;
  /** Giờ ăn trưa dự kiến của lớp — chỉ có dữ liệu cho 3A (seed gốc của `TEACHER_CLASS` cũ). */
  lunchTime?: string;
}

export const CLASSROOMS: Classroom[] = [
  { id: "3A", grade: 3, teacher: "Nguyễn Thu Trang", teacherId: "U-0004", students: 38, allergies: 3, academicYearId: "AY-2025", capacity: 45, lunchTime: "10:45" },
  { id: "3B", grade: 3, teacher: "Phan Thị Hồng", teacherId: "U-0009", students: 40, allergies: 2, academicYearId: "AY-2025", capacity: 45 },
  { id: "4A", grade: 4, teacher: "Đào Văn Quân", teacherId: "U-0010", students: 42, allergies: 5, academicYearId: "AY-2025", capacity: 50 },
  { id: "4B", grade: 4, teacher: "Lưu Thị Bích", teacherId: "U-0011", students: 41, allergies: 1, academicYearId: "AY-2025", capacity: 50 },
  { id: "5A", grade: 5, teacher: "Trịnh Minh Đức", teacherId: "U-0012", students: 45, allergies: 4, academicYearId: "AY-2025", capacity: 50 },
  { id: "1A", grade: 1, teacher: "Hà Thị Lan", students: 35, allergies: 2, academicYearId: "AY-2025", capacity: 45 },
  { id: "1B", grade: 1, teacher: "Vũ Thu Hường", students: 34, allergies: 0, academicYearId: "AY-2025", capacity: 45 },
  { id: "2A", grade: 2, teacher: "Đinh Văn Tú", students: 39, allergies: 1, academicYearId: "AY-2025", capacity: 45 },
  { id: "2B", grade: 2, teacher: "Lê Thanh Nga", students: 37, allergies: 2, academicYearId: "AY-2025", capacity: 45 },
  { id: "5B", grade: 5, teacher: "Phùng Quang Vinh", students: 44, allergies: 3, academicYearId: "AY-2025", capacity: 50 },
];

export interface AcademicYear {
  id: string;
  name: string;
  start: string;
  end: string;
  classes: number;
  students: number;
  status: keyof typeof STATUS_BADGE; // ACTIVE | DRAFT | CLOSED (BR-191)
  /** BR-194: khi tạo, sao chép cấu trúc lớp từ năm liền trước (không chuyển HS). */
  copyStructure?: boolean;
}

export const ACADEMIC_YEARS: AcademicYear[] = [
  { id: "AY-2025", name: "Năm học 2025–2026", start: "05/09/2025", end: "31/05/2026", classes: 28, students: 1180, status: "ACTIVE" },
  { id: "AY-2026", name: "Năm học 2026–2027", start: "05/09/2026", end: "31/05/2027", classes: 0, students: 0, status: "DRAFT" },
  { id: "AY-2024", name: "Năm học 2024–2025", start: "05/09/2024", end: "31/05/2025", classes: 27, students: 1145, status: "CLOSED" },
];

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

export const VIOLATIONS: ViolationRecord[] = [
  // 19/07/2026 (review violations Mục 2.3/2.4): bổ sung file thật + PDF artifact + link
  // ngược bản ghi kiểm thực gốc cho 2 bản ghi mẫu — các bản ghi cũ giữ nguyên để kiểm
  // thử đường tương thích ngược (chỉ có evidencePhotos/signedRecordPhoto).
  { id: "VIO-2026-008", type: "Quy trình", subject: "Khu sơ chế — không tách thớt sống/chín", recordedBy: "Ngô Thị Hà (Y tế)", date: "30/06/2026", severity: "MEDIUM", resolution: "PENDING_APPROVAL", status: "OPEN", violationType: "HYGIENE_VIOLATION", description: "Phát hiện dùng chung thớt cho thịt sống và rau đã chần tại khu sơ chế.", witness: "Lê Thị Mai", evidencePhotos: 2, evidenceFiles: [{ name: "bang_chung_01.jpg", uploadedAt: "30/06/2026 09:40", size: "1.6 MB" }, { name: "bang_chung_02.jpg", uploadedAt: "30/06/2026 09:41", size: "2.0 MB" }], actionTaken: "Yêu cầu thay thớt, tách khu; huấn luyện lại quy trình.", handledBy: "Ngô Thị Hà (Y tế)", locationOfIncident: "Khu sơ chế, bếp trung tâm", occurredAt: "30/06/2026 09:15", caterersRepresentative: "Trần Văn Hùng (Cty TNHH Suất ăn An Bình)", pdfExported: true, pdfGeneratedAt: "30/06/2026 10:05", pdfVersion: 1, signedRecordPhoto: true, signedRecordFiles: [{ name: "bien_ban_da_ky_01.jpg", uploadedAt: "30/06/2026 14:20", size: "2.4 MB" }], relatedIncidentId: "INC-2026-014", sourceInspectionId: "INSP-20260630-L" },
  { id: "VIO-2026-007", type: "Nhân sự", subject: "NV bếp không đeo găng tay khi chia suất", recordedBy: "Ngô Thị Hà (Y tế)", date: "26/06/2026", severity: "MEDIUM", resolution: "APPROVED", status: "CLOSED", staffName: "Đỗ Văn Kiên", staffPosition: "Phụ bếp", violationType: "HYGIENE_VIOLATION", description: "Chia suất ăn không đeo găng tay dùng một lần.", occurredAt: "26/06/2026 10:30", evidencePhotos: 1, actionTaken: "Nhắc nhở trực tiếp + ký cam kết.", disciplinaryLevel: "WRITTEN_WARNING", handledBy: "Ngô Thị Hà (Y tế)", locationOfIncident: "Khu chia suất, bếp trung tâm", caterersRepresentative: "Trần Văn Hùng (Cty TNHH Suất ăn An Bình)", pdfExported: true, signedRecordPhoto: true },
  // Chuyển từ `CATERER_VIOLATIONS` (21/07/2026) — bản ghi duy nhất không trùng với
  // vi phạm do Y tế lập; 3 bản còn lại của bảng cũ là trùng lặp nên đã bỏ.
  { id: "VIO-2026-009", type: "Quy trình", subject: "Bỏ qua bước ghi nhiệt độ lõi khi kiểm thực Bước 2", recordedBy: "Trần Văn Hùng", date: "28/06/2026", severity: "MEDIUM", resolution: "PENDING_APPROVAL", status: "OPEN", selfReported: true, violationType: "PROCEDURE_SKIP", description: "Bỏ qua bước ghi nhiệt độ lõi khi kiểm thực Bước 2 bữa phụ 28/06.", witness: "Lê Thị Mai", evidencePhotos: 2, occurredAt: "28/06/2026 09:40" },
  { id: "VIO-2026-006", type: "Quy trình", subject: "Ghi thiếu giờ bắt đầu chế biến (Bước 2)", recordedBy: "Trần Văn Hùng", date: "20/06/2026", severity: "LOW", resolution: "APPROVED", status: "CLOSED", violationType: "PROCEDURE_SKIP", description: "Sổ kiểm thực Bước 2 thiếu giờ bắt đầu chế biến.", evidencePhotos: 1, actionTaken: "Bổ sung ngay tại chỗ, nhắc nhở.", handledBy: "Trần Văn Hùng" },
  { id: "VIO-2026-005", type: "Nhân sự", subject: "Lạm dụng manual override khi xuất kho (OVERRIDE_ABUSE)", recordedBy: "Hệ thống (BR-190)", date: "15/06/2026", severity: "HIGH", resolution: "APPROVED", status: "CLOSED", staffName: "Đỗ Văn Kiên", staffPosition: "Phụ bếp", violationType: "OVERRIDE_ABUSE", description: "3 lần override FEFO trong 1 tuần không có lý do hợp lệ.", occurredAt: "15/06/2026 06:20", evidencePhotos: 0, actionTaken: "Đình chỉ thao tác kho 1 tuần, đào tạo lại.", disciplinaryLevel: "SUSPENSION", handledBy: "Ngô Thị Hà (Y tế)", repeatSuspect: true },
  { id: "VIO-2026-004", type: "Quy trình", subject: "Không dán nhãn mẫu lưu theo Mẫu số 4 (BR-106)", recordedBy: "Ngô Thị Hà (Y tế)", date: "08/06/2026", severity: "MEDIUM", resolution: "APPROVED", status: "CLOSED", violationType: "PROCEDURE_SKIP", description: "2 hộp mẫu lưu bữa trưa không dán nhãn Mẫu số 4.", witness: "Lê Thị Mai", evidencePhotos: 2, actionTaken: "Dán nhãn bổ sung, rà soát quy trình cuối ca.", handledBy: "Ngô Thị Hà (Y tế)" },
];

// ===== Phản hồi phụ huynh (BR-139..BR-145) =====

export interface Feedback {
  id: string;
  /** Khoá về học sinh — thiếu nó thì nhà trường không biết phản hồi của lớp nào. */
  studentId: string;
  student: string;
  class: string;
  date: string; // dd/MM/yyyy — ngày bữa ăn được phản hồi
  mealType: "Bữa sáng" | "Bữa trưa" | "Bữa phụ";
  dish: string;
  /** Khoá về THƯ VIỆN MÓN — nối ở vòng backfill. */
  dishId?: string;
  rating: number;
  tags: string[];
  comment: string;
  submittedAt: string;
  illnessKeyword: boolean; // BR-142
}

/** BR-142: từ khoá bệnh lý trong phản hồi → chuyển Y tế trường xem ngay. */
const ILLNESS_KEYWORDS = [
  "đau bụng",
  "buồn nôn",
  "nôn",
  "tiêu chảy",
  "dị ứng",
  "nổi mẩn",
  "ngứa",
  "sốt",
  "ngộ độc",
  "khó thở",
];

export function hasIllnessKeyword(text: string): boolean {
  const t = text.toLowerCase();
  return ILLNESS_KEYWORDS.some((k) => t.includes(k));
}

export const FEEDBACKS: Feedback[] = [
  { id: "FB-8812", studentId: "HS-3A-001", student: "Nguyễn Minh Khang", class: "3A", date: "01/07/2026", mealType: "Bữa trưa", dish: "Cá basa sốt cà chua", rating: 5, tags: [], comment: "Con ăn hết suất, khen ngon.", submittedAt: "01/07/2026 19:30", illnessKeyword: false },
  { id: "FB-8809", studentId: "HS-3A-001", student: "Nguyễn Minh Khang", class: "3A", date: "30/06/2026", mealType: "Bữa trưa", dish: "Gà rang gừng", rating: 2, tags: ["Món ăn nguội", "Khẩu phần ít"], comment: "Con về kêu đau bụng nhẹ buổi chiều.", submittedAt: "30/06/2026 20:15", illnessKeyword: true },
  { id: "FB-8801", studentId: "HS-3A-002", student: "Trần Bảo An", class: "3A", date: "29/06/2026", mealType: "Bữa trưa", dish: "Thịt kho trứng cút", rating: 4, tags: [], comment: "Ổn, con thích món trứng cút.", submittedAt: "29/06/2026 18:50", illnessKeyword: false },
];

// ===== Danh mục Permission (UC-129 — cố định trong code, chỉ đọc) =====

export interface Permission {
  id: string;
  name: string;
  module: string;
  description: string;
}

export const PERMISSIONS: Permission[] = [
  { id: "PERM-001", name: "user.create", module: "User Management", description: "Tạo tài khoản người dùng (UC-026)" },
  { id: "PERM-002", name: "user.toggle-status", module: "User Management", description: "Kích hoạt / vô hiệu hóa tài khoản (UC-003)" },
  { id: "PERM-003", name: "role.manage", module: "User Management", description: "Tạo/sửa/xóa vai trò tùy chỉnh (UC-022/098/029)" },
  { id: "PERM-004", name: "school.view", module: "School Management", description: "Xem danh sách & hồ sơ trường (UC-140/122)" },
  { id: "PERM-005", name: "school.update", module: "School Management", description: "Cập nhật hồ sơ trường, mở khóa tọa độ GPS (UC-095, BR-028)" },
  { id: "PERM-006", name: "school.toggle-status", module: "School Management", description: "Kích hoạt / vô hiệu hóa trường — cascade (UC-001, BR-021)" },
  { id: "PERM-007", name: "classroom.manage", module: "School Management", description: "Quản lý lớp học, phân công GVCN (UC-018/090/008)" },
  { id: "PERM-008", name: "student.manage", module: "Student & Parent", description: "Quản lý hồ sơ học sinh, liên kết phụ huynh (UC-025/099, BR-030)" },
  { id: "PERM-009", name: "menu.approve", module: "Menu Management", description: "Phê duyệt / từ chối thực đơn (UC-006, BR-068)" },
  { id: "PERM-010", name: "partnership.approve", module: "Partnership", description: "Duyệt hồ sơ NCC / đơn vị suất ăn (UC-007/004)" },
  { id: "PERM-011", name: "incident.close", module: "Incident Management", description: "Đóng sự cố sau khi CAPA hoàn thành (UC-013, BR-155)" },
  { id: "PERM-012", name: "violation.approve", module: "Violation Management", description: "Phê duyệt biên bản xử lý vi phạm (UC-182, BR-186)" },
  { id: "PERM-013", name: "report.view", module: "Reporting & Analytics", description: "Xem báo cáo trong phạm vi được cấp (UC-171, BR-175/176)" },
  { id: "PERM-014", name: "report.export", module: "Reporting & Analytics", description: "Xuất báo cáo PDF/Excel (UC-172, BR-178)" },
  { id: "PERM-015", name: "audit.view", module: "System", description: "Truy vấn audit log bất biến (UC-106, BR-162)" },
  { id: "PERM-016", name: "system.configure", module: "System", description: "Cấu hình bảo mật, tích hợp ngoài (UC-184/040)" },
];

// ===== Vai trò (UC-130/022/098/029 — BR-008) =====

export interface RoleRecord {
  id: string;
  name: string;
  description: string;
  users: number;
  /** BR-008: role hệ thống mặc định không được sửa tên/xóa */
  system: boolean;
  permissions: string[];
}

export const ROLE_RECORDS: RoleRecord[] = [
  { id: "ROLE-01", name: "System Admin", description: "Quản trị toàn nền tảng", users: 2, system: true, permissions: ["PERM-001", "PERM-002", "PERM-003", "PERM-004", "PERM-005", "PERM-006", "PERM-015", "PERM-016"] },
  { id: "ROLE-02", name: "School Administration", description: "Ban giám hiệu — phê duyệt & quản lý cấp trường", users: 8, system: true, permissions: ["PERM-007", "PERM-008", "PERM-009", "PERM-010", "PERM-011", "PERM-012", "PERM-013", "PERM-014", "PERM-015"] },
  { id: "ROLE-03", name: "Medical Staff", description: "Y tế trường — xác minh sức khỏe, giám sát ATTP", users: 12, system: true, permissions: ["PERM-008", "PERM-013"] },
  { id: "ROLE-04", name: "Service Caterers", description: "Đơn vị suất ăn — vận hành bếp", users: 26, system: true, permissions: ["PERM-013"] },
  { id: "ROLE-05", name: "Teacher", description: "Giáo viên chủ nhiệm — phân phát bữa ăn", users: 214, system: true, permissions: ["PERM-013"] },
  { id: "ROLE-06", name: "Parent", description: "Phụ huynh — khai báo & theo dõi", users: 4380, system: true, permissions: [] },
  { id: "ROLE-07", name: "Raw Food Supplier", description: "Nhà cung cấp nguyên liệu", users: 31, system: true, permissions: [] },
  { id: "ROLE-08", name: "Kitchen Auditor", description: "Vai trò tùy chỉnh — kiểm toán bếp độc lập", users: 0, system: false, permissions: ["PERM-013", "PERM-015"] },
];

// ===== Nhân viên trường (UC-144/024/002 — School Administration) =====

export interface StaffMember {
  id: string;
  name: string;
  email: string;
  phone: string;
  role: "Teacher" | "Medical Staff" | "Service Caterers";
  status: keyof typeof STATUS_BADGE; // ACTIVE | INACTIVE | PENDING
  /** BR-010: trạng thái xác minh email (Invitation Status) */
  emailVerified: "VERIFIED" | "PENDING";
  invitedAt: string;
}

export const SCHOOL_STAFF: StaffMember[] = [
  { id: "STF-001", name: "Nguyễn Thu Trang", email: "trangnt@dtd.edu.vn", phone: "0912 111 222", role: "Teacher", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2025" },
  { id: "STF-002", name: "Phan Thị Hồng", email: "hongpt@dtd.edu.vn", phone: "0912 333 444", role: "Teacher", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2025" },
  { id: "STF-003", name: "Đào Văn Quân", email: "quandv@dtd.edu.vn", phone: "0913 555 666", role: "Teacher", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2025" },
  { id: "STF-004", name: "Lưu Thị Bích", email: "bichlt@dtd.edu.vn", phone: "0914 777 888", role: "Teacher", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2025" },
  { id: "STF-005", name: "Trịnh Minh Đức", email: "ductm@dtd.edu.vn", phone: "0915 999 000", role: "Teacher", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2025" },
  { id: "STF-006", name: "Ngô Thị Hà", email: "hant@dtd.edu.vn", phone: "0916 121 212", role: "Medical Staff", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2025" },
  { id: "STF-007", name: "Trần Văn Hùng", email: "hungtv@anbinhcatering.vn", phone: "0904 123 456", role: "Service Caterers", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "12/08/2025" },
  { id: "STF-008", name: "Lê Thị Mai", email: "maile@anbinhcatering.vn", phone: "0905 654 321", role: "Service Caterers", status: "ACTIVE", emailVerified: "VERIFIED", invitedAt: "12/08/2025" },
  { id: "STF-009", name: "Hà Thị Lan", email: "lanht@dtd.edu.vn", phone: "0917 232 323", role: "Teacher", status: "PENDING", emailVerified: "PENDING", invitedAt: "28/06/2026" },
  { id: "STF-010", name: "Vũ Thu Hường", email: "huongvt@dtd.edu.vn", phone: "0918 343 434", role: "Teacher", status: "PENDING", emailVerified: "PENDING", invitedAt: "28/06/2026" },
  { id: "STF-011", name: "Đinh Văn Tú", email: "tudv@dtd.edu.vn", phone: "0919 454 545", role: "Teacher", status: "INACTIVE", emailVerified: "VERIFIED", invitedAt: "10/08/2024" },
];

// ===== Nhật ký lỗi hệ thống (UC-111 — System Admin) =====

export interface ErrorLog {
  id: string;
  timestamp: string;
  message: string;
  severity: keyof typeof STATUS_BADGE; // CRITICAL | HIGH | MEDIUM | LOW
  module: string;
  path: string;
  stackTrace: string;
  sessionInfo: string;
}

export const ERROR_LOGS: ErrorLog[] = [
  { id: "ERR-5521", timestamp: "02/07/2026 09:41:12", message: "Gemini AI request timeout sau 10s", severity: "HIGH", module: "MenuRecommendationService", path: "/api/menus/recommendations", stackTrace: "TimeoutException: upstream deadline exceeded\n  at GeminiClient.generate(gemini_client.ts:88)\n  at MenuRecommendationService.suggest(menu_reco.ts:41)", sessionInfo: "1 phiên Service Caterers bị ảnh hưởng (hungtv@anbinhcatering.vn)" },
  { id: "ERR-5518", timestamp: "01/07/2026 22:14:37", message: "Đăng nhập thất bại 5 lần liên tiếp — khóa tài khoản 15 phút (BR-002)", severity: "MEDIUM", module: "AuthService", path: "/api/auth/login", stackTrace: "AccountLockedException (BR-002)\n  at AuthService.login(auth_service.ts:120)", sessionInfo: "IP 203.113.99.87 — không có phiên hợp lệ" },
  { id: "ERR-5515", timestamp: "01/07/2026 14:02:55", message: "FCM push thất bại tới 3 thiết bị (token hết hạn)", severity: "LOW", module: "NotificationService", path: "/api/notifications/push", stackTrace: "FcmError: UNREGISTERED\n  at FcmClient.send(fcm_client.ts:52)", sessionInfo: "3 thiết bị Parent — retry theo lịch" },
  { id: "ERR-5509", timestamp: "30/06/2026 12:40:08", message: "DB connection pool đạt 95% khi burst điều tra sự cố", severity: "CRITICAL", module: "Database", path: "pool://primary", stackTrace: "PoolExhaustedWarning: 57/60 connections\n  at Pool.acquire(pool.ts:203)", sessionInfo: "Toàn hệ thống — tự hồi phục sau 4 phút" },
  { id: "ERR-5502", timestamp: "29/06/2026 08:15:44", message: "GPS Service trả sai số > 50m cho 1 phiên giao hàng", severity: "MEDIUM", module: "GpsService", path: "/api/deliveries/verify-location", stackTrace: "AccuracyWarning: accuracy=62m > threshold\n  at GpsService.verify(gps_service.ts:77)", sessionInfo: "1 phiên Supplier (tamdv@greenfarm.vn)" },
];

// ===== Báo cáo lạm dụng manual override hàng tuần (UC-183, BR-190) =====

export interface OverrideAbuseRow {
  id: string;
  week: string;
  staffName: string;
  staffRole: string;
  overrideCount: number;
  threshold: number;
  context: string;
  severity: keyof typeof STATUS_BADGE;
  violationId?: string;
}

export const OVERRIDE_ABUSE_REPORT: OverrideAbuseRow[] = [
  { id: "OVR-W27-01", week: "Tuần 27 (29/06–05/07)", staffName: "Trần Văn Hùng", staffRole: "Service Caterers", overrideCount: 4, threshold: 3, context: "Override cảnh báo FEFO khi xuất kho (BATCH-GF-4410)", severity: "MEDIUM", violationId: "VIO-2026-008" },
  { id: "OVR-W27-02", week: "Tuần 27 (29/06–05/07)", staffName: "Lê Thị Mai", staffRole: "Service Caterers", overrideCount: 2, threshold: 3, context: "Override xác nhận nhiệt độ tủ mẫu lưu (chênh 0.4°C)", severity: "LOW" },
  { id: "OVR-W26-01", week: "Tuần 26 (22/06–28/06)", staffName: "Trần Văn Hùng", staffRole: "Service Caterers", overrideCount: 6, threshold: 3, context: "Lạm dụng manual override khi xuất kho — đã lập biên bản OVERRIDE_ABUSE", severity: "HIGH", violationId: "VIO-2026-005" },
];

// ===== Yêu cầu bữa ăn đặc biệt (UC-036/078, BR-066) =====
/** Danh mục chế độ ăn đặc biệt chuẩn (BR-066) */
export const STANDARD_DIETS = ["Halal", "Vegetarian", "Gluten-Free", "Nut-Free", "Lactose-Free"];

export interface SpecialMealRequest {
  id: string;
  /**
   * Khoá về học sinh. Trước 21/07/2026 chỉ có `student` + `class` dạng chuỗi hiển
   * thị nên màn Phụ huynh không lọc được "yêu cầu của con tôi" — vì thế nó dùng
   * một mảng seed riêng (`PARENT_SPECIAL_MEAL_REQUESTS`), tách hẳn khỏi mảng mà
   * Y tế trường duyệt ⇒ phụ huynh gửi yêu cầu thì Y tế không bao giờ nhận được.
   */
  studentId: string;
  student: string;
  class: string;
  /** BR-066: phải thuộc danh mục chuẩn Halal/Vegetarian/Gluten-Free/Nut-Free... */
  dietaryType: string;
  duration: string;
  justification: string;
  hasDocument: boolean;
  documentName?: string;
  requestedBy: string;
  date: string;
  status: keyof typeof STATUS_BADGE; // PENDING | APPROVED | REJECTED | NEEDS_INFO
  reviewNote?: string;
  reviewedBy?: string;
}

/**
 * MỘT nguồn duy nhất cho yêu cầu bữa ăn đặc biệt: phụ huynh gửi vào đây, Y tế
 * trường duyệt tại đây (resource `special-meals`). Mảng riêng của màn Phụ huynh
 * đã gộp vào (SMR-2026-019).
 */
export const SPECIAL_MEAL_REQUESTS: SpecialMealRequest[] = [
  { id: "SMR-2026-019", studentId: "HS-3A-001", student: "Nguyễn Minh Khang", class: "3A", dietaryType: "Nut-Free", duration: "Cả năm học 2026–2027", justification: "Dị ứng Tôm, Cua mức Nặng — tránh chế biến chung để phòng nhiễm chéo.", hasDocument: true, documentName: "don-bac-si-khang.pdf", requestedBy: "Nguyễn Văn Bình (PH)", date: "15/01/2026", status: "APPROVED", reviewedBy: "Ngô Thị Hà (Y tế)", reviewNote: "Đơn bác sĩ hợp lệ; đã yêu cầu Caterer chế biến tách biệt (BR-082)." },
  { id: "SMR-2026-045", studentId: "HS-3A-005", student: "Vũ Ngọc Diệp", class: "3A", dietaryType: "Lactose-Free", duration: "Cả năm học 2026–2027", justification: "Không dung nạp lactose — đang chờ giấy xác nhận của bác sĩ tiêu hóa.", hasDocument: false, requestedBy: "Vũ Đình Phong (PH)", date: "01/07/2026", status: "PENDING" },
  { id: "SMR-2026-044", studentId: "HS-3A-004", student: "Phạm Đức Minh", class: "3A", dietaryType: "Vegetarian", duration: "Không thời hạn", justification: "Gia đình ăn chay trường theo tôn giáo.", hasDocument: true, documentName: "don-de-nghi-an-chay.pdf", requestedBy: "Phạm Văn Cường (PH)", date: "01/07/2026", status: "PENDING" },
  { id: "SMR-2026-046", studentId: "HS-3B-003", student: "Cao Minh Anh", class: "3B", dietaryType: "Halal", duration: "Cả năm học", justification: "Gia đình theo đạo Hồi, yêu cầu suất ăn Halal.", hasDocument: true, documentName: "giay-xac-nhan-cong-dong.pdf", requestedBy: "Cao Văn Đại (PH)", date: "02/07/2026", status: "PENDING" },
  { id: "SMR-2026-041", studentId: "HS-3A-003", student: "Lê Gia Hân", class: "3A", dietaryType: "Nut-Free", duration: "Cả năm học 2026–2027", justification: "Dị ứng đậu phộng mức phản vệ — có đơn bác sĩ Nhi Trung ương.", hasDocument: true, documentName: "don-bac-si-nhi-trung-uong.pdf", requestedBy: "Lê Hoàng Nam (PH)", date: "28/06/2026", status: "APPROVED", reviewedBy: "Ngô Thị Hà (Y tế)", reviewNote: "Đơn bác sĩ hợp lệ; đã yêu cầu Caterer chế biến tách biệt (BR-082)." },
  { id: "SMR-2026-038", studentId: "HS-5A-002", student: "Chu Ngọc Hân", class: "5A", dietaryType: "Nut-Free", duration: "Cả năm học", justification: "Dị ứng đậu phộng.", hasDocument: true, documentName: "so-kham-di-ung.jpg", requestedBy: "Chu Đức Long (PH)", date: "20/06/2026", status: "APPROVED", reviewedBy: "Ngô Thị Hà (Y tế)" },
  { id: "SMR-2026-035", studentId: "HS-3A-010", student: "Đỗ Quang Huy", class: "3A", dietaryType: "Keto", duration: "3 tháng", justification: "Gia đình muốn áp dụng chế độ ăn kiêng riêng.", hasDocument: false, requestedBy: "Đỗ Mạnh Hùng (PH)", date: "15/06/2026", status: "REJECTED", reviewedBy: "Ngô Thị Hà (Y tế)", reviewNote: "Loại chế độ ăn không thuộc danh mục chuẩn hỗ trợ (BR-066)." },
];

// ===== Lịch sử sức khỏe học sinh (UC-146, BR-172 append-only) =====

export interface HealthHistoryEvent {
  id: string;
  studentId: string;
  at: string;
  type: "Khai báo" | "Xác minh" | "Ghi chú lâm sàng" | "Sự cố" | "Cập nhật thuốc";
  event: string;
  by: string;
}

export const HEALTH_HISTORY: HealthHistoryEvent[] = [
  { id: "HH-101", studentId: "HS-3A-001", at: "05/09/2025 08:10", type: "Khai báo", event: "Phụ huynh khai báo dị ứng Tôm, Cua (mức Severe) kèm đơn bác sĩ.", by: "Nguyễn Văn Bình (PH)" },
  { id: "HH-102", studentId: "HS-3A-001", at: "06/09/2025 09:30", type: "Xác minh", event: "Y tế xác minh hồ sơ dị ứng — kích hoạt cảnh báo phục vụ (BR-123).", by: "Ngô Thị Hà (Y tế)" },
  { id: "HH-103", studentId: "HS-3A-001", at: "14/01/2026 10:45", type: "Ghi chú lâm sàng", event: "Lên cơn hen nhẹ sau giờ thể dục, đã xịt Ventolin, theo dõi 30 phút, ổn định.", by: "Ngô Thị Hà (Y tế)" },
  { id: "HH-104", studentId: "HS-3A-001", at: "18/06/2026 12:30", type: "Sự cố", event: "Nổi mẩn nghi dị ứng tôm (INC-2026-012) — xử trí kháng histamin, phụ huynh đón về.", by: "Ngô Thị Hà (Y tế)" },
  { id: "HH-105", studentId: "HS-3A-001", at: "19/06/2026 08:00", type: "Cập nhật thuốc", event: "Bổ sung Loratadin 5mg dự phòng theo đơn tái khám.", by: "Ngô Thị Hà (Y tế)" },
  { id: "HH-201", studentId: "HS-3A-003", at: "05/09/2025 08:20", type: "Khai báo", event: "Khai báo dị ứng Đậu phộng (Severe) + đơn bác sĩ Nhi Trung ương.", by: "Lê Hoàng Nam (PH)" },
  { id: "HH-202", studentId: "HS-3A-003", at: "05/09/2025 15:00", type: "Xác minh", event: "Y tế xác minh, ghi nhận EpiPen Jr. dự phòng tại phòng y tế.", by: "Ngô Thị Hà (Y tế)" },
  { id: "HH-301", studentId: "HS-3A-005", at: "28/06/2026 20:00", type: "Khai báo", event: "Khai báo không dung nạp lactose — thiếu giấy xác nhận bác sĩ.", by: "Vũ Đình Phong (PH)" },
  { id: "HH-302", studentId: "HS-3A-005", at: "29/06/2026 09:00", type: "Xác minh", event: "Y tế yêu cầu bổ sung tài liệu (trạng thái Cần bổ sung — BR-031).", by: "Ngô Thị Hà (Y tế)" },
];

// ===== Thông báo cá nhân (Notification Center — Report 3 §3.3.2.1.13, BR-158/159) =====

/**
 * Ai nhận thông báo.
 *
 * Trước 22/07/2026 "gửi toàn hệ thống" được diễn đạt bằng cách BỎ TRỐNG
 * `recipientRoles` — tức thiếu dữ liệu mang nghĩa nghiệp vụ. Không phân biệt được
 * "cố ý gửi tất cả" với "quên khai người nhận", và backend thì không đặt được
 * ràng buộc NOT NULL cho cột nào cả. Khai thẳng ra một enum.
 */
export type NotificationAudience = "ALL" | "ROLES";

export interface UserNotification {
  id: string;
  /**
   * Vai nhận thông báo. Trước 22/07/2026 model không có trường này nên MỌI vai
   * dùng chung một hộp: phụ huynh thấy việc của Y tế, đơn vị suất ăn thấy thông
   * báo gửi Ban giám hiệu. Thông báo là kênh điều phối liên vai nên thiếu người
   * nhận là hỏng đúng chức năng chính của nó.
   *
   * Để mảng vì một sự kiện có thể gửi nhiều vai (sự cố ATTP → Y tế + Ban giám hiệu).
   */
  audience: NotificationAudience;
  /** Chỉ có nghĩa khi `audience = "ROLES"`. */
  recipientRoles?: RoleKey[];
  /** ALLERGY/FOOD_SAFETY = badge đỏ · NEWS = vàng · SYSTEM = xám */
  type: "ALLERGY" | "FOOD_SAFETY" | "NEWS" | "SYSTEM";
  title: string;
  content: string;
  sentAt: string;
  read: boolean;
  /** Deep link theo Navigation_Guidelines.md mục 3 */
  link?: string;
  linkLabel?: string;
}

export const NOTIFICATIONS: UserNotification[] = [
  { audience: "ROLES", id: "NTF-901", recipientRoles: ["medical-staff"], type: "ALLERGY", title: "Cảnh báo dị ứng — khai báo mới chờ xác minh", content: "Phụ huynh Vũ Đình Phong vừa cập nhật khai báo không dung nạp lactose cho học sinh Vũ Ngọc Diệp (3A). Thông tin chỉ có hiệu lực sau khi Y tế xác minh (BR-031).", sentAt: "02/07/2026 06:45", read: false, link: "/medical-staff/health-reviews", linkLabel: "Duyệt khai báo" },
  { audience: "ROLES", id: "NTF-900", recipientRoles: ["medical-staff", "school-admin"], type: "FOOD_SAFETY", title: "Sự cố INC-2026-014 — kết quả CDC sơ bộ", content: "CDC Hà Nội gửi kết quả sơ bộ mẫu SMP-20260630-03: đang chờ nuôi cấy 48h. Bản đồ truy xuất đã hoàn tất trong 4 giờ (BR-151).", sentAt: "01/07/2026 15:30", read: false, link: "/medical-staff/incidents/INC-2026-014", linkLabel: "Xem sự cố" },
  { audience: "ROLES", id: "NTF-899", recipientRoles: ["school-admin"], type: "FOOD_SAFETY", title: "CAPA-2026-033 chờ Ban giám hiệu duyệt kế hoạch", content: "Kế hoạch khắc phục sự cố INC-2026-014 đã gửi Ban giám hiệu duyệt trước khi đơn vị suất ăn thực hiện (BR-153/BR-216). Không đặt hạn SLA — hệ thống chỉ ghi mốc gửi và mốc nộp bằng chứng.", sentAt: "01/07/2026 09:05", read: true, link: "/school-admin/corrective-actions", linkLabel: "Duyệt kế hoạch" },
  { audience: "ROLES", id: "NTF-897", recipientRoles: ["school-admin"], type: "NEWS", title: "Thực đơn tuần 28 chờ phê duyệt", content: "Cty TNHH Suất ăn An Bình đã nộp thực đơn tuần 28 (06/07–10/07), có 1 cảnh báo xung đột dị ứng cần rà soát.", sentAt: "01/07/2026 08:00", read: true },
  { audience: "ALL", id: "NTF-895", type: "SYSTEM", title: "Bảo trì hệ thống 05/07 (22:00–23:00)", content: "Hệ thống SFMS bảo trì định kỳ. Trong thời gian này chức năng ghi nhận dữ liệu tạm gián đoạn; cảnh báo khẩn cấp vẫn hoạt động (BR-158).", sentAt: "30/06/2026 17:00", read: true },
];

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
export interface SecurityPolicy {
  id: string;
  /** BR-002 — số lần đăng nhập sai tối đa (3–10) và thời gian khoá (5–60 phút). */
  maxLoginAttempts: number;
  lockoutMinutes: number;
  /** BR-004 — phút (10–120). */
  sessionTimeoutMinutes: number;
  /** BR-006 — độ mạnh mật khẩu. */
  passwordMinLength: number;
  requireSpecialChar: boolean;
  requireNumber: boolean;
  requireMixedCase: boolean;
  /** Không dùng lại 3 mật khẩu gần nhất. */
  noReuseLast3: boolean;
  updatedAt?: string;
  updatedBy?: string;
}

export const SECURITY_POLICIES: SecurityPolicy[] = [
  {
    id: "SEC-DEFAULT",
    maxLoginAttempts: 5,
    lockoutMinutes: 15,
    sessionTimeoutMinutes: 30,
    passwordMinLength: 8,
    requireSpecialChar: true,
    requireNumber: true,
    requireMixedCase: true,
    noReuseLast3: true,
    updatedAt: "01/07/2026 08:15",
    updatedBy: "Nguyễn Hải Đăng (System Admin)",
  },
];

/**
 * Dịch vụ ngoài (UC-183, BR-163).
 *
 * Danh sách này từng tồn tại HAI BẢN: màn "Tích hợp ngoài & AI" khai một mảng,
 * màn "Trạng thái hệ thống" khai một mảng khác — và chúng nói ngược nhau về
 * Gemini (một bên "chưa kết nối", một bên "đang chạy, chậm 2.1s"). Quản trị viên
 * mở hai màn cạnh nhau thấy hai sự thật.
 */
export interface ExternalIntegration {
  id: string;
  label: string;
  endpoint: string;
  endpointPlaceholder: string;
  /** BR-163: phải Test connection thành công mới được lưu; đây là kết quả lần test gần nhất. */
  connected: boolean;
  /** Ghi chú vận hành hiển thị ở màn Trạng thái hệ thống. */
  statusNote?: string;
  purpose: string;
  lastTestedAt?: string;
}

export const EXTERNAL_INTEGRATIONS: ExternalIntegration[] = [
  { id: "gps", label: "GPS Service (Google Maps)", endpoint: "https://maps.googleapis.com/maps/api", endpointPlaceholder: "https://maps.googleapis.com/...", connected: true, purpose: "Xác minh vị trí giao hàng (BR-073/074)", lastTestedAt: "02/07/2026 06:00" },
  { id: "gmail", label: "Email Service (Gmail SMTP)", endpoint: "smtp.gmail.com:587", endpointPlaceholder: "smtp.gmail.com:587", connected: true, purpose: "Gửi email xác minh tài khoản (BR-010)", lastTestedAt: "02/07/2026 06:00" },
  { id: "firebase", label: "Firebase Cloud Messaging", endpoint: "https://fcm.googleapis.com/v1", endpointPlaceholder: "https://fcm.googleapis.com/...", connected: true, purpose: "Push notification khẩn cấp (BR-148)", lastTestedAt: "02/07/2026 06:00" },
  { id: "gemini", label: "Gemini AI Service", endpoint: "https://generativelanguage.googleapis.com/v1beta", endpointPlaceholder: "https://generativelanguage.googleapis.com/...", connected: false, statusNote: "Chưa kết nối — thiếu API key hợp lệ", purpose: "Gợi ý thực đơn — pipeline RAG (ADR-003)" },
];

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
export interface ReceivingAuthority {
  id: string;
  name: string;
  /** LAB = gửi xét nghiệm, chờ kết quả · AUTHORITY = cơ quan chức năng thu giữ. */
  type: "LAB" | "AUTHORITY";
  /** Đầu mối nhận mẫu — dùng khi bàn giao. */
  contact?: string;
  address?: string;
  /** Ngừng hợp tác thì không chỉ định mới được nữa, hồ sơ cũ vẫn tra được. */
  isActive: boolean;
  note?: string;
}

export const RECEIVING_AUTHORITIES: ReceivingAuthority[] = [
  { id: "LAB-CDC-HN", name: "CDC Hà Nội", type: "LAB", contact: "Nguyễn Thị Vân — 024 3736 1211", address: "70 Nguyễn Chí Thanh, Đống Đa, Hà Nội", isActive: true, note: "Cử cán bộ tới trường lấy mẫu trong ngày làm việc" },
  { id: "LAB-NIFC", name: "Viện Kiểm nghiệm ATVSTP Quốc gia", type: "LAB", contact: "Phòng tiếp nhận mẫu — 024 3971 4045", address: "65 Phạm Thận Duật, Cầu Giấy, Hà Nội", isActive: true },
  { id: "LAB-TTYT-QUAN", name: "Trung tâm Y tế quận Nam Từ Liêm", type: "LAB", contact: "Khoa Xét nghiệm — 024 3837 2222", address: "Mỹ Đình 2, Nam Từ Liêm, Hà Nội", isActive: true },
  { id: "AUTH-CA-QUAN", name: "Công an quận Nam Từ Liêm", type: "AUTHORITY", contact: "Đội Cảnh sát kinh tế — 069 219 5555", isActive: true, note: "Thu giữ mẫu làm vật chứng; trường giữ biên bản thu giữ, không nhận kết quả xét nghiệm" },
  { id: "AUTH-TT-SYT", name: "Thanh tra Sở Y tế Hà Nội", type: "AUTHORITY", contact: "Phòng Thanh tra — 024 3998 5765", isActive: true, note: "Thu mẫu phục vụ thanh tra ATTP" },
];

// ===== Ký xác nhận an toàn thực phẩm hằng ngày (UC-027 bước 5, BR-091/100/104) =====

/**
 * Chữ ký xác nhận trạng thái an toàn thực phẩm trong ngày của Ban giám hiệu.
 *
 * Trước 22/07/2026 nút "Ký xác nhận" chỉ đặt `useState(true)`: tải lại trang là
 * mất, không vai nào khác thấy, và khi thanh tra hỏi "hôm đó ai xác nhận" thì
 * không có gì để đưa ra. Đây là bản ghi tuân thủ, không phải trạng thái giao diện.
 */
export interface SafetySignoff {
  id: string;
  /** Ngày được xác nhận (dd/MM/yyyy) — mỗi ngày đúng một chữ ký. */
  date: string;
  signedBy: string;
  signedAt: string;
  /** Chụp lại căn cứ tại thời điểm ký, để sau này không phải dựng lại. */
  hygieneScore?: number;
  failedSteps: number;
  note?: string;
}

export const SAFETY_SIGNOFFS: SafetySignoff[] = [
  { id: "SGN-20260706", date: "06/07/2026", signedBy: "Phạm Quốc Toản (BGH)", signedAt: "06/07/2026 13:40", hygieneScore: 92, failedSteps: 0, note: "Ba bước kiểm thực đạt, điểm vệ sinh bếp 92." },
];

// ===== Chính sách trường học (UC-137, BR-022) =====

export interface SchoolPolicy {
  id: string;
  title: string;
  category: string;
  issuedAt: string;
  issuedBy: string;
  content: string;
}

export const SCHOOL_POLICIES: SchoolPolicy[] = [
  { id: "POL-001", title: "Quy định an toàn thực phẩm bếp ăn bán trú", category: "An toàn thực phẩm", issuedAt: "15/08/2025", issuedBy: "BGH — Phạm Quốc Toản", content: "1. Bếp ăn thực hiện kiểm thực ba bước theo QĐ 1246/QĐ-BYT cho 100% bữa ăn.\n2. Mẫu lưu ≥100g món đặc / ≥150ml món lỏng, lưu 72 giờ ở 2–8°C (BR-110/111/203).\n3. Nhân viên bếp khám sức khỏe định kỳ hàng năm, có xác nhận đủ điều kiện (BR-103).\n4. Kiểm soát côn trùng thực hiện hàng quý bởi đơn vị có chứng nhận (BR-102).\n5. Điểm vệ sinh bếp dưới 60% → cách ly bếp, dừng phân phát bữa ăn (BR-105)." },
  { id: "POL-002", title: "Quy trình xử lý sự cố an toàn thực phẩm", category: "An toàn thực phẩm", issuedAt: "15/08/2025", issuedBy: "BGH — Phạm Quốc Toản", content: "1. ≥3 học sinh cùng triệu chứng trong 24h → tự động kích hoạt điều tra (BR-146).\n2. Y tế phân loại mức độ Low/Medium/High trong 2 giờ (BR-147); High → cảnh báo cơ quan y tế địa phương ngay (BR-148).\n3. Lô nguyên liệu nghi vấn cách ly tự động (BR-149); bản đồ truy xuất hoàn tất trong 4 giờ (BR-151).\n4. CAPA giao cho đơn vị suất ăn với hạn 12 giờ (BR-152); sự cố chỉ đóng khi Y tế xác nhận học sinh hồi phục 100% (BR-155)." },
  { id: "POL-003", title: "Quy định quản lý dị ứng & bữa ăn đặc biệt", category: "Sức khỏe học sinh", issuedAt: "20/08/2025", issuedBy: "Y tế — Ngô Thị Hà", content: "1. Khai báo dị ứng của phụ huynh chỉ có hiệu lực sau khi Y tế xác minh (BR-031); mức Severe bắt buộc kèm tài liệu y tế.\n2. Danh mục chế độ ăn đặc biệt hỗ trợ: Halal, Vegetarian, Gluten-Free, Nut-Free, Lactose-Free (BR-066).\n3. Suất ăn đặc biệt chế biến tách biệt, dán nhãn riêng (BR-082).\n4. Quét ID học sinh có hồ sơ dị ứng đã xác minh khi phục vụ sẽ kích hoạt cảnh báo trực quan + âm thanh (BR-123)." },
  { id: "POL-004", title: "Nội quy phòng y tế học đường", category: "Y tế học đường", issuedAt: "01/09/2025", issuedBy: "Y tế — Ngô Thị Hà", content: "1. Hồ sơ sức khỏe học sinh là dữ liệu xuyên suốt, chỉ bổ sung (append-only), không sửa/xóa lịch sử đã xác minh (BR-172).\n2. Thuốc dự phòng cá nhân (EpiPen, Ventolin...) lưu tại phòng y tế kèm đơn bác sĩ còn hiệu lực.\n3. Mọi ca xử trí tại phòng y tế phải ghi nhận vào lịch sử sức khỏe trong ngày." },
];

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

export const PARENT_CHILDREN: ParentChild[] = [
  {
    studentId: "HS-3A-001",
    name: "Nguyễn Minh Khang",
    class: "3A",
    dob: "12/03/2018",
    age: 8,
    school: "TH Đoàn Thị Điểm",
  },
  {
    studentId: "HS-1B-007",
    name: "Nguyễn Minh Anh",
    class: "1B",
    dob: "25/09/2020",
    age: 6,
    school: "TH Đoàn Thị Điểm",
  },
];



// ===== Hồ sơ sức khỏe học sinh (07/08/09 — UC-050/084/093, BR-031) =====

export interface HealthRecord {
  /**
   * review/parent (`review_parent_data_linkage_plan.md` Đợt 0, 27/07/2026):
   * mỗi hồ sơ sức khỏe gắn với MỘT học sinh cụ thể — trước đó `HEALTH_RECORD`
   * là object đơn dùng chung cho mọi con, khiến 2 con của cùng phụ huynh hiển
   * thị y hệt nhau khi đổi con ở Topbar.
   */
  studentId: string;
  height: number; // cm
  weight: number; // kg
  bloodType: string;
  bmi: number;
  status: keyof typeof STATUS_BADGE; // PENDING | VERIFIED | NEEDS_INFO
  updatedAt: string;
  proofFile?: string;
}

export const HEALTH_RECORDS: HealthRecord[] = [
  {
    studentId: "HS-3A-001",
    height: 128,
    weight: 26.5,
    bloodType: "O",
    bmi: 16.2,
    status: "VERIFIED",
    updatedAt: "15/01/2026",
    proofFile: "kham-suc-khoe-2026.pdf",
  },
  {
    studentId: "HS-1B-007",
    height: 112,
    weight: 19.8,
    bloodType: "A",
    bmi: 15.8,
    status: "VERIFIED",
    updatedAt: "20/01/2026",
    proofFile: "kham-suc-khoe-2026.pdf",
  },
];

export function healthRecordOfStudent(studentId: string): HealthRecord | undefined {
  return HEALTH_RECORDS.find((r) => r.studentId === studentId);
}

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

export interface AllergyDeclaration {
  id: string;
  /**
   * BR-030: khai báo dị ứng thuộc về MỘT học sinh cụ thể. Trước 21/07/2026 field
   * này không tồn tại nên phụ huynh có nhiều con không biết dị ứng của đứa nào —
   * mọi màn Parent đều hiển thị chung một danh sách. Backend: đây là
   * AllergyProfile(student_id, allergen_id) — xem review_data_model_*.
   */
  studentId: string;
  category: string;
  allergen: string;
  severity: "Nhẹ" | "Trung bình" | "Nặng";
  declaredAt: string;
  status: keyof typeof STATUS_BADGE;
  proofFile?: string;
}

/**
 * KHAI BÁO DỊ ỨNG — nguồn sự thật duy nhất về dị ứng của học sinh (BR-030/031).
 *
 * Trước 22/07/2026 hệ thống có HAI mô hình song song và chúng cấp cho HAI luồng
 * khác nhau: màn duyệt thực đơn đọc `Student.allergies` + `healthStatus`, còn màn
 * phát suất đọc bảng này — vốn chỉ có 2 dòng cho 1 em trong khi 9 em có dị ứng.
 * Hệ quả không phải là chuyện gọn gàng mà là an toàn thực phẩm: Ban giám hiệu
 * được báo "đã xử lý xung đột dị ứng", nhưng em dị ứng Trứng vẫn được giao
 * "Thịt kho trứng cút" và em dị ứng Hải sản vẫn nhận "Canh bí đỏ nấu tôm", vì
 * bên phát suất tra ra danh sách rỗng.
 *
 * Nay mọi tác nhân đều có một dòng khai báo: ai khai, khai ngày nào, giấy tờ gì,
 * và TỪNG tác nhân được xác minh riêng — đúng BR-031. `Student.allergies` trở
 * thành trường DẪN XUẤT sinh từ bảng này (xem vòng backfill bên dưới); backend
 * không tạo cột đó, nó là một phép join.
 */
export const ALLERGY_DECLARATIONS: AllergyDeclaration[] = [
  { id: "ALG-001", studentId: "HS-3A-001", category: "Hải sản (tôm, cua, sò, ốc)", allergen: "Tôm", severity: "Nặng", declaredAt: "15/01/2026", status: "VERIFIED", proofFile: "don-bac-si-di-ung-tom-cua.pdf" }, // Nguyễn Minh Khang
  { id: "ALG-002", studentId: "HS-3A-001", category: "Hải sản (tôm, cua, sò, ốc)", allergen: "Cua", severity: "Nặng", declaredAt: "15/01/2026", status: "VERIFIED", proofFile: "don-bac-si-di-ung-tom-cua.pdf" }, // Nguyễn Minh Khang
  { id: "ALG-003", studentId: "HS-3A-003", category: "Đậu phộng & các loại hạt", allergen: "Đậu phộng", severity: "Nặng", declaredAt: "15/01/2026", status: "VERIFIED", proofFile: "don-bac-si-nhi-trung-uong.pdf" }, // Lê Gia Hân
  { id: "ALG-004", studentId: "HS-3A-005", category: "Sữa & chế phẩm từ sữa", allergen: "Sữa bò", severity: "Trung bình", declaredAt: "15/01/2026", status: "NEEDS_INFO" }, // Vũ Ngọc Diệp
  { id: "ALG-005", studentId: "HS-3A-009", category: "Trứng", allergen: "Trứng", severity: "Trung bình", declaredAt: "15/01/2026", status: "VERIFIED" }, // Ngô Khánh Vy
  { id: "ALG-006", studentId: "HS-3B-001", category: "Hải sản (tôm, cua, sò, ốc)", allergen: "Hải sản", severity: "Trung bình", declaredAt: "15/01/2026", status: "VERIFIED" }, // Trịnh Bảo Ngọc
  { id: "ALG-007", studentId: "HS-4A-001", category: "Đậu nành", allergen: "Đậu nành", severity: "Trung bình", declaredAt: "15/01/2026", status: "PENDING" }, // Vương Hà My
  { id: "ALG-008", studentId: "HS-4A-003", category: "Sữa & chế phẩm từ sữa", allergen: "Sữa bò", severity: "Trung bình", declaredAt: "15/01/2026", status: "VERIFIED" }, // Mai Phương Thảo
  { id: "ALG-009", studentId: "HS-4A-003", category: "Trứng", allergen: "Trứng", severity: "Trung bình", declaredAt: "15/01/2026", status: "VERIFIED" }, // Mai Phương Thảo
  { id: "ALG-010", studentId: "HS-5A-002", category: "Đậu phộng & các loại hạt", allergen: "Đậu phộng", severity: "Trung bình", declaredAt: "15/01/2026", status: "VERIFIED" }, // Chu Ngọc Hân
];

/**
 * `Student.allergies` là TRƯỜNG DẪN XUẤT — sinh từ bảng khai báo ở trên.
 *
 * Giữ lại vì 23 màn đang đọc nó, nhưng nó không còn là nguồn: sửa dị ứng thì sửa
 * ở khai báo, không sửa ở đây. Backend KHÔNG tạo cột này — nó là một phép join
 * `student ⋈ allergy_declaration`.
 */
for (const s of STUDENTS) {
  const declared = ALLERGY_DECLARATIONS.filter((a) => a.studentId === s.id).map((a) => a.allergen);
  if (declared.length) s.allergies = [...new Set(declared)];
}

/** Khai báo dị ứng của MỘT học sinh (BR-030) — nguồn chung cho mọi màn Parent. */
export function allergiesOfStudent(studentId: string): AllergyDeclaration[] {
  return ALLERGY_DECLARATIONS.filter((a) => a.studentId === studentId);
}

/** Tác nhân đã được Y tế xác minh của 1 học sinh — căn cứ cảnh báo/chặn (BR-031). */
export function verifiedAllergensOfStudent(studentId: string): string[] {
  return allergiesOfStudent(studentId)
    .filter((a) => a.status === "VERIFIED")
    .map((a) => a.allergen);
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

export interface ChronicConditionDeclaration {
  id: string;
  /**
   * BR-030: khai báo thuộc về MỘT học sinh. Thiếu trường này (đến 22/07/2026) thì
   * phụ huynh có nhiều con thấy chung một danh sách bệnh lý cho mọi đứa — đúng lỗi
   * mà `AllergyDeclaration` đã sửa trước đó. Backend: khoá ngoại về `student`.
   */
  studentId: string;
  category: string;
  /** Tên/chi tiết bệnh lý (bắt buộc khi chọn "Khác") */
  conditionName: string;
  /** Mức độ theo dõi */
  severity: "Nhẹ" | "Trung bình" | "Nặng";
  /** Ghi chú: thuốc đang dùng, lưu ý xử trí khẩn cấp */
  notes?: string;
  declaredAt: string;
  status: keyof typeof STATUS_BADGE;
  proofFile?: string;
}

export const PARENT_CHRONIC_CONDITIONS: ChronicConditionDeclaration[] = [
  {
    id: "CHR-001",
    // Khớp STUDENTS: HS-3A-001 có medicalConditions "Hen phế quản nhẹ".
    studentId: "HS-3A-001",
    category: "Hen phế quản / Hen suyễn",
    conditionName: "Hen phế quản nhẹ",
    severity: "Trung bình",
    notes: "Dùng Ventolin xịt khi lên cơn hen; tránh vận động gắng sức khi thời tiết lạnh.",
    declaredAt: "15/01/2026",
    status: "VERIFIED",
    proofFile: "chan-doan-hen-2026.pdf",
  },
];

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
  category: "Món chính" | "Món mặn" | "Canh" | "Rau" | "Bữa phụ";
  // `supplier`/`kitchen` gõ tay đã bỏ 23/07/2026: nguồn gốc suy từ nguyên liệu
  // (xem `dishSourcing`), còn bếp luôn là đơn vị suất ăn đang vận hành.
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

/**
 * Rút gọn khai BOM seed. `id` bỏ trống = nguyên liệu ngoài catalog (gia vị/rau thơm
 * chưa nhập kho) — UI sẽ báo "ngoài danh mục, không truy xuất được lô".
 */
function bom(
  ingredientId: string | undefined,
  name: string,
  quantity: string,
  unit = "g"
): DishIngredient {
  return ingredientId ? { ingredientId, name, quantity, unit } : { name, quantity, unit };
}

/** Định lượng là cho MỘT suất học sinh tiểu học (BR-062). */
export const DISH_LIBRARY: Dish[] = [
  { id: "com-trang", name: "Cơm trắng", category: "Món chính", kcal: 260, protein: 5, fat: 1, carb: 57, manualAllergens: [], isActive: true, ingredients: [bom("ING-GAO-TAM", "Gạo tám thơm", "80"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "thit-kho-trung-cut", name: "Thịt kho trứng cút", category: "Món mặn", kcal: 210, protein: 16, fat: 14, carb: 4, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "50"), bom("ING-TRUNG-CUT", "Trứng cút", "3", "quả"), bom("ING-NUOC-MAM", "Nước mắm truyền thống", "4", "ml"), bom("ING-DUONG", "Đường trắng", "3"), bom("ING-HANH-KHO", "Hành khô", "2")] },
  // `defaultSubstituteId` (G10): cặp thay thế dùng đi dùng lại trong thực tế —
  // khai ở thư viện để mọi thực đơn/mẫu tự điền, không phải gõ lại mỗi tuần.
  { id: "canh-bi-do-nau-tom", name: "Canh bí đỏ nấu tôm", category: "Canh", kcal: 80, protein: 6, fat: 2, carb: 9, manualAllergens: [], isActive: true, defaultSubstituteId: "canh-bi-do-chay", ingredients: [bom("ING-BI-DO", "Bí đỏ", "60"), bom("ING-TOM-SU", "Tôm sú", "15"), bom(undefined, "Hành lá", "2"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "su-su-xao", name: "Su su xào", category: "Rau", kcal: 70, protein: 2, fat: 4, carb: 8, manualAllergens: [], isActive: true, ingredients: [bom("ING-SU-SU", "Su su", "70"), bom("ING-DAU-AN", "Dầu ăn thực vật", "4", "ml"), bom("ING-TOI", "Tỏi", "2")] },
  { id: "ca-basa-sot-ca-chua", name: "Cá basa sốt cà chua", category: "Món mặn", kcal: 190, protein: 17, fat: 11, carb: 5, manualAllergens: [], isActive: true, defaultSubstituteId: "dau-phu-sot-ca-chua", ingredients: [bom("ING-CA-BASA", "Cá basa phi lê", "60"), bom("ING-CA-CHUA", "Cà chua", "30"), bom("ING-HANH-KHO", "Hành khô", "3"), bom("ING-NUOC-MAM", "Nước mắm truyền thống", "4", "ml"), bom("ING-DAU-AN", "Dầu ăn thực vật", "5", "ml")] },
  { id: "canh-rau-ngot-thit-bam", name: "Canh rau ngót thịt băm", category: "Canh", kcal: 75, protein: 6, fat: 3, carb: 6, manualAllergens: [], isActive: true, ingredients: [bom("ING-RAU-NGOT", "Rau ngót", "40"), bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "15"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "gia-xao", name: "Giá xào", category: "Rau", kcal: 60, protein: 3, fat: 3, carb: 6, manualAllergens: [], isActive: true, ingredients: [bom("ING-GIA-DO", "Giá đỗ", "70"), bom(undefined, "Hẹ", "5"), bom("ING-DAU-AN", "Dầu ăn thực vật", "4", "ml")] },
  { id: "ga-rang-gung", name: "Gà rang gừng", category: "Món mặn", kcal: 220, protein: 20, fat: 14, carb: 3, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-GA", "Thịt gà ta", "70"), bom("ING-GUNG", "Gừng", "3"), bom("ING-NUOC-MAM", "Nước mắm truyền thống", "4", "ml"), bom("ING-HANH-KHO", "Hành khô", "2")] },
  { id: "canh-mong-toi-nau-cua", name: "Canh mồng tơi nấu cua", category: "Canh", kcal: 85, protein: 7, fat: 3, carb: 7, manualAllergens: [], isActive: true, defaultSubstituteId: "canh-mong-toi", ingredients: [bom("ING-MONG-TOI", "Mồng tơi", "40"), bom("ING-CUA-DONG", "Cua đồng xay", "20"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "bap-cai-luoc", name: "Bắp cải luộc", category: "Rau", kcal: 40, protein: 2, fat: 0, carb: 8, manualAllergens: [], isActive: true, ingredients: [bom("ING-BAP-CAI", "Bắp cải", "80"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "thit-bo-xao-rau-cu", name: "Thịt bò xào rau củ", category: "Món mặn", kcal: 215, protein: 19, fat: 12, carb: 7, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-BO", "Thịt bò thăn", "55"), bom("ING-CA-ROT", "Cà rốt", "25"), bom(undefined, "Súp lơ", "25"), bom("ING-DAU-AN", "Dầu ăn thực vật", "5", "ml"), bom("ING-TOI", "Tỏi", "2")] },
  { id: "canh-chua-ca", name: "Canh chua cá", category: "Canh", kcal: 90, protein: 8, fat: 3, carb: 8, manualAllergens: [], isActive: true, ingredients: [bom("ING-CA-BASA", "Cá basa phi lê", "35"), bom("ING-CA-CHUA", "Cà chua", "25"), bom(undefined, "Dứa", "20"), bom("ING-GIA-DO", "Giá đỗ", "15"), bom(undefined, "Me", "3")] },
  { id: "dau-phu-sot", name: "Đậu phụ sốt", category: "Món mặn", kcal: 130, protein: 10, fat: 8, carb: 5, manualAllergens: [], isActive: true, ingredients: [bom("ING-DAU-PHU", "Đậu phụ", "80"), bom("ING-CA-CHUA", "Cà chua", "30"), bom(undefined, "Hành lá", "2"), bom("ING-DAU-AN", "Dầu ăn thực vật", "5", "ml")] },
  { id: "tom-rim-thit", name: "Tôm rim thịt", category: "Món mặn", kcal: 205, protein: 18, fat: 12, carb: 4, manualAllergens: [], isActive: true, defaultSubstituteId: "thit-kho-hanh", ingredients: [bom("ING-TOM-SU", "Tôm sú", "35"), bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "25"), bom("ING-NUOC-MAM", "Nước mắm truyền thống", "4", "ml"), bom("ING-DUONG", "Đường trắng", "3"), bom("ING-HANH-KHO", "Hành khô", "2")] },
  { id: "canh-khoai-tay-ca-rot", name: "Canh khoai tây cà rốt", category: "Canh", kcal: 85, protein: 3, fat: 2, carb: 15, manualAllergens: [], isActive: true, ingredients: [bom("ING-KHOAI-TAY", "Khoai tây", "45"), bom("ING-CA-ROT", "Cà rốt", "25"), bom(undefined, "Xương lợn", "20"), bom(undefined, "Hành lá", "2")] },
  // Bổ sung 22/07/2026: thư viện 16 món ban đầu KHÔNG có món nào thuộc nhóm "Bữa
  // phụ", nên 19/47 món trong thực đơn seed không gắn được `dishId`. Định lượng lấy
  // đúng theo thực đơn đã khai để hai bên không lệch; macro là ước lượng demo cùng
  // mức tin cậy với 16 món gốc.
  //
  // Đáng chú ý: nhóm này mang nhiều tác nhân dị ứng mà trước đây hệ thống không
  // thấy vì món không có mặt trong thư viện — sữa chua/sữa tươi (Sữa bò), bánh flan
  // (Trứng + Sữa bò), bánh mì phô mai và bánh quy (Gluten), lạc rang (Đậu phộng).
  // Món thay thế dùng thật trong seed (bản chay của "Canh bí đỏ nấu tôm") nhưng
  // chưa có trong thư viện — suất thay thế cũng phải nối được về dish master.
  /*
   * Bổ sung 22/07/2026 — các món vốn chỉ tồn tại như "suất thay thế" gắn trong
   * thực đơn. Từ khi bữa ăn đặc biệt là MỘT THỰC ĐƠN RIÊNG cho nhóm chế độ ăn
   * (không còn thay từng món), chúng phải là món thật trong thư viện thì thực đơn
   * riêng mới trỏ về được bằng `dishId`.
   */
  { id: "thit-kho-tau", name: "Thịt kho tàu", category: "Món mặn", kcal: 215, protein: 17, fat: 14, carb: 3, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "70"), bom("ING-NUOC-MAM", "Nước mắm truyền thống", "4", "ml")] },
  { id: "dau-phu-sot-ca-chua", name: "Đậu phụ sốt cà chua", category: "Món mặn", kcal: 195, protein: 12, fat: 11, carb: 10, manualAllergens: [], isActive: true, ingredients: [bom("ING-DAU-PHU", "Đậu phụ", "80"), bom("ING-CA-CHUA", "Cà chua", "40")] },
  { id: "dau-phu-rang-gung", name: "Đậu phụ rang gừng", category: "Món mặn", kcal: 190, protein: 12, fat: 11, carb: 8, manualAllergens: [], isActive: true, ingredients: [bom("ING-DAU-PHU", "Đậu phụ", "85"), bom("ING-GUNG", "Gừng", "3")] },
  { id: "ga-kho-gung", name: "Gà kho gừng", category: "Món mặn", kcal: 210, protein: 19, fat: 12, carb: 4, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-GA", "Thịt gà ta", "65"), bom("ING-GUNG", "Gừng", "3")] },
  { id: "canh-bi-do-chay", name: "Canh bí đỏ chay", category: "Canh", kcal: 70, protein: 2, fat: 1, carb: 14, manualAllergens: [], isActive: true, ingredients: [bom("ING-BI-DO", "Bí đỏ", "60"), bom("ING-HANH-KHO", "Hành khô", "2")] },
  { id: "canh-rau-ngot-chay", name: "Canh rau ngót chay", category: "Canh", kcal: 55, protein: 3, fat: 1, carb: 8, manualAllergens: [], isActive: true, ingredients: [bom("ING-RAU-NGOT", "Rau ngót", "60")] },
  { id: "canh-rau-ngot-thit-ga", name: "Canh rau ngót thịt gà", category: "Canh", kcal: 78, protein: 7, fat: 2, carb: 8, manualAllergens: [], isActive: true, ingredients: [bom("ING-RAU-NGOT", "Rau ngót", "50"), bom("ING-THIT-GA", "Thịt gà ta", "20")] },
  { id: "canh-bi-do-thit-bam", name: "Canh bí đỏ thịt băm", category: "Canh", kcal: 85, protein: 6, fat: 3, carb: 9, manualAllergens: [], isActive: true, ingredients: [bom("ING-BI-DO", "Bí đỏ", "60"), bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "20"), bom("ING-HANH-KHO", "Hành khô", "2")] },
  { id: "sua-chua", name: "Sữa chua", category: "Bữa phụ", kcal: 100, protein: 4, fat: 3, carb: 14, manualAllergens: [], isActive: true, defaultSubstituteId: "sua-dau-nanh", ingredients: [bom("ING-SUA-CHUA", "Sữa chua", "1", "hộp")] },
  { id: "sua-tuoi", name: "Sữa tươi", category: "Bữa phụ", kcal: 120, protein: 6, fat: 6, carb: 10, manualAllergens: [], isActive: true, defaultSubstituteId: "sua-dau-nanh", ingredients: [bom("ING-SUA-TUOI", "Sữa tươi tiệt trùng", "180", "ml")] },
  { id: "sua-dau-nanh", name: "Sữa đậu nành", category: "Bữa phụ", kcal: 110, protein: 6, fat: 4, carb: 12, manualAllergens: [], isActive: true, ingredients: [bom("ING-SUA-DAU-NANH", "Sữa đậu nành", "180", "ml")] },
  { id: "chuoi-tay", name: "Chuối tây", category: "Bữa phụ", kcal: 105, protein: 1, fat: 0, carb: 27, manualAllergens: [], isActive: true, ingredients: [bom("ING-CHUOI", "Chuối tây", "100", "g")] },
  { id: "dua-hau", name: "Dưa hấu", category: "Bữa phụ", kcal: 50, protein: 1, fat: 0, carb: 12, manualAllergens: [], isActive: true, ingredients: [bom("ING-DUA-HAU", "Dưa hấu", "100")] },
  { id: "banh-flan", name: "Bánh flan", category: "Bữa phụ", kcal: 145, protein: 5, fat: 6, carb: 18, manualAllergens: [], isActive: true, defaultSubstituteId: "chuoi-tay", ingredients: [bom("ING-TRUNG-GA", "Trứng gà", "1", "quả"), bom("ING-SUA-TUOI", "Sữa tươi tiệt trùng", "40", "ml"), bom("ING-DUONG", "Đường trắng", "10")] },
  { id: "banh-mi-pho-mai", name: "Bánh mì phô mai", category: "Bữa phụ", kcal: 180, protein: 7, fat: 6, carb: 25, manualAllergens: ["Sữa bò"], isActive: true, ingredients: [bom("ING-BANH-MI", "Bánh mì sandwich", "1", "ổ"), bom(undefined, "Phô mai lát", "15")] },
  { id: "banh-quy", name: "Bánh quy", category: "Bữa phụ", kcal: 130, protein: 2, fat: 5, carb: 19, manualAllergens: [], isActive: true, ingredients: [bom("ING-BOT-MI", "Bột mì", "25"), bom("ING-DUONG", "Đường trắng", "5")] },
  { id: "che-dau-xanh", name: "Chè đậu xanh", category: "Bữa phụ", kcal: 150, protein: 5, fat: 1, carb: 32, manualAllergens: [], isActive: true, ingredients: [bom(undefined, "Đậu xanh", "30"), bom("ING-DUONG", "Đường trắng", "15")] },
  { id: "lac-rang-muoi", name: "Lạc rang muối", category: "Bữa phụ", kcal: 160, protein: 7, fat: 13, carb: 5, manualAllergens: [], isActive: true, ingredients: [bom("ING-DAU-PHONG", "Đậu phộng (lạc)", "25"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "canh-cai-ngot", name: "Canh cải ngọt", category: "Canh", kcal: 70, protein: 3, fat: 2, carb: 7, manualAllergens: [], isActive: true, ingredients: [bom("ING-RAU-CAI-NGOT", "Rau cải ngọt", "60"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "canh-mong-toi", name: "Canh mồng tơi", category: "Canh", kcal: 65, protein: 3, fat: 2, carb: 6, manualAllergens: [], isActive: true, ingredients: [bom("ING-MONG-TOI", "Mồng tơi", "50"), bom("ING-MUOI", "Muối i-ốt", "1")] },
  { id: "rau-muong-xao-toi", name: "Rau muống xào tỏi", category: "Rau", kcal: 65, protein: 3, fat: 4, carb: 5, manualAllergens: [], isActive: true, ingredients: [bom("ING-RAU-MUONG", "Rau muống", "80"), bom("ING-TOI", "Tỏi", "2"), bom("ING-DAU-AN", "Dầu ăn thực vật", "4", "ml")] },
  // Bổ sung 23/07/2026 cùng lịch thực đơn sinh tự động bên dưới: 8 món đó nếu
  // không có trong thư viện thì `dishId` để trống, kéo theo mất truy xuất món ↔ lô
  // (BR-076) và trang dinh dưỡng của phụ huynh không mở được. BỮA SÁNG trước nay
  // chưa có món nào trong thư viện — 5 món cháo dưới đây lấp đúng chỗ đó.
  { id: "chao-thit-bam", name: "Cháo thịt bằm", category: "Món chính", kcal: 210, protein: 9, fat: 4, carb: 34, manualAllergens: [], isActive: true, ingredients: [bom("ING-GAO-TAM", "Gạo tám thơm", "45"), bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "20"), bom("ING-HANH-LA", "Hành lá", "3")] },
  { id: "chao-ga", name: "Cháo gà", category: "Món chính", kcal: 220, protein: 11, fat: 4, carb: 34, manualAllergens: [], isActive: true, ingredients: [bom("ING-GAO-TAM", "Gạo tám thơm", "45"), bom("ING-THIT-GA", "Thịt gà ta", "25"), bom("ING-HANH-LA", "Hành lá", "3")] },
  { id: "chao-dau-xanh", name: "Cháo đậu xanh", category: "Món chính", kcal: 195, protein: 7, fat: 1, carb: 40, manualAllergens: [], isActive: true, ingredients: [bom("ING-GAO-TAM", "Gạo tám thơm", "45"), bom("ING-DAU-XANH", "Đậu xanh", "20")] },
  { id: "chao-bi-do", name: "Cháo bí đỏ", category: "Món chính", kcal: 190, protein: 4, fat: 1, carb: 41, manualAllergens: [], isActive: true, ingredients: [bom("ING-GAO-TAM", "Gạo tám thơm", "45"), bom("ING-BI-DO", "Bí đỏ", "40")] },
  { id: "chao-ca-rot-thit-bam", name: "Cháo cà rốt thịt bằm", category: "Món chính", kcal: 205, protein: 9, fat: 4, carb: 34, manualAllergens: [], isActive: true, ingredients: [bom("ING-GAO-TAM", "Gạo tám thơm", "45"), bom("ING-CA-ROT", "Cà rốt", "20"), bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "18")] },
  { id: "thit-kho-hanh", name: "Thịt kho hành", category: "Món mặn", kcal: 225, protein: 17, fat: 15, carb: 4, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-LON-VAI", "Thịt lợn nạc vai", "60"), bom("ING-HANH-KHO", "Hành khô", "3"), bom("ING-DUONG", "Đường trắng", "3")] },
  { id: "bo-ham-khoai-tay", name: "Bò hầm khoai tây", category: "Món mặn", kcal: 230, protein: 19, fat: 11, carb: 14, manualAllergens: [], isActive: true, ingredients: [bom("ING-THIT-BO", "Thịt bò thăn", "55"), bom("ING-KHOAI-TAY", "Khoai tây", "40"), bom("ING-CA-ROT", "Cà rốt", "20"), bom("ING-HANH-KHO", "Hành khô", "2")] },
  { id: "ca-rot-xao", name: "Cà rốt xào", category: "Rau", kcal: 65, protein: 1, fat: 4, carb: 7, manualAllergens: [], isActive: true, ingredients: [bom("ING-CA-ROT", "Cà rốt", "70"), bom("ING-DAU-AN", "Dầu ăn thực vật", "4", "ml"), bom("ING-TOI", "Tỏi", "2")] },
];

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

export interface MenuTemplate {
  id: string;
  /** Tên bắt buộc, người dùng tự đặt — khoá tìm kiếm/lọc trong danh sách mẫu. */
  name: string;
  /**
   * Mẫu thuộc BỮA nào (Minh 25/07/2026 đợt 2): nút "+ Thêm thực đơn" ở ô lịch
   * chỉ liệt kê mẫu đúng bữa của ô đó (thêm ở bữa sáng → chỉ thấy mẫu bữa sáng).
   */
  mealType: MealType;
  /** "ALL" = tiêu chuẩn; DietGroup.id = mẫu riêng cho nhóm chế độ ăn. */
  audience: "ALL" | string;
  /** Món của bữa, theo thứ tự dọn. */
  dishIds: string[];
  note?: string;
  /** Ngừng dùng thay cho xoá — ô lịch đã áp mẫu vẫn giữ templateId để đối chiếu. */
  isActive: boolean;
}

// Seed theo đúng ví dụ Minh 25/07/2026, món map về thư viện hiện có ("Bún"/"thịt
// nướng" chưa có trong thư viện nên C dùng món gần nhất). B và C cố tình chứa món
// mang dị ứng (cá) có `defaultSubstituteId` — demo luồng áp mẫu tự đổ suất thay thế.
export const MENU_TEMPLATES: MenuTemplate[] = [
  { id: "TPL-STD-A", name: "Thực đơn tiêu chuẩn A", mealType: "LUNCH", audience: "ALL", dishIds: ["com-trang", "canh-mong-toi", "thit-kho-hanh"], isActive: true },
  { id: "TPL-STD-B", name: "Thực đơn tiêu chuẩn B", mealType: "LUNCH", audience: "ALL", dishIds: ["com-trang", "canh-khoai-tay-ca-rot", "ca-basa-sot-ca-chua"], isActive: true },
  { id: "TPL-STD-C", name: "Thực đơn tiêu chuẩn C", mealType: "LUNCH", audience: "ALL", dishIds: ["com-trang", "canh-chua-ca", "ga-rang-gung", "rau-muong-xao-toi"], isActive: true },
  { id: "TPL-SANG-A", name: "Thực đơn sáng A", mealType: "BREAKFAST", audience: "ALL", dishIds: ["chao-thit-bam"], isActive: true },
  { id: "TPL-SANG-B", name: "Thực đơn sáng B", mealType: "BREAKFAST", audience: "ALL", dishIds: ["chao-ga"], isActive: true },
  { id: "TPL-XE-A", name: "Thực đơn chiều A", mealType: "SNACK", audience: "ALL", dishIds: ["sua-chua", "chuoi-tay"], isActive: true },
  { id: "TPL-CHAY-A1", name: "Thực đơn ăn chay A1", mealType: "LUNCH", audience: "VEGETARIAN", dishIds: ["com-trang", "canh-rau-ngot-chay", "dau-phu-rang-gung"], note: "Nấu nồi riêng, không dùng chung muôi với món mặn.", isActive: true },
];

/* ============================================================================
 * LỊCH THỰC ĐƠN TỪ HÔM NAY TỚI HẾT TUẦN SAU — sinh tự động (23/07/2026, Minh)
 *
 * Vì sao sinh chứ không gõ tay: 5 khối × ~9 ngày học là hơn 40 bản ghi, gõ tay
 * vừa mất thời gian vừa chắc chắn lệch nhau (đúng thứ đã xảy ra với các bản ghi
 * cũ: cùng Khối 5 chỗ ghi 300 suất, chỗ ghi 410).
 *
 * VÌ SAO ĐẶT Ở ĐÂY, không nằm cạnh `MENUS`: sinh dữ liệu cần `CLASSROOMS` (sĩ số
 * thật của khối) mà mảng đó khai SAU `MENUS`; đồng thời phải chạy TRƯỚC hai vòng
 * backfill ngay bên dưới (nối `dishId` về DISH_LIBRARY và `ingredientId` về
 * INGREDIENT_CATALOG) — nằm sau chúng thì thực đơn sinh ra không truy xuất được
 * lô và không tính được chi phí.
 *
 * Nguyên liệu chỉ dùng nhóm KHÔNG mang tác nhân dị ứng (tránh cả nước mắm — nó
 * mang tác nhân "Cá" và chạm ô "Hải sản" của học sinh đã xác minh), nên toàn bộ
 * lịch này sạch BR-063 và duyệt được. Muốn thử luồng suất riêng thì dùng các bản
 * ghi khai tay phía trên, vốn cố tình có món chạm dị ứng kèm suất thay thế.
 * ==========================================================================*/

/** Một "thực đơn mẫu" của một ngày — hàm để mỗi bản ghi có object món RIÊNG. */
type DayPlan = () => SeedMealBlock[];

const GENERATED_DAY_PLANS: DayPlan[] = [
  () => [
    { mealType: "BREAKFAST", dishes: [dish("Cháo thịt bằm", 210, [ing("Gạo tám thơm", 45, "g"), ing("Thịt lợn nạc vai", 20, "g"), ing("Hành lá", 3, "g")])] },
    { mealType: "LUNCH", dishes: [
      dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Gà rang gừng", 220, [ing("Thịt gà ta", 70, "g"), ing("Gừng", 3, "g"), ing("Hành khô", 2, "g")]),
      dish("Canh bí đỏ chay", 70, [ing("Bí đỏ", 60, "g"), ing("Hành khô", 2, "g")]),
      dish("Bắp cải luộc", 40, [ing("Bắp cải", 80, "g"), ing("Muối i-ốt", 1, "g")]),
    ] },
    { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
  ],
  () => [
    { mealType: "BREAKFAST", dishes: [dish("Cháo gà", 220, [ing("Gạo tám thơm", 45, "g"), ing("Thịt gà ta", 25, "g"), ing("Hành lá", 3, "g")])] },
    { mealType: "LUNCH", dishes: [
      dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Thịt bò xào rau củ", 215, [ing("Thịt bò thăn", 55, "g"), ing("Cà rốt", 25, "g"), ing("Súp lơ", 25, "g"), ing("Tỏi", 2, "g"), ing("Dầu ăn thực vật", 5, "ml")]),
      dish("Canh rau ngót thịt băm", 75, [ing("Rau ngót", 40, "g"), ing("Thịt lợn nạc vai", 15, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Su su xào", 70, [ing("Su su", 70, "g"), ing("Dầu ăn thực vật", 4, "ml"), ing("Tỏi", 2, "g")]),
    ] },
    { mealType: "SNACK", dishes: [dish("Dưa hấu", 50, [ing("Dưa hấu", 100, "g")])] },
  ],
  () => [
    { mealType: "BREAKFAST", dishes: [dish("Cháo đậu xanh", 195, [ing("Gạo tám thơm", 45, "g"), ing("Đậu xanh", 20, "g")])] },
    { mealType: "LUNCH", dishes: [
      dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Thịt kho hành", 225, [ing("Thịt lợn nạc vai", 60, "g"), ing("Hành khô", 3, "g"), ing("Đường trắng", 3, "g")]),
      dish("Canh khoai tây cà rốt", 85, [ing("Khoai tây", 45, "g"), ing("Cà rốt", 25, "g"), ing("Xương lợn", 20, "g")]),
      dish("Rau muống xào tỏi", 60, [ing("Rau muống", 70, "g"), ing("Tỏi", 3, "g"), ing("Dầu ăn thực vật", 4, "ml")]),
    ] },
    { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
  ],
  () => [
    { mealType: "BREAKFAST", dishes: [dish("Cháo bí đỏ", 190, [ing("Gạo tám thơm", 45, "g"), ing("Bí đỏ", 40, "g")])] },
    { mealType: "LUNCH", dishes: [
      dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Gà kho gừng", 215, [ing("Thịt gà ta", 65, "g"), ing("Gừng", 4, "g"), ing("Đường trắng", 3, "g")]),
      dish("Canh mồng tơi", 65, [ing("Mồng tơi", 50, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Cà rốt xào", 65, [ing("Cà rốt", 70, "g"), ing("Dầu ăn thực vật", 4, "ml"), ing("Tỏi", 2, "g")]),
    ] },
    { mealType: "SNACK", dishes: [dish("Dưa hấu", 50, [ing("Dưa hấu", 100, "g")])] },
  ],
  () => [
    { mealType: "BREAKFAST", dishes: [dish("Cháo cà rốt thịt bằm", 205, [ing("Gạo tám thơm", 45, "g"), ing("Cà rốt", 20, "g"), ing("Thịt lợn nạc vai", 18, "g")])] },
    { mealType: "LUNCH", dishes: [
      dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Bò hầm khoai tây", 230, [ing("Thịt bò thăn", 55, "g"), ing("Khoai tây", 40, "g"), ing("Cà rốt", 20, "g"), ing("Hành khô", 2, "g")]),
      dish("Canh cải ngọt", 60, [ing("Rau cải ngọt", 55, "g"), ing("Muối i-ốt", 1, "g")]),
      dish("Bắp cải luộc", 40, [ing("Bắp cải", 80, "g"), ing("Muối i-ốt", 1, "g")]),
    ] },
    { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
  ],
];

/**
 * THỰC ĐƠN RIÊNG đi kèm, cho các nhóm chế độ ăn mà bữa gốc ở trên không dùng được.
 *
 * BR-065: khối có học sinh thuộc nhóm nào thì nhóm đó phải có gì ăn — thực đơn
 * công bố mà bỏ trống là vi phạm, và `npm run audit` bắt đúng lỗi này ngay lần
 * chạy đầu (luật NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN).
 *
 * `matchDiet` viết lặp ở đây thay vì tra `DIET_GROUPS` vì mảng đó khai SAU khối
 * này trong file — đọc lúc nạp module sẽ vướng TDZ. Chỉ ba nhóm cần thực đơn
 * riêng: NUT_FREE và LACTOSE_FREE ăn được bữa gốc (bộ nguyên liệu ở trên không có
 * lạc, vừng hay sữa), nên không dựng thừa.
 */
const GENERATED_DIET_PLANS: { groupId: string; matchDiet: string; plan: DayPlan }[] = [
  {
    groupId: "VEGETARIAN",
    matchDiet: "Vegetarian",
    plan: () => [
      { mealType: "BREAKFAST", dishes: [dish("Cháo đậu xanh", 195, [ing("Gạo tám thơm", 45, "g"), ing("Đậu xanh", 20, "g")])] },
      { mealType: "LUNCH", dishes: [
        dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
        dish("Đậu phụ sốt cà chua", 195, [ing("Đậu phụ", 80, "g"), ing("Cà chua", 40, "g"), ing("Dầu ăn thực vật", 5, "ml")]),
        dish("Canh bí đỏ chay", 70, [ing("Bí đỏ", 60, "g"), ing("Hành khô", 2, "g")]),
        dish("Su su xào", 70, [ing("Su su", 70, "g"), ing("Dầu ăn thực vật", 4, "ml"), ing("Tỏi", 2, "g")]),
      ] },
      { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
    ],
  },
  {
    // Không "đường trắng" ở bất kỳ món nào — đó là từ khoá loại trừ của nhóm này.
    groupId: "DIABETIC",
    matchDiet: "Diabetic",
    plan: () => [
      { mealType: "BREAKFAST", dishes: [dish("Cháo đậu xanh", 195, [ing("Gạo tám thơm", 45, "g"), ing("Đậu xanh", 20, "g")])] },
      { mealType: "LUNCH", dishes: [
        dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
        dish("Gà rang gừng", 220, [ing("Thịt gà ta", 70, "g"), ing("Gừng", 3, "g"), ing("Hành khô", 2, "g")]),
        dish("Canh rau ngót thịt băm", 75, [ing("Rau ngót", 40, "g"), ing("Thịt lợn nạc vai", 15, "g"), ing("Muối i-ốt", 1, "g")]),
        dish("Bắp cải luộc", 40, [ing("Bắp cải", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      ] },
      { mealType: "SNACK", dishes: [dish("Dưa hấu", 50, [ing("Dưa hấu", 100, "g")])] },
    ],
  },
  {
    // Không thịt lợn, không xương lợn.
    groupId: "HALAL",
    matchDiet: "Halal",
    plan: () => [
      { mealType: "BREAKFAST", dishes: [dish("Cháo gà", 220, [ing("Gạo tám thơm", 45, "g"), ing("Thịt gà ta", 25, "g"), ing("Hành lá", 3, "g")])] },
      { mealType: "LUNCH", dishes: [
        dish("Cơm trắng", 260, [ing("Gạo tám thơm", 80, "g"), ing("Muối i-ốt", 1, "g")]),
        dish("Gà rang gừng", 220, [ing("Thịt gà ta", 70, "g"), ing("Gừng", 3, "g"), ing("Hành khô", 2, "g")]),
        dish("Canh bí đỏ chay", 70, [ing("Bí đỏ", 60, "g"), ing("Hành khô", 2, "g")]),
        dish("Bắp cải luộc", 40, [ing("Bắp cải", 80, "g"), ing("Muối i-ốt", 1, "g")]),
      ] },
      { mealType: "SNACK", dishes: [dish("Chuối tây", 105, [ing("Chuối tây", 100, "g")])] },
    ],
  },
];

const GENERATED_MENUS: SeedMenu[] = (() => {
  const names = ["Chủ nhật", "Thứ 2", "Thứ 3", "Thứ 4", "Thứ 5", "Thứ 6", "Thứ 7"];
  const today = new Date();
  // Thứ 7 của TUẦN SAU tính từ hôm nay: lùi về Thứ 2 tuần này (+0..6) rồi cộng 12.
  const lastOffset = 12 - ((today.getDay() + 6) % 7);
  const grades = gradeLevelsOf();
  const rows: SeedMenu[] = [];

  for (let offset = 0; offset <= lastOffset; offset += 1) {
    const d = new Date();
    d.setDate(d.getDate() + offset);
    if (d.getDay() === 0) continue; // Chủ nhật không có bữa ở trường
    const p = (x: number) => String(x).padStart(2, "0");
    const iso = `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
    const label = `${names[d.getDay()]} — ${p(d.getDate())}/${p(d.getMonth() + 1)}/${d.getFullYear()}`;

    grades.forEach((grade, gi) => {
      // Mỗi ngày × khối chỉ 1 thực đơn — ngày nào đã khai tay ở trên thì bỏ qua,
      // không đè lên kịch bản đã dựng sẵn (thực đơn hôm nay, cảnh báo thiếu NL…).
      if (MENUS.some((m) => m.grade === grade && m.menuDateIso === iso)) return;
      // Lệch bộ món theo khối để cùng một ngày các khối không ăn giống hệt nhau —
      // và để màn "So sánh mọi chế độ ăn" có gì đó để so.
      const blocks = GENERATED_DAY_PLANS[(offset + gi) % GENERATED_DAY_PLANS.length]();
      const servings = gradeEnrollmentOf(grade);
      // Khối chưa có lớp nào ⇒ không có ai ăn; lập thực đơn 0 suất chỉ tạo bản ghi
      // rác và làm vòng tính chi phí cuối file bỏ qua (giữ nguyên 0₫).
      if (servings <= 0) return;
      rows.push({
        id: `MENU-W-${iso.slice(5).replace("-", "")}-${gi + 1}`,
        week: label,
        name: `Thực đơn ${label.replace(" — ", " ")} — ${grade}`,
        grade,
        menuDateIso: iso,
        status: "PUBLISHED",
        dishes: blocks.reduce((s, b) => s + b.dishes.length, 0),
        allergyConflicts: 0,
        // Để 0 có chủ đích: vòng cuối file tính lại `estimatedCost` cho MỌI thực
        // đơn từ công thức × báo giá nhà cung cấp. Khai tay ở đây chỉ là con số
        // chết bị ghi đè — đúng thứ ghi chú ở vòng đó cảnh báo.
        estimatedCost: 0,
        costCeiling: 30000,
        submittedAt: vnInDays(offset - 4),
        createdBy: "Cty TNHH Suất ăn An Bình",
        approvedBy: "Nguyễn Thị Lan (Hiệu phó)",
        servings,
        blocksData: blocks,
      });
    });
  }

  /*
   * Đúng HAI bản còn CHỜ DUYỆT (yêu cầu Minh) — đặt ở 1/4 và 3/4 danh sách nên
   * một bản rơi vào tuần này (mở màn duyệt là thấy việc ngay), một bản ở tuần sau
   * (thử được thao tác chuyển tuần). Chọn theo vị trí thay vì cố định khối+ngày để
   * không phải sửa lại mỗi khi hôm nay rơi vào thứ khác.
   */
  for (const idx of [Math.floor(rows.length * 0.25), Math.floor(rows.length * 0.75)]) {
    const m = rows[idx];
    if (!m) continue;
    m.status = "PENDING_APPROVAL";
    m.approvedBy = undefined;
  }

  /*
   * Thực đơn riêng ĐI KÈM: cùng khối+ngày, `audience` = nhóm, KHÔNG có `reason`
   * (suất bổ sung muộn mới cần lý do). Trạng thái bám theo thực đơn chung — chung
   * còn chờ duyệt thì riêng cũng vậy, đúng mô hình "duyệt cùng một lượt".
   */
  const withDiets: SeedMenu[] = [];
  for (const m of rows) {
    withDiets.push(m);
    for (const g of GENERATED_DIET_PLANS) {
      const members = STUDENTS.filter(
        (s) => gradeOfClass(s.class) === m.grade && s.specialDiet === g.matchDiet
      );
      if (members.length === 0) continue;
      const blocks = g.plan();
      withDiets.push({
        ...m,
        id: `${m.id}--${g.groupId}`,
        name: `${m.name} · ${g.matchDiet}`,
        audience: g.groupId,
        dietGroupId: g.groupId,
        dishes: blocks.reduce((s, b) => s + b.dishes.length, 0),
        servings: members.length,
        blocksData: blocks,
      });
    }
  }
  return withDiets;
})();

MENUS.push(...GENERATED_MENUS);

/**
 * Gắn `dishId` cho món trong THỰC ĐƠN SEED.
 *
 * `MENUS` khai báo trước `DISH_LIBRARY` trong file này nên hàm `dish()` không tra
 * được thư viện lúc dựng seed — phải nối sau. Không có khoá này thì "cùng một món
 * ở nhiều khối" chỉ nhận ra được bằng SO TÊN CHUỖI: bữa trưa 21/07 Khối 1 và Khối
 * 3 cùng có "Cơm trắng" và "Gà rang gừng" nhưng hệ thống coi là 4 món rời nhau.
 *
 * Món không khớp tên nào trong thư viện thì để trống — đúng trạng thái "món tự do
 * ngoài thư viện", không bịa khoá.
 */
for (const menu of MENUS) {
  for (const block of menu.blocksData ?? []) {
    for (const d of block.dishes) {
      d.dishId ??= DISH_LIBRARY.find((x) => x.name === d.name)?.id;
      // Suất thay thế cũng là MÓN — trước đây chỉ nối bằng tên nên không thống kê
      // được dinh dưỡng/allergen của suất riêng theo cùng một đường với món chính.
      for (const sub of d.substitutes ?? []) {
        sub.dishId ??= DISH_LIBRARY.find((x) => x.name === sub.name)?.id;
      }
    }
  }
}

export function findDishByName(name: string): Dish | undefined {
  return DISH_LIBRARY.find((d) => d.name === name);
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
  library: Pick<Dish, "id" | "name">[] = DISH_LIBRARY
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

/** Tổng dinh dưỡng của 1 variant, cộng từ thư viện món (BR-062). */
export function variantNutrition(
  variant: Pick<MenuVariant, "dishes">
): { kcal: number; protein: number; fat: number; carb: number } {
  return variant.dishes.reduce(
    (sum, d) => {
      const dish = DISH_LIBRARY.find((x) => x.id === d.dishId);
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

/**
 * THỰC ĐƠN RIÊNG cho từng nhóm chế độ ăn.
 *
 * Chốt 22/07/2026: bữa ăn đặc biệt là MỘT THỰC ĐƠN RIÊNG cho nhóm chế độ ăn, không
 * còn là "thay từng món" gắn trong thực đơn chung. Dị ứng thì ngược lại — thay
 * đúng món chạm tác nhân, ngay tại thực đơn chung (`SubstituteDish`).
 *
 * Lý do tách hai cơ chế: dị ứng là loại trừ MỘT tác nhân khỏi MỘT món, còn chế độ
 * ăn thay đổi cả cấu trúc bữa (tiểu đường giảm tinh bột toàn bữa, ăn chay bỏ mọi
 * món động vật). Ép chế độ ăn vào mô hình thay-từng-món thì bếp phải suy ra bữa
 * hoàn chỉnh trong đầu, và không ai cộng được tổng dinh dưỡng của suất đó.
 *
 * `replacesDishId` chỉ để hiển thị "thay cho món nào" — thực đơn riêng là danh sách
 * món ĐẦY ĐỦ, không phải danh sách chênh lệch.
 *
 * ⚠️ 22/07/2026 — GỘP VÀO `Menu`. Đây chỉ còn là SEED ĐẦU VÀO để dựng các thực đơn
 * riêng (audience != "ALL") đẩy vào `MENUS` ngay bên dưới. Backend KHÔNG có bảng
 * này: một thực đơn riêng là một `Menu` đầy đủ, phân biệt bằng `audience`. FE đọc
 * qua `specialMenuMeals(menus)` — projection tách mỗi thực đơn riêng thành từng buổi
 * để các màn cũ render không phải đổi logic.
 */
const SPECIAL_MEAL_SEED: MenuVariant[] = [
  // Bữa phụ 07/07 là "Chè đậu xanh" — nhóm tiểu đường không dùng được.
  {
    id: "MV-205",
    menuId: "MENU-2026-27B",
    mealType: "SNACK",
    dietGroupId: "DIABETIC",
    mode: "FULL",
    portions: 1,
    status: "PLANNED",
    dishes: [{ dishId: "sua-dau-nanh" }],
    note: "Sữa đậu nành KHÔNG đường thay chè đậu xanh",
  },

  /*
   * Bổ sung 22/07/2026 — suất riêng của MENU-2026-28 (Khối 3, đang chờ duyệt).
   *
   * Thực đơn này đã nộp duyệt trong khi nhóm chay/Halal/không lactose của khối
   * chưa có gì ăn: bữa trưa có "Tôm rim thịt", bữa phụ có "Bánh flan". Với điều
   * kiện nộp duyệt thứ 8 (BR-065) thì lẽ ra nó không nộp được. Vá seed cho khớp
   * luật thay vì nới luật cho khớp seed.
   *
   * `status: "PLANNED"` — khai lúc thực đơn còn nháp nên đi kèm thực đơn đi
   * duyệt, không cần duyệt riêng.
   */
  {
    id: "MV-201",
    menuId: "MENU-2026-28",
    mealType: "LUNCH",
    dietGroupId: "VEGETARIAN",
    mode: "FULL",
    portions: 1,
    status: "PLANNED",
    dishes: [
      { dishId: "com-trang" },
      { dishId: "dau-phu-sot-ca-chua", replacesDishId: "tom-rim-thit" },
      { dishId: "canh-khoai-tay-ca-rot" },
      { dishId: "rau-muong-xao-toi" },
    ],
    note: "Đậu phụ thay tôm rim thịt; canh nấu nước rau, không nước xương (BR-082)",
  },
  {
    id: "MV-202",
    menuId: "MENU-2026-28",
    mealType: "LUNCH",
    dietGroupId: "HALAL",
    mode: "FULL",
    portions: 1,
    status: "PLANNED",
    dishes: [
      { dishId: "com-trang" },
      { dishId: "ga-kho-gung", replacesDishId: "tom-rim-thit" },
      { dishId: "canh-khoai-tay-ca-rot" },
      { dishId: "rau-muong-xao-toi" },
    ],
    note: "Bỏ thịt lợn trong món tôm rim; gà nguồn chứng nhận Halal, nấu dụng cụ riêng",
  },
  {
    id: "MV-203",
    menuId: "MENU-2026-28",
    mealType: "SNACK",
    dietGroupId: "VEGETARIAN",
    mode: "FULL",
    portions: 1,
    status: "PLANNED",
    dishes: [{ dishId: "chuoi-tay" }, { dishId: "dua-hau" }],
    note: "Bánh flan có trứng gà — thay bằng hoa quả",
  },
  {
    id: "MV-204",
    menuId: "MENU-2026-28",
    mealType: "SNACK",
    dietGroupId: "LACTOSE_FREE",
    mode: "FULL",
    portions: 1,
    status: "PLANNED",
    dishes: [{ dishId: "sua-dau-nanh" }, { dishId: "dua-hau" }],
    note: "Bánh flan có sữa bò — thay bằng sữa đậu nành",
  },

  {
    // Nhóm tiểu đường Khối 3 — đúng ca mà mô hình "thay món lẻ" không tả nổi
    id: "MV-001",
    menuId: "MENU-2026-30",
    mealType: "LUNCH",
    dietGroupId: "DIABETIC",
    mode: "FULL",
    portions: 2,
    dishes: [
      { dishId: "com-trang" },
      { dishId: "ga-rang-gung" },
      { dishId: "canh-rau-ngot-thit-bam" },
      { dishId: "bap-cai-luoc" },
    ],
    note: "Giảm tinh bột, bỏ món ngọt — nấu và chia suất riêng (BR-082)",
  },

  /* Chuyển 22/07/2026 từ 9 "suất thay thế theo nhóm chế độ ăn" gắn rải rác trong
     thực đơn — nay mỗi nhóm × mỗi bữa là một thực đơn riêng đầy đủ. */
  {
    id: "MV-101",
    menuId: "MENU-2026-30",
    mealType: "LUNCH",
    dietGroupId: "VEGETARIAN",
    mode: "FULL",
    portions: 1,
    dishes: [
      { dishId: "com-trang" },
      { dishId: "canh-bi-do-chay", replacesDishId: "canh-bi-do-nau-tom" },
      { dishId: "su-su-xao" },
      { dishId: "dau-phu-rang-gung", replacesDishId: "ga-rang-gung" },
    ],
    note: "Thực đơn riêng cho nhóm Ăn chay (Vegetarian) — bữa trưa",
  },
  {
    id: "MV-102",
    menuId: "MENU-2026-27",
    mealType: "LUNCH",
    dietGroupId: "HALAL",
    mode: "FULL",
    portions: 1,
    dishes: [
      { dishId: "com-trang" },
      { dishId: "ga-kho-gung", replacesDishId: "thit-kho-trung-cut" },
      { dishId: "canh-bi-do-nau-tom" },
      { dishId: "su-su-xao" },
    ],
    note: "Thực đơn riêng cho nhóm Halal — bữa trưa",
  },
  {
    id: "MV-103",
    menuId: "MENU-2026-27",
    mealType: "LUNCH",
    dietGroupId: "VEGETARIAN",
    mode: "FULL",
    portions: 1,
    dishes: [
      { dishId: "com-trang" },
      { dishId: "dau-phu-sot-ca-chua", replacesDishId: "thit-kho-trung-cut" },
      { dishId: "canh-bi-do-chay", replacesDishId: "canh-bi-do-nau-tom" },
      { dishId: "su-su-xao" },
    ],
    note: "Thực đơn riêng cho nhóm Ăn chay (Vegetarian) — bữa trưa",
  },
  {
    id: "MV-104",
    menuId: "MENU-2026-27",
    mealType: "SNACK",
    dietGroupId: "LACTOSE_FREE",
    mode: "FULL",
    portions: 1,
    dishes: [
      { dishId: "sua-dau-nanh", replacesDishId: "sua-chua" },
      { dishId: "chuoi-tay" },
    ],
    note: "Thực đơn riêng cho nhóm Không lactose (Lactose-Free) — bữa phụ",
  },
  {
    id: "MV-105",
    menuId: "MENU-2026-27B",
    mealType: "LUNCH",
    dietGroupId: "HALAL",
    mode: "FULL",
    portions: 1,
    dishes: [
      { dishId: "com-trang" },
      { dishId: "ca-basa-sot-ca-chua" },
      { dishId: "canh-rau-ngot-thit-ga", replacesDishId: "canh-rau-ngot-thit-bam" },
      { dishId: "gia-xao" },
    ],
    note: "Thực đơn riêng cho nhóm Halal — bữa trưa",
  },
  {
    id: "MV-106",
    menuId: "MENU-2026-27B",
    mealType: "LUNCH",
    dietGroupId: "VEGETARIAN",
    mode: "FULL",
    portions: 1,
    dishes: [
      { dishId: "com-trang" },
      { dishId: "dau-phu-sot-ca-chua", replacesDishId: "ca-basa-sot-ca-chua" },
      { dishId: "canh-rau-ngot-chay", replacesDishId: "canh-rau-ngot-thit-bam" },
      { dishId: "gia-xao" },
    ],
    note: "Thực đơn riêng cho nhóm Ăn chay (Vegetarian) — bữa trưa",
  },
];

// ---------------------------------------------------------------------------
// GỘP thực đơn riêng vào bảng `Menu` (22/07/2026)
// ---------------------------------------------------------------------------
// Mỗi (thực đơn chung × nhóm chế độ ăn) trong SPECIAL_MEAL_SEED trở thành MỘT
// `Menu` đầy đủ với `audience = <DietGroup.id>` và `blocksData` chứa MÓN ĐẦY ĐỦ
// (không còn `replacesDishId`/delta). Đẩy thẳng vào `MENUS` — backend chỉ có một
// bảng menus, phân biệt bằng `audience`.

/** Dựng một `DishForm`-shape đầy đủ từ THƯ VIỆN MÓN để nhét vào blocksData. */
function expandDishForMenu(dishId: string): SeedDish {
  const d = DISH_LIBRARY.find((x) => x.id === dishId);
  if (!d) {
    return { dishId, name: dishId, ingredients: [], manualAllergens: [], kcal: "", protein: "", carb: "", fat: "", isSpecialDish: false, substitutes: [] };
  }
  return {
    dishId: d.id,
    name: d.name,
    ingredients: d.ingredients.map((i) => ({ name: i.name, quantity: i.quantity, unit: i.unit, ingredientId: i.ingredientId })),
    manualAllergens: [...d.manualAllergens],
    kcal: String(d.kcal), protein: String(d.protein), carb: String(d.carb), fat: String(d.fat),
    isSpecialDish: false,
    substitutes: [],
  };
}

const MEAL_ORDER: Record<MealType, number> = { BREAKFAST: 0, LUNCH: 1, SNACK: 2 };

const SPECIAL_MENUS: SeedMenu[] = (() => {
  const byMenuGroup = new Map<string, MenuVariant[]>();
  for (const v of SPECIAL_MEAL_SEED) {
    const key = `${v.menuId}__${v.dietGroupId}`;
    const arr = byMenuGroup.get(key);
    if (arr) arr.push(v);
    else byMenuGroup.set(key, [v]);
  }
  const out: SeedMenu[] = [];
  for (const vs of byMenuGroup.values()) {
    const base = MENUS.find((m) => m.id === vs[0].menuId);
    if (!base) continue;
    const group = vs[0].dietGroupId;
    // Nhãn nhóm (DIET_GROUPS) khai sau khối này nên không tra ở đây; các màn hiển
    // thị label qua DIET_GROUPS.find(audience) lúc render. Tên menu để mã nhóm.
    const label = group;
    const blocks: SeedMealBlock[] = [...vs]
      .sort((a, b) => MEAL_ORDER[a.mealType] - MEAL_ORDER[b.mealType])
      .map((v) => ({
        mealType: v.mealType,
        dishes: v.dishes.map((vd) => expandDishForMenu(vd.dishId)),
        note: v.note,
        portions: v.portions,
      }));
    // Trạng thái: PLANNED/rỗng đi kèm thực đơn chung ⇒ theo `base.status`; bổ sung
    // muộn (PENDING_APPROVAL/APPROVED/REJECTED) giữ nguyên vòng đời riêng của nó.
    const explicit = vs.map((v) => v.status).filter((s): s is NonNullable<typeof s> => Boolean(s) && s !== "PLANNED");
    const status: MenuItem["status"] = explicit.includes("PENDING_APPROVAL")
      ? "PENDING_APPROVAL"
      : explicit.includes("REJECTED")
        ? "REJECTED"
        : explicit.includes("APPROVED")
          ? "APPROVED"
          : base.status;
    out.push({
      id: `${base.id}--${group}`,
      week: base.week,
      name: `${base.name} · ${label}`,
      grade: base.grade,
      menuDateIso: base.menuDateIso,
      status,
      dishes: blocks.reduce((s, b) => s + b.dishes.length, 0),
      allergyConflicts: 0,
      estimatedCost: base.estimatedCost,
      costCeiling: base.costCeiling,
      submittedAt: base.submittedAt,
      createdBy: base.createdBy,
      createdBySupplierId: base.createdBySupplierId,
      approvedBy: status === "APPROVED" || status === "PUBLISHED" ? base.approvedBy : undefined,
      audience: group,
      dietGroupId: group,
      reason: vs.find((v) => v.reason)?.reason,
      servings: Math.max(0, ...vs.map((v) => v.portions || 0)) || base.servings,
      blocksData: blocks,
    });
  }
  return out;
})();

MENUS.push(...SPECIAL_MENUS);

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

/**
 * Mục tiêu năng lượng bữa trưa nhóm 6–11 tuổi (BR-062). Dùng chung cho mọi học
 * sinh trong nhóm tuổi này (kể cả HS-3A-001 8 tuổi và HS-1B-007 6 tuổi — cùng
 * nhóm 6–11 theo BR-062, không tách riêng theo review/parent Đợt 2, 27/07/2026).
 */
export const NUTRITION_TARGET = { kcal: 715, protein: 28, fat: 22, carb: 95 };

export interface NutritionDay {
  /**
   * review/parent (`review_parent_data_linkage_plan.md` Đợt 2, 27/07/2026):
   * mỗi điểm dữ liệu dinh dưỡng thuộc về MỘT học sinh — trước đó mảng này dùng
   * chung cho mọi con, khiến biểu đồ dinh dưỡng giống hệt nhau khi đổi con.
   */
  studentId: string;
  date: string;
  kcal: number;
  protein: number;
  fat: number;
  carb: number;
}

export const NUTRITION_HISTORY: NutritionDay[] = [
  { studentId: "HS-3A-001", date: "22/06", kcal: 700, protein: 27, fat: 21, carb: 94 },
  { studentId: "HS-3A-001", date: "23/06", kcal: 725, protein: 29, fat: 23, carb: 96 },
  { studentId: "HS-3A-001", date: "24/06", kcal: 690, protein: 26, fat: 20, carb: 93 },
  { studentId: "HS-3A-001", date: "25/06", kcal: 735, protein: 30, fat: 24, carb: 97 },
  { studentId: "HS-3A-001", date: "26/06", kcal: 705, protein: 28, fat: 22, carb: 92 },
  { studentId: "HS-3A-001", date: "29/06", kcal: 720, protein: 28, fat: 23, carb: 95 },
  { studentId: "HS-3A-001", date: "30/06", kcal: 740, protein: 30, fat: 24, carb: 98 },
  { studentId: "HS-3A-001", date: "01/07", kcal: 695, protein: 27, fat: 21, carb: 93 },
  { studentId: "HS-3A-001", date: "02/07", kcal: 730, protein: 29, fat: 23, carb: 96 },
  // HS-1B-007 (Nguyễn Minh Anh, lớp 1B) — bổ sung theo review/parent Đợt 2 (27/07/2026).
  { studentId: "HS-1B-007", date: "22/06", kcal: 640, protein: 24, fat: 19, carb: 85 },
  { studentId: "HS-1B-007", date: "23/06", kcal: 655, protein: 25, fat: 20, carb: 87 },
  { studentId: "HS-1B-007", date: "24/06", kcal: 620, protein: 23, fat: 18, carb: 83 },
  { studentId: "HS-1B-007", date: "25/06", kcal: 660, protein: 26, fat: 21, carb: 88 },
  { studentId: "HS-1B-007", date: "26/06", kcal: 630, protein: 24, fat: 19, carb: 84 },
  { studentId: "HS-1B-007", date: "29/06", kcal: 645, protein: 25, fat: 20, carb: 86 },
  { studentId: "HS-1B-007", date: "30/06", kcal: 665, protein: 26, fat: 21, carb: 89 },
  { studentId: "HS-1B-007", date: "01/07", kcal: 610, protein: 23, fat: 18, carb: 82 },
  { studentId: "HS-1B-007", date: "02/07", kcal: 650, protein: 25, fat: 20, carb: 86 },
];

// ===== Lịch sử bữa ăn chi tiết (12/13_MealHistory — BR-065, BR-069, BR-121) =====

export interface MealHistoryEntry {
  id: string; // yyyymmdd (+ hậu tố nếu trùng ngày khác học sinh) — dùng làm route param
  /**
   * review/parent (`review_parent_data_linkage_plan.md` Đợt 1, 27/07/2026):
   * mỗi bản ghi lịch sử bữa ăn thuộc về MỘT học sinh — trước đó mảng này dùng
   * chung cho mọi con, khiến đổi con ở Topbar không đổi nội dung trang.
   */
  studentId: string;
  date: string;
  meal: "Bữa sáng" | "Bữa trưa" | "Bữa phụ";
  kcal: number;
  servedAt: string;
  consumption: keyof typeof STATUS_BADGE; // FULLY | PARTIALLY | NOT_CONSUMED
  specialMealNote?: string; // BR-065
  /** Đơn vị cung cấp suất ăn (Service Caterer) của bữa — nhãn hiển thị. */
  caterer: string;
  /** Khoá về `PARTNERS`. */
  catererSupplierId?: string;
  /** Ghi đè theo món nếu món do đơn vị khác cung cấp (mặc định dùng caterer của bữa). */
  dishes: { name: string; kcal: number; caterer?: string }[];
  feedbackOpen: boolean;
}

export const MEAL_HISTORY: MealHistoryEntry[] = [
  { id: "20260701", studentId: "HS-3A-001", date: "01/07/2026", meal: "Bữa trưa", kcal: 695, servedAt: "10:45 01/07/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "FULLY", dishes: [{ name: "Cơm trắng", kcal: 260 }, { name: "Cá basa sốt cà chua", kcal: 190 }, { name: "Canh rau ngót thịt băm", kcal: 75 }, { name: "Giá xào", kcal: 60 }], feedbackOpen: true },
  { id: "20260630", studentId: "HS-3A-001", date: "30/06/2026", meal: "Bữa trưa", kcal: 740, servedAt: "10:50 30/06/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "PARTIALLY", dishes: [{ name: "Cơm trắng", kcal: 260 }, { name: "Gà rang gừng", kcal: 220 }, { name: "Canh mồng tơi nấu cua", kcal: 85 }, { name: "Bắp cải luộc", kcal: 40 }], specialMealNote: "Canh mồng tơi nấu cua → suất riêng: canh rau cải (dị ứng Cua — BR-065)", feedbackOpen: false },
  { id: "20260629", studentId: "HS-3A-001", date: "29/06/2026", meal: "Bữa trưa", kcal: 720, servedAt: "10:40 29/06/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "FULLY", dishes: [{ name: "Cơm trắng", kcal: 260 }, { name: "Thịt kho trứng cút", kcal: 210 }, { name: "Canh bí đỏ nấu tôm", kcal: 80 }, { name: "Su su xào", kcal: 70 }], specialMealNote: "Canh bí đỏ nấu tôm → suất riêng: canh bí đỏ chay (dị ứng Tôm — BR-065)", feedbackOpen: false },
  { id: "20260626", studentId: "HS-3A-001", date: "26/06/2026", meal: "Bữa trưa", kcal: 705, servedAt: "10:45 26/06/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "FULLY", dishes: [{ name: "Cơm trắng", kcal: 260 }, { name: "Tôm rim thịt", kcal: 205 }, { name: "Canh khoai tây cà rốt", kcal: 85 }, { name: "Rau muống xào tỏi", kcal: 65 }], specialMealNote: "Tôm rim thịt → suất riêng: Thịt rim (dị ứng Tôm — BR-065)", feedbackOpen: false },
  // HS-1B-007 (Nguyễn Minh Anh, lớp 1B) — bổ sung theo review/parent Đợt 1 (27/07/2026)
  // để 2 con của cùng phụ huynh có dữ liệu khác nhau khi đổi con ở Topbar.
  { id: "20260701-1B", studentId: "HS-1B-007", date: "01/07/2026", meal: "Bữa trưa", kcal: 610, servedAt: "10:40 01/07/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "FULLY", dishes: [{ name: "Cơm trắng", kcal: 220 }, { name: "Cá basa sốt cà chua", kcal: 160 }, { name: "Canh rau ngót thịt băm", kcal: 65 }, { name: "Giá xào", kcal: 50 }], feedbackOpen: true },
  { id: "20260630-1B", studentId: "HS-1B-007", date: "30/06/2026", meal: "Bữa trưa", kcal: 650, servedAt: "10:45 30/06/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "FULLY", dishes: [{ name: "Cơm trắng", kcal: 220 }, { name: "Gà rang gừng", kcal: 190 }, { name: "Canh mồng tơi nấu cua", kcal: 75 }, { name: "Bắp cải luộc", kcal: 35 }], feedbackOpen: false },
  { id: "20260629-1B", studentId: "HS-1B-007", date: "29/06/2026", meal: "Bữa trưa", kcal: 630, servedAt: "10:35 29/06/2026", caterer: "Cty TNHH Suất ăn An Bình", consumption: "PARTIALLY", dishes: [{ name: "Cơm trắng", kcal: 220 }, { name: "Thịt kho trứng cút", kcal: 180 }, { name: "Canh bí đỏ nấu tôm", kcal: 70 }, { name: "Su su xào", kcal: 60 }], feedbackOpen: false },
];

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

export const CONCERN_SYMPTOMS = ["Đau bụng", "Nôn/Buồn nôn", "Tiêu chảy", "Sốt", "Dị ứng", "Khó thở", "Khác"];

// ===== Giáo viên chủ nhiệm (mock session Teacher) =====

/**
 * @deprecated review/teacher (`review_teacher_data_linkage_plan.md` Đợt 0,
 * 27/07/2026) — hằng số tĩnh, không đọc từ phiên đăng nhập. Dùng
 * `useTeacherClass()` (`lib/stores/auth-store.ts`) thay thế: join theo
 * `Classroom.teacherId` với `SessionUser.id` đang đăng nhập, không so tên hiển
 * thị. Không còn nơi nào trong `teacher/*` đọc hằng số này — giữ lại vài kỳ
 * nữa phòng khi có tài liệu/screen spec cũ còn trích dẫn, không xoá ngay.
 */
export const TEACHER_CLASS = {
  classId: "3A",
  teacher: "Nguyễn Thu Trang",
  students: 38,
  lunchTime: "10:45",
};

// ===== Phân phát theo từng học sinh (08/09/10 — UC-016, BR-120/123/125/130) =====

/**
 * Một món trong suất ăn thực tế của một học sinh (gap T3).
 * Ở backend đây là bảng `serving_dish` nối với `serving_record`; trong mock nhúng
 * vào bản ghi phát suất cho gọn — quan hệ vẫn là 1-n, không mất thông tin.
 */
export interface ServingDish {
  dishId: string;
  dishName: string;
  /** true khi đây là suất riêng, không phải món chung của khối. */
  isSubstitute: boolean;
  /** Món gốc bị thay (chỉ có khi isSubstitute). */
  substitutedForDishName?: string;
  /** Vì sao được thay: "Dị ứng Tôm" / "Chế độ Vegetarian". */
  reason?: string;
}

export interface StudentDistribution {
  id: string;
  date: string;
  /**
   * Buổi ăn của bản ghi phát suất. BR-130 nói "duplicate scan for this meal
   * session" — tức bản ghi vốn thuộc về một BỮA cụ thể, nhưng trước 22/07/2026
   * chỉ có ngày, nên không tra được phiên kiểm thực của bữa đó để áp BR-094.
   */
  mealType: MealType;
  studentId: string;
  name: string;
  class: string;
  /** Suất ăn được phân công (BR-125) — nhãn hiển thị, KHÔNG dùng để truy xuất. */
  assignedMeal: string;
  /** Suất thay thế nếu có (BR-065) — nhãn hiển thị. */
  specialMeal?: string;
  /**
   * Suất ăn THỰC TẾ đã phát, đóng băng tại thời điểm giáo viên xác nhận (gap T3).
   *
   * Trước 22/07/2026 suất ăn chỉ tồn tại dưới dạng chuỗi `assignedMeal` và được
   * DẪN XUẤT lại mỗi lần xem, nên hai hệ quả:
   *   - không trả lời được "em nào đã ăn món nhiễm?" khi điều tra sự cố;
   *   - học sinh đổi chế độ ăn sau đó thì LỊCH SỬ BỊ VIẾT LẠI — suất đã phát tháng
   *     trước hiện ra theo hồ sơ dị ứng của hôm nay.
   *
   * Chốt lúc phát và không tính lại. Rỗng = chưa phát (`status` vẫn PENDING).
   */
  servedDishes?: ServingDish[];
  dietTag?: string;
  allergens: string[];
  /** BR-123 — allergen trùng thực đơn hôm nay → Allergy Warning Banner */
  allergyWarning?: string;
  /** Cờ lọc tab "Special Meal" (mock-server lọc chính xác theo field) */
  isSpecial: "yes" | "no";
  status: keyof typeof STATUS_BADGE; // PENDING | SERVED
  servedAt?: string;
}

/**
 * Gộp bản ghi phát suất từng học sinh thành dòng tổng hợp NGÀY × LỚP.
 * Đây là nguồn của widget "Phân phát bữa ăn" (Giáo viên) và cột "Phân phát gần
 * nhất" (đơn vị suất ăn) — hai màn trước đây đọc mảng seed riêng nên không thấy
 * việc giáo viên vừa phát.
 */
export function distributionSummaries(rows: StudentDistribution[]): DistributionRecord[] {
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
export const STUDENT_DISTRIBUTIONS: StudentDistribution[] = [
  // ===== HÔM NAY lớp 3A — MENU-TD-3 (thực đơn chay) — tất cả PENDING để phát thử =====
  // Thực đơn chay nên không em nào cần suất riêng; cột dị ứng vẫn ghi để cảnh báo.
  // Sửa 27/07/2026 (review/teacher): HS-3A-001/009 trước đây có thêm
  // `allergyWarning` dù ghi rõ "thực đơn chay không chạm" (tức KHÔNG xung đột)
  // và `isSpecial: "no"` — mâu thuẫn ngay trong chính dòng dữ liệu, khiến 2 em
  // này hiện banner đỏ "bắt buộc phát suất thay thế" trong khi thực đơn hôm nay
  // vốn đã an toàn với các em, còn HS-3A-003/005 (cũng có allergen, cũng
  // `isSpecial:"no"`) chỉ hiện badge vàng thông tin. Bỏ `allergyWarning` ở 2
  // dòng đó cho nhất quán với đúng comment ở trên: mọi em `isSpecial:"no"` hôm
  // nay chỉ hiện allergen dạng thông tin, không hiện cảnh báo đỏ.
  { id: "DSTU-TD-3A-001", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-001", name: "Nguyễn Minh Khang", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", allergens: ["Tôm","Cua"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-002", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-002", name: "Trần Bảo An", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-003", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-003", name: "Lê Gia Hân", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", dietTag: "Nut-Free", allergens: ["Đậu phộng"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-004", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-004", name: "Phạm Đức Minh", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", dietTag: "Vegetarian", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-005", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-005", name: "Vũ Ngọc Diệp", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", dietTag: "Lactose-Free", allergens: ["Sữa bò"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-006", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-006", name: "Hoàng Tuấn Kiệt", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", dietTag: "Diabetic", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-007", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-007", name: "Đặng Thùy Linh", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-008", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-008", name: "Bùi Nhật Nam", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-009", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-009", name: "Ngô Khánh Vy", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", allergens: ["Trứng"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-TD-3A-010", date: vnInDays(0), mealType: "LUNCH", studentId: "HS-3A-010", name: "Đỗ Quang Huy", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", allergens: [], isSpecial: "no", status: "PENDING" },

  // 06/07/2026 lớp 3A — MENU-2026-27
  { id: "DSTU-0706A-001", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-001", name: "Nguyễn Minh Khang", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ thịt băm · Su su xào", specialMeal: "Canh bí đỏ thịt băm (thay Canh bí đỏ nấu tôm)", allergens: ["Tôm","Cua"], allergyWarning: "Dị ứng Tôm, Cua", isSpecial: "yes", status: "PENDING" },
  { id: "DSTU-0706A-002", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-002", name: "Trần Bảo An", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", allergens: [], isSpecial: "no", status: "SERVED", servedAt: "10:47 06/07/2026", servedDishes: [{ dishId: "com-trang", dishName: "Cơm trắng", isSubstitute: false }, { dishId: "thit-kho-trung-cut", dishName: "Thịt kho trứng cút", isSubstitute: false }, { dishId: "canh-bi-do-nau-tom", dishName: "Canh bí đỏ nấu tôm", isSubstitute: false }, { dishId: "su-su-xao", dishName: "Su su xào", isSubstitute: false }] },
  { id: "DSTU-0706A-003", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-003", name: "Lê Gia Hân", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", dietTag: "Nut-Free", allergens: ["Đậu phộng"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0706A-004", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-004", name: "Phạm Đức Minh", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", specialMeal: "Đậu phụ sốt cà chua (thay Thịt kho trứng cút); Canh bí đỏ chay (thay Canh bí đỏ nấu tôm)", dietTag: "Vegetarian", allergens: [], allergyWarning: "Chế độ Vegetarian; Chế độ Vegetarian", isSpecial: "yes", status: "PENDING" },
  { id: "DSTU-0706A-005", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-005", name: "Vũ Ngọc Diệp", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", dietTag: "Lactose-Free", allergens: ["Sữa bò"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0706A-006", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-006", name: "Hoàng Tuấn Kiệt", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", allergens: [], isSpecial: "no", status: "SERVED", servedAt: "10:48 06/07/2026", servedDishes: [{ dishId: "com-trang", dishName: "Cơm trắng", isSubstitute: false }, { dishId: "thit-kho-trung-cut", dishName: "Thịt kho trứng cút", isSubstitute: false }, { dishId: "canh-bi-do-nau-tom", dishName: "Canh bí đỏ nấu tôm", isSubstitute: false }, { dishId: "su-su-xao", dishName: "Su su xào", isSubstitute: false }] },
  { id: "DSTU-0706A-007", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-007", name: "Đặng Thùy Linh", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", allergens: [], isSpecial: "no", status: "SERVED", servedAt: "10:49 06/07/2026", servedDishes: [{ dishId: "com-trang", dishName: "Cơm trắng", isSubstitute: false }, { dishId: "thit-kho-trung-cut", dishName: "Thịt kho trứng cút", isSubstitute: false }, { dishId: "canh-bi-do-nau-tom", dishName: "Canh bí đỏ nấu tôm", isSubstitute: false }, { dishId: "su-su-xao", dishName: "Su su xào", isSubstitute: false }] },
  { id: "DSTU-0706A-008", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-008", name: "Bùi Nhật Nam", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0706A-009", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-009", name: "Ngô Khánh Vy", class: "3A", assignedMeal: "Cơm trắng · Thịt kho tàu · Canh bí đỏ nấu tôm · Su su xào", specialMeal: "Thịt kho tàu (thay Thịt kho trứng cút)", allergens: ["Trứng"], allergyWarning: "Dị ứng Trứng", isSpecial: "yes", status: "PENDING" },
  { id: "DSTU-0706A-010", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3A-010", name: "Đỗ Quang Huy", class: "3A", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", allergens: [], isSpecial: "no", status: "PENDING" },
  // 06/07/2026 lớp 3B — MENU-2026-27
  { id: "DSTU-0706B-001", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3B-001", name: "Trịnh Bảo Ngọc", class: "3B", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ thịt băm · Su su xào", specialMeal: "Canh bí đỏ thịt băm (thay Canh bí đỏ nấu tôm)", allergens: ["Hải sản"], allergyWarning: "Dị ứng Hải sản", isSpecial: "yes", status: "PENDING" },
  { id: "DSTU-0706B-002", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3B-002", name: "Lý Gia Bảo", class: "3B", assignedMeal: "Cơm trắng · Thịt kho trứng cút · Canh bí đỏ nấu tôm · Su su xào", allergens: [], isSpecial: "no", status: "SERVED", servedAt: "10:52 06/07/2026", servedDishes: [{ dishId: "com-trang", dishName: "Cơm trắng", isSubstitute: false }, { dishId: "thit-kho-trung-cut", dishName: "Thịt kho trứng cút", isSubstitute: false }, { dishId: "canh-bi-do-nau-tom", dishName: "Canh bí đỏ nấu tôm", isSubstitute: false }, { dishId: "su-su-xao", dishName: "Su su xào", isSubstitute: false }] },
  { id: "DSTU-0706B-003", date: "06/07/2026", mealType: "LUNCH", studentId: "HS-3B-003", name: "Cao Minh Anh", class: "3B", assignedMeal: "Cơm trắng · Gà kho gừng · Canh bí đỏ nấu tôm · Su su xào", specialMeal: "Gà kho gừng (thay Thịt kho trứng cút)", dietTag: "Halal", allergens: [], allergyWarning: "Chế độ Halal", isSpecial: "yes", status: "PENDING" },
  // 07/07/2026 lớp 3A — MENU-2026-27B (kiểm thực Bước 2 KHÔNG ĐẠT ⇒ không suất nào được phát)
  { id: "DSTU-0707A-001", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-001", name: "Nguyễn Minh Khang", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: ["Tôm","Cua"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-002", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-002", name: "Trần Bảo An", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-003", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-003", name: "Lê Gia Hân", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", dietTag: "Nut-Free", allergens: ["Đậu phộng"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-004", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-004", name: "Phạm Đức Minh", class: "3A", assignedMeal: "Cơm trắng · Đậu phụ sốt cà chua · Canh rau ngót chay · Giá xào", specialMeal: "Đậu phụ sốt cà chua (thay Cá basa sốt cà chua); Canh rau ngót chay (thay Canh rau ngót thịt băm)", dietTag: "Vegetarian", allergens: [], allergyWarning: "Chế độ Vegetarian; Chế độ Vegetarian", isSpecial: "yes", status: "PENDING" },
  { id: "DSTU-0707A-005", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-005", name: "Vũ Ngọc Diệp", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", dietTag: "Lactose-Free", allergens: ["Sữa bò"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-006", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-006", name: "Hoàng Tuấn Kiệt", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-007", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-007", name: "Đặng Thùy Linh", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-008", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-008", name: "Bùi Nhật Nam", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: [], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-009", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-009", name: "Ngô Khánh Vy", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: ["Trứng"], isSpecial: "no", status: "PENDING" },
  { id: "DSTU-0707A-010", date: "07/07/2026", mealType: "LUNCH", studentId: "HS-3A-010", name: "Đỗ Quang Huy", class: "3A", assignedMeal: "Cơm trắng · Cá basa sốt cà chua · Canh rau ngót thịt băm · Giá xào", allergens: [], isSpecial: "no", status: "PENDING" },
];

// ===== Ghi nhận tiêu thụ bữa ăn (07_MealRecords — UC-056/UC-080, BR-121/125) =====

export interface TeacherMealRecord {
  id: string;
  date: string;
  studentId: string;
  student: string;
  dishes: string;
  specialDiet?: string;
  /** Suất riêng đã phát cho học sinh chế độ ăn đặc biệt (BR-125) */
  specialMealServed?: string;
  consumption: keyof typeof STATUS_BADGE; // PENDING | FULLY | PARTIALLY | NOT_CONSUMED
  recordedAt?: string;
}

export const TEACHER_MEAL_RECORDS: TeacherMealRecord[] = [
  // ===== HÔM NAY lớp 3A — MENU-TD-3 (thực đơn chay), khớp đúng
  // `STUDENT_DISTRIBUTIONS` "hôm nay" (cùng `vnInDays(0)`, review/teacher —
  // phát hiện 27/07/2026 khi kiểm `teacher/meal-records`). Trước đây Ghi nhận
  // bữa ăn chỉ có 2 lô cố định 03/07 và 02/07 — giáo viên vừa phát suất hôm nay
  // (Distribution List) xong thì không có dòng nào để ghi nhận tiêu thụ đúng
  // buổi đó, màn hình hiện "ngày 3" trong khi đang là ngày chạy thực.
  { id: "MR-TD-3A-001", date: vnInDays(0), studentId: "HS-3A-001", student: "Nguyễn Minh Khang", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", consumption: "PENDING" },
  { id: "MR-TD-3A-002", date: vnInDays(0), studentId: "HS-3A-002", student: "Trần Bảo An", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", consumption: "PENDING" },
  { id: "MR-TD-3A-003", date: vnInDays(0), studentId: "HS-3A-003", student: "Lê Gia Hân", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", specialDiet: "Nut-Free", consumption: "PENDING" },
  { id: "MR-TD-3A-004", date: vnInDays(0), studentId: "HS-3A-004", student: "Phạm Đức Minh", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", specialDiet: "Vegetarian", consumption: "PENDING" },
  { id: "MR-TD-3A-005", date: vnInDays(0), studentId: "HS-3A-005", student: "Vũ Ngọc Diệp", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", specialDiet: "Lactose-Free", consumption: "PENDING" },
  { id: "MR-TD-3A-006", date: vnInDays(0), studentId: "HS-3A-006", student: "Hoàng Tuấn Kiệt", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", specialDiet: "Diabetic", consumption: "PENDING" },
  { id: "MR-TD-3A-007", date: vnInDays(0), studentId: "HS-3A-007", student: "Đặng Thùy Linh", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", consumption: "PENDING" },
  { id: "MR-TD-3A-008", date: vnInDays(0), studentId: "HS-3A-008", student: "Bùi Nhật Nam", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", consumption: "PENDING" },
  { id: "MR-TD-3A-009", date: vnInDays(0), studentId: "HS-3A-009", student: "Ngô Khánh Vy", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", consumption: "PENDING" },
  { id: "MR-TD-3A-010", date: vnInDays(0), studentId: "HS-3A-010", student: "Đỗ Quang Huy", dishes: "Cơm trắng · Đậu phụ sốt cà chua · Canh bí đỏ chay · Su su xào", consumption: "PENDING" },
  { id: "MR-0703-001", date: "03/07/2026", studentId: "HS-3A-001", student: "Nguyễn Minh Khang", dishes: "Cơm trắng · Thịt rim · Canh khoai tây cà rốt · Rau muống xào tỏi", specialMealServed: "Thịt rim (thay Tôm rim thịt)", consumption: "PENDING" },
  { id: "MR-0703-003", date: "03/07/2026", studentId: "HS-3A-003", student: "Lê Gia Hân", dishes: "Suất Nut-Free", specialDiet: "Nut-Free", specialMealServed: "Suất Nut-Free", consumption: "PENDING" },
  { id: "MR-0703-004", date: "03/07/2026", studentId: "HS-3A-004", student: "Phạm Đức Minh", dishes: "Cơm trắng · Đậu phụ sốt · Canh khoai tây cà rốt", specialDiet: "Vegetarian", specialMealServed: "Đậu phụ sốt (thay Tôm rim thịt)", consumption: "PENDING" },
  { id: "MR-0703-005", date: "03/07/2026", studentId: "HS-3A-005", student: "Vũ Ngọc Diệp", dishes: "Suất chuẩn + sữa đậu nành", specialDiet: "Lactose-Free", specialMealServed: "Sữa đậu nành (bữa phụ)", consumption: "PENDING" },
  { id: "MR-0703-002", date: "03/07/2026", studentId: "HS-3A-002", student: "Trần Bảo An", dishes: "Suất chuẩn — Tôm rim thịt", consumption: "PENDING" },
  { id: "MR-0702-001", date: "02/07/2026", studentId: "HS-3A-001", student: "Nguyễn Minh Khang", dishes: "Cơm trắng · Thịt bò xào rau củ · Canh chua cá · Đậu phụ sốt", consumption: "FULLY", recordedAt: "11:20 02/07/2026" },
  { id: "MR-0702-003", date: "02/07/2026", studentId: "HS-3A-003", student: "Lê Gia Hân", dishes: "Suất Nut-Free", specialDiet: "Nut-Free", specialMealServed: "Suất Nut-Free", consumption: "FULLY", recordedAt: "11:22 02/07/2026" },
  { id: "MR-0702-004", date: "02/07/2026", studentId: "HS-3A-004", student: "Phạm Đức Minh", dishes: "Cơm trắng · Đậu phụ sốt · Canh chua cá (chay)", specialDiet: "Vegetarian", specialMealServed: "Canh chua chay", consumption: "PARTIALLY", recordedAt: "11:25 02/07/2026" },
  { id: "MR-0702-005", date: "02/07/2026", studentId: "HS-3A-005", student: "Vũ Ngọc Diệp", dishes: "Suất chuẩn + sữa đậu nành", specialDiet: "Lactose-Free", specialMealServed: "Sữa đậu nành (bữa phụ)", consumption: "NOT_CONSUMED", recordedAt: "11:30 02/07/2026" },
];

// ===== Thông tin sức khỏe/dị ứng học sinh — Teacher chỉ đọc (04/05 — UC-145, BR-030/031/172) =====

export interface TeacherStudentHealth {
  height: number;
  weight: number;
  bmi: number;
  bloodType: string;
  chronicConditions: string[];
  emergencyNote: string;
  status: keyof typeof STATUS_BADGE;
  updatedAt: string;
  allergies: { allergen: string; severity: "Nhẹ" | "Trung bình" | "Nặng"; status: keyof typeof STATUS_BADGE }[];
}

export const STUDENT_HEALTH_INFO: Record<string, TeacherStudentHealth> = {
  "HS-3A-001": { height: 128, weight: 26.5, bmi: 16.2, bloodType: "O", chronicConditions: [], emergencyNote: "Bố: Nguyễn Văn Bình — 0912 345 678", status: "VERIFIED", updatedAt: "15/01/2026", allergies: [{ allergen: "Tôm", severity: "Nặng", status: "VERIFIED" }, { allergen: "Cua", severity: "Nặng", status: "VERIFIED" }] },
  "HS-3A-003": { height: 125, weight: 24.0, bmi: 15.4, bloodType: "A", chronicConditions: ["Hen suyễn nhẹ — có ống hít tại phòng y tế"], emergencyNote: "Bố: Lê Hoàng Nam — 0987 654 321", status: "VERIFIED", updatedAt: "20/01/2026", allergies: [{ allergen: "Đậu phộng", severity: "Nặng", status: "VERIFIED" }] },
  "HS-3A-004": { height: 130, weight: 28.0, bmi: 16.6, bloodType: "B", chronicConditions: [], emergencyNote: "Bố: Phạm Văn Cường — 0903 111 222", status: "PENDING", updatedAt: "28/06/2026", allergies: [] },
  "HS-3A-005": { height: 126, weight: 25.2, bmi: 15.9, bloodType: "AB", chronicConditions: ["Bất dung nạp lactose"], emergencyNote: "Bố: Vũ Đình Phong — 0934 555 666", status: "NEEDS_INFO", updatedAt: "22/06/2026", allergies: [{ allergen: "Sữa bò", severity: "Trung bình", status: "NEEDS_INFO" }] },
  "HS-3A-009": { height: 127, weight: 26.0, bmi: 16.1, bloodType: "O", chronicConditions: [], emergencyNote: "Bố: Ngô Đức Anh — 0977 888 999", status: "VERIFIED", updatedAt: "10/02/2026", allergies: [{ allergen: "Trứng", severity: "Nhẹ", status: "VERIFIED" }] },
};

// ===== Yêu cầu chế độ ăn đặc biệt của lớp (06 — UC-143, BR-065/066/082/125) =====

export interface SpecialDietRequirement {
  id: string;
  studentId: string;
  student: string;
  dietType: string;
  reason: string;
  approval: keyof typeof STATUS_BADGE;
  /** BR-082 — ghi chú chế biến riêng */
  prepNote: string;
}

export const SPECIAL_DIET_REQUIREMENTS: SpecialDietRequirement[] = [
  { id: "SDR-3A-01", studentId: "HS-3A-003", student: "Lê Gia Hân", dietType: "Nut-Free", reason: "Dị ứng đậu phộng mức Nặng (đã xác minh y tế)", approval: "APPROVED", prepNote: "Không dùng dầu lạc/bơ đậu phộng; chế biến tại khu riêng, dụng cụ tách biệt" },
  { id: "SDR-3A-02", studentId: "HS-3A-004", student: "Phạm Đức Minh", dietType: "Vegetarian", reason: "Yêu cầu của gia đình (ăn chay)", approval: "PENDING_APPROVAL", prepNote: "Thay món mặn bằng đậu phụ/nấm; không dùng nước dùng xương" },
  { id: "SDR-3A-03", studentId: "HS-3A-005", student: "Vũ Ngọc Diệp", dietType: "Lactose-Free", reason: "Bất dung nạp lactose (đơn bác sĩ)", approval: "APPROVED", prepNote: "Thay sữa bò bằng sữa đậu nành ở bữa phụ; kiểm tra thành phần sữa trong món tráng miệng" },
];

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

export const INVENTORY_TRANSACTIONS: InventoryTransaction[] = [
  /*
   * Bổ sung 22/07/2026 — hai món còn thiếu của bữa trưa 06/07 (`MENU-2026-27`).
   * Bữa này là chuỗi truy xuất mẫu của hệ thống nhưng phiếu xuất chỉ có 2/4 món,
   * nên truy ngược từ mẫu lưu "Cơm trắng"/"Canh bí đỏ nấu tôm" không ra lô nào.
   */
  { id: "TXN-20260706-06", timestamp: "06/07/2026 05:40", type: "Xuất kho (FEFO)", batchId: "BATCH-GF-4410", ingredientId: "ING-GAO-TAM", ingredient: "Gạo tám thơm", quantity: "-30.4 kg", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Cơm trắng", serviceDate: "06/07/2026" },
  { id: "TXN-20260706-07", timestamp: "06/07/2026 05:44", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2195", ingredientId: "ING-BI-DO", ingredient: "Bí đỏ", quantity: "-31.8 kg", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Canh bí đỏ nấu tôm", serviceDate: "06/07/2026" },
  { id: "TXN-20260706-08", timestamp: "06/07/2026 05:46", type: "Xuất kho (FEFO)", batchId: "BATCH-DD-1080", ingredientId: "ING-TOM-SU", ingredient: "Tôm sú", quantity: "-5.5 kg", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Canh bí đỏ nấu tôm", serviceDate: "06/07/2026" },
  /*
   * Bổ sung 22/07/2026 — phiếu xuất của các bữa CŨ (01/07, 02/07).
   *
   * Hai ngày này đã có mẫu lưu và phiên kiểm thực trong seed, nhưng KHÔNG có phiếu
   * xuất kho nào. Hệ quả ở màn điều tra sự cố: món hiện ra để tick (lấy từ mẫu lưu
   * trong khung 72 giờ) nhưng bấm vào thì không ra lô nào để cách ly, vì bản đồ
   * truy xuất dựng từ phiếu xuất. Nhìn như tính năng hỏng, thực ra là thiếu mắt
   * xích dữ liệu.
   *
   * Không gắn `menuId`: thực đơn số hoá trong hệ thống bắt đầu từ 06/07, các bữa
   * trước đó nhập liệu sau nên chỉ có phiếu xuất, không có bản ghi thực đơn. Bịa
   * thêm thực đơn cho khớp còn tệ hơn — `menuId` là trường tuỳ chọn đúng cho ca này.
   */
  { id: "TXN-20260702-02", timestamp: "02/07/2026 05:48", type: "Xuất kho (FEFO)", batchId: "BATCH-MP-8825", ingredientId: "ING-TRUNG-CUT", ingredient: "Trứng cút", quantity: "-1140 quả", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Thịt kho trứng cút", serviceDate: "02/07/2026" },
  { id: "TXN-20260702-01", timestamp: "02/07/2026 05:45", type: "Xuất kho (FEFO)", batchId: "BATCH-MP-8830", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai", quantity: "-24.1 kg", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Thịt kho trứng cút", serviceDate: "02/07/2026" },
  { id: "TXN-20260702-03", timestamp: "02/07/2026 05:52", type: "Xuất kho (FEFO)", batchId: "BATCH-DD-1080", ingredientId: "ING-TOM-SU", ingredient: "Tôm sú", quantity: "-5.5 kg", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Canh bí đỏ nấu tôm", serviceDate: "02/07/2026" },
  { id: "TXN-20260702-04", timestamp: "02/07/2026 05:53", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2195", ingredientId: "ING-BI-DO", ingredient: "Bí đỏ", quantity: "-31.8 kg", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Canh bí đỏ nấu tôm", serviceDate: "02/07/2026" },
  { id: "TXN-20260701-01", timestamp: "01/07/2026 05:47", type: "Xuất kho (FEFO)", batchId: "BATCH-DD-1095", ingredientId: "ING-CA-BASA", ingredient: "Cá basa phi lê", quantity: "-26.6 kg", actor: "Trần Văn Hùng", mealType: "LUNCH", dishName: "Cá basa sốt cà chua", serviceDate: "01/07/2026" },
  // --- Xuất kho theo thực đơn 07/07 (MENU-2026-27B, Khối 3, 380 suất) ---
  // Định lượng = BOM mỗi suất × số suất, quy đổi g→kg / ml→lít; lô do FEFO chọn.
  { id: "TXN-20260707-05", timestamp: "07/07/2026 05:52", type: "Xuất kho (FEFO)", batchId: "BATCH-GF-4420", ingredientId: "ING-DAU-AN", ingredient: "Dầu ăn thực vật", quantity: "-1.9 lít", actor: "Trần Văn Hùng", menuId: "MENU-2026-27B", mealType: "LUNCH", dishName: "Cá basa sốt cà chua", serviceDate: "07/07/2026" },
  { id: "TXN-20260707-04", timestamp: "07/07/2026 05:51", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2210", ingredientId: "ING-CA-CHUA", ingredient: "Cà chua", quantity: "-15.2 kg", actor: "Trần Văn Hùng", menuId: "MENU-2026-27B", mealType: "LUNCH", dishName: "Cá basa sốt cà chua", serviceDate: "07/07/2026" },
  { id: "TXN-20260707-03", timestamp: "07/07/2026 05:50", type: "Xuất kho (FEFO)", batchId: "BATCH-DD-1095", ingredientId: "ING-CA-BASA", ingredient: "Cá basa phi lê", quantity: "-26.6 kg", actor: "Trần Văn Hùng", menuId: "MENU-2026-27B", mealType: "LUNCH", dishName: "Cá basa sốt cà chua", serviceDate: "07/07/2026" },
  { id: "TXN-20260707-02", timestamp: "07/07/2026 05:48", type: "Xuất kho (FEFO)", batchId: "BATCH-MP-8842", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai", quantity: "-7.6 kg", actor: "Trần Văn Hùng", menuId: "MENU-2026-27B", mealType: "LUNCH", dishName: "Canh rau ngót thịt băm", serviceDate: "07/07/2026" },
  { id: "TXN-20260707-01", timestamp: "07/07/2026 05:45", type: "Xuất kho (FEFO)", batchId: "BATCH-GF-4410", ingredientId: "ING-GAO-TAM", ingredient: "Gạo tám thơm", quantity: "-30.4 kg", actor: "Trần Văn Hùng", menuId: "MENU-2026-27B", mealType: "LUNCH", dishName: "Cơm trắng", serviceDate: "07/07/2026" },

  // --- Xuất kho theo thực đơn 06/07 (MENU-2026-27, Khối 3, 380 suất) ---
  { id: "TXN-20260706-05", timestamp: "06/07/2026 05:55", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2211", ingredientId: "ING-TOI", ingredient: "Tỏi", quantity: "-1.14 kg", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Su su xào", serviceDate: "06/07/2026" },
  { id: "TXN-20260706-04", timestamp: "06/07/2026 05:54", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2220", ingredientId: "ING-SU-SU", ingredient: "Su su", quantity: "-26.6 kg", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Su su xào", serviceDate: "06/07/2026" },
  { id: "TXN-20260706-03", timestamp: "06/07/2026 05:52", type: "Xuất kho (FEFO)", batchId: "BATCH-GF-4401", ingredientId: "ING-NUOC-MAM", ingredient: "Nước mắm truyền thống", quantity: "-1.52 lít", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Thịt kho trứng cút", serviceDate: "06/07/2026" },
  { id: "TXN-20260706-02", timestamp: "06/07/2026 05:50", type: "Xuất kho (FEFO)", batchId: "BATCH-MP-8825", ingredientId: "ING-TRUNG-CUT", ingredient: "Trứng cút", quantity: "-1140 quả", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Thịt kho trứng cút", serviceDate: "06/07/2026" },
  { id: "TXN-20260706-01", timestamp: "06/07/2026 05:48", type: "Xuất kho (FEFO)", batchId: "BATCH-MP-8842", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai", quantity: "-22.8 kg", actor: "Lê Thị Mai", menuId: "MENU-2026-27", mealType: "LUNCH", dishName: "Thịt kho trứng cút", serviceDate: "06/07/2026" },

  // --- Bữa trưa 30/06 — dữ liệu gốc của sự cố INC-2026-014 ---
  // Không gắn menuId vì đây là bản ghi lịch sử trước khi luồng xuất kho theo
  // thực đơn được áp dụng; `dishName` vẫn đủ để dựng bản đồ truy xuất (BR-151).
  // Hai món còn lại của bữa 30/06 — thiếu phiếu xuất thì mẫu lưu của chúng không
  // truy ngược ra lô nào để cách ly khi điều tra.
  { id: "TXN-20260630-02", timestamp: "30/06/2026 05:42", type: "Xuất kho (FEFO)", batchId: "BATCH-GF-4410", ingredientId: "ING-GAO-TAM", ingredient: "Gạo tám thơm", quantity: "-29.9 kg", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Cơm trắng", serviceDate: "30/06/2026" },
  { id: "TXN-20260630-03", timestamp: "30/06/2026 05:56", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2188", ingredientId: "ING-SU-SU", ingredient: "Su su", quantity: "-26.2 kg", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Su su xào", serviceDate: "30/06/2026" },
  { id: "TXN-20260630-04", timestamp: "30/06/2026 05:58", type: "Xuất kho (FEFO)", batchId: "BATCH-VN-2211", ingredientId: "ING-TOI", ingredient: "Tỏi", quantity: "-0.9 kg", actor: "Lê Thị Mai", mealType: "LUNCH", dishName: "Su su xào", serviceDate: "30/06/2026" },
  { id: "TXN-20260630-01", timestamp: "30/06/2026 05:50", type: "Xuất kho (FEFO)", batchId: "BATCH-MP-8820", ingredientId: "ING-THIT-GA", ingredient: "Thịt gà ta", quantity: "-22.8 kg", actor: "Lê Thị Mai", dishName: "Gà rang gừng", serviceDate: "30/06/2026" },

  // --- Nhập kho & điều chỉnh (không gắn món — chưa có mục đích sử dụng cụ thể) ---
  { id: "TXN-20260702-04", timestamp: "02/07/2026 06:20", type: "Nhập kho", batchId: "BATCH-GF-4411", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai (bổ sung)", quantity: "+45 kg", actor: "Trần Văn Hùng" },
  { id: "TXN-20260702-03", timestamp: "02/07/2026 06:18", type: "Nhập kho", batchId: "BATCH-GF-4412", ingredientId: "ING-RAU-CAI-NGOT", ingredient: "Rau cải ngọt", quantity: "+30 kg", actor: "Trần Văn Hùng" },
  { id: "TXN-20260625-01", timestamp: "25/06/2026 13:47", type: "Điều chỉnh", batchId: "BATCH-GF-4388", ingredientId: "ING-DAU-AN", ingredient: "Dầu ăn thực vật", quantity: "-2 lít", reasonCode: "spoilage", actor: "Trần Văn Hùng", managerSignature: "Lê Thị Mai" },
  { id: "TXN-20260701-02", timestamp: "01/07/2026 06:30", type: "Nhập kho", batchId: "BATCH-VN-2201", ingredientId: "ING-RAU-MUONG", ingredient: "Rau muống", quantity: "+25 kg", actor: "Trần Văn Hùng" },
  { id: "TXN-20260701-01", timestamp: "01/07/2026 06:05", type: "Nhập kho", batchId: "BATCH-MP-8830", ingredientId: "ING-THIT-LON-VAI", ingredient: "Thịt lợn nạc vai", quantity: "+45 kg", actor: "Lê Thị Mai" },
];

// ===== Phiếu xuất kho (IssueSlip) =====

/**
 * PHIẾU XUẤT KHO — bản ghi cha nhóm nhiều dòng INVENTORY_TRANSACTIONS cùng bữa.
 *
 * Trước khi có bảng này, truy xuất "bữa trưa 06/07 dùng những lô nào" phải join
 * ngầm qua (serviceDate, mealType) trên từng dòng xuất kho — không có ID phiếu
 * riêng nên audit log không ghi được "ai tạo phiếu, lúc mấy giờ". Bổ sung
 * IssueSlip giải quyết đúng mắt xích còn thiếu trong chuỗi:
 *   InventoryBatch → IssueSlip → IssueSlipLine (= InventoryTransaction) → Dish → Menu.
 *
 * Liên kết với INVENTORY_TRANSACTIONS qua (serviceDate, mealType): mọi giao dịch
 * "Xuất kho (FEFO)" khớp với slip cùng ngày + loại bữa thuộc về slip đó.
 * Không thêm cột issueSlipId vào từng dòng transaction để không phá cấu trúc hiện có.
 */
export interface IssueSlip {
  id: string;
  /** Ngày phục vụ dd/mm/yyyy — khớp serviceDate trên INVENTORY_TRANSACTIONS. */
  serviceDate: string;
  mealType: MealType;
  /** Mã thực đơn (có thể nhiều khối) — dùng để tra ngược lại kế hoạch. */
  menuIds: string[];
  /** Tổng số dòng xuất kho (đếm INVENTORY_TRANSACTIONS khớp slip này). */
  lineCount: number;
  createdBy: string;
  createdAt: string;
  /** Trạng thái duyệt phiếu: DRAFT → CONFIRMED → POSTED (đã vào sổ kho). */
  status: "DRAFT" | "CONFIRMED" | "POSTED";
  note?: string;
}

export const ISSUE_SLIPS: IssueSlip[] = [
  {
    id: "ISS-20260707",
    serviceDate: "07/07/2026",
    mealType: "LUNCH",
    menuIds: ["MENU-2026-27B"],
    lineCount: 5,
    createdBy: "Trần Văn Hùng",
    createdAt: "07/07/2026 05:30",
    status: "POSTED",
  },
  {
    id: "ISS-20260706",
    serviceDate: "06/07/2026",
    mealType: "LUNCH",
    menuIds: ["MENU-2026-27"],
    lineCount: 5,
    createdBy: "Lê Thị Mai",
    createdAt: "06/07/2026 05:30",
    status: "POSTED",
  },
  {
    id: "ISS-20260702",
    serviceDate: "02/07/2026",
    mealType: "LUNCH",
    menuIds: [],
    lineCount: 4,
    createdBy: "Lê Thị Mai",
    createdAt: "02/07/2026 05:30",
    status: "POSTED",
    note: "Bữa trước khi số hoá thực đơn — không có menuId.",
  },
  {
    id: "ISS-20260701",
    serviceDate: "01/07/2026",
    mealType: "LUNCH",
    menuIds: [],
    lineCount: 1,
    createdBy: "Trần Văn Hùng",
    createdAt: "01/07/2026 05:30",
    status: "POSTED",
  },
  {
    id: "ISS-20260630",
    serviceDate: "30/06/2026",
    mealType: "LUNCH",
    menuIds: [],
    lineCount: 4,
    createdBy: "Lê Thị Mai",
    createdAt: "30/06/2026 05:30",
    status: "POSTED",
    note: "Bữa nghi vấn sự cố INC-2026-014 — thiếu phiếu xuất cho 2 món còn lại.",
  },
];

/**
 * Tìm IssueSlip tương ứng với một giao dịch xuất kho.
 * Join qua (serviceDate, mealType) — không có cột issueSlipId trực tiếp.
 */
export function issueSlipOfTransaction(
  tx: Pick<InventoryTransaction, "serviceDate" | "mealType">
): IssueSlip | undefined {
  if (!tx.serviceDate || !tx.mealType) return undefined;
  return ISSUE_SLIPS.find(
    (s) => s.serviceDate === tx.serviceDate && s.mealType === tx.mealType
  );
}

// ===== Kế hoạch bữa ăn đặc biệt (SpecialMealPlan, BR-063/065/066/071) =====



// ===== Vi phạm quy trình & nhân sự (UC-178, BR-183/184/186) =====


// ===== Sổ chế biến (Record Preparation Information — spec 19/20) =====

export interface PreparationRecord {
  id: string;
  date: string;
  /** Tên món — chọn từ thực đơn của ngày, không nhập tay (khóa join với phiếu xuất). */
  dish: string;
  /** Khoá về THƯ VIỆN MÓN — nối ở vòng backfill. */
  dishId?: string;
  /**
   * Lô nguyên liệu đã dùng. KHÔNG nhập tay: prefill từ các phiếu xuất kho gắn
   * cùng (menuId, mealType, dish) — xem `batchesIssuedForDish()`. Đầu bếp chỉ
   * bổ sung lô phát sinh ngoài kế hoạch, và phải ghi lý do vào `notes`.
   */
  batchesUsed: string[];
  startTime: string;
  endTime: string;
  cook: string;
  notes?: string;
  /** Neo về thực đơn — nguồn của danh sách món và của phiếu xuất tương ứng (BR-076). */
  menuId?: string;
  mealType?: MealType;
  /** Lô do đầu bếp thêm tay ngoài phiếu xuất — tách riêng để audit đối chiếu. */
  extraBatches?: string[];
}

// Seed khớp thực đơn thật: mỗi bản ghi neo vào (menuId, mealType) và các lô
// trùng với phiếu xuất FEFO cùng món trong INVENTORY_TRANSACTIONS bên dưới.
// (Sửa 21/07/2026: bản cũ gán BATCH-GF-4410 = Gạo tám thơm cho món "Cá basa sốt
// cà chua" và BATCH-GF-4412 = Rau cải ngọt cho "Canh chua cá" — lô không hề nằm
// trong thành phần món, bản đồ truy xuất BR-151 vì thế trỏ sai nguyên liệu.)
export const PREPARATION_RECORDS: PreparationRecord[] = [
  { id: "PREP-20260707-02", date: "07/07/2026", menuId: "MENU-2026-27B", mealType: "LUNCH", dish: "Canh rau ngót thịt băm", batchesUsed: ["BATCH-MP-8842"], startTime: "08:10", endTime: "09:35", cook: "Lê Thị Mai", notes: "Nếm thử trước khi chia" },
  { id: "PREP-20260707-01", date: "07/07/2026", menuId: "MENU-2026-27B", mealType: "LUNCH", dish: "Cá basa sốt cà chua", batchesUsed: ["BATCH-DD-1095", "BATCH-VN-2210", "BATCH-GF-4420"], startTime: "07:30", endTime: "09:15", cook: "Trần Văn Hùng", notes: "Nhiệt độ lõi 85°C đạt BR-090" },
  { id: "PREP-20260706-02", date: "06/07/2026", menuId: "MENU-2026-27", mealType: "LUNCH", dish: "Su su xào", batchesUsed: ["BATCH-VN-2220", "BATCH-VN-2211"], startTime: "08:00", endTime: "09:30", cook: "Lê Thị Mai" },
  { id: "PREP-20260706-01", date: "06/07/2026", menuId: "MENU-2026-27", mealType: "LUNCH", dish: "Thịt kho trứng cút", batchesUsed: ["BATCH-MP-8842", "BATCH-MP-8825", "BATCH-GF-4401"], startTime: "07:45", endTime: "09:20", cook: "Trần Văn Hùng" },
  // Bản ghi lịch sử của bữa trưa 30/06 — món nghi vấn trong sự cố INC-2026-014.
  { id: "PREP-20260630-01", date: "30/06/2026", mealType: "LUNCH", dish: "Gà rang gừng", batchesUsed: ["BATCH-MP-8820"], startTime: "07:40", endTime: "09:10", cook: "Lê Thị Mai", notes: "Nhiệt lõi 68°C < 75°C — Bước 2 không đạt (INSP-20260630-L)" },
];

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
 * Truyền `classrooms` từ db khi gọi trong màn hình; bỏ trống thì dùng seed.
 */
export function gradeLevelsOf(classrooms: Pick<Classroom, "grade">[] = CLASSROOMS): string[] {
  return [...new Set(classrooms.map((c) => c.grade))]
    .sort((a, b) => a - b)
    .map((g) => `Khối ${g}`);
}

/** Sĩ số của một khối, cộng từ các lớp thuộc khối đó. */
export function gradeEnrollmentOf(
  grade: string,
  classrooms: Pick<Classroom, "grade" | "students">[] = CLASSROOMS
): number {
  const n = Number(grade.replace(/\D/g, ""));
  return classrooms.filter((c) => c.grade === n).reduce((sum, c) => sum + c.students, 0);
}

/** @deprecated Dùng `gradeLevelsOf(classrooms)` để bám cấu hình lớp thật. */
export const GRADE_LEVELS = gradeLevelsOf();

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
 */
export function isAllergenVerified(studentId: string, allergen: string): boolean {
  return ALLERGY_DECLARATIONS.some(
    (a) => a.studentId === studentId && a.allergen === allergen && a.status === "VERIFIED"
  );
}

/**
 * Tra tác nhân dị ứng của 1 dòng thành phần món, theo thứ tự tin cậy giảm dần:
 *   1. `ingredientId` → catalog (chính xác tuyệt đối, BR-076)
 *   2. tên khớp đúng catalog
 *   3. dò từ khoá trên tên tự do (kém tin cậy — chỉ chữ có dấu)
 *
 * Trước đây hàm tương đương ở menu-form trả về [] khi tên không khớp catalog,
 * KHÔNG dò từ khoá và KHÔNG báo gì → nguyên liệu gõ tay ("Tôm sú đông lạnh" ≠
 * "Tôm sú") bị coi là không có allergen. `source` trả về để UI cảnh báo thay vì
 * im lặng bỏ qua (BR-063/076).
 */
export function resolveIngredientAllergens(row: {
  name: string;
  ingredientId?: string;
}): { allergens: string[]; source: "catalog" | "keyword" | "unknown" } {
  const byId = row.ingredientId
    ? INGREDIENT_CATALOG.find((c) => c.id === row.ingredientId)
    : undefined;
  if (byId) return { allergens: [...byId.allergens], source: "catalog" };

  const name = row.name.trim();
  if (!name) return { allergens: [], source: "unknown" };

  const byName = INGREDIENT_CATALOG.find(
    (c) => c.name.toLowerCase() === name.toLowerCase()
  );
  if (byName) return { allergens: [...byName.allergens], source: "catalog" };

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

export const INGREDIENT_CATALOG: CatalogIngredient[] = [
  // Bổ sung 22/07/2026: 8 nguyên liệu này đã được dùng trong thư viện món nhưng
  // KHÔNG có trong danh mục, nên `ingredientIdOfName` trả undefined và FEFO coi như
  // thiếu hàng vĩnh viễn — món chứa chúng không đặt mua và không truy được lô.
  { id: "ING-HANH-LA", name: "Hành lá", unit: "kg", allergens: [], refuseFactor: 0.15 },
  { id: "ING-HE", name: "Hẹ", unit: "kg", allergens: [], refuseFactor: 0.12 },
  { id: "ING-SUP-LO", name: "Súp lơ", unit: "kg", allergens: [], refuseFactor: 0.3 },
  { id: "ING-DUA", name: "Dứa", unit: "kg", allergens: [], refuseFactor: 0.45 },
  { id: "ING-ME", name: "Me", unit: "kg", allergens: [], refuseFactor: 0.3 },
  { id: "ING-XUONG-LON", name: "Xương lợn", unit: "kg", allergens: [], refuseFactor: 0.35 },
  { id: "ING-PHO-MAI", name: "Phô mai lát", unit: "kg", allergens: ["Sữa"] },
  { id: "ING-DAU-XANH", name: "Đậu xanh", unit: "kg", allergens: [] },
  { id: "ING-GAO-TAM", name: "Gạo tám thơm", unit: "kg", allergens: [] },
  { id: "ING-THIT-LON-VAI", name: "Thịt lợn nạc vai", unit: "kg", allergens: [], refuseFactor: 0.05 },
  { id: "ING-THIT-GA", name: "Thịt gà ta", unit: "kg", allergens: [], refuseFactor: 0.35 },
  { id: "ING-THIT-BO", name: "Thịt bò thăn", unit: "kg", allergens: [], refuseFactor: 0.05 },
  { id: "ING-CA-BASA", name: "Cá basa phi lê", unit: "kg", allergens: ["Cá"], refuseFactor: 0.0 },
  { id: "ING-CA-THU", name: "Cá thu", unit: "kg", allergens: ["Cá"], refuseFactor: 0.4 },
  { id: "ING-TOM-SU", name: "Tôm sú", unit: "kg", allergens: ["Tôm", "Hải sản"], refuseFactor: 0.45 },
  { id: "ING-CUA-DONG", name: "Cua đồng xay", unit: "kg", allergens: ["Cua", "Hải sản"], refuseFactor: 0.0 },
  { id: "ING-TRUNG-GA", name: "Trứng gà", unit: "quả", allergens: ["Trứng"], refuseFactor: 0.12 },
  { id: "ING-TRUNG-CUT", name: "Trứng cút", unit: "quả", allergens: ["Trứng"], refuseFactor: 0.18 },
  { id: "ING-SUA-TUOI", name: "Sữa tươi tiệt trùng", unit: "lít", allergens: ["Sữa bò"] },
  { id: "ING-SUA-CHUA", name: "Sữa chua", unit: "hộp", allergens: ["Sữa bò"] },
  { id: "ING-DAU-PHU", name: "Đậu phụ", unit: "kg", allergens: ["Đậu nành"] },
  { id: "ING-SUA-DAU-NANH", name: "Sữa đậu nành", unit: "lít", allergens: ["Đậu nành"] },
  { id: "ING-DAU-PHONG", name: "Đậu phộng (lạc)", unit: "kg", allergens: ["Đậu phộng"], refuseFactor: 0.3 },
  { id: "ING-BOT-MI", name: "Bột mì", unit: "kg", allergens: ["Lúa mì (Gluten)"] },
  { id: "ING-BANH-MI", name: "Bánh mì sandwich", unit: "ổ", allergens: ["Lúa mì (Gluten)"] },
  { id: "ING-VUNG", name: "Vừng (mè) trắng", unit: "kg", allergens: ["Mè (vừng)"] },
  { id: "ING-RAU-MUONG", name: "Rau muống", unit: "kg", allergens: [], refuseFactor: 0.32 },
  { id: "ING-RAU-CAI-NGOT", name: "Rau cải ngọt", unit: "kg", allergens: [], refuseFactor: 0.2 },
  { id: "ING-RAU-NGOT", name: "Rau ngót", unit: "kg", allergens: [], refuseFactor: 0.35 },
  { id: "ING-MONG-TOI", name: "Mồng tơi", unit: "kg", allergens: [], refuseFactor: 0.28 },
  { id: "ING-BI-DO", name: "Bí đỏ", unit: "kg", allergens: [], refuseFactor: 0.28 },
  { id: "ING-SU-SU", name: "Su su", unit: "kg", allergens: [], refuseFactor: 0.2 },
  { id: "ING-CA-ROT", name: "Cà rốt", unit: "kg", allergens: [], refuseFactor: 0.15 },
  { id: "ING-KHOAI-TAY", name: "Khoai tây", unit: "kg", allergens: [], refuseFactor: 0.2 },
  { id: "ING-CA-CHUA", name: "Cà chua", unit: "kg", allergens: [], refuseFactor: 0.05 },
  { id: "ING-BAP-CAI", name: "Bắp cải", unit: "kg", allergens: [], refuseFactor: 0.2 },
  { id: "ING-GIA-DO", name: "Giá đỗ", unit: "kg", allergens: ["Đậu nành"], refuseFactor: 0.05 },
  { id: "ING-HANH-KHO", name: "Hành khô", unit: "kg", allergens: [], refuseFactor: 0.12 },
  { id: "ING-GUNG", name: "Gừng", unit: "kg", allergens: [], refuseFactor: 0.2 },
  { id: "ING-TOI", name: "Tỏi", unit: "kg", allergens: [], refuseFactor: 0.12 },
  { id: "ING-NUOC-MAM", name: "Nước mắm truyền thống", unit: "lít", allergens: ["Cá"] },
  { id: "ING-DAU-AN", name: "Dầu ăn thực vật", unit: "lít", allergens: [] },
  { id: "ING-MUOI", name: "Muối i-ốt", unit: "kg", allergens: [] },
  { id: "ING-DUONG", name: "Đường trắng", unit: "kg", allergens: [] },
  { id: "ING-CHUOI", name: "Chuối tây", unit: "kg", allergens: [], refuseFactor: 0.35 },
  { id: "ING-DUA-HAU", name: "Dưa hấu", unit: "kg", allergens: [], refuseFactor: 0.4 },
];

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
export interface SupplierProduct {
  id: string;
  supplierId: string;
  /** Khoá về danh mục chuẩn của trường — một nguyên liệu, nhiều NCC cùng bán. */
  ingredientId: string;
  /** Tên NCC dùng trên hoá đơn/bao bì, có thể khác tên danh mục ("Tôm sú đông lạnh"). */
  label?: string;
  /**
   * Nhãn hiệu / thương hiệu sản phẩm — kế toán đặt mua SẢN PHẨM ("Ức gà CP 2kg"),
   * không đặt mua "Ingredient". Trường không cần biết brand, nhưng NCC cần ghi để
   * hoá đơn khớp với phiếu đặt hàng.
   */
  brand?: string;
  /** Đơn vị báo giá, khớp `CatalogIngredient.unit`. */
  unit: string;
  unitPrice: number;
  /** Quy cách đóng gói (thùng 10kg, khay 30 quả…) — dùng khi làm tròn đơn đặt hàng. */
  packaging?: string;
  /**
   * Số lượng đặt tối thiểu mỗi lần — đơn vị khớp với `unit`.
   * Trường cần 3kg gạo nhưng NCC bán tối thiểu 25kg (1 bao) → hệ thống làm tròn lên.
   * Nếu không khai thì dùng `packaging` để làm tròn; không có cả hai thì không làm tròn.
   */
  minimumOrderQty?: number;
  effectiveFrom: string;
}

export const SUPPLIER_PRODUCTS: SupplierProduct[] = [
  // GreenFarm — rau củ, gạo, dầu, gia vị khô
  { id: "SIP-001", supplierId: "SUP-001", ingredientId: "ING-GAO-TAM", brand: "Gạo Nàng Hương ST25", unit: "kg", unitPrice: 22000, packaging: "Bao 25kg", minimumOrderQty: 25, effectiveFrom: "01/07/2026" },
  { id: "SIP-002", supplierId: "SUP-001", ingredientId: "ING-BI-DO", unit: "kg", unitPrice: 15000, minimumOrderQty: 5, effectiveFrom: "01/07/2026" },
  { id: "SIP-003", supplierId: "SUP-001", ingredientId: "ING-SU-SU", unit: "kg", unitPrice: 14000, minimumOrderQty: 5, effectiveFrom: "01/07/2026" },
  { id: "SIP-004", supplierId: "SUP-001", ingredientId: "ING-CA-ROT", unit: "kg", unitPrice: 18000, minimumOrderQty: 5, effectiveFrom: "01/07/2026" },
  { id: "SIP-005", supplierId: "SUP-001", ingredientId: "ING-KHOAI-TAY", unit: "kg", unitPrice: 20000, minimumOrderQty: 5, effectiveFrom: "01/07/2026" },
  { id: "SIP-006", supplierId: "SUP-001", ingredientId: "ING-CA-CHUA", unit: "kg", unitPrice: 19000, minimumOrderQty: 3, effectiveFrom: "01/07/2026" },
  { id: "SIP-007", supplierId: "SUP-001", ingredientId: "ING-DAU-AN", brand: "Dầu ăn Neptune", unit: "lít", unitPrice: 48000, packaging: "Can 5 lít", minimumOrderQty: 5, effectiveFrom: "01/07/2026" },
  { id: "SIP-008", supplierId: "SUP-001", ingredientId: "ING-HANH-KHO", unit: "kg", unitPrice: 35000, effectiveFrom: "01/07/2026" },
  { id: "SIP-009", supplierId: "SUP-001", ingredientId: "ING-TOI", unit: "kg", unitPrice: 60000, effectiveFrom: "01/07/2026" },
  { id: "SIP-010", supplierId: "SUP-001", ingredientId: "ING-GUNG", unit: "kg", unitPrice: 40000, effectiveFrom: "01/07/2026" },
  { id: "SIP-011", supplierId: "SUP-001", ingredientId: "ING-DUONG", brand: "Đường Biên Hòa", unit: "kg", unitPrice: 24000, minimumOrderQty: 5, effectiveFrom: "01/07/2026" },
  { id: "SIP-012", supplierId: "SUP-001", ingredientId: "ING-NUOC-MAM", brand: "Nước mắm Phú Quốc Khải Hoàn", label: "Nước mắm 40°", unit: "lít", unitPrice: 55000, packaging: "Thùng 12 chai 1l", minimumOrderQty: 12, effectiveFrom: "01/07/2026" },
  { id: "SIP-013", supplierId: "SUP-001", ingredientId: "ING-CHUOI", unit: "kg", unitPrice: 25000, minimumOrderQty: 5, effectiveFrom: "01/07/2026" },

  // Vân Nội — rau ăn lá (giá rau rẻ hơn GreenFarm, đúng thế mạnh HTX)
  { id: "SIP-020", supplierId: "SUP-002", ingredientId: "ING-RAU-MUONG", unit: "kg", unitPrice: 12000, effectiveFrom: "01/07/2026" },
  { id: "SIP-021", supplierId: "SUP-002", ingredientId: "ING-RAU-CAI-NGOT", unit: "kg", unitPrice: 13000, effectiveFrom: "01/07/2026" },
  { id: "SIP-022", supplierId: "SUP-002", ingredientId: "ING-RAU-NGOT", unit: "kg", unitPrice: 16000, effectiveFrom: "01/07/2026" },
  { id: "SIP-023", supplierId: "SUP-002", ingredientId: "ING-MONG-TOI", unit: "kg", unitPrice: 13000, effectiveFrom: "01/07/2026" },
  { id: "SIP-024", supplierId: "SUP-002", ingredientId: "ING-GIA-DO", unit: "kg", unitPrice: 15000, effectiveFrom: "01/07/2026" },
  { id: "SIP-025", supplierId: "SUP-002", ingredientId: "ING-HANH-LA", unit: "kg", unitPrice: 30000, effectiveFrom: "01/07/2026" },
  { id: "SIP-026", supplierId: "SUP-002", ingredientId: "ING-SU-SU", unit: "kg", unitPrice: 13500, effectiveFrom: "01/07/2026" },
  { id: "SIP-027", supplierId: "SUP-002", ingredientId: "ING-BI-DO", unit: "kg", unitPrice: 14500, effectiveFrom: "01/07/2026" },

  // Minh Phát — thịt, trứng
  { id: "SIP-030", supplierId: "SUP-003", ingredientId: "ING-THIT-LON-VAI", unit: "kg", unitPrice: 145000, effectiveFrom: "01/07/2026" },
  { id: "SIP-031", supplierId: "SUP-003", ingredientId: "ING-THIT-GA", unit: "kg", unitPrice: 120000, effectiveFrom: "01/07/2026" },
  { id: "SIP-032", supplierId: "SUP-003", ingredientId: "ING-THIT-BO", unit: "kg", unitPrice: 280000, effectiveFrom: "01/07/2026" },
  { id: "SIP-033", supplierId: "SUP-003", ingredientId: "ING-TRUNG-CUT", unit: "quả", unitPrice: 900, packaging: "Khay 30 quả", effectiveFrom: "01/07/2026" },
  { id: "SIP-034", supplierId: "SUP-003", ingredientId: "ING-TRUNG-GA", unit: "quả", unitPrice: 3200, packaging: "Khay 30 quả", effectiveFrom: "01/07/2026" },
  { id: "SIP-035", supplierId: "SUP-003", ingredientId: "ING-XUONG-LON", unit: "kg", unitPrice: 45000, effectiveFrom: "01/07/2026" },

  // Đại Dương — hải sản (NCC đang bị đình chỉ, giữ báo giá để thấy hệ quả)
  { id: "SIP-040", supplierId: "SUP-004", ingredientId: "ING-TOM-SU", unit: "kg", unitPrice: 210000, effectiveFrom: "01/07/2026" },
  { id: "SIP-041", supplierId: "SUP-004", ingredientId: "ING-CA-BASA", unit: "kg", unitPrice: 95000, effectiveFrom: "01/07/2026" },
  { id: "SIP-042", supplierId: "SUP-004", ingredientId: "ING-CUA-DONG", unit: "kg", unitPrice: 110000, effectiveFrom: "01/07/2026" },

  // Thực phẩm chế biến — GreenFarm
  { id: "SIP-050", supplierId: "SUP-001", ingredientId: "ING-DAU-PHU", unit: "kg", unitPrice: 30000, effectiveFrom: "01/07/2026" },
  { id: "SIP-051", supplierId: "SUP-001", ingredientId: "ING-SUA-CHUA", unit: "hộp", unitPrice: 4200, packaging: "Lốc 4 hộp", effectiveFrom: "01/07/2026" },
  { id: "SIP-052", supplierId: "SUP-001", ingredientId: "ING-SUA-DAU-NANH", unit: "lít", unitPrice: 22000, effectiveFrom: "01/07/2026" },
  { id: "SIP-053", supplierId: "SUP-001", ingredientId: "ING-BANH-MI", unit: "ổ", unitPrice: 5000, effectiveFrom: "01/07/2026" },
  { id: "SIP-054", supplierId: "SUP-001", ingredientId: "ING-PHO-MAI", unit: "kg", unitPrice: 180000, effectiveFrom: "01/07/2026" },
  { id: "SIP-055", supplierId: "SUP-001", ingredientId: "ING-DUA-HAU", unit: "kg", unitPrice: 16000, effectiveFrom: "01/07/2026" },
  { id: "SIP-056", supplierId: "SUP-001", ingredientId: "ING-BOT-MI", unit: "kg", unitPrice: 28000, effectiveFrom: "01/07/2026" },
  { id: "SIP-057", supplierId: "SUP-001", ingredientId: "ING-DAU-XANH", unit: "kg", unitPrice: 42000, effectiveFrom: "01/07/2026" },
  { id: "SIP-058", supplierId: "SUP-001", ingredientId: "ING-SUA-TUOI", unit: "lít", unitPrice: 34000, effectiveFrom: "01/07/2026" },

  /*
   * Hải sản có HAI nguồn với giá khác nhau: Đại Dương rẻ hơn nhưng đang bị đình
   * chỉ, GreenFarm đắt hơn. Đây là chỗ thấy rõ hệ quả của quyết định đình chỉ
   * (BR-070): giá tốt nhất tự động nhảy sang nguồn còn hợp tác, chi phí bữa tăng
   * — thay vì nguyên liệu biến mất khỏi bảng giá.
   */
  { id: "SIP-060", supplierId: "SUP-001", ingredientId: "ING-TOM-SU", unit: "kg", unitPrice: 240000, effectiveFrom: "01/07/2026" },
  { id: "SIP-061", supplierId: "SUP-001", ingredientId: "ING-CA-BASA", unit: "kg", unitPrice: 105000, effectiveFrom: "01/07/2026" },

  // GreenFarm còn cung cấp các mặt hàng dưới đây (các lô SUP-001 trỏ về):
  { id: "SIP-070", supplierId: "SUP-001", ingredientId: "ING-RAU-CAI-NGOT", unit: "kg", unitPrice: 14500, effectiveFrom: "01/07/2026" },
  { id: "SIP-071", supplierId: "SUP-001", ingredientId: "ING-RAU-MUONG", unit: "kg", unitPrice: 13500, effectiveFrom: "01/07/2026" },
  { id: "SIP-072", supplierId: "SUP-001", ingredientId: "ING-THIT-LON-VAI", unit: "kg", unitPrice: 152000, effectiveFrom: "01/07/2026" },
  { id: "SIP-073", supplierId: "SUP-001", ingredientId: "ING-TRUNG-GA", unit: "quả", unitPrice: 3400, packaging: "Khay 30 quả", effectiveFrom: "01/07/2026" },
];

/** Sản phẩm NCC → nguyên liệu trong danh mục chuẩn của trường. */
export function ingredientIdOfProduct(productId: string): string | undefined {
  return SUPPLIER_PRODUCTS.find((p) => p.id === productId)?.ingredientId;
}

/** Nhãn hiển thị của một sản phẩm NCC. */
export function productLabel(productId: string): string {
  const prod = SUPPLIER_PRODUCTS.find((p) => p.id === productId);
  if (!prod) return productId;
  return prod.label ?? INGREDIENT_CATALOG.find((i) => i.id === prod.ingredientId)?.name ?? productId;
}

/**
 * Backfill nhãn lô từ sản phẩm — nhãn là DẪN XUẤT, không khai tay trong seed.
 * Đặt ngay sau khi cả hai bảng đã dựng xong.
 */
for (const b of SUPPLIER_BATCHES) b.productName ??= productLabel(b.productId);

/**
 * Giá tốt nhất đang có của một nguyên liệu — chỉ xét NCC còn hợp tác.
 * Nhà cung cấp bị đình chỉ (BR-070) thì báo giá của họ không dùng để ước tính nữa.
 */
export function bestPriceOf(
  ingredientId: string | undefined,
  prices: SupplierProduct[] = SUPPLIER_PRODUCTS,
  partners: Pick<Partner, "id" | "status">[] = PARTNERS
): SupplierProduct | undefined {
  if (!ingredientId) return undefined;
  const usable = prices.filter(
    (p) =>
      p.ingredientId === ingredientId &&
      partners.find((s) => s.id === p.supplierId)?.status === "APPROVED"
  );
  return usable.sort((a, b) => a.unitPrice - b.unitPrice)[0];
}

/**
 * Nhà cung cấp NGUỒN GỐC của một món — SUY từ nguyên liệu, không khai tay.
 *
 * 23/07/2026: bỏ trường `Dish.supplier` gõ tay. Nó tự nhận là BR-076 nhưng truy
 * xuất thật đi qua `ingredientId`, không qua chuỗi chữ đó; và một món có nhiều
 * nguyên liệu từ nhiều nhà cung cấp nên "một NCC chính" là hư cấu. Gõ "Đại Dương"
 * rồi mai đối tác đổi tên hay bị đình chỉ là chữ đó nói dối — đúng lỗi "link bằng
 * tên" mà cả hệ thống đang dọn.
 *
 * Nay hiển thị đâu cần nguồn gấc thì suy tại chỗ: mỗi nguyên liệu → nhà cung cấp
 * đang bán nó (còn hợp tác). Nguyên liệu ngoài danh mục thì không suy được, trả
 * về `undefined` để nơi gọi tự xử.
 */
export function dishSourcing(
  dish: Pick<Dish, "ingredients">
): { ingredient: string; supplier?: string }[] {
  return dish.ingredients.map((ing) => {
    const price = bestPriceOf(ing.ingredientId);
    const supplier = price
      ? PARTNERS.find((p) => p.id === price.supplierId)?.name
      : undefined;
    return { ingredient: ing.name, supplier };
  });
}

/** Suy khối từ tên lớp: "3A" → "Khối 3". */
export function gradeOfClass(className: string): string {
  const digit = className.match(/^\d/)?.[0];
  return digit ? `Khối ${digit}` : "Khối ?";
}

/**
 * Hồ sơ dị ứng theo khối (BR-063): allergen → số học sinh của khối bị dị ứng.
 * Mock đối chiếu từ STUDENTS; bản thật lấy từ AllergyProfile đã Y tế xác minh.
 */
export function gradeAllergySummary(
  grade: string,
  opts: { verifiedOnly?: boolean } = {}
): Record<string, number> {
  const summary: Record<string, number> = {};
  for (const s of STUDENTS) {
    if (gradeOfClass(s.class) !== grade) continue;
    for (const a of s.allergies) {
      if (opts.verifiedOnly && !isAllergenVerified(s.id, a)) continue;
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
export function gradeAllergyStudents(
  grade: string,
  allergen: string,
  opts: { verifiedOnly?: boolean } = {}
): Student[] {
  return STUDENTS.filter(
    (s) =>
      gradeOfClass(s.class) === grade &&
      s.allergies.some(
        (sa) =>
          allergenTouches(sa, allergen) &&
          (!opts.verifiedOnly || isAllergenVerified(s.id, sa))
      )
  );
}

/**
 * `Menu.allergyConflicts` là SỐ ĐẾM DẪN XUẤT — tính lại từ chính thực đơn.
 *
 * Seed vốn khai tay con số này và cả 4 thực đơn có xung đột đều sai: MENU-2026-27
 * ghi 0 trong khi còn 1 món vướng, MENU-2026-28 ghi 1 trong khi có 2, MENU-2026-25
 * ghi 3 trong khi còn 1. Ban giám hiệu duyệt dựa trên con số này nên nó sai là
 * duyệt sai. Backend không lưu cột này.
 */
for (const menu of MENUS) {
  let n = 0;
  for (const block of menu.blocksData ?? []) {
    for (const d of block.dishes) {
      const all = new Set<string>(d.manualAllergens);
      for (const ing of d.ingredients) {
        resolveIngredientAllergens(ing).allergens.forEach((a) => all.add(a));
      }
      const cham = [...all].filter(
        (a) => gradeAllergyStudents(menu.grade, a, { verifiedOnly: true }).length > 0
      );
      // Có suất riêng cho nhóm dị ứng thì coi như đã xử lý (BR-063/065).
      // Từ 22/07/2026 suất thay thế trong thực đơn CHỈ còn loại dị ứng.
      const coSuatRieng = (d.substitutes ?? []).length > 0;
      if (cham.length > 0 && !coSuatRieng) n++;
    }
  }
  menu.allergyConflicts = n;
}

/**
 * Nối nguyên liệu của THỰC ĐƠN và THƯ VIỆN MÓN về danh mục chuẩn.
 *
 * Đến 22/07/2026 toàn bộ 113 dòng nguyên liệu trong thực đơn KHÔNG có
 * `ingredientId`; chuỗi mua hàng chạy được chỉ vì `mealIngredientNeeds` lùi về so
 * TÊN CHUỖI tuyệt đối (`ing.ingredientId ?? ingredientIdOfName(ing.name)`). Sai một
 * dấu cách hay viết "Tôm sú đông lạnh" thay vì "Tôm sú" là nguyên liệu biến mất
 * khỏi phiếu xuất kho — im lặng, FEFO chỉ báo thiếu hàng.
 *
 * Nối khoá tại đây, và cảnh báo ngay lúc nạp module nếu còn tên không khớp danh
 * mục: dữ liệu seed sai thì phải kêu, không để lặng lẽ trôi vào phiếu xuất.
 */
const unmatchedIngredients = new Set<string>();
function linkIngredient(row: { name: string; ingredientId?: string }) {
  row.ingredientId ??= ingredientIdOfName(row.name);
  if (!row.ingredientId) unmatchedIngredients.add(row.name);
}
for (const menu of MENUS) {
  for (const block of menu.blocksData ?? []) {
    for (const d of block.dishes) {
      d.ingredients.forEach(linkIngredient);
      for (const sub of d.substitutes ?? []) sub.ingredients.forEach(linkIngredient);
    }
  }
}
for (const d of DISH_LIBRARY) d.ingredients.forEach(linkIngredient);
if (unmatchedIngredients.size > 0) {
  console.warn(
    `[mock-data] ${unmatchedIngredients.size} nguyên liệu không khớp danh mục — sẽ không đặt mua và không truy được lô: ${[...unmatchedIngredients].join(", ")}`
  );
}

/**
 * Đơn vị trong công thức phải QUY VỀ ĐƯỢC đơn vị của danh mục.
 *
 * "Chuối tây" từng khai `1 quả` trong công thức trong khi danh mục tính theo `kg`
 * và báo giá 25.000₫/kg — hệ thống nhân 1 × 25.000 và ra 25.000₫ tiền chuối cho
 * MỖI SUẤT, đẩy chi phí bữa từ ~17.000₫ lên 41.841₫. Lệch đơn vị không gây lỗi cú
 * pháp, chỉ âm thầm cho ra con số sai gấp mười lần.
 */
const unitMismatches = new Set<string>();
for (const c of INGREDIENT_CATALOG) {
  const rows: { unit: string }[] = [];
  for (const menu of MENUS)
    for (const block of menu.blocksData ?? [])
      for (const d of block.dishes) {
        rows.push(...d.ingredients.filter((i) => i.ingredientId === c.id));
        for (const sub of d.substitutes ?? [])
          rows.push(...sub.ingredients.filter((i) => i.ingredientId === c.id));
      }
  for (const d of DISH_LIBRARY) rows.push(...d.ingredients.filter((i) => i.ingredientId === c.id));
  // Tra bảng ngay tại chỗ thay vì gọi `toBaseQuantity`: hàm đó đọc `UNIT_FACTOR`
  // khai phía dưới, gọi ở đây là lỗi TDZ lúc nạp module.
  const baseOf = (u: string) => ({ g: "kg", kg: "kg", ml: "lít", "lít": "lít" })[u] ?? u;
  for (const r of rows) {
    if (baseOf(r.unit) !== c.unit) {
      unitMismatches.add(`${c.name} (công thức "${r.unit}" ≠ danh mục "${c.unit}")`);
    }
  }
}
if (unitMismatches.size > 0) {
  console.warn(`[mock-data] Lệch đơn vị — chi phí và lượng xuất kho sẽ sai: ${[...unitMismatches].join("; ")}`);
}

/**
 * Backfill `allergenIds` cho suất dị ứng của thực đơn seed.
 *
 * Suất dị ứng phải ghi rõ nó sinh ra vì tác nhân nào — nhãn "ALLERGY" trống rỗng
 * trước đây không trả lời được câu hỏi đó. Tính từ chính món gốc: tác nhân của
 * món CHẠM hồ sơ đã được Y tế xác minh của khối này.
 *
 * Đặt ở đây chứ không gộp vào vòng backfill `dishId` phía trên: hai hàm dưới đây
 * đọc `INGREDIENT_CATALOG` và `STUDENTS` — khai báo sau `MENUS` trong file này,
 * chạy sớm hơn là lỗi "Cannot access before initialization" lúc nạp module.
 */
for (const menu of MENUS) {
  for (const block of menu.blocksData ?? []) {
    for (const d of block.dishes) {
      for (const sub of d.substitutes ?? []) {
        if (sub.allergenIds != null) continue;
        const dishAllergens = new Set<string>(d.manualAllergens);
        for (const ing of d.ingredients) {
          resolveIngredientAllergens(ing).allergens.forEach((a) => dishAllergens.add(a));
        }
        sub.allergenIds = [...dishAllergens].filter(
          (a) => gradeAllergyStudents(menu.grade, a, { verifiedOnly: true }).length > 0
        );
      }
    }
  }
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
export interface DietGroup {
  id: string;
  label: string;
  /** Khớp Student.specialDiet (mock). */
  matchDiet?: string;
  excludeAllergens?: string[];
  excludeIngredientKeywords?: string[];
  /** Diễn giải cho người lập thực đơn — hiện ngay cạnh cảnh báo thiếu suất. */
  note?: string;
}

export const DIET_GROUPS: DietGroup[] = [
  {
    id: "VEGETARIAN",
    label: "Ăn chay (Vegetarian)",
    matchDiet: "Vegetarian",
    excludeIngredientKeywords: [
      "thịt", "gà", "bò", "lợn", "heo", "cá", "tôm", "cua", "mực", "ngao", "sò",
      "ốc", "hải sản", "xương", "mỡ động vật", "nước mắm",
    ],
    note: "Không đạm động vật; nước mắm thay bằng nước tương.",
  },
  {
    id: "DIABETIC",
    label: "Tiểu đường (Diabetic)",
    matchDiet: "Diabetic",
    excludeIngredientKeywords: ["đường", "sữa đặc", "mật ong", "bánh ngọt", "chè"],
    note: "Giảm tinh bột nhanh và đường tự do; ưu tiên rau và đạm.",
  },
  {
    id: "HALAL",
    label: "Halal",
    matchDiet: "Halal",
    excludeIngredientKeywords: ["lợn", "heo", "giăm bông", "xúc xích", "mỡ lợn", "rượu"],
    note: "Không thịt lợn; thịt khác phải có chứng nhận giết mổ Halal.",
  },
  {
    id: "NUT_FREE",
    label: "Không hạt (Nut-Free)",
    matchDiet: "Nut-Free",
    excludeAllergens: ["Đậu phộng", "Mè (vừng)"],
    note: "Tránh lạc/đậu phộng, vừng và dầu ép từ chúng.",
  },
  {
    id: "LACTOSE_FREE",
    label: "Không lactose (Lactose-Free)",
    matchDiet: "Lactose-Free",
    excludeAllergens: ["Sữa bò"],
    note: "Tránh sữa bò và chế phẩm (sữa chua, phô mai, bơ).",
  },
  {
    id: "GLUTEN_FREE",
    label: "Không gluten (Gluten-Free)",
    matchDiet: "Gluten-Free",
    excludeAllergens: ["Lúa mì (Gluten)"],
    note: "Tránh lúa mì, bánh mì, mì sợi; nước tương phải loại không gluten.",
  },
];

/** Học sinh thuộc 1 nhóm chế độ ăn (đã Y tế xác minh mới đủ điều kiện — BR-065). */
export function dietGroupMembers(groupId: string): Student[] {
  const group = DIET_GROUPS.find((g) => g.id === groupId);
  if (!group?.matchDiet) return [];
  return STUDENTS.filter((s) => s.specialDiet === group.matchDiet);
}

/**
 * Thành viên nhóm chế độ ăn GIỚI HẠN trong 1 khối — dùng khi kế hoạch bữa ăn
 * đặc biệt neo vào thực đơn theo khối (hướng A, 07/07/2026): nhóm trải nhiều
 * khối thì mỗi khối lập kế hoạch riêng theo thực đơn của khối đó.
 */
export function dietGroupMembersInGrade(groupId: string, grade: string): Student[] {
  return dietGroupMembers(groupId).filter((s) => gradeOfClass(s.class) === grade);
}

/** Nhóm chế độ ăn CÓ HỌC SINH trong khối — thứ một thực đơn của khối phải phủ. */
export function dietGroupsNeededByGrade(
  grade: string,
  students: Student[] = STUDENTS
): DietGroup[] {
  return DIET_GROUPS.filter((g) =>
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
  students: Student[] = STUDENTS
): DietGap[] {
  const needed = dietGroupsNeededByGrade(grade, students);
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
  menus: SeedMenu[] = MENUS,
  variants: MenuVariant[] = specialMenuMeals(menus)
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
      const group = DIET_GROUPS.find((g) => g.id === v.dietGroupId);
      const dishes = v.dishes
        .map((x) => DISH_LIBRARY.find((y) => y.id === x.dishId)?.name ?? x.dishId)
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

/** Tra nguyên liệu catalog theo id. */
export function ingredientById(id: string): CatalogIngredient | undefined {
  return INGREDIENT_CATALOG.find((i) => i.id === id);
}

/**
 * Tra id nguyên liệu từ tên hiển thị — dùng cho thành phần món trong thực đơn
 * (`SeedIngredientRow.name` vốn chọn từ chính catalog nên khớp tuyệt đối).
 * Trả `undefined` nếu tên không có trong danh mục: màn xuất kho phải cảnh báo
 * thay vì im lặng bỏ qua, vì nguyên liệu không định danh được = không truy xuất
 * được (BR-076).
 */
export function ingredientIdOfName(name: string): string | undefined {
  return INGREDIENT_CATALOG.find((i) => i.name === name)?.id;
}

/**
 * Lượng THỰC MUA từ lượng ĂN ĐƯỢC, theo hệ số thải bỏ của nguyên liệu.
 *
 * Công thức món ghi phần ăn được; bếp phải xuất nhiều hơn vì còn gọt vỏ, nhặt rau,
 * bỏ đầu vỏ tôm. Trước 22/07/2026 hai lượng này là MỘT, nên phiếu xuất kho xuất
 * đúng bằng lượng ăn được và bếp nấu xong sẽ thiếu — 45% với tôm sú, 35% với thịt gà.
 */
export function toPurchaseQuantity(
  edible: number,
  ingredientId: string | undefined,
  baseUnit: string
): number {
  // Chỉ áp cho khối lượng/thể tích. Đơn vị ĐẾM thì công thức đã ghi đúng thứ phải
  // mua: "3 quả trứng cút" là mua 3 quả cả vỏ, không phải 3.66 quả vì vỏ chiếm 18%.
  if (baseUnit !== "kg" && baseUnit !== "lít") return Number(edible.toFixed(3));
  const rf = INGREDIENT_CATALOG.find((i) => i.id === ingredientId)?.refuseFactor ?? 0;
  if (rf <= 0 || rf >= 1) return Number(edible.toFixed(3));
  return Number((edible / (1 - rf)).toFixed(3));
}

/** Quy đổi định lượng BOM (g/ml mỗi suất) về đơn vị gốc của kho (kg/lít). */
const UNIT_FACTOR: Record<string, { base: string; factor: number }> = {
  g: { base: "kg", factor: 0.001 },
  kg: { base: "kg", factor: 1 },
  ml: { base: "lít", factor: 0.001 },
  "lít": { base: "lít", factor: 1 },
};

/** Quy đổi 1 dòng BOM; đơn vị đếm (quả/hộp/ổ) giữ nguyên 1:1. */
export function toBaseQuantity(quantity: number, unit: string): { qty: number; unit: string } {
  const conv = UNIT_FACTOR[unit];
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

/** Tồn còn lại của lô = lượng nhập ban đầu trừ mọi giao dịch xuất/điều chỉnh đã ghi. */
export function batchRemaining(
  batchId: string,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): number {
  const batch = batches.find((b) => b.id === batchId);
  if (!batch) return 0;
  const consumed = transactions
    .filter((t) => t.batchId === batchId && t.quantity.trim().startsWith("-"))
    .reduce((sum, t) => sum + parseQuantity(t.quantity), 0);
  return Math.max(0, parseQuantity(batch.quantity) - consumed);
}

export interface FefoAllocation {
  batch: InventoryBatch;
  /** Lượng phân bổ cho lô này, theo đơn vị gốc của nguyên liệu. */
  take: number;
  /** Tồn khả dụng của lô tại thời điểm tính. */
  remaining: number;
}

/**
 * Phân bổ nhu cầu vào các lô theo FEFO — hạn dùng gần nhất xuất trước (BR-075).
 * Lô khóa (BR-205/BR-149) bị loại khỏi danh sách chọn chứ không chỉ ẩn đi.
 */
export function allocateFEFO(
  ingredientId: string,
  needed: number,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): { lines: FefoAllocation[]; shortfall: number } {
  const candidates = batches.filter(
    (b) => b.ingredientId === ingredientId && !isBatchLocked(b)
  ).sort((a, b) => parseVnDate(a.expiryDate) - parseVnDate(b.expiryDate));

  let left = needed;
  const lines: FefoAllocation[] = [];
  for (const batch of candidates) {
    if (left <= 0) break;
    const remaining = batchRemaining(batch.id, transactions, batches);
    if (remaining <= 0) continue;
    const take = Math.min(remaining, left);
    left -= take;
    lines.push({ batch, take: Number(take.toFixed(3)), remaining });
  }
  return { lines, shortfall: Number(Math.max(0, left).toFixed(3)) };
}

export interface DishRequirement {
  dishName: string;
  ingredientId?: string;
  ingredientName: string;
  /** Định lượng mỗi suất, giữ nguyên đơn vị BOM để đối chiếu bằng mắt. */
  perServing: string;
  /** Tổng nhu cầu, đã quy đổi về đơn vị gốc. */
  needed: number;
  unit: string;
  allocation: FefoAllocation[];
  shortfall: number;
  /** Nguyên liệu không có trong catalog → không truy xuất được (BR-076). */
  unknownIngredient: boolean;
}

/**
 * Nổ định mức một buổi ăn của thực đơn thành nhu cầu từng nguyên liệu, đã phân
 * bổ FEFO. Đây là nguồn dữ liệu của màn Xuất kho: người dùng chỉ chọn thực đơn
 * và buổi, toàn bộ định lượng lẫn lô đều do hệ thống tính.
 *
 * Món thay thế (suất đặc biệt) nhân theo `substitute.portions` chứ không theo
 * tổng số suất — BR-082 buộc chế biến riêng nên nguyên liệu cũng xuất riêng.
 */
export function mealRequirements(
  menu: (typeof MENUS)[number],
  mealType: MealType,
  servings: number,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): DishRequirement[] {
  const block = menu.blocksData?.find((b) => b.mealType === mealType);
  if (!block) return [];

  const rows: DishRequirement[] = [];
  const build = (
    dishName: string,
    ing: { name: string; quantity: string; unit: string; ingredientId?: string },
    portions: number
  ) => {
    // Khoá join đã chốt lúc chọn từ danh mục thì dùng thẳng — tra lại theo tên sẽ
    // trượt với nguyên liệu gõ thêm hậu tố ("Tôm sú đông lạnh" ≠ "Tôm sú", BR-076).
    const ingredientId = ing.ingredientId ?? ingredientIdOfName(ing.name);
    const base = toBaseQuantity(Number(ing.quantity) || 0, ing.unit);
    const needed = Number((base.qty * portions).toFixed(3));
    const { lines, shortfall } = ingredientId
      ? allocateFEFO(ingredientId, needed, transactions, batches)
      : { lines: [] as FefoAllocation[], shortfall: needed };
    rows.push({
      dishName,
      ingredientId,
      ingredientName: ing.name,
      perServing: `${ing.quantity} ${ing.unit}`,
      needed,
      unit: base.unit,
      allocation: lines,
      shortfall,
      unknownIngredient: !ingredientId,
    });
  };

  for (const d of block.dishes) {
    for (const ing of d.ingredients) build(d.name, ing, servings);
    // Nổ định mức cho MỌI suất riêng của món (dị ứng + từng nhóm chế độ ăn).
    // Trước đây chỉ tính 1 suất nên bếp bị thiếu nguyên liệu cho các nhóm còn lại.
    for (const sub of seedSubsOf(d)) {
      const portions = Number(sub.portions) || 0;
      for (const ing of sub.ingredients) {
        build(`${sub.name} (suất thay thế)`, ing, portions);
      }
    }
  }
  return rows;
}

/* ===== Nối nhu cầu nguyên liệu → ĐẶT HÀNG (Đợt E, 21/07/2026) =====
 *
 * `mealRequirements()` vốn tính ra chính xác phần thiếu, nhưng chỉ màn Xuất kho
 * dùng và cũng chỉ hiện chữ "hãy tạo Purchase Request" — người dùng phải sang
 * màn đặt hàng gõ lại từ đầu. Ba hàm dưới đây biến kết quả đó thành DÒNG ĐƠN
 * ĐẶT HÀNG dùng chung cho: nút tạo đơn ở màn Xuất kho, prefill màn Đặt hàng, và
 * cảnh báo sớm trên dashboard.
 */

/** 1 dòng thiếu hàng đã GỘP theo nguyên liệu (nhiều món có thể cùng thiếu 1 thứ). */
export interface ShortageLine {
  ingredientId?: string;
  /** Tên catalog nếu định danh được, không thì tên gõ trong thực đơn. */
  ingredientName: string;
  /** Lượng thiếu, đơn vị gốc của kho (kg/lít/quả…). */
  quantity: number;
  unit: string;
  /** Các món kéo theo nhu cầu này — để giải thích "đặt vì món nào". */
  dishes: string[];
  /** NCC đã giao nguyên liệu này gần nhất — gợi ý, không tự chọn thay người dùng. */
  suggestedSupplier?: string;
}

/** NCC của lô gần nhất theo `ingredientId` — cơ sở gợi ý NCC khi đặt bổ sung. */
export function lastSupplierOfIngredient(ingredientId?: string): string | undefined {
  if (!ingredientId) return undefined;
  return INVENTORY.filter((b) => b.ingredientId === ingredientId)
    .sort((a, b) => parseVnDate(b.receivedDate) - parseVnDate(a.receivedDate))[0]?.supplier;
}

/** Phần thiếu của 1 buổi ăn, gộp theo nguyên liệu và kèm gợi ý NCC. */
export function mealShortages(
  menu: (typeof MENUS)[number],
  mealType: MealType,
  servings: number,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): ShortageLine[] {
  const byIngredient = new Map<string, ShortageLine>();
  for (const r of mealRequirements(menu, mealType, servings, transactions, batches)) {
    if (r.shortfall <= 0) continue;
    // Nguyên liệu ngoài catalog vẫn phải hiện: đặt hàng được (gõ tay) nhưng
    // không truy xuất được lô — người dùng cần thấy để bổ sung danh mục.
    const key = r.ingredientId ?? `name:${r.ingredientName}`;
    const line = byIngredient.get(key) ?? {
      ingredientId: r.ingredientId,
      ingredientName:
        (r.ingredientId ? ingredientById(r.ingredientId)?.name : undefined) ??
        r.ingredientName,
      quantity: 0,
      unit: r.unit,
      dishes: [],
      suggestedSupplier: lastSupplierOfIngredient(r.ingredientId),
    };
    line.quantity = Number((line.quantity + r.shortfall).toFixed(3));
    if (!line.dishes.includes(r.dishName)) line.dishes.push(r.dishName);
    byIngredient.set(key, line);
  }
  return [...byIngredient.values()];
}

/** Thiếu hàng của 1 thực đơn sắp tới, gộp mọi buổi ăn trong ngày. */
export interface UpcomingShortage {
  menu: (typeof MENUS)[number];
  lines: ShortageLine[];
}

/**
 * Cảnh báo sớm: quét thực đơn ĐÃ DUYỆT của `days` ngày tới xem thiếu gì.
 *
 * Trước đây thiếu hàng chỉ lộ ra lúc bấm Xuất kho — tức đúng lúc chuẩn bị nấu,
 * đã muộn để đặt hàng (BR-071 buộc NCC có ≥24h). BR-061 bắt lập thực đơn trước
 * ≥3 ngày nên dữ liệu để cảnh báo vốn đã có sẵn.
 */
export function upcomingShortages(
  days = 3,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS
): UpcomingShortage[] {
  const today = new Date();
  today.setHours(0, 0, 0, 0);
  const limit = new Date(today);
  limit.setDate(limit.getDate() + days);

  return issuableMenus()
    .filter((m) => {
      if (!m.menuDateIso) return false;
      const d = new Date(`${m.menuDateIso}T00:00:00`);
      return d >= today && d <= limit;
    })
    .sort((a, b) => (a.menuDateIso ?? "").localeCompare(b.menuDateIso ?? ""))
    .map((menu) => {
      const servings = menu.servings ?? 0;
      const lines = (menu.blocksData ?? []).flatMap((b) =>
        // Buổi đã xuất kho rồi thì "thiếu" là hệ quả của việc đã trừ tồn, không
        // phải nhu cầu mua thêm — bỏ qua để cảnh báo không kêu oan.
        issuedLinesForMeal(menu.id, b.mealType, transactions).length > 0
          ? []
          : mealShortages(menu, b.mealType, servings, transactions)
      );
      return { menu, lines };
    })
    .filter((x) => x.lines.length > 0);
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
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): { fresh: Step1FreshRow[]; dry: Step1DryRow[] } {
  const fresh: Step1FreshRow[] = [];
  const dry: Step1DryRow[] = [];

  const issuedBatchIds = new Set(
    transactions
      .filter(
        (t) =>
          t.type === "Xuất kho (FEFO)" &&
          t.serviceDate === dateVn &&
          (!mealType || t.mealType === mealType)
      )
      .map((t) => t.batchId)
  );

  for (const b of batches.filter((x) => issuedBatchIds.has(x.id))) {
    const partner = PARTNERS.find(
      (p) => p.name.includes(b.supplier) || b.supplier.includes(p.name)
    );
    const supplierBatch = SUPPLIER_BATCHES.find((sb) => sb.id === b.id);
    const delivery = DELIVERIES.find((d) => (d.linkedBatches ?? []).includes(b.id));
    const certs = supplierBatch?.certificates ?? [];
    // Cột (9) giấy VS thú y và (10) giấy kiểm dịch suy từ chứng từ NCC đã nộp
    const docNames = certs
      .map((c) => {
        const doc = SUPPLIER_DOCUMENTS.find((d) => d.id === c);
        return doc ? `${doc.documentType} ${doc.name}` : "";
      })
      .join(" ");
    const contact = partner ? `${partner.address} — ${partner.contact}` : "";

    if (b.storageZone === "Dry") {
      dry.push({
        name: b.ingredient,
        producer: partner?.name ?? b.supplier,
        producerAddress: partner?.address ?? "",
        receivedAt: b.receivedDate,
        quantity: b.quantity,
        supplierName: partner?.name ?? b.supplier,
        deliverer: delivery?.driver ?? "",
        supplierContact: contact,
        expiry: b.expiryDate,
        storage: "Thường",
        hasInvoice: certs.length > 0,
        // Cảm quan là đánh giá TẠI CHỖ — để Đạt, người kiểm tự hạ khi thấy sai
        sensoryPass: true,
        action: "",
      });
    } else {
      fresh.push({
        name: b.ingredient,
        receivedAt: b.receivedDate,
        quantity: b.quantity,
        supplierName: partner?.name ?? b.supplier,
        supplierContact: contact,
        deliverer: delivery?.driver ?? "",
        hasInvoice: certs.length > 0,
        hasVetCert: /thú y|veterinary/i.test(docNames),
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
  menus: (typeof MENUS)[number][],
  mealType: MealType,
  servingsOf: (menu: (typeof MENUS)[number]) => number
): Step2Row[] {
  const byDish = new Map<
    string,
    { servings: number; ing: Map<string, { qty: number; unit: string }> }
  >();

  for (const menu of menus) {
    const block = menu.blocksData?.find((b) => b.mealType === mealType);
    if (!block) continue;
    const servings = servingsOf(menu);
    for (const d of block.dishes) {
      const entry = byDish.get(d.name) ?? { servings: 0, ing: new Map() };
      entry.servings += servings;
      for (const ing of d.ingredients) {
        const base = toBaseQuantity(Number(ing.quantity) || 0, ing.unit);
        const cur = entry.ing.get(ing.name) ?? { qty: 0, unit: base.unit };
        cur.qty = Number((cur.qty + base.qty * servings).toFixed(2));
        entry.ing.set(ing.name, cur);
      }
      byDish.set(d.name, entry);
    }
  }

  return [...byDish.entries()].map(([dishName, v]) => ({
    shift: MEAL_TYPE_LABEL[mealType],
    dishName,
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
  menus: (typeof MENUS)[number][],
  mealType: MealType,
  servingsOf: (menu: (typeof MENUS)[number]) => number
): Step3Row[] {
  return step2RowsOfMeal(menus, mealType, servingsOf).map((r) => ({
    shift: r.shift,
    dishName: r.dishName,
    servings: r.servings,
    dividedDoneAt: "",
    eatStartAt: "",
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
  menus: (typeof MENUS)[number][] = MENUS
): (typeof MENUS)[number][] {
  const iso = isoOfVnDate(session.date);
  const mealType = mealTypeOfLabel(session.meal);
  if (!iso || !mealType) return [];
  return menus.filter(
    (m) => m.menuDateIso === iso && (m.blocksData ?? []).some((b) => b.mealType === mealType)
  );
}

/**
 * Danh sách món (kèm số suất) mà một phiên kiểm thực đã kiểm — dùng để điền
 * dropdown "Tên mẫu thức ăn" ở màn Đăng ký lưu mẫu (MF08, nhận xét Minh
 * 27/07/2026): mẫu chỉ nên lưu đúng món đã qua kiểm thực, không gõ tay tự do.
 * Ưu tiên Bước 3 (đã chia suất — sát thời điểm lấy mẫu, Điều 7.1), rồi Bước 2,
 * rồi thực đơn của bữa; phiên seed cũ chỉ có `dish` đơn thì trả đúng một món đó.
 */
export function sampleDishOptionsOfInspection(
  session: Pick<InspectionSession, "date" | "meal" | "dish" | "step2Rows" | "step3Rows">,
  menus: (typeof MENUS)[number][] = MENUS
): { name: string; servings: string }[] {
  if (session.step3Rows?.length) {
    return session.step3Rows
      .filter((r) => r.dishName.trim())
      .map((r) => ({ name: r.dishName, servings: r.servings }));
  }
  if (session.step2Rows?.length) {
    return session.step2Rows
      .filter((r) => r.dishName.trim())
      .map((r) => ({ name: r.dishName, servings: r.servings }));
  }
  const mealType = mealTypeOfLabel(session.meal);
  if (mealType) {
    const scopeMenus = menusOfInspection(session, menus);
    const rows = step3RowsOfMeal(scopeMenus, mealType, (mn) => mn.servings ?? 0);
    if (rows.length) return rows.map((r) => ({ name: r.dishName, servings: r.servings }));
  }
  return session.dish ? [{ name: session.dish, servings: "" }] : [];
}

/** Phiên kiểm thực của một bữa cụ thể (chiều ngược lại). */
export function inspectionForMeal(
  dateIso: string,
  mealType: MealType,
  sessions: InspectionSession[] = INSPECTIONS
): InspectionSession | undefined {
  return sessions.find(
    (s) => isoOfVnDate(s.date) === dateIso && mealTypeOfLabel(s.meal) === mealType
  );
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
  sessions: InspectionSession[] = INSPECTIONS
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
 * Chi phí nguyên liệu của MỘT SUẤT một món.
 *
 * Dùng để so món gốc với suất thay thế: suất riêng cho nhóm dị ứng/chế độ ăn có
 * thể đắt hơn đáng kể (thay thịt lợn bằng thịt gà Halal, thay tôm bằng thịt băm),
 * và người lập thực đơn nên biết TRƯỚC khi nộp chứ không phải sau khi bếp đi chợ.
 */
export function dishCostPerServing(
  dish: { ingredients: { name: string; quantity: string; unit: string; ingredientId?: string }[] },
  prices: SupplierProduct[] = SUPPLIER_PRODUCTS,
  partners: Pick<Partner, "id" | "status">[] = PARTNERS
): { cost: number; missingPrices: string[] } {
  let cost = 0;
  const missing: string[] = [];
  for (const ing of dish.ingredients) {
    const id = ing.ingredientId ?? ingredientIdOfName(ing.name);
    const base = toBaseQuantity(Number(ing.quantity) || 0, ing.unit);
    const price = bestPriceOf(id, prices, partners);
    if (!price) {
      if (ing.name.trim() && !missing.includes(ing.name)) missing.push(ing.name);
      continue;
    }
    cost += toPurchaseQuantity(base.qty, id, base.unit) * price.unitPrice;
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
  substitute: { ingredients: { name: string; quantity: string; unit: string; ingredientId?: string }[]; portions?: string }
): { originalCost: number; substituteCost: number; delta: number; portions: number; extraTotal: number; comparable: boolean } {
  const a = dishCostPerServing(original);
  const b = dishCostPerServing(substitute);
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
  menu: (typeof MENUS)[number],
  mealType: MealType,
  servings: number,
  prices: SupplierProduct[] = SUPPLIER_PRODUCTS,
  partners: Pick<Partner, "id" | "status">[] = PARTNERS
): { perServing: number; total: number; missingPrices: string[] } {
  if (servings <= 0) return { perServing: 0, total: 0, missingPrices: [] };
  let total = 0;
  const missing: string[] = [];
  for (const r of mealIngredientNeeds(menu, mealType, servings)) {
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
  menu: (typeof MENUS)[number],
  servings: number
): { perServing: number; missingPrices: string[] } {
  const blocks = menu.blocksData ?? [];
  let per = 0;
  const missing: string[] = [];
  for (const b of blocks) {
    const r = estimateMealCostPerServing(menu, b.mealType, servings);
    per += r.perServing;
    for (const nm of r.missingPrices) if (!missing.includes(nm)) missing.push(nm);
  }
  return { perServing: per, missingPrices: missing };
}

/**
 * Đồng bộ `Menu.estimatedCost` của seed
 *
 * ĐẶT Ở ĐÂY, không đặt cùng chỗ với các backfill khác phía trên: vòng này gọi
 * `mealIngredientNeeds` → `toBaseQuantity` → `UNIT_FACTOR`, mà `UNIT_FACTOR` khai
 * bên dưới. Chạy sớm hơn là lỗi "Cannot access before initialization" lúc nạp
 * module — `tsc` không bắt được, chỉ chết khi chạy thật. với chi phí tính từ công thức × báo giá.
 *
 * Seed vốn khai tay con số này (26.400–31.500₫) trong khi `menu-form` ghi 0 cho mọi
 * thực đơn tạo mới — nhìn seed thì thấy hợp lý, dùng thật thì luôn 0₫/30.000₫ và
 * luôn đạt trần. Nay seed lấy đúng con số hệ thống tính ra, để bảng duyệt của Ban
 * giám hiệu không có hai loại thực đơn nói hai kiểu.
 *
 * Lưu ý mô hình: `estimatedCost` VẪN LÀ CỘT LƯU, không dẫn xuất lúc đọc — giá
 * nguyên liệu đổi theo thời gian, thực đơn đã duyệt phải giữ chi phí tại thời điểm
 * duyệt (cùng nguyên tắc với `costCeiling` và `servedDishes`).
 */
for (const menu of MENUS) {
  const servings = menu.servings ?? 0;
  if (servings <= 0) continue;
  menu.estimatedCost = estimateMenuCostPerServing(menu, servings).perServing;
}


/* ===== Xuất kho GỘP THEO BỮA (22/07/2026) =====
 *
 * Thực đơn lập theo NGÀY × KHỐI, nhưng bếp nấu theo BỮA: cùng "Cơm trắng" thì
 * Khối 1 và Khối 3 ăn chung một nồi. Xuất kho từng thực đơn khối một như trước
 * gây hai chuyện:
 *   1. FEFO chạy độc lập cho mỗi khối ⇒ cùng một nồi có thể bị ghi nhận lấy từ
 *      hai lô khác nhau. Khi lô A nhiễm, bản đồ truy xuất chỉ ra đúng khối lấy lô
 *      A, trong khi thực tế mọi khối ăn chung nồi đều bị ảnh hưởng.
 *   2. Thủ kho phải thao tác N lần cho một bữa.
 *
 * Cách làm: cộng nhu cầu theo NGUYÊN LIỆU trên toàn bộ thực đơn của bữa → chạy
 * FEFO MỘT lần → chia ngược lượng đã phân bổ về từng thực đơn khối theo đúng thứ
 * tự FEFO, để mỗi dòng phiếu xuất vẫn neo `menuId` (BR-076 giữ nguyên độ mịn).
 */

/** Nhu cầu 1 nguyên liệu của 1 thực đơn khối, trước khi phân bổ lô. */
interface MenuIngredientNeed {
  menuId: string;
  grade: string;
  dishNames: string[];
  needed: number;
}

/** 1 dòng kế hoạch xuất kho của cả bữa, đã gộp mọi khối. */
export interface MealIssueLine {
  ingredientId?: string;
  ingredientName: string;
  unit: string;
  perServing: string;
  /** Tổng nhu cầu của TOÀN BỘ các khối ăn bữa này. */
  totalNeeded: number;
  /** Kết quả FEFO chạy MỘT lần cho tổng nhu cầu. */
  allocation: FefoAllocation[];
  shortfall: number;
  unknownIngredient: boolean;
  /** Chia ngược về từng thực đơn khối — giữ neo menuId cho phiếu xuất. */
  perMenu: (MenuIngredientNeed & { allocation: FefoAllocation[] })[];
}

/**
 * Nhu cầu nguyên liệu của 1 buổi ăn thuộc 1 thực đơn — KHÔNG phân bổ lô.
 * Tách khỏi `mealRequirements` để phần gộp theo bữa dùng lại được.
 */
function mealIngredientNeeds(
  menu: (typeof MENUS)[number],
  mealType: MealType,
  servings: number
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
    const ingredientId = ing.ingredientId ?? ingredientIdOfName(ing.name);
    const edible = Number((base.qty * portions).toFixed(3));
    rows.push({
      dishName,
      ingredientId,
      ingredientName: ing.name,
      perServing: `${ing.quantity} ${ing.unit}`,
      edible,
      // Xuất kho tính THẲNG theo định lượng công thức × số suất, KHÔNG cộng hệ số
      // thải bỏ (yêu cầu 22/07/2026 — Minh): phiếu xuất khớp đúng định lượng đã khai
      // trong thực đơn. `needed` vì thế bằng luôn `edible`.
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
 * Kế hoạch xuất kho cho MỘT BỮA của cả trường.
 *
 * @param menus  mọi thực đơn (khối) phục vụ bữa đó trong ngày
 * @param servingsOf  số suất thực tế theo từng thực đơn
 */
export function mealIssuePlan(
  menus: (typeof MENUS)[number][],
  mealType: MealType,
  servingsOf: (menu: (typeof MENUS)[number]) => number,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): MealIssueLine[] {
  // Gộp theo KHOÁ nguyên liệu; thứ ngoài danh mục gộp theo tên để vẫn hiện ra
  const byIngredient = new Map<
    string,
    { ingredientId?: string; ingredientName: string; unit: string; perServing: string; needs: MenuIngredientNeed[] }
  >();

  for (const menu of menus) {
    for (const r of mealIngredientNeeds(menu, mealType, servingsOf(menu))) {
      const key = r.ingredientId ?? `name:${r.ingredientName}`;
      const entry =
        byIngredient.get(key) ??
        { ingredientId: r.ingredientId, ingredientName: r.ingredientName, unit: r.unit, perServing: r.perServing, needs: [] };
      let need = entry.needs.find((n) => n.menuId === menu.id);
      if (!need) {
        need = { menuId: menu.id, grade: menu.grade, dishNames: [], needed: 0 };
        entry.needs.push(need);
      }
      need.needed = Number((need.needed + r.needed).toFixed(3));
      if (!need.dishNames.includes(r.dishName)) need.dishNames.push(r.dishName);
      byIngredient.set(key, entry);
    }
  }

  return [...byIngredient.values()].map((entry) => {
    const totalNeeded = Number(entry.needs.reduce((s, n) => s + n.needed, 0).toFixed(3));
    const { lines, shortfall } = entry.ingredientId
      ? allocateFEFO(entry.ingredientId, totalNeeded, transactions, batches)
      : { lines: [] as FefoAllocation[], shortfall: totalNeeded };

    // Chia ngược theo ĐÚNG thứ tự FEFO: khối nào cũng lấy từ lô hạn gần nhất
    // trước, nên nếu phải cắt sang lô thứ hai thì cắt ở đúng một chỗ.
    const cursor = lines.map((l) => ({ line: l, left: l.take }));
    const perMenu = entry.needs.map((n) => {
      let remain = n.needed;
      const taken: FefoAllocation[] = [];
      for (const c of cursor) {
        if (remain <= 0) break;
        if (c.left <= 0) continue;
        const take = Math.min(c.left, remain);
        c.left = Number((c.left - take).toFixed(3));
        remain = Number((remain - take).toFixed(3));
        taken.push({ ...c.line, take: Number(take.toFixed(3)) });
      }
      return { ...n, allocation: taken };
    });

    return {
      ingredientId: entry.ingredientId,
      ingredientName: entry.ingredientName,
      unit: entry.unit,
      perServing: entry.perServing,
      totalNeeded,
      allocation: lines,
      shortfall,
      unknownIngredient: !entry.ingredientId,
      perMenu,
    };
  });
}

/** Phần thiếu của CẢ BỮA (mọi khối), gộp theo nguyên liệu — nguồn của đơn đặt hàng. */
export function mealIssueShortages(
  menus: (typeof MENUS)[number][],
  mealType: MealType,
  servingsOf: (menu: (typeof MENUS)[number]) => number,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  batches: InventoryBatch[] = INVENTORY
): ShortageLine[] {
  return mealIssuePlan(menus, mealType, servingsOf, transactions, batches)
    .filter((l) => l.shortfall > 0)
    .map((l) => ({
      ingredientId: l.ingredientId,
      ingredientName: l.ingredientId
        ? ingredientById(l.ingredientId)?.name ?? l.ingredientName
        : l.ingredientName,
      quantity: l.shortfall,
      unit: l.unit,
      dishes: [...new Set(l.perMenu.flatMap((p) => p.dishNames))],
      suggestedSupplier: lastSupplierOfIngredient(l.ingredientId),
    }));
}

/** Các lô đã xuất cho đúng một món của một buổi ăn — nguồn prefill sổ chế biến. */
export function batchesIssuedForDish(
  menuId: string,
  mealType: MealType,
  dishName: string,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS
): string[] {
  return [
    ...new Set(
      transactions
        .filter(
          (t) =>
            t.type === "Xuất kho (FEFO)" &&
            t.menuId === menuId &&
            t.mealType === mealType &&
            t.dishName === dishName
        )
        .map((t) => t.batchId)
    ),
  ];
}

/**
 * Các phiếu xuất đã ghi cho một buổi ăn — dùng để chặn xuất trùng. Xuất lại một
 * buổi đã xuất sẽ trừ tồn lần hai và làm sai cả tồn kho lẫn bản đồ truy xuất.
 */
export function issuedLinesForMeal(
  menuId: string,
  mealType: MealType,
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS
): InventoryTransaction[] {
  return transactions.filter(
    (t) => t.type === "Xuất kho (FEFO)" && t.menuId === menuId && t.mealType === mealType
  );
}

/**
 * Bản đồ món → lô nguyên liệu, DẪN XUẤT từ phiếu xuất kho và sổ chế biến.
 * Thay cho map hardcode cũ; là nguồn của bản đồ truy xuất BR-151 và cách ly BR-149.
 */
/**
 * Phạm vi truy xuất món→lô. BẮT BUỘC dùng khi điều tra sự cố: cùng một tên món
 * được nấu nhiều ngày bằng các lô khác nhau ("Cơm trắng" ngày nào cũng có), nên
 * bản đồ không giới hạn ngày sẽ khoá nhầm lô của ngày khác (over-quarantine).
 * Bỏ trống = toàn bộ lịch sử (chỉ dùng cho thống kê, không dùng để cách ly).
 */
export interface DishBatchScope {
  /** dd/MM/yyyy — ngày phục vụ của sự cố. */
  serviceDate?: string;
  menuId?: string;
  mealType?: string;
}

export function dishBatchMap(
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  preparations: PreparationRecord[] = PREPARATION_RECORDS,
  scope: DishBatchScope = {}
): Record<string, string[]> {
  const map: Record<string, Set<string>> = {};
  const add = (dish: string, batchId: string) => {
    (map[dish] ??= new Set()).add(batchId);
  };

  // Khoá món dùng để gom: ưu tiên `dishId` (không lệch vì tên), lùi về tên khi
  // món nằm ngoài thư viện.
  const keyOf = (dishId?: string, name?: string) =>
    DISH_LIBRARY.find((d) => d.id === dishId)?.name ?? name;

  for (const t of transactions) {
    if (t.type !== "Xuất kho (FEFO)" || !t.dishName) continue;
    if (scope.serviceDate && t.serviceDate !== scope.serviceDate) continue;
    if (scope.menuId && t.menuId !== scope.menuId) continue;
    if (scope.mealType && t.mealType !== scope.mealType) continue;
    const k = keyOf(t.dishId, t.dishName);
    if (k) add(k, t.batchId);
  }
  // Sổ chế biến bổ sung các lô phát sinh ngoài phiếu xuất (đầu bếp thêm tay).
  for (const p of preparations) {
    if (scope.serviceDate && p.date !== scope.serviceDate) continue;
    if (scope.mealType && p.mealType !== scope.mealType) continue;
    const k = keyOf(p.dishId, p.dish);
    if (k) for (const b of p.batchesUsed) add(k, b);
  }

  return Object.fromEntries(Object.entries(map).map(([dish, set]) => [dish, [...set]]));
}

/** Bản đồ ngược lô → các món đã dùng lô đó — dùng khi cách ly lô (BR-149). */
export function batchDishMap(
  transactions: InventoryTransaction[] = INVENTORY_TRANSACTIONS,
  preparations: PreparationRecord[] = PREPARATION_RECORDS
): Record<string, string[]> {
  const reverse: Record<string, Set<string>> = {};
  for (const [dish, batches] of Object.entries(dishBatchMap(transactions, preparations))) {
    for (const b of batches) (reverse[b] ??= new Set()).add(dish);
  }
  return Object.fromEntries(Object.entries(reverse).map(([b, set]) => [b, [...set]]));
}

/**
 * Tương thích ngược cho màn đang import hằng số — nay là dữ liệu dẫn xuất.
 * @deprecated Không dùng để CÁCH LY lô: map này gộp mọi ngày nên sẽ khoá thừa lô
 * của ngày khác. Điều tra sự cố phải dùng `dishBatchesForIncident()`.
 */

/**
 * Lô của một món TRONG ĐÚNG ngày xảy ra sự cố (BR-149/BR-151) — đây mới là tập
 * lô được phép cách ly. `mealDate` lấy từ chính Incident.
 */
export function dishBatchesForIncident(dish: string, mealDate?: string): string[] {
  if (!mealDate) return [];
  return dishBatchMap(INVENTORY_TRANSACTIONS, PREPARATION_RECORDS, {
    serviceDate: mealDate,
  })[dish] ?? [];
}

/**
 * Thông tin để Y tế quyết định có cách ly một lô hay không (BR-149).
 *
 * Khoá lô nay CHẶN THẬT phiếu xuất kho (FEFO loại lô bị cách ly), nên người bấm
 * phải nhìn thấy mình đang khoá cái gì: còn bao nhiêu tồn, và bao nhiêu bữa sắp
 * tới đang trông vào lô này. Khoá nhầm lô gạo là bữa mai không xuất kho được.
 */
export interface QuarantineCandidate {
  batchId: string;
  ingredientName: string;
  remaining: number;
  unit: string;
  receivedDate: string;
  expiryDate: string;
  /** Số bữa ĐÃ CÔNG BỐ từ hôm nay trở đi cần nguyên liệu của lô này. */
  upcomingMeals: number;
  quarantined: boolean;
}

/**
 * @param liveBatches Danh sách lô ĐANG SỐNG (đọc qua API). Bắt buộc truyền nếu màn
 *   hình có thao tác khoá/nhả lô.
 *
 *   `INVENTORY` ở đây chỉ là SEED: `db.inventory = clone(INVENTORY)`, nên mọi thay
 *   đổi đi vào bản sao trong db còn mảng seed thì đứng yên vĩnh viễn. Trước
 *   22/07/2026 hàm này đọc thẳng seed, nên tick cách ly một lô xong `quarantined`
 *   vẫn trả về `false` và ô tick bật lại ngay — nhìn hệt như checkbox bị hỏng,
 *   trong khi lệnh khoá thật ra đã chạy. Không lỗi nào báo ra vì không có gì sai
 *   về kiểu, chỉ là đọc nhầm bản sao.
 */
export function quarantineCandidates(
  batchIds: string[],
  liveBatches?: InventoryBatch[],
  today = new Date()
): QuarantineCandidate[] {
  const source = liveBatches?.length ? liveBatches : INVENTORY;
  const todayIso = today.toISOString().slice(0, 10);
  return batchIds
    .map((id) => {
      const batch = source.find((b) => b.id === id);
      if (!batch) return undefined;
      const upcoming = MENUS.filter(
        (mn) => isMenuLive(mn) && (mn.menuDateIso ?? "") >= todayIso
      ).filter((mn) =>
        (mn.blocksData ?? []).some((bl) =>
          mealIngredientNeeds(mn, bl.mealType, mn.servings ?? 0).some(
            (r) => r.ingredientId === batch.ingredientId
          )
        )
      ).length;
      const unit = batch.quantity.split(" ").slice(1).join(" ") || "kg";
      return {
        batchId: id,
        ingredientName: batch.ingredient,
        remaining: batchRemaining(id, INVENTORY_TRANSACTIONS, source),
        unit,
        receivedDate: batch.receivedDate,
        expiryDate: batch.expiryDate,
        upcomingMeals: upcoming,
        quarantined: batch.quarantine != null,
      };
    })
    .filter((x): x is QuarantineCandidate => x != null);
}

/** Nối yêu cầu thu hồi mẫu về cơ quan tiếp nhận bằng khoá, không bằng tên. */
for (const r of SAMPLE_LAB_REQUESTS) {
  const hit = RECEIVING_AUTHORITIES.find((a) => a.name === r.labDestination);
  r.authorityId ??= hit?.id;
  r.handoverType ??= hit?.type ?? "LAB";
}

/**
 * Nối NGƯỢC mẫu lưu về phiếu bàn giao.
 *
 * Phiếu liệt kê `sampleIds`, nhưng màn danh sách mẫu lại đọc `sample.labRequestId`
 * để biết mẫu đã bàn giao chưa. Một chiều có, chiều kia trống thì mẫu đã giao cho
 * CDC vẫn hiện là đang lưu tại trường — hai màn nói hai chuyện khác nhau về cùng
 * một hộp mẫu. Quan hệ hai chiều thì phải khớp cả hai chiều.
 */
for (const r of SAMPLE_LAB_REQUESTS) {
  for (const sid of r.sampleIds ?? []) {
    const s = FOOD_SAMPLES.find((x) => x.id === sid);
    if (!s) {
      console.warn(`[mock] Phiếu ${r.id} trỏ mẫu không tồn tại: ${sid}`);
      continue;
    }
    s.labRequestId ??= r.id;
    s.incidentId ??= r.incidentId;
  }
}

/**
 * Nguồn gốc của một lô: nguyên liệu, nhà cung cấp, phiếu nhận.
 *
 * Màn điều tra sự cố trước đây giữ một bảng tra CỨNG bốn lô. Bảng đó vừa thiếu
 * (lô thứ năm hiện "—") vừa SAI: `BATCH-GF-4411` ghi là "Rau củ tổng hợp" trong
 * khi lô này là thịt lợn nạc vai, và `BATCH-DD-1080` ghi phiếu nhận
 * `RCV-20260630-02` — phiếu đó nhận lô khác và đã bị TỪ CHỐI. Khoá tồn tại, trỏ
 * đúng bảng, nhưng mô tả sai sự thật; người điều tra đọc xong sẽ đi tìm nhầm nhà
 * cung cấp.
 */
export function batchOriginOf(batchId: string):
  | { ingredient: string; supplier: string; receipt?: string }
  | undefined {
  const b = INVENTORY.find((x) => x.id === batchId);
  if (!b) return undefined;
  const rcv = RECEIVING_SESSIONS.find((r) => r.batches.includes(batchId));
  return { ingredient: b.ingredient, supplier: rcv?.supplier ?? b.supplier, receipt: rcv?.id };
}

/**
 * NỐI CHUỖI NHÀ CUNG CẤP BẰNG KHOÁ.
 *
 * Đơn đặt hàng, phiếu nhận hàng và thực đơn đều chỉ giữ TÊN đối tác, nên màn nào
 * cần biết đối tác còn hợp tác hay không đều phải dò theo tên
 * (`PARTNERS.find(p => p.name === pr.supplier)`). Đổi tên đối tác — hoặc chỉ cần
 * viết tắt khác đi — là đơn cũ mất liên kết, và kiểm tra BR-070 "không nhận hàng
 * của đối tác bị đình chỉ" âm thầm không chạy.
 */
const partnerNameNotLinked = new Set<string>();
function partnerIdOfName(name?: string): string | undefined {
  if (!name) return undefined;
  const hit = PARTNERS.find((p) => p.name === name || name.includes(p.name));
  if (!hit) partnerNameNotLinked.add(name);
  return hit?.id;
}
for (const x of PURCHASE_REQUESTS) x.supplierId ??= partnerIdOfName(x.supplier);
for (const x of RECEIVING_SESSIONS) x.supplierId ??= partnerIdOfName(x.supplier);
for (const x of MENUS) x.createdBySupplierId ??= partnerIdOfName(x.createdBy);
for (const x of MEAL_HISTORY) x.catererSupplierId ??= partnerIdOfName(x.caterer);
// Người chịu trách nhiệm CAPA có thể là cá nhân (bếp trưởng) hoặc đơn vị đối tác —
// chỉ nối khoá cho trường hợp đơn vị, cá nhân thì để trống chứ không bịa.
for (const x of CORRECTIVE_ACTIONS) {
  const hit = PARTNERS.find((p) => x.assignee === p.name || x.assignee.includes(p.name));
  if (hit) x.assigneeSupplierId ??= hit.id;
}
if (partnerNameNotLinked.size > 0) {
  console.warn(
    `[mock-data] ${partnerNameNotLinked.size} tên đối tác không khớp danh sách: ${[...partnerNameNotLinked].join(", ")}`
  );
}

/**
 * NỐI CHUỖI TRUY XUẤT MÓN BẰNG KHOÁ, không bằng tên.
 *
 * Năm bảng cùng nói về "món" nhưng mỗi bảng giữ một chuỗi tên riêng: mẫu lưu, phiên
 * kiểm thực, phiếu xuất kho, sổ chế biến, phản hồi phụ huynh. Chúng nối với nhau —
 * và với thực đơn — bằng cách SO TÊN, nên lệch một chữ là đứt mắt xích mà không có
 * lỗi nào báo ra: "Su su xào tỏi" ở phiếu xuất so với "Su su xào" ở thực đơn từng
 * làm màn điều tra sự cố không hiện được lô nào để cách ly.
 *
 * Backfill khoá tại đây và cảnh báo tên nào không khớp thư viện món.
 */
const dishNameNotLinked = new Set<string>();
function linkDish<T extends { dishId?: string }>(row: T, name: string | undefined) {
  if (!name) return;
  row.dishId ??= DISH_LIBRARY.find((d) => d.name === name)?.id;
  if (!row.dishId) dishNameNotLinked.add(name);
}
for (const x of FOOD_SAMPLES) linkDish(x, x.dish);
for (const x of INSPECTIONS) linkDish(x, x.dish);
for (const x of INVENTORY_TRANSACTIONS) linkDish(x, x.dishName);
for (const x of PREPARATION_RECORDS) linkDish(x, x.dish);
for (const x of FEEDBACKS) linkDish(x, x.dish);
if (dishNameNotLinked.size > 0) {
  console.warn(
    `[mock-data] ${dishNameNotLinked.size} tên món không khớp thư viện — chuỗi truy xuất đứt tại đó: ${[...dishNameNotLinked].join(", ")}`
  );
}

/**
 * CHỐT CHẶN CÔ LẬP DỮ LIỆU — chạy một lần khi nạp module.
 *
 * Các lỗi "món không ra lô", "đơn hàng không kiểm được đối tác còn hợp tác" đều
 * cùng một gốc: một bảng giữ TÊN của thực thể khác thay vì khoá, rồi nối lại bằng
 * so chuỗi. Lệch một chữ là đứt, mà không có gì báo ra — chỉ lộ khi có người bấm
 * đúng vào ca đó. Kiểm ngay tại đây: trường nào giữ tên trùng với tên của một thực
 * thể khác thì phải có khoá đi kèm và khoá đó phải được điền.
 *
 * Bảng ghi log/nhật ký không tính: chúng chép lại nội dung tại thời điểm xảy ra,
 * không phải tham chiếu sống.
 */
const LOG_LIKE = new Set([
  "AUDIT_LOGS",
  "ERROR_LOGS",
  "HEALTH_HISTORY",
  "INCIDENT_EVENTS",
  "NOTIFICATIONS",
  "SENT_REPORTS",
]);
/*
 * Bảng có trường `name` của CHÍNH NÓ thì không phải cô lập — nhận ra bằng cách so
 * `id` của bảng với `id` của thực thể, không liệt kê tên bảng: registry dùng tên
 * resource ("dishes") khác tên hằng ("DISH_LIBRARY"), liệt kê tay là sót.
 */

export function findIsolatedNameFields(
  tables: Record<string, unknown>
): string[] {
  const pools = [
    { label: "món", names: new Set(DISH_LIBRARY.map((d) => d.name)), ids: new Set(DISH_LIBRARY.map((d) => d.id)), suffix: "dishid" },
    {
      label: "nguyên liệu",
      names: new Set(INGREDIENT_CATALOG.map((c) => c.name)),
      ids: new Set(INGREDIENT_CATALOG.map((c) => c.id)),
      suffix: "ingredientid",
      // Lô hàng của NCC nối gián tiếp: productId → SupplierProduct → ingredientId.
      // Nối một chặng vẫn là nối; ép thêm `ingredientId` ở đây chỉ tạo bản sao.
      altSuffix: "productid",
    },
    { label: "đối tác", names: new Set(PARTNERS.map((p) => p.name)), ids: new Set(PARTNERS.map((p) => p.id)), suffix: "supplierid" },
    { label: "học sinh", names: new Set(STUDENTS.map((st) => st.name)), ids: new Set(STUDENTS.map((st) => st.id)), suffix: "studentid" },
  ];
  const problems: string[] = [];
  for (const [name, value] of Object.entries(tables)) {
    if (LOG_LIKE.has(name)) continue;
    if (!Array.isArray(value) || value.length === 0) continue;
    const rows = value.filter((r): r is Record<string, unknown> => r != null && typeof r === "object");
    if (rows.length === 0) continue;
    const fields = new Set<string>();
    for (const r of rows) for (const k of Object.keys(r)) fields.add(k);
    for (const f of fields) {
      if (/Id$|Ids$/.test(f)) continue;
      for (const pool of pools) {
        // Chính bảng của thực thể đó — `name` là tên của nó, không phải tham chiếu.
        const ownIds = rows.filter((r) => typeof r.id === "string" && pool.ids.has(r.id as string));
        if (ownIds.length / rows.length >= 0.6) continue;
        const vals = rows.map((r) => r[f]).filter((v): v is string => typeof v === "string");
        if (vals.length === 0) continue;
        const hits = vals.filter((v) => pool.names.has(v));
        if (hits.length / vals.length < 0.6) continue;
        const alt = (pool as { altSuffix?: string }).altSuffix;
        const keyField = [...fields].find(
          (k) => k.toLowerCase().endsWith(pool.suffix) || (alt != null && k.toLowerCase().endsWith(alt))
        );
        const named = rows.filter((r) => typeof r[f] === "string" && pool.names.has(r[f] as string));
        if (!keyField) {
          problems.push(`${name}.${f} giữ tên ${pool.label} nhưng KHÔNG có trường khoá`);
        } else {
          const missing = named.filter((r) => !r[keyField]).length;
          if (missing > 0) problems.push(`${name}.${f} → ${keyField} còn trống ở ${missing}/${named.length} dòng`);
        }
      }
    }
  }
  return problems;
}

/*
 * Mẫu lưu CÒN DÙNG ĐƯỢC phải truy ra lô nguyên liệu.
 *
 * Màn điều tra sự cố cho tick món (lấy từ mẫu lưu) rồi hiện lô để cách ly; lô tra
 * từ phiếu xuất kho theo (ngày phục vụ, tên món). Thiếu phiếu xuất — hoặc tên món
 * trong phiếu xuất lệch tên trong thực đơn dù chỉ một chữ ("Su su xào tỏi" vs
 * "Su su xào") — là bảng lô rỗng, không báo gì, nhìn như tính năng hỏng.
 *
 * Mẫu đã huỷ thì không tính: chúng không chọn được để cách ly nữa.
 */
const samplesWithoutBatches = FOOD_SAMPLES.filter(
  (s) => s.status !== "DISPOSED" && dishBatchesForIncident(s.dish, s.collectedAt.slice(0, 10)).length === 0
);
if (samplesWithoutBatches.length > 0) {
  console.warn(
    `[mock-data] ${samplesWithoutBatches.length} mẫu lưu không truy ra lô nguyên liệu — màn điều tra sẽ không hiện lô nào để cách ly: ${samplesWithoutBatches.map((s) => `${s.dish} (${s.collectedAt.slice(0, 10)})`).join(", ")}`
  );
}

/** Nhân sự bếp đủ điều kiện đứng tên người nấu — nguồn Select, không gõ tay. */
export function kitchenStaffNames(): string[] {
  return SCHOOL_STAFF.filter((s) => s.role === "Service Caterers" && s.status === "ACTIVE").map(
    (s) => s.name
  );
}

/** Chỉ thực đơn đã duyệt/đã công bố mới được xuất kho phục vụ. */
export function issuableMenus(): typeof MENUS {
  return MENUS.filter(isMenuLive);
}
