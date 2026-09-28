"use client";

import { useState } from "react";
import { BookMarked, Plus, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select, Textarea } from "@/components/ui/input";
import { useQueryClient } from "@tanstack/react-query";
import { api } from "@/lib/api/client";
import { useCreate, useDelete, usePatch, usePlainList } from "@/lib/api/hooks";
import type { CreateKnowledgeDocumentDto, KnowledgeDocumentDto } from "@/lib/api/types";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDateTime } from "@/lib/format";
import { cn } from "@/lib/utils";

/**
 * Kho tri thức AI — nguồn RAG bền vững (Postgres, `ai_knowledge_documents`) thay cho
 * `KNOWLEDGE_BASE` in-memory của rag_service (mất khi container restart). Tài liệu
 * `active` được backend gửi kèm mỗi lượt gọi RAG (`GeminiClient.complete`), ưu tiên
 * hơn việc rag_service tự khớp từ khoá trong 4 đoạn cứng nội bộ của nó.
 */

const CATEGORY_LABEL: Record<string, string> = {
  ATTP_REGULATION: "Quy định ATTP",
  NUTRITION_STANDARD: "Tiêu chuẩn dinh dưỡng",
  SCHOOL_POLICY: "Chính sách trường học",
  OTHER: "Khác",
};

/**
 * Nội dung tài liệu — MẶC ĐỊNH THU GỌN.
 *
 * <p>Trước 09/08/2026 chỗ này in nguyên văn `content`. Hồi kho tri thức còn là vài đoạn gõ tay
 * thì không sao; từ khi nạp được file thật (`00_Policies/`) thì một tài liệu là **122.000 ký
 * tự** — trang Kho tri thức dài vô tận, không cuộn nổi tới tài liệu thứ hai.
 *
 * <p>Hiện 600 ký tự đầu + **số ký tự thật** (để biết bóc được bao nhiêu, thứ quyết định chất
 * lượng RAG) và cho mở rộng khi cần đối chiếu. Phần mở rộng có `max-h` + cuộn riêng, không đẩy
 * dài trang.
 */
function DocumentBody({ content }: { content: string }) {
  const [expanded, setExpanded] = useState(false);
  const PREVIEW = 600;
  const isLong = content.length > PREVIEW;

  return (
    <div className="space-y-2">
      <p
        className={cn(
          "whitespace-pre-wrap text-sm text-muted-foreground",
          expanded && "max-h-96 overflow-y-auto rounded-md border p-3"
        )}
      >
        {expanded || !isLong ? content : content.slice(0, PREVIEW).trimEnd() + "…"}
      </p>
      {isLong && (
        <div className="flex items-center gap-3">
          <Button size="sm" variant="outline" onClick={() => setExpanded((v) => !v)}>
            {expanded ? "Thu gọn" : "Xem toàn văn"}
          </Button>
          <span className="text-xs text-muted-foreground">
            {content.length.toLocaleString("vi-VN")} ký tự đã bóc được
          </span>
        </div>
      )}
    </div>
  );
}

