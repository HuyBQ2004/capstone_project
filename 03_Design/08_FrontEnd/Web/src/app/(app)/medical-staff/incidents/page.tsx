"use client";

/**
 * 25_IncidentList.md — REDESIGN 15/07/2026 theo review_medicalstaff_incidentmanagement.md
 * Mục 10 (đảo ngược 1 phần Mục 7.8): luồng "Tạo báo cáo sự cố" 2 bước thay "Xác nhận
 * liên quan ATTP" 1 bước — bước giữa là SOẠN BIÊN BẢN chính thức (spec 33, UC-114 mở rộng).
 * 4 tab: "Chờ tiếp nhận" (RPT PENDING_TRIAGE/DRAFTING, multi-select + nút "Xem chi tiết"
 * modal read-only đúng field 12_IncidentForm — Mục 10.2/9.13) · "Biên bản chờ xử lý"
 * (biên bản COMPLETED, 2 nút Đóng/Mở phiên điều tra — Mục 10.3) · "Đang xử lý" (INC,
 * badge trạng thái chi tiết) · "Đã đóng" (RPT + INC, cột Loại hồ sơ/Lý do đóng).
 * "Mở phiên điều tra": biên bản gắn caseCode INC-xxx, nội dung pre-fill "Tường trình
 * ban đầu" (Mục 10.4); "Đóng — Không liên quan ATTP" giữ 1 dòng/lần (Nhóm F1).
 */

import { useMemo, useState } from "react";
import { useRouter } from "next/navigation";
import { Eye, FilePlus2, FolderSearch, XCircle } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { useCreate, usePagedList, useUpdate } from "@/lib/api/hooks";
import { uniqueAffected, type Incident, type IncidentRecordDraft, type IncidentSourceReport } from "@/lib/mock-data";

/** Nhãn trạng thái chi tiết cho tab "Đang xử lý" (Mục 9.3) */
const PROCESSING_STATUSES = ["SEVERITY_PENDING", "TREATMENT", "INVESTIGATING", "CAPA_PENDING", "CAPA_APPROVED", "VALIDATED"];

