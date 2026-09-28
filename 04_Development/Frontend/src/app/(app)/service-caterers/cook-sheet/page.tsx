"use client";

import { useMemo, useState } from "react";
import { Flame, Soup, TriangleAlert } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { SimpleTable, type SimpleColumn } from "@/components/app/simple-table";
import { Input, Label, Select } from "@/components/ui/input";
import { usePagedList, useSingle } from "@/lib/api/hooks";
import { useDietGroups } from "@/lib/diet-groups";
import { gradeLevelsOf, MEAL_TYPE_LABEL, type MealType } from "@/lib/domain-utils";
import type { ClassroomDto, PlannedDishesDto } from "@/lib/api/types";

const ALL_GRADES = "";
const STANDARD_AUDIENCE = "ALL";

/**
 * BẢNG NẤU ĂN HÔM NAY — Service Caterers, MF Vận hành bếp.
 *
 * Trước 23/07/2026 không có màn nào nói cho bếp trước giờ nấu "món này bao nhiêu suất". Nhiều
 * khối cùng ăn một bữa và dùng chung món (Cơm trắng 06/07 là 1030 suất gộp ba khối), mà bếp
 * phải mở từng thực đơn khối rồi tự cộng — dễ nấu thiếu.
 *
 * ─────────────────────────────────────────────────────────────────────────────────────────
 * ĐỔI NGUỒN 13/08/2026 (theo Nam) — đọc `GET /student-distributions/planned-dishes` thay vì
 * tự tính bằng `cookSheet()` ở frontend.
 *
 * Bản cũ tính từ SỐ KHAI trong thực đơn: `mainPortions = menu.servings − fullVariantPortions −
 * subPortions`, với `subPortions` là con số Caterer TỰ GÕ ở màn lập thực đơn. Nó vẫn trừ đúng
 * về mặt số học, nhưng là một nguồn sự thật THỨ HAI, độc lập với thứ thực sự được phát:
 *
 *   · số suất thay thế — bản cũ lấy số gõ tay, hệ thống phát đếm HS có allergen đã xác minh.
 *     Gõ "2" mà thực tế 3 em ⇒ thiếu 1 suất trong nồi, và em thứ ba bị `serve()` chặn tại quầy;
 *   · món thay thế — bản cũ lấy món khai riêng cho tuần này, hệ thống phát lấy
 *     `Dish.defaultSubstituteId` của thư viện. Khác nhau ⇒ bếp nấu món này, hệ thống ghi món kia;
 *   · mẫu số — bản cũ trừ từ `menu.servings` (số khai cấp thực đơn), hệ thống phát đếm bản ghi
 *     `student_distributions` thật. HS nghỉ / lớp mới mở làm hai con số rời nhau.
 *
 * Nay bếp, xuất kho (`inventory/issue`), kiểm thực (`inspections/[id]`) và quầy phát dùng CHUNG
 * một con số. Không cần trừ gì nữa: `planDishes()` phía backend đã đổi món cho em dị ứng, nên
 * em đó cộng vào món THAY THẾ và không cộng vào món gốc — phép trừ biến mất theo cấu trúc.
 *
 * BR-082 (nấu nồi riêng, dụng cụ riêng) dựa vào `menuAudience` — thêm vào `PlannedDishRow` cùng
 * đợt này; thiếu nó thì suất từ thực đơn chế độ ăn riêng bị gộp thẳng vào nồi lớn.
 */
function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

/** 1 dòng bảng nấu — gộp theo món (nồi lớn) hoặc theo món × món-bị-thay (suất riêng). */
interface CookRow {
  key: string;
  dish: string;
  portions: number;
  /** Nhãn nguồn: khối (nồi lớn) hoặc lớp (suất riêng) — bếp cần biết nấu cho ai. */
  sources: string[];
  kind: "MAIN" | "ALLERGY_SUB" | "DIET_VARIANT";
  /** Chỉ ALLERGY_SUB — tên món gốc mà suất này thay cho. */
  substitutedFor?: string;
  /** Chỉ DIET_VARIANT — mã nhóm chế độ ăn. */
  audience?: string;
}

const mainColumns: SimpleColumn<CookRow>[] = [
  { header: "Món", cell: (l) => <span className="font-medium">{l.dish}</span> },
  {
    header: "Tổng suất",
    className: "text-right",
    cell: (l) => <span className="text-base font-semibold tabular-nums">{l.portions}</span>,
  },
  {
    header: "Gộp từ khối",
    cell: (l) => <span className="text-sm text-muted-foreground">{l.sources.join(" · ")}</span>,
  },
];

