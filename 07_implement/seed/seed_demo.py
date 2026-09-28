"""Seed dữ liệu demo SFMS — CHỈ qua HTTP API thật, không một câu SQL nào.

    python 07_implement/seed/seed_demo.py            # chạy hết
    python 07_implement/seed/seed_demo.py menu serve # chạy vài pha (xem PHASES cuối file)

Yêu cầu trước khi chạy — xem README.md cùng thư mục:
  1. Stack compose đang chạy và backend là bản BUILD MỚI (`docker compose build backend`).
  2. Backend trỏ SMTP về Mailpit (file `docker-compose.mailpit.yml` ở gốc repo) — script
     lấy token kích hoạt tài khoản từ hộp thư Mailpit.
  3. `MENU_LEAD_TIME_DAYS=0` (compose đã đặt sẵn) để tạo được thực đơn trong ngày.
  4. File `.env.seed` khai 4 tài khoản gốc (theo `.env.seed.example`).

Thứ tự pha bám đúng thứ tự dựng tay ở `09_Dev_Runbook.md` mục 11.
"""
from __future__ import annotations

import base64 as _b64
import datetime as _dt
import sys

from seed_lib import ApiError, call, call_multipart, login, step, account, activation_token

_TODAY_D = _dt.date.today()
TODAY = _TODAY_D.isoformat()
# Ngày tương đối để script không hết hạn khi chạy lại vào hôm khác.
MFG_DATE = (_TODAY_D - _dt.timedelta(days=2)).isoformat()      # ngày sản xuất lô
EXP_DATE = (_TODAY_D + _dt.timedelta(days=30)).isoformat()     # hạn dùng lô
PRICE_FROM = (_TODAY_D - _dt.timedelta(days=7)).isoformat()    # giá có hiệu lực từ

# Mật khẩu cho các tài khoản script tự tạo (chỉ dùng cho môi trường demo).
PWD = {
    "medical": "MedE2e@2026x",
    "teacher": "TeachE2e@2026x",
    "parent": "ParentE2e@2026x",
}

STAFF = [
    ("Ngô Thị Y Tế", "medical.e2e@example.test", "0912345678", "medical-staff", "medical"),
    ("Đỗ Thị Quỳnh", "teacher.e2e@example.test", "0912345679", "teacher", "teacher"),
]

INGREDIENTS = [
    ("ING-THIT-HEO", "Thịt heo nạc", "kg", "CHILLED"),
    ("ING-CA-BASA", "Cá basa phi lê", "kg", "FROZEN"),
    ("ING-GAO-TE", "Gạo tẻ", "kg", "DRY"),
    ("ING-RAU-CAI", "Rau cải ngọt", "kg", "CHILLED"),
    ("ING-TRUNG-GA", "Trứng gà", "quả", "CHILLED"),
    ("ING-CA-CHUA", "Cà chua", "kg", "CHILLED"),
]

DISHES = [
    ("DISH-THIT-KHO", "Thịt kho trứng", "MON_MAN", 320, 18, 14, 20,
     [("ING-THIT-HEO", "Thịt heo nạc", 0.08, "kg"), ("ING-TRUNG-GA", "Trứng gà", 1, "quả")]),
    ("DISH-CA-SOT", "Cá basa sốt cà chua", "MON_MAN", 280, 20, 10, 18,
     [("ING-CA-BASA", "Cá basa phi lê", 0.07, "kg"), ("ING-CA-CHUA", "Cà chua", 0.05, "kg")]),
    ("DISH-CANH-CAI", "Canh rau cải", "CANH", 60, 3, 2, 6,
     [("ING-RAU-CAI", "Rau cải ngọt", 0.05, "kg")]),
    ("DISH-COM-TRANG", "Cơm trắng", "MON_CHINH", 200, 4, 1, 45,
     [("ING-GAO-TE", "Gạo tẻ", 0.1, "kg")]),
]

STUDENTS = [
    ("STU-E2E-01", "Nguyễn Văn An", "2019-03-12", "Nguyễn Văn Hùng", "parent1.e2e@example.test"),
    ("STU-E2E-02", "Trần Thị Bình", "2019-05-20", "Trần Văn Long", "parent2.e2e@example.test"),
    ("STU-E2E-03", "Lê Văn Cường", "2019-07-02", "Lê Thị Mai", "parent3.e2e@example.test"),
]

PRODUCTS = [
    ("SP-THIT-HEO", "ING-THIT-HEO", "Thịt heo nạc loại 1", "kg", 145000),
    ("SP-CA-BASA", "ING-CA-BASA", "Cá basa phi lê đông lạnh", "kg", 98000),
    ("SP-GAO-TE", "ING-GAO-TE", "Gạo tẻ thơm", "kg", 22000),
    ("SP-RAU-CAI", "ING-RAU-CAI", "Rau cải ngọt", "kg", 18000),
]

# Món thực sự lên thực đơn hôm nay (và do đó có mẫu lưu + là ứng viên nghi ngờ khi có sự cố).
MENU_DISHES = [(DISHES[0][0], DISHES[0][1]), (DISHES[2][0], DISHES[2][1])]

SCHOOL_CODE = "SCH-0001"
CLASSROOM = "1A"
SUPPLIER_CODE = "SUP-202"
CATERER_CODE = "CAT-201"

# Ảnh PNG 1×1 hợp lệ — vài endpoint BẮT BUỘC có ảnh bằng chứng (SMP-107, CAPA submit-evidence).
PNG_1PX = _b64.b64decode(
    "iVBORw0KGgoAAAANSUhEUgAAAAEAAAABCAYAAAAfFcSJAAAADUlEQVR42mP8z8BQDwAEhQGAhKmMIQAAAABJRU5ErkJggg==")

