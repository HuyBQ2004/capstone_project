"use client";

import { useState } from "react";
import { ClipboardList, Eye, ShieldCheck, Soup, TriangleAlert } from "lucide-react";
import { PageHeader } from "@/components/app/page-header";
import { StatCard } from "@/components/app/stat-card";
import { StatusBadge } from "@/components/app/status-badge";
import { Alert } from "@/components/ui/alert";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
} from "@/components/ui/dialog";
import { Input, Label, Select } from "@/components/ui/input";
import { Tabs, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList, useSpecialMeals } from "@/lib/api/hooks";
import type { PersistedMenu } from "@/components/app/menu-form";
import {
  cookSheet,
  gradeLevelsOf,
  mealServingGate,
  menuAudienceOf,
  isMenuLive,
  MEAL_TYPE_LABEL,
  DIET_GROUPS,
  type Classroom,
  type InspectionSession,
  type MealType,
  type MenuTemplate,
  type Student,
  type StudentDistribution,
} from "@/lib/mock-data";

function todayIso(): string {
  const d = new Date();
  const p = (n: number) => String(n).padStart(2, "0");
  return `${d.getFullYear()}-${p(d.getMonth() + 1)}-${p(d.getDate())}`;
}

/** Thực đơn CHUẨN (audience ALL) của đúng khối, đang phục vụ đúng ngày. */
function standardMenuOf(
  menus: PersistedMenu[],
  grade: string,
  dateIso: string
): PersistedMenu | undefined {
  return menus.find(
    (m) => menuAudienceOf(m) === "ALL" && isMenuLive(m) && m.menuDateIso === dateIso && m.grade === grade
  );
}

/**
 * Mẫu chế độ ăn riêng (`MenuTemplate.audience !== "ALL"`) đã được ÁP vào đúng
 * buổi (mealType) của thực đơn chuẩn — nhận xét Minh 28/07/2026: "Lịch thực
 * đơn tuần" (25/07/2026) gộp mẫu bằng `MealBlock.templates[]` ngay trên thực
 * đơn chuẩn, KHÔNG còn tạo `Menu` audience riêng/`MenuVariant` như hệ cũ
 * (`specialMenuMeals`) — nên phải đọc từ `templates[]` mới thấy mẫu Halal/Chay
 * người dùng vừa thêm qua màn đó.
 */
function dietTemplatesInBlock(
  standardMenu: PersistedMenu | undefined,
  mealType: MealType,
  templates: MenuTemplate[]
): MenuTemplate[] {
  const block = (standardMenu?.blocksData ?? []).find((b) => b.mealType === mealType);
  const ids = new Set((block?.templates ?? []).map((t) => t.templateId));
  return templates.filter((t) => ids.has(t.id) && t.audience !== "ALL");
}

/**
 * Nhận xét Minh 27–28/07/2026: bếp chỉ cần biết CHIA BAO NHIÊU SUẤT tới từng
 * lớp (tiêu chuẩn / chế độ ăn riêng) — em nào ăn suất nào là việc của giáo
 * viên khi quét phát, không phải màn này. Sĩ số lấy từ `Classroom.students`
 * (sĩ số khai báo — cùng nguồn với `gradeEnrollmentOf`/suất của Bảng nấu ăn
 * hôm nay), KHÔNG đếm theo mảng `STUDENTS` (mock chỉ seed rải rác vài lớp nên
 * đếm trực tiếp ra sai số rất xa sĩ số thật). Chỉ tách dòng chế độ ăn khi CẢ 2
 * đúng: (a) buổi đó thực đơn chuẩn có mẫu riêng cho nhóm đó (mục 3), VÀ (b)
 * lớp có học sinh khai chế độ ăn đó (mục 5) — nếu chỉ có mẫu mà lớp không ai
 * theo chế độ đó, hoặc ngược lại, vẫn giữ nguyên "Tiêu chuẩn".
 *
 * Nhận xét Minh 28/07/2026 (đợt 2): PHẢI có `standardMenu` (khối đó đã công
 * bố thực đơn cho đúng ngày+bữa) mới được coi là có suất — trước đó hàm luôn
 * trả sĩ số kể cả khi khối chưa có thực đơn ngày đó, nên cộng dồn "Số suất
 * phân phát" của mọi khối (kể cả khối chưa nấu bữa này) vượt xa "Suất kế
 * hoạch" (vốn chỉ cộng các khối THỰC SỰ có thực đơn, qua `cookSheet`).
 */
