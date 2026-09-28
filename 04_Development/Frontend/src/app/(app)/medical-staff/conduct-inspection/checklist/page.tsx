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
import { Input, Label, Textarea } from "@/components/ui/input";
import { useCreate, useDelete, usePagedList, usePlainList, useUpdate } from "@/lib/api/hooks";
import type { ChecklistGroupDto, ChecklistItemDto } from "@/lib/api/types";
import type { KitchenInspectionDto } from "@/lib/api/types";
import { cn } from "@/lib/utils";

let localId = 0;
const newId = (prefix: string) => `${prefix}-NEW-${++localId}`;

/**
 * `ChecklistGroupDto`/`ChecklistItemDto` (= `Api<"ChecklistGroupResponse">`/
 * `Api<"ChecklistItemResponse">`) khai MỌI field optional (springdoc không khai
 * `required`). Trang này giữ một BẢN NHÁP CỤC BỘ luôn đầy đủ field (chỉnh sửa/sắp
 * xếp trước khi bấm "Lưu thay đổi") — narrow về "Draft" NGAY TẠI ranh giới nạp dữ
 * liệu (`normalizeGroup`/`normalizeItem`) thay vì rải `?? ""` khắp trang. Draft
 * dẫn xuất bằng `Pick<>` từ chính DTO thật, không phải type khai tay độc lập.
 */
type ChecklistItemDraft = Pick<ChecklistItemDto, "id" | "name" | "legalNote"> & {
  id: string;
  name: string;
};
type ChecklistGroupDraft = Pick<ChecklistGroupDto, "id" | "name" | "order"> & {
  id: string;
  name: string;
  order: number;
  items: ChecklistItemDraft[];
};

function normalizeItem(i: ChecklistItemDto): ChecklistItemDraft {
  return { id: i.id ?? newId("CLI"), name: i.name ?? "", legalNote: i.legalNote };
}

function normalizeGroup(g: ChecklistGroupDto): ChecklistGroupDraft {
  return {
    id: g.id ?? newId("CLG"),
    name: g.name ?? "",
    order: g.order ?? 0,
    items: (g.items ?? []).map(normalizeItem),
  };
}

