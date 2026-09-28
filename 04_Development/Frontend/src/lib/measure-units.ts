"use client";

/**
 * Nguồn THẬT DUY NHẤT cho danh mục đơn vị đo — `GET /measure-units` (nối thật
 * 08/08/2026, backlog D5). Cùng mẫu với `useDietGroups()` ở `lib/diet-groups.ts`.
 *
 * Vì sao phải đọc từ server: bảng `measure_units` có 13 dòng và backend đã dùng nó để
 * validate (`MeasureUnitService.isValidUnit`) — tức người dùng gõ tay một đơn vị ngoài
 * bảng thì server TỪ CHỐI. Nhưng FE lại khai cứng 10 đơn vị ở `OFF_CATALOG_UNIT_OPTIONS`
 * và 4 hệ số ở `UNIT_FACTOR`, nên vừa thiếu lựa chọn hợp lệ vừa để lọt đơn vị không quy
 * đổi được. Đây là lần thứ 4 của cùng lớp lỗi "hằng số nhân đôi" (sau `NUTRITION_TARGET`,
 * `menuLeadTimeDays`, `STANDARD_DIETS`).
 */

import { useEffect, useMemo } from "react";
import { usePlainList } from "@/lib/api/hooks";
import type { MeasureUnitDto } from "@/lib/api/types";
import { setUnitFactors } from "@/lib/domain-utils";

export interface MeasureUnit {
  code: string;
  /** WEIGHT | VOLUME | COUNT */
  family: string;
  /**
   * Hệ số về đơn vị cơ sở của family (kg cho WEIGHT, lít cho VOLUME).
   * `null` với COUNT — đếm KHÔNG có quy đổi phổ quát, ĐỪNG đoán hệ số.
   */
  factorToBase: number | null;
}

function toMeasureUnit(u: MeasureUnitDto): MeasureUnit {
  return {
    code: u.code ?? "",
    family: u.family ?? "COUNT",
    factorToBase: u.factorToBase ?? null,
  };
}

/** Đơn vị cơ sở của mỗi family — khớp `factorToBase` do backend đo thật (kg=1, lít=1). */
const BASE_OF_FAMILY: Record<string, string> = { WEIGHT: "kg", VOLUME: "lít" };

export interface MeasureUnitCatalog {
  units: MeasureUnit[];
  /** Mã đơn vị cho `<option>` — đã sắp theo family rồi code (thứ tự server trả). */
  options: string[];
  /** true khi danh mục đã về; dùng để `disabled` dropdown thay vì hiện danh sách rỗng. */
  ready: boolean;
  /**
   * Quy đổi 1 lượng về đơn vị cơ sở. Đơn vị ĐẾM (COUNT) và đơn vị lạ giữ nguyên 1:1 —
   * đúng như `toBaseQuantity()` cũ, nhưng hệ số nay lấy từ DB chứ không khai tay.
   */
  toBase: (quantity: number, unit: string) => { qty: number; unit: string };
}

export function useMeasureUnits(): MeasureUnitCatalog {
  const { data: dtos = [], isSuccess } = usePlainList<MeasureUnitDto>("measure-units");

  /*
   * Nạp bảng hệ số THẬT cho `toBaseQuantity()` ở `mock-data.ts` (D5-FE, mảnh cuối).
   *
   * `toBaseQuantity` là hàm THUẦN nằm ở đáy ba chuỗi hàm thuần nên không gọi hook được; nó
   * đọc một biến cấp module mà hàm này ghi vào. Nhờ vậy bỏ được bảng 4 hệ số khai tay (DB có
   * 13 đơn vị) mà không phải sửa xuyên suốt mọi nơi gọi — xem Javadoc `setUnitFactors`.
   *
   * Chỉ nạp đơn vị CÓ hệ số (WEIGHT/VOLUME). Đơn vị ĐẾM `factorToBase = null` cố ý bỏ qua để
   * `toBaseQuantity` giữ nguyên 1:1 — "3 quả trứng" không quy đổi về đâu cả.
   */
  useEffect(() => {
    if (!isSuccess || dtos.length === 0) return;
    const table: Record<string, { base: string; factor: number }> = {};
    for (const u of dtos) {
      const base = u.family ? BASE_OF_FAMILY[u.family] : undefined;
      if (!u.code || !base || u.factorToBase == null) continue;
      table[u.code] = { base, factor: Number(u.factorToBase) };
    }
    if (Object.keys(table).length > 0) setUnitFactors(table);
  }, [dtos, isSuccess]);

  return useMemo(() => {
    const units = dtos.map(toMeasureUnit).filter((u) => u.code !== "");
    const byCode = new Map(units.map((u) => [u.code, u]));
    return {
      units,
      options: units.map((u) => u.code),
      ready: isSuccess && units.length > 0,
      toBase: (quantity, unit) => {
        const u = byCode.get(unit);
        const base = u ? BASE_OF_FAMILY[u.family] : undefined;
        if (!u || u.factorToBase == null || !base) return { qty: quantity, unit };
        return { qty: quantity * u.factorToBase, unit: base };
      },
    };
  }, [dtos, isSuccess]);
}