function classPortionLines(
  classroom: Classroom,
  dateIso: string,
  mealType: MealType,
  menus: PersistedMenu[],
  templates: MenuTemplate[],
  students: Student[]
): { hasMenu: boolean; standard: number; diets: { label: string; count: number }[] } {
  const standardMenu = standardMenuOf(menus, `Khối ${classroom.grade}`, dateIso);
  const hasBlock = (standardMenu?.blocksData ?? []).some((b) => b.mealType === mealType);
  if (!standardMenu || !hasBlock) return { hasMenu: false, standard: 0, diets: [] };
  const activeGroupIds = new Set(
    dietTemplatesInBlock(standardMenu, mealType, templates).map((t) => t.audience)
  );
  const classStudents = students.filter((s) => s.class === classroom.id);
  let standard = classroom.students;
  const diets: { label: string; count: number }[] = [];
  for (const group of DIET_GROUPS) {
    if (!activeGroupIds.has(group.id)) continue;
    const count = classStudents.filter((s) => s.specialDiet === group.matchDiet).length;
    if (count > 0) {
      diets.push({ label: group.label, count });
      standard -= count;
    }
  }
  return { hasMenu: true, standard, diets };
}

/**
 * 24_MealServiceManagementDashboard + 25/26 (gộp Web) — UX 07/07/2026:
 * chọn KHỐI trước → danh sách LỚP của khối → "Chi tiết" mở popup to CHỈ hiện
 * học sinh có chế độ ăn đặc biệt / dị ứng của lớp đó, kèm bộ lọc.
 * BR-125: suất phân công · BR-082: suất đặc biệt chế biến riêng.
 * Service Caterers chỉ xem (Teacher quét phát).
 */