T = {}          # token theo vai
CTX = {}        # dữ liệu sinh ra giữa các pha


# ───────────────────────────── pha 0: đăng nhập + nhân sự ─────────────────────────────


# ─────────────────────── pha -1: dựng nền (DB TRẮNG) ───────────────────────

def phase_bootstrap():
    """
    Dựng TRƯỜNG + 3 tài khoản gốc + năm học + khối + lớp trên DB hoàn toàn trắng.

    Vì sao có pha này (thêm 08/08/2026): trước đó README ghi tiền đề "Nam đã dựng tay qua UI"
    — nghĩa là seed KHÔNG dựng lại được demo từ số 0. Máy khác, DB khác, hoặc lỡ tay xoá là
    phải làm tay lại toàn bộ. Phát hiện đúng lúc thử drop DB rồi chạy seed: đứt ngay pha 0 vì
    DB trắng chỉ còn tài khoản System Admin.

    Đi ĐÚNG luồng thật, không chèn SQL:
      đăng ký (công khai) → System Admin/BGH duyệt → kích hoạt qua email → đăng nhập.
    Nhờ vậy pha này cũng là phép thử end-to-end cho luồng đăng ký + duyệt đối tác — thứ chưa
    có test nào phủ.

    ⚠️ CẦN MAILPIT: ba tài khoản gốc đều kích hoạt bằng token trong email.
        docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend

    ⚠️ RATE LIMIT 5 lần/phút/IP ở cả `/schools/register` lẫn `/partners/register/*`. Chạy lại
    liên tiếp nhiều lần sẽ gặp 429 — chờ một phút, KHÔNG phải lỗi code.
    """
    print("[-1] Dựng nền trên DB trắng: trường + 3 tài khoản gốc + năm học + khối + lớp")
    T["sys"] = login(*account("SEED_SYSADMIN"))

    school_email, school_pwd = account("SEED_SCHOOL_ADMIN")
    caterer_email, caterer_pwd = account("SEED_CATERER")
    supplier_email, supplier_pwd = account("SEED_SUPPLIER")

    # ── Trường: đăng ký công khai → System Admin kích hoạt ──
    st, b = call("POST", "/schools/register", body={
        "id": SCHOOL_CODE, "name": "Trường Trung Học Nam Nguyễn", "taxId": "0109988776",
        "email": school_email, "address": "Thanh Đa, Phúc Thọ, Hà Nội",
        "lat": 21.0885, "lng": 105.5860,
        "operatingHours": "07:00 – 17:00 (T2–T7)",
        "emergencyContact": "Nguyễn Văn Nam — 0912 000 111"})
    step(f"POST /schools/register {SCHOOL_CODE}", st, b, tolerate=(409,))
    st, b = call("PATCH", f"/schools/{SCHOOL_CODE}/status", T["sys"], {"status": "ACTIVE"})
    step(f"PATCH /schools/{SCHOOL_CODE}/status ACTIVE", st, b, tolerate=(409, 422))

    # Đăng ký trường tự tạo UserAccount role school-admin ở PENDING_VERIFICATION.
    _activate_or_login("school", school_email, school_pwd)

    # ── Hai đối tác: đăng ký công khai → BGH duyệt sau (pha supply) ──
    st, b = call("POST", "/partners/register/caterer", body={
        "id": CATERER_CODE, "companyName": "Công ty Nấu ăn An Bình", "licenseNo": "0109210987",
        "address": "Cầu Giấy, Hà Nội", "contactPerson": "Trần An Bình",
        "email": caterer_email, "phone": "0912000222"})
    step(f"POST /partners/register/caterer {CATERER_CODE}", st, b, tolerate=(409,))
    _activate_or_login("caterer", caterer_email, caterer_pwd)

    st, b = call("POST", "/partners/register/supplier", body={
        "id": SUPPLIER_CODE, "companyName": "Công ty Thực phẩm Nguyễn Nam", "licenseNo": "0109210988",
        "address": "Mộc Châu, Sơn La", "contactPerson": "Nguyễn Văn Nam",
        "email": supplier_email, "phone": "0912000333"})
    step(f"POST /partners/register/supplier {SUPPLIER_CODE}", st, b, tolerate=(409,))
    _activate_or_login("supplier", supplier_email, supplier_pwd)

    # ── Hợp tác của ĐƠN VỊ SUẤT ĂN với trường ──
    # BẮT BUỘC, không phải trang trí: tài khoản đối tác không mang `schoolId` trong JWT, backend
    # suy "trường của tôi" từ HỢP TÁC ĐANG HIỆU LỰC (`ActorScope`/`Mf06Scope`). Chưa có hợp tác
    # thì mọi endpoint MF06 trả 403 ING-005 — chạy thật 08/08/2026 đứt đúng ở `POST
    # /purchase-requests`. Bếp phải được trường ký hợp đồng rồi mới đặt hàng được, đúng đời thật.
    st, b = call("GET", "/partners/me", T["caterer"])
    if b.get("status") != "APPROVED":
        st, b = call_multipart("POST", "/partnership-applications", T["caterer"], parts={"meta": {
            "companyName": b.get("name") or "Đơn vị suất ăn",
            "taxId": b.get("taxId") or "0109210987",
            "address": b.get("address") or "Hà Nội", "schoolId": SCHOOL_CODE}})
        step("POST /partnership-applications (Đơn vị suất ăn)", st, b, tolerate=(409,))
        st, b = call("POST", f"/partners/{CATERER_CODE}/approve", T["school"], {})
        step(f"POST /partners/{CATERER_CODE}/approve", st, b, tolerate=(409,))

    # ── Năm học ──
    # Năm học VN chạy 05/09 → 31/05. Chọn năm CHƯA KẾT THÚC tính tới hôm nay:
    #   · đang trong năm học  ⇒ chính năm đó;
    #   · đang nghỉ hè (như 08/08) ⇒ năm sắp khai giảng.
    # Backend chặn kích hoạt năm ĐÃ kết thúc — chạy thật 08/08/2026 đã bị chặn đúng khi script
    # (bản đầu) chọn nhầm năm 2025–2026 đã đóng ngày 31/05/2026. Giữ nguyên luật, sửa script.
    # Năm sắp khai giảng vẫn kích hoạt được (trường phải chuẩn bị lớp trước) và dashboard ghi
    # rõ "Active — chưa bắt đầu", đúng sự thật.
    _base = _TODAY_D.year if _TODAY_D <= _dt.date(_TODAY_D.year, 5, 31) else _TODAY_D.year
    year_start = _dt.date(_base if _TODAY_D >= _dt.date(_base, 6, 1) else _base - 1, 9, 5)
    year_end = _dt.date(year_start.year + 1, 5, 31)
    ay = f"AY-{year_start.year}"
    st, b = call("POST", "/academic-years", T["school"], {
        "id": ay, "name": f"Năm học {year_start.year}–{year_start.year + 1}",
        "startDate": year_start.isoformat(), "endDate": year_end.isoformat(),
        "copyStructure": False})
    step(f"POST /academic-years {ay}", st, b, tolerate=(409,))
    st, b = call("POST", f"/academic-years/{ay}/activate", T["school"], {})
    step(f"POST /academic-years/{ay}/activate", st, b, tolerate=(409, 422))

    # ── Khối + lớp ──
    for name, order in [("Khối 1", 1), ("Khối 2", 2), ("Khối 3", 3)]:
        st, b = call("POST", "/grades", T["school"], {"name": name, "sortOrder": order})
        step(f"POST /grades {name}", st, b, tolerate=(409,))
    st, b = call("GET", "/grades", T["school"])
    grade_id = next((g["id"] for g in b if g.get("name") == "Khối 1"), None)
    if not grade_id:
        step("GET /grades (tìm Khối 1)", st, b, ok=False)
    st, b = call("POST", "/classrooms", T["school"], {
        "id": CLASSROOM, "name": CLASSROOM, "gradeId": grade_id, "capacity": 35,
        "academicYearId": ay})
    step(f"POST /classrooms {CLASSROOM}", st, b, tolerate=(409,))


