"use client";

import { useState } from "react";
import { Layers, Plus, Pencil, Trash2, RotateCcw, X } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
import { Card, CardContent, CardHeader, CardTitle } from "@/components/ui/card";
import { Button } from "@/components/ui/button";
import { Badge } from "@/components/ui/badge";
import { Input, Label } from "@/components/ui/input";
import { SimpleTable } from "@/components/app/simple-table";
import { usePlainList, useCreate, useUpdate, useDelete } from "@/lib/api/hooks";
import type { GradeDto } from "@/lib/api/types";

/**
 * Quản lý Khối (grade) — School Admin.
 *
 * Danh mục khối THEO TRƯỜNG (school-scoped qua JWT, không có `schoolId` ở FE),
 * nguồn để `Lớp học` (`/school-admin/classrooms`) chọn khối khi tạo/lọc lớp thay
 * vì hằng số cứng 1–12 (BR-024 cũ giả định mọi trường đều 12 khối phổ thông —
 * sai với trường mầm non/tiểu học riêng chỉ có vài khối).
 */

interface FormState {
  name: string;
  sortOrder: string;
}

const EMPTY_FORM: FormState = { name: "", sortOrder: "1" };

export default function GradesPage() {
  const { data: grades = [], isLoading, isError, error, refetch } = usePlainList<GradeDto>("grades");

  const create = useCreate<Partial<GradeDto>, GradeDto>("grades", { handledError: true });
  const update = useUpdate<{ id: string } & Partial<GradeDto>, GradeDto>("grades", { handledError: true });
  const remove = useDelete("grades", { handledError: true });

  const [showForm, setShowForm] = useState(false);
  const [editingId, setEditingId] = useState<string | null>(null);
  const [form, setForm] = useState<FormState>(EMPTY_FORM);
  const [saving, setSaving] = useState(false);

  const sorted = [...grades].sort((a, b) => (a.sortOrder ?? 0) - (b.sortOrder ?? 0));

  function openCreate() {
    setEditingId(null);
    setForm({ name: "", sortOrder: String(grades.length + 1) });
    setShowForm(true);
  }

  // `id` optional trên `GradeDto` (springdoc) — `editingId === null` nghĩa là "đang TẠO mới",
  // nên bản ghi thiếu id phải rơi về `null` chứ không phải `undefined`.
  function openEdit(g: GradeDto) {
    setEditingId(g.id ?? null);
    setForm({ name: g.name ?? "", sortOrder: String(g.sortOrder ?? 1) });
    setShowForm(true);
  }

  function closeForm() {
    setShowForm(false);
    setEditingId(null);
    setForm(EMPTY_FORM);
  }

  function patch(key: keyof FormState, value: string) {
    setForm((prev) => ({ ...prev, [key]: value }));
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();
    if (!form.name.trim()) {
      toast.error("Tên khối không được để trống.");
      return;
    }
    const sortOrder = Number(form.sortOrder);
    if (!Number.isFinite(sortOrder)) {
      toast.error("Thứ tự sắp xếp phải là số.");
      return;
    }
    setSaving(true);
    const payload = { name: form.name.trim(), sortOrder };

    if (editingId) {
      update.mutate(
        { id: editingId, ...payload },
        {
          onSuccess: () => {
            toast.success(`Đã cập nhật khối "${form.name}".`);
            closeForm();
            refetch();
          },
          onError: () => toast.error("Cập nhật thất bại. Thử lại sau."),
          onSettled: () => setSaving(false),
        }
      );
    } else {
      create.mutate(payload, {
        onSuccess: () => {
          toast.success(`Đã thêm khối "${form.name}".`);
          closeForm();
          refetch();
        },
        onError: () => toast.error("Thêm khối thất bại. Thử lại sau."),
        onSettled: () => setSaving(false),
      });
    }
  }

  function toggleActive(g: GradeDto) {
    if (!g.id) return;   // `id` dựng URL PATCH — thiếu thì không gọi được
    update.mutate(
      { id: g.id, active: !g.active },
      {
        onSuccess: () => {
          toast.success(g.active ? `Đã vô hiệu hoá khối "${g.name}".` : `Đã kích hoạt lại khối "${g.name}".`);
          refetch();
        },
        onError: () => toast.error("Cập nhật trạng thái thất bại."),
      }
    );
  }

  function handleDeactivate(g: GradeDto) {
    if (!g.id) return;   // cùng lý do với toggleActive()
    if (!confirm(`Vô hiệu hoá khối "${g.name}"? Lớp học đang thuộc khối này vẫn giữ nguyên liên kết.`)) return;
    remove.mutate(g.id, {
      onSuccess: () => {
        toast.success(`Đã vô hiệu hoá khối "${g.name}".`);
        refetch();
      },
      onError: () => toast.error("Vô hiệu hoá thất bại."),
    });
  }

  return (
    <div className="space-y-6">
      <PageHeader
        title="Quản lý Khối"
        description="Danh mục khối của trường — dùng để tạo và lọc lớp học. Vô hiệu hoá (xoá mềm) không ảnh hưởng lớp học đã có."
      />

      <div className="flex justify-end">
        <Button onClick={openCreate} className="shrink-0">
          <Plus className="size-4 mr-1.5" /> Thêm khối
        </Button>
      </div>

      {showForm && (
        <Card className="border-primary/30 bg-primary/5">
          <CardHeader className="pb-3">
            <CardTitle className="text-base flex items-center justify-between">
              <span>{editingId ? "Sửa khối" : "Thêm khối mới"}</span>
              <Button variant="ghost" size="icon" onClick={closeForm}>
                <X className="size-4" />
              </Button>
            </CardTitle>
          </CardHeader>
          <CardContent>
            <form onSubmit={handleSubmit} className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-4">
              <div className="space-y-1">
                <Label htmlFor="gr-name">Tên khối *</Label>
                <Input
                  id="gr-name"
                  placeholder="VD: Khối 1"
                  value={form.name}
                  onChange={(e) => patch("name", e.target.value)}
                  required
                />
              </div>
              <div className="space-y-1">
                <Label htmlFor="gr-order">Thứ tự sắp xếp *</Label>
                <Input
                  id="gr-order"
                  type="number"
                  min={0}
                  step={1}
                  value={form.sortOrder}
                  onChange={(e) => patch("sortOrder", e.target.value)}
                  required
                />
              </div>
              <div className="flex items-end gap-2 sm:col-span-2 lg:col-span-1">
                <Button type="submit" disabled={saving} className="flex-1">
                  {saving ? "Đang lưu…" : editingId ? "Cập nhật" : "Thêm khối"}
                </Button>
                <Button type="button" variant="outline" onClick={closeForm}>Huỷ</Button>
              </div>
            </form>
          </CardContent>
        </Card>
      )}

      <Card>
        <CardHeader>
          <CardTitle className="text-base flex items-center gap-2">
            <Layers className="size-4 text-primary" />
            Danh sách khối
            <Badge variant="neutral" className="ml-1">{sorted.length}</Badge>
          </CardTitle>
        </CardHeader>
        <CardContent className="p-0">
          {/*
            * Chuyển từ `<table>` thô sang `SimpleTable` (08/08/2026). Bản cũ tự viết lại
            * skeleton + empty state bằng tay và KHÔNG có nhánh lỗi — query hỏng cũng hiện
            * "Chưa có khối nào", mời người dùng tạo trùng danh mục đã tồn tại.
            */}
          <SimpleTable<GradeDto>
            rows={sorted}
            rowKey={(g) => g.id ?? ""}
            isLoading={isLoading}
            isError={isError}
            error={error}
            emptyMessage={'Chưa có khối nào — bấm "Thêm khối" để tạo mới.'}
            columns={[
              { header: "Tên khối", cell: (g) => <span className="font-medium">{g.name}</span> },
              { header: "Thứ tự", cell: (g) => <span className="text-muted-foreground">{g.sortOrder}</span> },
              {
                header: "Trạng thái",
                cell: (g) => (
                  <Badge variant={g.active ? "success" : "neutral"}>
                    {g.active ? "Đang dùng" : "Đã vô hiệu hoá"}
                  </Badge>
                ),
              },
              {
                header: "Thao tác",
                className: "text-right",
                cell: (g) => (
                  <div className="flex items-center justify-end gap-1">
                    <Button variant="ghost" size="icon" className="size-7" onClick={() => openEdit(g)} title="Sửa">
                      <Pencil className="size-3.5" />
                    </Button>
                    {g.active ? (
                      <Button
                        variant="ghost"
                        size="icon"
                        className="size-7 text-destructive hover:text-destructive"
                        onClick={() => handleDeactivate(g)}
                        title="Vô hiệu hoá"
                      >
                        <Trash2 className="size-3.5" />
                      </Button>
                    ) : (
                      <Button
                        variant="ghost"
                        size="icon"
                        className="size-7"
                        onClick={() => toggleActive(g)}
                        title="Kích hoạt lại"
                      >
                        <RotateCcw className="size-3.5" />
                      </Button>
                    )}
                  </div>
                ),
              },
            ]}
          />
        </CardContent>
      </Card>

      <p className="text-xs text-muted-foreground flex items-center gap-1">
        <RotateCcw className="size-3" />
        Vô hiệu hoá (xoá) là soft-delete — khối không bị xoá cứng, lớp học cũ vẫn giữ nguyên liên kết.
      </p>
    </div>
  );
}