export default function CookSheetPage() {
  const { data: classroomsPage } = usePagedList<ClassroomDto>("classrooms", { pageSize: 200 });
  const classrooms = useMemo(() => classroomsPage?.items ?? [], [classroomsPage]);
  const dietGroups = useDietGroups();
  const gradeLevels = useMemo(() => gradeLevelsOf(classrooms), [classrooms]);

  const [dateIso, setDateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>("LUNCH");
  const [grade, setGrade] = useState<string>(ALL_GRADES);

  // Bỏ `class` ⇒ gộp TOÀN TRƯỜNG (Service Caterers được phép; TEACHER thì backend bắt buộc
  // truyền `class`, xem `plannedDishes()`). Lọc theo khối làm ở FE qua bảng lớp bên dưới, vì
  // endpoint khoá theo LỚP chứ không theo khối.
  const { data: planned, isLoading } = useSingle<PlannedDishesDto>(
    "student-distributions/planned-dishes",
    { date: dateIso, mealType: mealType as string },
    { enabled: Boolean(dateIso && mealType) }
  );

  /** `className` (code lớp, VD "3C") → nhãn khối ("Khối 3"). */
  const gradeOfClass = useMemo(() => {
    const m = new Map<string, string>();
    for (const c of classrooms) {
      if (c.id && c.grade) m.set(c.id, c.grade);
      if (c.name && c.grade) m.set(c.name, c.grade);
    }
    return m;
  }, [classrooms]);

  const sheet = useMemo(() => {
    const rows = (planned?.perStudent ?? []).filter((r) => {
      if (!grade) return true;
      return gradeOfClass.get(r.className ?? "") === grade;
    });

    const main = new Map<string, CookRow>();
    const allergySub = new Map<string, CookRow>();
    const dietVariant = new Map<string, CookRow>();
    // Suất BỊ CHẶN tách hẳn ra: chúng chưa quyết được nấu gì (dị ứng trùng món mà chưa khai
    // món thay thế), đếm lẫn vào nồi lớn là bếp nấu thừa một suất sẽ không ai ăn.
    const blocked: CookRow[] = [];

    for (const r of rows) {
      const dishName = r.dishName ?? r.dishId ?? "—";
      const source = gradeOfClass.get(r.className ?? "") ?? r.className ?? "—";
      const push = (map: Map<string, CookRow>, key: string, row: Omit<CookRow, "key" | "portions" | "sources">) => {
        const cur = map.get(key);
        if (cur) {
          cur.portions += 1;
          if (!cur.sources.includes(source)) cur.sources.push(source);
          return;
        }
        map.set(key, { key, portions: 1, sources: [source], ...row });
      };

      if (r.blocked) {
        blocked.push({
          key: `${r.distributionId}`,
          dish: dishName,
          portions: 1,
          sources: [`${r.studentName ?? r.studentId} · ${r.className ?? ""}`],
          kind: "MAIN",
        });
        continue;
      }
      if (r.isSubstitute) {
        push(allergySub, `${dishName}__${r.substitutedForDishName ?? ""}`, {
          dish: dishName,
          kind: "ALLERGY_SUB",
          substitutedFor: r.substitutedForDishName ?? undefined,
        });
        continue;
      }
      // BR-082 — suất đến từ THỰC ĐƠN RIÊNG của nhóm chế độ ăn: nồi riêng, dụng cụ riêng.
      const audience = r.menuAudience ?? STANDARD_AUDIENCE;
      if (audience !== STANDARD_AUDIENCE) {
        push(dietVariant, `${audience}__${dishName}`, { dish: dishName, kind: "DIET_VARIANT", audience });
        continue;
      }
      push(main, dishName, { dish: dishName, kind: "MAIN" });
    }

    const byPortionsDesc = (a: CookRow, b: CookRow) => b.portions - a.portions;
    const mainRows = [...main.values()].sort(byPortionsDesc);
    const specialRows = [...allergySub.values(), ...dietVariant.values()].sort(byPortionsDesc);
    return {
      main: mainRows,
      special: specialRows,
      blocked,
      totalMain: mainRows.reduce((s, r) => s + r.portions, 0),
      totalSpecial: specialRows.reduce((s, r) => s + r.portions, 0),
      grades: [...new Set(rows.map((r) => gradeOfClass.get(r.className ?? "") ?? r.className ?? "—"))],
    };
  }, [planned, grade, gradeOfClass]);

  const vnDate = dateIso.split("-").reverse().join("/");
  const empty = sheet.main.length === 0 && sheet.special.length === 0 && sheet.blocked.length === 0;
  const groupLabel = (code?: string) =>
    dietGroups.find((g) => g.id === code)?.label ?? code ?? "chế độ ăn riêng";

  return (
    <>
      <PageHeader
        title="Bảng nấu ăn hôm nay"
        description="Đếm từ suất THẬT sẽ phát cho từng học sinh — cùng con số với xuất kho và quầy phát. Món chính nấu nồi lớn, suất thay món dị ứng và thực đơn chế độ ăn riêng nấu nồi riêng (BR-082)."
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
              {sheet.blocked.length > 0 && (
                <Badge variant="danger">{sheet.blocked.length} suất bị chặn</Badge>
              )}
            </div>
          )}
        </CardContent>
      </Card>

      {isLoading ? (
        <div className="h-40 animate-pulse rounded-lg bg-muted" />
      ) : empty ? (
        <Alert variant="info" title={`Chưa có suất nào cần nấu cho ${MEAL_TYPE_LABEL[mealType]} ngày ${vnDate}`}>
          Bảng này đếm từ phiên phát suất của từng học sinh, mà phiên chỉ sinh ra khi thực đơn đã
          được CÔNG BỐ. Thực đơn mới duyệt xong nhưng chưa công bố thì chưa lên bảng nấu.
        </Alert>
      ) : (
        <div className="space-y-6">
          {/* Suất bị chặn phải nằm TRÊN CÙNG: bếp có thể nấu xong xuôi mà tới giờ phát mới biết
              có em không nhận được suất nào (BR-065/SRV-026). Việc cần làm là khai món thay thế
              ở Thư viện món trước giờ ăn, không phải nấu thêm. */}
          {sheet.blocked.length > 0 && (
            <Alert variant="danger" title={`${sheet.blocked.length} suất sẽ BỊ CHẶN lúc phát (BR-065)`}>
              <p className="mb-2">
                Các em dưới đây dị ứng đúng món của thực đơn nhưng món đó CHƯA khai món thay thế
                mặc định trong Thư viện món — hệ thống sẽ không cho phát và các em chưa có gì ăn.
                Khai món thay thế cho món gốc trước giờ ăn.
              </p>
              <ul className="space-y-0.5 text-sm">
                {sheet.blocked.map((b) => (
                  <li key={b.key}>
                    {b.sources[0]} — món <b>{b.dish}</b>
                  </li>
                ))}
              </ul>
            </Alert>
          )}

          <Card>
            <CardHeader>
              <CardTitle className="flex items-center gap-2">
                <Flame className="size-4 text-muted-foreground" />
                Món chính — nồi lớn ({sheet.main.length} món · {sheet.totalMain} suất)
              </CardTitle>
            </CardHeader>
            <CardContent>
              <SimpleTable<CookRow> columns={mainColumns} rows={sheet.main} rowKey={(l) => l.key} />
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
                  Các suất dưới đây KHÔNG nằm trong nồi lớn và đã được trừ khỏi số suất món chính:
                  suất thay món cho em dị ứng, và suất từ thực đơn riêng của nhóm chế độ ăn. Nấu
                  tách để tránh nhiễm chéo.
                </p>
                {sheet.special.map((l) => (
                  <div key={l.key} className="flex flex-wrap items-center gap-2 rounded-lg border p-2.5 text-sm">
                    <Badge variant={l.kind === "ALLERGY_SUB" ? "danger" : "warning"}>
                      {l.kind === "ALLERGY_SUB" ? "Thay món (dị ứng)" : groupLabel(l.audience)}
                    </Badge>
                    <span className="min-w-0 flex-1 font-medium">
                      {l.dish}
                      {l.substitutedFor && (
                        <span className="font-normal text-muted-foreground"> (thay {l.substitutedFor})</span>
                      )}
                    </span>
                    <Badge variant="neutral">{l.portions} suất</Badge>
                    <span className="text-xs text-muted-foreground">{l.sources.join(", ")}</span>
                  </div>
                ))}
              </CardContent>
            </Card>
          )}

          <Alert variant="info" title="Sau khi nấu">
            Ghi lại mẻ nấu ở <b>Sổ chế biến</b> (lô nguyên liệu lấy tự động từ phiếu xuất kho),
            rồi <b>Lưu mẫu</b> đủ món trước khi phục vụ.
          </Alert>

          <p className="flex items-start gap-1.5 text-xs text-muted-foreground">
            <TriangleAlert className="mt-0.5 size-3.5 shrink-0" />
            Số suất chỉ đếm phiên còn <b>chờ phát</b> — đã phát cho em nào thì trừ khỏi bảng. Mở
            giữa giờ ăn sẽ thấy số nhỏ dần, đó là số CÒN PHẢI NẤU chứ không phải tổng cả bữa.
          </p>
        </div>
      )}
    </>
  );
}
