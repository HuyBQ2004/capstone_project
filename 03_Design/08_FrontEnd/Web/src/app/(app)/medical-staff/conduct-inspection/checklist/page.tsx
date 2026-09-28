"use client";

/**
 * 12_ConductFoodSafetyInspectionScreen.md, mục "Quản lý Checklist (UC-186)" — BR-210.
 * Medical Staff tự CRUD nhóm/hạng mục checklist bếp + chú thích căn cứ pháp lý (icon "ℹ"),
 * KHÔNG qua System Admin, KHÔNG cần phê duyệt (BR-210 thay điều khoản cấu hình tập trung
 * của BR-207 gốc — quyết định 06/07/2026). Xóa nhóm/hạng mục đang được tham chiếu bởi
 * phiên kiểm tra đã lưu → cảnh báo xác nhận MSG-094 (không chặn cứng) — dữ liệu lịch sử
 * giữ nguyên (phiên đã lưu tham chiếu theo TÊN hạng mục), chỉ ẩn khỏi phiên mới.
 * Mọi thay đổi ghi audit log chung khi lưu (BR-170, qua generic handler mock API).
 * Sắp xếp thứ tự: nút lên/xuống (bản thật dùng kéo-thả theo Screen Spec — drag handle).
 */

import { useEffect, useState } from "react";
import Link from "next/link";
import { ArrowDown, ArrowUp, ChevronLeft, ListChecks, Pencil, Plus, Save, Trash2 } from "lucide-react";
import { toast } from "sonner";
import { PageHeader } from "@/components/app/page-header";
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
import { Label, Textarea } from "@/components/ui/input";
import { useCreate, useDelete, usePagedList, useUpdate } from "@/lib/api/hooks";
import type { ChecklistGroup, ChecklistItem, KitchenInspection } from "@/lib/mock-data";
import { cn } from "@/lib/utils";

let localId = 0;
const newId = (prefix: string) => `${prefix}-NEW-${++localId}`;

