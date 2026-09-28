/**
 * KIỂM TRA RÀNG BUỘC NGỮ NGHĨA CỦA MOCK DB
 *
 * Chạy: npm run audit:data
 *
 * Đây là tầng thứ ba, sau `tsc` và sau `findIsolatedNameFields()`:
 *
 *   tsc                     — kiểu dữ liệu đúng chưa
 *   findIsolatedNameFields  — bản ghi có nối bằng khoá không, hay nối bằng tên
 *   script này              — khoá nối ĐÚNG DÒNG chưa
 *
 * Loại lỗi ở đây là loại khó thấy nhất: khoá tồn tại, trỏ đúng bảng, nhưng trỏ
 * vào dòng sai — phiếu xuất "Bí đỏ" trỏ lô muối, mẫu lưu bữa 02/07 gắn vào sự cố
 * bữa 30/06, nấu ngày 06/07 bằng lô hết hạn 03/07. Màn hình vẫn render, không lỗi
 * nào báo ra, chỉ có nghiệp vụ là sai.
 *
 * QUY TẮC ĐỌC KẾT QUẢ: mỗi luật đếm số dòng THỰC SỰ kiểm được. Luật kiểm 0 dòng
 * là luật HỎNG (thường do đổi tên trường), không phải luật sạch — script thoát
 * với mã lỗi trong cả hai trường hợp.
 */
import * as M from "../src/lib/mock-data";

type V = { rule: string; id: string; detail: string };
const bad: V[] = [];
const seen = new Map<string, number>();
const add = (rule: string, id: string, detail: string) => bad.push({ rule, id, detail });
const check = (rule: string) => seen.set(rule, (seen.get(rule) ?? 0) + 1);

/** "dd/mm/yyyy [HH:MM]" hoặc "yyyy-mm-dd" → ms. Trả null nếu không phải ngày. */
function ts(s?: string): number | null {
  if (!s) return null;
  const m = s.match(/(\d{2})\/(\d{2})\/(\d{4})/);
  if (m) {
    const t = s.match(/(\d{1,2}):(\d{2})/);
    return new Date(+m[3], +m[2] - 1, +m[1], t ? +t[1] : 0, t ? +t[2] : 0).getTime();
  }
  const iso = s.match(/^(\d{4})-(\d{2})-(\d{2})/);
  return iso ? new Date(+iso[1], +iso[2] - 1, +iso[3]).getTime() : null;
}
const dayOf = (s?: string) => (s?.match(/\d{2}\/\d{2}\/\d{4}/) ?? [null])[0];
const isoToVn = (s?: string) => {
  const m = s?.match(/^(\d{4})-(\d{2})-(\d{2})/);
  return m ? `${m[3]}/${m[2]}/${m[1]}` : undefined;
};

// ===== 1. Phiếu xuất kho vs vòng đời lô =====
const batchById = new Map(M.INVENTORY.map((b) => [b.id, b]));
for (const t of M.INVENTORY_TRANSACTIONS as any[]) {
  const b: any = batchById.get(t.batchId);
  if (!b) continue;
  const use = ts(t.serviceDate) ?? ts(t.timestamp);
  if (!use) continue;
  if (ts(b.expiryDate)) {
    check("XUAT_LO_HET_HAN");
    if (ts(b.expiryDate)! < use)
      add("XUAT_LO_HET_HAN", t.id, `dùng ${t.serviceDate ?? t.timestamp} > HSD lô ${b.id} ${b.expiryDate}`);
  }
  if (ts(b.receivedDate)) {
    check("XUAT_TRUOC_KHI_NHAP");
    if (ts(b.receivedDate)! > use)
      add("XUAT_TRUOC_KHI_NHAP", t.id, `dùng ${t.serviceDate ?? t.timestamp} < ngày nhập lô ${b.id} ${b.receivedDate}`);
  }
  if (t.ingredientId && b.ingredientId) {
    check("PHIEU_SAI_NGUYEN_LIEU_CUA_LO");
    if (t.ingredientId !== b.ingredientId)
      add("PHIEU_SAI_NGUYEN_LIEU_CUA_LO", t.id, `phiếu ${t.ingredientId} nhưng lô ${b.id} là ${b.ingredientId}`);
  }
}

