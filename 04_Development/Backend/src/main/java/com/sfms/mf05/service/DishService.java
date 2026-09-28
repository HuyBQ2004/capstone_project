package com.sfms.mf05.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.mf05.dto.DishDtos.CreateDishRequest;
import com.sfms.mf05.dto.DishDtos.DishResponse;
import com.sfms.mf05.dto.DishDtos.UpdateDishResult;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

/** UC-020 — thư viện món (TDS §8.1/§9.3.6). */
public interface DishService {

    PagedResponse<DishResponse> list(ListQuery query);

    DishResponse detail(String code);

    DishResponse create(CreateDishRequest request);

    /** {@code PUT} merge một phần (C8) — dùng riêng {@code {isActive:false}} để ngừng dùng. */
    UpdateDishResult update(String code, Map<String, Object> body);

    /** TDS §9.5 mục 11 — chặn xóa món đang được thực đơn tham chiếu. */
    void delete(String code);

    /** POST /dishes/{code}/image — D10-BIS, ảnh minh hoạ món (URL công khai, giống avatar). */
    DishResponse uploadImage(String code, MultipartFile file);
}
