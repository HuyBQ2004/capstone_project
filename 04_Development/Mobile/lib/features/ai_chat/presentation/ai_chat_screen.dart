import 'package:flutter/material.dart';
import 'package:gpt_markdown/gpt_markdown.dart';

import '../../../core/network/api_exception.dart';
import '../../../core/theme/app_theme.dart';
import '../data/ai_chat_repository.dart';

class _ChatMessage {
  final String id;
  final bool fromUser;
  final String text;
  final DateTime at;
  _ChatMessage({required this.id, required this.fromUser, required this.text, required this.at});
}

String _timeLabel(DateTime d) {
  String p(int n) => n.toString().padLeft(2, '0');
  return '${p(d.hour)}:${p(d.minute)}';
}

/// Trợ lý AI (Gemini RAG, UC-012/UC-119) — MÀN CHUNG cho mọi vai trò, đối chiếu
/// `04_Development/Frontend/src/app/(app)/ai-chat/page.tsx`.
///
/// 13/08/2026 (Nam) — trước đây mobile chỉ có sẵn hằng số `ApiConstants.aiChat`
/// (`core/constants/api_constants.dart`) nhưng KHÔNG MÀN NÀO gọi tới nó — hằng số
/// chết, người dùng mobile không hỏi được trợ lý AI dù backend đã có đầy đủ (7 vai
/// trò đều được phép qua `AiController.chat()`). Phạm vi trả lời CHỈ an toàn thực
/// phẩm/dinh dưỡng học đường — siết ở SYSTEM_PROMPT phía server (BR-157), không phải
/// giới hạn UI.
class AiChatScreen extends StatefulWidget {
  const AiChatScreen({super.key});

  @override
  State<AiChatScreen> createState() => _AiChatScreenState();
}

class _AiChatScreenState extends State<AiChatScreen> {
  final _inputCtrl = TextEditingController();
  final _scrollCtrl = ScrollController();
  final List<_ChatMessage> _messages = [
    _ChatMessage(
      id: 'greeting',
      fromUser: false,
      text: 'Xin chào! Tôi là Trợ lý AI An toàn Thực phẩm & Dinh dưỡng Học đường. '
          'Tôi chỉ hỗ trợ các câu hỏi trong phạm vi này — bạn cần giúp gì?',
      at: DateTime.now(),
    ),
  ];
  String? _conversationId;
  bool _sending = false;

  @override
  void dispose() {
    _inputCtrl.dispose();
    _scrollCtrl.dispose();
    super.dispose();
  }

  void _scrollToEnd() {
    WidgetsBinding.instance.addPostFrameCallback((_) {
      if (!_scrollCtrl.hasClients) return;
      _scrollCtrl.animateTo(_scrollCtrl.position.maxScrollExtent, duration: const Duration(milliseconds: 200), curve: Curves.easeOut);
    });
  }

  Future<void> _send() async {
    final text = _inputCtrl.text.trim();
    if (text.isEmpty || _sending) return;
    setState(() {
      _messages.add(_ChatMessage(id: 'u-${DateTime.now().microsecondsSinceEpoch}', fromUser: true, text: text, at: DateTime.now()));
      _inputCtrl.clear();
      _sending = true;
    });
    _scrollToEnd();
    try {
      final result = await AiChatRepository.instance.send(message: text, conversationId: _conversationId);
      setState(() {
        _conversationId = result.conversationId;
        _messages.add(_ChatMessage(
          id: 'a-${DateTime.now().microsecondsSinceEpoch}',
          fromUser: false,
          text: result.reply.isEmpty ? 'Đã xử lý câu hỏi của bạn.' : result.reply,
          at: DateTime.now(),
        ));
      });
    } on ApiException catch (e) {
      setState(() {
        _messages.add(_ChatMessage(id: 'err-${DateTime.now().microsecondsSinceEpoch}', fromUser: false, text: e.message, at: DateTime.now()));
      });
    } catch (_) {
      setState(() {
        _messages.add(_ChatMessage(
          id: 'err-${DateTime.now().microsecondsSinceEpoch}',
          fromUser: false,
          text: 'Không thể kết nối tới trợ lý AI — vui lòng thử lại sau.',
          at: DateTime.now(),
        ));
      });
    } finally {
      if (mounted) setState(() => _sending = false);
      _scrollToEnd();
    }
  }

