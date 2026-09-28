import 'dart:convert';

import 'package:http/http.dart' as http;
import 'package:http_parser/http_parser.dart';

import '../constants/api_constants.dart';
import '../storage/token_storage.dart';
import 'api_exception.dart';

/// Client HTTP dùng chung cho toàn app mobile.
///
/// Đối chiếu `04_Development/Frontend/src/lib/api/client.ts`: cùng base URL
/// `/api/v1`, cùng cơ chế access token trong memory + 401 → refresh (rotate) →
/// replay đúng một lần. Khác ở chỗ refresh token không có cookie httpOnly trên
/// mobile nên phải tự giữ trong [TokenStorage] (Keystore/Keychain).
///
/// 11/08/2026 (theo Nam, đo thật "mobile load API chậm hơn web") — TRƯỚC ĐÂY mọi
/// request dùng hàm tiện ích cấp cao nhất của `package:http` (`http.get()`/`post()`/
/// `patch()`); các hàm này tự tạo MỘT `http.Client()` MỚI rồi đóng ngay sau mỗi lần
/// gọi — nghĩa là mỗi request phải bắt tay TCP+TLS lại từ đầu, không có keep-alive/
/// connection reuse. Web dùng `fetch` của trình duyệt vốn tự giữ 1 kết nối HTTP/2
/// keep-alive cho cả phiên, nên nhanh hơn hẳn — cùng domain, cùng qua Cloudflare
/// Tunnel, chỉ khác tầng vận chuyển. Giữ MỘT [http.Client] sống suốt vòng đời app
/// (đóng khi logout hẳn/app kill) để tái sử dụng kết nối giữa các request.
class ApiClient {
  ApiClient._();
  static final ApiClient instance = ApiClient._();

  /// Giữ sống xuyên suốt — KHÔNG tạo Client mới mỗi request (xem Javadoc lớp).
  final http.Client _client = http.Client();

  /// 13/08/2026 — trước đây KHÔNG có timeout tường minh: nếu server treo (chậm bất
  /// thường, không hẳn là lỗi hẳn) thay vì trả lỗi ngay, request có thể chờ vô thời hạn
  /// theo mặc định hệ điều hành, làm UI treo mãi ở trạng thái loading không rõ nguyên
  /// nhân. `TimeoutException` rơi vào đúng nhánh `catch (_) => ApiException.network()`
  /// đã có sẵn ở mọi chỗ gọi — không cần sửa gì thêm ở call site.
  static const _timeout = Duration(seconds: 20);
  /// Upload ảnh/tệp cần lâu hơn request JSON thường — mạng di động chậm lúc tải lên.
  static const _uploadTimeout = Duration(seconds: 60);

  /// Đường dẫn gọi mô hình ngôn ngữ — CHẬM theo bản chất, không phải do mạng lỗi.
  ///
  /// Đối chiếu `04_Development/Frontend/src/lib/api/client.ts` (`SLOW_AI_PATHS`/
  /// `AI_TIMEOUT_MS`) — đo thật 09/08/2026: một lượt sinh của rag-service mất 11.5s cho
  /// câu hỏi ngắn, vượt 60s cho câu phức tạp. Với `_timeout` 20s dùng chung, MỌI câu hỏi
  /// gửi từ Trợ lý AI mobile đều bị huỷ trước khi có câu trả lời — báo "Không kết nối
  /// được máy chủ" trong khi backend vẫn sinh xong và trả 200 (chỉ là client đã bỏ cuộc).
  static const _aiTimeout = Duration(seconds: 180);
  static const _slowAiPaths = ['/ai/'];
  Duration _timeoutFor(String path) =>
      _slowAiPaths.any((p) => path.contains(p)) ? _aiTimeout : _timeout;

  String? _accessToken;

  /// Các path tự nó là luồng xác thực — 401 ở đây KHÔNG được kích hoạt refresh
  /// (giống `NO_REFRESH_PATHS` bên web).
  static const _noRefreshPaths = [
    '/auth/login',
    '/auth/refresh',
    '/auth/forgot-password',
    '/auth/reset-password',
    '/auth/activate',
  ];

