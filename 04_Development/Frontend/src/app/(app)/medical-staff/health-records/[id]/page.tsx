"use client";

/**
 * 04_StudentHealthRecordDetail.md + 05_UpdateHealthRecordScreen.md (Medical Staff).
 * UC-051 bước 4 (clinical file) + UC-094 Update Health Record.
 * BR-031: Y tế xác nhận khai báo · BR-172: lịch sử đã xác minh append-only ·
 * MSG-036 khi lưu thành công.
 *
 * VÒNG 2 (review_medicalstaff_healthrecord.md Mục 4, chốt 26/07/2026 — áp code 27/07):
 *  · Mục 4.2 QĐ 2: thêm khối "Bệnh lý nền đã khai báo" (đọc) cạnh Dị ứng/Chế độ ăn;
 *    cả 3 khối chuyển sang `CollapsiblePanel` dùng chung (không tạo component mới).
 *  · Mục 4.3 điểm 4 (lỗi gate BR-031): `saveEdit()` KHÔNG còn set
 *    `healthStatus: "VERIFIED"` vô điều kiện — cập nhật ghi chú lâm sàng không được
 *    tự xác minh hồ sơ dị ứng; trạng thái xác minh chỉ đổi qua gate Approve/Reject
 *    của UC-077 (nơi có `docsValid()`).
 *  · Mục 4.6 điểm 2 + 4 (bỏ dual-source): modal UC-094 BỎ ô nhập "Dị ứng" và
 *    "Chế độ ăn đặc biệt" — 2 field này thuộc luồng khai báo có gate duyệt riêng
 *    (UC-077 / UC-078), tại đây chỉ hiển thị chỉ đọc + link sang đúng màn duyệt.
 *    UC-094 chỉ còn sửa: Ghi chú lâm sàng · Thuốc cấp cứu · Hướng dẫn khẩn cấp.
 *  · Mục 4.3 điểm 2: nút xem tài liệu mở file THẬT — 12/08/2026 nối vào endpoint tải
 *    xuống thật (`openHealthDocument`), thay `openDocPreview` vốn chỉ vẽ trang giả từ
 *    tên file. `id` của route CHÍNH LÀ code học sinh (D1), dùng luôn làm đoạn đường dẫn.
 */

import { use, useState } from "react";
import Link from "next/link";
import { ArrowLeft, History, Paperclip, Pencil } from "lucide-react";
import { toast } from "sonner";
import { CollapsiblePanel } from "@/components/app/collapsible-panel";
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
import { Input, Label, Textarea } from "@/components/ui/input";
import { useDetail, usePagedList, usePatchAction, useSubDetail } from "@/lib/api/hooks";
import type {
  ChronicConditionDto,
  HealthFileDto,
  SpecialMealRequestDto,
  StudentDto,
} from "@/lib/api/types";
import { latestApprovedMeal, openHealthDocument } from "@/lib/health-docs";
import { SEVERITY_BADGE_VARIANT, SEVERITY_LABEL_VI } from "@/lib/health-severity";
import { formatVnDate } from "@/lib/format";