// ===== 2. Lô trong kho =====
for (const b of M.INVENTORY as any[]) {
  if (ts(b.receivedDate) && ts(b.expiryDate)) {
    check("NHAP_LO_DA_HET_HAN");
    if (ts(b.expiryDate)! < ts(b.receivedDate)!) add("NHAP_LO_DA_HET_HAN", b.id, `nhập ${b.receivedDate}, HSD ${b.expiryDate}`);
  }
}

// ===== 3. Mẫu lưu (QĐ 1246 — Mẫu số 5) =====
for (const s of M.FOOD_SAMPLES as any[]) {
  const cd = dayOf(s.collectedAt);
  const mealDM = s.meal?.match(/(\d{2}\/\d{2})/)?.[1];
  if (cd && mealDM) {
    check("MAU_SAI_BUA");
    if (!cd.startsWith(mealDM)) add("MAU_SAI_BUA", s.id, `meal="${s.meal}" nhưng collectedAt=${s.collectedAt}`);
  }
  const c = ts(s.collectedAt), r = ts(s.retentionDeadline);
  if (c && r) {
    check("HAN_LUU_KHONG_DU_24H");
    if (r - c < 24 * 3600e3 - 60e3) add("HAN_LUU_KHONG_DU_24H", s.id, `${s.collectedAt} → ${s.retentionDeadline} = ${((r - c) / 3600e3).toFixed(1)}h`);
  }
  if (c && ts(s.sentToLabAt)) {
    check("GUI_LAB_TRUOC_KHI_LAY");
    if (ts(s.sentToLabAt)! < c) add("GUI_LAB_TRUOC_KHI_LAY", s.id, `gửi ${s.sentToLabAt} < lấy ${s.collectedAt}`);
  }
  if (r && ts(s.disposedAt)) {
    check("HUY_MAU_TRUOC_HAN_24H");
    if (ts(s.disposedAt)! < r) add("HUY_MAU_TRUOC_HAN_24H", s.id, `huỷ ${s.disposedAt} < hạn lưu ${s.retentionDeadline}`);
  }
}

// ===== 4. Mẫu gắn sự cố phải nằm trong khung điều tra =====
const incById = new Map((M.INCIDENTS as any[]).map((i) => [i.id, i]));
for (const s of M.FOOD_SAMPLES as any[]) {
  const inc: any = s.incidentId ? incById.get(s.incidentId) : null;
  if (!inc) continue;
  const md = ts(inc.mealDate) ?? ts(inc.reportedAt);
  const c = ts(s.collectedAt);
  if (!md || !c) continue;
  // Mẫu lấy SAU sự cố quá 24h, hoặc trước đó quá 72h, thì không thể là tang chứng.
  check("MAU_NGOAI_KHUNG_SU_CO");
  if (c > md + 24 * 3600e3 || c < md - 72 * 3600e3)
    add("MAU_NGOAI_KHUNG_SU_CO", s.id, `mẫu ${s.collectedAt} vs sự cố ${s.incidentId} bữa ${inc.mealDate ?? inc.reportedAt}`);
}

// ===== 5. Phiếu bàn giao mẫu =====
const smpById = new Map((M.FOOD_SAMPLES as any[]).map((s) => [s.id, s]));
for (const q of M.SAMPLE_LAB_REQUESTS as any[]) {
  const req = ts(q.requestedAt);
  for (const sid of q.sampleIds ?? []) {
    const s: any = smpById.get(sid);
    if (!s) { add("PHIEU_TRO_MAU_KHONG_TON_TAI", q.id, sid); continue; }
    if (req && ts(s.collectedAt)) {
      check("BAN_GIAO_TRUOC_KHI_LAY_MAU");
      if (req < ts(s.collectedAt)!) add("BAN_GIAO_TRUOC_KHI_LAY_MAU", q.id, `phiếu ${q.requestedAt} < mẫu ${sid} lấy ${s.collectedAt}`);
    }
    if (q.incidentId && s.incidentId) {
      check("PHIEU_KHAC_SU_CO_VOI_MAU");
      if (q.incidentId !== s.incidentId)
        add("PHIEU_KHAC_SU_CO_VOI_MAU", q.id, `phiếu thuộc ${q.incidentId}, mẫu ${sid} thuộc ${s.incidentId}`);
    }
    // Quan hệ hai chiều phải khớp cả hai chiều, nếu không hai màn nói hai chuyện
    // khác nhau về cùng một hộp mẫu.
    check("MAU_KHONG_GAN_NGUOC_VE_PHIEU");
    if (s.labRequestId !== q.id)
      add("MAU_KHONG_GAN_NGUOC_VE_PHIEU", q.id, `phiếu liệt kê ${sid} nhưng mẫu ghi labRequestId=${s.labRequestId ?? "(trống)"}`);
  }
  if (q.handoverType === "AUTHORITY" || q.authorityId) {
    check("PHIEU_THU_GIU_THIEU_CO_QUAN");
    if (!q.authorityId) add("PHIEU_THU_GIU_THIEU_CO_QUAN", q.id, "handoverType=AUTHORITY nhưng thiếu authorityId");
  }
}