def _activate_or_login(key, email, pwd):
    """Kích hoạt tài khoản mới qua token trong Mailpit; đã kích hoạt rồi thì đăng nhập luôn."""
    try:
        T[key] = login(email, pwd)
        print(f"  OK   đã có sẵn, đăng nhập {key}: {email}")
        return
    except ApiError:
        pass
    token = activation_token(email)
    if not token:
        # Tài khoản đã tồn tại từ lần chạy trước (POST tạo trả 409) nên KHÔNG có email mới
        # trong Mailpit — yêu cầu gửi lại. Nhờ đó chạy lại script trên DB đã có dữ liệu vẫn
        # kích hoạt được, không phải drop DB.
        st, _ = call("POST", "/auth/activate/resend", body={"email": email})
        if st in (200, 202):
            token = activation_token(email)
    if not token:
        raise SystemExit(
            f"Không thấy email kích hoạt cho {email} trong Mailpit. "
            f"Backend có đang trỏ SMTP về mailpit không? Chạy: "
            f"docker compose -f docker-compose.yml -f docker-compose.mailpit.yml up -d backend")
    st, b = call("POST", "/auth/activate", body={"token": token, "newPassword": pwd})
    step(f"POST /auth/activate {email}", st, b)
    T[key] = login(email, pwd)
    print(f"  OK   kích hoạt + đăng nhập {key}: {email}")


def phase_accounts():
    print("[0] Đăng nhập 4 tài khoản gốc + tạo nhân sự trường")
    for key, env in [("sys", "SEED_SYSADMIN"), ("school", "SEED_SCHOOL_ADMIN"),
                     ("caterer", "SEED_CATERER"), ("supplier", "SEED_SUPPLIER")]:
        email, pwd = account(env)
        T[key] = login(email, pwd)
        print(f"  OK   đăng nhập {key}: {email}")

    for name, email, phone, role, pwd_key in STAFF:
        st, b = call("POST", "/staff", T["school"],
                     {"name": name, "email": email, "phone": phone, "role": role})
        step(f"POST /staff {role} {email}", st, b, tolerate=(409,))
        if st == 409:                       # đã tạo lần chạy trước
            T[pwd_key] = login(email, PWD[pwd_key])
            continue
        token = activation_token(email)
        if not token:
            raise SystemExit(f"Không thấy email kích hoạt cho {email} trong Mailpit — "
                             f"backend có đang trỏ SMTP về mailpit không? (xem README)")
        st, b = call("POST", "/auth/activate", body={"token": token, "newPassword": PWD[pwd_key]})
        step(f"POST /auth/activate {email}", st, b)
        T[pwd_key] = login(email, PWD[pwd_key])
        print(f"  OK   kích hoạt + đăng nhập {email}")


