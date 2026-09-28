import json, os, urllib.request, urllib.error

B = os.environ.get("SFMS_API", "http://localhost:8080/api/v1")
ACCOUNTS = {
    "system-admin":     "dangnh.sysadmin.synthetic@example.test",
    "school-admin":     "tranth.admin.synthetic@example.test",
    "medical-staff":    "ngoth.medical.synthetic@example.test",
    "teacher":          "quynhdt.teacher.synthetic@example.test",
    "service-caterers": "duchm.caterer.synthetic@example.test",
    "supplier":         "sonlt.supplier.synthetic@example.test",
    "parent":           "binhnv.parent.synthetic@example.test",
}
# Mật khẩu tài khoản seed SYNTHETIC dùng cho môi trường dev. Không hardcode trong file commit —
# đặt `SFMS_DEV_PW` trước khi chạy:  SFMS_DEV_PW='...' python probe2.py
PW = os.environ.get("SFMS_DEV_PW")
if not PW:
    raise SystemExit("Thiếu biến môi trường SFMS_DEV_PW (mật khẩu tài khoản seed dev).")
SKIP_ROOT = {"auth", "incident-events", "menu-variants"}


def login(email):
    req = urllib.request.Request(
        B + "/auth/login", data=json.dumps({"email": email, "password": PW}).encode(),
        method="POST", headers={"Content-Type": "application/json"})
    with urllib.request.urlopen(req) as r:
        return json.load(r)["accessToken"]


def status(path, tok):
    req = urllib.request.Request(B + "/" + path, headers={"Authorization": "Bearer " + tok})
    try:
        with urllib.request.urlopen(req) as r:
            return r.status
    except urllib.error.HTTPError as e:
        return e.code
    except Exception:
        return -1


by_role = json.load(open("by_role2.json", encoding="utf-8"))
rows = []
for role, email in ACCOUNTS.items():
    tok = login(email)
    for res in by_role.get(role, []):
        if res.split("/")[0] in SKIP_ROOT:
            continue
        c = status(res, tok)
        rows.append((role, res, c))

bad = [r for r in rows if r[2] in (401, 403) or r[2] >= 500 or r[2] == -1]
print("Da do %d cap (role,endpoint). Van de: %d\n" % (len(rows), len(bad)))
print("%-18s %-34s %s" % ("ROLE", "RESOURCE", "HTTP"))
for role, res, c in bad:
    print("%-18s %-34s %s" % (role, res, c))
