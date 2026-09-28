"use client";

/**
 * CHI TIẾT PHIÊN KIỂM THỰC BA BƯỚC — UC-112 (`11_FoodInspectionRecords.md`), Medical Staff.
 *
 * ═══ Vòng 10 — 27/07/2026 (Mục 19.3) ═══
 * Thay **Dialog** chi tiết ở màn danh sách bằng **route riêng**. Hai lý do:
 *  1. Nội dung là snapshot đầy đủ Mẫu 1/2/3 (bảng 10–15 cột) — nhét vào popup thì lúc nào
 *     cũng phải cuộn ngang lẫn dọc.
 *  2. Luồng thật có 4 bước (xem bản ghi Caterers → đánh giá Đạt/Không đạt → nhập lý do nếu
 *     Không đạt → in hồ sơ), tức là một màn làm việc, không phải một hộp thoại tra cứu.
 *
 * **Không điều hướng chéo role**: nút in KHÔNG còn trỏ sang
 * `/service-caterers/inspections/[id]/print` (cách cũ theo Mục 8.5 điểm 2 — bấm vào là đổi
 * cả sidebar/app shell sang giao diện Caterers). Bản in nay sinh từ hàm dùng chung
 * `lib/inspection-sheets.ts` và mở trong cửa sổ in riêng, vẫn đúng một khuôn mẫu biểu với
 * bản in phía Caterers.
 *
 * **Điều kiện in hồ sơ chính thức**: chỉ bật khi Caterers đã chốt (`overall !== "IN_PROGRESS"`)
 * và có ít nhất 1 mẫu ở bước ĐẠT — không phát hành văn bản "chính thức" cho dữ liệu còn dở.
 *
 * Khối "Đánh giá của Y tế khi xem lại" (Mục 8.5 điểm 1) chuyển nguyên vẹn từ dialog sang
 * đây, giữ đúng nguyên tắc: KHÔNG ghi đè `overall` (vốn tự tính theo BR-090/093/094 từ số
 * liệu Caterers nhập); lý do bắt buộc khi Không đạt.
 */

import { use, useEffect, useState } from "react";
import Link from "next/link";
import { ArrowLeft, Camera, FlaskConical, MapPin, Printer } from "lucide-react";
import { toast } from "sonner";
import { EvidenceGallery } from "@/components/app/evidence-gallery";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import { Label, Textarea } from "@/components/ui/input";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useAction, useDetail, usePagedList } from "@/lib/api/hooks";
import { blockedForms, openInspectionSheetsPrint, printableForms } from "@/lib/inspection-sheets";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { apiErrorMessage } from "@/lib/messages";
import { formatVnDate, formatVnDateTime } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import type { FoodSampleDto, InspectionSessionDto, MedicalReviewDto } from "@/lib/api/types";

/** Có/Không (Mẫu 1/2/3 dùng dấu tick) — field DTO thật đều optional. */
const yn = (v: boolean | undefined) => (v ? "Có" : "Không");
const passFail = (v: boolean | undefined) =>
  v ? <Badge variant="success">Đạt</Badge> : <Badge variant="danger">Không đạt</Badge>;

const mealPrefix = (meal: string) => (meal === "Bữa trưa" ? "Trưa" : "Phụ");

