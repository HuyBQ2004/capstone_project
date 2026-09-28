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
 *
 * Nối thật 04/08/2026 (Bước 3 MF11): backend làm ATOMIC mọi bước từng phải điều phối
 * tay ở đây — `POST /incident-records` tự chuyển RPT nguồn sang DRAFTING,
 * `POST .../open-investigation` tự tạo Incident + gộp RPT + gắn REC, không còn client
 * tự gọi 2-3 mutation nối tiếp như bản mock. Điểm khác duy nhất cần thêm UI: mở phiên
 * điều tra nay đòi `title` + `affectedStudentCodes` — 2 field KHÔNG suy ra được từ RPT
 * (RPT không lưu học sinh có cấu trúc, chỉ còn trong `description` tự do từ 27/07/2026)
 * — thêm dialog nhập 2 field này thay vì suy diễn ngầm như bản cũ.
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
import { Alert } from "@/components/ui/alert";
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
import { useAction, useCreate, usePagedList, useAllPages } from "@/lib/api/hooks";
import type {
  CreateRecordDto,
  IncidentDto,
  IncidentRecordDto,
  OpenInvestigationDto,
  SourceReportDto,
} from "@/lib/api/types";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import type { StudentDto } from "@/lib/api/types";

/** Nhãn trạng thái chi tiết cho tab "Đang xử lý" (Mục 9.3) */
const PROCESSING_STATUSES = ["SEVERITY_PENDING", "TREATMENT", "INVESTIGATING", "CAPA_PENDING", "CAPA_APPROVED", "VALIDATED"];

