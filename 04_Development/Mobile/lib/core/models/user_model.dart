enum UserRole {
  teacher,
  parent,
  medicalStaff,
  serviceCaterers,
  rawFoodSupplier,
  /// Ngoài phạm vi mobile (TASKS_MOBILE_SCOPE.md — "làm việc bàn giấy, ưu tiên
  /// thấp") — khai ở đây CHỈ để `roleFromKey()` parse được (không throw) và
  /// `_dashboardFor()` trả về thông báo "chưa hỗ trợ" nhất quán, KHÔNG có
  /// dashboard/màn hình nào cho 2 role này trên mobile.
  schoolAdmin,
  systemAdmin,
}

extension UserRoleExtension on UserRole {
  String get displayName {
    switch (this) {
      case UserRole.teacher:
        return 'Giáo Viên Chủ Nhiệm';
      case UserRole.parent:
        return 'Phụ Huynh Học Sinh';
      case UserRole.medicalStaff:
        return 'Cán Bộ Y Tế Trường';
      case UserRole.serviceCaterers:
        return 'Đơn Vị Bếp Ăn';
      case UserRole.rawFoodSupplier:
        return 'Nhà Cung Cấp Thực Phẩm';
      case UserRole.schoolAdmin:
        return 'Ban Giám Hiệu';
      case UserRole.systemAdmin:
        return 'Quản Trị Hệ Thống';
    }
  }

  String get code {
    switch (this) {
      case UserRole.teacher:
        return 'TEACHER';
      case UserRole.parent:
        return 'PARENT';
      case UserRole.medicalStaff:
        return 'MEDICAL_STAFF';
      case UserRole.serviceCaterers:
        return 'SERVICE_CATERERS';
      case UserRole.rawFoodSupplier:
        return 'SUPPLIER';
      case UserRole.schoolAdmin:
        return 'SCHOOL_ADMIN';
      case UserRole.systemAdmin:
        return 'SYSTEM_ADMIN';
    }
  }
}

class User {
  final String id;
  final String fullName;
  final String email;
  final UserRole role;

  User({
    required this.id,
    required this.fullName,
    required this.email,
    required this.role,
  });
}