export default function MedicalIncidentsPage() {
  const router = useRouter();
  const { data: rptData, refetch: refetchReports } = usePagedList<IncidentSourceReport>("source-reports", { pageSize: 100 });
  const { data: incData } = usePagedList<Incident>("incidents", { pageSize: 100 });
  const { data: recData } = usePagedList<IncidentRecordDraft>("incident-records", { pageSize: 100 });
  const createIncident = useCreate<Record<string, unknown>, Incident>("incidents");
  const createRecord = useCreate<Record<string, unknown>, IncidentRecordDraft>("incident-records");
  const updateReport = useUpdate<{ id: string } & Partial<IncidentSourceReport>>("source-reports");
  const updateRecord = useUpdate<{ id: string } & Partial<IncidentRecordDraft>>("incident-records");

  // `?? []` tạo mảng MỚI mỗi lần render, nên mọi useMemo phụ thuộc nó đều tính lại
  // vô ích — React cảnh báo đúng. Ghim tham chiếu lại.
  const reports = useMemo(() => rptData?.items ?? [], [rptData]);
  const incidents = useMemo(() => incData?.items ?? [], [incData]);
  const records = useMemo(() => recData?.items ?? [], [recData]);

  // Tab 1 — filter theo lớp / triệu chứng / ngày báo cáo (không filter severity — chưa có)
  const [fClass, setFClass] = useState("");
  const [fSymptom, setFSymptom] = useState("");
  const [fDate, setFDate] = useState("");
  const [selected, setSelected] = useState<Record<string, boolean>>({});
  // Design pattern vòng 2 (Mục 5): spec 33 chuyển từ Dialog sang route Detail riêng —
  // state recordOpen/recordDraft/recordReports của dialog cũ đã bỏ.
  const [closeTarget, setCloseTarget] = useState<IncidentSourceReport | null>(null);
  const [closeRecordTarget, setCloseRecordTarget] = useState<IncidentRecordDraft | null>(null);
  // Mục 10.2/9.13 — modal "Xem chi tiết" read-only cho RPT (không có màn riêng)
  const [viewReport, setViewReport] = useState<IncidentSourceReport | null>(null);

  // Tab Chờ tiếp nhận: PENDING_TRIAGE + DRAFTING (badge "Đang soạn biên bản (nháp)")
  const pendingReports = useMemo(() => {
    return reports
      .filter((r) => r.status === "PENDING_TRIAGE" || r.status === "DRAFTING")
      .filter((r) => (fClass ? r.className === fClass : true))
      .filter((r) => (fSymptom ? r.symptoms.includes(fSymptom) : true))
      .filter((r) => {
        if (!fDate) return true;
        const [y, m, d] = fDate.split("-");
        return r.reportedAt.startsWith(`${d}/${m}/${y}`);
      });
  }, [reports, fClass, fSymptom, fDate]);

  const classes = useMemo(() => [...new Set(reports.map((r) => r.className))].sort(), [reports]);
  const symptoms = useMemo(() => [...new Set(reports.flatMap((r) => r.symptoms))].sort(), [reports]);

  const selectedReports = pendingReports.filter((r) => selected[r.id]);
  const selectedCount = selectedReports.length;

  const processing = incidents.filter((i) => PROCESSING_STATUSES.includes(i.status));
  const pendingRecords = records.filter((rec) => rec.status === "COMPLETED");
  const closedRows = [
    ...reports
      .filter((r) => r.status === "CLOSED_NOT_FOOD_SAFETY")
      .map((r) => ({ id: r.id, kind: "Báo cáo (RPT)", title: r.description, when: r.reportedAt, reason: "Không liên quan ATTP" })),
    ...incidents
      .filter((i) => i.status === "CLOSED")
      .map((i) => ({ id: i.id, kind: "Sự cố (INC)", title: i.title, when: i.reportedAt, reason: "Đã hoàn tất điều tra" })),
  ];

  /**
   * Mục 8.2 vòng 3 (19/07/2026) — CHỈ 1 LỐI VÀO TẠO MỚI: checkbox ở List (chỉ tick
   * được PENDING_TRIAGE; dòng DRAFTING ẩn checkbox, "Xem chi tiết" dẫn thẳng vào
   * nháp đang dở). Tick 1 hoặc nhiều dòng → LUÔN tạo 1 nháp MỚI rồi điều hướng
   * sang route Detail — hết ambiguous "nhập vào nháp nào" (Hình 3, Mục 8.2).
   */
  async function openRecordDetail() {
    const fresh = await refetchReports();
    const freshItems = fresh.data?.items ?? [];
    const chosen = selectedReports.map((r) => freshItems.find((f) => f.id === r.id) ?? r);
    if (chosen.some((r) => r.status !== "PENDING_TRIAGE")) {
      toast.error("Chỉ tạo biên bản từ báo cáo Chờ tiếp nhận — báo cáo đang soạn nháp vào bằng \"Xem chi tiết\".");
      setSelected({});
      return;
    }
    // Tạo mới (luôn luôn): pre-fill gợi ý = ghép mô tả các RPT (Mục 9.12), sửa ở Detail
    const pad = (n: number) => String(n).padStart(2, "0");
    const d = new Date();
    const stamp = `${pad(d.getDate())}/${pad(d.getMonth() + 1)}/${d.getFullYear()} ${pad(d.getHours())}:${pad(d.getMinutes())}`;
    const saved = await createRecord.mutateAsync({
      sourceReportIds: chosen.map((r) => r.id),
      content: chosen
        .map((r) => `Theo báo cáo ${r.id} của ${r.reportedBy} lúc ${r.reportedAt}: ${r.description}`)
        .join("<br>"),
      status: "DRAFT",
      createdBy: "Ngô Thị Hà (Y tế)",
      createdAt: stamp,
    });
    for (const r of chosen) {
      await updateReport.mutateAsync({ id: r.id, status: "DRAFTING", draftRecordId: saved.id });
    }
    setSelected({});
    router.push(`/medical-staff/incidents/records/${saved.id}`);
  }

  /** Nhóm F1: đóng RPT đơn lẻ, 1 dòng/lần */
  function confirmCloseReport() {
    if (!closeTarget) return;
    updateReport.mutate(
      { id: closeTarget.id, status: "CLOSED_NOT_FOOD_SAFETY" },
      {
        onSuccess: () => {
          toast.success(`Đã đóng báo cáo ${closeTarget.id} — Không liên quan ATTP.`);
          setCloseTarget(null);
          setSelected({});
        },
      }
    );
  }

  /** Mục 10.3.3 — đóng biên bản: biên bản + toàn bộ RPT nguồn → Không liên quan ATTP */
  async function confirmCloseRecord() {
    if (!closeRecordTarget) return;
    await updateRecord.mutateAsync({ id: closeRecordTarget.id, status: "CLOSED_NOT_FOOD_SAFETY" });
    for (const rid of closeRecordTarget.sourceReportIds) {
      await updateReport.mutateAsync({ id: rid, status: "CLOSED_NOT_FOOD_SAFETY" });
    }
    toast.success(`Đã đóng biên bản ${closeRecordTarget.id} — Không liên quan ATTP.`);
    setCloseRecordTarget(null);
  }

  /** Mục 10.3.4 — Mở phiên điều tra: biên bản gắn caseCode INC-xxx, nội dung pre-fill Tường trình */
  function openInvestigation(rec: IncidentRecordDraft) {
    const sources = reports.filter((r) => rec.sourceReportIds.includes(r.id));
    const students = uniqueAffected(sources.flatMap((r) => r.affectedStudents));
    const symptomUnion = [...new Set(sources.flatMap((r) => r.symptoms))];
    const first = sources[0];
    const code = `INC-2026-${String(15 + incidents.length - 2).padStart(3, "0")}`;
    createIncident.mutate(
      {
        id: code,
        title: `Nghi sự cố ATTP — ${first?.suspectedMealText ?? "chưa rõ món"} (${first?.mealDate ?? "—"})`,
        reportedAt: rec.createdAt,
        reportedBy: `Ngô Thị Hà (Y tế) — biên bản ${rec.id} (${rec.sourceReportIds.join(", ")})`,
        affectedStudents: students,
        severity: "PENDING", // chỉ Medical Staff set qua khối đánh giá gộp (Mục 10.4)
        status: "SEVERITY_PENDING",
        symptoms: symptomUnion,
        sourceReportIds: rec.sourceReportIds,
        suspectedMeal: first ? `${first.suspectedMealText} (${first.mealDate})` : undefined,
        mealDate: first?.mealDate,
        // Mục 10.4: nội dung biên bản → pre-fill "Tường trình ban đầu" (sửa được ở Incident Detail)
        firstAidNotes: rec.content,
        traceabilityMapStatus: "COMPILING",
      },
      {
        onSuccess: async (inc) => {
          for (const rid of rec.sourceReportIds) {
            await updateReport.mutateAsync({ id: rid, status: "MERGED", mergedIntoIncidentId: inc.id });
          }
          await updateRecord.mutateAsync({ id: rec.id, status: "OPENED", incidentId: inc.id });
          toast.success(`Đã mở phiên điều tra ${inc.id} từ biên bản ${rec.id}.`);
          router.push(`/medical-staff/incidents/${inc.id}`);
        },
      }
    );
  }

  return (
    <>
      {/* QĐ 6 vòng 5 (design pattern Mục 12.2): mô tả kỹ thuật cũ ("Luồng 2 bước... BR-146
          là tín hiệu, gộp do Medical Staff chủ động" — Mục 10.3) chuyển vào comment này,
          subtitle dùng câu vận hành; khung nội dung tối đa 1280px. */}
      <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title="Sự cố & Điều tra"
        description="Theo dõi và xử lý các báo cáo nghi ngờ sự cố an toàn thực phẩm"
      />

      <Tabs defaultValue="triage">
        <TabsList>
          <TabsTrigger value="triage">Chờ tiếp nhận ({reports.filter((r) => r.status === "PENDING_TRIAGE" || r.status === "DRAFTING").length})</TabsTrigger>
          {/* Mục 10.3.3 — tab mới chèn giữa */}
          <TabsTrigger value="records">Biên bản chờ xử lý ({pendingRecords.length})</TabsTrigger>
          <TabsTrigger value="processing">Đang xử lý ({processing.length})</TabsTrigger>
          <TabsTrigger value="closed">Đã đóng ({closedRows.length})</TabsTrigger>
        </TabsList>

        {/* ===== Tab 1 — Chờ tiếp nhận ===== */}
        <TabsContent value="triage">
          <Card>
            <CardContent className="pt-5">
              <div className="mb-4 flex flex-wrap items-end gap-2">
                <div className="space-y-1">
                  <Label htmlFor="tf-class" className="text-xs">Lớp</Label>
                  <Select id="tf-class" value={fClass} onChange={(e) => setFClass(e.target.value)} className="w-28">
                    <option value="">Tất cả</option>
                    {classes.map((c) => <option key={c} value={c}>{c}</option>)}
                  </Select>
                </div>
                <div className="space-y-1">
                  <Label htmlFor="tf-symptom" className="text-xs">Triệu chứng</Label>
                  <Select id="tf-symptom" value={fSymptom} onChange={(e) => setFSymptom(e.target.value)} className="w-36">
                    <option value="">Tất cả</option>
                    {symptoms.map((s) => <option key={s} value={s}>{s}</option>)}
                  </Select>
                </div>
                <div className="space-y-1">
                  <Label htmlFor="tf-date" className="text-xs">Ngày báo cáo</Label>
                  <Input id="tf-date" type="date" value={fDate} onChange={(e) => setFDate(e.target.value)} className="w-36" />
                </div>
              </div>

              {pendingReports.length === 0 ? (
                <p className="py-8 text-center text-sm text-muted-foreground">Không có báo cáo nào chờ tiếp nhận.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-8" />
                      <TableHead>Mã</TableHead>
                      <TableHead>Học sinh</TableHead>
                      <TableHead>Lớp</TableHead>
                      <TableHead>Triệu chứng</TableHead>
                      <TableHead>Bữa ăn/món nghi vấn</TableHead>
                      <TableHead>Thời điểm báo cáo</TableHead>
                      <TableHead>Người báo cáo</TableHead>
                      <TableHead>Trạng thái</TableHead>
                      <TableHead />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {pendingReports.map((r) => (
                      <TableRow
                        key={r.id}
                        className={r.status === "PENDING_TRIAGE" ? "cursor-pointer" : undefined}
                        onClick={r.status === "PENDING_TRIAGE" ? () => setSelected((s) => ({ ...s, [r.id]: !s[r.id] })) : undefined}
                      >
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          {/* Mục 8.2 QĐ 1 — dòng DRAFTING ẨN checkbox: đã thuộc 1 nháp,
                              chỉ còn "Xem chi tiết" dẫn thẳng vào nháp đó */}
                          {r.status === "PENDING_TRIAGE" && (
                            <input
                              type="checkbox"
                              checked={Boolean(selected[r.id])}
                              onChange={(e) => setSelected((s) => ({ ...s, [r.id]: e.target.checked }))}
                              className="size-4 accent-[var(--primary)]"
                              aria-label={`Chọn ${r.id}`}
                            />
                          )}
                        </TableCell>
                        <TableCell className="font-medium">{r.id}</TableCell>
                        <TableCell>{r.affectedStudents.map((a) => a.studentName).join(", ") || "—"}</TableCell>
                        <TableCell>{r.className}</TableCell>
                        <TableCell>{r.symptoms.join(", ")}</TableCell>
                        <TableCell className="max-w-44 truncate" title={r.suspectedMealText}>
                          {r.suspectedMealText} <span className="text-xs text-muted-foreground">({r.mealDate})</span>
                        </TableCell>
                        <TableCell>{r.reportedAt}</TableCell>
                        <TableCell className="text-muted-foreground">{r.reportedBy}</TableCell>
                        <TableCell>
                          {r.status === "DRAFTING" ? <StatusBadge status="DRAFTING" /> : <StatusBadge status="PENDING_TRIAGE" />}
                        </TableCell>
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          {/* Mục 10.2/9.13 — PENDING_TRIAGE: modal read-only (KHÔNG có nút tạo
                              trong modal — Mục 8.2 QĐ 2). DRAFTING: dẫn thẳng vào nháp đang dở. */}
                          {r.status === "DRAFTING" && r.draftRecordId ? (
                            <Button size="sm" variant="ghost" onClick={() => router.push(`/medical-staff/incidents/records/${r.draftRecordId}`)}>
                              <Eye /> Xem chi tiết (nháp)
                            </Button>
                          ) : (
                            <Button size="sm" variant="ghost" onClick={() => setViewReport(r)}>
                              <Eye /> Xem chi tiết
                            </Button>
                          )}
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}

              {selectedCount > 0 && (
                <div className="sticky bottom-0 mt-4 flex flex-wrap items-center gap-2 rounded-lg border bg-card p-3 shadow-sm">
                  {/* QĐ 6 vòng 5 — ghi rõ số lượng đã chọn */}
                  <span className="text-sm font-medium">Đã chọn {selectedCount} báo cáo</span>
                  <div className="ml-auto flex gap-2">
                    <Button
                      variant="outline"
                      disabled={selectedCount !== 1 || selectedReports[0]?.status !== "PENDING_TRIAGE"}
                      title="Chỉ đóng từng báo cáo Chờ tiếp nhận một (Nhóm F1); báo cáo đang soạn biên bản không đóng trực tiếp"
                      onClick={() => setCloseTarget(selectedReports[0])}
                    >
                      <XCircle /> Đóng — Không liên quan ATTP
                    </Button>
                    {/* Mục 8.2 QĐ 1 (vòng 3) — lối vào tạo mới DUY NHẤT, luôn tạo nháp MỚI
                        (checkbox chỉ còn trên PENDING_TRIAGE nên không còn nhánh "Tiếp tục soạn") */}
                    <Button onClick={openRecordDetail} disabled={createRecord.isPending}>
                      <FilePlus2 /> Tạo biên bản sự cố
                    </Button>
                  </div>
                </div>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* ===== Tab 2 — Biên bản chờ xử lý (Mục 10.3.3) ===== */}
        <TabsContent value="records">
          <Card>
            <CardContent className="pt-5">
              {pendingRecords.length === 0 ? (
                <p className="py-8 text-center text-sm text-muted-foreground">Chưa có biên bản nào chờ xử lý.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Mã biên bản</TableHead>
                      <TableHead>Báo cáo nguồn</TableHead>
                      <TableHead>Nội dung (trích)</TableHead>
                      <TableHead>Người lập · Lúc</TableHead>
                      <TableHead />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {pendingRecords.map((rec) => (
                      <TableRow key={rec.id}>
                        <TableCell className="font-medium">{rec.id}</TableCell>
                        <TableCell>
                          <div className="flex flex-wrap gap-1">
                            {rec.sourceReportIds.map((rid) => <Badge key={rid} variant="info">{rid}</Badge>)}
                          </div>
                        </TableCell>
                        <TableCell className="max-w-80"><span className="line-clamp-2 text-muted-foreground">{rec.content}</span></TableCell>
                        <TableCell className="text-xs text-muted-foreground">{rec.createdBy} · {rec.updatedAt ?? rec.createdAt}</TableCell>
                        <TableCell>
                          <div className="flex gap-1.5">
                            {/* Mục 5.5 vòng 2 — "Xem chi tiết" điều hướng sang route Detail
                                của biên bản thay vì mở lại modal */}
                            <Button size="sm" variant="ghost" onClick={() => router.push(`/medical-staff/incidents/records/${rec.id}`)}>
                              <Eye /> Xem chi tiết
                            </Button>
                            <Button size="sm" variant="outline" onClick={() => setCloseRecordTarget(rec)}>
                              <XCircle /> Đóng — Không liên quan ATTP
                            </Button>
                            <Button size="sm" disabled={createIncident.isPending} onClick={() => openInvestigation(rec)}>
                              <FolderSearch /> Mở phiên điều tra
                            </Button>
                          </div>
                        </TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* ===== Tab 3 — Đang xử lý ===== */}
        <TabsContent value="processing">
          <Card>
            <CardContent className="pt-5">
              {processing.length === 0 ? (
                <p className="py-8 text-center text-sm text-muted-foreground">Không có phiên điều tra nào đang xử lý.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Mã</TableHead>
                      <TableHead>Sự cố</TableHead>
                      <TableHead>Thời điểm</TableHead>
                      <TableHead>HS ảnh hưởng</TableHead>
                      <TableHead>Triệu chứng</TableHead>
                      <TableHead>Mức độ (BR-147)</TableHead>
                      <TableHead>Trạng thái</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {processing.map((i) => (
                      <TableRow key={i.id} className="cursor-pointer" onClick={() => router.push(`/medical-staff/incidents/${i.id}`)}>
                        <TableCell className="font-medium">{i.id}</TableCell>
                        <TableCell className="max-w-72 truncate">{i.title}</TableCell>
                        <TableCell>{i.reportedAt}</TableCell>
                        <TableCell>
                          {i.affectedStudents.length >= 3 ? (
                            <Badge variant="critical">{i.affectedStudents.length} (BR-146)</Badge>
                          ) : (
                            i.affectedStudents.length
                          )}
                        </TableCell>
                        <TableCell>{i.symptoms.length > 0 ? i.symptoms.join(", ") : "—"}</TableCell>
                        <TableCell>
                          {i.status === "SEVERITY_PENDING" ? (
                            <span className="text-xs text-muted-foreground">Chưa đánh giá</span>
                          ) : (
                            <StatusBadge status={i.severity} />
                          )}
                        </TableCell>
                        <TableCell><StatusBadge status={i.status} /></TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>

        {/* ===== Tab 4 — Đã đóng ===== */}
        <TabsContent value="closed">
          <Card>
            <CardContent className="pt-5">
              {closedRows.length === 0 ? (
                <p className="py-8 text-center text-sm text-muted-foreground">Chưa có hồ sơ nào đã đóng.</p>
              ) : (
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead>Mã</TableHead>
                      <TableHead>Loại hồ sơ</TableHead>
                      <TableHead>Nội dung</TableHead>
                      <TableHead>Thời điểm</TableHead>
                      <TableHead>Lý do đóng</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {closedRows.map((row) => (
                      <TableRow
                        key={row.id}
                        className={row.kind === "Sự cố (INC)" ? "cursor-pointer" : undefined}
                        onClick={row.kind === "Sự cố (INC)" ? () => router.push(`/medical-staff/incidents/${row.id}`) : undefined}
                      >
                        <TableCell className="font-medium">{row.id}</TableCell>
                        <TableCell><Badge variant={row.kind === "Sự cố (INC)" ? "info" : "neutral"}>{row.kind}</Badge></TableCell>
                        <TableCell className="max-w-80 truncate" title={row.title}>{row.title}</TableCell>
                        <TableCell>{row.when}</TableCell>
                        <TableCell className="text-muted-foreground">{row.reason}</TableCell>
                      </TableRow>
                    ))}
                  </TableBody>
                </Table>
              )}
            </CardContent>
          </Card>
        </TabsContent>
      </Tabs>
      </div>

      {/* Spec 33 nay là ROUTE DETAIL riêng (/incidents/records/[id]) — dialog cũ đã
          deprecated (design pattern vòng 2, Mục 5.5) */}

      {/* Mục 10.2 — modal "Xem chi tiết" RPT: đúng và CHỈ các field của Teacher/12_IncidentForm.
          Mục 11.2 (đợt 2): phóng to theo pattern Dialog cỡ lớn, bố cục 2 cột. */}
      <Dialog open={viewReport !== null} onOpenChange={(o) => !o && setViewReport(null)}>
        <DialogContent className="max-w-3xl">
          {viewReport && (
            <>
              <DialogHeader>
                {/* QĐ 6 vòng 5 — bỏ ghi chú kỹ thuật ("RPT không có màn hình riêng — Mục
                    9.13/10.2") khỏi modal, chỉ giữ nội dung báo cáo */}
                <DialogTitle>Chi tiết báo cáo {viewReport.id}</DialogTitle>
                <DialogDescription>
                  Báo cáo của {viewReport.reportedBy} lúc {viewReport.reportedAt} (chỉ đọc)
                </DialogDescription>
              </DialogHeader>
              <div className="grid grid-cols-1 gap-x-6 gap-y-3 text-sm md:grid-cols-2">
                <div className="space-y-3">
                  <p><b>Học sinh liên quan:</b> {viewReport.affectedStudents.map((a) => a.studentName).join(", ")}</p>
                  <p><b>Lớp:</b> {viewReport.className}</p>
                  <p><b>Số học sinh ảnh hưởng:</b> {viewReport.affectedStudents.length}</p>
                  <p><b>Món ăn nghi vấn:</b> {viewReport.suspectedMealText}</p>
                  <p><b>Ngày ăn:</b> {viewReport.mealDate}</p>
                  <p><b>Người báo cáo:</b> {viewReport.reportedBy}</p>
                  <p><b>Thời điểm báo cáo:</b> {viewReport.reportedAt}</p>
                  <p>
                    <b>Mức độ nghiêm trọng:</b>{" "}
                    {viewReport.severityLevel ? (
                      <StatusBadge status={viewReport.severityLevel} />
                    ) : (
                      <span className="text-muted-foreground">Chưa đánh giá (chỉ Medical Staff đánh giá — BR-147)</span>
                    )}
                  </p>
                </div>
                <div className="space-y-3">
                  <div>
                    <b>Triệu chứng phát hiện:</b>
                    <div className="mt-1 flex flex-wrap gap-1">
                      {viewReport.symptoms.map((s) => <Badge key={s} variant="warning">{s}</Badge>)}
                    </div>
                  </div>
                  <div>
                    <b>Mô tả sự cố:</b>
                    <p className="mt-1 rounded-lg bg-muted p-3">{viewReport.description}</p>
                  </div>
                  <div>
                    <b>Ảnh đính kèm:</b>
                    {viewReport.evidencePhotos > 0 ? (
                      <div className="mt-1 flex flex-wrap gap-2">
                        {Array.from({ length: viewReport.evidencePhotos }).map((_, i) => (
                          <div key={i} className="flex size-16 items-center justify-center rounded-md border bg-muted text-xs text-muted-foreground">Ảnh {i + 1}</div>
                        ))}
                      </div>
                    ) : (
                      <span className="text-muted-foreground"> Không có</span>
                    )}
                  </div>
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setViewReport(null)}>Đóng</Button>
              </DialogFooter>
            </>
          )}
        </DialogContent>
      </Dialog>

      {/* Confirm đóng RPT đơn lẻ (Nhóm F1 — 1 dòng/lần) */}
      <Dialog open={closeTarget !== null} onOpenChange={(o) => !o && setCloseTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xác nhận đóng báo cáo này?</DialogTitle>
            <DialogDescription>
              {closeTarget?.id} — {closeTarget?.description} · Đóng ở mức báo cáo đơn lẻ, không tạo
              biên bản/phiên điều tra (E3).
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCloseTarget(null)}>Hủy</Button>
            <Button variant="destructive" disabled={updateReport.isPending} onClick={confirmCloseReport}>
              Đóng — Không liên quan ATTP
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Confirm đóng biên bản (Mục 10.3.3) */}
      <Dialog open={closeRecordTarget !== null} onOpenChange={(o) => !o && setCloseRecordTarget(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Đóng biên bản {closeRecordTarget?.id}?</DialogTitle>
            <DialogDescription>
              Toàn bộ báo cáo nguồn ({closeRecordTarget?.sourceReportIds.join(", ")}) sẽ chuyển
              &quot;Đã đóng — Không liên quan ATTP&quot;; không tạo phiên điều tra.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCloseRecordTarget(null)}>Hủy</Button>
            <Button variant="destructive" disabled={updateRecord.isPending} onClick={confirmCloseRecord}>
              Đóng — Không liên quan ATTP
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
