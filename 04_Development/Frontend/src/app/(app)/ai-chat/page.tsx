"use client";

import { useRef, useState } from "react";
import { Bot, Clock, Send, ShieldCheck, Sparkles, User } from "lucide-react";
import { MarkdownMessage } from "@/components/app/markdown-message";
import { PageHeader } from "@/components/app/page-header";
import { Card } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";
import { useAuthStore } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";

/**
 * UC-012/UC-119 — Trợ lý AI (Gemini RAG, `AiController.chat()`). Phạm vi CHỈ an toàn
 * thực phẩm/dinh dưỡng học đường (siết ở `rag_chain.py` SYSTEM_PROMPT mục 5) — câu hỏi
 * ngoài phạm vi sẽ bị AI từ chối lịch sự, không phải giới hạn phía UI.
 *
 * Sửa 06/08/2026: bản trước dùng `fetch()` trần KHÔNG gắn Bearer token — mọi request
 * đều 401 với backend thật (`AiController` có `@PreAuthorize`), lỗi bị nuốt thành thông
 * báo "lỗi kết nối" chung chung. Đổi sang `api` (axios client dùng chung, tự gắn token +
 * tự refresh) như mọi trang khác, và đổi giao diện sang design token của hệ thống
 * (trước đó hard-code nền slate-900, lệch hẳn theme sáng/tối toàn app).
 */

interface ChatMessage {
  id: string;
  sender: "user" | "ai";
  text: string;
  timestamp: string;
}

/** Khớp `AiDtos.AskRequest`/`AiChatResponse` thật — KHÔNG phải shape `{prompt,module}`/
 *  `{response_text,sources}` của RAG service Python (đó là hợp đồng nội bộ Java↔RAG,
 *  qua `GeminiClient`, không phải hợp đồng FE↔Java). */
interface AiChatResponse {
  conversationId: string;
  reply: string;
  model: string;
  redacted: boolean;
}

function now(): string {
  return new Date().toLocaleTimeString("vi-VN", { hour: "2-digit", minute: "2-digit" });
}

export default function AiChatPage() {
  const role = useAuthStore((s) => s.user?.role);
  const isParent = role === "parent";
  const [messages, setMessages] = useState<ChatMessage[]>([
    {
      id: "msg-1",
      sender: "ai",
      text: isParent
        ? "Xin chào! Tôi là Trợ lý AI An toàn Thực phẩm & Dinh dưỡng Học đường. Tôi chỉ hỗ trợ các câu hỏi trong phạm vi này — bạn có thể hỏi về dị ứng, bữa ăn gần đây của con, hoặc quy định an toàn thực phẩm nói chung."
        : "Xin chào! Tôi là Trợ lý AI An toàn Thực phẩm & Dinh dưỡng Học đường. Tôi chỉ hỗ trợ các câu hỏi trong phạm vi này — bạn cần giúp gì?",
      timestamp: now(),
    },
  ]);
  const [inputPrompt, setInputPrompt] = useState("");
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | undefined>(undefined);
  const scrollRef = useRef<HTMLDivElement>(null);

  async function handleSend(e?: React.FormEvent) {
    if (e) e.preventDefault();
    const userText = inputPrompt.trim();
    if (!userText || loading) return;

    setMessages((prev) => [...prev, { id: `user-${Date.now()}`, sender: "user", text: userText, timestamp: now() }]);
    setInputPrompt("");
    setLoading(true);

    try {
      const { data } = await api.post<AiChatResponse>("/ai/chat", {
        conversationId,
        message: userText,
      });
      setConversationId(data.conversationId);
      setMessages((prev) => [
        ...prev,
        { id: `ai-${Date.now()}`, sender: "ai", text: data.reply || "Đã xử lý câu hỏi của bạn.", timestamp: now() },
      ]);
    } catch (err) {
      setMessages((prev) => [
        ...prev,
        {
          id: `ai-err-${Date.now()}`,
          sender: "ai",
          text: apiErrorMessage(err, "Không thể kết nối tới trợ lý AI — vui lòng thử lại sau."),
          timestamp: now(),
        },
      ]);
    } finally {
      setLoading(false);
      requestAnimationFrame(() => scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" }));
    }
  }

  return (
    <>
      <PageHeader
        title="Trợ lý AI"
        description="Hỏi đáp về an toàn thực phẩm, dinh dưỡng học đường, kiểm thực, lưu mẫu — trợ lý chỉ trả lời trong phạm vi này (BR-157, PDPD)"
      />

      <Card className="flex h-[calc(100vh-14rem)] flex-col overflow-hidden p-0">
        <div className="flex items-center gap-2 border-b bg-muted/30 px-4 py-2.5 text-xs text-muted-foreground">
          <ShieldCheck className="size-3.5 text-success" /> Gemini RAG · Tuân thủ BR-157 & PDPD
        </div>

        <div ref={scrollRef} className="flex-1 space-y-4 overflow-y-auto p-4">
          {messages.map((msg) => (
            <div key={msg.id} className={cn("flex items-start gap-2.5", msg.sender === "user" && "flex-row-reverse")}>
              <div
                className={cn(
                  "flex size-7 shrink-0 items-center justify-center rounded-full",
                  msg.sender === "user" ? "bg-primary text-primary-foreground" : "bg-primary-soft text-primary"
                )}
              >
                {msg.sender === "user" ? <User className="size-4" /> : <Bot className="size-4" />}
              </div>
              <div
                className={cn(
                  "max-w-[80%] rounded-2xl border px-4 py-2.5 text-sm shadow-sm",
                  msg.sender === "user" ? "rounded-tr-sm bg-primary-soft" : "rounded-tl-sm bg-card"
                )}
              >
                {msg.sender === "user" ? (
                  <p className="whitespace-pre-wrap leading-relaxed">{msg.text}</p>
                ) : (
                  <MarkdownMessage text={msg.text} className="leading-relaxed" />
                )}
                <div className="mt-1 flex items-center justify-end gap-1 text-[10px] text-muted-foreground">
                  <Clock className="size-3" /> {msg.timestamp}
                </div>
              </div>
            </div>
          ))}
          {loading && (
            <div className="flex items-center gap-2.5">
              <div className="flex size-7 items-center justify-center rounded-full bg-primary-soft text-primary">
                <Bot className="size-4 animate-pulse" />
              </div>
              <div className="flex items-center gap-1.5 rounded-2xl rounded-tl-sm border bg-card px-4 py-2.5 text-xs text-muted-foreground">
                <Sparkles className="size-3.5 animate-spin" /> Đang truy vấn…
              </div>
            </div>
          )}
        </div>

        <form onSubmit={handleSend} className="flex items-center gap-2 border-t p-3">
          <Input
            value={inputPrompt}
            onChange={(e) => setInputPrompt(e.target.value)}
            placeholder={isParent ? "Hỏi về con bạn (dị ứng, bữa ăn gần đây…) hoặc quy định an toàn thực phẩm…" : "Hỏi về quy định an toàn thực phẩm, lưu mẫu 24h, dị ứng…"}
            className="flex-1"
          />
          <Button type="submit" disabled={!inputPrompt.trim() || loading}>
            <Send /> Gửi
          </Button>
        </form>
      </Card>
    </>
  );
}
