package com.sfms.common.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import org.springframework.data.domain.Page;

import java.util.List;

/**
 * Hợp đồng phân trang bất biến cho <b>mọi</b> {@code GET /:resource} của 13 module
 * (Team Plan §2b-1 — dịch nguyên hàm {@code paginate()} của {@code mock-server.ts}).
 *
 * <p>Đúng <b>5 khóa</b>, <b>không</b> wrapper {@code data}/{@code result} — FE
 * {@code usePagedList<T>} đọc thẳng shape này.
 *
 * <p>Edge case (§2b-6):
 * <ul>
 *   <li>{@code page} vượt {@code pageCount} ⇒ <b>clamp</b> về {@code pageCount}</li>
 *   <li>tập rỗng ⇒ {@code pageCount=1}, {@code page=1}, {@code total=0}, HTTP <b>200</b> (không 404)</li>
 *   <li>{@code pageSize} ≤ 0 ⇒ clamp về 1</li>
 * </ul>
 *
 * <p>D8/S7: mọi GET danh sách khai response là schema generic này — Nam khai một lần.
 */
/*
 * ⚠️ KHÔNG đặt `name = "PagedResponse"` ở đây.
 *
 * Khai `name` = GHIM một tên schema duy nhất, nên springdoc đăng ký ĐÚNG MỘT schema cho
 * mọi tham số hoá và `items` tụt xuống `{}` (mảng không kiểu). Hệ quả đo được 02/08/2026:
 * cả 11 endpoint danh sách của 3 module đều trả `$ref: PagedResponse` với `items: {}` —
 * tức OpenAPI KHÔNG nói được item có hình dạng gì, đúng chỗ FE cần nhất. Đó là lý do đợt
 * nối MF03 phải curl tay từng endpoint để dò tên field (`clazz` vs `class`, thiếu
 * `justification`/`requestedBy`…) thay vì để trình sinh type bắt lúc biên dịch.
 *
 * Bỏ `name` đi thì springdoc sinh `PagedResponseStudentResponse`,
 * `PagedResponseClassroomResponse`… với `items` ĐÚNG KIỂU, và `openapi-typescript`
 * (`npm run gen:api`) dựng được type FE từ đó.
 */
@Schema(description = "Hợp đồng phân trang dùng chung (Team Plan §2b-1)")
public record PagedResponse<T>(

        @Schema(description = "Phần tử của trang hiện tại")
        List<T> items,

        @Schema(description = "Tổng bản ghi sau khi lọc", example = "42")
        long total,

        @Schema(description = "Trang hiện tại, 1-indexed, đã clamp ≤ pageCount", example = "1")
        int page,

        @Schema(description = "Số bản ghi mỗi trang", example = "8")
        int pageSize,

        @Schema(description = "= max(1, ceil(total/pageSize))", example = "6")
        int pageCount
) {

    /** Kích thước trang mặc định — khớp mock (`pageSize=8`), TDS-MF01 §4.4. */
    public static final int DEFAULT_PAGE_SIZE = 8;

    /**
     * @param p       kết quả Spring Data (0-indexed)
     * @param reqPage trang người dùng yêu cầu (1-indexed, chưa clamp)
     * @param reqSize pageSize đã clamp ≥ 1
     */
    public static <T> PagedResponse<T> of(Page<T> p, int reqPage, int reqSize) {
        int pageCount = Math.max(1, p.getTotalPages());
        int safePage = Math.min(Math.max(1, reqPage), pageCount);
        return new PagedResponse<>(p.getContent(), p.getTotalElements(), safePage, reqSize, pageCount);
    }

    /** Dựng từ danh sách đã map sang DTO, giữ nguyên metadata phân trang của Page gốc. */
    public static <S, T> PagedResponse<T> of(Page<S> p, List<T> mappedItems, int reqPage, int reqSize) {
        int pageCount = Math.max(1, p.getTotalPages());
        int safePage = Math.min(Math.max(1, reqPage), pageCount);
        return new PagedResponse<>(mappedItems, p.getTotalElements(), safePage, reqSize, pageCount);
    }

    public static <T> PagedResponse<T> empty(int reqPage, int reqSize) {
        return new PagedResponse<>(List.of(), 0L, 1, reqSize, 1);
    }
}
