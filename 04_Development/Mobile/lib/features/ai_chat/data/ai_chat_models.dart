/// `AiDtos.AskRequest`/`AiChatResponse` (backend, MF12 — UC-012/UC-119, `AiController.chat()`).
/// Đối chiếu `04_Development/Frontend/src/app/(app)/ai-chat/page.tsx`.
class AiChatResult {
  final String conversationId;
  final String reply;
  final String? model;
  final bool redacted;

  AiChatResult({required this.conversationId, required this.reply, required this.model, required this.redacted});

  factory AiChatResult.fromJson(Map<String, dynamic> json) => AiChatResult(
        conversationId: json['conversationId'] as String? ?? '',
        reply: json['reply'] as String? ?? '',
        model: json['model'] as String?,
        redacted: json['redacted'] as bool? ?? false,
      );
}