// ===== 6. Sổ phát suất =====
const stuById = new Map((M.STUDENTS as any[]).map((s) => [s.id, s]));
for (const d of M.STUDENT_DISTRIBUTIONS as any[]) {
  if (d.servedAt) {
    check("PHAT_SAI_NGAY");
    if (dayOf(d.servedAt) !== d.date) add("PHAT_SAI_NGAY", d.id, `date=${d.date} nhưng servedAt=${d.servedAt}`);
  }
  check("TRANG_THAI_VS_GIO_PHAT");
  if (d.status !== "SERVED" && d.servedAt) add("TRANG_THAI_VS_GIO_PHAT", d.id, `status=${d.status} nhưng đã có servedAt=${d.servedAt}`);
  if (d.status === "SERVED" && !d.servedAt) add("TRANG_THAI_VS_GIO_PHAT", d.id, "status=SERVED nhưng thiếu servedAt");
  if (d.status === "SERVED") {
    check("PHAT_ROI_KHONG_CO_MON");
    if (!d.servedDishes?.length) add("PHAT_ROI_KHONG_CO_MON", d.id, "SERVED nhưng servedDishes rỗng — mất dấu món đã trao");
  }
  const st: any = stuById.get(d.studentId);
  if (st) {
    check("HOC_SINH_SAI_LOP");
    if (st.class !== d.class) add("HOC_SINH_SAI_LOP", d.id, `${d.studentId} thuộc lớp ${st.class}, sổ ghi ${d.class}`);
    check("TEN_KHONG_KHOP_HOC_SINH");
    if (st.name !== d.name) add("TEN_KHONG_KHOP_HOC_SINH", d.id, `${d.studentId}="${st.name}", sổ ghi "${d.name}"`);
  }
}

// ===== 7. Kiểm thực trượt mà vẫn phát suất (BR-090/BR-096) =====
for (const s of M.INSPECTIONS as any[]) {
  const d = dayOf(s.date);
  if (!d || !s.overall) continue;
  check("PHAT_SUAT_DU_KIEM_THUC_TRUOT");
  if (!/KHÔNG ĐẠT|FAIL/i.test(String(s.overall))) continue;
  const served = (M.STUDENT_DISTRIBUTIONS as any[]).filter((x) => x.date === d && x.status === "SERVED");
  if (served.length)
    add("PHAT_SUAT_DU_KIEM_THUC_TRUOT", s.id, `${d}: ${served.length} suất SERVED dù phiên kiểm thực ${s.overall}`);
}