# ───────────────────────────── pha 1: danh mục ─────────────────────────────

def phase_catalog():
    print("[1] Danh mục nguyên liệu (System Admin) + món ăn (Đơn vị suất ăn)")
    for code, name, unit, zone in INGREDIENTS:
        st, b = call("POST", "/ingredients", T["sys"],
                     {"code": code, "name": name, "unit": unit, "storageZone": zone})
        step(f"POST /ingredients {code}", st, b, tolerate=(409,))

    for code, name, cat, kcal, protein, fat, carb, ings in DISHES:
        st, b = call("POST", "/dishes", T["caterer"], {
            "id": code, "name": name, "category": cat, "kcal": kcal, "protein": protein,
            "fat": fat, "carb": carb,
            "ingredients": [{"ingredientId": i, "name": n, "quantity": q, "unit": u}
                            for i, n, q, u in ings]})
        step(f"POST /dishes {code}", st, b, tolerate=(409,))


# ───────────────────────────── pha 2: học sinh ─────────────────────────────

def phase_students():
    print("[2] Học sinh + phụ huynh (School Admin)")
    for code, name, dob, parent_name, parent_mail in STUDENTS:
        st, b = call("POST", "/students", T["school"], {
            "id": code, "name": name, "dob": dob, "classroomId": CLASSROOM,
            "parent": {"fullName": parent_name, "email": parent_mail,
                       "phone": "0987654321", "relationship": "Cha/mẹ"}})
        step(f"POST /students {code}", st, b, tolerate=(409,))

    # Kích hoạt TÀI KHOẢN PHỤ HUYNH của học sinh đầu tiên — cần cho việc rà 14 màn vai Phụ
    # huynh (thêm 08/08/2026). `POST /students` tự tạo user role `parent` ở
    # PENDING_VERIFICATION; không kích hoạt thì KHÔNG có tài khoản Phụ huynh nào đăng nhập
    # được, và đó đúng là lý do vai này bị bỏ sót trong mọi đợt rà trước.
    parent_mail = STUDENTS[0][4]
    _activate_or_login("parent", parent_mail, PWD["parent"])


# ───────────────────────────── pha 3: chuỗi cung ứng ─────────────────────────────

