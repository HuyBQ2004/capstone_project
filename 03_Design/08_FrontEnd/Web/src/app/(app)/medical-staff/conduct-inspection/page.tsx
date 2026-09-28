"use client";

/**
 * 12_ConductFoodSafetyInspectionScreen.md — UC-014 Conduct Food Safety Inspection
 * (Medical Staff). BR-208 (Loại kiểm tra Định kỳ/Đột xuất) đã DEPRECATED 09/07/2026
 * (review_medicalstaff_compliance_kitcheninspection.md Mục 3) — bỏ hẳn bước chọn loại,
 * stepper bắt đầu thẳng từ nhóm checklist đầu tiên. "Lịch sử kiểm tra bếp" tách thành
 * màn riêng (nút ở header — Mục 4 cùng review). Các bước đánh giá Đạt/Không đạt theo
 * từng hạng mục kèm ảnh minh chứng + lý do
 * bắt buộc khi Không đạt và icon "ℹ" chú thích căn cứ pháp lý (BR-207); xác minh
 * chứng nhận kiểm soát côn trùng (BR-102) + khám sức khỏe nhân viên bếp (BR-103).
 * Checklist ĐỘNG (06/07/2026): nhóm/hạng mục/chú thích nạp từ resource
 * "checklist-groups" — Medical Staff tự CRUD qua màn "Quản lý Checklist" (UC-186,
 * BR-210, field 2e), không còn hard-code trong component.
 * Điểm <60% → lưu kết quả "Failed" + tự động gửi cảnh báo tới School Administration
 * (MSG-093, Warning) — bếp TIẾP TỤC vận hành bình thường, KHÔNG chặn phân phát bữa ăn
 * (descope 05/07/2026 so với UC-014 AF1 gốc; không gắn mã BR cho ngưỡng 60%;
 * không dùng MSG-024 — thuộc Incident Management/BR-149. Xem business_rules.md §12).
 *
 * VÒNG 2 (Mục 8.4, chốt 26/07/2026 — áp code 27/07): màn này nay CHỈ là màn TẠO phiên
 * mới; điểm vào chính của UC-014 chuyển sang màn Lịch sử (`conduct-inspection/history`).
 *  · Stepper ngang dạng nút số → `CollapsiblePanel` mỗi nhóm (component dùng chung,
 *    không tạo mới); nút "Tiếp theo (Nhóm x/y)" đóng nhóm hiện tại, mở nhóm kế tiếp.
 *
 * VÒNG 10 (Mục 19.1, 27/07/2026) — SỬA HỒI QUY: cách thực thi "CollapsiblePanel mỗi nhóm"
 * ở trên render CẢ 6 panel cùng lúc, thành một accordion dài chứ không phải wizard như ý
 * định của Mục 8.4. Nay chỉ **mount đúng nhóm đang xem**; label bước là dropdown liệt kê 6
 * nhóm kèm tiến độ, cho nhảy không tuần tự; thêm nút "← Quay lại". Nhảy nhóm chỉ đổi màn
 * đang xem, KHÔNG tính là hoàn thành nhóm — tiến độ vẫn đếm theo số hạng mục thực sự đã
 * bấm Đạt/Không đạt. Luật đánh giá và gate "Hoàn thành phiên" (MSG-016, BR-207) không đổi.
 *  · Nút cuối đổi "Nộp checklist" → "Hoàn thành": phiên chuyển
 *    `AWAITING_CATERERS_CONFIRM` (chờ Service Caterers xác nhận) thay vì đóng ngay.
 *  · Nút "Lịch sử kiểm tra"/"Quản lý Checklist" chuyển sang header màn Lịch sử.
 */

import { useMemo, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Camera, ChevronLeft, ChevronRight, ClipboardCheck, Info } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
/* Vòng 10 Mục 19.1 — `CollapsiblePanel` không còn dùng ở đây: 6 nhóm không còn xổ đồng thời */
import { Select, Textarea } from "@/components/ui/input";
import { useCreate, usePagedList } from "@/lib/api/hooks";
import type { ChecklistGroup, CorrectiveAction, InspectionItemFinding, KitchenInspection } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

interface ItemEvidence {
  photos: number;
  reason: string;
}