  @override
  Widget build(BuildContext context) {
    return Scaffold(
      appBar: AppBar(title: const Text('Trợ lý AI')),
      body: SafeArea(
        child: Column(
          children: [
            Container(
              width: double.infinity,
              padding: const EdgeInsets.symmetric(horizontal: 16, vertical: 8),
              color: AppTheme.cardDark,
              child: const Row(
                children: [
                  Icon(Icons.verified_user_outlined, size: 14, color: AppTheme.successEmerald),
                  SizedBox(width: 6),
                  Expanded(
                    child: Text(
                      'Gemini RAG · chỉ trả lời an toàn thực phẩm & dinh dưỡng học đường (BR-157)',
                      style: TextStyle(fontSize: 11, color: Colors.white54),
                    ),
                  ),
                ],
              ),
            ),
            Expanded(
              child: ListView.builder(
                controller: _scrollCtrl,
                padding: const EdgeInsets.all(16),
                itemCount: _messages.length + (_sending ? 1 : 0),
                itemBuilder: (context, index) {
                  if (index == _messages.length) return const _TypingBubble();
                  return _MessageBubble(message: _messages[index]);
                },
              ),
            ),
            SafeArea(
              top: false,
              child: Padding(
                padding: const EdgeInsets.all(12),
                child: Row(
                  children: [
                    Expanded(
                      child: TextField(
                        controller: _inputCtrl,
                        minLines: 1,
                        maxLines: 4,
                        textInputAction: TextInputAction.send,
                        onSubmitted: (_) => _send(),
                        decoration: const InputDecoration(
                          hintText: 'Hỏi về an toàn thực phẩm, dinh dưỡng, kiểm thực, lưu mẫu…',
                          border: OutlineInputBorder(),
                          isDense: true,
                        ),
                      ),
                    ),
                    const SizedBox(width: 8),
                    IconButton.filled(
                      onPressed: _sending ? null : _send,
                      icon: _sending
                          ? const SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2, color: Colors.white))
                          : const Icon(Icons.send),
                    ),
                  ],
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

class _MessageBubble extends StatelessWidget {
  final _ChatMessage message;
  const _MessageBubble({required this.message});

  @override
  Widget build(BuildContext context) {
    final isUser = message.fromUser;
    return Padding(
      padding: const EdgeInsets.only(bottom: 12),
      child: Row(
        mainAxisAlignment: isUser ? MainAxisAlignment.end : MainAxisAlignment.start,
        crossAxisAlignment: CrossAxisAlignment.end,
        children: [
          if (!isUser) ...[
            const CircleAvatar(radius: 14, backgroundColor: AppTheme.primarySky, child: Icon(Icons.smart_toy_outlined, size: 15, color: Colors.white)),
            const SizedBox(width: 8),
          ],
          Flexible(
            child: Container(
              padding: const EdgeInsets.symmetric(horizontal: 14, vertical: 10),
              decoration: BoxDecoration(
                color: isUser ? AppTheme.primarySky.withValues(alpha: 0.18) : AppTheme.cardDark,
                border: Border.all(color: AppTheme.borderDark),
                borderRadius: BorderRadius.only(
                  topLeft: const Radius.circular(14),
                  topRight: const Radius.circular(14),
                  bottomLeft: Radius.circular(isUser ? 14 : 2),
                  bottomRight: Radius.circular(isUser ? 2 : 14),
                ),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                mainAxisSize: MainAxisSize.min,
                children: [
                  // 14/08/2026 (Nam) — trợ lý AI trả lời bằng Markdown (`**đậm**`, gạch đầu
                  // dòng, đôi khi cả bảng); in `Text` thô làm người dùng thấy nguyên ký tự
                  // `**`/`-`/`|`. Chỉ render Markdown cho tin nhắn CỦA AI — tin người dùng gõ
                  // giữ nguyên text thuần, tránh ký tự vô tình (VD `2*3`) bị hiểu thành định dạng.
                  if (isUser)
                    Text(message.text, style: const TextStyle(fontSize: 14, height: 1.4))
                  else
                    GptMarkdown(message.text, style: const TextStyle(fontSize: 14, height: 1.4)),
                  const SizedBox(height: 4),
                  Text(_timeLabel(message.at), style: const TextStyle(fontSize: 10, color: Colors.white38)),
                ],
              ),
            ),
          ),
          if (isUser) ...[
            const SizedBox(width: 8),
            const CircleAvatar(radius: 14, backgroundColor: AppTheme.successEmerald, child: Icon(Icons.person, size: 15, color: Colors.white)),
          ],
        ],
      ),
    );
  }
}

class _TypingBubble extends StatelessWidget {
  const _TypingBubble();

  @override
  Widget build(BuildContext context) {
    return const Padding(
      padding: EdgeInsets.only(bottom: 12),
      child: Row(
        children: [
          CircleAvatar(radius: 14, backgroundColor: AppTheme.primarySky, child: Icon(Icons.smart_toy_outlined, size: 15, color: Colors.white)),
          SizedBox(width: 8),
          SizedBox(width: 16, height: 16, child: CircularProgressIndicator(strokeWidth: 2)),
          SizedBox(width: 10),
          Text('Đang truy vấn…', style: TextStyle(fontSize: 12, color: Colors.white54)),
        ],
      ),
    );
  }
}