export default function MedicalIncidentsPage() {
  const router = useRouter();
  const { data: rptData } = usePagedList<SourceReportDto>("source-reports", { pageSize: 100 });
  const { data: incData } = usePagedList<IncidentDto>("incidents", { pageSize: 100 });
  const { data: recData } = usePagedList<IncidentRecordDto>("incident-records", { pageSize: 100 });
  // Datalist chọn học sinh khi mở phiên điều tra (nối thật 05/08/2026).
  const { data: studentsPage } = useAllPages<StudentDto>("students", { pageSize: 200 });

  const createRecord = useCreate<CreateRecordDto, IncidentRecordDto>("incident-records");
  const reportAction = useAction<SourceReportDto>("source-reports");
  const recordAction = useAction<IncidentRecordDto>("incident-records");
  const openInvestigationAction = useAction<{ incidentCode: string }>("incident-records");

  const reports = useMemo(() => rptData?.items ?? [], [rptData]);
  const incidents = useMemo(() => incData?.items ?? [], [incData]);
  const records = useMemo(() => recData?.items ?? [], [recData]);
  const allStudents = useMemo(() => studentsPage?.items ?? [], [studentsPage]);

  // Tab 1 — filter theo lớp / triệu chứng / ngày báo cáo (không filter severity — chưa có)
  const [fClass, setFClass] = useState("");
  const [fDate, setFDate] = useState("");
  const [selected, setSelected] = useState<Record<string, boolean>>({});
  const [closeTarget, setCloseTarget] = useState<SourceReportDto | null>(null);
  const [closeRecordTarget, setCloseRecordTarget] = useState<IncidentRecordDto | null>(null);
  // Mục 10.2/9.13 — modal "Xem chi tiết" read-only cho RPT (không có màn riêng)
  const [viewReport, setViewReport] = useState<SourceReportDto | null>(null);
  // Nối thật — dialog "Mở phiên điều tra": title + affectedStudentCodes bắt buộc, không
  // suy ra được từ RPT (khác bản mock cũ tự động điền).
  const [oiTarget, setOiTarget] = useState<IncidentRecordDto | null>(null);
  const [oiTitle, setOiTitle] = useState("");
  const [oiStudentSearch, setOiStudentSearch] = useState("");
  const [oiStudents, setOiStudents] = useState<string[]>([]);
  const [oiError, setOiError] = useState("");

  // Tab Chờ tiếp nhận: PENDING_TRIAGE + DRAFTING (badge "Đang soạn biên bản (nháp)")
  const pendingReports = useMemo(() => {
    return reports
      .filter((r) => r.status === "PENDING_TRIAGE" || r.status === "DRAFTING")
      .filter((r) => (fClass ? r.className === fClass : true))
      .filter((r) => {
        if (!fDate || !r.reportedAt) return true;
        return r.reportedAt.startsWith(fDate);
      });
  }, [reports, fClass, fDate]);

  const classes = useMemo(
    () => [...new Set(reports.map((r) => r.className).filter((c): c is string => Boolean(c)))].sort(),
    [reports]
  );

  const selectedReports = pendingReports.filter((r) => r.id && selected[r.id]);
  const selectedCount = selectedReports.length;

  const processing = incidents.filter((i) => i.status && PROCESSING_STATUSES.includes(i.status));
  const pendingRecords = records.filter((rec) => rec.status === "COMPLETED");
  const closedRows = [
    ...reports
      .filter((r) => r.status === "CLOSED_NOT_FOOD_SAFETY")
      .map((r) => ({ id: r.id!, kind: "Báo cáo (RPT)", title: r.description ?? "", when: r.reportedAt, reason: "Không liên quan ATTP" })),
    ...incidents
      .filter((i) => i.status === "CLOSED")
      .map((i) => ({ id: i.id!, kind: "Sự cố (INC)", title: i.title ?? "", when: i.reportedAt, reason: "Đã hoàn tất điều tra" })),
  ];

  /**
   * Mục 8.2 vòng 3 (19/07/2026) — CHỈ 1 LỐI VÀO TẠO MỚI: checkbox ở List (chỉ tick
   * được PENDING_TRIAGE; dòng DRAFTING ẩn checkbox, "Xem chi tiết" dẫn thẳng vào
   * nháp đang dở). Tick 1 hoặc nhiều dòng → LUÔN tạo 1 nháp MỚI rồi điều hướng
   * sang route Detail — hết ambiguous "nhập vào nháp nào" (Hình 3, Mục 8.2).
   *
   * Nối thật — MỘT lệnh gọi (`POST /incident-records`) đủ: backend tự chuyển các RPT
   * nguồn sang DRAFTING trong cùng transaction (`IncidentRecordServiceImpl.create`).
   */
  async function openRecordDetail() {
    if (selectedReports.some((r) => r.status !== "PENDING_TRIAGE")) {
      toast.error("Chỉ tạo biên bản từ báo cáo Chờ tiếp nhận — báo cáo đang soạn nháp vào bằng \"Xem chi tiết\".");
      setSelected({});
      return;
    }
    const saved = await createRecord.mutateAsync({
      sourceReportCodes: selectedReports.map((r) => r.id!),
      content: selectedReports
        .map((r) => `Theo báo cáo ${r.id} của ${r.reportedByLabel} lúc ${formatVnDateTime(r.reportedAt)}: ${r.description}`)
        .join("<br>"),
    });
    setSelected({});
    router.push(`/medical-staff/incidents/records/${saved.id}`);
  }

  /** Nhóm F1: đóng RPT đơn lẻ, 1 dòng/lần */
  function confirmCloseReport() {
    if (!closeTarget) return;
    reportAction.mutate(
      { id: closeTarget.id!, action: "close-not-food-safety" },
      {
        onSuccess: () => {
          toast.success(`Đã đóng báo cáo ${closeTarget.id} — Không liên quan ATTP.`);
          setCloseTarget(null);
          setSelected({});
        },
      }
    );
  }

  /** Mục 10.3.3 — đóng biên bản: backend tự đóng toàn bộ RPT nguồn cùng transaction. */
  function confirmCloseRecord() {
    if (!closeRecordTarget) return;
    recordAction.mutate(
      { id: closeRecordTarget.id!, action: "close-not-food-safety" },
      {
        onSuccess: () => {
          toast.success(`Đã đóng biên bản ${closeRecordTarget.id} — Không liên quan ATTP.`);
          setCloseRecordTarget(null);
        },
      }
    );
  }

  function openOiDialog(rec: IncidentRecordDto) {
    setOiTarget(rec);
    setOiTitle("");
    setOiStudentSearch("");
    setOiStudents([]);
    setOiError("");
  }

  function toggleOiStudent(id: string) {
    setOiStudents((prev) => (prev.includes(id) ? prev.filter((x) => x !== id) : [...prev, id]));
  }

  /** Mục 10.3.4 — Mở phiên điều tra: MỘT lệnh atomic tạo Incident + gộp RPT + gắn REC. */
  function submitOpenInvestigation() {
    if (!oiTarget) return;
    if (!oiTitle.trim() || oiStudents.length === 0) {
      setOiError("Cần tiêu đề sự cố và ít nhất 1 học sinh bị ảnh hưởng.");
      return;
    }
    setOiError("");
    const body: OpenInvestigationDto = {
      title: oiTitle.trim(),
      affectedStudentCodes: oiStudents,
    };
    openInvestigationAction.mutate(
      { id: oiTarget.id!, action: "open-investigation", body },
      {
        onSuccess: (res) => {
          toast.success(`Đã mở phiên điều tra ${res.incidentCode} từ biên bản ${oiTarget.id}.`);
          setOiTarget(null);
          router.push(`/medical-staff/incidents/${res.incidentCode}`);
        },
      }
    );
  }

  const oiStudentCandidates = allStudents.filter(
    (s) =>
      oiStudentSearch === "" ||
      (s.name ?? "").toLowerCase().includes(oiStudentSearch.toLowerCase()) ||
      (s.id ?? "").toLowerCase().includes(oiStudentSearch.toLowerCase())
  );

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
                      <TableHead>Lớp</TableHead>
                      <TableHead>Mô tả</TableHead>
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
                        onClick={r.status === "PENDING_TRIAGE" ? () => setSelected((s) => ({ ...s, [r.id!]: !s[r.id!] })) : undefined}
                      >
                        <TableCell onClick={(e) => e.stopPropagation()}>
                          {/* Mục 8.2 QĐ 1 — dòng DRAFTING ẨN checkbox: đã thuộc 1 nháp,
                              chỉ còn "Xem chi tiết" dẫn thẳng vào nháp đó */}
                          {r.status === "PENDING_TRIAGE" && (
                            <input
                              type="checkbox"
                              checked={Boolean(selected[r.id!])}
                              onChange={(e) => setSelected((s) => ({ ...s, [r.id!]: e.target.checked }))}
                              className="size-4 accent-[var(--primary)]"
                              aria-label={`Chọn ${r.id}`}
                            />
                          )}
                        </TableCell>
                        <TableCell className="font-medium">{r.id}</TableCell>
                        <TableCell>{r.className}</TableCell>
                        <TableCell className="max-w-56 truncate" title={r.description}>{r.description}</TableCell>
                        <TableCell className="max-w-44 truncate" title={r.suspectedMealText}>
                          {r.suspectedMealText} <span className="text-xs text-muted-foreground">({formatVnDate(r.mealDate)})</span>
                        </TableCell>
                        <TableCell>{formatVnDateTime(r.reportedAt)}</TableCell>
                        <TableCell className="text-muted-foreground">{r.reportedByLabel}</TableCell>
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
                      <TableHead>Lúc</TableHead>
                      <TableHead />
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {pendingRecords.map((rec) => (
                      <TableRow key={rec.id}>
                        <TableCell className="font-medium">{rec.id}</TableCell>
                        <TableCell>
                          <div className="flex flex-wrap gap-1">
                            {(rec.sourceReportCodes ?? []).map((rid) => <Badge key={rid} variant="info">{rid}</Badge>)}
                          </div>
                        </TableCell>
                        <TableCell className="max-w-80"><span className="line-clamp-2 text-muted-foreground">{rec.content}</span></TableCell>
                        <TableCell className="text-xs text-muted-foreground">{formatVnDateTime(rec.createdAt)}</TableCell>
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
                            <Button size="sm" disabled={openInvestigationAction.isPending} onClick={() => openOiDialog(rec)}>
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
                        <TableCell>{formatVnDateTime(i.reportedAt)}</TableCell>
                        <TableCell>
                          {(i.affectedStudents?.length ?? 0) >= 3 ? (
                            <Badge variant="critical">{i.affectedStudents!.length} (BR-146)</Badge>
                          ) : (
                            i.affectedStudents?.length ?? 0
                          )}
                        </TableCell>
                        <TableCell>{(i.symptoms?.length ?? 0) > 0 ? i.symptoms!.join(", ") : "—"}</TableCell>
                        <TableCell>
                          {i.status === "SEVERITY_PENDING" ? (
                            <span className="text-xs text-muted-foreground">Chưa đánh giá</span>
                          ) : (
                            <StatusBadge status={i.severity ?? ""} />
                          )}
                        </TableCell>
                        <TableCell><StatusBadge status={i.status ?? ""} /></TableCell>
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
                        <TableCell>{formatVnDateTime(row.when)}</TableCell>
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
                <DialogTitle>Chi tiết báo cáo {viewReport.id}</DialogTitle>
                <DialogDescription>
                  Báo cáo của {viewReport.reportedByLabel} lúc {formatVnDateTime(viewReport.reportedAt)} (chỉ đọc)
                </DialogDescription>
              </DialogHeader>
              <div className="grid grid-cols-1 gap-x-6 gap-y-3 text-sm md:grid-cols-2">
                <div className="space-y-3">
                  <p><b>Lớp:</b> {viewReport.className}</p>
                  <p><b>Món ăn nghi vấn:</b> {viewReport.suspectedMealText}</p>
                  <p><b>Ngày ăn:</b> {formatVnDate(viewReport.mealDate)}</p>
                  <p><b>Người báo cáo:</b> {viewReport.reportedByLabel}</p>
                  <p><b>Thời điểm báo cáo:</b> {formatVnDateTime(viewReport.reportedAt)}</p>
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
                      {(viewReport.symptoms ?? []).map((s) => <Badge key={s} variant="warning">{s}</Badge>)}
                    </div>
                  </div>
                  <div>
                    <b>Mô tả sự cố:</b>
                    <p className="mt-1 rounded-lg bg-muted p-3">{viewReport.description}</p>
                  </div>
                  <div>
                    <b>Ảnh đính kèm:</b>
                    {(viewReport.evidencePhotos ?? 0) > 0 ? (
                      <div className="mt-1 flex flex-wrap gap-2">
                        {Array.from({ length: viewReport.evidencePhotos ?? 0 }).map((_, i) => (
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
            <Button variant="destructive" disabled={reportAction.isPending} onClick={confirmCloseReport}>
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
              Toàn bộ báo cáo nguồn ({(closeRecordTarget?.sourceReportCodes ?? []).join(", ")}) sẽ chuyển
              &quot;Đã đóng — Không liên quan ATTP&quot;; không tạo phiên điều tra.
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setCloseRecordTarget(null)}>Hủy</Button>
            <Button variant="destructive" disabled={recordAction.isPending} onClick={confirmCloseRecord}>
              Đóng — Không liên quan ATTP
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>

      {/* Mở phiên điều tra — title + affectedStudentCodes bắt buộc (không suy ra được từ RPT) */}
      <Dialog open={oiTarget !== null} onOpenChange={(o) => !o && setOiTarget(null)}>
        <DialogContent className="max-w-2xl">
          <DialogHeader>
            <DialogTitle>Mở phiên điều tra từ biên bản {oiTarget?.id}</DialogTitle>
            <DialogDescription>
              Sự cố (INC-xxx) sẽ được tạo, gộp toàn bộ báo cáo nguồn của biên bản này (BR-146).
            </DialogDescription>
          </DialogHeader>
          {oiError && <Alert variant="danger">{oiError}</Alert>}
          <div className="space-y-3">
            <div className="space-y-1.5">
              <Label htmlFor="oi-title">Tiêu đề sự cố *</Label>
              <Input
                id="oi-title"
                value={oiTitle}
                onChange={(e) => setOiTitle(e.target.value)}
                placeholder="VD: Nghi ngộ độc thực phẩm sau bữa trưa lớp 3A"
              />
            </div>
            <div className="space-y-1.5">
              <Label htmlFor="oi-students">Học sinh bị ảnh hưởng * ({oiStudents.length} đã chọn)</Label>
              <Input
                id="oi-students"
                value={oiStudentSearch}
                onChange={(e) => setOiStudentSearch(e.target.value)}
                placeholder="Tìm theo tên hoặc mã học sinh…"
              />
              <div className="max-h-48 space-y-1 overflow-y-auto rounded-lg border p-1.5">
                {oiStudentCandidates.slice(0, 50).map((s) => {
                  const sid = s.id ?? "";
                  return (
                    <label key={sid} className="flex cursor-pointer items-center gap-2 rounded-md px-2 py-1 text-sm hover:bg-muted">
                      <input
                        type="checkbox"
                        checked={oiStudents.includes(sid)}
                        onChange={() => toggleOiStudent(sid)}
                        className="size-4 accent-[var(--primary)]"
                      />
                      {s.name} — {sid} ({s.class})
                    </label>
                  );
                })}
                {oiStudentCandidates.length === 0 && (
                  <p className="px-2 py-1 text-xs text-muted-foreground">Không tìm thấy học sinh phù hợp.</p>
                )}
              </div>
            </div>
          </div>
          <DialogFooter>
            <Button variant="outline" onClick={() => setOiTarget(null)}>Hủy</Button>
            <Button disabled={openInvestigationAction.isPending} onClick={submitOpenInvestigation}>
              <FolderSearch /> Mở phiên điều tra
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
