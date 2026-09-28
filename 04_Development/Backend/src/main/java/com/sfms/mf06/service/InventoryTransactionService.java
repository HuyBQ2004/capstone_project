package com.sfms.mf06.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.dto.PagedResponse;
import com.sfms.common.enums.MealType;
import com.sfms.common.query.ListQuery;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf06.domain.InventoryBatch;
import com.sfms.mf06.domain.InventoryTransaction;
import com.sfms.mf06.domain.TransactionType;
import com.sfms.mf06.dto.InventoryDtos.InventoryTransactionResponse;
import com.sfms.mf06.dto.InventoryDtos.ManualAdjustmentRequest;
import com.sfms.mf06.error.IngExceptions;
import com.sfms.mf06.repository.InventoryBatchRepository;
import com.sfms.mf06.repository.InventoryTransactionRepository;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** UC-064 — sổ giao dịch kho APPEND-ONLY (ADR-M06-1) + điều chỉnh thủ công (BR-204). */
@Service
public class InventoryTransactionService {

    private final InventoryTransactionRepository transactionRepository;
    private final InventoryBatchRepository batchRepository;
    private final ReferenceResolver referenceResolver;
    private final CodeGenerator codeGenerator;
    private final Clock clock;

    public InventoryTransactionService(InventoryTransactionRepository transactionRepository,
                                        InventoryBatchRepository batchRepository,
                                        ReferenceResolver referenceResolver, CodeGenerator codeGenerator,
                                        Clock clock) {
        this.transactionRepository = transactionRepository;
        this.batchRepository = batchRepository;
        this.referenceResolver = referenceResolver;
        this.codeGenerator = codeGenerator;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public PagedResponse<InventoryTransactionResponse> list(ListQuery query, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        Specification<InventoryTransaction> spec = (root, cq, cb) -> {
            List<Predicate> predicates = new ArrayList<>();
            if (schoolId != null) {
                predicates.add(cb.equal(root.get("schoolId"), schoolId));
            }
            return cb.and(predicates.toArray(new Predicate[0]));
        };
        Page<InventoryTransaction> page = transactionRepository.findAll(spec, query.toPageable());
        List<InventoryTransactionResponse> items = page.getContent().stream().map(this::toResponse).toList();
        return PagedResponse.of(page, items, query.page(), query.pageSize());
    }

    private InventoryTransactionResponse toResponse(InventoryTransaction t) {
        var user = referenceResolver.userById(t.getPerformedByUserId());
        return new InventoryTransactionResponse(
                t.getCode(), t.getOccurredAt(), t.getType().name(), batchCodeOf(t.getBatchId()),
                referenceResolver.ingredientCode(t.getIngredientId()), referenceResolver.ingredientName(t.getIngredientId()),
                t.getDelta(), t.getUnit(), t.getReasonCode() == null ? null : t.getReasonCode().name(),
                user == null ? null : user[2], t.getManagerSignature(),
                t.getIssueSlipId() == null ? null : t.getIssueSlipId().toString(),
                t.getMenuId() == null ? null : referenceResolver.menuCode(t.getMenuId()),
                t.getDishId() == null ? null : referenceResolver.dishCode(t.getDishId()), t.getDishLabel(),
                t.getMealType() == null ? null : t.getMealType().name(), t.getServiceDate());
    }

    private String batchCodeOf(UUID batchId) {
        return batchRepository.findById(batchId).map(InventoryBatch::getCode).orElse(null);
    }

    /** BR-204 — mã lý do + chữ ký quản lý bắt buộc (đã enforce ở DTO @NotNull/@NotBlank + CHECK DB). */
    @Transactional
    public InventoryTransactionResponse adjust(ManualAdjustmentRequest request, AuthUser actor) {
        UUID schoolId = Mf06Scope.requireSchoolId(actor, referenceResolver);
        if (schoolId == null) {
            throw IngExceptions.accessDenied();
        }
        InventoryBatch batch = batchRepository.findBySchoolIdAndCode(schoolId, request.batchId())
                .orElseThrow(IngExceptions::notFound);
        LocalDate today = LocalDate.now(clock);
        if (batch.getQuarantine() != null) {
            throw IngExceptions.batchQuarantined();
        }
        if (batch.shelfStatus(today).name().equals("EXPIRED")) {
            throw IngExceptions.batchExpiredLocked();
        }
        // PROPOSED — xem plan "Đơn vị đo lường": đơn vị điều chỉnh PHẢI khớp đơn vị của
        // chính lô đang trừ/cộng — trước đây ghi thẳng request.unit() vào sổ cái mà không
        // so với batch.getUnit(), có thể lệch tồn kho âm thầm (VD gõ nhầm "g" cho lô "kg").
        if (!request.unit().equals(batch.getUnit())) {
            throw IngExceptions.validationFailed("unit",
                    "phải khớp đơn vị của lô (" + batch.getUnit() + "), nhận được: " + request.unit());
        }

        InventoryTransaction tx = new InventoryTransaction();
        tx.setSchoolId(schoolId);
        tx.setOccurredAt(clock.instant());
        tx.setType(TransactionType.ADJUSTMENT);
        tx.setBatchId(batch.getId());
        tx.setIngredientId(batch.getIngredientId());
        tx.setDelta(request.delta());
        tx.setUnit(request.unit());
        tx.setPerformedByUserId(actor.userId());
        tx.setReasonCode(request.reasonCode());
        tx.setManagerSignature(request.managerSignature());
        if (request.mealType() != null) {
            try {
                tx.setMealType(MealType.valueOf(request.mealType()));
            } catch (IllegalArgumentException ex) {
                throw IngExceptions.validationFailed("mealType", "unknown meal type");
            }
        }
        tx.setServiceDate(request.serviceDate());
        tx.setCode(codeGenerator.next("mf06_txn:" + today, "TXN-" + today.toString().replace("-", ""), 2));
        transactionRepository.saveAndFlush(tx);
        return toResponse(tx);
    }
}