def phase_supply():
    print("[3] NCC: hồ sơ hợp tác → duyệt → mặt hàng → lô → đơn mua → giao → nhận hàng")
    st, b = call("GET", "/partners/me", T["supplier"])
    if b.get("status") != "APPROVED":
        st, b = call_multipart("POST", "/partnership-applications", T["supplier"], parts={"meta": {
            "companyName": b.get("name") or "Nhà cung cấp", "taxId": b.get("taxId") or "2500782165",
            "address": b.get("address") or "Hà Nội", "schoolId": SCHOOL_CODE}})
        step("POST /partnership-applications (multipart)", st, b, tolerate=(409,))
        st, b = call("POST", f"/partners/{SUPPLIER_CODE}/approve", T["school"], {})
        step(f"POST /partners/{SUPPLIER_CODE}/approve", st, b, tolerate=(409,))

    for code, ing, label, unit, price in PRODUCTS:
        st, b = call("POST", "/supplier-products", T["supplier"], {
            "id": code, "ingredientId": ing, "label": label, "unit": unit,
            "unitPrice": price, "effectiveFrom": PRICE_FROM, "conversionToBase": 1})
        step(f"POST /supplier-products {code}", st, b, tolerate=(409,))

    for code, ing, label, unit, price in PRODUCTS:
        sb = "SB-" + code.split("-", 1)[1]
        st, b = call("POST", "/supplier-batches", T["supplier"], {
            "id": sb, "productId": code, "lotNumber": "LOT-" + code.split("-", 1)[1],
            "quantity": 50, "unit": unit, "manufacturingDate": MFG_DATE,
            "packagingDate": MFG_DATE, "expirationDate": EXP_DATE})
        step(f"POST /supplier-batches {sb}", st, b, tolerate=(409,))

    # ── Danh mục hợp đồng: NCC đề xuất → trường duyệt ───────────────────────────────
    # BẮT BUỘC từ 08/08/2026: `POST /purchase-requests` chặn nguyên liệu chưa nằm trong
    # `partnership_catalog_items` đã duyệt (ING-077). Trước đó bước này bỏ qua được, nên
    # cả luồng duyệt danh mục chưa từng chạy thật lần nào dù code có sẵn từ lâu.
    st, b = call("GET", "/supplier-partnerships", T["supplier"])
    ptn = (b.get("items") or [{}])[0].get("id")
    if not ptn:
        step("GET /supplier-partnerships (tìm mã hợp tác)", st, b, ok=False)
    for idx, (code, ing, label, unit, price) in enumerate(PRODUCTS, start=1):
        ccr = f"CCR-E2E-{idx:03d}"
        st, b = call("POST", f"/partnerships/{ptn}/catalog-change-requests", T["supplier"], {
            "id": ccr, "ingredientId": ing, "requestedUnit": unit,
            "requestedUnitPrice": price, "requestedConversionToBase": 1,
            "reason": "Đăng ký mặt hàng cung ứng theo hợp đồng"})
        step(f"POST /partnerships/{ptn}/catalog-change-requests {ccr}", st, b, tolerate=(409,))
        st, b = call("POST", f"/catalog-change-requests/{ccr}/decision", T["school"], {
            "approve": True, "note": "Duyệt theo hợp đồng đã ký"})
        step(f"POST /catalog-change-requests/{ccr}/decision (duyệt)", st, b, tolerate=(409, 422))

    pr = "PO-E2E-001"
    st, b = call("POST", "/purchase-requests", T["caterer"], {
        "id": pr, "supplierId": SUPPLIER_CODE, "deliveryDate": TODAY,
        "items": [{"ingredientId": p[1], "quantity": 5, "unit": p[3]} for p in PRODUCTS]})
    step(f"POST /purchase-requests {pr}", st, b, tolerate=(409,))

    st, b = call("POST", f"/purchase-requests/{pr}/quote", T["supplier"], {
        "items": [{"ingredientId": p[1], "unit": p[3], "unitPrice": p[4]} for p in PRODUCTS],
        "quoteNote": "Giá theo hợp đồng khung 2026"})
    step("POST quote", st, b, tolerate=(409,))
    st, b = call("POST", f"/purchase-requests/{pr}/accept-price", T["caterer"], {})
    step("POST accept-price", st, b, tolerate=(409,))
    st, b = call("POST", f"/purchase-requests/{pr}/confirm-schedule", T["supplier"], {
        "batchAssignments": {p[1]: "SB-" + p[0].split("-", 1)[1] for p in PRODUCTS}})
    step("POST confirm-schedule", st, b, tolerate=(409,))

    # Nhân viên giao hàng — BẮT BUỘC từ 08/08/2026: `POST /deliveries` chặn thiếu `driverId`
    # (truy vết "ai mang lô hàng này tới trường" khi có sự cố ATTP).
    dp = "DP-E2E-01"
    st, b = call("POST", "/delivery-persons", T["supplier"], {
        "id": dp, "fullName": "Lê Văn Giao", "nationalId": "001099212345",
        "phone": "0905112233", "vehiclePlate": "29C-123.45"})
    step(f"POST /delivery-persons {dp}", st, b, tolerate=(409,))

    dlv = "DLV-E2E-001"
    st, b = call("POST", "/deliveries", T["supplier"], {
        "id": dlv, "purchaseRequestId": pr, "scheduledAt": TODAY + "T05:30:00Z",
        "driverId": dp, "vehiclePlate": "29C-123.45",
        "supplierBatchIds": ["SB-" + p[0].split("-", 1)[1] for p in PRODUCTS]})
    step(f"POST /deliveries {dlv}", st, b, tolerate=(409,))
    st, b = call("PATCH", f"/deliveries/{dlv}/status", T["supplier"], {"status": "DISPATCHED"})
    step("PATCH delivery DISPATCHED", st, b)

    # ⚠️ `rejected` là boolean NGUYÊN THUỶ trong ReceivingLineRequest — thiếu nó server trả
    #    422 "malformed request body" mà không nói field nào (xem báo cáo mục "bẫy").
    zones = {"ING-THIT-HEO": "CHILLED", "ING-CA-BASA": "FROZEN",
             "ING-GAO-TE": "DRY", "ING-RAU-CAI": "CHILLED"}
    lines = [{"ingredientId": p[1], "supplierBatchId": "SB-" + p[0].split("-", 1)[1],
              "storageZone": zones[p[1]], "rejected": False} for p in PRODUCTS]
    st, b = call("POST", "/receiving-sessions", T["caterer"], {
        "deliveryId": dlv, "lat": 21.0245, "lng": 105.8412, "photosCount": 3,
        "deviceInfo": "seed_demo.py", "lines": lines})
    step("POST /receiving-sessions", st, b, tolerate=(409,))
    CTX["batches"] = ["SB-" + p[0].split("-", 1)[1] for p in PRODUCTS]


# ───────────────────────────── pha 4: thực đơn ─────────────────────────────

def phase_menu():
    print("[4] Thực đơn: tạo → nộp → duyệt → công bố (sinh phân phát cho từng học sinh)")
    st, grades = call("GET", "/grades", T["caterer"])
    grade_id = [g for g in grades if g["sortOrder"] == 1][0]["id"]

    menu = "MENU-E2E-001"
    st, b = call("POST", "/menus", T["caterer"], {
        "id": menu, "name": f"Thực đơn trưa {TODAY} — Khối 1", "menuDateIso": TODAY,
        "mealType": "LUNCH", "gradeId": grade_id, "audience": "ALL",
        "servings": len(STUDENTS), "week": TODAY,
        "blocksData": [{"mealType": "LUNCH", "portions": len(STUDENTS), "dishes": [
            {"dishId": c, "portions": len(STUDENTS)} for c, _ in MENU_DISHES]}]})
    step(f"POST /menus {menu}", st, b, tolerate=(409,))
    for path, token, label in [(f"/menus/{menu}/submit", T["caterer"], "submit"),
                               (f"/menus/{menu}/approve", T["school"], "approve"),
                               (f"/menus/{menu}/publish", T["school"], "publish")]:
        st, b = call("POST", path, token, {})
        step("POST " + label, st, b, tolerate=(409, 422))
    CTX["menu"] = menu

    st, b = call("GET", f"/student-distributions?date={TODAY}", T["caterer"])
    step(f"GET /student-distributions?date={TODAY} → {b.get('total')} suất", st, b)
    CTX["distributions"] = [i["id"] for i in b["items"]]


# ───────────────────────────── pha 5: kiểm thực + lưu mẫu ─────────────────────────────

def _fresh(name, batch):
    return {"name": name, "batchId": batch, "quantity": "5 kg",
            "receivedAt": TODAY + "T05:35:00Z", "supplierName": "NCC", "supplierContact": "0987654321",
            "deliverer": "Tài xế A", "hasInvoice": True, "hasVetCert": True,
            "hasQuarantineCert": True, "sensoryPass": True, "quickTest": "PASS",
            "action": "ACCEPT", "photoCount": 2}


