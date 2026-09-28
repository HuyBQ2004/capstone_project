# -*- coding: utf-8 -*-
"""
Codemod: gan `{ handledError: true }` vao dung nhung loi goi factory-mutation ma
`scan_mut_handled.py` cham la "handled" (man do TU toast loi roi).

Chay scan truoc de co mut_handled.json:
    python 07_implement/audit-scripts/scan_mut_handled.py
    python 07_implement/audit-scripts/apply_handled_meta.py --dry
    python 07_implement/audit-scripts/apply_handled_meta.py

Luat chen:
  useCreate("x")                  -> useCreate("x", { handledError: true })
  useCreateAt("x", "y")           -> useCreateAt("x", "y", { handledError: true })
  useUploadMutation("x")          -> useUploadMutation("x", "post", { handledError: true })
                                     (phai bu tham so `method` mac dinh vi opts dung sau no)
Chen tu CUOI file len DAU de offset khong xe dich.
"""
import os, sys, json, re

HERE = os.path.dirname(os.path.abspath(__file__))
APP = os.path.join(HERE, "..", "..", "04_Development", "Frontend", "src", "app", "(app)")
FLAG = "{ handledError: true }"
DRY = "--dry" in sys.argv

rows = json.load(open(os.path.join(HERE, "mut_handled.json"), encoding="utf-8"))
targets = {}
for r in rows:
    if r["verdict"] != "handled":
        continue
    targets.setdefault(r["file"], set()).add((r["line"], r["hook"], r["var"]))

decl_pat = re.compile(
    r"const\s+(\w+)\s*=\s*(useCreateAt|usePatchAction|useUploadMutation|useCreate|useUpdate|usePatch|useDelete|useAction)\s*(?:<[^;]*?>)?\s*\("
)


def close_of(s, open_idx):
    depth, i = 0, open_idx
    while i < len(s):
        if s[i] == "(":
            depth += 1
        elif s[i] == ")":
            depth -= 1
            if depth == 0:
                return i
        i += 1
    raise ValueError("khong tim thay ngoac dong")


changed = skipped = 0
for rel, wanted in sorted(targets.items()):
    p = os.path.join(APP, *rel.split("/"))
    s = open(p, encoding="utf-8").read()
    edits = []
    for m in decl_pat.finditer(s):
        line = s[: m.start()].count("\n") + 1
        key = (line, m.group(2), m.group(1))
        if key not in wanted:
            continue
        wanted.discard(key)
        open_idx = m.end() - 1
        end = close_of(s, open_idx)
        args = s[open_idx + 1 : end]
        if FLAG in args:
            skipped += 1
            continue
        ins = FLAG
        if m.group(2) == "useUploadMutation" and '"put"' not in args and "'put'" not in args and '"post"' not in args:
            ins = '"post", ' + FLAG
        edits.append((end, ", " + ins))
    for pos, text in sorted(edits, reverse=True):
        s = s[:pos] + text + s[pos:]
        changed += 1
    if edits and not DRY:
        open(p, "w", encoding="utf-8", newline="").write(s)
    if edits:
        print("%-58s %d loi goi" % (rel, len(edits)))
    if wanted:
        print("   !! KHONG khop, phai sua tay:", sorted(wanted))

print()
print("Da chen:", changed, "| bo qua (da co co):", skipped, "| DRY-RUN" if DRY else "| DA GHI FILE")