export default function ConductInspectionPage() {
  /**
   * Vòng 10 Mục 19.1 (27/07/2026) — WIZARD, không phải accordion.
   *
   * Mục 8.4 điểm 2 chốt "bỏ stepper, mỗi nhóm 1 CollapsiblePanel", nhưng cách thực thi
   * render CẢ 6 panel cùng lúc (chỉ 1 panel mở) — thành một accordion dài, không phải
   * "làm xong nhóm này mới sang nhóm kế". Nay chỉ nhóm đang xem được **mount**; 5 nhóm
   * còn lại không render (khác hẳn việc chỉ đóng bằng state như accordion cũ).
   */
  const [groupIndex, setGroupIndex] = useState(0);
  // undefined = chưa đánh giá; true = Đạt; false = Không đạt (BR-207 field 2a)
  const [checks, setChecks] = useState<Record<string, boolean | undefined>>({});
  // BR-207 field 2b/2c: ảnh + lý do theo từng hạng mục
  const [evidence, setEvidence] = useState<Record<string, ItemEvidence>>({});
  const [openInfo, setOpenInfo] = useState<string | null>(null);
  /*
   * Vòng 9 Mục 17.2 — state `pestControlValid` (ô tick chứng nhận côn trùng) đã bỏ khỏi
   * form. Payload vẫn gửi `pestControlValid: true` cố định: field trên `KitchenInspection`
   * còn được `school-admin/compliance` và `school-admin/reports/[slug]` đọc trực tiếp, bỏ
   * hẳn khỏi payload sẽ làm phiên mới thiếu field so với dữ liệu cũ và gãy màn của actor
   * khác — việc dọn field này thuộc đợt review School Administration.
   */
  const [staffHealthValid, setStaffHealthValid] = useState(true);
  const [lastResult, setLastResult] = useState<KitchenInspection | null>(null);

  const create = useCreate<Record<string, unknown>, KitchenInspection>("kitchen-inspections");
  // review 23/07/2026 — BR-095 ("bước kiểm tra thất bại thì hệ thống tự tạo yêu cầu
  // hành động khắc phục") đã có cho kiểm thực ba bước (service-caterers/inspections)
  // nhưng CHƯA có cho kiểm tra bếp (KIN-xxx): điểm <60% trước đây chỉ cảnh báo, không
  // sinh CAPA nào — Y tế không có việc gì để mở trong màn CAPA cho ca này.
  const createCapa = useCreate<Record<string, unknown>, CorrectiveAction>("corrective-actions");
  // Checklist động (UC-186/BR-210): nạp từ resource, không hard-code
  const { data: clData, isLoading: clLoading } = usePagedList<ChecklistGroup>("checklist-groups", {
    pageSize: 50,
  });
  const checklistGroups = useMemo(
    () => [...(clData?.items ?? [])].sort((a, b) => a.order - b.order),
    [clData]
  );
  // Đánh giá/bằng chứng key theo TÊN hạng mục — phiên đã lưu giữ nguyên dữ liệu khi checklist đổi (BR-210)
  const ALL_ITEMS = useMemo(
    () => checklistGroups.flatMap((g) => g.items.map((i) => i.name)),
    [checklistGroups]
  );
  /* Danh sách STEPS/LAST_STEP của stepper đã bỏ cùng stepper (Mục 8.4 điểm 2) */

  /**
   * Vòng 10 Mục 19.1 — số hạng mục đã đánh giá theo TỪNG nhóm, phục vụ dropdown nhảy bước.
   * Đếm theo hạng mục THỰC SỰ đã bấm Đạt/Không đạt, không theo việc đã "ghé qua" nhóm nào:
   * nhảy bước chỉ đổi màn đang xem, không được tính là hoàn thành nhóm đó.
   */
  const groupProgress = checklistGroups.map((g) => {
    const names = g.items.map((i) => i.name);
    return {
      total: names.length,
      done: names.filter((n) => checks[n] !== undefined).length,
      failed: names.filter((n) => checks[n] === false).length,
    };
  });
  // Chỉ số nhóm luôn nằm trong khoảng hợp lệ kể cả khi checklist động đổi số nhóm (BR-210)
  const safeIndex = Math.min(groupIndex, Math.max(0, checklistGroups.length - 1));
  const currentGroup = checklistGroups[safeIndex];
  const currentProgress = groupProgress[safeIndex];

  const answered = ALL_ITEMS.filter((i) => checks[i] !== undefined).length;
  const passedCount = ALL_ITEMS.filter((i) => checks[i] === true).length;
  const score = useMemo(
    () => (ALL_ITEMS.length === 0 ? 0 : Math.round((passedCount / ALL_ITEMS.length) * 100)),
    [passedCount, ALL_ITEMS]
  );

  const failedItems = ALL_ITEMS.filter((i) => checks[i] === false);
  // BR-207: hạng mục Không đạt bắt buộc ≥1 ảnh + lý do — thiếu thì chặn submit (cảnh báo inline, mã MSG chưa cấp)
  const itemsMissingEvidence = failedItems.filter(
    (i) => (evidence[i]?.photos ?? 0) === 0 || !(evidence[i]?.reason ?? "").trim()
  );
  const submitDisabled =
    answered < ALL_ITEMS.length || itemsMissingEvidence.length > 0 || create.isPending;

  function setCheck(item: string, value: boolean) {
    setChecks((c) => ({ ...c, [item]: value }));
  }

  function addPhoto(item: string) {
    setEvidence((e) => ({
      ...e,
      [item]: { photos: (e[item]?.photos ?? 0) + 1, reason: e[item]?.reason ?? "" },
    }));
  }

  function setReason(item: string, reason: string) {
    setEvidence((e) => ({ ...e, [item]: { photos: e[item]?.photos ?? 0, reason } }));
  }

  function submit() {
    if (submitDisabled) return;
    const findings: InspectionItemFinding[] = failedItems.map((item) => ({
      item,
      photos: evidence[item]?.photos ?? 0,
      reason: (evidence[item]?.reason ?? "").trim(),
    }));
    const photosCount = ALL_ITEMS.reduce((sum, i) => sum + (evidence[i]?.photos ?? 0), 0);
    // BR-208 deprecated 09/07/2026: payload không còn field `type` (Loại kiểm tra)
    create.mutate(
      {
        score,
        inspector: "Ngô Thị Hà",
        // @deprecated — xem chú thích ở khai báo state phía trên (Vòng 9 Mục 17.2)
        pestControlValid: true,
        staffHealthValid,
        failedItems,
        itemFindings: findings,
        photosCount,
        // Mục 8.4 điểm 3 (vòng 2) — KHÔNG đóng ngay: chờ Service Caterers xác nhận
        confirmStatus: "AWAITING_CATERERS_CONFIRM",
        // Snapshot từng hạng mục phục vụ màn xem lại chỉ đọc (Mục 8.4 điểm 1)
        itemResults: ALL_ITEMS.map((item) => ({ item, passed: checks[item] === true })),
      },
      {
        onSuccess: (record) => {
          setLastResult(record);
          if (record.result === "PASSED") {
            toast.success(
              "Đã hoàn thành phiên kiểm tra — chờ Service Caterers xác nhận kết quả trước khi lập biên bản. (MSG-043)"
            );
          } else {
            // Descope 05/07/2026: cảnh báo không chặn vận hành (MSG-093) — nhưng vẫn phải
            // sinh CAPA để theo dõi khắc phục (BR-095), tách biệt với việc không khoá bếp.
            createCapa.mutate({
              inspectionId: record.id,
              source: `${record.id} (kiểm tra bếp ${record.date} — ${record.score}% < 60%)`,
              description:
                record.failedItems && record.failedItems.length > 0
                  ? `Khắc phục các hạng mục không đạt của phiên kiểm tra bếp ${record.id}: ${record.failedItems.join(", ")} (BR-207).`
                  : `Khắc phục các hạng mục không đạt của phiên kiểm tra bếp ${record.id} (BR-207).`,
              assignee: "Cty TNHH Suất ăn An Bình",
              status: "DRAFT",
            });
            toast.warning(
              "Kitchen safety score below 60% threshold. Alert sent to School Administration — kitchen operations continue as normal. Đã tạo yêu cầu hành động khắc phục (CAPA), Y tế soạn kế hoạch tại màn Hành động khắc phục (BR-095/MSG-093)."
            );
          }
        },
      }
    );
  }

  return (
    <>
      <PageHeader
        title="Kiểm tra an toàn bếp (UC-014)"
        description="Đánh giá + bằng chứng theo từng hạng mục (BR-207) · Điểm <60%: lưu Failed + cảnh báo School Administration (MSG-093), bếp vẫn vận hành bình thường"
        actions={
          /* Mục 8.4 điểm 1 (vòng 2): "Lịch sử kiểm tra" + "Quản lý Checklist" chuyển sang
             header màn Lịch sử (điểm vào chính); màn này chỉ còn đường quay lại */
          <Link href="/medical-staff/conduct-inspection/history">
            <Button variant="outline">
              <ArrowLeft /> Về danh sách phiên kiểm tra
            </Button>
          </Link>
        }
      />

      {lastResult && lastResult.score < 60 && (
        <Alert variant="warning" className="mb-4" title="Điểm kiểm tra dưới ngưỡng 60% (MSG-093)">
          Điểm kiểm tra {lastResult.score}% &lt; 60% — đã lưu kết quả Failed, tự động gửi
          cảnh báo tới School Administration và tạo yêu cầu hành động khắc phục (CAPA, BR-095)
          — xem tại{" "}
          <Link href="/medical-staff/corrective-actions" className="underline">
            Hành động khắc phục
          </Link>
          . Bếp tiếp tục vận hành bình thường, không chặn phân phát bữa ăn (descope 05/07/2026
          so với UC-014 AF1 gốc).
        </Alert>
      )}
      {lastResult && lastResult.score >= 60 && (
        <Alert variant="success" className="mb-4" title={`Đã lưu phiên kiểm tra ${lastResult.id}`}>
          Điểm {lastResult.score}% — kết quả Passed, nhật ký kiểm tra bếp đã được ghi (MSG-043).
        </Alert>
      )}

      {/* ═══ Vòng 10 Mục 19.1 — WIZARD: đúng 1 nhóm được mount tại một thời điểm ═══ */}
      <div className="grid gap-4 lg:grid-cols-3">
        <div className="space-y-4 lg:col-span-2">
          {clLoading && <div className="h-24 animate-pulse rounded-lg bg-muted/40" />}
          {currentGroup && (
            <Card>
              <CardHeader className="space-y-2">
                {/*
                  Label bước là DROPDOWN chọn nhóm, không phải tiêu đề tĩnh: cho phép nhảy
                  không tuần tự (VD từ Nhóm 1 sang thẳng Nhóm 3) khi thực tế kiểm tra không
                  đi theo đúng thứ tự checklist. Mỗi option kèm tiến độ riêng để biết nhóm
                  nào còn dở mà không phải mở ra xem.
                */}
                <div className="flex flex-wrap items-center gap-2">
                  <Select
                    aria-label="Chọn nhóm hạng mục"
                    className="w-full max-w-md"
                    value={String(safeIndex)}
                    onChange={(e) => setGroupIndex(Number(e.target.value))}
                  >
                    {checklistGroups.map((g, i) => (
                      <option key={g.id} value={i}>
                        {i + 1} — {g.name} ({groupProgress[i].done}/{groupProgress[i].total})
                      </option>
                    ))}
                  </Select>
                </div>
                <CardTitle className="text-sm font-normal text-muted-foreground">
                  Bước {safeIndex + 1}/{checklistGroups.length} ·{" "}
                  {currentProgress.done}/{currentProgress.total} hạng mục đã đánh giá
                  {currentProgress.failed > 0 && ` · ${currentProgress.failed} không đạt`}
                </CardTitle>
              </CardHeader>
              <CardContent>
                <div className="space-y-2">
                {currentGroup.items.map((it) => {
                  const item = it.name;
                  const failed = checks[item] === false;
                  const ev = evidence[item];
                  const missingPhoto = failed && (ev?.photos ?? 0) === 0;
                  const missingReason = failed && !(ev?.reason ?? "").trim();
                  return (
                    <div key={item} className="space-y-2 rounded-lg border p-2.5 text-sm">
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <span className="flex min-w-0 flex-1 items-center gap-1.5">
                          {item}
                          {/* BR-207 field 2d: icon ℹ chú thích căn cứ pháp lý */}
                          <button
                            type="button"
                            aria-label="Chú thích căn cứ pháp lý"
                            onClick={() => setOpenInfo((o) => (o === item ? null : item))}
                            className="text-muted-foreground hover:text-[var(--primary)]"
                          >
                            <Info className="size-3.5" />
                          </button>
                        </span>
                        <div className="flex gap-1.5">
                          <Button
                            size="sm"
                            variant={checks[item] === true ? "success" : "outline"}
                            onClick={() => setCheck(item, true)}
                          >
                            Đạt
                          </Button>
                          <Button
                            size="sm"
                            variant={checks[item] === false ? "destructive" : "outline"}
                            onClick={() => setCheck(item, false)}
                          >
                            Không đạt
                          </Button>
                        </div>
                      </div>

                      {openInfo === item && (
                        <p className="rounded-md bg-muted/50 p-2 text-xs text-muted-foreground">
                          {/* BR-207/BR-210: chú thích per-item, Medical Staff tự sửa qua UC-186 */}
                          {it.legalNote?.trim()
                            ? it.legalNote
                            : "Chưa có chú thích căn cứ pháp lý cho hạng mục này — thêm qua màn hình Quản lý Checklist (UC-186, BR-210)."}
                        </p>
                      )}

                      {/* BR-207 field 2b: ảnh minh chứng per-item — Đạt: tùy chọn, Không đạt: bắt buộc ≥1 */}
                      <div className="flex flex-wrap items-center gap-2">
                        <Button size="sm" variant="outline" onClick={() => addPhoto(item)}>
                          <Camera /> Ảnh minh chứng ({ev?.photos ?? 0})
                          {failed && <span className="text-destructive">*</span>}
                        </Button>
                        <span className="text-xs text-muted-foreground">
                          GPS + timestamp tự động (mock) · {failed ? "Bắt buộc khi Không đạt" : "Tùy chọn khi Đạt"}
                        </span>
                      </div>
                      {missingPhoto && (
                        <p className="text-xs font-medium text-destructive">
                          Hạng mục Không đạt: bắt buộc ít nhất 1 ảnh gắn đúng hạng mục (BR-207).
                        </p>
                      )}

                      {/* BR-207 field 2c: lý do bắt buộc khi Không đạt, ẩn khi Đạt */}
                      {failed && (
                        <div className="space-y-1">
                          <Textarea
                            value={ev?.reason ?? ""}
                            onChange={(e) => setReason(item, e.target.value)}
                            placeholder="Lý do không đạt (bắt buộc) — có thể tái sử dụng cho Mô tả vi phạm (UC-178)"
                            rows={2}
                          />
                          {missingReason && (
                            <p className="text-xs font-medium text-destructive">
                              Bắt buộc nhập lý do cho hạng mục Không đạt (BR-207).
                            </p>
                          )}
                        </div>
                      )}
                    </div>
                  );
                })}
                </div>

                {/* Điều hướng wizard — "← Quay lại" là phần bổ sung ở Vòng 10 (trước chỉ có
                    "Tiếp theo", nên lỡ sang nhóm sau là không có đường lùi rõ ràng) */}
                <div className="mt-4 flex flex-wrap items-center justify-between gap-2 border-t pt-4">
                  <Button
                    variant="outline"
                    disabled={safeIndex === 0}
                    onClick={() => setGroupIndex(safeIndex - 1)}
                  >
                    <ChevronLeft /> Quay lại
                  </Button>
                  {safeIndex < checklistGroups.length - 1 ? (
                    <Button variant="outline" onClick={() => setGroupIndex(safeIndex + 1)}>
                      Tiếp theo · {safeIndex + 2}/{checklistGroups.length} <ChevronRight />
                    </Button>
                  ) : (
                    <span className="text-xs text-muted-foreground">
                      Nhóm cuối — hoàn tất phiên ở khối bên dưới
                    </span>
                  )}
                </div>
              </CardContent>
            </Card>
          )}

          {/* Bước cuối — xác minh chứng nhận & hoàn thành (không còn là "step" của stepper) */}
          <Card>
              <CardHeader>
                <CardTitle>Xác minh chứng nhận &amp; hoàn thành phiên</CardTitle>
              </CardHeader>
              <CardContent className="space-y-3 text-sm">
                {/*
                  Vòng 9 Mục 17.2 (27/07/2026) — ô tick "Chứng nhận kiểm soát côn trùng quý
                  còn hiệu lực" ĐÃ BỎ. Nó là tàn dư của mô hình BR-102 cũ ("chứng nhận có
                  thời hạn hiệu lực") — mô hình đó đã bị bỏ hai lần: Mục 1 đổi sang "nhật ký
                  sự kiện", rồi Mục 17 bỏ luôn cả nhật ký. Côn trùng nay được đánh giá ngay
                  trong checklist bên trên: CLI-3-8 (Nhóm 3 — dấu vết côn trùng/động vật gây
                  hại) và CLI-4-6 (Nhóm 4 — phương tiện phòng chống), cả hai đều có căn cứ
                  Luật ATTP và đã là mục Đạt/Không đạt bắt buộc của mọi phiên.
                */}
                <label className="flex items-center justify-between gap-2">
                  <span>Giấy khám sức khỏe nhân viên bếp còn hiệu lực (BR-103)</span>
                  <input
                    type="checkbox"
                    checked={staffHealthValid}
                    onChange={(e) => setStaffHealthValid(e.target.checked)}
                    className="size-4 accent-[var(--primary)]"
                  />
                </label>

                {answered < ALL_ITEMS.length && (
                  <Alert variant="warning" title="Checklist chưa đầy đủ (MSG-016)">
                    Còn {ALL_ITEMS.length - answered} hạng mục chưa đánh giá — bắt buộc đánh giá đủ
                    mọi hạng mục trước khi nộp (BR-101).
                  </Alert>
                )}
                {itemsMissingEvidence.length > 0 && (
                  <Alert variant="warning" title="Thiếu bằng chứng tại hạng mục Không đạt (BR-207)">
                    {itemsMissingEvidence.length} hạng mục Không đạt còn thiếu ảnh hoặc lý do:{" "}
                    {itemsMissingEvidence.join("; ")}. Bổ sung tại bước tương ứng để mở khóa nút nộp.
                  </Alert>
                )}
              </CardContent>
          </Card>
        </div>

        <div className="space-y-4">
          <Card>
            <CardHeader>
              <CardTitle>Tiến độ phiên kiểm tra</CardTitle>
            </CardHeader>
            <CardContent className="space-y-2 text-sm">
              {/* Field "Loại kiểm tra" đã xóa — BR-208 deprecated 09/07/2026 */}
              <p>
                Đã đánh giá: {answered}/{ALL_ITEMS.length} hạng mục
              </p>
              <p>
                Điểm tạm tính:{" "}
                <span className={cn("font-semibold", score < 60 ? "text-destructive" : "text-success")}>
                  {score}%
                </span>
              </p>
              {failedItems.length > 0 && (
                <p className="text-xs text-muted-foreground">
                  {failedItems.length} hạng mục Không đạt · {itemsMissingEvidence.length} mục còn
                  thiếu ảnh/lý do (BR-207)
                </p>
              )}
            </CardContent>
          </Card>

          {/* Mục 8.4 điểm 3 — "Hoàn thành" thay "Nộp checklist": phiên chờ Caterers xác nhận */}
          <Button className="w-full" disabled={submitDisabled} onClick={submit} title="MSG-043">
            <ClipboardCheck /> Hoàn thành phiên kiểm tra
          </Button>
          <p className="text-xs text-muted-foreground">
            Nút bị khóa khi: còn hạng mục chưa đánh giá, hoặc hạng mục Không đạt thiếu
            ảnh/lý do (BR-207). Sau khi hoàn thành, phiên chờ <b>Service Caterers xác nhận</b> kết
            quả; xác nhận xong mới lập được biên bản sự việc. Điểm &lt; 60%: lưu Failed + cảnh báo
            School Administration (MSG-093) — bếp vẫn vận hành bình thường.
          </p>
        </div>
      </div>
      {/* Bảng "Lịch sử kiểm tra bếp" đã tách thành màn riêng — nút "Lịch sử kiểm tra" ở header (Mục 4, Nhóm C) */}
    </>
  );
}
