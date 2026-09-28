#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
Sinh migration Flyway chứa dữ liệu test HÀNG LOẠT cho các bảng NỀN (đầu vào của luồng):
học sinh, phụ huynh, mặt hàng cung ứng (supplier_products), lô nguyên liệu (inventory_batches).

KHÔNG sinh dữ liệu cho các bảng THUỘC QUY TRÌNH (kiểm thực, nhận hàng, phiếu xuất kho, sự
cố...) — những cái đó nên test bằng tay qua đúng luồng nghiệp vụ thật, sinh sẵn sẽ che mất
lỗi luồng nếu có.

Cách dùng:
    cd 04_Development/Backend
    python scripts/generate_bulk_seed.py

Ghi ra: src/main/resources/db/migration/V<N>__bulk_test_seed.sql (N tự dò số kế tiếp).
Toàn bộ mã nghiệp vụ sinh ra đều có tiền tố "BULK" — dễ nhận ra, dễ xoá sau:
    DELETE FROM students WHERE code LIKE 'HS-BULK-%';
    DELETE FROM user_accounts WHERE code LIKE 'U-BULK-%';
    ... (script tự in hướng dẫn xoá đầy đủ ở cuối file SQL)

Tham chiếu tới dữ liệu ĐÃ CÓ (trường, năm học, nguyên liệu, đối tác) bằng subquery theo mã
nghiệp vụ (code) — không hardcode UUID của bản ghi có sẵn, nên script này chạy được trên bất
kỳ lần seed nào miễn các mã dưới đây còn tồn tại (đã xác nhận qua psql lúc viết script này).
"""

import random
import uuid
from datetime import date, timedelta
from pathlib import Path

random.seed(42)  # tái lập được — chạy lại ra đúng data này (đổi số nếu muốn bộ data khác)

SCHOOL_CODE = "SCH-MF03-0001"
ACADEMIC_YEAR_CODE = "AY-MF03-2026"
APPROVED_SUPPLIER_CODE = "SUP-101"   # RAW_FOOD_SUPPLIER, APPROVED — dùng cho supplier_products/inventory_batches
BCRYPT_HASH = "$2a$12$K19BeUNTqLNKiuAsHIkEsu8GBU6eRu.V0UmpNU5wFGevBNHMJuIo."  # = "P@ssw0rd123" (khớp mọi seed synthetic khác)

# 11 nguyên liệu đã có trong catalog_ingredients (code, unit, storage_zone) — đối chiếu psql.
INGREDIENTS = [
    ("ING-CA-BASA", "kg", "CHILLED"),
    ("ING-DAU-AN", "lít", "DRY"),
    ("ING-DAU-PHU", "kg", "CHILLED"),
    ("ING-GAO", "kg", "DRY"),
    ("ING-GAO-TAM", "kg", "DRY"),
    ("ING-RAU-CAI-NGOT", "kg", "CHILLED"),
    ("ING-THIT-GA", "kg", "CHILLED"),
    ("ING-THIT-LON-VAI", "kg", "CHILLED"),
    ("ING-TOM", "kg", "FROZEN"),
    ("ING-TOM-SU", "kg", "FROZEN"),
    ("ING-TRUNG-CUT", "quả", "CHILLED"),
]

# ───────────────────────── Cấu hình số lượng — chỉnh ở đây nếu cần nhiều/ít hơn ─────────────────────────
NEW_CLASSROOMS = ["1A", "1B", "2A", "2B", "3B", "4B", "5A", "5B"]  # 3A/4A đã có sẵn, không đụng
STUDENTS_PER_NEW_CLASSROOM = 12
STUDENTS_PER_EXISTING_CLASSROOM = 8  # thêm vào 3A/4A đã có (không đổi HS-3A-001.. cũ)
SUPPLIER_BATCHES_PER_INGREDIENT = 4   # lô nguyên liệu mỗi loại (trải nhiều ngày HSD để test FEFO/cảnh báo)

HO = ["Nguyễn", "Trần", "Lê", "Phạm", "Hoàng", "Huỳnh", "Vũ", "Võ", "Đặng", "Bùi", "Đỗ", "Ngô", "Dương", "Lý"]
TEN_DEM_NAM = ["Văn", "Minh", "Đức", "Quốc", "Anh", "Bảo", "Gia", "Hữu", "Thành", "Tuấn"]
TEN_DEM_NU = ["Thị", "Ngọc", "Thu", "Khánh", "Bảo", "Gia", "Hồng", "Diệp", "Linh", "Phương"]
TEN_NAM = ["Khang", "An", "Bình", "Cường", "Dũng", "Đạt", "Hùng", "Kiệt", "Long", "Minh", "Nam", "Phúc", "Sơn", "Tài", "Việt"]
TEN_NU = ["Anh", "Chi", "Diệp", "Hà", "Hân", "Huyền", "Lan", "Linh", "Mai", "Nhi", "Trang", "Vy", "Yến", "Ngọc"]

REL = ["Bố", "Mẹ"]


def vn_name(male: bool) -> str:
    ho = random.choice(HO)
    dem = random.choice(TEN_DEM_NAM if male else TEN_DEM_NU)
    ten = random.choice(TEN_NAM if male else TEN_NU)
    return f"{ho} {dem} {ten}"


def parent_name(male: bool) -> str:
    ho = random.choice(HO)
    dem = random.choice(TEN_DEM_NAM if male else TEN_DEM_NU)
    ten = random.choice(TEN_NAM if male else TEN_NU)
    return f"{ho} {dem} {ten} (synthetic)"


def dob_for_grade(grade: int) -> date:
    # Lớp 1 ~ 6 tuổi, mỗi khối +1 tuổi — dao động vài tháng cho tự nhiên.
    age = 5 + grade
    base = date(2026, 9, 1)
    years_back = base.year - age
    month = random.randint(1, 12)
    day = random.randint(1, 28)
    return date(years_back, month, day)


def sql_escape(s: str) -> str:
    return s.replace("'", "''")


def next_migration_number(migration_dir: Path) -> int:
    max_n = 0
    for f in migration_dir.glob("V*__*.sql"):
        try:
            n = int(f.name[1:].split("__")[0])
            max_n = max(max_n, n)
        except ValueError:
            continue
    return max_n + 1


def main() -> None:
    backend_dir = Path(__file__).resolve().parent.parent
    migration_dir = backend_dir / "src" / "main" / "resources" / "db" / "migration"
    if not migration_dir.exists():
        raise SystemExit(f"Không thấy thư mục migration: {migration_dir}")

    n = next_migration_number(migration_dir)
    out_path = migration_dir / f"V{n}__bulk_test_seed.sql"

    lines: list[str] = []
    a = lines.append

    a(f"-- V{n}__bulk_test_seed.sql — SINH TỰ ĐỘNG bởi scripts/generate_bulk_seed.py, KHÔNG sửa tay.")
    a("-- Dữ liệu NỀN cho test thủ công: học sinh, phụ huynh, mặt hàng cung ứng, lô nguyên liệu.")
    a("-- KHÔNG có dữ liệu quy trình (kiểm thực/nhận hàng/phiếu xuất/sự cố) — test tay đúng luồng.")
    a("-- Mọi mã nghiệp vụ sinh ra đều có tiền tố BULK — xem hướng dẫn xoá ở cuối file.")
    a("")

    # ═══════════════ 1. Lớp học mới (grade 1,2,5 + 3B/4B) ═══════════════
    a("-- ═══════════════ Lớp học mới ═══════════════")
    classroom_ids: dict[str, str] = {}
    for code in NEW_CLASSROOMS:
        grade = int(code[0])
        cid = str(uuid.uuid4())
        classroom_ids[code] = cid
        a(
            f"INSERT INTO classrooms (id, code, school_id, academic_year_id, name, grade, capacity) "
            f"SELECT '{cid}', '{code}', s.id, ay.id, '{code}', {grade}, 40 "
            f"FROM schools s, academic_years ay "
            f"WHERE s.code = '{SCHOOL_CODE}' AND ay.code = '{ACADEMIC_YEAR_CODE}' "
            f"ON CONFLICT (academic_year_id, code) DO NOTHING;"
        )
    a("")

    # ═══════════════ 2. Học sinh + phụ huynh ═══════════════
    a("-- ═══════════════ Học sinh + phụ huynh (mỗi học sinh 1 phụ huynh riêng) ═══════════════")

    reg_seq = 5001  # tránh đụng DTD-2026-0001..0004 đã có
    parent_seq = 1

    def emit_student_and_parent(classroom_code: str, seq_in_class: int, use_classroom_subquery: bool):
        nonlocal reg_seq, parent_seq
        male = random.random() < 0.5
        student_code = f"HS-BULK-{classroom_code}-{seq_in_class:03d}"
        student_name = vn_name(male) + " (synthetic)"
        reg_no = f"BULK-2026-{reg_seq:05d}"
        reg_seq += 1
        grade = int(classroom_code[0])
        dob = dob_for_grade(grade)
        student_id = str(uuid.uuid4())

        if use_classroom_subquery:
            classroom_ref = (
                f"(SELECT id FROM classrooms WHERE code = '{classroom_code}' "
                f"AND academic_year_id = (SELECT id FROM academic_years WHERE code = '{ACADEMIC_YEAR_CODE}'))"
            )
        else:
            classroom_ref = f"'{classroom_ids[classroom_code]}'"

        a(
            f"INSERT INTO students (id, code, registration_no, name, dob, school_id, classroom_id, status) "
            f"SELECT '{student_id}', '{student_code}', '{reg_no}', '{sql_escape(student_name)}', "
            f"DATE '{dob.isoformat()}', s.id, {classroom_ref}, 'ACTIVE' "
            f"FROM schools s WHERE s.code = '{SCHOOL_CODE}' "
            f"ON CONFLICT (school_id, code) DO NOTHING;"
        )

        parent_male = random.random() < 0.5
        p_name = parent_name(parent_male)
        p_code = f"U-BULK-{parent_seq:05d}"
        p_email = f"parent{parent_seq:05d}.bulk.synthetic@example.test"
        parent_id = str(uuid.uuid4())
        parent_seq += 1

        a(
            f"INSERT INTO user_accounts (id, code, account_scope, name, email, password_hash, role_id, "
            f"school_id, status, email_verified) "
            f"SELECT '{parent_id}', '{p_code}', 'PLATFORM', '{sql_escape(p_name)}', '{p_email}', "
            f"'{BCRYPT_HASH}', r.id, s.id, 'ACTIVE', true "
            f"FROM roles r, schools s WHERE r.role_key = 'parent' AND s.code = '{SCHOOL_CODE}' "
            f"ON CONFLICT (school_id, code) DO NOTHING;"
        )
        a(
            f"INSERT INTO student_parents (student_id, parent_user_id, relationship, is_primary) "
            f"VALUES ('{student_id}', '{parent_id}', '{REL[0] if parent_male else REL[1]}', true) "
            f"ON CONFLICT (student_id, parent_user_id) DO NOTHING;"
        )

    for code in NEW_CLASSROOMS:
        for i in range(1, STUDENTS_PER_NEW_CLASSROOM + 1):
            emit_student_and_parent(code, i, use_classroom_subquery=False)

    for code in ["3A", "4A"]:
        # +100 offset để không đụng số thứ tự học sinh gốc (HS-3A-001..) của 2 lớp này.
        for i in range(101, 101 + STUDENTS_PER_EXISTING_CLASSROOM):
            emit_student_and_parent(code, i, use_classroom_subquery=True)
    a("")

    # ═══════════════ 3. Mặt hàng cung ứng (supplier_products, SUP-101) ═══════════════
    a("-- ═══════════════ Mặt hàng cung ứng — SUP-101 báo giá đủ 11 nguyên liệu ═══════════════")
    sp_seq = 1
    # Giá tham khảo THEO TỪNG NGUYÊN LIỆU (₫) — khoảng giá thị trường thực tế, không dùng chung
    # một khoảng cho mọi "kg" (gạo/rau rẻ hơn hẳn tôm/thịt dù cùng đơn vị).
    ingredient_price_range = {
        "ING-CA-BASA": (45_000, 65_000),
        "ING-DAU-AN": (35_000, 55_000),
        "ING-DAU-PHU": (15_000, 25_000),
        "ING-GAO": (18_000, 25_000),
        "ING-GAO-TAM": (20_000, 28_000),
        "ING-RAU-CAI-NGOT": (15_000, 25_000),
        "ING-THIT-GA": (90_000, 130_000),
        "ING-THIT-LON-VAI": (110_000, 140_000),
        "ING-TOM": (150_000, 220_000),
        "ING-TOM-SU": (180_000, 260_000),
        "ING-TRUNG-CUT": (3_000, 4_500),
    }
    for ing_code, unit, _zone in INGREDIENTS:
        lo, hi = ingredient_price_range[ing_code]
        step = 500 if hi >= 20_000 else 100
        price = random.randint(lo // step, hi // step) * step
        sp_code = f"SP-BULK-{sp_seq:03d}"
        eff_from = date(2026, 1, 1) + timedelta(days=random.randint(0, 200))
        a(
            f"INSERT INTO supplier_products (id, code, partner_id, ingredient_id, unit_price, unit, "
            f"packaging, effective_from) "
            f"SELECT '{uuid.uuid4()}', '{sp_code}', p.id, ci.id, {price}, '{unit}', "
            f"'Đóng gói tiêu chuẩn', DATE '{eff_from.isoformat()}' "
            f"FROM partners p, catalog_ingredients ci "
            f"WHERE p.code = '{APPROVED_SUPPLIER_CODE}' AND ci.code = '{ing_code}' "
            f"ON CONFLICT (partner_id, code) DO NOTHING;"
        )
        sp_seq += 1
    a("")

    # ═══════════════ 4. Lô nguyên liệu (inventory_batches) ═══════════════
    a("-- ═══════════════ Lô nguyên liệu — trải HSD để test FEFO/cảnh báo sắp hết hạn ═══════════════")
    batch_seq = 1
    today = date(2026, 8, 6)  # khớp "hôm nay" của seed hệ thống (xem V17 mf09)
    # Lệch HSD đa dạng: đã hết hạn, sắp hết hạn (<3 ngày), còn hạn dài — để test đủ nhánh cảnh báo.
    expiry_offsets = [-2, 1, 2, 5, 10, 30, 60, 90]
    for ing_code, unit, zone in INGREDIENTS:
        for _ in range(SUPPLIER_BATCHES_PER_INGREDIENT):
            offset = random.choice(expiry_offsets)
            received = today - timedelta(days=random.randint(1, 10))
            expiry = received + timedelta(days=max(offset, 1) + random.randint(0, 5))
            if expiry <= received:
                expiry = received + timedelta(days=1)
            qty = round(random.uniform(10, 80), 1)
            batch_code = f"BATCH-BULK-{batch_seq:04d}"
            a(
                f"INSERT INTO inventory_batches (id, code, school_id, ingredient_id, ingredient_label, "
                f"supplier_id, initial_quantity, unit, storage_zone, received_date, expiry_date) "
                f"SELECT '{uuid.uuid4()}', '{batch_code}', s.id, ci.id, ci.name, p.id, {qty}, '{unit}', "
                f"'{zone}', DATE '{received.isoformat()}', DATE '{expiry.isoformat()}' "
                f"FROM schools s, catalog_ingredients ci, partners p "
                f"WHERE s.code = '{SCHOOL_CODE}' AND ci.code = '{ing_code}' AND p.code = '{APPROVED_SUPPLIER_CODE}' "
                f"ON CONFLICT (school_id, code) DO NOTHING;"
            )
            batch_seq += 1
    a("")

    a("-- ═══════════════ Xoá sạch dữ liệu BULK này (chạy tay khi không cần nữa) ═══════════════")
    a("-- DELETE FROM student_parents WHERE parent_user_id IN (SELECT id FROM user_accounts WHERE code LIKE 'U-BULK-%');")
    a("-- DELETE FROM students WHERE code LIKE 'HS-BULK-%';")
    a("-- DELETE FROM user_accounts WHERE code LIKE 'U-BULK-%';")
    a("-- DELETE FROM supplier_products WHERE code LIKE 'SP-BULK-%';")
    a("-- DELETE FROM inventory_batches WHERE code LIKE 'BATCH-BULK-%';")
    a("-- DELETE FROM classrooms WHERE code IN ('1A','1B','2A','2B','3B','4B','5A','5B');")

    out_path.write_text("\n".join(lines) + "\n", encoding="utf-8")
    total_students = len(NEW_CLASSROOMS) * STUDENTS_PER_NEW_CLASSROOM + 2 * STUDENTS_PER_EXISTING_CLASSROOM
    print(f"Đã ghi {out_path}")
    print(f"  - {len(NEW_CLASSROOMS)} lớp mới")
    print(f"  - {total_students} học sinh + {total_students} phụ huynh")
    print(f"  - {len(INGREDIENTS)} mặt hàng cung ứng mới (SUP-101)")
    print(f"  - {len(INGREDIENTS) * SUPPLIER_BATCHES_PER_INGREDIENT} lô nguyên liệu mới")


if __name__ == "__main__":
    main()