export default function MealServicePage() {
  const { data: studentsPage } = usePagedList<Student>("students", { pageSize: 300 });
  const students = studentsPage?.items ?? [];
  const { data: classroomsPage } = usePagedList<Classroom>("classrooms", { pageSize: 100 });
  const classrooms = classroomsPage?.items ?? [];
  // Khối lấy từ cấu hình lớp học THẬT của Ban giám hiệu (db), không phải hằng số:
  // trường mở thêm khối là màn này thấy ngay, chưa mở khối nào thì không mời chọn.
  const { data: classroomPage } = usePagedList<Classroom>("classrooms", { pageSize: 200 });
  const gradeLevels = gradeLevelsOf(classroomPage?.items ?? []);

  // Nhận xét Minh 27/07: "phục vụ" là chuyện của một NGÀY + BỮA cụ thể (nấu/chia
  // riêng từng buổi) — trước đây màn này ngầm định "hôm nay" và không có bữa nào
  // để chọn, nên suất kế hoạch/trạng thái kiểm thực không tra đúng buổi đang xem.
  const { data: menusPage } = usePagedList<PersistedMenu>("menus", { pageSize: 200 });
  const menus = menusPage?.items ?? [];
  const { variants } = useSpecialMeals();
  // "Lịch thực đơn tuần" (25/07/2026) áp mẫu chế độ ăn thẳng vào
  // `MealBlock.templates[]` của thực đơn CHUẨN — nguồn đúng cho "thực đơn
  // khác" hiện tại, khác với `useSpecialMeals()`/`MenuVariant` (hệ cũ, chỉ
  // thấy Menu có `audience` riêng, nay không còn được ghi khi áp mẫu qua lịch).
  const { data: templatesPage } = usePagedList<MenuTemplate>("menu-templates", { pageSize: 200 });
  const templates = templatesPage?.items ?? [];
  const { data: inspectionsPage } = usePagedList<InspectionSession>("inspections", { pageSize: 100 });
  const inspections = inspectionsPage?.items ?? [];

  const [dateIso, setDateIso] = useState(todayIso());
  const [mealType, setMealType] = useState<MealType>("LUNCH");
  const [grade, setGrade] = useState<string>("Khối 3");
  const [viewClass, setViewClass] = useState<Classroom | null>(null);
  // Bộ lọc trong dialog chi tiết — lọc theo TÊN chế độ ăn cụ thể của lớp đang xem
  const [dietFilter, setDietFilter] = useState("");

  const vnDate = dateIso.split("-").reverse().join("/");
  const sheet = cookSheet(dateIso, mealType, menus as never, variants);
  const totalPlanned = sheet.totalMain + sheet.totalSpecial;
  const standardMenusForMeal = menus.filter(
    (m) =>
      menuAudienceOf(m) === "ALL" &&
      isMenuLive(m) &&
      m.menuDateIso === dateIso &&
      (m.blocksData ?? []).some((b) => b.mealType === mealType)
  );
  const dietTemplatesForMeal = standardMenusForMeal.flatMap((m) =>
    dietTemplatesInBlock(m, mealType, templates)
  );
  const gate = mealServingGate(dateIso, mealType, inspections);
  // Chỉ 2 trạng thái hiển thị theo yêu cầu — "Chờ kiểm thực" gộp cả 2 trường hợp
  // "chưa có phiên" và "có phiên nhưng FAILED" (BR-094 vẫn chặn phục vụ), phân
  // biệt bằng màu/ghi chú để bếp không hiểu nhầm là chỉ cần đợi.
  const servingReady = !gate.noSession && !gate.blocked;
  const servingLabel = servingReady ? "Sẵn sàng" : "Chờ kiểm thực";
  const servingTone = servingReady ? "success" : gate.blocked ? "danger" : "warning";
  const servingHint = servingReady
    ? "Đủ 3 bước kiểm thực Đạt — được phép phục vụ (BR-094)."
    : gate.blocked
      ? `Không đạt: ${gate.failedSteps.join(", ")} — cần xử lý lại, không tự hết (BR-094).`
      : "Chưa có phiên kiểm thực ba bước cho bữa này.";

  // Sổ phát suất THẬT của giáo viên — vẫn dùng để hiện "Suất phân công gần nhất"
  // của TỪNG học sinh trong dialog chi tiết (Service Caterers chỉ xem).
  const { data: distPage } = usePagedList<StudentDistribution>("student-distributions", {
    pageSize: 200,
  });
  const servingRows = distPage?.items ?? [];

  const gradeNumber = Number(grade.replace("Khối ", ""));
  const gradeClasses = classrooms.filter((c) => c.grade === gradeNumber);

  // Chế độ ăn cụ thể có mặt trong lớp đang xem — nguồn cho dropdown lọc (mục 7).
  const classDietOptions = viewClass
    ? [...new Set(
        students
          .filter((s) => s.class === viewClass.id && s.specialDiet)
          .map((s) => s.specialDiet as string)
      )]
    : [];

  // Học sinh ĐẶC BIỆT của lớp đang xem (chỉ dị ứng / chế độ ăn — không hiện HS thường)
  const specialRows = (viewClass ? students.filter((s) => s.class === viewClass.id) : [])
    .filter((s) => s.allergies.length > 0 || s.specialDiet)
    .filter((s) => !dietFilter || s.specialDiet === dietFilter);

  /** Suất phân công mới nhất của học sinh (nếu có trong sổ phân phát). */
  function latestServing(studentId: string) {
    return servingRows.find((r) => r.studentId === studentId);
  }

  return (
    <>
      <PageHeader
        title="Phục vụ bữa ăn"
        description="Chọn ngày → bữa → khối → lớp; chỉ phục vụ sau khi cả 3 bước kiểm thực Passed (BR-094/BR-098)"
      />

      <div className="mb-6 flex flex-wrap items-end gap-4">
        <div className="space-y-1.5">
          <Label htmlFor="ms-date">Ngày phục vụ</Label>
          <Input
            id="ms-date"
            type="date"
            className="w-44"
            value={dateIso}
            onChange={(e) => setDateIso(e.target.value)}
          />
        </div>
        <div className="space-y-1.5">
          <Label htmlFor="ms-meal">Bữa ăn</Label>
          <Select
            id="ms-meal"
            className="w-40"
            value={mealType}
            onChange={(e) => setMealType(e.target.value as MealType)}
          >
            {(Object.keys(MEAL_TYPE_LABEL) as MealType[]).map((m) => (
              <option key={m} value={m}>{MEAL_TYPE_LABEL[m]}</option>
            ))}
          </Select>
        </div>
      </div>

      <div className="mb-6 grid gap-4 sm:grid-cols-3">
        <StatCard
          label="Suất kế hoạch"
          value={totalPlanned.toLocaleString("vi-VN")}
          hint={`${MEAL_TYPE_LABEL[mealType]} · ${vnDate}`}
          icon={Soup}
          tone="primary"
        />
        <StatCard
          label="Thực đơn phục vụ"
          value={`${standardMenusForMeal.length} tiêu chuẩn`}
          hint={`${dietTemplatesForMeal.length} thực đơn khác (chế độ ăn riêng)`}
          icon={ClipboardList}
          tone="info"
        />
        <StatCard
          label="Trạng thái phục vụ"
          value={servingLabel}
          hint={servingHint}
          icon={ShieldCheck}
          tone={servingTone}
        />
      </div>

      {/* Bước 1: chọn khối */}
      <Tabs value={grade} onValueChange={setGrade} className="mb-4">
        <TabsList>
          {gradeLevels.map((g) => (
            <TabsTrigger key={g} value={g}>
              {g}
            </TabsTrigger>
          ))}
        </TabsList>
      </Tabs>

      {/* Bước 2: các lớp của khối */}
      <Card>
        <CardHeader>
          <CardTitle>
            Các lớp {grade} ({gradeClasses.length}) — bấm Chi tiết xem học sinh đặc biệt
          </CardTitle>
        </CardHeader>
        <CardContent>
          <Table>
            <TableHeader>
              <TableRow>
                <TableHead>Lớp</TableHead>
                <TableHead>GV chủ nhiệm</TableHead>
                <TableHead>Sĩ số</TableHead>
                <TableHead>HS dị ứng / đặc biệt</TableHead>
                <TableHead>Số suất phân phát</TableHead>
                <TableHead></TableHead>
              </TableRow>
            </TableHeader>
            <TableBody>
              {gradeClasses.map((c) => {
                const specialCount = students.filter(
                  (s) => s.class === c.id && (s.allergies.length > 0 || s.specialDiet)
                ).length;
                const portions = classPortionLines(c, dateIso, mealType, menus, templates, students);
                return (
                  <TableRow key={c.id}>
                    <TableCell className="font-medium">{c.id}</TableCell>
                    <TableCell>{c.teacher}</TableCell>
                    <TableCell>{c.students}</TableCell>
                    <TableCell>
                      {specialCount > 0 ? (
                        <Badge variant="warning">{specialCount} HS</Badge>
                      ) : c.allergies > 0 ? (
                        <Badge variant="warning">{c.allergies} HS (theo sổ lớp)</Badge>
                      ) : (
                        <Badge variant="success">Không</Badge>
                      )}
                    </TableCell>
                    <TableCell>
                      {portions.hasMenu ? (
                        <div className="space-y-0.5 text-sm">
                          <p>Tiêu chuẩn × {portions.standard}</p>
                          {portions.diets.map((d) => (
                            <p key={d.label} className="text-muted-foreground">
                              {d.label} × {d.count}
                            </p>
                          ))}
                        </div>
                      ) : (
                        <span className="text-sm text-muted-foreground">Chưa có thực đơn bữa này</span>
                      )}
                    </TableCell>
                    <TableCell>
                      <Button
                        variant="outline"
                        size="sm"
                        onClick={() => {
                          setViewClass(c);
                          setDietFilter("");
                        }}
                      >
                        <Eye /> Chi tiết
                      </Button>
                    </TableCell>
                  </TableRow>
                );
              })}
            </TableBody>
          </Table>
        </CardContent>
      </Card>

      {/* Bước 3: popup to — CHỈ học sinh đặc biệt/dị ứng của lớp + bộ lọc */}
      <Dialog open={viewClass != null} onOpenChange={(v) => !v && setViewClass(null)}>
        <DialogContent className="max-h-[90vh] overflow-y-auto sm:max-w-4xl">
          <DialogHeader>
            <DialogTitle>
              Lớp {viewClass?.id} — học sinh chế độ ăn đặc biệt & dị ứng
            </DialogTitle>
            <DialogDescription>
              GVCN {viewClass?.teacher} · sĩ số {viewClass?.students} · chỉ hiển thị
              học sinh cần lưu ý khi chia suất (BR-125) — Service Caterers
              chỉ xem, giáo viên là người quét phát.
            </DialogDescription>
          </DialogHeader>

          <div className="flex flex-wrap items-center gap-2">
            <Select
              className="h-8 w-52"
              value={dietFilter}
              onChange={(e) => setDietFilter(e.target.value)}
              aria-label="Lọc theo chế độ ăn"
            >
              <option value="">Tất cả (dị ứng + chế độ ăn)</option>
              {classDietOptions.map((d) => (
                <option key={d} value={d}>{d}</option>
              ))}
            </Select>
            <Badge variant="info" className="ml-auto">{specialRows.length} học sinh</Badge>
          </div>

          {viewClass && students.every((s) => s.class !== viewClass.id) ? (
            <Alert variant="info" title="Chưa có dữ liệu học sinh chi tiết">
              Lớp {viewClass.id} chưa được seed danh sách học sinh trong mock —
              sổ lớp ghi nhận {viewClass.allergies} HS có dị ứng.
            </Alert>
          ) : specialRows.length === 0 ? (
            <Alert variant="success" title="Không có học sinh nào khớp bộ lọc">
              Lớp {viewClass?.id} không có học sinh cần suất đặc biệt theo bộ lọc hiện tại.
            </Alert>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Học sinh</TableHead>
                  <TableHead>Dị ứng (BR-031)</TableHead>
                  <TableHead>Chế độ ăn</TableHead>
                  <TableHead>Suất phân công gần nhất (BR-125)</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {specialRows.map((s) => {
                  const serving = latestServing(s.id);
                  return (
                    <TableRow key={s.id} className={serving?.allergyWarning ? "bg-destructive/5" : ""}>
                      <TableCell className="font-medium">
                        {s.name}
                        {s.allergySeverity === "Severe" && (
                          <Badge variant="critical" className="ml-1.5">Severe</Badge>
                        )}
                      </TableCell>
                      <TableCell>
                        <div className="flex flex-wrap gap-1">
                          {s.allergies.length > 0
                            ? s.allergies.map((a) => (
                                <Badge key={a} variant="danger">{a}</Badge>
                              ))
                            : "—"}
                        </div>
                      </TableCell>
                      <TableCell>
                        {s.specialDiet ? <Badge variant="info">{s.specialDiet}</Badge> : "—"}
                      </TableCell>
                      <TableCell className="max-w-72">
                        {serving ? (
                          <>
                            <p className="text-sm">{serving.assignedMeal}</p>
                            {serving.specialMeal && (
                              <p className="mt-0.5 text-xs">
                                <Badge variant="warning" className="mr-1">Suất đặc biệt</Badge>
                                <span className="text-muted-foreground">{serving.specialMeal}</span>
                              </p>
                            )}
                            {serving.allergyWarning && (
                              <p className="mt-0.5 text-xs font-medium text-destructive">
                                <TriangleAlert className="mr-1 inline size-3" />
                                {serving.allergyWarning}
                              </p>
                            )}
                            <p className="mt-0.5 text-xs text-muted-foreground">
                              {serving.date} · <StatusBadge status={serving.status} />
                            </p>
                          </>
                        ) : (
                          <span className="text-muted-foreground">Chưa có trong sổ phân phát</span>
                        )}
                      </TableCell>
                    </TableRow>
                  );
                })}
              </TableBody>
            </Table>
          )}
          <DialogFooter>
            <Button variant="outline" onClick={() => setViewClass(null)}>Đóng</Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