export default function ManageChecklistPage() {
  const { data, isLoading } = usePagedList<ChecklistGroup>("checklist-groups", { pageSize: 50 });
  const { data: kitchenData } = usePagedList<KitchenInspection>("kitchen-inspections", { pageSize: 50 });
  const createGroup = useCreate<Record<string, unknown>, ChecklistGroup>("checklist-groups");
  const updateGroup = useUpdate<{ id: string } & Partial<ChecklistGroup>>("checklist-groups");
  const deleteGroup = useDelete("checklist-groups");

  // Bản nháp cục bộ — chỉ ghi xuống API khi bấm "Lưu thay đổi" (field M7)
  const [groups, setGroups] = useState<ChecklistGroup[]>([]);
  const [deletedGroupIds, setDeletedGroupIds] = useState<string[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);
  const [dirty, setDirty] = useState(false);
  // Dialog xác nhận xóa (MSG-094 khi mục/nhóm đang được tham chiếu)
  const [confirmDelete, setConfirmDelete] = useState<
    { kind: "group"; group: ChecklistGroup } | { kind: "item"; groupId: string; item: ChecklistItem } | null
  >(null);
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (data && !dirty && groups.length === 0) {
      const sorted = [...data.items].sort((a, b) => a.order - b.order);
      setGroups(sorted);
      if (sorted.length > 0) setSelectedGroupId(sorted[0].id);
    }
  }, [data, dirty, groups.length]);

  const selectedGroup = groups.find((g) => g.id === selectedGroupId) ?? null;

  // MSG-094: hạng mục "đang được tham chiếu" = tên xuất hiện trong failedItems/itemFindings
  // của phiên kiểm tra đã lưu (phiên lưu theo TÊN nên xóa không mất dữ liệu lịch sử)
  const referencedNames = new Set<string>();
  for (const k of kitchenData?.items ?? []) {
    for (const n of k.failedItems ?? []) referencedNames.add(n);
    for (const f of k.itemFindings ?? []) referencedNames.add(f.item);
  }

  function mutateGroups(fn: (gs: ChecklistGroup[]) => ChecklistGroup[]) {
    setGroups((gs) => fn(gs));
    setDirty(true);
  }

  function addGroup() {
    const name = window.prompt("Tên nhóm mới (bắt buộc, BR-210):")?.trim();
    if (!name) return;
    const g: ChecklistGroup = { id: newId("CLG"), name, order: groups.length + 1, items: [] };
    mutateGroups((gs) => [...gs, g]);
    setSelectedGroupId(g.id);
  }

  function renameGroup(g: ChecklistGroup) {
    const name = window.prompt("Sửa tên nhóm:", g.name)?.trim();
    if (!name) return;
    mutateGroups((gs) => gs.map((x) => (x.id === g.id ? { ...x, name } : x)));
  }

  function requestDeleteGroup(g: ChecklistGroup) {
    // Nhóm có hạng mục → luôn xác nhận trước (field M3)
    if (g.items.length > 0) setConfirmDelete({ kind: "group", group: g });
    else doDeleteGroup(g);
  }

  function doDeleteGroup(g: ChecklistGroup) {
    if (!g.id.includes("-NEW-")) setDeletedGroupIds((ids) => [...ids, g.id]);
    mutateGroups((gs) => gs.filter((x) => x.id !== g.id).map((x, i) => ({ ...x, order: i + 1 })));
    if (selectedGroupId === g.id) setSelectedGroupId(null);
    setConfirmDelete(null);
  }

  function moveGroup(g: ChecklistGroup, dir: -1 | 1) {
    mutateGroups((gs) => {
      const idx = gs.findIndex((x) => x.id === g.id);
      const to = idx + dir;
      if (to < 0 || to >= gs.length) return gs;
      const next = [...gs];
      [next[idx], next[to]] = [next[to], next[idx]];
      return next.map((x, i) => ({ ...x, order: i + 1 }));
    });
  }

  function addItem(groupId: string) {
    const name = window.prompt("Tên hạng mục mới (bắt buộc, BR-210):")?.trim();
    if (!name) return;
    // Hạng mục mới mặc định chưa có chú thích căn cứ pháp lý (field M4)
    mutateGroups((gs) =>
      gs.map((g) => (g.id === groupId ? { ...g, items: [...g.items, { id: newId("CLI"), name }] } : g))
    );
  }

  function renameItem(groupId: string, item: ChecklistItem) {
    const name = window.prompt("Sửa tên hạng mục:", item.name)?.trim();
    if (!name) return;
    mutateGroups((gs) =>
      gs.map((g) =>
        g.id === groupId
          ? { ...g, items: g.items.map((i) => (i.id === item.id ? { ...i, name } : i)) }
          : g
      )
    );
  }

  function requestDeleteItem(groupId: string, item: ChecklistItem) {
    if (referencedNames.has(item.name)) setConfirmDelete({ kind: "item", groupId, item });
    else doDeleteItem(groupId, item);
  }

  function doDeleteItem(groupId: string, item: ChecklistItem) {
    mutateGroups((gs) =>
      gs.map((g) => (g.id === groupId ? { ...g, items: g.items.filter((i) => i.id !== item.id) } : g))
    );
    setConfirmDelete(null);
  }

  function moveItem(groupId: string, item: ChecklistItem, dir: -1 | 1) {
    mutateGroups((gs) =>
      gs.map((g) => {
        if (g.id !== groupId) return g;
        const idx = g.items.findIndex((i) => i.id === item.id);
        const to = idx + dir;
        if (to < 0 || to >= g.items.length) return g;
        const items = [...g.items];
        [items[idx], items[to]] = [items[to], items[idx]];
        return { ...g, items };
      })
    );
  }

  function setLegalNote(groupId: string, itemId: string, legalNote: string) {
    mutateGroups((gs) =>
      gs.map((g) =>
        g.id === groupId
          ? { ...g, items: g.items.map((i) => (i.id === itemId ? { ...i, legalNote } : i)) }
          : g
      )
    );
  }

  // Field M7 — Lưu thay đổi: ghi toàn bộ bản nháp xuống mock API (audit log qua generic handler, BR-170)
  async function saveAll() {
    setSaving(true);
    try {
      for (const id of deletedGroupIds) await deleteGroup.mutateAsync(id);
      for (const g of groups) {
        if (g.id.includes("-NEW-")) {
          // Nhóm mới mang id tạm "-NEW-" phía client; bỏ id đi để server cấp id thật.
          const body: Record<string, unknown> = { ...g };
          delete body.id;
          await createGroup.mutateAsync(body);
        } else {
          await updateGroup.mutateAsync(g);
        }
      }
      setDeletedGroupIds([]);
      setDirty(false);
      setGroups([]); // nạp lại từ API (id mới cho nhóm vừa tạo)
      toast.success("Đã lưu thay đổi checklist — áp dụng ngay cho phiên kiểm tra tiếp theo (BR-210, audit log BR-170).");
    } finally {
      setSaving(false);
    }
  }

  return (
    <>
      <PageHeader
        title="Quản lý Checklist kiểm tra bếp (UC-186)"
        description="Medical Staff tự thêm/sửa/xóa/sắp xếp nhóm, hạng mục và chú thích căn cứ pháp lý — không cần System Admin, không cần phê duyệt (BR-210) · Thay đổi ghi audit log (BR-170)"
        actions={
          <div className="flex gap-2">
            <Link href="/medical-staff/conduct-inspection">
              <Button variant="outline">
                <ChevronLeft /> Về màn hình kiểm tra
              </Button>
            </Link>
            <Button disabled={!dirty || saving} onClick={saveAll}>
              <Save /> Lưu thay đổi
            </Button>
          </div>
        }
      />

      {dirty && (
        <Alert variant="warning" className="mb-4" title="Có thay đổi chưa lưu">
          Thay đổi chỉ áp dụng cho phiên kiểm tra mới sau khi bấm &quot;Lưu thay đổi&quot;.
          Dữ liệu các phiên kiểm tra đã lưu không bị ảnh hưởng (tham chiếu theo tên hạng mục).
        </Alert>
      )}

      {isLoading && groups.length === 0 ? (
        <div className="h-64 animate-pulse rounded-lg bg-muted/40" />
      ) : groups.length === 0 ? (
        // Empty state theo spec: gợi ý thêm nhóm đầu tiên (seed 6 nhóm nạp sẵn khi khởi tạo trường)
        <Card>
          <CardContent className="py-10 text-center text-sm text-muted-foreground">
            Chưa có nhóm checklist nào.{" "}
            <Button variant="outline" size="sm" onClick={addGroup}>
              <Plus /> Thêm nhóm đầu tiên
            </Button>
          </CardContent>
        </Card>
      ) : (
        <div className="grid gap-4 lg:grid-cols-3">
          {/* Cột trái — danh sách nhóm (field M1/M2/M3) */}
          <Card>
            <CardHeader className="flex-row items-center justify-between">
              <CardTitle className="flex items-center gap-2">
                <ListChecks className="size-4" /> Nhóm checklist
              </CardTitle>
              <Button size="sm" variant="outline" onClick={addGroup}>
                <Plus /> Thêm nhóm
              </Button>
            </CardHeader>
            <CardContent className="space-y-1.5">
              {groups.map((g, gi) => (
                <div
                  key={g.id}
                  className={cn(
                    "flex items-center gap-1 rounded-lg border p-2 text-sm",
                    g.id === selectedGroupId && "border-[var(--primary)] ring-1 ring-[var(--primary)]"
                  )}
                >
                  <button
                    type="button"
                    className="min-w-0 flex-1 truncate text-left"
                    onClick={() => setSelectedGroupId(g.id)}
                    title={g.name}
                  >
                    {g.name}{" "}
                    <span className="text-xs text-muted-foreground">({g.items.length})</span>
                  </button>
                  <Button size="icon" className="size-7" variant="ghost" aria-label="Lên" disabled={gi === 0} onClick={() => moveGroup(g, -1)}>
                    <ArrowUp />
                  </Button>
                  <Button size="icon" className="size-7" variant="ghost" aria-label="Xuống" disabled={gi === groups.length - 1} onClick={() => moveGroup(g, 1)}>
                    <ArrowDown />
                  </Button>
                  <Button size="icon" className="size-7" variant="ghost" aria-label="Sửa tên nhóm" onClick={() => renameGroup(g)}>
                    <Pencil />
                  </Button>
                  <Button size="icon" className="size-7" variant="ghost" aria-label="Xóa nhóm" onClick={() => requestDeleteGroup(g)}>
                    <Trash2 className="text-destructive" />
                  </Button>
                </div>
              ))}
            </CardContent>
          </Card>

          {/* Cột phải — hạng mục của nhóm đang chọn (field M4/M5/M6) */}
          <div className="lg:col-span-2">
            {selectedGroup ? (
              <Card>
                <CardHeader className="flex-row items-center justify-between">
                  <CardTitle className="truncate">{selectedGroup.name}</CardTitle>
                  <Button size="sm" variant="outline" onClick={() => addItem(selectedGroup.id)}>
                    <Plus /> Thêm hạng mục
                  </Button>
                </CardHeader>
                <CardContent className="space-y-3">
                  {selectedGroup.items.length === 0 && (
                    <p className="text-sm text-muted-foreground">Nhóm chưa có hạng mục nào.</p>
                  )}
                  {selectedGroup.items.map((item, ii) => (
                    <div key={item.id} className="space-y-2 rounded-lg border p-3 text-sm">
                      <div className="flex items-start gap-1.5">
                        <span className="min-w-0 flex-1 font-medium">
                          {item.name}{" "}
                          {referencedNames.has(item.name) && (
                            <Badge variant="info" title="Đã dùng trong phiên kiểm tra đã lưu — xóa sẽ hiện cảnh báo MSG-094">
                              Đang tham chiếu
                            </Badge>
                          )}
                        </span>
                        <Button size="icon" className="size-7" variant="ghost" aria-label="Lên" disabled={ii === 0} onClick={() => moveItem(selectedGroup.id, item, -1)}>
                          <ArrowUp />
                        </Button>
                        <Button size="icon" className="size-7" variant="ghost" aria-label="Xuống" disabled={ii === selectedGroup.items.length - 1} onClick={() => moveItem(selectedGroup.id, item, 1)}>
                          <ArrowDown />
                        </Button>
                        <Button size="icon" className="size-7" variant="ghost" aria-label="Sửa tên" onClick={() => renameItem(selectedGroup.id, item)}>
                          <Pencil />
                        </Button>
                        <Button size="icon" className="size-7" variant="ghost" aria-label="Xóa hạng mục" onClick={() => requestDeleteItem(selectedGroup.id, item)}>
                          <Trash2 className="text-destructive" />
                        </Button>
                      </div>
                      <div className="space-y-1">
                        <Label htmlFor={`note-${item.id}`} className="text-xs text-muted-foreground">
                          Chú thích căn cứ pháp lý (icon &quot;ℹ&quot; — không bắt buộc, BR-207/BR-210)
                        </Label>
                        <Textarea
                          id={`note-${item.id}`}
                          value={item.legalNote ?? ""}
                          onChange={(e) => setLegalNote(selectedGroup.id, item.id, e.target.value)}
                          placeholder="Trích dẫn điều khoản pháp lý; mục chưa có căn cứ ghi rõ [cần xác nhận thêm] — không suy diễn nguồn."
                          rows={2}
                        />
                      </div>
                    </div>
                  ))}
                </CardContent>
              </Card>
            ) : (
              <Card>
                <CardContent className="py-10 text-center text-sm text-muted-foreground">
                  Chọn một nhóm ở cột trái để xem/sửa hạng mục.
                </CardContent>
              </Card>
            )}
          </div>
        </div>
      )}

      {/* Xác nhận xóa — MSG-094 (cảnh báo, không chặn cứng) */}
      <Dialog open={confirmDelete !== null} onOpenChange={(o) => !o && setConfirmDelete(null)}>
        <DialogContent>
          <DialogHeader>
            <DialogTitle>Xác nhận xóa {confirmDelete?.kind === "group" ? "nhóm" : "hạng mục"} (MSG-094)</DialogTitle>
            <DialogDescription>
              {confirmDelete?.kind === "group"
                ? `Nhóm "${confirmDelete.group.name}" đang có ${confirmDelete.group.items.length} hạng mục.`
                : confirmDelete
                  ? `"${confirmDelete.item.name}" đã được dùng trong các phiên kiểm tra đã lưu.`
                  : ""}{" "}
              Xóa sẽ không xóa dữ liệu lịch sử — chỉ ẩn khỏi các phiên kiểm tra mới (BR-210). Tiếp tục?
            </DialogDescription>
          </DialogHeader>
          <DialogFooter>
            <Button variant="outline" onClick={() => setConfirmDelete(null)}>Hủy</Button>
            <Button
              variant="destructive"
              onClick={() => {
                if (!confirmDelete) return;
                if (confirmDelete.kind === "group") doDeleteGroup(confirmDelete.group);
                else doDeleteItem(confirmDelete.groupId, confirmDelete.item);
              }}
            >
              Xóa (giữ dữ liệu lịch sử)
            </Button>
          </DialogFooter>
        </DialogContent>
      </Dialog>
    </>
  );
}