export default function ManageChecklistPage() {
  // `checklist-groups` trả THẲNG mảng (danh mục nhỏ theo trường, không phân trang) —
  // usePlainList, KHÔNG usePagedList (data.items sẽ luôn undefined).
  const { data: groupsData, isLoading } = usePlainList<ChecklistGroupDto>("checklist-groups");
  const { data: kitchenData } = usePagedList<KitchenInspectionDto>("kitchen-inspections", { pageSize: 50 });
  const createGroup = useCreate<Record<string, unknown>, ChecklistGroupDto>("checklist-groups", { handledError: true });
  const updateGroup = useUpdate<{ id: string } & Record<string, unknown>, ChecklistGroupDto>("checklist-groups", { handledError: true });
  const deleteGroup = useDelete("checklist-groups", { handledError: true });

  // Bản nháp cục bộ — chỉ ghi xuống API khi bấm "Lưu thay đổi" (field M7)
  const [groups, setGroups] = useState<ChecklistGroupDraft[]>([]);
  const [deletedGroupIds, setDeletedGroupIds] = useState<string[]>([]);
  const [selectedGroupId, setSelectedGroupId] = useState<string | null>(null);
  const [dirty, setDirty] = useState(false);
  // Dialog xác nhận xóa (MSG-094 khi mục/nhóm đang được tham chiếu)
  const [confirmDelete, setConfirmDelete] = useState<
    { kind: "group"; group: ChecklistGroupDraft } | { kind: "item"; groupId: string; item: ChecklistItemDraft } | null
  >(null);
  const [saving, setSaving] = useState(false);
  // Dialog nhập tên khi thêm/sửa nhóm hoặc hạng mục (thay window.prompt)
  const [namePrompt, setNamePrompt] = useState<
    | { kind: "add-group"; title: string }
    | { kind: "rename-group"; title: string; group: ChecklistGroupDraft }
    | { kind: "add-item"; title: string; groupId: string }
    | { kind: "rename-item"; title: string; groupId: string; item: ChecklistItemDraft }
    | null
  >(null);
  const [namePromptValue, setNamePromptValue] = useState("");

  useEffect(() => {
    if (groupsData && !dirty && groups.length === 0) {
      const sorted = groupsData.map(normalizeGroup).sort((a, b) => a.order - b.order);
      setGroups(sorted);
      if (sorted.length > 0) setSelectedGroupId(sorted[0].id);
    }
  }, [groupsData, dirty, groups.length]);

  const selectedGroup = groups.find((g) => g.id === selectedGroupId) ?? null;

  // MSG-094: hạng mục "đang được tham chiếu" = tên xuất hiện trong failedItems/itemFindings
  // của phiên kiểm tra đã lưu (phiên lưu theo TÊN nên xóa không mất dữ liệu lịch sử)
  const referencedNames = new Set<string>();
  for (const k of kitchenData?.items ?? []) {
    for (const n of k.failedItems ?? []) referencedNames.add(n);
    for (const f of k.itemFindings ?? []) if (f.item) referencedNames.add(f.item);
  }

  function mutateGroups(fn: (gs: ChecklistGroupDraft[]) => ChecklistGroupDraft[]) {
    setGroups((gs) => fn(gs));
    setDirty(true);
  }

  function addGroup() {
    setNamePromptValue("");
    setNamePrompt({ kind: "add-group", title: "Thêm nhóm checklist mới (BR-210)" });
  }

  function renameGroup(g: ChecklistGroupDraft) {
    setNamePromptValue(g.name);
    setNamePrompt({ kind: "rename-group", title: "Sửa tên nhóm", group: g });
  }

  function requestDeleteGroup(g: ChecklistGroupDraft) {
    // Nhóm có hạng mục → luôn xác nhận trước (field M3)
    if (g.items.length > 0) setConfirmDelete({ kind: "group", group: g });
    else doDeleteGroup(g);
  }

  function doDeleteGroup(g: ChecklistGroupDraft) {
    if (!g.id.includes("-NEW-")) setDeletedGroupIds((ids) => [...ids, g.id]);
    mutateGroups((gs) => gs.filter((x) => x.id !== g.id).map((x, i) => ({ ...x, order: i + 1 })));
    if (selectedGroupId === g.id) setSelectedGroupId(null);
    setConfirmDelete(null);
  }

  function moveGroup(g: ChecklistGroupDraft, dir: -1 | 1) {
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
    setNamePromptValue("");
    setNamePrompt({ kind: "add-item", title: "Thêm hạng mục mới (BR-210)", groupId });
  }

  function renameItem(groupId: string, item: ChecklistItemDraft) {
    setNamePromptValue(item.name);
    setNamePrompt({ kind: "rename-item", title: "Sửa tên hạng mục", groupId, item });
  }

  function submitNamePrompt() {
    const name = namePromptValue.trim();
    if (!name || !namePrompt) return;
    switch (namePrompt.kind) {
      case "add-group": {
        const g: ChecklistGroupDraft = { id: newId("CLG"), name, order: groups.length + 1, items: [] };
        mutateGroups((gs) => [...gs, g]);
        setSelectedGroupId(g.id);
        break;
      }
      case "rename-group": {
        const groupId = namePrompt.group.id;
        mutateGroups((gs) => gs.map((x) => (x.id === groupId ? { ...x, name } : x)));
        break;
      }
      case "add-item": {
        // Hạng mục mới mặc định chưa có chú thích căn cứ pháp lý (field M4)
        const groupId = namePrompt.groupId;
        mutateGroups((gs) =>
          gs.map((g) => (g.id === groupId ? { ...g, items: [...g.items, { id: newId("CLI"), name }] } : g))
        );
        break;
      }
      case "rename-item": {
        const { groupId, item } = namePrompt;
        mutateGroups((gs) =>
          gs.map((g) =>
            g.id === groupId
              ? { ...g, items: g.items.map((i) => (i.id === item.id ? { ...i, name } : i)) }
              : g
          )
        );
        break;
      }
    }
    setNamePrompt(null);
  }

  function requestDeleteItem(groupId: string, item: ChecklistItemDraft) {
    if (referencedNames.has(item.name)) setConfirmDelete({ kind: "item", groupId, item });
    else doDeleteItem(groupId, item);
  }

  function doDeleteItem(groupId: string, item: ChecklistItemDraft) {
    mutateGroups((gs) =>
      gs.map((g) => (g.id === groupId ? { ...g, items: g.items.filter((i) => i.id !== item.id) } : g))
    );
    setConfirmDelete(null);
  }

  function moveItem(groupId: string, item: ChecklistItemDraft, dir: -1 | 1) {
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
      // `?confirmed=true` — người dùng đã xác nhận qua dialog MSG-094 (hoặc nhóm rỗng,
      // không cần hỏi) TRƯỚC khi vào `deletedGroupIds`; CF-15/BR-210 chỉ chặn cứng khi
      // xoá KHÔNG xác nhận, không chặn xoá-mềm sau khi người dùng đã đồng ý.
      for (const id of deletedGroupIds) await deleteGroup.mutateAsync({ id, params: { confirmed: true } });
      for (const g of groups) {
        // Hạng mục MỚI cũng mang id tạm "CLI-NEW-n" — bỏ đi để server tự cấp mã thật
        // (D5). Backend DÙNG THẲNG `id` nếu khác rỗng (`ItemRequest.id`), nên gửi id tạm
        // lên sẽ biến "CLI-NEW-3" thành mã VĨNH VIỄN của hạng mục — không phải lỗi hiển
        // thị, mà là dữ liệu rác lưu thật trong DB.
        const items = g.items.map(({ id, name, legalNote }) =>
          id.includes("-NEW-") ? { name, legalNote } : { id, name, legalNote }
        );
        if (g.id.includes("-NEW-")) {
          // Nhóm mới mang id tạm "-NEW-" phía client; bỏ id đi để server cấp id thật.
          await createGroup.mutateAsync({ name: g.name, order: g.order, items });
        } else {
          await updateGroup.mutateAsync({ id: g.id, name: g.name, order: g.order, items });
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

      {/* Thêm/sửa tên nhóm hoặc hạng mục (thay window.prompt) */}
      <Dialog open={namePrompt !== null} onOpenChange={(o) => !o && setNamePrompt(null)}>
        <DialogContent>
          <form
            onSubmit={(e) => {
              e.preventDefault();
              submitNamePrompt();
            }}
          >
            <DialogHeader>
              <DialogTitle>{namePrompt?.title}</DialogTitle>
            </DialogHeader>
            <div className="space-y-1.5 py-2">
              <Label htmlFor="name-prompt-input">Tên (bắt buộc, BR-210)</Label>
              <Input
                id="name-prompt-input"
                autoFocus
                value={namePromptValue}
                onChange={(e) => setNamePromptValue(e.target.value)}
              />
            </div>
            <DialogFooter>
              <Button type="button" variant="outline" onClick={() => setNamePrompt(null)}>
                Hủy
              </Button>
              <Button type="submit" disabled={!namePromptValue.trim()}>
                Lưu
              </Button>
            </DialogFooter>
          </form>
        </DialogContent>
      </Dialog>

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