def _dry(name, batch):
    return {"name": name, "batchId": batch, "producer": "NSX", "producerAddress": "Hà Nội",
            "receivedAt": TODAY + "T05:35:00Z", "quantity": "4 kg", "supplierName": "NCC",
            "deliverer": "Tài xế A", "supplierContact": "0987654321", "expiry": EXP_DATE,
            "storage": "THUONG", "hasInvoice": True, "sensoryPass": True,
            "action": "ACCEPT", "photoCount": 1}


def _step2(dish_code, dish_name):
    return {"shift": "TRUA", "dishId": dish_code, "dishName": dish_name,
            "mainIngredients": "theo định lượng", "servings": str(len(STUDENTS)),
            "prepDoneAt": TODAY + "T09:30:00Z", "cookDoneAt": TODAY + "T10:15:00Z",
            "hygieneStaff": True, "hygieneEquipment": True, "hygieneArea": True,
            "sensoryPass": True, "coreTemp": 78, "action": "ACCEPT", "photoCount": 1}


def _step3(dish_code, dish_name):
    return {"shift": "TRUA", "dishId": dish_code, "dishName": dish_name,
            "servings": str(len(STUDENTS)), "dividedDoneAt": TODAY + "T10:30:00Z",
            "eatStartAt": TODAY + "T11:00:00Z",
            # Cột (7) Mẫu 3 là ĐÁNH GIÁ Đạt/Không đạt (V56, 08/08/2026), không phải tên dụng cụ.
            # `utensilsPass` là boolean NGUYÊN THUỶ ⇒ thiếu nó server trả 422 "malformed request
            # body" mà KHÔNG nói field nào — đúng bẫy đã ghi ở `_step1_fresh`. `utensils` giữ lại
            # làm ghi chú (tuỳ chọn).
            "utensilsPass": True, "utensils": "Khay inox",
            "holdingTemp": 62, "sensoryPass": True, "action": "ACCEPT", "photoCount": 1}


# Lô kho tương ứng món nghi vấn — dùng cho cả xuất kho lẫn cách ly, để bản đồ truy xuất
# nối được "lô ⇄ món". `MENU_DISHES[0]` = Thịt kho trứng ⇒ nguyên liệu chính là thịt heo.
BATCH_OF_SUSPECT_DISH = "SB-THIT-HEO"


def phase_issue():
    """
    Xuất kho cho bữa trưa — mắt xích DUY NHẤT nối LÔ với MÓN.

    Thêm 08/08/2026 (backlog B5). `preparation_records` đã DROP ở V43, nên `inventory_transactions`
    (có cả `batch_id` lẫn `dish_id`) là đường duy nhất trả lời "lô nghi vấn đã đi vào món nào".
    Bỏ bước này thì bản đồ truy xuất của sự cố trả `dishes: []` — đúng nửa quan trọng nhất với
    người điều tra, và đó chính là điều đo được trước khi thêm pha này.
    """
    print("[5a] Xuất kho cho bữa trưa (nối lô ⇄ món cho bản đồ truy xuất)")
    st, b = call("POST", "/inventory/issue-slips", T["caterer"], {
        "serviceDate": TODAY, "mealType": "LUNCH", "menuIds": [CTX.get("menu", "MENU-E2E-001")],
        "note": "Xuất kho bữa trưa",
        "lines": [{"batchId": BATCH_OF_SUSPECT_DISH, "dishId": MENU_DISHES[0][0],
                   "quantity": 0.3, "unit": "kg"},
                  {"batchId": "SB-RAU-CAI", "dishId": MENU_DISHES[1][0],
                   "quantity": 0.2, "unit": "kg"}]})
    step("POST /inventory/issue-slips", st, b, tolerate=(409, 422))


def phase_inspection():
    print("[5] Kiểm thực 3 bước + đăng ký lưu mẫu")
    st, b = call("POST", "/inspections", T["caterer"], {"date": TODAY, "mealType": "LUNCH"})
    step("POST /inspections", st, b, tolerate=(409,))
    insp = b["id"] if st in (200, 201) else f"INSP-{TODAY.replace('-', '')}-L"
    CTX["inspection"] = insp

    # `formHeader` ĐÃ BỎ khỏi `SubmitStepRequest` (08/08/2026): nó là field ma — client gửi,
    # DTO nhận, không service nào lưu, rồi lúc đọc backend dựng lại header từ chính bản ghi
    # phiên (tên trường + người kiểm + địa chỉ trường). Không gửi nữa.
    st, b = call("POST", f"/inspections/{insp}/steps/1", T["caterer"], {
        "result": "PASSED",
        "step1Fresh": [_fresh("Thịt heo nạc", "SB-THIT-HEO"), _fresh("Rau cải ngọt", "SB-RAU-CAI")],
        "step1Dry": [_dry("Gạo tẻ", "SB-GAO-TE")]})
    step("POST steps/1", st, b)

    st, b = call("POST", f"/inspections/{insp}/steps/2", T["caterer"], {
        "result": "PASSED", "coreTemp": 78,
        "step2Rows": [_step2(c, n) for c, n in MENU_DISHES]})
    step("POST steps/2", st, b)

    st, b = call("POST", f"/inspections/{insp}/steps/3", T["caterer"], {
        "result": "PASSED", "holdingTemp": 62,
        "step3Rows": [_step3(c, n) for c, n in MENU_DISHES]})
    step("POST steps/3", st, b)

    # ⚠️ POST /samples là MULTIPART (part "payload") và BẮT BUỘC ≥1 ảnh (SMP-107);
    #    unit chỉ nhận "G"/"ML" viết hoa.
    for code, name in MENU_DISHES:
        st, b = call_multipart("POST", "/samples", T["caterer"], parts={"payload": {
            "id": "SMP-" + code.split("-", 1)[1], "inspectionId": insp, "dishId": code,
            "mealType": "LUNCH", "mealShift": "TRUA", "amount": 120, "unit": "G",
            "container": "Hộp inox", "storageLocation": "Tủ mẫu A", "fridgeTemp": 4,
            "servings": len(STUDENTS), "sealed": True, "sanitized": True,
            "collectedAt": TODAY + "T11:05:00Z"}},
            files={"evidencePhotos": ("mau.png", PNG_1PX, "image/png")})
        step(f"POST /samples {code}", st, b, tolerate=(409,))