  /// Loại trừ khả năng Cloudflare (Bot Fight Mode/WAF) soi User-Agent mặc định của
  /// `package:http` (`Dart/x.x (dart:io)`) mà chèn thêm bước kiểm tra cho request
  /// không giống trình duyệt.
  static const _headers = {'User-Agent': 'SfmsMobile/1.0 (Flutter)'};

  void setAccessToken(String? token) => _accessToken = token;
  String? get accessToken => _accessToken;

  /// Single-flight refresh — nhiều request 401 cùng lúc chỉ rotate refresh token
  /// đúng một lần (BR-004 v1.9: token cũ bị revoke ngay khi rotate).
  Future<String?>? _refreshInFlight;

  Future<Map<String, dynamic>?> get(String path, {Map<String, String>? query}) async {
    final result = await _request('GET', path, query: query);
    return result as Map<String, dynamic>?;
  }

  Future<Map<String, dynamic>?> post(String path, {Object? body}) async {
    final result = await _request('POST', path, body: body);
    return result as Map<String, dynamic>?;
  }

  Future<Map<String, dynamic>?> patch(String path, {Object? body}) async {
    final result = await _request('PATCH', path, body: body);
    return result as Map<String, dynamic>?;
  }

  Future<Map<String, dynamic>?> delete(String path) async {
    final result = await _request('DELETE', path);
    return result as Map<String, dynamic>?;
  }

  /// Biến thể trả mảng JSON trần (không bọc `{items:...}`) — đối chiếu
  /// `usePlainList`/response kiểu `DistributionResponse[]` bên web
  /// (`lib/api/hooks.ts`): vài resource (VD `class-meal-deliveries`,
  /// `POST /student-distributions/confirm`) trả thẳng mảng, không có
  /// `PagedResponse` bọc ngoài.
  Future<List<dynamic>?> getList(String path, {Map<String, String>? query}) async {
    final result = await _request('GET', path, query: query);
    return result as List<dynamic>?;
  }

  Future<List<dynamic>?> postList(String path, {Object? body}) async {
    final result = await _request('POST', path, body: body);
    return result as List<dynamic>?;
  }

  /// Upload multipart — dùng cho `POST /profile/me/photo` (field `file`).
  /// Không đi qua `_request` (chỉ hỗ trợ JSON) nhưng vẫn tự refresh-retry một
  /// lần khi 401, cùng luật với các request JSON khác.
  Future<Map<String, dynamic>?> uploadFile(
    String path, {
    required String fieldName,
    required String filePath,
    bool retried = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final request = http.MultipartRequest('POST', uri);
    request.headers.addAll(_headers);
    if (_accessToken != null) {
      request.headers['Authorization'] = 'Bearer $_accessToken';
    }
    request.files.add(await http.MultipartFile.fromPath(fieldName, filePath));

    http.StreamedResponse streamed;
    try {
      streamed = await _client.send(request).timeout(_uploadTimeout);
    } catch (_) {
      throw ApiException.network();
    }
    final response = await http.Response.fromStream(streamed);

    dynamic decoded;
    try {
      decoded = response.body.isEmpty ? null : jsonDecode(response.body);
    } catch (_) {
      decoded = null;
    }

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decoded as Map<String, dynamic>?;
    }

    final refreshable = response.statusCode == 401 && !retried;
    if (refreshable) {
      final fresh = await _refreshAccessToken();
      if (fresh != null) {
        return uploadFile(path, fieldName: fieldName, filePath: filePath, retried: true);
      }
    }

    throw ApiException.fromResponse(response.statusCode, decoded);
  }

