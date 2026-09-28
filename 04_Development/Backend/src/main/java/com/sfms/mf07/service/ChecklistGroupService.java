package com.sfms.mf07.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.dto.ChecklistDtos.CreateGroupRequest;
import com.sfms.mf07.dto.ChecklistDtos.GroupResponse;
import com.sfms.mf07.dto.ChecklistDtos.UpdateGroupRequest;

import java.util.List;

/** Checklist động (BR-207/BR-210 — UC-186). M07-D10: MF07 sở hữu bảng + controller. */
public interface ChecklistGroupService {

    List<GroupResponse> list(AuthUser actor);

    GroupResponse create(CreateGroupRequest request, AuthUser actor);

    GroupResponse update(String code, UpdateGroupRequest request, AuthUser actor);

    /** CF-15 — DELETE thật đổi thành soft-delete (is_active=false); 409 nếu đang được tham chiếu. */
    void delete(String code, boolean confirmed, AuthUser actor);
}
