/**
 * In-memory database cho mock API.
 * Seed từ mock-data.ts; mọi mutation (create/update) chỉ tồn tại
 * trong phiên hiện tại — reload trang sẽ reset về seed.
 * Resource mới cho Teacher: student-distributions, meal-records.
 */

import {
  ACADEMIC_YEARS,
  ANNOUNCEMENTS,
  AUDIT_LOGS,
  CHECKLIST_GROUPS_SEED,
  CLASSROOMS,
  CORRECTIVE_ACTIONS,
  DELIVERIES,
  DELIVERY_PERSONS,
  DISH_LIBRARY,
  ERROR_LOGS,
  FEEDBACKS,
  FOOD_SAMPLES,
  ALLERGY_DECLARATIONS,
  findIsolatedNameFields,
  SAFETY_SIGNOFFS,
  RECEIVING_AUTHORITIES,
  SCHOOL_CONFIG,
  SECURITY_POLICIES,
  EXTERNAL_INTEGRATIONS,
  SUPPLIER_PRODUCTS,
  PARENT_CHRONIC_CONDITIONS,
  HEALTH_HISTORY,
  INCIDENTS,
  INCIDENT_EVENTS,
  INCIDENT_RECORDS,
  INSPECTIONS,
  INVENTORY,
  INVENTORY_TRANSACTIONS,
  KITCHEN_INSPECTIONS,
  MENUS,
  MENU_TEMPLATES,
  OVERRIDE_ABUSE_REPORT,
  NOTIFICATIONS,
  PARTNERS,
  PARTNERSHIP_APPLICATIONS,
  PERMISSIONS,
  PREPARATION_RECORDS,
  SAMPLE_LAB_REQUESTS,
  PURCHASE_REQUESTS,
  RECEIVING_SESSIONS,
  ROLE_RECORDS,
  SCHOOLS,
  SCHOOL_STAFF,
  SENT_REPORTS,
  SOURCE_REPORTS,
  SPECIAL_MEAL_REQUESTS,
  STUDENT_DISTRIBUTIONS,
  STUDENTS,
  TEACHER_MEAL_RECORDS,
  SUPPLIER_BATCHES,
  SUPPLIER_DOCUMENTS,
  SUPPLIER_PARTNERSHIPS,
  USER_ACCOUNTS,
  VIOLATIONS,
} from "@/lib/mock-data";

/** Bản ghi bất kỳ có id — mọi resource trong db đều thỏa mãn. */
export type Row = { id: string } & Record<string, unknown>;

function clone<T>(rows: T[]): Row[] {
  return JSON.parse(JSON.stringify(rows)) as Row[];
}

/**
 * Registry resource → mảng bản ghi.
 * Tên resource khớp path REST: /api/<resource> và /api/<resource>/<id>.
 */