function AddDocumentDialog({ open, onOpenChange }: { open: boolean; onOpenChange: (v: boolean) => void }) {
  const create = useCreate<CreateKnowledgeDocumentDto, KnowledgeDocumentDto>("ai/knowledge-documents", { handledError: true });
  const [title, setTitle] = useState("");
  const [category, setCategory] = useState("ATTP_REGULATION");
  const [content, setContent] = useState("");
  const [error, setError] = useState("");
  const [file, setFile] = useState<File | null>(null);
  const [uploading, setUploading] = useState(false);
  const queryClient = useQueryClient();

  function reset() {
    setTitle("");
    setCategory("ATTP_REGULATION");
    setContent("");
    setError("");
    setFile(null);
  }

  /*
   * Nạp bằng FILE thay vì bắt dán tay. Tài liệu thật (Nghị định 15/2018, QĐ 1246/QĐ-BYT) là
   * PDF/DOCX vài chục trang — mở file, bôi đen, dán vào Textarea là rào cản đủ lớn để không ai
   * làm, và kho tri thức rỗng thì trợ lý TỪ CHỐI trả lời câu nghiệp vụ (`SYSTEM_PROMPT` buộc
   * chỉ dựa trên tài liệu tham chiếu — đo 09/08/2026 với mô hình local).
   *
   * Server bóc văn bản bằng PDFBox/POI (`POST /ai/knowledge-documents/upload`) chứ KHÔNG parse ở
   * trình duyệt: hai thư viện đó đã có sẵn trong backend, còn thêm bộ parse PDF vào bundle web
   * là thêm phụ thuộc nặng cho một màn hình mà chỉ System Admin mở.
   */
  async function submitFile() {
    if (!file) return;
    setError("");
    setUploading(true);
    try {
      const form = new FormData();
      form.append("file", file);
      if (title.trim()) form.append("title", title.trim());
      form.append("category", category);
      await api.post("/ai/knowledge-documents/upload", form);
      await queryClient.invalidateQueries({ queryKey: ["ai/knowledge-documents"] });
      toast.success(`Đã nạp "${file.name}" — AI dùng ngay từ lượt hỏi tiếp theo.`);
      onOpenChange(false);
      reset();
    } catch (err) {
      setError(apiErrorMessage(err, "Không nạp được file."));
    } finally {
      setUploading(false);
    }
  }

  function submit() {
    if (file) {
      void submitFile();
      return;
    }
    if (!title.trim() || !content.trim()) {
      setError("Tiêu đề và nội dung là bắt buộc.");
      return;
    }
    setError("");
    create.mutate(
      { title: title.trim(), category, content: content.trim() },
      {
        onSuccess: () => {
          toast.success("Đã thêm tài liệu tri thức — AI sẽ dùng ngay từ lượt hỏi tiếp theo.");
          onOpenChange(false);
          reset();
        },
        onError: (err) => setError(apiErrorMessage(err, "Không thể thêm tài liệu.")),
      }
    );
  }

  return (
    <Dialog open={open} onOpenChange={(v) => { onOpenChange(v); if (!v) reset(); }}>
      <DialogContent className="sm:max-w-lg">
        <DialogHeader>
          <DialogTitle>Thêm tài liệu tri thức</DialogTitle>
          <DialogDescription>
            Nội dung sẽ được gửi kèm làm ngữ cảnh cho mọi câu trả lời của AI (RAG) — viết
            ngắn gọn, chính xác, đúng phạm vi an toàn thực phẩm/dinh dưỡng học đường.
          </DialogDescription>
        </DialogHeader>
        {error && <Alert variant="danger" title="Không thể lưu">{error}</Alert>}
        <div className="space-y-4">
          <div className="space-y-1.5">
            <Label htmlFor="kb-title">Tiêu đề *</Label>
            <Input id="kb-title" value={title} onChange={(e) => setTitle(e.target.value)} placeholder="VD: Quy định lưu mẫu thực phẩm 24h" />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="kb-category">Phân loại *</Label>
            <Select id="kb-category" value={category} onChange={(e) => setCategory(e.target.value)}>
              {Object.entries(CATEGORY_LABEL).map(([value, label]) => (
                <option key={value} value={value}>{label}</option>
              ))}
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="kb-file">Nạp từ file (tuỳ chọn)</Label>
            <Input
              id="kb-file"
              type="file"
              accept=".pdf,.docx,.txt,.md,.csv"
              onChange={(e) => {
                setFile(e.target.files?.[0] ?? null);
                setError("");
              }}
            />
            <p className="text-xs text-muted-foreground">
              .pdf · .docx · .txt · .md · .csv — tối đa 10MB. Server tự bóc văn bản; bỏ trống tiêu
              đề thì lấy tên file. <strong>PDF bản scan (ảnh) sẽ bị từ chối</strong> vì không bóc
              được chữ.
            </p>
          </div>
          {/* Chọn file rồi thì ô nội dung thành thừa — ẩn hẳn để khỏi phải đoán cái nào thắng. */}
          {!file && (
            <div className="space-y-1.5">
              <Label htmlFor="kb-content">Nội dung *</Label>
              <Textarea
                id="kb-content"
                rows={6}
                value={content}
                onChange={(e) => setContent(e.target.value)}
                placeholder="Trích dẫn nguyên văn điều khoản/quy định — AI trả lời dựa trên đúng nội dung này."
              />
            </div>
          )}
        </div>
        <DialogFooter>
          <Button variant="outline" onClick={() => onOpenChange(false)}>Hủy</Button>
          <Button disabled={create.isPending || uploading} onClick={submit}>
            {uploading ? "Đang bóc văn bản…" : file ? `Nạp "${file.name}"` : "Lưu tài liệu"}
          </Button>
        </DialogFooter>
      </DialogContent>
    </Dialog>
  );
}

