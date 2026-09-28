# -*- coding: utf-8 -*-
"""
LUU Y (07/08/2026): danh sach thu HAI ("man co mutation nhung khong xu ly loi")
KHONG con la danh sach gap ke tu khi co `MutationCache.onError` toan cuc trong
`components/app/providers.tsx` — nhung man do gio da duoc handler chung toast ho.
Danh sach can theo doi bay gio la chieu NGUOC LAI: man TU toast ma QUEN co
`{ handledError: true }` se ra 2 toast. Dung `scan_mut_handled.py` de cham viec do.
"""
import os, re

SRC = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "04_Development", "Frontend", "src", "app", "(app)")

read_pat = re.compile(r'use(PagedList|PlainList|Single|Detail|SubDetail)\s*<')
mut_pat = re.compile(r'\.mutate\(|\.mutateAsync\(')
onerr_pat = re.compile(r'onError')
catch_pat = re.compile(r'\bcatch\s*\(|apiErrorMessage')

no_loading, mut_no_err = [], []

for root, _d, fns in os.walk(SRC):
    for fn in fns:
        if fn != "page.tsx":
            continue
        p = os.path.join(root, fn)
        s = open(p, encoding="utf-8").read()
        rel = os.path.relpath(p, SRC).replace("\\", "/")

        if read_pat.search(s) and "isLoading" not in s and "isPending" not in s:
            no_loading.append(rel)

        n_mut = len(mut_pat.findall(s))
        if n_mut and not onerr_pat.search(s) and not catch_pat.search(s):
            mut_no_err.append((rel, n_mut))

print("=== Man DOC API nhung KHONG co trang thai loading (%d):" % len(no_loading))
for r in no_loading:
    print("   ", r)
print()
print("=== Man CO mutation nhung KHONG xu ly loi (onError/catch/apiErrorMessage) (%d):" % len(mut_no_err))
for r, n in mut_no_err:
    print("   %-62s %d mutation" % (r, n))