# ───────────────────────────── pha 6: phát suất ─────────────────────────────

def phase_serve():
    print("[6] Phát suất ăn (cổng BR-094 phải mở vì kiểm thực đã PASSED)")
    # Giáo viên chỉ thấy suất của LỚP MÌNH CHỦ NHIỆM — chưa gán thì danh sách rỗng.
    st, b = call("PATCH", f"/classrooms/{CLASSROOM}/homeroom-teacher", T["school"],
                 {"teacherId": "STF-002"})
    step(f"PATCH /classrooms/{CLASSROOM}/homeroom-teacher", st, b, tolerate=(409,))

    st, b = call("GET", f"/inspections/serving-gate?date={TODAY}&mealType=LUNCH", T["teacher"])
    step("GET /inspections/serving-gate", st, b)
    print(f"       cổng: {b}")
    # ── Bếp giao suất tới lớp → giáo viên xác nhận nhận ───────────────────────────
    # Mắt xích này TRƯỚC ĐÂY BỊ BỎ SÓT trong seed (phát hiện 08/08/2026 khi Nam mở màn
    # "Giao suất tới lớp" thấy trống): `class_meal_deliveries` chưa từng có bản ghi nào, tức
    # bước bàn giao bếp → lớp chưa bao giờ được chạy thật dù code hai đầu đã có.
    st, b = call("POST", "/class-meal-deliveries", T["caterer"], {
        "date": TODAY, "mealType": "LUNCH", "classroomId": CLASSROOM,
        "note": "Giao suất trưa tới lớp"})
    step("POST /class-meal-deliveries", st, b, tolerate=(409,))
    cmd_code = b.get("id") if isinstance(b, dict) else None
    if cmd_code:
        st, r = call("POST", f"/class-meal-deliveries/{cmd_code}/receive", T["teacher"], {})
        step(f"POST /class-meal-deliveries/{cmd_code}/receive (GV xác nhận)", st, r,
             tolerate=(409, 422))

    st, b = call("GET", f"/student-distributions?date={TODAY}", T["teacher"])
    for code in [i["id"] for i in b["items"]]:
        st, r = call("POST", f"/student-distributions/{code}/serve", T["teacher"], {})
        step(f"POST serve {code}", st, r, tolerate=(409, 422))


# ───────────────────────────── pha 7: luồng sự cố ─────────────────────────────

