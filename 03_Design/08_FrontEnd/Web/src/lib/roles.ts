import type { LucideIcon } from "lucide-react";
import {
  Activity,
  AlertTriangle,
  Apple,
  Archive,
  BarChart3,
  Bell,
  BookOpen,
  Building2,
  CalendarDays,
  CheckSquare,
  ClipboardCheck,
  ClipboardList,
  CookingPot,
  Database,
  FileCheck2,
  FileText,
  FlaskConical,
  Gavel,
  GraduationCap,
  Handshake,
  HeartPulse,
  History,
  LayoutDashboard,
  LayoutTemplate,
  MessageSquare,
  Package,
  PackageSearch,
  PackageCheck,
  ScanFace,
  School,
  Send,
  Settings,
  ShieldCheck,
  Soup,
  Stethoscope,
  Thermometer,
  Truck,
  UserCog,
  Users,
  UtensilsCrossed,
  Warehouse,
} from "lucide-react";

export type RoleKey =
  | "system-admin"
  | "school-admin"
  | "medical-staff"
  | "service-caterers"
  | "teacher"
  | "parent"
  | "supplier";

export interface NavItem {
  label: string;
  href: string;
  icon: LucideIcon;
}

export interface NavGroup {
  title?: string;
  items: NavItem[];
}

export interface RoleConfig {
  key: RoleKey;
  label: string;
  labelEn: string;
  icon: LucideIcon;
  description: string;
  nav: NavGroup[];
}

