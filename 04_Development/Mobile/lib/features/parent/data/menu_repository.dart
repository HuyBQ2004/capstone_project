import '../../../core/network/api_client.dart';
import 'menu_models.dart';

String todayIso() {
  final d = DateTime.now();
  String p(int n) => n.toString().padLeft(2, '0');
  return '${d.year}-${p(d.month)}-${p(d.day)}';
}

/// `GET /grades` (MF02, V44) — danh mục khối lớp school-scoped. Đối chiếu
/// `04_Development/Frontend/src/lib/grades.ts`: nhãn khối ("Khối 3") chỉ dùng để
/// HIỂN THỊ, mọi request GHI/LỌC theo khối phải dùng `gradeId` (UUID) — `MenuSpec.FIELD_MAP`
/// (backend, mf05) không nhận nhãn `grade`, gửi nhãn bị 422 MENU-422.
///
/// `studentCode` (11/08/2026, theo Nam) — Parent KHÔNG có `schoolId` trong JWT; server suy
/// trường từ CON đang chọn qua tham số này (`GradeServiceImpl.resolveSchoolId`). Thiếu nó,
/// phụ huynh có con ở ≥2 trường bị suy nhầm về trường ĐẦU TIÊN (không phải trường của con
/// đang xem) — và trước 11/08 còn tệ hơn: `GET /grades` trả gộp khối của MỌI trường, tra
/// nhãn dễ trúng UUID sai nếu 2 trường trùng tên khối. Cache theo TỪNG `studentCode` — cache
/// dùng chung 1 danh sách cho mọi con sẽ phục vụ nhầm khối của trường khác cho con thứ 2.
class GradeRepository {
  GradeRepository._();
  static final GradeRepository instance = GradeRepository._();

  final _api = ApiClient.instance;
  final Map<String, List<Map<String, dynamic>>> _cache = {};

  Future<List<Map<String, dynamic>>> _grades(String studentCode) async {
    final cached = _cache[studentCode];
    if (cached != null) return cached;
    final list = await _api.getList('/grades', query: {'studentCode': studentCode});
    final grades = (list ?? const []).cast<Map<String, dynamic>>();
    _cache[studentCode] = grades;
    return grades;
  }

  /// Nhãn khối ("Khối 3") → UUID **của đúng trường `studentCode` đang học**. Trả `null` nếu
  /// chưa tải xong hoặc không khớp khối nào — nơi gọi phải bỏ qua request thay vì gửi thiếu
  /// `gradeId` (422).
  Future<String?> resolveGradeId(String gradeLabel, String studentCode) async {
    final grades = await _grades(studentCode);
    for (final g in grades) {
      if (g['name'] == gradeLabel) return g['id'] as String?;
    }
    return null;
  }
}

/// `GET /menus?gradeId=&studentCode=&pageSize=` — đối chiếu `04_Development/Frontend/src/app/(app)/parent/weekly-menu/page.tsx`
/// và `.../parent/dashboard/page.tsx` (`usePagedList<PersistedMenu>("menus", { gradeId, studentCode })`).
/// Chỉ lấy thực đơn CHUNG đã công bố của đúng khối con đang học — bỏ qua phần
/// "suất riêng theo nhóm chế độ ăn" (`personalizeBlock`/`useSpecialMeals`, cần
/// thêm `diet-groups` + `special-meals` + thư viện `dishes`) để giữ màn mobile
/// đơn giản, đúng tinh thần "bố cục lại cho hợp mobile" — cảnh báo allergen vẫn
/// hiển thị bằng cách so `manualAllergens` của từng món với dị ứng đã xác minh
/// của con (`ParentChild.allergies`), không cần chiếu suất thay thế đầy đủ.
class ParentMenuRepository {
  ParentMenuRepository._();
  static final ParentMenuRepository instance = ParentMenuRepository._();

  final _api = ApiClient.instance;

  /// `grade` là NHÃN hiển thị ("Khối 3", suy từ `ParentChild.grade`) — được đổi
  /// sang `gradeId` (UUID) ngay trước khi gửi request, cùng quy ước với web
  /// (`useGradeId()`, `lib/grades.ts`). Trước đây gửi thẳng `?grade=Khối 3` khiến
  /// `MenuSpec` (allow-list field) từ chối với 422 MENU-422 "unknown filter field".
  ///
  /// `studentCode` (D1, `ParentChild.id`) — con ĐANG CHỌN; bắt buộc để cả bước tra
  /// `gradeId` lẫn `GET /menus` cùng phạm vi hoá đúng trường của con đó (xem Javadoc
  /// `GradeRepository`).
  Future<List<ParentMenu>> fetchPublishedForGrade(String grade, String studentCode) async {
    final gradeId = await GradeRepository.instance.resolveGradeId(grade, studentCode);
    if (gradeId == null) return const [];

    final json = await _api.get('/menus', query: {
      'gradeId': gradeId,
      'studentCode': studentCode,
      'pageSize': '100',
    });
    final items = (json?['items'] as List?) ?? const [];
    final menus = items
        .map((e) => ParentMenu.fromJson(e as Map<String, dynamic>))
        .where((m) => m.isStandard && m.isLive)
        .toList()
      ..sort((a, b) => (a.menuDateIso ?? '').compareTo(b.menuDateIso ?? ''));
    return menus;
  }

  /// `GET /dishes/{code}` — chi tiết đầy đủ 1 món (nguyên liệu + ảnh + dinh dưỡng), dùng cho
  /// "Xem chi tiết món" khi phụ huynh bấm vào 1 dòng món trong thực đơn tuần (13/08/2026, Nam).
  Future<DishDetail> fetchDishDetail(String dishId) async {
    final json = await _api.get('/dishes/$dishId');
    return DishDetail.fromJson(json!);
  }
}
