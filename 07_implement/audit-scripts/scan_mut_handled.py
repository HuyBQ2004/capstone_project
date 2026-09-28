# -*- coding: utf-8 -*-
"""
Cham TUNG loi goi factory-mutation (khong phai tung FILE) xem noi goi co tu xu ly
loi khong. Dung cho huong (a): global MutationCache.onError + co
`meta:{handledError:true}` dat ngay tai loi goi hook.

Vi sao khong cham theo file: mot man co the co 3 mutation, 2 cai tu toast con 1
cai nuot loi. Gan co o ca file se lam cai thu 3 tiep tuc im lang.

Cach cham mot bien mutation `NAME`:
  · `NAME.mutate(args, { onError })`  -> handled
  · `await NAME.mutateAsync(...)` nam trong `try {`  -> handled
  · con lai -> unhandled
Bien khong co loi goi `.mutate` nao (truyen thang xuong component con) -> "unknown",
phai xem tay.

Chay:  python 07_implement/audit-scripts/scan_mut_handled.py
"""
import os, re, json

APP = os.path.join(
    os.path.dirname(os.path.abspath(__file__)),
    "..", "..", "04_Development", "Frontend", "src", "app", "(app)",
)

FACT = r"(useCreateAt|usePatchAction|useUploadMutation|useCreate|useUpdate|usePatch|useDelete|useAction)"
decl_pat = re.compile(r"const\s+(\w+)\s*=\s*" + FACT + r"\s*(?:<[^;]*?>)?\s*\(")


def balanced(s, open_idx):
    """Tra ve noi dung trong cap ngoac bat dau tai s[open_idx] == '('."""
    depth, i = 0, open_idx
    while i < len(s):
        c = s[i]
        if c == "(":
            depth += 1
        elif c == ")":
            depth -= 1
            if depth == 0:
                return s[open_idx + 1 : i], i
        i += 1
    return s[open_idx + 1 :], len(s)


def in_try(s, pos):
    """Xap xi: vi tri `pos` co nam trong mot khoi `try {` chua dong khong."""
    head = s[:pos]
    t = head.rfind("try {")
    if t < 0:
        return False
    c = head.rfind("} catch")
    return t > c


rows = []
for root, _d, fns in os.walk(APP):
    for fn in fns:
        if not fn.endswith(".tsx"):
            continue
        p = os.path.join(root, fn)
        s = open(p, encoding="utf-8").read()
        rel = os.path.relpath(p, APP).replace("\\", "/")
        for m in decl_pat.finditer(s):
            name, factory = m.group(1), m.group(2)
            line = s[: m.start()].count("\n") + 1
            sites = []
            for c in re.finditer(r"\b" + re.escape(name) + r"\.(mutateAsync|mutate)\s*\(", s):
                args, _end = balanced(s, c.end() - 1)
                if c.group(1) == "mutateAsync":
                    ok = in_try(s, c.start()) or "onError" in args
                else:
                    ok = "onError" in args
                sites.append(ok)
            verdict = "unknown" if not sites else ("handled" if all(sites) else "unhandled")
            rows.append({
                "file": rel, "line": line, "var": name, "hook": factory,
                "sites": len(sites), "verdict": verdict,
                "has_meta": "handledError" in s,
            })

for v in ("handled", "unhandled", "unknown"):
    sel = [r for r in rows if r["verdict"] == v]
    print("=== %s (%d loi goi):" % (v.upper(), len(sel)))
    for r in sel:
        print("   %-56s:%-5d %-16s %-18s %d site" % (r["file"], r["line"], r["var"], r["hook"], r["sites"]))
    print()
print("Tong loi goi factory-mutation:", len(rows))

with open(os.path.join(os.path.dirname(os.path.abspath(__file__)), "mut_handled.json"), "w", encoding="utf-8") as f:
    json.dump(rows, f, ensure_ascii=False, indent=1)