def phase_incident():
    print("[7] Luồng sự cố: báo cáo → điều tra → cách ly lô → giữ mẫu → CAPA → đóng")
    st, b = call("POST", "/source-reports", T["teacher"], {
        "className": CLASSROOM, "description": "2 em lớp 1A đau bụng sau bữa trưa",
        "studentCode": STUDENTS[0][0], "mealDate": TODAY,
        "suspectedMealText": MENU_DISHES[0][1], "symptoms": ["đau bụng", "buồn nôn"],
        "evidencePhotos": 0})
    step("POST /source-reports", st, b)
    report = b["id"]

    st, b = call("POST", "/incident-records", T["medical"], {
        "content": f"Nghi ngờ ngộ độc thực phẩm sau bữa trưa {TODAY}", "location": "Lớp 1A",
        "occurredAt": TODAY + "T12:00:00Z", "sourceReportCodes": [report]})
    step("POST /incident-records", st, b)
    record = b["id"]

    # Phải COMPLETED mới mở điều tra được.
    st, b = call("POST", f"/incident-records/{record}/finalize", T["medical"], {})
    step("POST finalize", st, b)
    st, b = call("POST", f"/incident-records/{record}/open-investigation", T["medical"], {
        "title": "Nghi ngờ ngộ độc thực phẩm lớp 1A",
        "affectedStudentCodes": [STUDENTS[0][0], STUDENTS[1][0]], "mealDate": TODAY,
        "mealTimeLabel": "Trưa", "occurredAt": TODAY + "T12:00:00Z",
        "suspectedMealText": MENU_DISHES[0][1], "symptoms": ["đau bụng", "buồn nôn"]})
    step("POST open-investigation", st, b)
    inc = b["incidentCode"]
    CTX["incident"] = inc
    print(f"       sự cố: {inc}")

    st, b = call("GET", f"/incidents/{inc}/suspected-dish-candidates", T["medical"])
    step("GET suspected-dish-candidates", st, b)
    print("       ứng viên món: " + ", ".join(
        f"{x['dishCode']}={x['affectedStudentsWhoAte']} em" for x in b))

    st, b = call("POST", f"/incidents/{inc}/assess", T["medical"], {
        "severity": "MEDIUM", "clinicalRiskAssessment": "2 em triệu chứng nhẹ, theo dõi 24h",
        "suspectedDishCodes": [MENU_DISHES[0][0]]})
    step("POST assess", st, b)

    st, b = call("POST", f"/incidents/{inc}/sample-hold", T["medical"],
                 {"sampleCodes": ["SMP-" + MENU_DISHES[0][0].split("-", 1)[1]]})
    step("POST sample-hold", st, b)

    # Cách ly là hành động MỘT CHIỀU (IEM-149B) — lô đã cách ly ở lần chạy trước không dùng
    # lại được, nên chọn lô còn tự do thay vì cắm cứng một mã.
    st, inv = call("GET", "/inventory", T["caterer"])
    free = [i["id"] for i in inv["items"] if not i.get("quarantine")]
    if not free:
        raise SystemExit("Không còn lô kho nào chưa bị cách ly — chạy lại pha `supply` để nhập thêm.")
    # Ưu tiên lô CỦA MÓN NGHI VẤN để hồ sơ mạch lạc: bản đồ truy xuất nối được lô ⇄ món ⇄ học
    # sinh đã ăn. Trước đây lấy bừa `free[0]` nên có lần cách ly lô Gạo tẻ trong khi món nghi
    # vấn là Thịt kho trứng — bản đồ vẫn "đúng" nhưng vô nghĩa với người đọc hồ sơ.
    batch = BATCH_OF_SUSPECT_DISH if BATCH_OF_SUSPECT_DISH in free else free[0]
    st, b = call("POST", f"/incidents/{inc}/quarantine-batches", T["medical"], {
        "batchCodes": [batch], "reason": "Nghi ngờ liên quan món " + MENU_DISHES[0][1]})
    step(f"POST quarantine-batches {batch}", st, b)

    st, b = call("POST", f"/incidents/{inc}/root-cause", T["medical"], {
        "rootCause": "Thịt kho giữ nóng quá lâu trước khi chia suất",
        "rootCauseStatus": "FINAL"})      # chỉ nhận PRELIMINARY | FINAL
    step("POST root-cause", st, b)

    st, b = call("POST", "/corrective-actions", T["medical"], {
        "incidentCode": inc, "assigneeSupplierCode": "CAT-201",
        "description": "Rà soát quy trình giữ nóng và thời gian chờ chia suất"})
    step("POST /corrective-actions", st, b)
    capa = b["id"]

    st, b = call("POST", f"/incidents/{inc}/recovered-students", T["medical"],
                 [STUDENTS[0][0], STUDENTS[1][0]])
    step("POST recovered-students", st, b)

    for path, token, label in [(f"/corrective-actions/{capa}/submit-for-approval", T["medical"], "CAPA submit-for-approval"),
                               (f"/corrective-actions/{capa}/approve", T["school"], "CAPA approve"),
                               (f"/corrective-actions/{capa}/accept", T["caterer"], "CAPA accept")]:
        st, b = call("POST", path, token, {})
        step("POST " + label, st, b)

    # part BẮT BUỘC tên "files"; submit-evidence đếm ảnh THẬT nên phải upload trước.
    st, b = call_multipart("POST", f"/corrective-actions/{capa}/evidence-photos", T["caterer"],
                           files={"files": ("bangchung.png", PNG_1PX, "image/png")})
    step("POST CAPA evidence-photos", st, b)
    st, b = call("POST", f"/corrective-actions/{capa}/submit-evidence", T["caterer"],
                 {"evidenceNote": "Đã sửa quy trình giữ nóng", "evidencePhotos": 1})
    step("POST CAPA submit-evidence", st, b)
    st, b = call("POST", f"/corrective-actions/{capa}/validate", T["medical"], {})
    step("POST CAPA validate", st, b)

    st, b = call("POST", f"/incidents/{inc}/quarantine-batches/{batch}/disposition",
                 T["medical"], {"disposition": "RETURNED_TO_SUPPLIER", "note": "Trả NCC theo biên bản"})
    step("POST disposition", st, b)

    st, b = call("GET", f"/incidents/{inc}/closure-readiness", T["school"])
    step("GET closure-readiness", st, b)
    print(f"       {b}")
    st, b = call("POST", f"/incidents/{inc}/close", T["school"], {
        "closingStatement": "Đã xác định nguyên nhân, CAPA hoàn tất, lô nghi ngờ đã trả NCC."})
    step("POST close", st, b)
    print(f"       trạng thái sự cố: {b.get('status')}")


PHASES = {
    "bootstrap": phase_bootstrap,
    "accounts": phase_accounts,
    "catalog": phase_catalog,
    "students": phase_students,
    "supply": phase_supply,
    "menu": phase_menu,
    "issue": phase_issue,
    "inspection": phase_inspection,
    "serve": phase_serve,
    "incident": phase_incident,
}


def main(argv):
    wanted = argv[1:] or list(PHASES)
    unknown = [w for w in wanted if w not in PHASES]
    if unknown:
        raise SystemExit(f"Pha không có: {unknown}. Có: {list(PHASES)}")
    if "accounts" not in wanted:
        phase_accounts()            # luôn cần token
    for name in wanted:
        try:
            PHASES[name]()
        except ApiError as e:
            print(f"  ✗ dừng ở pha {name}: {e}")
            return 1
    print("\nXong. Dữ liệu demo đã sẵn trên", TODAY)
    return 0


if __name__ == "__main__":
    sys.exit(main(sys.argv))
