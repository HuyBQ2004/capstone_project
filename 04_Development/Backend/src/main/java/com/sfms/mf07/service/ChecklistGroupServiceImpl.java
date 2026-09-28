package com.sfms.mf07.service;

import com.sfms.iam.security.AuthUser;
import com.sfms.mf07.domain.ChecklistGroup;
import com.sfms.mf07.domain.ChecklistItem;
import com.sfms.mf07.dto.ChecklistDtos.CreateGroupRequest;
import com.sfms.mf07.dto.ChecklistDtos.GroupResponse;
import com.sfms.mf07.dto.ChecklistDtos.ItemRequest;
import com.sfms.mf07.dto.ChecklistDtos.ItemResponse;
import com.sfms.mf07.dto.ChecklistDtos.UpdateGroupRequest;
import com.sfms.mf07.error.InspError;
import com.sfms.mf07.error.InspExceptions;
import com.sfms.common.dto.ErrorResponse;
import com.sfms.common.error.ApiException;
import com.sfms.mf07.repository.ChecklistGroupRepository;
import com.sfms.mf07.repository.ChecklistItemResultRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

@Service
public class ChecklistGroupServiceImpl implements ChecklistGroupService {

    private final ChecklistGroupRepository groupRepository;
    private final ChecklistItemResultRepository resultRepository;

    public ChecklistGroupServiceImpl(ChecklistGroupRepository groupRepository,
                                      ChecklistItemResultRepository resultRepository) {
        this.groupRepository = groupRepository;
        this.resultRepository = resultRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<GroupResponse> list(AuthUser actor) {
        UUID schoolId = actor.schoolId();
        return groupRepository.findBySchoolIdAndActiveTrueOrderByDisplayOrderAsc(schoolId).stream()
                .map(this::toResponse).toList();
    }

    @Override
    @Transactional
    public GroupResponse create(CreateGroupRequest request, AuthUser actor) {
        UUID schoolId = actor.schoolId();
        ChecklistGroup group = new ChecklistGroup();
        group.setSchoolId(schoolId);
        group.setName(request.name());
        group.setDisplayOrder(request.order() == null ? nextGroupOrder(schoolId) : request.order());
        group.setCode(nextGroupCode(schoolId));
        applyItems(group, request.items());
        groupRepository.saveAndFlush(group);
        return toResponse(group);
    }

    @Override
    @Transactional
    public GroupResponse update(String code, UpdateGroupRequest request, AuthUser actor) {
        ChecklistGroup group = load(code, actor);
        if (request.name() != null) {
            group.setName(request.name());
        }
        if (request.order() != null) {
            group.setDisplayOrder(request.order());
        }
        if (request.items() != null) {
            applyItems(group, request.items());
        }
        groupRepository.saveAndFlush(group);
        return toResponse(group);
    }

    @Override
    @Transactional
    public void delete(String code, boolean confirmed, AuthUser actor) {
        ChecklistGroup group = load(code, actor);
        boolean referenced = resultRepository.existsByGroupId(group.getId());
        if (referenced && !confirmed) {
            // CF-15 — BR-210: xoá cứng bị cấm khi đã có phiên tham chiếu; chờ ?confirmed=true.
            throw new ApiException(InspError.DUPLICATE_VALUE, HttpStatus.CONFLICT,
                    "This checklist group is referenced by at least one saved inspection and cannot be hard-deleted.",
                    List.of(new ErrorResponse.Detail("id", code)));
        }
        group.setActive(false);   // BR-210 — xoá = ẩn, giữ lịch sử
        groupRepository.saveAndFlush(group);
    }

    private void applyItems(ChecklistGroup group, List<ItemRequest> items) {
        group.getItems().clear();
        if (items == null) {
            return;
        }
        int order = 0;
        for (ItemRequest ir : items) {
            ChecklistItem item = new ChecklistItem();
            item.setGroup(group);
            item.setSchoolId(group.getSchoolId());
            item.setName(ir.name());
            item.setLegalNote(ir.legalNote());
            item.setDisplayOrder(order);
            item.setCode(ir.id() != null && !ir.id().isBlank() ? ir.id()
                    : group.getCode() + "-" + (order + 1));
            order++;
            group.getItems().add(item);
        }
    }

    private ChecklistGroup load(String code, AuthUser actor) {
        return groupRepository.findBySchoolIdAndCode(actor.schoolId(), code).orElseThrow(InspExceptions::notFound);
    }

    private GroupResponse toResponse(ChecklistGroup g) {
        List<ItemResponse> items = g.getItems().stream()
                .map(i -> new ItemResponse(i.getCode(), i.getName(), i.getLegalNote())).toList();
        return new GroupResponse(g.getCode(), g.getName(), g.getDisplayOrder(), items);
    }

    private int nextGroupOrder(UUID schoolId) {
        return groupRepository.findBySchoolIdOrderByDisplayOrderAsc(schoolId).size() + 1;
    }

    /** D5 — checklist là danh mục nhỏ theo trường; mã tuần tự đơn giản {@code CLG-<n>}. */
    private String nextGroupCode(UUID schoolId) {
        AtomicInteger max = new AtomicInteger(0);
        groupRepository.findBySchoolIdOrderByDisplayOrderAsc(schoolId).forEach(g -> {
            String n = g.getCode().replaceAll("\\D", "");
            if (!n.isBlank()) {
                max.set(Math.max(max.get(), Integer.parseInt(n)));
            }
        });
        return "CLG-" + (max.get() + 1);
    }

}