export const db: Record<string, Row[]> = {
  announcements: clone(ANNOUNCEMENTS),
  // 22/07/2026: THỰC ĐƠN RIÊNG đã GỘP vào đây — một `Menu` đầy đủ với `audience`
  // = DietGroup.id (không còn resource "menu-variants" tách rời). FE nhìn per-buổi
  // qua `specialMenuMeals(menus)`. Backend: một bảng menus, lọc theo cột audience.
  menus: clone(MENUS),
  // Đợt B (21/07/2026): THƯ VIỆN MÓN dùng chung — lập thực đơn chọn món từ đây
  // thay vì gõ lại tên + thành phần + kcal mỗi lần (nguồn của lỗi món nối bằng
  // tên chuỗi tự do và allergen lệch catalog).
  dishes: clone(DISH_LIBRARY),
  // G9 (25/07/2026): THỰC ĐƠN MẪU — bộ món đặt tên sẵn ("Tiêu chuẩn A/B/C",
  // "Ăn chay A1"), áp vào ô (ngày × buổi) của Lịch thực đơn tuần thay vì khai
  // lại từng món mỗi tuần. Món tham chiếu DISH_LIBRARY qua dishIds.
  "menu-templates": clone(MENU_TEMPLATES),
  receiving: clone(RECEIVING_SESSIONS),
  inventory: clone(INVENTORY),
  inspections: clone(INSPECTIONS),
  "kitchen-inspections": clone(KITCHEN_INSPECTIONS),
  // UC-187–190 (13/07/2026): lịch sử chung 3 loại báo cáo ATTP đã gửi (BR-177) — thay
  // resource "compliance-reports" cũ (chỉ phục vụ UC-185); nay UC-185/188/189 đều ghi vào đây
  "sent-reports": clone(SENT_REPORTS),
  // UC-186/BR-210: checklist kiểm tra bếp động — Medical Staff tự CRUD (seed 6 nhóm, 06/07/2026)
  "checklist-groups": clone(CHECKLIST_GROUPS_SEED),
  // Redesign Incident 09/07/2026: báo cáo thô RPT-xxx của Teacher/Parent (N-1 vào INC-xxx)
  "source-reports": clone(SOURCE_REPORTS),
  // Mục 10.3 (15/07/2026): biên bản sự cố — bước soạn thảo giữa triage và mở phiên điều tra
  "incident-records": clone(INCIDENT_RECORDS),
  // Resource "pest-control-log" ĐÃ BỎ 27/07/2026 (Vòng 9 Mục 17): không actor nào ghi vào
  // được (grep `useCreate` ra 0 kết quả), và côn trùng nay chỉ phản ánh qua 2 mục
  // CLI-3-8/CLI-4-6 của checklist UC-014 — xem chú thích trong `mock-data.ts`.
  samples: clone(FOOD_SAMPLES),
  // review foodsamples Mục 3.5 (19/07/2026): task giao việc thu hồi & gửi xét nghiệm —
  // Medical Staff tạo (BR-113), Service Caterers thực hiện/xác nhận (BR-215)
  "sample-lab-requests": clone(SAMPLE_LAB_REQUESTS),
  incidents: clone(INCIDENTS),
  // Sổ sự kiện sự cố append-only (22/07/2026): dòng thời gian điều tra phải có mốc
  // THẬT, không dựng lại từ cột trạng thái (mô hình chỉ lưu trạng thái cuối).
  "incident-events": clone(INCIDENT_EVENTS),
  // Derived fields hasAllergy/hasSpecialDiet phục vụ Profile Filter Tabs
  // (mock-server chỉ hỗ trợ lọc chính xác theo field — 02_StudentList.md)
  students: clone(
    STUDENTS.map((s) => ({
      ...s,
      hasAllergy: s.allergies.length > 0 ? "yes" : "no",
      hasSpecialDiet: s.specialDiet ? "yes" : "no",
    }))
  ),
  partners: clone(PARTNERS),
  "purchase-requests": clone(PURCHASE_REQUESTS),
  deliveries: clone(DELIVERIES),
  // Review 07/2026: module sinh trắc học nhân viên giao hàng (BR-050/BR-051)
  "delivery-persons": clone(DELIVERY_PERSONS),
  // Sản phẩm của NCC — mang giá, và là cha của các lô hàng. Một nguyên liệu trong
  // danh mục trường có thể có nhiều NCC bán với giá khác nhau.
  "supplier-products": clone(SUPPLIER_PRODUCTS),
  "supplier-batches": clone(SUPPLIER_BATCHES),
  "supplier-documents": clone(SUPPLIER_DOCUMENTS),
  // Trước 23/07/2026: 2 bảng này là mảng tĩnh import thẳng vào màn Supplier
  // Partnerships, không nằm trong db → nộp đơn xong không có gì để ghi vào,
  // useCreate/invalidate không chạy được, nên đơn mới không bao giờ lên bảng.
  "supplier-partnerships": clone(SUPPLIER_PARTNERSHIPS),
  "partnership-applications": clone(PARTNERSHIP_APPLICATIONS),
  users: clone(USER_ACCOUNTS),
  schools: clone(SCHOOLS),
  "audit-logs": clone(AUDIT_LOGS),
  // `distributions` đã bỏ (21/07/2026): tổng hợp ngày × lớp nay DẪN XUẤT từ
  // `student-distributions` qua `distributionSummaries()` — giữ bảng riêng thì bếp
  // đọc số liệu đóng băng, không thấy việc giáo viên vừa phát.
  "student-distributions": clone(STUDENT_DISTRIBUTIONS),
  "meal-records": clone(TEACHER_MEAL_RECORDS),
  "corrective-actions": clone(CORRECTIVE_ACTIONS),
  feedbacks: clone(FEEDBACKS),
  classrooms: clone(CLASSROOMS),
  "academic-years": clone(ACADEMIC_YEARS),
  violations: clone(VIOLATIONS),
  permissions: clone(PERMISSIONS),
  roles: clone(ROLE_RECORDS),
  staff: clone(SCHOOL_STAFF),
  "error-logs": clone(ERROR_LOGS),
  "override-abuse": clone(OVERRIDE_ABUSE_REPORT),
  "special-meals": clone(SPECIAL_MEAL_REQUESTS),
  // Khai báo dị ứng (22/07/2026): trước đó màn khai của Phụ huynh chỉ giữ trong
  // useState — tải lại trang là mất, và Y tế không bao giờ nhìn thấy khai báo mới.
  // Từ khi `Student.allergies` dẫn xuất từ bảng này, nó là nguồn sự thật nên bắt
  // buộc phải nằm trong db chứ không thể là state của một màn.
  "allergy-declarations": clone(ALLERGY_DECLARATIONS),
  // Chữ ký xác nhận an toàn hằng ngày của BGH — bản ghi tuân thủ, trước đây chỉ là
  // `useState` nên tải lại trang là mất và thanh tra không có gì để đối chiếu.
  // Cơ quan tiếp nhận mẫu: phòng kiểm nghiệm (gửi xét nghiệm, chờ kết quả) và cơ
  // quan chức năng (công an/thanh tra thu giữ làm vật chứng) — hai loại khác hẳn.
  "receiving-authorities": clone(RECEIVING_AUTHORITIES),
  "safety-signoffs": clone(SAFETY_SIGNOFFS),
  // Tham số vận hành cấp trường (trần chi phí, geofence, email báo cáo) — trước đây
  // màn Cấu hình trường chỉ giữ trong state và không tham số nào tới được nơi dùng.
  "school-config": clone(SCHOOL_CONFIG),
  // Chính sách bảo mật + dịch vụ ngoài: hai màn System Admin trước đây giữ trong
  // state, và danh sách dịch vụ ngoài còn tồn tại hai bản nói ngược nhau.
  "security-policies": clone(SECURITY_POLICIES),
  "integrations": clone(EXTERNAL_INTEGRATIONS),
  // Bệnh lý nền — cùng lý do với khai báo dị ứng: màn của Phụ huynh vốn chỉ giữ
  // trong useState nên khai xong là mất.
  "chronic-conditions": clone(PARENT_CHRONIC_CONDITIONS),
  "health-history": clone(HEALTH_HISTORY),
  notifications: clone(NOTIFICATIONS),
  "inventory-transactions": clone(INVENTORY_TRANSACTIONS),
  // `special-meal-plans` đã bỏ (21/07/2026): kế hoạch suất riêng nay DẪN XUẤT từ
  // `menus` + `menu-variants` qua `lib/special-meal-plans` — bảng cũ là seed đóng
  // băng không màn nào ghi, khiến BGH giám sát trên dữ liệu chết.
  // `caterer-violations` đã bỏ (21/07/2026): trùng mô hình với `violations` —
  // 3/4 bản ghi trùng đúng sự việc đã có bên đó, khiến báo cáo tổng hợp BR-214
  // đếm trùng và gửi kèm mã vi phạm không nằm trong bảng.
  "preparation-records": clone(PREPARATION_RECORDS),
};

/*
 * Chạy chốt chặn cô lập dữ liệu ngay sau khi registry dựng xong — đây là chỗ duy
 * nhất thấy được TOÀN BỘ bảng cùng lúc. Cảnh báo chứ không ném lỗi: bản mock vẫn
 * phải chạy được để người dùng thấy chỗ hỏng, nhưng không được im lặng.
 */
const isolated = findIsolatedNameFields(db as unknown as Record<string, unknown>);
if (isolated.length > 0) {
  console.warn(
    `[db] ${isolated.length} chỗ dữ liệu bị cô lập (giữ tên thay vì khoá): ${isolated.join(" | ")}`
  );
}
