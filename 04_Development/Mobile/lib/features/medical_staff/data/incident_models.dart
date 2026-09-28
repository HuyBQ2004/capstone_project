/// `SourceReportResponse` (backend, MF11 — UC-114). Đối chiếu
/// `04_Development/Frontend/src/app/(app)/medical-staff/incidents/page.tsx`.
class SourceReport {
  final String id;
  final String? className;
  final String? description;
  final String? suspectedMealText;
  final String? mealDate;
  final String? reportedAt;
  final String? reportedByLabel;
  final String? status;
  final String? severityLevel;
  final List<String> symptoms;
  final String? draftRecordId;

  SourceReport({
    required this.id,
    required this.className,
    required this.description,
    required this.suspectedMealText,
    required this.mealDate,
    required this.reportedAt,
    required this.reportedByLabel,
    required this.status,
    required this.severityLevel,
    required this.symptoms,
    required this.draftRecordId,
  });

  factory SourceReport.fromJson(Map<String, dynamic> json) => SourceReport(
        id: json['id'] as String? ?? '',
        className: json['className'] as String?,
        description: json['description'] as String?,
        suspectedMealText: json['suspectedMealText'] as String?,
        mealDate: json['mealDate'] as String?,
        reportedAt: json['reportedAt'] as String?,
        reportedByLabel: json['reportedByLabel'] as String?,
        status: json['status'] as String?,
        severityLevel: json['severityLevel'] as String?,
        symptoms: (json['symptoms'] as List?)?.cast<String>() ?? const [],
        draftRecordId: json['draftRecordId'] as String?,
      );
}

/// `IncidentRecordResponse` (backend, MF11 — biên bản sự cố, ADR-M11-1).
class IncidentRecord {
  final String id;
  final List<String> sourceReportCodes;
  final String? content;
  final String? createdAt;
  final String? status;
  final String? incidentId;

  IncidentRecord({
    required this.id,
    required this.sourceReportCodes,
    required this.content,
    required this.createdAt,
    required this.status,
    required this.incidentId,
  });

  factory IncidentRecord.fromJson(Map<String, dynamic> json) => IncidentRecord(
        id: json['id'] as String? ?? '',
        sourceReportCodes: (json['sourceReportCodes'] as List?)?.cast<String>() ?? const [],
        content: json['content'] as String?,
        createdAt: json['createdAt'] as String?,
        status: json['status'] as String?,
        incidentId: json['incidentId'] as String?,
      );
}

/// `IncidentResponse` (backend, MF11 — đủ field cho Medical Staff).
class Incident {
  final String id;
  final String? title;
  final String? status;
  final String? severity;
  final String? reportedAt;
  final String? reportedByLabel;
  final int affectedStudentsCount;
  final List<String> symptoms;
  final String? clinicalRiskAssessment;
  final String? rootCause;
  final String? rootCauseStatus;
  final bool? reportExported;

  Incident({
    required this.id,
    required this.title,
    required this.status,
    required this.severity,
    required this.reportedAt,
    required this.reportedByLabel,
    required this.affectedStudentsCount,
    required this.symptoms,
    required this.clinicalRiskAssessment,
    required this.rootCause,
    required this.rootCauseStatus,
    required this.reportExported,
  });

  factory Incident.fromJson(Map<String, dynamic> json) => Incident(
        id: json['id'] as String? ?? '',
        title: json['title'] as String?,
        status: json['status'] as String?,
        severity: json['severity'] as String?,
        reportedAt: json['reportedAt'] as String?,
        reportedByLabel: json['reportedByLabel'] as String?,
        affectedStudentsCount: (json['affectedStudents'] as List?)?.length ?? 0,
        symptoms: (json['symptoms'] as List?)?.cast<String>() ?? const [],
        clinicalRiskAssessment: json['clinicalRiskAssessment'] as String?,
        rootCause: json['rootCause'] as String?,
        rootCauseStatus: json['rootCauseStatus'] as String?,
        reportExported: json['reportExported'] as bool?,
      );
}

/// `CorrectiveActionResponse` (backend, MF11 — CAPA, BR-152/BR-216) — chỉ phần
/// Medical Staff cần theo dõi từ màn Sự cố (trạng thái + xác nhận khắc phục).
class CorrectiveAction {
  final String id;
  final String? status;
  final String? incidentId;
  final String? description;
  final String? rejectReason;

  CorrectiveAction({
    required this.id,
    required this.status,
    required this.incidentId,
    required this.description,
    required this.rejectReason,
  });

  factory CorrectiveAction.fromJson(Map<String, dynamic> json) => CorrectiveAction(
        id: json['id'] as String? ?? '',
        status: json['status'] as String?,
        incidentId: json['incidentId'] as String?,
        description: json['description'] as String?,
        rejectReason: json['rejectReason'] as String?,
      );
}

const List<String> processingIncidentStatuses = [
  'SEVERITY_PENDING',
  'TREATMENT',
  'INVESTIGATING',
  'CAPA_PENDING',
  'CAPA_APPROVED',
  'VALIDATED',
];
