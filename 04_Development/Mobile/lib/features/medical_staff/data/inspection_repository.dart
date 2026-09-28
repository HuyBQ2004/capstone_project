import '../../../core/network/api_client.dart';
import 'inspection_models.dart';

/// `checklist-groups` + `kitchen-inspections` (backend, MF07 — UC-014/UC-186).
/// Đối chiếu `04_Development/Frontend/src/app/(app)/medical-staff/conduct-inspection/`.
class InspectionRepository {
  InspectionRepository._();
  static final InspectionRepository instance = InspectionRepository._();

  final _api = ApiClient.instance;

  /// `GET /checklist-groups` — trả THẲNG mảng (danh mục nhỏ, không phân trang).
  Future<List<ChecklistGroup>> fetchChecklistGroups() async {
    final json = await _api.getList('/checklist-groups');
    final groups = (json ?? const [])
        .map((e) => ChecklistGroup.fromJson(e as Map<String, dynamic>))
        .toList()
      ..sort((a, b) => a.order.compareTo(b.order));
    return groups;
  }

  /// `POST /kitchen-inspections` — `SubmitKitchenInspectionRequest` chỉ nhận
  /// đúng 3 field; điểm/kết quả server tự tính lại từ `itemResults` (CF-26).
  ///
  /// 13/08/2026 (Nam) — `staffHealthValid` (như `pestControlValid`) hết là ô tick trên form:
  /// đã có hạng mục checklist riêng đánh giá đúng nội dung "Giấy khám sức khỏe nhân viên bếp"
  /// (tự tạo qua checklist-groups, BR-210) nên ô tick ở đây là trùng lặp. Đối chiếu bản Web
  /// (`conduct-inspection/page.tsx`, sửa 11/08/2026) — payload VẪN gửi cố định `true`, không bỏ
  /// hẳn field: `staffHealthValid` còn được `school-admin/compliance`,
  /// `school-admin/reports/[slug]`, `medical-staff/compliance*` đọc trực tiếp cho báo cáo QĐ
  /// 1246, bỏ khỏi payload sẽ làm phiên mới thiếu field so với dữ liệu cũ.
  Future<KitchenInspection> submit({
    required List<Map<String, dynamic>> itemResults,
  }) async {
    final json = await _api.post('/kitchen-inspections', body: {
      'pestControlValid': true,
      'staffHealthValid': true,
      'itemResults': itemResults,
    });
    return KitchenInspection.fromJson(json!);
  }

  /// `POST /kitchen-inspections/{code}/evidences/{itemCode}` (V68) — ảnh bằng chứng gắn
  /// ĐÚNG hạng mục checklist, chụp trực tiếp qua camera (BR-207, D10-BIS).
  Future<void> uploadEvidence(String code, String itemCode, String filePath) async {
    await _api.uploadFile('/kitchen-inspections/$code/evidences/$itemCode', fieldName: 'file', filePath: filePath);
  }

  Future<List<KitchenInspection>> fetchHistory() async {
    final json = await _api.get('/kitchen-inspections', query: const {'pageSize': '50'});
    final items = (json?['items'] as List?) ?? const [];
    return items
        .map((e) => KitchenInspection.fromJson(e as Map<String, dynamic>))
        .toList()
      ..sort((a, b) => (b.date ?? '').compareTo(a.date ?? ''));
  }

  Future<KitchenInspection> fetchDetail(String code) async {
    final json = await _api.get('/kitchen-inspections/$code');
    return KitchenInspection.fromJson(json!);
  }

  /// `PATCH /kitchen-inspections/{code}/record` (MEDICAL_STAFF only) — sửa nội
  /// dung biên bản tự sinh lúc nộp checklist; khoá sau khi Caterers đã xác
  /// nhận (409 `INSP-STEP_LOCKED`). Đối chiếu
  /// `04_Development/Frontend/src/app/(app)/medical-staff/conduct-inspection/[id]/page.tsx`.
  Future<KitchenInspection> updateRecord(String code, String content) async {
    final json = await _api.patch('/kitchen-inspections/$code/record', body: {'content': content});
    return KitchenInspection.fromJson(json!);
  }
}
