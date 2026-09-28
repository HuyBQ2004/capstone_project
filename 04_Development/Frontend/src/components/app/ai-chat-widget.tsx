"use client";

import { useEffect, useRef, useState } from "react";
import { Bot, Maximize2, Send, ShieldCheck, Sparkles, User, X } from "lucide-react";
import Link from "next/link";
import { MarkdownMessage } from "@/components/app/markdown-message";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { api } from "@/lib/api/client";
import { apiErrorMessage } from "@/lib/messages";
import { useAuthStore } from "@/lib/stores/auth-store";
import { cn } from "@/lib/utils";

/**
 * Trợ lý AI dạng POPUP — nổi ở góc phải dưới, mở được từ MỌI màn trong `(app)`.
 *
 * <p><b>Vì sao đổi khỏi trang riêng.</b> Trước đây trợ lý chỉ là một mục sidebar dẫn sang màn
 * `/ai-chat` chiếm trọn màn hình: muốn hỏi "lưu mẫu bao nhiêu giờ" giữa lúc đang lập thực đơn
 * thì phải RỜI màn đang làm, hỏi xong quay lại và mất ngữ cảnh. Trợ lý là công cụ tra cứu
 * kèm theo công việc, không phải một màn công việc.
 *
 * <p>Trang `/ai-chat` GIỮ NGUYÊN — nó vẫn hữu ích cho hội thoại dài và là đích của link
 * "Mở toàn màn hình" bên dưới. Popup và trang dùng chung đúng một API (`POST /ai/chat`).
 *
 * <p>⚠️ Trước 09/08/2026 mọi câu trả lời đều là chuỗi mẫu ("Đây là phản hồi mẫu (stub) cho…")
 * vì `GeminiClient` có một constructor không tham số cắm cứng `localhost:8001`, khiến Spring
 * bỏ qua `@Value` và không bao giờ gọi tới rag-service. Đã sửa — xem Javadoc ở đó.
 */

interface ChatMessage {
  id: string;
  sender: "user" | "ai";
  text: string;
}

interface AiChatResponse {
  conversationId: string;
  reply: string;
  model: string;
  redacted: boolean;
}

/** Câu gợi ý bấm-là-hỏi — bỏ trang giấy trắng, và cho thấy ngay phạm vi trợ lý phục vụ. */
const QUICK_ASKS = [
  "Mẫu thực phẩm phải lưu tối thiểu bao nhiêu giờ?",
  "Kiểm thực ba bước gồm những bước nào?",
  "Nhiệt độ lõi món ăn chín an toàn là bao nhiêu?",
];