export const ROLES: Record<RoleKey, RoleConfig> = {
  "system-admin": {
    key: "system-admin",
    label: "Quản trị Hệ thống",
    labelEn: "System Admin",
    icon: Settings,
    description: "Quản lý người dùng, vai trò, trường học và cấu hình hệ thống",
    nav: [
      {
        items: [
          {
            label: "Tổng quan",
            href: "/system-admin/dashboard",
            icon: LayoutDashboard,
          },
        ],
      },
      {
        title: "Quản trị",
        items: [
          { label: "Tài khoản người dùng", href: "/system-admin/users", icon: Users },
          { label: "Vai trò", href: "/system-admin/roles", icon: UserCog },
          { label: "Danh mục quyền", href: "/system-admin/permissions", icon: ShieldCheck },
          { label: "Trường học", href: "/system-admin/schools", icon: School },
        ],
      },
      {
        title: "Cấu hình hệ thống",
        items: [
          { label: "Chính sách bảo mật", href: "/system-admin/security-policies", icon: Settings },
          { label: "Tích hợp ngoài & AI", href: "/system-admin/integrations", icon: Handshake },
        ],
      },
      {
        title: "Giám sát & Audit",
        items: [
          { label: "Audit Logs", href: "/system-admin/audit-logs", icon: History },
          { label: "Giám sát hoạt động", href: "/system-admin/monitoring", icon: Activity },
          { label: "Trạng thái hệ thống", href: "/system-admin/system-status", icon: CheckSquare },
          { label: "Sao lưu dữ liệu", href: "/system-admin/backup", icon: Database },
          { label: "Nhật ký lỗi", href: "/system-admin/error-logs", icon: AlertTriangle },
          { label: "Thống kê sử dụng", href: "/system-admin/usage-statistics", icon: BarChart3 },
        ],
      },
    ],
  },
  "school-admin": {
    key: "school-admin",
    label: "Ban Giám hiệu",
    labelEn: "School Administration",
    icon: Building2,
    description: "Phê duyệt đối tác, thực đơn, quản lý lớp học và sự cố",
    nav: [
      {
        items: [
          {
            label: "Tổng quan",
            href: "/school-admin/dashboard",
            icon: LayoutDashboard,
          },
        ],
      },
      {
        title: "Đối tác & Thực đơn",
        items: [
          { label: "Yêu cầu hợp tác", href: "/school-admin/partnerships", icon: Handshake },
          { label: "Phê duyệt thực đơn", href: "/school-admin/menu-approval", icon: UtensilsCrossed },
        ],
      },
      {
        title: "Trường học",
        items: [
          { label: "Năm học", href: "/school-admin/academic-years", icon: CalendarDays },
          { label: "Lớp học", href: "/school-admin/classrooms", icon: GraduationCap },
          { label: "Học sinh & Phụ huynh", href: "/school-admin/students", icon: Users },
          { label: "Nhân viên trường", href: "/school-admin/staff", icon: UserCog },
          { label: "Thông báo", href: "/school-admin/announcements", icon: Bell },
        ],
      },
      {
        title: "An toàn thực phẩm",
        items: [
          { label: "Sự cố", href: "/school-admin/incidents", icon: AlertTriangle },
          { label: "Bảng tuân thủ hôm nay", href: "/school-admin/compliance", icon: Activity },
          { label: "Duyệt khắc phục (CAPA)", href: "/school-admin/corrective-actions", icon: CheckSquare },
          { label: "Vi phạm & Xử lý", href: "/school-admin/violations", icon: Gavel },
          { label: "Rà soát dị ứng", href: "/school-admin/allergy-review", icon: HeartPulse },
          { label: "Suất ăn đặc biệt", href: "/school-admin/special-meals", icon: UtensilsCrossed },
          { label: "Phản hồi phụ huynh", href: "/school-admin/feedback", icon: MessageSquare },
          { label: "Báo cáo tuân thủ", href: "/school-admin/reports", icon: BarChart3 },
        ],
      },
      {
        title: "Cấu hình",
        items: [
          { label: "Cấu hình trường", href: "/school-admin/config", icon: Settings },
        ],
      },
    ],
  },
  "medical-staff": {
    key: "medical-staff",
    label: "Nhân viên Y tế",
    labelEn: "Medical Staff",
    icon: Stethoscope,
    description: "Hồ sơ sức khỏe, duyệt dị ứng, giám sát kiểm thực và sự cố",
    /**
     * IA sidebar tái cấu trúc 27/07/2026 theo
     * `review/medical_staff/review_medicalstaff_navigation_ia.md` Mục 3 — 7 nhóm cũ gộp
     * còn 6, xếp theo TẦN SUẤT THAO TÁC thực tế (phỏng vấn Cán bộ Y tế 25/07/2026):
     * việc hàng ngày → việc phát sinh → dữ liệu đầu kỳ → báo cáo nhìn lại → nhà trường.
     *
     * Thuần điều hướng: KHÔNG đổi `href`/`icon` của bất kỳ mục nào, không đổi field,
     * không đổi business rule. Các điểm đáng chú ý:
     *  · "Sổ lưu mẫu" (nhóm riêng cũ) gộp vào Module 2 cùng việc kiểm tra hàng ngày.
     *  · Module 3 gộp 2 nhóm cũ (Sự cố + Vi phạm) — "Sổ vi phạm & Xử lý" Ở LẠI đây,
     *    KHÔNG chuyển sang nhóm Báo cáo (Mục 2.2, sửa sau phản biện: Y tế đi kiểm tra
     *    bếp trực tiếp vẫn cần lối vào lập biên bản độc lập, không qua Incident nào).
     *  · "Chính sách trường học" chuyển từ nhóm ATTP sang nhóm "Nhà trường".
     *  · "Danh mục & Hồ sơ pháp lý NCC" KHÔNG thêm (Mục 2.1 — thuộc Service Caterers).
     */
    nav: [
      {
        items: [
          {
            label: "Tổng quan",
            href: "/medical-staff/dashboard",
            icon: LayoutDashboard,
          },
        ],
      },
      {
        // Module 2 — đẩy lên đầu: việc lặp lại mỗi ngày
        title: "Công việc Kiểm tra Hàng ngày",
        items: [
          /*
           * Vòng 10 Mục 19.2 (27/07/2026) — SỬA HỒI QUY: href trỏ thẳng vào màn TẠO phiên
           * mới, trong khi Mục 8.4 điểm 1 và Mục 14.1 đều đã chốt màn Lịch sử là điểm vào
           * chính của UC-014 (xem lại phiên cũ, lập biên bản, rồi mới "Tạo phiên kiểm tra
           * mới" từ đó). Nhãn và vị trí trong sidebar giữ nguyên.
           */
          { label: "Kiểm tra an toàn bếp", href: "/medical-staff/conduct-inspection/history", icon: CheckSquare },
          { label: "Hồ sơ kiểm thực", href: "/medical-staff/inspections", icon: ClipboardCheck },
          { label: "Sổ lưu mẫu (Mẫu số 5)", href: "/medical-staff/samples", icon: FlaskConical },
        ],
      },
      {
        // Module 3 — việc phát sinh: sự cố → điều tra → khắc phục → biên bản vi phạm
        title: "Xử lý Sự cố & Vi phạm",
        items: [
          { label: "Sự cố & Điều tra", href: "/medical-staff/incidents", icon: AlertTriangle },
          { label: "Hành động khắc phục", href: "/medical-staff/corrective-actions", icon: CheckSquare },
          { label: "Sổ vi phạm & Xử lý", href: "/medical-staff/violations", icon: Gavel },
        ],
      },
      {
        // Module 1 — dữ liệu đầu kỳ, ít thay đổi trong năm học
        title: "Hồ sơ Sức khỏe & Dị ứng",
        items: [
          { label: "Duyệt khai báo sức khỏe", href: "/medical-staff/health-reviews", icon: HeartPulse },
          { label: "Hồ sơ sức khỏe", href: "/medical-staff/health-records", icon: FileText },
          { label: "Bữa ăn đặc biệt", href: "/medical-staff/special-meals", icon: Soup },
        ],
      },
      {
        // Module 4 — công cụ NHÌN LẠI, tách khỏi việc hàng ngày (Mục 2.3)
        title: "Báo cáo & Thống kê",
        items: [
          { label: "Bảng giám sát tuân thủ", href: "/medical-staff/compliance", icon: Activity },
          /*
           * Vòng 4 — 26/07/2026 (review compliance_kitcheninspection Mục 10.2/10.4):
           * mục "Trung tâm Báo cáo ATTP" (UC-187) ĐÃ BỎ — hub chỉ còn làm nhiệm vụ điều
           * hướng, đúng thứ nguyên tắc Mục 10.6 điểm 2 cấm. Hai mục dưới thay nó:
           *  · "Báo cáo đã gửi" = UC-190, tái dùng path `/reports` vừa trống.
           * Soạn báo cáo không có mục nav riêng: vào từ đúng module (Compliance / Vi phạm).
           *
           * Vòng 6 — 27/07/2026 (Mục 13.3 điểm 1): mục "Tuân thủ theo kỳ"
           * (`/compliance/history`) thêm ở Vòng 4 nay BỎ luôn — màn đó chỉ chép lại dữ liệu
           * nguồn, không tổng hợp gì; nội dung "xem/soạn theo kỳ" gộp vào Bảng giám sát
           * tuân thủ, còn UC-115/117 thu gọn thành 1 nút xuất dữ liệu ở chính màn đó.
           */
          { label: "Báo cáo đã gửi", href: "/medical-staff/reports", icon: Send },
        ],
      },
      {
        title: "Nhà trường",
        items: [
          { label: "Chính sách trường học", href: "/medical-staff/policies", icon: ShieldCheck },
          { label: "Thông báo trường", href: "/medical-staff/announcements", icon: Bell },
        ],
      },
    ],
  },
  "service-caterers": {
    key: "service-caterers",
    label: "Đơn vị Suất ăn",
    labelEn: "Service Caterers",
    icon: UtensilsCrossed,
    description:
      "Thực đơn, nhận nguyên liệu, kiểm thực ba bước, lưu mẫu, kho và phục vụ",
    nav: [
      {
        items: [
          {
            label: "Tổng quan",
            href: "/service-caterers/dashboard",
            icon: LayoutDashboard,
          },
        ],
      },
      {
        // Sắp xếp 27/07/2026: nhóm theo nhịp công việc trong ngày của bếp —
        // "Hàng ngày" gom đúng 4 việc phải làm tại chỗ, đặt ngay dưới Tổng quan.
        title: "Hàng ngày",
        items: [
          { label: "Bảng nấu ăn hôm nay", href: "/service-caterers/cook-sheet", icon: CookingPot },
          { label: "Kiểm thực ba bước", href: "/service-caterers/inspections", icon: ClipboardCheck },
          { label: "Lưu mẫu thực phẩm", href: "/service-caterers/samples", icon: FlaskConical },
          { label: "Phục vụ bữa ăn", href: "/service-caterers/meal-service", icon: Soup },
        ],
      },
      {
        title: "Nguyên liệu",
        items: [
          { label: "Kho nguyên liệu", href: "/service-caterers/inventory", icon: Warehouse },
          { label: "Giao dịch kho", href: "/service-caterers/inventory-transactions", icon: History },
          { label: "Đặt hàng nguyên liệu", href: "/service-caterers/orders", icon: ClipboardList },
          { label: "Nhận nguyên liệu", href: "/service-caterers/receiving", icon: PackageCheck },
        ],
      },
      {
        title: "Thực đơn",
        items: [
          // Gộp 23/07/2026: "Bữa ăn đặc biệt" không còn là màn riêng — suất theo
          // chế độ ăn nay là các dòng accordion trong chính Lịch thực đơn tuần.
          { label: "Lịch thực đơn tuần", href: "/service-caterers/menus", icon: UtensilsCrossed },
          // G9 (25/07/2026): bộ món đặt tên sẵn ("Tiêu chuẩn A/B/C", "Ăn chay A1")
          // để áp vào ô lịch tuần thay vì khai lại từng món mỗi tuần.
          { label: "Thực đơn mẫu", href: "/service-caterers/menu-templates", icon: LayoutTemplate },
          { label: "Thư viện món", href: "/service-caterers/dishes", icon: BookOpen },
          { label: "Sổ chế biến", href: "/service-caterers/preparation-log", icon: FileText },
        ],
      },
      {
        title: "Tuân thủ",
        items: [
          { label: "Sự cố liên quan", href: "/service-caterers/incidents", icon: AlertTriangle },
          { label: "Hành động khắc phục", href: "/service-caterers/corrective-actions", icon: CheckSquare },
        ],
      },
      {
        title: "Hồ sơ",
        items: [
          { label: "Hồ sơ đơn vị", href: "/service-caterers/profile", icon: UserCog },
        ],
      },
      {
        title: "Nhà trường & Cá nhân",
        items: [
          { label: "Thông báo trường", href: "/service-caterers/announcements", icon: Bell },
          { label: "Cài đặt thông báo", href: "/service-caterers/notification-settings", icon: Settings },
        ],
      },
    ],
  },
  teacher: {
    key: "teacher",
    label: "Giáo viên",
    labelEn: "Teacher",
    icon: GraduationCap,
    description: "Học sinh lớp chủ nhiệm, phân phát bữa ăn, báo cáo sự cố",
    nav: [
      {
        items: [
          { label: "Tổng quan", href: "/teacher/dashboard", icon: LayoutDashboard },
        ],
      },
      {
        title: "Bữa ăn hôm nay",
        items: [
          { label: "Phân phát bữa ăn", href: "/teacher/distribution", icon: Soup },
          { label: "Ghi nhận bữa ăn", href: "/teacher/meal-records", icon: ClipboardList },
        ],
      },
      {
        title: "Học sinh",
        items: [
          { label: "Danh sách học sinh", href: "/teacher/students", icon: Users },
          { label: "Suất ăn đặc biệt", href: "/teacher/special-meals", icon: Apple },
        ],
      },
      {
        title: "An toàn",
        items: [
          { label: "Báo cáo sự cố", href: "/teacher/incidents", icon: AlertTriangle },
        ],
      },
      {
        title: "Nhà trường",
        items: [
          { label: "Thông báo trường", href: "/teacher/announcements", icon: Bell },
        ],
      },
    ],
  },
  parent: {
    key: "parent",
    label: "Phụ huynh",
    labelEn: "Parent",
    icon: Users,
    description: "Thực đơn tuần, dinh dưỡng, khai báo dị ứng, phản hồi bữa ăn",
    nav: [
      {
        title: "Tổng quan",
        items: [
          { label: "Trang tổng quan", href: "/parent/dashboard", icon: LayoutDashboard },
        ],
      },
      {
        title: "Bữa ăn của con",
        items: [
          { label: "Bữa ăn trường học", href: "/parent/school-meals", icon: Soup },
          { label: "Thực đơn tuần", href: "/parent/weekly-menu", icon: UtensilsCrossed },
          { label: "Lịch sử bữa ăn", href: "/parent/meal-history", icon: History },
          { label: "Lịch sử dinh dưỡng", href: "/parent/nutrition-history", icon: BarChart3 },
        ],
      },
      {
        title: "Sức khỏe",
        items: [
          { label: "Sức khỏe của con", href: "/parent/health", icon: HeartPulse },
          { label: "Suất ăn đặc biệt", href: "/parent/special-meals", icon: Apple },
          { label: "Khai báo dị ứng", href: "/parent/allergies", icon: Stethoscope },
          { label: "Khai báo bệnh lý nền", href: "/parent/chronic-conditions", icon: Activity },
          { label: "Khai báo bữa ăn đặc biệt", href: "/parent/special-meal-requests", icon: ClipboardList },
        ],
      },
      {
        title: "Phản hồi",
        items: [
          { label: "Trung tâm phản hồi", href: "/parent/feedback", icon: MessageSquare },
          { label: "Báo sự cố ATTP", href: "/parent/safety-concern", icon: AlertTriangle },
        ],
      },
      {
        title: "Nhà trường",
        items: [
          { label: "Thông báo trường", href: "/parent/announcements", icon: Bell },
        ],
      },
    ],
  },
  supplier: {
    key: "supplier",
    label: "Nhà cung cấp",
    labelEn: "Raw Food Supplier",
    icon: Truck,
    description: "Đơn hàng, lô nguyên liệu, giao hàng và chứng nhận ATTP",
    nav: [
      {
        items: [
          { label: "Tổng quan", href: "/supplier/dashboard", icon: LayoutDashboard },
        ],
      },
      {
        title: "Cung ứng",
        items: [
          { label: "Mặt hàng cung ứng", href: "/supplier/products", icon: PackageSearch },
          { label: "Yêu cầu mua hàng", href: "/supplier/purchase-requests", icon: ClipboardList },
          { label: "Lô nguyên liệu", href: "/supplier/batches", icon: Package },
          { label: "Giao hàng", href: "/supplier/deliveries", icon: Truck },
          { label: "Nhân viên giao hàng", href: "/supplier/delivery-staff", icon: ScanFace },
        ],
      },
      {
        title: "Hồ sơ",
        items: [
          { label: "Chứng nhận & Giấy phép", href: "/supplier/certificates", icon: FileCheck2 },
          { label: "Trường hợp tác", href: "/supplier/partnerships", icon: Handshake },
          { label: "Thông báo trường", href: "/supplier/announcements", icon: Bell },
        ],
      },
    ],
  },
};

export const ROLE_LIST = Object.values(ROLES);

export function getRoleFromPathname(pathname: string): RoleConfig | undefined {
  const segment = pathname.split("/").filter(Boolean)[0];
  return ROLES[segment as RoleKey];
}

// Icon re-exports dùng chung cho các trang
export {
  Apple,
  Archive,
  Thermometer,
};
