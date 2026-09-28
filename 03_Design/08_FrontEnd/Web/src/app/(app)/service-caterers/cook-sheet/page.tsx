"use client";

import { useMemo, useState } from "react";
import { Flame, Soup } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Input, Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList, useSpecialMeals } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import {
  cookSheet,
  gradeLevelsOf,
  isMenuLive,
  MEAL_TYPE_LABEL,
  type Classroom,
  type MealType,
} from "@/lib/mock-data";

const ALL_GRADES = "";

/**
 * BẢNG NẤU ĂN HÔM NAY — Service Caterers, MF Vận hành bếp.
 *
 * Trước 23/07/2026 không có màn nào nói cho bếp trước giờ nấu "món này bao nhiêu
 * suất". Nhiều khối cùng ăn một bữa và dùng chung món (Cơm trắng 06/07 là 1030
 * suất gộp ba khối), mà bếp phải mở từng thực đơn khối rồi tự cộng — dễ nấu thiếu.
 *
 * Đọc thực đơn + suất riêng qua API (không đọc mảng seed) rồi để `cookSheet()`
 * gộp: món chính nồi lớn đã trừ các em ăn suất riêng, phần riêng tách hẳn xuống
 * dưới vì bếp nấu nồi khác, dụng cụ khác (BR-082).
 */
function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

