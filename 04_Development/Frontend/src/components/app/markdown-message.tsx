"use client";

import ReactMarkdown from "react-markdown";
import remarkGfm from "remark-gfm";
import { cn } from "@/lib/utils";

/**
 * 14/08/2026 (Nam) — trợ lý AI trả lời bằng Markdown (`**đậm**`, gạch đầu dòng, đôi khi cả
 * bảng) nhưng 2 màn chat đang in text thô nên người dùng thấy nguyên ký tự `**`, `-`, `|`.
 * Component này render đúng Markdown, dùng chung cho widget nổi lẫn trang chat đầy đủ.
 *
 * `remark-gfm` cho bảng/gạch ngang/checkbox — Gemini hay trả bảng so sánh (VD dinh dưỡng theo
 * món), thiếu plugin này thì bảng vẫn hiện thành `|---|---|`.
 *
 * KHÔNG dùng cho tin nhắn NGƯỜI DÙNG gõ: người dùng gõ text thuần, render Markdown ở đó sẽ
 * biến ký tự vô tình (VD `*` trong "2*3") thành định dạng, sai với thứ họ đã gõ.
 */
export function MarkdownMessage({ text, className }: { text: string; className?: string }) {
  return (
    <div
      className={cn(
        // Khoảng cách gọn cho bong bóng chat: bỏ margin mặc định của <p>/<ul>, chỉ chừa
        // giữa các khối; danh sách vẫn giữ bullet/số để đọc đúng cấu trúc câu trả lời.
        "space-y-2 [&_code]:rounded [&_code]:bg-black/10 [&_code]:px-1 [&_code]:py-0.5",
        "[&_ol]:list-decimal [&_ol]:space-y-1 [&_ol]:pl-5 [&_ul]:list-disc [&_ul]:space-y-1 [&_ul]:pl-5",
        "[&_a]:underline [&_h1]:font-semibold [&_h2]:font-semibold [&_h3]:font-semibold",
        "[&_pre]:overflow-x-auto [&_pre]:rounded [&_pre]:bg-black/10 [&_pre]:p-2",
        "[&_table]:w-full [&_table]:border-collapse [&_td]:border [&_td]:px-1.5 [&_td]:py-0.5",
        "[&_th]:border [&_th]:px-1.5 [&_th]:py-0.5 [&_th]:text-left",
        className
      )}
    >
      <ReactMarkdown
        remarkPlugins={[remarkGfm]}
        components={{
          // Link do AI sinh ra là nội dung KHÔNG kiểm soát được — mở tab mới + chặn
          // `window.opener` (tab mới không điều khiển ngược được trang gốc).
          a: ({ ...props }) => <a {...props} target="_blank" rel="noopener noreferrer" />,
        }}
      >
        {text}
      </ReactMarkdown>
    </div>
  );
}
