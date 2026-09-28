import '../../../core/network/api_client.dart';
import 'ai_chat_models.dart';

/// `POST /ai/chat` — trợ lý AI (Gemini RAG). Dùng CHUNG cho mọi vai trò
/// (`AiController.chat()` cho phép cả 7 role) — không có bản riêng theo role,
/// đối chiếu `04_Development/Frontend/src/app/(app)/ai-chat/page.tsx`.
class AiChatRepository {
  AiChatRepository._();
  static final AiChatRepository instance = AiChatRepository._();

  final _api = ApiClient.instance;

  Future<AiChatResult> send({required String message, String? conversationId}) async {
    final json = await _api.post('/ai/chat', body: {
      if (conversationId != null) 'conversationId': conversationId,
      'message': message,
    });
    return AiChatResult.fromJson(json!);
  }
}