export default function CookSheetPage() {
  const { data: menusPage, isLoading } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  const { data: classroomsPage } = usePagedList<Classroom>("classrooms", { pageSize: 200 });
  const { variants } = useSpecialMeals();
  const menus = useMemo(() => menusPage?.items ?? [], [menusPage]);
  const gradeLevels = useMemo(() => gradeLevelsOf(classroomsPage?.items ?? []), [classroomsPage]);

  const [dateIso, setDateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>("LUNCH");
  const [grade, setGrade] = useState<string>(ALL_GRADES);

  /** Chỉ nấu cho đúng khối đang xem — bỏ trống ("Tất cả khối") thì gộp như cũ. */
  const scopedMenus = useMemo(
    () => (grade ? menus.filter((m) => m.grade === grade) : menus),
    [menus, grade]
  );

  /** Ngày còn thực đơn đang phục vụ (của khối đang xem) — để nhắc bếp khi hôm nay trống. */
  const servingDates = useMemo(
    () =>
      [...new Set(scopedMenus.filter(isMenuLive).map((m) => m.menuDateIso).filter(Boolean))].sort() as string[],
    [scopedMenus]
  );

  const sheet = useMemo(
    () => cookSheet(dateIso, mealType, scopedMenus as never, variants),
    [dateIso, mealType, scopedMenus, variants]
  );

  const vnDate = dateIso.split("-").reverse().join("/");
  const empty = sheet.main.length === 0 && sheet.special.length === 0;

  return (
    <>
      <PageHeader
        title="Bảng nấu ăn hôm nay"
        description="Gộp mọi khối cùng ăn một bữa — món chính nấu nồi lớn, suất chế độ ăn đặc biệt nấu riêng (BR-082). Số suất món chính đã trừ các em ăn suất riêng."
      />

      <Card className="mb-6">
        <CardContent className="flex flex-wrap items-end gap-4 pt-6">
          <div className="space-y-1.5">
            <Label htmlFor="cs-date">Ngày phục vụ</Label>
            <Input
              id="cs-date"
              type="date"
              className="w-44"
              value={dateIso}
              onChange={(e) => setDateIso(e.target.value)}
            />
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cs-meal">Buổi ăn</Label>
            <Select
              id="cs-meal"
              className="w-40"
              value={mealType}
              onChange={(e) => setMealType(e.target.value as MealType)}
            >
              {(Object.keys(MEAL_TYPE_LABEL) as MealType[]).map((m) => (
                <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
              ))}
            </Select>
          </div>
          <div className="space-y-1.5">
            <Label htmlFor="cs-grade">Khối</Label>
            <Select
              id="cs-grade"
              className="w-40"
              value={grade}
              onChange={(e) => setGrade(e.target.value)}
            >
              <option value={ALL_GRADES}>Tất cả khối</option>
              {gradeLevels.map((g) => (
                <option key={g} value={g}>{g}</option>
              ))}
            </Select>
          </div>
          {!empty && (
            <div className="flex flex-wrap items-center gap-1.5 pb-1">
              {sheet.grades.map((g) => (
                <Badge key={g} variant="neutral">{g}</Badge>
              ))}
              <Badge variant="info">{sheet.totalMain} suất chính</Badge>
              {sheet.totalSpecial > 0 && (
                <Badge variant="warning">{sheet.totalSpecial} suất riêng</Badge>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {isLoading ? (
        <div className="h-40 animate-pulse rounded-lg bg-muted" />
      ) : empty ? (
        <Alert variant="info" title={`Không có thực đơn phục vụ ${MEAL_TYPE_LABEL[mealType]} ngày ${vnDate}`}>
          {servingDates.length > 0 ? (
            <>
              Các ngày đang có thực đơn phục vụ:{" "}
              {servingDates.map((d) => d.split("-").reverse().join("/")).join(" · ")}. Chỉ thực đơn
              đã duyệt/công bố mới lên bảng nấu.
            </>
          ) : (
            "Chưa có thực đơn nào ở trạng thái đã duyệt/công bố."
          )}
        </Alert>
      ) : (
        <div className="space-y-6">
          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Flame className="size-4 text-muted-foreground" />
                Món chính — nồi lớn ({sheet.main.length} món · {sheet.totalMain} suất)
              </CardTitle>
            </CardHeader>
            <CardContent>
              <Table>
                <TableHeader>
                  <TableRow>
                    <TableHead>Món</TableHead>
                    <TableHead className="text-right">Tổng suất</TableHead>
                    <TableHead>Gộp từ khối</TableHead>
                  </TableRow>
                </TableHeader>
                <TableBody>
                  {sheet.main.map((l) => (
                    <TableRow key={l.dish}>
                      <TableCell className="font-medium">{l.dish}</TableCell>
                      <TableCell className="text-right text-base font-semibold tabular-nums">
                        {l.portions}
                      </TableCell>
                      <TableCell className="text-sm text-muted-foreground">
                        {l.sources.join(" · ")}
                      </TableCell>
                    </TableRow>
                  ))}
                </TableBody>
              </Table>
            </CardContent>
          </Card>

          {sheet.special.length > 0 && (
            <Card className="border-warning/40">
              <CardHeader>
                <CardTitle className="flex items-center gap-2">
                  <Soup className="size-4 text-warning" />
                  Suất riêng — nấu nồi riêng, dụng cụ riêng (BR-082)
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-2">
                <p className="text-xs text-muted-foreground">
                  Các suất dưới đây KHÔNG nằm trong nồi lớn: suất thay món cho em dị ứng, và
                  thực đơn riêng cho nhóm chế độ ăn. Nấu tách để tránh nhiễm chéo.
                </p>
                {sheet.special.map((l, i) => (
                  <div key={i} className="flex flex-wrap items-center gap-2 rounded-lg border p-2.5 text-sm">
                    <Badge variant={l.kind === "ALLERGY_SUB" ? "danger" : "warning"}>
                      {l.kind === "ALLERGY_SUB" ? "Thay món (dị ứng)" : "Chế độ ăn"}
                    </Badge>
                    <span className="min-w-0 flex-1 font-medium">{l.dish}</span>
                    <Badge variant="neutral">{l.portions} suất</Badge>
                    <span className="text-xs text-muted-foreground">{l.sources.join(", ")}</span>
                    {l.note && (
                      <span className="w-full text-xs text-muted-foreground">↳ {l.note}</span>
                    )}
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          <Alert variant="info" title="Sau khi nấu">
            Ghi lại mẻ nấu ở <b>Sổ chế biến</b> (lô nguyên liệu lấy tự động từ phiếu xuất kho),
            rồi <b>Lưu mẫu</b> đủ món trước khi phục vụ.
          </Alert>
        </div>
      )}
    </>
  );
}
