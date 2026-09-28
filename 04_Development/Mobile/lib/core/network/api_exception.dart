/// Lỗi API đã bóc khỏi envelope D2 (`{ error: { code, message, details } }`).
///
/// Đối chiếu `04_Development/Frontend/src/lib/api/client.ts` (`ApiError`) —
/// giữ đúng field `code`/`message`/`status` để tra nhãn tiếng Việt qua
/// `error_message.md` giống bản web.
class ApiException implements Exception {
  final String? code;
  final String message;
  final List<dynamic> details;
  final int? status;

  ApiException({
    required this.message,
    this.code,
    this.details = const [],
    this.status,
  });

  /// BR-002 — số lần đăng nhập sai còn lại, đính trong `details` của IAM-002.
  int? get attemptsLeft {
    for (final d in details) {
      if (d is Map && d['field'] == 'attemptsLeft') {
        final value = d['value'];
        if (value is int) return value;
      }
    }
    return null;
  }

  factory ApiException.fromResponse(int status, dynamic body) {
    if (body is Map) {
      final error = body['error'];
      if (error is Map) {
        return ApiException(
          code: error['code'] as String?,
          message: (error['message'] as String?) ?? 'Đã có lỗi xảy ra',
          details: (error['details'] as List?) ?? const [],
          status: status,
        );
      }
      final message = body['message'];
      if (message is String) {
        return ApiException(message: message, status: status);
      }
    }
    return ApiException(message: 'Đã có lỗi xảy ra', status: status);
  }

  factory ApiException.network() => ApiException(
        message: 'Không kết nối được máy chủ. Kiểm tra mạng và thử lại.',
      );

  @override
  String toString() => 'ApiException($code, $message)';
}
