"use client";

/**
 * CHI TIẾT PHIÊN KIỂM THỰC BA BƯỚC — School Admin, VIEW-ONLY.
 *
 * Copy có chủ đích từ `medical-staff/inspections/[id]/page.tsx` (10/08/2026, Nam) — cùng
 * dữ liệu, cùng bản in (`lib/inspection-sheets.ts`), BỎ khối "Đánh giá của Y tế" (thao tác
 * ghi thuộc riêng MEDICAL_STAFF) và nút "Mẫu lưu cùng bữa" (School Admin không thao tác
 * lưu mẫu — xem ở `/school-admin/samples` riêng).
 */

import { use } from "react";
import Link from "next/link";
import { ArrowLeft, Camera, MapPin, Printer } from "lucide-react";
import { toast } from "sonner";
import { EvidenceGallery } from "@/components/app/evidence-gallery";
import { PageHeader } from "@/components/app/page-header";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card } from "@/components/ui/card";
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table";
import { useDetail } from "@/lib/api/hooks";
import { blockedForms, openInspectionSheetsPrint, printableForms } from "@/lib/inspection-sheets";
import { inspectionDtoToMock } from "@/lib/inspection-adapter";
import { formatVnDate } from "@/lib/format";
import { MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import type { InspectionSessionDto } from "@/lib/api/types";

const yn = (v: boolean | undefined) => (v ? "Có" : "Không");
const passFail = (v: boolean | undefined) =>
  v ? <Badge variant="success">Đạt</Badge> : <Badge variant="danger">Không đạt</Badge>;

export default function SchoolAdminInspectionDetailPage({
  params,
}: {
  params: Promise<{ id: string }>;
}) {
  const { id } = use(params);
  const { data: detail, isLoading } = useDetail<InspectionSessionDto>("inspections", id);

  if (isLoading) return <Card className="h-64 animate-pulse bg-muted/40" />;
  if (!detail) {
    return (
      <Card className="p-10 text-center text-muted-foreground">
        Không tìm thấy phiên kiểm thực.{" "}
        <Link className="underline" href="/school-admin/inspections">Về danh sách</Link>
      </Card>
    );
  }

  const dateVn = formatVnDate(detail.date);
  const mealVn = MEAL_TYPE_LABEL[(detail.meal ?? "") as MealType] ?? "";
  const detailMock = inspectionDtoToMock(detail);
  const catererFinalized = detail.overall !== "IN_PROGRESS";
  const forms = printableForms(detailMock);
  const blocked = blockedForms(detailMock);
  const canPrint = catererFinalized && forms.length > 0;

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
            <Link href="/school-admin/inspections">
              <Button variant="outline"><ArrowLeft /> Về danh sách</Button>
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
          hồ sơ đã chốt.
        </Alert>
      )}
      {catererFinalized && blocked.length > 0 && (
        <Alert variant="info" className="mb-4" title="Một phần mẫu biểu chưa đủ điều kiện in">
          {blocked.map((f) => `Mẫu số ${f} (Bước ${f})`).join(", ")} chưa in được — bước tương ứng
          phải ĐẠT trước khi phát hành bản giấy.
        </Alert>
      )}

      <div className="space-y-4">
        <Card>
          <div className="flex flex-wrap items-center gap-3 p-5 text-sm">
            <span>Bước 1 <StatusBadge status={detail.step1 ?? ""} /></span>
            <span>Bước 2 <StatusBadge status={detail.step2 ?? ""} /></span>
            <span>Bước 3 <StatusBadge status={detail.step3 ?? ""} /></span>
            <span>Tổng thể <StatusBadge status={detail.overall ?? ""} /></span>
            {detail.coreTemp != null && (
              <span className="text-xs" title="BR-090">
                Nhiệt lõi: <b className={detail.coreTemp < 75 ? "text-destructive" : ""}>{detail.coreTemp}°C</b>
              </span>
            )}
          </div>
        </Card>

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

        {(detail.step2Rows?.length ?? 0) > 0 && (
          <Card>
            <div className="space-y-2 p-5 text-sm">
              <p className="font-semibold" title="BR-090">Bước 2 (Mẫu số 2) — Kiểm tra khi chế biến (nhiệt lõi ≥75°C)</p>
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
                <Camera className="size-3.5" /> Ảnh bằng chứng
              </p>
              <EvidenceGallery inspectionCode={detail.id!} />
            </div>
          </div>
        </Card>
      </div>
    </div>
  );
}