// ===== 8. Sổ chế biến =====
const menuById = new Map((M.MENUS as any[]).map((m) => [m.id, m]));
for (const p of M.PREPARATION_RECORDS as any[]) {
  const m: any = p.menuId ? menuById.get(p.menuId) : null;
  const md = m ? isoToVn(m.menuDateIso) : undefined;
  if (md) {
    check("CHE_BIEN_SAI_NGAY_THUC_DON");
    if (md !== p.date) add("CHE_BIEN_SAI_NGAY_THUC_DON", p.id, `sổ ghi ${p.date}, thực đơn ${p.menuId} phục vụ ${md}`);
  }
  for (const bid of p.batchesUsed ?? []) {
    const b: any = batchById.get(bid);
    if (!b) { add("SO_CHE_BIEN_TRO_LO_KHONG_TON_TAI", p.id, bid); continue; }
    if (ts(b.expiryDate) && ts(p.date)) {
      check("CHE_BIEN_DUNG_LO_HET_HAN");
      if (ts(b.expiryDate)! < ts(p.date)!) add("CHE_BIEN_DUNG_LO_HET_HAN", p.id, `chế biến ${p.date} dùng lô ${bid} HSD ${b.expiryDate}`);
    }
    check("LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT");
    const hasTxn = (M.INVENTORY_TRANSACTIONS as any[]).some(
      (t) => t.batchId === bid && (t.serviceDate === p.date || dayOf(t.timestamp) === p.date)
    );
    if (!hasTxn) add("LO_CHE_BIEN_KHONG_CO_PHIEU_XUAT", p.id, `dùng lô ${bid} ngày ${p.date} nhưng không có phiếu xuất tương ứng`);
  }
}

// ===== 9. Sự cố, dòng thời gian & CAPA =====
for (const i of M.INCIDENTS as any[]) {
  if (ts(i.mealDate) && ts(i.reportedAt)) {
    check("BAO_CAO_TRUOC_BUA_AN");
    if (ts(i.reportedAt)! < ts(i.mealDate)!) add("BAO_CAO_TRUOC_BUA_AN", i.id, `bữa ${i.mealDate}, báo cáo ${i.reportedAt}`);
  }
  for (const bid of i.quarantineBatches ?? []) {
    check("CACH_LY_LO_KHONG_LIEN_QUAN_BUA");
    const used = (M.INVENTORY_TRANSACTIONS as any[]).some((t) => t.batchId === bid && t.serviceDate === i.mealDate);
    if (!used) add("CACH_LY_LO_KHONG_LIEN_QUAN_BUA", i.id, `cách ly lô ${bid} nhưng không có phiếu xuất lô này cho bữa ${i.mealDate}`);
  }
  for (const sid of i.sourceReportIds ?? []) {
    const r: any = (M.SOURCE_REPORTS as any[]).find((x) => x.id === sid);
    if (!r) { add("SU_CO_TRO_BAO_CAO_KHONG_TON_TAI", i.id, sid); continue; }
    const rd = ts(r.reportedAt ?? r.createdAt ?? r.submittedAt);
    const md = ts(i.mealDate);
    if (rd && md) {
      check("BAO_CAO_NGUON_KHAC_BUA");
      if (rd < md || rd > md + 7 * 24 * 3600e3)
        add("BAO_CAO_NGUON_KHAC_BUA", i.id, `báo cáo ${sid} lúc ${r.reportedAt ?? r.createdAt ?? r.submittedAt} ngoài khung 7 ngày sau bữa ${i.mealDate}`);
    }
  }
}
const evByInc = new Map<string, any[]>();
for (const e of M.INCIDENT_EVENTS as any[]) {
  if (!evByInc.has(e.incidentId)) evByInc.set(e.incidentId, []);
  evByInc.get(e.incidentId)!.push(e);
}
for (const [iid, evs] of evByInc) {
  const inc: any = incById.get(iid);
  if (!inc) { add("MOC_TRO_SU_CO_KHONG_TON_TAI", iid, `${evs.length} mốc`); continue; }
  const base = ts(inc.mealDate) ?? ts(inc.reportedAt);
  let prev = -Infinity;
  for (const e of evs) {
    const t = ts(e.at);
    if (t == null) continue;
    if (base) {
      check("MOC_TRUOC_KHI_SU_CO_XAY_RA");
      if (t < base) add("MOC_TRUOC_KHI_SU_CO_XAY_RA", e.id, `${e.at} < bữa/báo cáo ${inc.mealDate ?? inc.reportedAt}`);
    }
    check("DONG_THOI_GIAN_LUI");
    if (t < prev) add("DONG_THOI_GIAN_LUI", e.id, `${e.at} đứng sau một mốc muộn hơn`);
    prev = t;
  }
}
for (const c of M.CORRECTIVE_ACTIONS as any[]) {
  const inc: any = c.incidentId ? incById.get(c.incidentId) : null;
  const sub = ts(c.submittedForApprovalAt);
  if (inc && sub) {
    check("CAPA_TRUOC_SU_CO");
    const im = ts(inc.reportedAt) ?? ts(inc.mealDate);
    if (im && sub < im) add("CAPA_TRUOC_SU_CO", c.id, `CAPA gửi duyệt ${c.submittedForApprovalAt} < sự cố ${c.incidentId} ${inc.reportedAt ?? inc.mealDate}`);
  }
  if (c.assigneeSupplierId) {
    check("CAPA_GIAO_NCC_KHONG_TON_TAI");
    if (!(M.PARTNERS as any[]).some((p) => p.id === c.assigneeSupplierId))
      add("CAPA_GIAO_NCC_KHONG_TON_TAI", c.id, c.assigneeSupplierId);
  }
}