export default function MedicalInspectionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: detail, isLoading } = useDetail<InspectionSessionDto>("inspections", id);
  const medicalReview = useAction<InspectionSessionDto>("inspections", { handledError: true });
  const { data: sampleData } = usePagedList<FoodSampleDto>("samples", { pageSize: 200 });

  const [reviewResult, setReviewResult] = useState<"" | "PASSED" | "FAILED">("");
  const [reviewReason, setReviewReason] = useState("");

  useEffect(() => {
    if (detail) {
      setReviewResult((detail.medicalReviewResult as "" | "PASSED" | "FAILED" | undefined) ?? "");
      setReviewReason(detail.medicalReviewReason ?? "");
    }
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [detail?.id]);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!detail) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm thực.{" "}
        <Link className="underline" href="/medical-staff/inspections">Về danh sách</Link>
      </Card>
    );
  }

  const dateVn = formatVnDate(detail.date);
  const mealVn = MEAL_TYPE_LABEL[(detail.meal ?? "") as MealType] ?? "";
  const samples = (sampleData?.items ?? []).filter(
    (f) => (f.collectedAt ?? "").startsWith(dateVn) || f.meal === `${mealPrefix(mealVn)} ${dateVn.slice(0, 5)}`
  );
  // `printableForms`/`blockedForms`/`openInspectionSheetsPrint` viết cho mock — adapter tại
  // chỗ gọi (cùng cách `[id]/print/page.tsx` phía Service Caterers).
  const detailMock = inspectionDtoToMock(detail);

  // Caterers đã chốt tài liệu chưa — gate của nút "In hồ sơ chính thức" (Mục 19.3)
  const catererFinalized = detail.overall !== "IN_PROGRESS";
  const forms = printableForms(detailMock);
  const blocked = blockedForms(detailMock);
  const canPrint = catererFinalized && forms.length > 0;

  /**
   * Mục 8.5 điểm 1 — lưu đánh giá riêng của Y tế; KHÔNG ghi đè `overall`.
   * `POST /inspections/{code}/medical-review` — `medicalReviewBy`/`medicalReviewAt` do
   * SERVER tự gán (`actor.userId()`/`clock.instant()`), không phải field client gửi lên
   * như bản mock cũ.
   */
  function saveMedicalReview() {
    if (!detail || reviewResult === "") return;
    if (reviewResult === "FAILED" && reviewReason.trim() === "") {
      toast.error("Nhập lý do khi đánh giá Không đạt.");
      return;
    }
    const body: MedicalReviewDto = {
      medicalReviewResult: reviewResult,
      medicalReviewReason: reviewReason.trim() || undefined,
    };
    medicalReview.mutate(
      { id: detail.id!, action: "medical-review", body },
      {
        onSuccess: () => toast.success("Đã lưu đánh giá của Y tế cho phiên kiểm thực này."),
        onError: (err) => toast.error(apiErrorMessage(err, "Không lưu được đánh giá của Y tế.")),
      }
    );
  }

  function printOfficial() {
    if (!detail || !canPrint) return;
    if (!openInspectionSheetsPrint(detailMock)) {
      toast.error("Trình duyệt chặn cửa sổ in — cho phép popup rồi thử lại.");
    }
  }

  return (
    <div className="mx-auto w-full max-w-[1280px]">
      <PageHeader
        title={`Phiên kiểm thực ${detail.id}`}
        description={
          detail.formHeader
            ? `${detail.formHeader.facility} · ${detail.formHeader.location} · Người kiểm tra: ${detail.formHeader.inspector} · ${detail.formHeader.checkedAt}`
            : `${dateVn} · ${mealVn} · ${detail.inspector}`
        }
        actions={
          <div className="flex flex-wrap gap-2">
            <Link href="/medical-staff/inspections">
              <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
            </Link>
            <Link href={`/medical-staff/samples?date=${dateVn}&meal=${encodeURIComponent(mealPrefix(mealVn))}`}>
              <Button variant="outline"><FlaskConical /> Mẫu lưu cùng bữa ({samples.length})</Button>
            </Link>
            <Button
              disabled={!canPrint}
              title={
                !catererFinalized
                  ? "Đơn vị suất ăn chưa chốt hồ sơ — chưa in được bản chính thức"
                  : forms.length === 0
                    ? "Chưa có bước nào Đạt — bản in chỉ phát hành cho bước đã đạt"
                    : `In Mẫu ${forms.join(" + ")}`
              }
              onClick={printOfficial}
            >
              <Printer /> In hồ sơ chính thức
            </Button>
          </div>
        }
      />

      {!catererFinalized && (
        <Alert variant="warning" className="mb-4" title="Đơn vị suất ăn chưa chốt hồ sơ">
          Phiên còn ở trạng thái đang thực hiện — dữ liệu bên dưới là bản ghi hiện tại, chưa phải
          hồ sơ đã chốt. Không phát hành bản in chính thức cho dữ liệu còn dở; vẫn xem và đánh giá
          được bình thường.
        </Alert>
      )}
      {catererFinalized && blocked.length > 0 && (
        <Alert variant="info" className="mb-4" title="Một phần mẫu biểu chưa đủ điều kiện in">
          {blocked.map((f) => `Mẫu số ${f} (Bước ${f})`).join(", ")} chưa in được — bước tương ứng
          phải ĐẠT trước khi phát hành bản giấy.
          {forms.length > 0 && ` Các mẫu đủ điều kiện (${forms.map((f) => `Mẫu ${f}`).join(", ")}) vẫn in được.`}
        </Alert>
      )}

      <div className="space-y-4">
        {/* ── Tóm tắt trạng thái 3 bước ── */}
        <Card>
          <div className="flex flex-wrap items-center gap-3 p-5 text-sm">
            <span>Bước 1 <StatusBadge status={detail.step1 ?? ""} /></span>
            <span>Bước 2 <StatusBadge status={detail.step2 ?? ""} /></span>
            <span>Bước 3 <StatusBadge status={detail.step3 ?? ""} /></span>
            <span>Tổng thể <StatusBadge status={detail.overall ?? ""} /></span>
            {detail.coreTemp != null && (
              <span className="text-xs" title="BR-090">
                Nhiệt lõi:{" "}
                <b className={detail.coreTemp < 75 ? "text-destructive" : ""}>{detail.coreTemp}°C</b>
              </span>
            )}
            {detail.coSigner && (
              <span className="text-xs text-muted-foreground">Đồng ký: {detail.coSigner}</span>
            )}
            <span className="ml-auto text-xs text-muted-foreground">
              Dữ liệu do đơn vị suất ăn nhập — Y tế xem chỉ đọc, dùng chung 100%
            </span>
          </div>
        </Card>

        {!detail.formHeader && (
          <Card>
            <p className="p-5 text-sm text-muted-foreground">
              Phiên này chưa được đơn vị suất ăn điền chi tiết theo Mẫu 1/2/3 — chỉ có tóm tắt
              trạng thái ở trên. Ghi chú: {detail.staffNotes ?? "—"}
            </p>
          </Card>
        )}

        {/* ── Bước 1 — Mẫu số 1, Phần I ── */}
        {(detail.step1Fresh?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <p className="font-semibold">Bước 1 (Mẫu số 1) — Phần I: Thực phẩm tươi sống, đông lạnh</p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tên thực phẩm</TableHead>
                    <TableHead>Thời gian nhập</TableHead>
                    <TableHead>Khối lượng</TableHead>
                    <TableHead>Nơi cung cấp + liên hệ</TableHead>
                    <TableHead>Người giao</TableHead>
                    <TableHead>Chứng từ / Thú y / Kiểm dịch</TableHead>
                    <TableHead>Cảm quan</TableHead>
                    <TableHead>XN nhanh</TableHead>
                    <TableHead>Biện pháp xử lý</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {detail.step1Fresh!.map((r, i) => (
                    <TableRow key={i}>
                      <TableCell className="font-medium">{r.name}</TableCell>
                      <TableCell>{r.receivedAt}</TableCell>
                      <TableCell>{r.quantity}</TableCell>
                      <TableCell className="max-w-52 text-xs">{r.supplierName} — {r.supplierContact}</TableCell>
                      <TableCell>{r.deliverer}</TableCell>
                      <TableCell className="text-xs">{yn(r.hasInvoice)} / {yn(r.hasVetCert)} / {yn(r.hasQuarantineCert)}</TableCell>
                      <TableCell>{passFail(r.sensoryPass)}</TableCell>
                      <TableCell>{r.quickTest === "NONE" ? "—" : r.quickTest === "PASS" ? "Đạt" : "Không đạt"}</TableCell>
                      <TableCell className="text-xs">{r.action || "—"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </Card>
        )}

        {/* ── Bước 1 — Mẫu số 1, Phần II ── */}
        {(detail.step1Dry?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <p className="font-semibold">Bước 1 (Mẫu số 1) — Phần II: Thực phẩm khô, bao gói sẵn, phụ gia</p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Tên thực phẩm</TableHead>
                    <TableHead>Cơ sở sản xuất</TableHead>
                    <TableHead>Thời gian nhập</TableHead>
                    <TableHead>Khối lượng</TableHead>
                    <TableHead>Nơi cung cấp</TableHead>
                    <TableHead>Hạn sử dụng</TableHead>
                    <TableHead>Bảo quản</TableHead>
                    <TableHead>Chứng từ</TableHead>
                    <TableHead>Cảm quan</TableHead>
                    <TableHead>Xử lý</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {detail.step1Dry!.map((r, i) => (
                    <TableRow key={i}>
                      <TableCell className="font-medium">{r.name}</TableCell>
                      <TableCell className="max-w-44 text-xs">{r.producer} — {r.producerAddress}</TableCell>
                      <TableCell>{r.receivedAt}</TableCell>
                      <TableCell>{r.quantity}</TableCell>
                      <TableCell className="max-w-44 text-xs">{r.supplierName}</TableCell>
                      <TableCell>{r.expiry}</TableCell>
                      <TableCell>{r.storage}</TableCell>
                      <TableCell>{yn(r.hasInvoice)}</TableCell>
                      <TableCell>{passFail(r.sensoryPass)}</TableCell>
                      <TableCell className="text-xs">{r.action || "—"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </Card>
        )}

        {/* ── Bước 2 — Mẫu số 2 ── */}
        {(detail.step2Rows?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <p className="font-semibold" title="BR-090">
                Bước 2 (Mẫu số 2) — Kiểm tra khi chế biến (nhiệt lõi ≥75°C)
              </p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Ca/bữa ăn</TableHead>
                    <TableHead>Món ăn</TableHead>
                    <TableHead>Nguyên liệu chính</TableHead>
                    <TableHead>Số suất</TableHead>
                    <TableHead>Sơ chế xong</TableHead>
                    <TableHead>Chế biến xong</TableHead>
                    <TableHead>VS người/thiết bị/khu vực</TableHead>
                    <TableHead>Cảm quan</TableHead>
                    <TableHead>Xử lý</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {detail.step2Rows!.map((r, i) => (
                    <TableRow key={i}>
                      <TableCell className="text-xs">{r.shift}</TableCell>
                      <TableCell className="font-medium">{r.dishName}</TableCell>
                      <TableCell className="max-w-48 text-xs">{r.mainIngredients}</TableCell>
                      <TableCell>{r.servings}</TableCell>
                      <TableCell className="text-xs">{r.prepDoneAt}</TableCell>
                      <TableCell className="text-xs">{r.cookDoneAt}</TableCell>
                      <TableCell className="text-xs">{yn(r.hygieneStaff)} / {yn(r.hygieneEquipment)} / {yn(r.hygieneArea)}</TableCell>
                      <TableCell>{passFail(r.sensoryPass)}</TableCell>
                      <TableCell className="text-xs">{r.action || "—"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </Card>
        )}

        {/* ── Bước 3 — Mẫu số 3 ── */}
        {(detail.step3Rows?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <p className="font-semibold">Bước 3 (Mẫu số 3) — Kiểm tra trước khi ăn</p>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Ca/bữa ăn</TableHead>
                    <TableHead>Món ăn</TableHead>
                    <TableHead>Số suất</TableHead>
                    <TableHead>Chia món xong</TableHead>
                    <TableHead>Bắt đầu ăn</TableHead>
                    <TableHead>Dụng cụ chia/chứa/che đậy</TableHead>
                    <TableHead>Cảm quan</TableHead>
                    <TableHead>Xử lý</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {detail.step3Rows!.map((r, i) => (
                    <TableRow key={i}>
                      <TableCell className="text-xs">{r.shift}</TableCell>
                      <TableCell className="font-medium">{r.dishName}</TableCell>
                      <TableCell>{r.servings}</TableCell>
                      <TableCell className="text-xs">{r.dividedDoneAt}</TableCell>
                      <TableCell className="text-xs">{r.eatStartAt}</TableCell>
                      <TableCell className="max-w-44 text-xs">{r.utensils}</TableCell>
                      <TableCell>{passFail(r.sensoryPass)}</TableCell>
                      <TableCell className="text-xs">{r.action || "—"}</TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </div>
          </Card>
        )}

        {/* ── Bằng chứng chung: ảnh + GPS + ghi chú (BR-083/BR-108) ── */}
        <Card>
          <div className="space-y-3 p-5 text-sm">
            <div className="grid gap-3 sm:grid-cols-2">
              <div className="rounded-lg border p-3">
                <p className="mb-1 flex items-center gap-1.5 text-xs text-muted-foreground">
                  <MapPin className="size-3.5" /> Vị trí GPS
                </p>
                <p className="text-xs">{detail.gps ?? "Chưa ghi nhận"}</p>
              </div>
              <div className="rounded-lg border p-3">
                <p className="mb-1 text-xs text-muted-foreground">Ghi chú nhân viên</p>
                <p className="text-xs">{detail.staffNotes ?? "—"}</p>
              </div>
            </div>
            <div>
              <p className="mb-1.5 flex items-center gap-1.5 text-xs text-muted-foreground" title="BR-083">
                <Camera className="size-3.5" /> Ảnh bằng chứng — xem trực tiếp, không chỉ số đếm
              </p>
              <EvidenceGallery inspectionCode={detail.id!} />
            </div>
          </div>
        </Card>

        {/* ── Đánh giá của Y tế (chuyển nguyên vẹn từ dialog cũ — Mục 8.5 điểm 1) ── */}
        <Card>
          <div className="space-y-3 p-5 text-sm">
            <div className="flex flex-wrap items-center justify-between gap-2">
              <p className="font-medium">Đánh giá của Y tế khi xem lại</p>
              <span className="text-xs text-muted-foreground">
                Kết quả tự tính theo bước: <StatusBadge status={detail.overall ?? ""} />
              </span>
            </div>
            <p className="text-xs text-muted-foreground">
              Đánh giá này ghi RIÊNG, không ghi đè kết quả tự tính từ số liệu đơn vị suất ăn nhập
              (BR-090/093/094) — hai con số phục vụ hai mục đích khác nhau.
            </p>
            {detail.medicalReviewAt && (
              <p className="text-xs text-muted-foreground">
                Đã đánh giá {detail.medicalReviewResult === "PASSED" ? "Đạt" : "Không đạt"} bởi{" "}
                {detail.medicalReviewBy} lúc {formatVnDateTime(detail.medicalReviewAt)}
                {detail.medicalReviewReason ? ` — ${detail.medicalReviewReason}` : ""}
              </p>
            )}
            <div className="flex flex-wrap gap-1.5">
              <Button
                size="sm"
                variant={reviewResult === "PASSED" ? "success" : "outline"}
                onClick={() => setReviewResult("PASSED")}
              >
                Đạt
              </Button>
              <Button
                size="sm"
                variant={reviewResult === "FAILED" ? "destructive" : "outline"}
                onClick={() => setReviewResult("FAILED")}
              >
                Không đạt
              </Button>
            </div>
            {reviewResult === "FAILED" && (
              <div className="space-y-1">
                <Label htmlFor="mr-reason">Lý do đánh giá Không đạt *</Label>
                <Textarea
                  id="mr-reason"
                  rows={2}
                  value={reviewReason}
                  onChange={(e) => setReviewReason(e.target.value)}
                  placeholder="VD: ảnh Bước 3 cho thấy khay chia không che đậy dù dữ liệu tự tính vẫn Đạt"
                />
              </div>
            )}
            <Button
              disabled={reviewResult === "" || medicalReview.isPending}
              title={reviewResult === "" ? "Chọn Đạt/Không đạt trước khi lưu" : undefined}
              onClick={saveMedicalReview}
            >
              Lưu đánh giá của Y tế
            </Button>
          </div>
        </Card>
      </div>
    </div>
  );
}