export function AiChatWidget() {
  const status = useAuthStore((s) => s.status);
  const [open, setOpen] = useState(false);
  const [messages, setMessages] = useState<ChatMessage[]>([]);
  const [prompt, setPrompt] = useState("");
  const [loading, setLoading] = useState(false);
  const [conversationId, setConversationId] = useState<string | undefined>();
  const scrollRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  // Đóng bằng Esc — popup che nội dung nên phải thoát được không cần rê chuột.
  useEffect(() => {
    if (!open) return;
    const onKey = (e: KeyboardEvent) => e.key === "Escape" && setOpen(false);
    window.addEventListener("keydown", onKey);
    inputRef.current?.focus();
    return () => window.removeEventListener("keydown", onKey);
  }, [open]);

  /*
   * Nút mở (đóng popup) luôn `fixed bottom-5 right-5` bất kể trang — trang nào có nút hành
   * động rơi đúng góc phải dưới khi cuộn tới (VD service-caterers/inspections/[id]) sẽ bị nút
   * này che mất vì z-40 cao hơn nội dung trang (z-index:auto). Mờ + tắt bắt sự kiện chuột khi
   * đang cuộn, hiện lại sau khi dừng cuộn ~400ms — không che gì trong lúc người dùng thao tác.
   */
  const [scrolling, setScrolling] = useState(false);
  useEffect(() => {
    if (open) return;
    let timer: ReturnType<typeof setTimeout>;
    const onScroll = () => {
      setScrolling(true);
      clearTimeout(timer);
      timer = setTimeout(() => setScrolling(false), 400);
    };
    window.addEventListener("scroll", onScroll, { capture: true, passive: true });
    return () => {
      clearTimeout(timer);
      window.removeEventListener("scroll", onScroll, { capture: true });
    };
  }, [open]);

  useEffect(() => {
    scrollRef.current?.scrollTo({ top: scrollRef.current.scrollHeight, behavior: "smooth" });
  }, [messages, loading]);

  // Chưa đăng nhập thì không có gì để hỏi — `AiController` yêu cầu quyền.
  if (status !== "authenticated") return null;

  async function send(text: string) {
    const userText = text.trim();
    if (!userText || loading) return;
    setMessages((p) => [...p, { id: `u-${Date.now()}`, sender: "user", text: userText }]);
    setPrompt("");
    setLoading(true);
    try {
      const { data } = await api.post<AiChatResponse>("/ai/chat", { conversationId, message: userText });
      setConversationId(data.conversationId);
      setMessages((p) => [
        ...p,
        { id: `a-${Date.now()}`, sender: "ai", text: data.reply || "Đã xử lý câu hỏi của bạn." },
      ]);
    } catch (err) {
      setMessages((p) => [
        ...p,
        {
          id: `e-${Date.now()}`,
          sender: "ai",
          text: apiErrorMessage(err, "Không kết nối được trợ lý AI — thử lại sau."),
        },
      ]);
    } finally {
      setLoading(false);
    }
  }

  if (!open) {
    return (
      <button
        type="button"
        onClick={() => setOpen(true)}
        aria-label="Mở trợ lý AI"
        className={cn(
          "fixed bottom-5 right-5 z-40 flex size-12 items-center justify-center rounded-full bg-primary text-primary-foreground shadow-lg transition-[transform,opacity] hover:scale-105 focus-visible:outline-none focus-visible:ring-2 focus-visible:ring-ring",
          scrolling && "pointer-events-none opacity-30"
        )}
      >
        <Sparkles className="size-5" />
      </button>
    );
  }

  return (
    <div
      role="dialog"
      aria-label="Trợ lý AI"
      /*
       * KHÔNG dùng overlay che toàn màn: đây là công cụ tra cứu KÈM THEO công việc — người
       * dùng cần đọc số liệu trên màn phía sau trong lúc hỏi. Che hết là quay lại đúng vấn đề
       * của trang riêng.
       */
      className="fixed bottom-5 right-5 z-40 flex h-[32rem] max-h-[calc(100vh-2.5rem)] w-[22rem] max-w-[calc(100vw-2.5rem)] flex-col overflow-hidden rounded-xl border bg-card shadow-2xl"
    >
      <div className="flex items-center gap-2 border-b bg-muted/40 px-3 py-2">
        <Sparkles className="size-4 text-primary" />
        <span className="text-sm font-medium">Trợ lý AI</span>
        <Link
          href="/ai-chat"
          className="ml-auto rounded p-1 text-muted-foreground hover:bg-muted hover:text-foreground"
          title="Mở toàn màn hình"
          onClick={() => setOpen(false)}
        >
          <Maximize2 className="size-4" />
        </Link>
        <button
          type="button"
          onClick={() => setOpen(false)}
          aria-label="Đóng trợ lý AI"
          className="rounded p-1 text-muted-foreground hover:bg-muted hover:text-foreground"
        >
          <X className="size-4" />
        </button>
      </div>

      <p className="flex items-center gap-1.5 border-b px-3 py-1.5 text-[11px] text-muted-foreground">
        <ShieldCheck className="size-3 text-success" />
        Chỉ trả lời về an toàn thực phẩm &amp; dinh dưỡng học đường (BR-157, PDPD)
      </p>

      <div ref={scrollRef} className="flex-1 space-y-3 overflow-y-auto p-3">
        {messages.length === 0 && (
          <div className="space-y-2">
            <p className="text-xs text-muted-foreground">Hỏi nhanh:</p>
            {QUICK_ASKS.map((q) => (
              <button
                key={q}
                type="button"
                onClick={() => send(q)}
                className="block w-full rounded-lg border p-2 text-left text-xs hover:bg-muted"
              >
                {q}
              </button>
            ))}
          </div>
        )}
        {messages.map((m) => (
          <div key={m.id} className={cn("flex gap-2", m.sender === "user" && "flex-row-reverse")}>
            <span
              className={cn(
                "flex size-6 shrink-0 items-center justify-center rounded-full",
                m.sender === "ai" ? "bg-primary-soft text-primary" : "bg-muted"
              )}
            >
              {m.sender === "ai" ? <Bot className="size-3.5" /> : <User className="size-3.5" />}
            </span>
            <div
              className={cn(
                "max-w-[85%] rounded-lg px-2.5 py-1.5 text-xs leading-relaxed",
                m.sender === "ai" ? "bg-muted" : "whitespace-pre-line bg-primary text-primary-foreground"
              )}
            >
              {m.sender === "ai" ? <MarkdownMessage text={m.text} /> : m.text}
            </div>
          </div>
        ))}
        {loading && <p className="text-xs text-muted-foreground">Trợ lý đang trả lời…</p>}
      </div>

      <form
        onSubmit={(e) => {
          e.preventDefault();
          send(prompt);
        }}
        className="flex gap-2 border-t p-2"
      >
        <Input
          ref={inputRef}
          value={prompt}
          onChange={(e) => setPrompt(e.target.value)}
          placeholder="Nhập câu hỏi…"
          className="h-9"
        />
        <Button type="submit" size="icon" className="size-9 shrink-0" disabled={loading || !prompt.trim()}>
          <Send className="size-4" />
        </Button>
      </form>
    </div>
  );
}
