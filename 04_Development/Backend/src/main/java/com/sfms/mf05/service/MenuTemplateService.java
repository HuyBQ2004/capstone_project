package com.sfms.mf05.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.dto.MenuTemplateDtos.CreateMenuTemplateRequest;
import com.sfms.mf05.dto.MenuTemplateDtos.MenuTemplateResponse;

import java.util.Map;

/** UC-096 AF — thực đơn mẫu (TDS §8.1/§9.3.7). */
public interface MenuTemplateService {

    PagedResponse<MenuTemplateResponse> list(ListQuery query, AuthUser actor);

    MenuTemplateResponse detail(String code, AuthUser actor);

    MenuTemplateResponse create(CreateMenuTemplateRequest request, AuthUser actor);

    MenuTemplateResponse update(String code, Map<String, Object> body, AuthUser actor);

    void delete(String code, AuthUser actor);
}