// ===== 9b. Cách ly lô: nguồn gốc và lối ra =====
for (const b of M.INVENTORY as any[]) {
  if (!b.quarantine) continue;
  // Cách ly luôn phải trả lời được "vì sự cố nào". Thiếu khoá này thì lúc kết luận
  // không dò ngược được lô nào thuộc sự cố nào để mở khoá.
  check("CACH_LY_KHONG_RO_SU_CO");
  const iid = b.quarantine.incidentId;
  if (iid && !incById.has(iid))
    add("CACH_LY_KHONG_RO_SU_CO", b.id, `quarantine.incidentId="${iid}" không phải mã sự cố nào`);
  if (!iid && !/đình chỉ|hết hạn|thu hồi/i.test(String(b.quarantine.reason ?? "")))
    add("CACH_LY_KHONG_RO_SU_CO", b.id, `cách ly không gắn sự cố và lý do không tự giải thích: "${b.quarantine.reason}"`);
}
// Sự cố đã đóng mà lô vẫn khoá thì lô nằm khoá vĩnh viễn — không màn nào nhắc nữa.
for (const i of M.INCIDENTS as any[]) {
  if (i.status !== "CLOSED") continue;
  check("DONG_SU_CO_MA_CON_LO_KHOA");
  if ((i.quarantineBatches ?? []).length)
    add("DONG_SU_CO_MA_CON_LO_KHOA", i.id, `đã đóng nhưng còn khoá ${(i.quarantineBatches ?? []).join(", ")}`);
}

// ===== 10. Nhà cung cấp: mặt hàng, lô, giao hàng =====
const prodById = new Map((M.SUPPLIER_PRODUCTS as any[]).map((p) => [p.id, p]));
for (const b of M.SUPPLIER_BATCHES as any[]) {
  const p: any = b.productId ? prodById.get(b.productId) : null;
  if (!p) { if (b.productId) add("LO_TRO_MAT_HANG_KHONG_TON_TAI", b.id, b.productId); continue; }
  const mfg = ts(b.manufacturingDate), pack = ts(b.packagingDate), exp = ts(b.expirationDate);
  if (mfg && exp) {
    check("HSD_TRUOC_NGAY_SX");
    if (exp <= mfg) add("HSD_TRUOC_NGAY_SX", b.id, `SX ${b.manufacturingDate}, HSD ${b.expirationDate}`);
  }
  if (mfg && pack) {
    check("DONG_GOI_TRUOC_KHI_SX");
    if (pack < mfg) add("DONG_GOI_TRUOC_KHI_SX", b.id, `SX ${b.manufacturingDate}, đóng gói ${b.packagingDate}`);
  }
  const catName = (M.INGREDIENT_CATALOG as any[]).find((c) => c.id === p.ingredientId)?.name;
  if (b.productName && catName) {
    check("TEN_LO_KHAC_MAT_HANG");
    if (b.productName !== catName && b.productName !== p.label)
      add("TEN_LO_KHAC_MAT_HANG", b.id, `lô ghi "${b.productName}", mặt hàng là "${p.label ?? catName}"`);
  }
}
const prById = new Map((M.PURCHASE_REQUESTS as any[]).map((p) => [p.id, p]));
for (const d of M.DELIVERIES as any[]) {
  const r: any = d.purchaseRequestId ? prById.get(d.purchaseRequestId) : null;
  if (!r) continue;
  const dt = ts(d.scheduledAt), rt = ts(r.requestedDate);
  if (dt && rt) {
    check("GIAO_TRUOC_KHI_DAT");
    if (dt < rt) add("GIAO_TRUOC_KHI_DAT", d.id, `giao ${d.scheduledAt} < đề nghị mua ${d.purchaseRequestId} ngày ${r.requestedDate}`);
  }
  for (const bid of d.linkedBatches ?? []) {
    const sb: any = (M.SUPPLIER_BATCHES as any[]).find((x) => x.id === bid);
    const prod: any = sb?.productId ? prodById.get(sb.productId) : null;
    if (prod && r.supplierId) {
      check("CHUYEN_GIAO_LO_KHAC_NCC");
      if (prod.supplierId !== r.supplierId)
        add("CHUYEN_GIAO_LO_KHAC_NCC", d.id, `lô ${bid} thuộc ${prod.supplierId} nhưng đơn ${d.purchaseRequestId} đặt của ${r.supplierId}`);
    }
  }
}
for (const p of M.PARTNERS as any[]) {
  const e = ts(p.certExpiry);
  if (!e) continue;
  check("MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN");
  const late = (M.PURCHASE_REQUESTS as any[]).filter((r) => r.supplierId === p.id && (ts(r.deliveryDate) ?? 0) > e);
  if (late.length)
    add("MUA_HANG_SAU_KHI_GIAY_TO_HET_HAN", p.id, `giấy tờ hết hạn ${p.certExpiry}, còn ${late.length} đơn giao sau đó (${late.map((x: any) => x.id).join(", ")})`);
}

