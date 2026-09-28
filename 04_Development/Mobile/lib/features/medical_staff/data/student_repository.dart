import '../../../core/network/api_client.dart';
import 'student_models.dart';

/// `GET /students` — dùng chung cho Dashboard (đếm khai báo chờ duyệt), màn
/// Sự cố (chọn học sinh ảnh hưởng khi mở phiên điều tra) và tra tên/lớp ở
/// màn Duyệt khai báo sức khỏe. Backend không lọc theo trường ở endpoint này
/// (giống bản web `usePagedList<StudentDto>("students", { pageSize: 300 })`).
class MedicalStudentRepository {
  MedicalStudentRepository._();
  static final MedicalStudentRepository instance = MedicalStudentRepository._();

  final _api = ApiClient.instance;

  Future<List<MedicalStudent>> fetchAll() async {
    final json = await _api.get('/students', query: const {'pageSize': '300'});
    final items = (json?['items'] as List?) ?? const [];
    return items.map((e) => MedicalStudent.fromJson(e as Map<String, dynamic>)).toList();
  }
}
