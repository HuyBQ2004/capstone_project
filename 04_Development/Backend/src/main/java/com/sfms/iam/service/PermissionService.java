package com.sfms.iam.service;

import com.sfms.common.dto.PagedResponse;
import com.sfms.common.query.ListQuery;
import com.sfms.iam.domain.Permission;
import com.sfms.iam.dto.RoleDtos.PermissionResponse;
import com.sfms.iam.mapper.RoleMapper;
import com.sfms.iam.repository.PermissionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * UC-129 View List Permission — danh mục <b>16 bản ghi cố định, chỉ đọc</b> (ADR-M04).
 * Không có phương thức ghi: seed nằm ở {@code V2__seed_roles_permissions_policy.sql}.
 */
@Service
public class PermissionService {

    private final PermissionRepository repository;
    private final RoleMapper mapper;

    public PermissionService(PermissionRepository repository, RoleMapper mapper) {
        this.repository = repository;
        this.mapper = mapper;
    }

    @Transactional(readOnly = true)
    public PagedResponse<PermissionResponse> list(ListQuery query) {
        Page<Permission> page = repository.findAll(
                (root, cq, cb) -> cb.conjunction(),
                query.toPageable(Sort.by(Sort.Direction.ASC, "code")));
        return PagedResponse.of(page, mapper.toPermissionResponseList(page.getContent()),
                query.page(), query.pageSize());
    }
}