// ===== 10b. Lô vs phiên nhận hàng — gốc của chuỗi truy xuất BR-076 =====
for (const b of M.INVENTORY as any[]) {
  check("LO_KHONG_CO_PHIEU_NHAN");
  const rcv = (M.RECEIVING_SESSIONS as any[]).filter((r) => r.batches?.includes(b.id));
  if (rcv.length === 0) add("LO_KHONG_CO_PHIEU_NHAN", b.id, `${b.ingredient} — không phiên nhận nào ghi lô này`);
  for (const r of rcv) {
    check("NHAN_LO_TU_PHIEU_BI_TU_CHOI");
    if (r.status === "REJECTED") add("NHAN_LO_TU_PHIEU_BI_TU_CHOI", b.id, `lô vào kho nhưng phiếu ${r.id} bị TỪ CHỐI`);
    check("NGAY_NHAP_LO_KHAC_PHIEU_NHAN");
    if (dayOf(r.deliveryDate) && dayOf(r.deliveryDate) !== b.receivedDate)
      add("NGAY_NHAP_LO_KHAC_PHIEU_NHAN", b.id, `lô ghi ${b.receivedDate}, phiếu ${r.id} giao ${r.deliveryDate}`);
    check("NCC_LO_KHAC_NCC_PHIEU_NHAN");
    if (b.supplier && r.supplier && !r.supplier.includes(b.supplier))
      add("NCC_LO_KHAC_NCC_PHIEU_NHAN", b.id, `lô ghi "${b.supplier}", phiếu ${r.id} của "${r.supplier}"`);
  }
}

// ===== 10c. BR-070: không nhận hàng của NCC đã mất hiệu lực giấy tờ =====
for (const r of M.RECEIVING_SESSIONS as any[]) {
  const p: any = (M.PARTNERS as any[]).find((x) => x.name === r.supplier);
  if (!p?.certExpiry || r.status !== "RECEIVED") continue;
  check("NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN");
  const e = ts(p.certExpiry), d = ts(r.deliveryDate);
  if (e && d && d > e)
    add("NHAN_HANG_SAU_KHI_CHUNG_NHAN_HET_HAN", r.id, `nhận ${r.deliveryDate} của ${p.name}, chứng nhận hết hiệu lực ${p.certExpiry}`);
  check("PHIEU_GHI_SAI_TRANG_THAI_NCC");
  if (r.supplierStatus && p.status && r.supplierStatus !== p.status && ts(r.deliveryDate)! > ts("01/07/2026")!)
    add("PHIEU_GHI_SAI_TRANG_THAI_NCC", r.id, `phiếu ghi ${r.supplierStatus}, hồ sơ đối tác ${p.status}`);
}

