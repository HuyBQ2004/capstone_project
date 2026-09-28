"use client";

import { useMemo, useState } from "react";
import Link from "next/link";
import { ArrowLeft, ArrowRight } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Alert } from "@/components/ui/alert";
import { Label, Select } from "@/components/ui/input";
import { Table, TableBody, TableCell, TableHead, TableHeader, TableRow } from "@/components/ui/table";
import { usePagedList, useUpdate } from "@/lib/api/hooks";
import type { Classroom, Student } from "@/lib/mock-data";

/**
 * 45_PromoteStudentsWorkspace.md — UC-177 (Promote Students to Next Academic Year).
 * BR-194: lên lớp là hành động tường minh, hỗ trợ loại trừ học sinh lưu ban;
 * học sinh bị loại trừ giữ nguyên lớp cũ (xử lý thủ công qua UC-090).
 * BR-025/MSG-010: chặn nếu lớp đích vượt sức chứa 50 · MSG-084 khi thành công.
 * Hồ sơ sức khỏe/dị ứng KHÔNG bị ảnh hưởng khi lên lớp (BR-194).
 */

const CAPACITY = 50;

export default function PromoteStudentsPage() {
  const { data: classrooms } = usePagedList<Classroom>("classrooms", { pageSize: 100 });
  const update = useUpdate<{ id: string; class?: string }>("students");

  const [sourceClass, setSourceClass] = useState("");
  const [targetClass, setTargetClass] = useState("");
  const [excluded, setExcluded] = useState<Set<string>>(new Set());
  const [done, setDone] = useState(false);
  const [processing, setProcessing] = useState(false);

  const { data: students } = usePagedList<Student>("students", {
    pageSize: 100,
    ...(sourceClass ? { class: sourceClass } : {}),
  });

  const rows = useMemo(
    () => (sourceClass ? students?.items ?? [] : []),
    [students, sourceClass]
  );
  const promoteCount = rows.length - excluded.size;
  const target = classrooms?.items.find((c) => c.id === targetClass);
  const overCapacity = target ? target.students + promoteCount > CAPACITY : false;

  function toggle(id: string) {
    setExcluded((prev) => {
      const next = new Set(prev);
      if (next.has(id)) next.delete(id);
      else next.add(id);
      return next;
    });
  }

  async function confirm() {
    if (!target || overCapacity || promoteCount <= 0) return;
    setProcessing(true);
    for (const s of rows) {
      if (excluded.has(s.id)) continue;
      await new Promise<void>((resolve) =>
        update.mutate({ id: s.id, class: target.id }, { onSettled: () => resolve() })
      );
    }
    setProcessing(false);
    setDone(true);
    toast.success(`Lên lớp học sinh thành công. (MSG-084) — ${promoteCount} học sinh chuyển sang lớp ${target.id}`);
  }

  return (
    <>
      <PageHeader
        title="Lên lớp hàng loạt"
        description="UC-177 — chọn lớp nguồn → lớp đích, loại trừ học sinh lưu ban (BR-194); hồ sơ sức khỏe/dị ứng giữ nguyên"
        actions={
          <Link href="/school-admin/academic-years">
            <Button variant="outline">
              <ArrowLeft /> Quay lại Năm học
            </Button>
          </Link>
        }
      />

      {/* Stepper */}
      <div className="mb-6 flex items-center gap-2 text-sm">
        <Badge variant={sourceClass ? "primary" : "neutral"}>1. Chọn lớp nguồn → đích</Badge>
        <Badge variant={sourceClass && targetClass ? "primary" : "neutral"}>2. Xem & loại trừ</Badge>
        <Badge variant={done ? "primary" : "neutral"}>3. Xác nhận</Badge>
      </div>

      {done ? (
        <Alert variant="success" title={`MSG-084 — Lên lớp thành công ${promoteCount} học sinh`}>
          {excluded.size > 0 &&
            `${excluded.size} học sinh bị loại trừ (lưu ban) giữ nguyên liên kết lớp cũ — xử lý thủ công sau qua Chi tiết lớp/Update Classroom (AF1, UC-090). `}
          <Link className="font-medium underline" href="/school-admin/classrooms">
            Xem danh sách lớp
          </Link>
        </Alert>
      ) : (
        <>
          <Card className="mb-6">
            <CardHeader>
              <CardTitle>Bước 1 — Lớp nguồn (năm học hiện tại) → Lớp đích (năm học mới)</CardTitle>
            </CardHeader>
            <CardContent>
              <div className="flex flex-wrap items-end gap-4">
                <div className="space-y-1.5">
                  <Label htmlFor="pr-src">Lớp nguồn (2025–2026)</Label>
                  <Select
                    id="pr-src"
                    value={sourceClass}
                    onChange={(e) => {
                      setSourceClass(e.target.value);
                      setExcluded(new Set());
                    }}
                  >
                    <option value="">— Chọn lớp —</option>
                    {(classrooms?.items ?? []).map((c) => (
                      <option key={c.id} value={c.id}>
                        {c.id} (Khối {c.grade}, {c.students} HS)
                      </option>
                    ))}
                  </Select>
                </div>
                <ArrowRight className="mb-2 size-5 text-muted-foreground" />
                <div className="space-y-1.5">
                  <Label htmlFor="pr-dst">Lớp đích (2026–2027 — cấu trúc đã sao chép, BR-194)</Label>
                  <Select id="pr-dst" value={targetClass} onChange={(e) => setTargetClass(e.target.value)}>
                    <option value="">— Chọn lớp —</option>
                    {(classrooms?.items ?? [])
                      .filter((c) => c.id !== sourceClass)
                      .map((c) => (
                        <option key={c.id} value={c.id}>
                          {c.id} (Khối {c.grade}, hiện {c.students}/{CAPACITY})
                        </option>
                      ))}
                  </Select>
                </div>
              </div>
              {overCapacity && (
                <Alert variant="danger" className="mt-4" title="MSG-010 — Vượt sức chứa lớp đích">
                  Vượt sức chứa lớp. Tối đa 50 học sinh. (BR-025) — hãy
                  loại trừ bớt học sinh hoặc chọn lớp đích khác.
                </Alert>
              )}
            </CardContent>
          </Card>

          {sourceClass && (
            <Card>
              <CardHeader>
                <CardTitle>
                  Bước 2 — Danh sách học sinh lớp {sourceClass} (bỏ chọn = lưu ban, BR-194)
                </CardTitle>
              </CardHeader>
              <CardContent className="space-y-3">
                <div className="flex gap-2 text-sm">
                  <Badge variant="success">{promoteCount} sẽ lên lớp</Badge>
                  <Badge variant="neutral">{excluded.size} loại trừ (lưu ban)</Badge>
                </div>
                <Table>
                  <TableHeader>
                    <TableRow>
                      <TableHead className="w-10">Lên lớp</TableHead>
                      <TableHead>Mã HS</TableHead>
                      <TableHead>Họ tên</TableHead>
                      <TableHead>Dị ứng (giữ nguyên hồ sơ — BR-194)</TableHead>
                    </TableRow>
                  </TableHeader>
                  <TableBody>
                    {rows.map((s) => {
                      const isExcluded = excluded.has(s.id);
                      return (
                        <TableRow key={s.id} className={isExcluded ? "opacity-50" : ""}>
                          <TableCell>
                            <input
                              type="checkbox"
                              className="size-4 accent-primary"
                              checked={!isExcluded}
                              onChange={() => toggle(s.id)}
                              aria-label={`Lên lớp ${s.name}`}
                            />
                          </TableCell>
                          <TableCell className="font-medium">{s.id}</TableCell>
                          <TableCell>{s.name}</TableCell>
                          <TableCell>
                            {s.allergies.length > 0 ? (
                              <div className="flex flex-wrap gap-1">
                                {s.allergies.map((a) => (
                                  <Badge key={a} variant="danger">{a}</Badge>
                                ))}
                              </div>
                            ) : (
                              "—"
                            )}
                          </TableCell>
                        </TableRow>
                      );
                    })}
                  </TableBody>
                </Table>
                <div className="border-t pt-4">
                  <Button
                    disabled={!targetClass || overCapacity || promoteCount <= 0 || processing}
                    onClick={confirm}
                  >
                    Xác nhận lên lớp {promoteCount} học sinh (MSG-084)
                  </Button>
                </div>
              </CardContent>
            </Card>
          )}

          {!sourceClass && (
            <Card className="p-10 text-center text-muted-foreground">
              Chưa chọn khối/lớp nguồn — chọn để xem danh sách học sinh.
            </Card>
          )}
        </>
      )}
    </>
  );
}