export default function HealthRecordDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: s, isLoading } = useDetail<StudentDto>("students", id);
  // Hồ sơ LÂM SÀNG (medicalConditions/medications/emergencyContact/medicalDocuments)
  // KHÔNG nằm trên `StudentResponse` thật — tách riêng `GET/PATCH .../health-file`
  // (C8/COND-009), nối thật 05/08/2026 cùng đợt `students`.
  const { data: healthFile } = useSubDetail<HealthFileDto>("students", id, "health-file");
  const patchHealthFile = usePatchAction<HealthFileDto>("students");
  // Vòng 2 — 2 nguồn dữ liệu chính thức thay cho field rời trên Student
  // `chronic-conditions` nối thật 04/08/2026 — xem `lib/health-severity.ts` cho map
  // enum↔nhãn (trước đây đọc mock, `severity` là nhãn tiếng Việt trực tiếp).
  const { data: chronicData } = usePagedList<ChronicConditionDto>("chronic-conditions", {
    pageSize: 200,
  });
  const { data: mealReqData } = usePagedList<SpecialMealRequestDto>("special-meals", { pageSize: 200 });

  const [editOpen, setEditOpen] = useState(false);
  // UC-094 (nối thật 05/08/2026): 4 field lâm sàng ĐÚNG khuôn backend (C8/COND-009 —
  // field lạ 400 STU-094). "Ghi chú lâm sàng" (bản mock cũ) → `medicalConditions` thật
  // (GHI ĐÈ, không phải append tự do — chỉ SỰ KIỆN lịch sử là append-only, server tự
  // ghi "Cập nhật hồ sơ lâm sàng" mỗi lần PATCH, xem `StudentHealthFileServiceImpl.
  // appendClinical`). `emergencyInstructions` tách riêng khỏi `emergencyContact` — mock
  // cũ gộp 1 field, backend có 2.
  const [medicalConditions, setMedicalConditions] = useState("");
  const [medications, setMedications] = useState("");
  const [emergencyContact, setEmergencyContact] = useState("");
  const [emergencyInstructions, setEmergencyInstructions] = useState("");

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!s) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy hồ sơ.{" "}
        <Link className="underline" href="/medical-staff/health-records">Về danh sách</Link>
      </Card>
    );
  }

  function openEdit() {
    if (!s) return;
    setMedicalConditions(healthFile?.medicalConditions ?? "");
    setMedications(healthFile?.medications ?? "");
    setEmergencyContact(healthFile?.emergencyContact ?? "");
    setEmergencyInstructions(healthFile?.emergencyInstructions ?? "");
    setEditOpen(true);
  }

  /**
   * Mục 4.3 điểm 4 (vòng 2) — VÁ LỖI GATE BR-031: bản cũ set `healthStatus: "VERIFIED"`
   * VÔ ĐIỀU KIỆN mỗi lần lưu, nên chỉ cần nhập 1 ghi chú lâm sàng là hồ sơ dị ứng tự
   * chuyển "Đã xác minh" — bỏ qua gate `docsValid()` của UC-077 (Severe bắt buộc có
   * tài liệu y tế). Nay CHỈ gọi `PATCH .../health-file` (đúng 4 field C8), không đụng
   * `healthStatus`/`allergies`/`specialDiet` (những field đó không tồn tại trên DTO này).
   */
  function saveEdit() {
    if (!medicalConditions.trim()) {
      toast.error("Ghi chú lâm sàng là bắt buộc khi cập nhật hồ sơ (UC-094, BR-172).");
      return;
    }
    patchHealthFile.mutate(
      {
        id,
        action: "health-file",
        body: {
          medicalConditions,
          medications,
          emergencyContact,
          emergencyInstructions,
        },
      },
      {
        onSuccess: () => {
          toast.success("Đã cập nhật hồ sơ sức khỏe. (MSG-036)");
          setEditOpen(false);
        },
      }
    );
  }

  // Nguồn chính thức cho 2 khối khai báo còn lại (vòng 2, Mục 4.2 QĐ 2 + 4.6 điểm 4)
  const chronicOf = (chronicData?.items ?? []).filter((c) => c.studentId === id);
  const approvedMeal = latestApprovedMeal(mealReqData?.items, id);

  return (
    <>
      <PageHeader
        title={`Hồ sơ sức khỏe — ${s.name ?? s.id ?? ""}`}
        description={`${s.id} · Lớp ${s.class} · Phụ huynh: ${s.parent ?? "—"}`}
        actions={
          <>
            <Link href="/medical-staff/health-records">
              <Button variant="outline"><ArrowLeft /> Quay lại</Button>
            </Link>
            <Link href={`/medical-staff/health-records/${id}/history`}>
              <Button variant="outline"><History /> Lịch sử sức khỏe (UC-146)</Button>
            </Link>
            <Button onClick={openEdit}><Pencil /> Cập nhật hồ sơ</Button>
          </>
        }
      />

      {s.healthStatus !== "VERIFIED" && (
        <Alert variant="warning" className="mb-4" title="Chưa xác minh (BR-031)">
          Khai báo của phụ huynh chưa có hiệu lực — cần Y tế xác nhận để kích hoạt
          cảnh báo dị ứng khi phục vụ (BR-123).
          {s.allergySeverity === "SEVERE" && !healthFile?.medicalDocuments?.length && (
            <> Mức độ Nặng bắt buộc kèm tài liệu y tế trước khi Approve (BR-031).</>
          )}
        </Alert>
      )}

      <div className="grid gap-4 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          <Card>
            <CardHeader><CardTitle>Tình trạng y tế (UC-051)</CardTitle></CardHeader>
            <CardContent className="space-y-4 text-sm">
              <div className="flex items-center gap-2">
                <span className="text-muted-foreground">Trạng thái xác minh:</span>
                <StatusBadge status={s.healthStatus ?? ""} />
              </div>
              <div>
                <p className="mb-1 text-muted-foreground">Medical conditions:</p>
                <p>{healthFile?.medicalConditions ?? "Chưa ghi nhận."}</p>
              </div>
              <div>
                <p className="mb-1 text-muted-foreground">Thuốc cấp cứu liên quan dị ứng/sự cố:</p>
                <p>{healthFile?.medications ?? "Không có."}</p>
              </div>
              <div>
                <p className="mb-1 text-muted-foreground">Emergency contacts:</p>
                <p>{healthFile?.emergencyContact ?? "Chưa cập nhật."}</p>
              </div>
              <div>
                <p className="mb-1 text-muted-foreground">Hướng dẫn xử lý khẩn cấp:</p>
                <p>{healthFile?.emergencyInstructions ?? "Chưa cập nhật."}</p>
              </div>
            </CardContent>
          </Card>

          {/* Mục 4.2 QĐ 2 (vòng 2) — 3 khối khai báo trình bày dạng Collapsible dùng chung;
              mỗi khối chỉ ĐỌC, mọi thay đổi phải qua đúng màn duyệt của loại đó */}
          <CollapsiblePanel
            title="Dị ứng đã khai báo"
            defaultOpen
            summary={(s.allergies?.length ?? 0) > 0 ? `${s.allergies!.length} tác nhân · ${s.allergySeverity ?? "—"}` : "Không có"}
          >
            <div className="space-y-2 text-sm">
              {(s.allergies?.length ?? 0) > 0 ? (
                <div className="flex flex-wrap items-center gap-1.5">
                  {(s.allergies ?? []).map((a) => (
                    <Badge key={a} variant="danger">{a}</Badge>
                  ))}
                  {s.allergySeverity && (
                    <Badge variant={s.allergySeverity === "SEVERE" ? "critical" : "warning"}>
                      Mức độ: {s.allergySeverity}
                    </Badge>
                  )}
                </div>
              ) : (
                <p>Không có.</p>
              )}
              <p className="text-xs text-muted-foreground" title="BR-031 — chỉ 1 nơi ghi dữ liệu dị ứng">
                Chỉ xem tại đây —{" "}
                <Link className="underline" href="/medical-staff/health-reviews">
                  duyệt/cập nhật tại module Dị ứng
                </Link>
                .
              </p>
            </div>
          </CollapsiblePanel>

          {/* Mục 4.2 QĐ 2 — khối MỚI: bệnh lý nền (nguồn ChronicConditionDeclaration) */}
          <CollapsiblePanel
            title="Bệnh lý nền đã khai báo"
            defaultOpen
            summary={
              chronicOf.length > 0
                ? `${chronicOf.length} khai báo · ${chronicOf.filter((c) => c.status === "VERIFIED").length} đã xác minh`
                : "Không có"
            }
          >
            <div className="space-y-2 text-sm">
              {chronicOf.length === 0 ? (
                <p>Không có.</p>
              ) : (
                chronicOf.map((c) => (
                  <div key={c.id} className="space-y-1 rounded-lg border p-2.5">
                    <div className="flex flex-wrap items-center gap-1.5">
                      <Badge variant="info">{c.category}</Badge>
                      <span className="font-medium">{c.conditionName}</span>
                      {c.severity && (
                        <Badge variant={SEVERITY_BADGE_VARIANT[c.severity]}>
                          {SEVERITY_LABEL_VI[c.severity]}
                        </Badge>
                      )}
                      <StatusBadge status={c.status ?? "PENDING"} />
                    </div>
                    <p className="text-muted-foreground">
                      Khai báo {formatVnDate(c.declaredAt)}
                    </p>
                    {c.notes && <p className="text-muted-foreground">{c.notes}</p>}
                    {c.proofFileId && (
                      <button
                        className="flex items-center gap-1.5 text-xs underline"
                        onClick={() => openHealthDocument(id, c.proofFileId!, c.proofFileName)}
                      >
                        <Paperclip className="size-3.5" /> {c.proofFileName ?? c.proofFileId}
                      </button>
                    )}
                  </div>
                ))
              )}
              <p className="text-xs text-muted-foreground">
                Chỉ xem tại đây —{" "}
                <Link className="underline" href="/medical-staff/health-reviews">
                  duyệt tại module Bệnh lý nền
                </Link>
                .
              </p>
            </div>
          </CollapsiblePanel>

          {/* Mục 4.6 điểm 4 — chế độ ăn đọc từ SpecialMealRequest ĐÃ DUYỆT gần nhất */}
          <CollapsiblePanel
            title="Chế độ ăn đặc biệt"
            defaultOpen
            summary={approvedMeal ? approvedMeal.dietaryType : "Không có"}
          >
            <div className="space-y-2 text-sm">
              {approvedMeal ? (
                <div className="space-y-1">
                  <div className="flex flex-wrap items-center gap-1.5">
                    <Badge variant="info">{approvedMeal.dietaryType}</Badge>
                    <StatusBadge status={approvedMeal.status} />
                    <span className="text-xs text-muted-foreground">
                      {/* D4 — API trả ISO `yyyy-MM-dd`, đổi định dạng ở tầng render */}
                      duyệt theo yêu cầu {approvedMeal.id} · {formatVnDate(approvedMeal.date)}
                    </span>
                  </div>
                  <p className="text-muted-foreground">{approvedMeal.justification}</p>
                  {approvedMeal.documentId && (
                    <button
                      className="flex items-center gap-2 text-xs underline"
                      onClick={() =>
                        openHealthDocument(id, approvedMeal.documentId!, approvedMeal.documentName)
                      }
                    >
                      <Paperclip className="size-3.5" />{" "}
                      {approvedMeal.documentName ?? approvedMeal.documentId}
                    </button>
                  )}
                </div>
              ) : (
                <p>Không có.</p>
              )}
              <p className="text-xs text-muted-foreground" title="BR-066 — nguồn duy nhất là SpecialMealRequest">
                Chỉ xem tại đây —{" "}
                <Link className="underline" href="/medical-staff/special-meals">
                  duyệt tại màn Yêu cầu bữa ăn đặc biệt
                </Link>
                .
              </p>
            </div>
          </CollapsiblePanel>
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader><CardTitle>Tài liệu y tế đính kèm (BR-031)</CardTitle></CardHeader>
            <CardContent className="space-y-2 text-sm">
              {/* 12/08/2026 — `medicalDocuments` nay là {id: mã HDOC, fileName} thay vì UUID thô,
                  nên hiện được TÊN FILE và mở được chính file đó. */}
              {healthFile?.medicalDocuments?.length ? (
                healthFile.medicalDocuments.map((doc) => (
                  <button
                    key={doc.id}
                    className="flex w-full items-center gap-2 rounded-lg border p-2.5 text-left hover:bg-muted"
                    onClick={() => openHealthDocument(id, doc.id!, doc.fileName)}
                  >
                    <Paperclip className="size-4 shrink-0 text-muted-foreground" />
                    <span className="truncate">{doc.fileName ?? doc.id}</span>
                    <span className="ml-auto shrink-0 text-xs text-muted-foreground">Xem</span>
                  </button>
                ))
              ) : (
                <p className="text-muted-foreground">
                  Chưa có tài liệu.
                  {s.allergySeverity === "SEVERE" && " Bắt buộc với dị ứng mức Nặng (BR-031)."}
                </p>
              )}
            </CardContent>
          </Card>

          <Card>
            <CardHeader><CardTitle>Tác động vận hành</CardTitle></CardHeader>
            <CardContent className="space-y-3 text-sm text-muted-foreground">
              <p>• Dị ứng đã xác minh bắt buộc Service Caterers tạo kế hoạch bữa ăn riêng (BR-065), chế biến tách biệt (BR-082).</p>
              <p>• Quét ID học sinh trong phiên phục vụ sẽ kích hoạt cảnh báo trực quan + âm thanh (BR-123).</p>
              <p>• Hồ sơ giữ nguyên xuyên suốt các năm học (BR-194); lịch sử đã xác minh không thể sửa/xóa (BR-172).</p>
            </CardContent>
          </Card>
        </div>
      </div>

      {/* 05_UpdateHealthRecordScreen — UC-094 */}
      <Dialog open={editOpen} onOpenChange={setEditOpen}>
        <DialogContent className="max-h-[85vh] overflow-y-auto">
          <DialogHeader>
            <DialogTitle>Cập nhật hồ sơ sức khỏe (UC-094)</DialogTitle>
            <DialogDescription>
              4 field lâm sàng (C8/COND-009) — mỗi lần lưu server tự ghi 1 sự kiện lịch sử
              append-only &quot;Cập nhật hồ sơ lâm sàng&quot; (BR-172), không sửa/xóa được sau đó.
            </DialogDescription>
          </DialogHeader>
          <div className="space-y-4">
            <div className="rounded-lg bg-muted px-3 py-2 text-sm">
              {s.name} · Lớp {s.class} <span className="text-muted-foreground">(read-only)</span>
            </div>
            {/* Nhóm A §2.4 (09/07/2026): Việt hóa toàn bộ label trong modal UC-094 */}
            <div className="space-y-1.5">
              <Label htmlFor="hr-conditions">Ghi chú lâm sàng / Medical conditions *</Label>
              <Textarea
                id="hr-conditions"
                value={medicalConditions}
                onChange={(e) => setMedicalConditions(e.target.value)}
                placeholder="Ghi chú lâm sàng (bắt buộc — BR-172, ghi đè bản trước, lịch sử thay đổi append-only)"
              />
            </div>
            <div className="space-y-1.5">
              {/* Nhóm A §2.3: thu hẹp phạm vi — không phải nơi khai báo mọi loại thuốc (SFMS ≠ EMR);
                  chỉ thuốc cấp cứu phục vụ BR-063 + Workflow 11 E5 */}
              <Label htmlFor="hr-medications">Thuốc cấp cứu liên quan dị ứng/sự cố</Label>
              <Input
                id="hr-medications"
                value={medications}
                onChange={(e) => setMedications(e.target.value)}
                placeholder="Chỉ ghi thuốc cấp cứu cần dùng khi có phản ứng dị ứng/sự cố ATTP — VD: EpiPen Jr. dự phòng tại phòng y tế"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="hr-emergency-contact">Liên hệ khẩn cấp</Label>
              <Input
                id="hr-emergency-contact"
                value={emergencyContact}
                onChange={(e) => setEmergencyContact(e.target.value)}
                placeholder="VD: Nguyễn Văn Bình (bố) — 0912 345 678"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="hr-emergency-instructions">Hướng dẫn xử lý khẩn cấp</Label>
              <Textarea
                id="hr-emergency-instructions"
                value={emergencyInstructions}
                onChange={(e) => setEmergencyInstructions(e.target.value)}
                placeholder="Hướng dẫn xử trí khẩn cấp khi có phản ứng dị ứng/sự cố"
              />
            </div>
            {/* Mục 4.6 điểm 2 + 4 (vòng 2) — 2 ô nhập "Dị ứng" và "Chế độ ăn đặc biệt" ĐÃ BỎ:
                mỗi loại khai báo chỉ có 1 nơi ghi, qua đúng gate duyệt của nó. Ở đây chỉ
                hiển thị chỉ đọc + link sang màn duyệt tương ứng. */}
            <div className="space-y-2 rounded-lg border bg-muted/40 p-3 text-sm">
              <p className="text-xs text-muted-foreground">Thông tin chỉ đọc tại màn này</p>
              <p className="flex flex-wrap items-center gap-1.5">
                <span className="text-muted-foreground">Dị ứng:</span>
                {(s.allergies?.length ?? 0) > 0 ? (
                  <>
                    {(s.allergies ?? []).map((a) => (
                      <Badge key={a} variant="danger">{a}</Badge>
                    ))}
                    {s.allergySeverity && <Badge variant="warning">{s.allergySeverity}</Badge>}
                  </>
                ) : (
                  <span>Không có</span>
                )}
                <Link className="text-xs underline" href="/medical-staff/health-reviews">
                  Xem/duyệt tại module Dị ứng
                </Link>
              </p>
              <p className="flex flex-wrap items-center gap-1.5">
                <span className="text-muted-foreground">Chế độ ăn đặc biệt:</span>
                {approvedMeal ? <Badge variant="info">{approvedMeal.dietaryType}</Badge> : <span>Không có</span>}
                <Link className="text-xs underline" href="/medical-staff/special-meals">
                  Xem/duyệt tại màn Yêu cầu bữa ăn đặc biệt
                </Link>
              </p>
            </div>

            {/* Nhóm A §2.2 (09/07/2026): BỎ khối "Lịch sử log đã xác minh" khỏi modal —
                tránh trùng lặp UI; xem lịch sử qua nút "Lịch sử sức khỏe (UC-146)" ở màn cha */}
            <p className="text-xs text-muted-foreground">
              Lịch sử đã xác minh (BR-172): xem qua nút &quot;Lịch sử sức khỏe (UC-146)&quot; ở màn hồ sơ.
            </p>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setEditOpen(false)}>Hủy</Button>
            <Button disabled={patchHealthFile.isPending || !medicalConditions.trim()} onClick={saveEdit}>
              Lưu (MSG-036)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