// ===== 11. Thực đơn =====
for (const m of M.MENUS as any[]) {
  const sd = ts(isoToVn(m.menuDateIso));
  if (ts(m.submittedAt) && sd) {
    check("GUI_DUYET_SAU_NGAY_PHUC_VU");
    if (ts(m.submittedAt)! > sd) add("GUI_DUYET_SAU_NGAY_PHUC_VU", m.id, `phục vụ ${isoToVn(m.menuDateIso)}, gửi duyệt ${m.submittedAt}`);
  }
  check("DA_DUYET_MA_THIEU_NGUOI_DUYET");
  if ((m.status === "APPROVED" || m.status === "PUBLISHED") && !m.approvedBy)
    add("DA_DUYET_MA_THIEU_NGUOI_DUYET", m.id, `status=${m.status} nhưng thiếu approvedBy`);
  // Vượt trần khi đang PENDING_APPROVAL là HỢP LỆ — đó chính là ca School Admin
  // phải thấy cảnh báo BR-080. Chỉ sai khi đã duyệt mà vẫn vượt.
  if (m.estimatedCost && m.costCeiling) {
    check("DA_DUYET_MA_VAN_VUOT_TRAN");
    if (m.estimatedCost > m.costCeiling && (m.status === "APPROVED" || m.status === "PUBLISHED"))
      add("DA_DUYET_MA_VAN_VUOT_TRAN", m.id, `${m.estimatedCost}₫ > trần ${m.costCeiling}₫ mà status=${m.status}`);
  }
}

// ===== 11b. Suất riêng theo chế độ ăn (BR-065/BR-125) =====
// Thực đơn riêng nay là `Menu` (audience != "ALL") — FE nhìn per-buổi qua projection.
const SPECIAL_MEALS = M.specialMenuMeals(M.MENUS as never);
for (const m of M.MENUS as any[]) {
  if (M.menuAudienceOf(m) !== "ALL") continue; // chỉ soi thực đơn CHUNG của khối
  // Thực đơn đã nộp duyệt trở lên thì mọi nhóm chế độ ăn có học sinh trong khối
  // phải ĂN ĐƯỢC: hoặc bữa gốc đã hợp, hoặc có suất riêng được phép nấu.
  if (m.status !== "PENDING_APPROVAL" && m.status !== "APPROVED" && m.status !== "PUBLISHED") continue;
  check("NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN");
  const gaps = M.dietCoverageGaps(m.grade, m.id, (m.blocksData ?? []) as never, SPECIAL_MEALS as never);
  if (gaps.length)
    add(
      "NOP_DUYET_MA_NHOM_CHE_DO_AN_CHUA_CO_GI_AN",
      m.id,
      `${m.grade} · ${m.status} — ` +
        gaps.map((g: any) => `${g.mealType}/${g.group.id}(${g.students} em)`).join(", ")
    );
}
for (const v of SPECIAL_MEALS as any[]) {
  check("SUAT_RIENG_TRO_THUC_DON_KHONG_TON_TAI");
  const m: any = (M.MENUS as any[]).find((x) => x.id === v.menuId);
  if (!m) { add("SUAT_RIENG_TRO_THUC_DON_KHONG_TON_TAI", v.id, v.menuId); continue; }
  // Suất thêm vào thực đơn đã công bố phải nêu lý do và chờ duyệt — không thể
  // vừa là "theo kế hoạch" vừa gắn vào thứ đã ký xong.
  check("SUAT_BO_SUNG_KHONG_RO_NGUON_GOC");
  const editable = m.status === "DRAFT" || m.status === "REJECTED";
  if (v.status === "PENDING_APPROVAL" && !v.reason)
    add("SUAT_BO_SUNG_KHONG_RO_NGUON_GOC", v.id, "chờ duyệt bổ sung nhưng không ghi lý do phát sinh");
  if (v.status === "APPROVED" && !v.approvedBy)
    add("SUAT_BO_SUNG_KHONG_RO_NGUON_GOC", v.id, "đã duyệt nhưng không có người duyệt");
  check("SUAT_RIENG_KHONG_CO_NGUOI_AN");
  const members = (M.STUDENTS as any[]).filter(
    (s) =>
      M.gradeOfClass(s.class) === m.grade &&
      s.specialDiet === (M.DIET_GROUPS as any[]).find((g) => g.id === v.dietGroupId)?.matchDiet
  );
  if (members.length === 0)
    add("SUAT_RIENG_KHONG_CO_NGUOI_AN", v.id, `${v.dietGroupId} ở ${m.grade} không có học sinh nào`);
  if (editable && v.status === "PENDING_APPROVAL")
    add("SUAT_BO_SUNG_KHONG_RO_NGUON_GOC", v.id, `thực đơn còn ${m.status} thì suất đi kèm thực đơn, không duyệt riêng`);
}

