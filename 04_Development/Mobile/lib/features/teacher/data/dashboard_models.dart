/// `IncidentTeacherResponse` (backend, MF11 — own-class, D11/M11-D15) —
/// riêng cho vai Teacher, KHÔNG có field lâm sàng nhạy cảm của bản
/// `IncidentResponse` đủ field. Đối chiếu `04_Development/Frontend/src/app/(app)/teacher/dashboard/page.tsx`.
class IncidentTeacher {
  final String id;
  final String? title;
  final String? status;
  final String? severity;
  final String? reportedAt;
  final int affectedStudentsCount;

  IncidentTeacher({
    required this.id,
    required this.title,
    required this.status,
    required this.severity,
    required this.reportedAt,
    required this.affectedStudentsCount,
  });

  factory IncidentTeacher.fromJson(Map<String, dynamic> json) => IncidentTeacher(
        id: json['id'] as String,
        title: json['title'] as String?,
        status: json['status'] as String?,
        severity: json['severity'] as String?,
        reportedAt: json['reportedAt'] as String?,
        affectedStudentsCount: (json['affectedStudents'] as List?)?.length ?? 0,
      );
}

/// `SpecialMealRequestResponse` (backend, MF03) — chỉ phần Dashboard cần.
class SpecialMealRequestSummary {
  final String id;
  final String? student;
  final String? dietaryType;
  final String? status;
  final String? classroomId;

  SpecialMealRequestSummary({
    required this.id,
    required this.student,
    required this.dietaryType,
    required this.status,
    required this.classroomId,
  });

  factory SpecialMealRequestSummary.fromJson(Map<String, dynamic> json) => SpecialMealRequestSummary(
        id: json['id'] as String,
        student: json['student'] as String?,
        dietaryType: json['dietaryType'] as String?,
        status: json['status'] as String?,
        classroomId: json['class'] as String?,
      );
}

/// Gộp toàn bộ dữ liệu Bảng tin hôm nay + thẻ thống kê của Dashboard Teacher
/// — mirror `TeacherDashboard` (bản web), không có endpoint tổng hợp riêng,
/// backend chưa có "dashboard summary" API cho vai Teacher.
class TeacherDashboardData {
  final int classStudentsCount;
  final int allergicStudentsCount;
  final List<IncidentTeacher> incidents;
  final int pendingReportsCount;
  final List<SpecialMealRequestSummary> approvedSpecialMeals;
  final int todayDistributed;
  final int todayPlanned;

  TeacherDashboardData({
    required this.classStudentsCount,
    required this.allergicStudentsCount,
    required this.incidents,
    required this.pendingReportsCount,
    required this.approvedSpecialMeals,
    required this.todayDistributed,
    required this.todayPlanned,
  });

  IncidentTeacher? get latestIncident => incidents.isEmpty ? null : incidents.first;
}