  /// Multipart request có 1 phần JSON (`@RequestPart("payload")`, đối chiếu
  /// `jsonPart()` bên web, `lib/api/hooks.ts`) + 0..n phần file theo tên field —
  /// dùng cho `POST /sample-lab-requests` (MF08, `sealPhotos[]`/`handoverRecords[]`).
  Future<Map<String, dynamic>?> uploadMultipart(
    String path, {
    required String jsonFieldName,
    required Map<String, dynamic> jsonBody,
    Map<String, List<String>> fileFields = const {},
    bool retried = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path');
    final request = http.MultipartRequest('POST', uri);
    request.headers.addAll(_headers);
    if (_accessToken != null) {
      request.headers['Authorization'] = 'Bearer $_accessToken';
    }
    request.files.add(
      http.MultipartFile.fromString(
        jsonFieldName,
        jsonEncode(jsonBody),
        contentType: MediaType('application', 'json'),
      ),
    );
    for (final entry in fileFields.entries) {
      for (final filePath in entry.value) {
        request.files.add(await http.MultipartFile.fromPath(entry.key, filePath));
      }
    }

    http.StreamedResponse streamed;
    try {
      streamed = await _client.send(request).timeout(_uploadTimeout);
    } catch (_) {
      throw ApiException.network();
    }
    final response = await http.Response.fromStream(streamed);

    dynamic decoded;
    try {
      decoded = response.body.isEmpty ? null : jsonDecode(response.body);
    } catch (_) {
      decoded = null;
    }

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decoded as Map<String, dynamic>?;
    }

    final refreshable = response.statusCode == 401 && !retried;
    if (refreshable) {
      final fresh = await _refreshAccessToken();
      if (fresh != null) {
        return uploadMultipart(
          path,
          jsonFieldName: jsonFieldName,
          jsonBody: jsonBody,
          fileFields: fileFields,
          retried: true,
        );
      }
    }

    throw ApiException.fromResponse(response.statusCode, decoded);
  }

  Future<dynamic> _request(
    String method,
    String path, {
    Map<String, String>? query,
    Object? body,
    bool retried = false,
  }) async {
    final uri = Uri.parse('${ApiConstants.baseUrl}$path')
        .replace(queryParameters: query);

    final headers = {
      ..._headers,
      'Content-Type': 'application/json',
      if (_accessToken != null) 'Authorization': 'Bearer $_accessToken',
    };

    final timeout = _timeoutFor(path);
    http.Response response;
    try {
      switch (method) {
        case 'GET':
          response = await _client.get(uri, headers: headers).timeout(timeout);
          break;
        case 'PATCH':
          response = await _client
              .patch(uri, headers: headers, body: body == null ? null : jsonEncode(body))
              .timeout(timeout);
          break;
        case 'DELETE':
          response = await _client
              .delete(uri, headers: headers, body: body == null ? null : jsonEncode(body))
              .timeout(timeout);
          break;
        default:
          response = await _client
              .post(uri, headers: headers, body: body == null ? null : jsonEncode(body))
              .timeout(timeout);
      }
    } catch (_) {
      throw ApiException.network();
    }

    if (response.statusCode == 204 || response.body.isEmpty) {
      if (response.statusCode >= 200 && response.statusCode < 300) return null;
    }

    dynamic decoded;
    try {
      decoded = response.body.isEmpty ? null : jsonDecode(response.body);
    } catch (_) {
      decoded = null;
    }

    if (response.statusCode >= 200 && response.statusCode < 300) {
      return decoded;
    }

    final refreshable = response.statusCode == 401 &&
        !retried &&
        !_noRefreshPaths.any((p) => path.startsWith(p));

    if (refreshable) {
      final fresh = await _refreshAccessToken();
      if (fresh != null) {
        return _request(method, path, query: query, body: body, retried: true);
      }
    }

    throw ApiException.fromResponse(response.statusCode, decoded);
  }

  Future<String?> _refreshAccessToken() {
    return _refreshInFlight ??= _doRefresh().whenComplete(() {
      _refreshInFlight = null;
    });
  }

  Future<String?> _doRefresh() async {
    final refreshToken = await TokenStorage.instance.readRefreshToken();
    if (refreshToken == null) return null;

    try {
      final response = await _client.post(
        Uri.parse('${ApiConstants.baseUrl}/auth/refresh'),
        headers: {..._headers, 'Content-Type': 'application/json'},
        body: jsonEncode({'refreshToken': refreshToken}),
      );
      if (response.statusCode != 200) {
        await TokenStorage.instance.clear();
        _accessToken = null;
        return null;
      }
      final pair = jsonDecode(response.body) as Map<String, dynamic>;
      final newAccess = pair['accessToken'] as String?;
      final newRefresh = pair['refreshToken'] as String?;
      if (newAccess == null || newRefresh == null) return null;

      _accessToken = newAccess;
      await TokenStorage.instance.saveRefreshToken(newRefresh);
      return newAccess;
    } catch (_) {
      return null;
    }
  }
}