export default function AiKnowledgePage() {
  const { data: documents = [], isLoading } = usePlainList<KnowledgeDocumentDto>("ai/knowledge-documents");
  const toggleActive = usePatch<{ id: string; active: boolean }, KnowledgeDocumentDto>("ai/knowledge-documents");
  const deleteDoc = useDelete("ai/knowledge-documents");
  const [open, setOpen] = useState(false);
  const [deleteTarget, setDeleteTarget] = useState<KnowledgeDocumentDto | null>(null);

  return (
    <>
      <PageHeader
        title="Kho tri thức AI"
        description="Tài liệu đang bật (Đang dùng) được gửi kèm mỗi lượt AI trả lời — nguồn lưu bền trong Postgres, không mất khi khởi động lại"
        actions={
          <Button onClick={() => setOpen(true)}>
            <Plus /> Thêm tài liệu
          </Button>
        }
      />

      {!isLoading && documents.length === 0 && (
        <Alert variant="info" title="Chưa có tài liệu tri thức nào">
          AI hiện chỉ trả lời dựa trên kiến thức nền sẵn có của model — thêm tài liệu để AI
          trả lời đúng theo quy định/hướng dẫn riêng của hệ thống.
        </Alert>
      )}

      <div className="space-y-3">
        {documents.map((d) => (
          <Card key={d.id}>
            <CardHeader className="flex-row items-start justify-between gap-2">
              <div>
                <CardTitle className="flex items-center gap-2 text-base">
                  <BookMarked className="size-4 text-primary" />
                  {d.title}
                </CardTitle>
                <div className="mt-1 flex items-center gap-2">
                  <Badge variant="info">{CATEGORY_LABEL[d.category ?? ""] ?? d.category}</Badge>
                  <StatusBadge status={d.active ? "ACTIVE" : "INACTIVE"} />
                  <span className="text-xs text-muted-foreground">
                    {formatVnDateTime(d.createdAt)}{d.createdBy ? ` · ${d.createdBy}` : ""}
                  </span>
                </div>
              </div>
              <div className="flex shrink-0 items-center gap-2">
                <Button
                  size="sm"
                  variant="outline"
                  onClick={() =>
                    toggleActive.mutate(
                      { id: d.id ?? "", active: !d.active },
                      {
                        onSuccess: () => toast.success(d.active ? "Đã tắt — AI không dùng tài liệu này nữa." : "Đã bật — AI dùng ngay từ lượt hỏi tiếp theo."),
                      }
                    )
                  }
                >
                  {d.active ? "Tắt" : "Bật"}
                </Button>
                <Button size="sm" variant="ghost" aria-label="Xoá" onClick={() => setDeleteTarget(d)}>
                  <Trash2 className="text-destructive" />
                </Button>
              </div>
            </CardHeader>
            <CardContent>
              <DocumentBody content={d.content ?? ""} />
            </CardContent>
          </Card>
        ))}
      </div>

      <AddDocumentDialog open={open} onOpenChange={setOpen} />

      <Dialog open={deleteTarget !== null} onOpenChange={(v) => !v && setDeleteTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xoá tài liệu tri thức</DialogTitle>
            <DialogDescription>
              {deleteTarget && (
                <>
                  Xoá &quot;{deleteTarget.title}&quot; — AI sẽ không còn dùng nội dung này để trả lời. Không thể hoàn tác.
                </>
              )}
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setDeleteTarget(null)}>Hủy</Button>
            <Button
              variant="destructive"
              disabled={deleteDoc.isPending}
              onClick={() => {
                if (!deleteTarget) return;
                deleteDoc.mutate(deleteTarget.id ?? "", {
                  onSuccess: () => {
                    toast.success("Đã xoá tài liệu.");
                    setDeleteTarget(null);
                  },
                });
              }}
            >
              Xoá
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