// ===== 11c. Bảng nấu ăn — không món nào vượt sĩ số =====
{
  const doneKeys = new Set<string>();
  for (const m of M.MENUS as any[]) {
    if (!M.isMenuLive(m) || !m.menuDateIso) continue;
    for (const b of m.blocksData ?? []) {
      const key = `${m.menuDateIso}/${b.mealType}`;
      if (doneKeys.has(key)) continue;
      doneKeys.add(key);
      const sheet = M.cookSheet(m.menuDateIso, b.mealType);
      // Sĩ số các khối cùng ăn bữa này — trần cho MỖI dòng món (một em ăn nhiều
      // món nên tổng suất-món lớn hơn sĩ số là bình thường; một MÓN nhiều suất
      // hơn số người ăn thì mới sai).
      const enrol = (M.MENUS as any[])
        .filter((x) => M.menuAudienceOf(x) === "ALL" && M.isMenuLive(x) && x.menuDateIso === m.menuDateIso &&
          (x.blocksData ?? []).some((bb: any) => bb.mealType === b.mealType))
        .reduce((s, x) => s + (x.servings ?? 0), 0);
      for (const line of [...sheet.main, ...sheet.special]) {
        check("BANG_NAU_MON_VUOT_SI_SO");
        if (line.portions > enrol)
          add("BANG_NAU_MON_VUOT_SI_SO", key, `"${line.dish}" ${line.portions} suất > sĩ số ${enrol}`);
        check("BANG_NAU_SO_SUAT_KHONG_DUONG");
        if (line.portions <= 0)
          add("BANG_NAU_SO_SUAT_KHONG_DUONG", key, `"${line.dish}" = ${line.portions} suất`);
      }
    }
  }
}

// ===== 12. Khai báo dị ứng =====
for (const a of M.ALLERGY_DECLARATIONS as any[]) {
  check("KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI");
  if (!stuById.has(a.studentId)) add("KHAI_DI_UNG_TRO_HOC_SINH_KHONG_TON_TAI", a.id, a.studentId);
  // BR-031 chỉ bắt buộc tài liệu y tế với mức NẶNG — mức nhẹ/trung bình Y tế xác
  // minh trực tiếp với phụ huynh, không có tệp vẫn hợp lệ.
  if (a.severity === "Nặng") {
    check("XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG");
    if (a.status === "VERIFIED" && !a.proofFile)
      add("XAC_MINH_MUC_NANG_KHONG_CO_MINH_CHUNG", a.id, `${a.studentId} — ${a.allergen} mức Nặng đã xác minh nhưng thiếu tài liệu y tế`);
  }
}

// ===== Kết quả =====
const byRule = new Map<string, V[]>();
for (const v of bad) {
  if (!byRule.has(v.rule)) byRule.set(v.rule, []);
  byRule.get(v.rule)!.push(v);
}
const empty = [...seen].filter(([, n]) => n === 0).map(([r]) => r);

if (bad.length === 0 && empty.length === 0) {
  console.log(`✔ ${seen.size} luật ngữ nghĩa, không có vi phạm.`);
  process.exit(0);
}
for (const [rule, vs] of [...byRule].sort((a, b) => b[1].length - a[1].length)) {
  console.log(`\n## ${rule} — ${vs.length} (kiểm ${seen.get(rule) ?? 0} dòng)`);
  for (const v of vs.slice(0, 10)) console.log(`   ${v.id}: ${v.detail}`);
  if (vs.length > 10) console.log(`   ... +${vs.length - 10}`);
}
if (empty.length) console.log(`\n!! Luật không kiểm được dòng nào (nhiều khả năng đổi tên trường): ${empty.join(", ")}`);
console.log(`\n✘ ${bad.length} vi phạm / ${byRule.size} loại / ${seen.size} luật.`);
process.exit(1);
