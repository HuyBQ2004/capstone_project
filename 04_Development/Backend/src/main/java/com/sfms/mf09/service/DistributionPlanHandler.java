package com.sfms.mf09.service;

import com.sfms.common.code.CodeGenerator;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.common.reference.ReferenceResolver.CatererRef;
import com.sfms.common.reference.ReferenceResolver.StudentRef;
import com.sfms.mf05.event.Mf05Events;
import com.sfms.mf09.domain.DistributionStatus;
import com.sfms.mf09.domain.StudentDistribution;
import com.sfms.mf09.repository.StudentDistributionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * TDS-MF09_Backend.md §7.2 — tiêu thụ {@code MenuPublished} (MF05) để sinh phiên phát suất
 * {@code PENDING} cho từng HS ACTIVE của khối × ngày × bữa (ràng buộc C3). Bịt khoảng trống nêu
 * ở Bước 2 (2026-08-03): trước đây MF05 chưa phát event này nên {@code student_distributions}
 * chỉ có dữ liệu seed, không có đường sinh tự động.
 *
 * <p><b>Định tuyến theo audience (sửa 06/08/2026)</b> — trước đây gán CÙNG 1 menu cho MỌI HS
 * trong khối bất kể chế độ ăn, và {@code MenuPublished} còn không phát cho thực đơn riêng nên
 * HS chế độ ăn đặc biệt luôn dính nhầm vào thực đơn chung. Giờ mỗi HS được định tuyến theo:
 * <ol>
 *   <li>Không có chế độ ăn riêng đã duyệt ({@link ReferenceResolver#approvedDietGroupCodeOfStudent})
 *       → luôn dùng thực đơn chung ({@code audience="ALL"}).</li>
 *   <li>Có chế độ ăn riêng, nhưng thực đơn chung của đúng slot không vi phạm giới hạn của nhóm đó
 *       ({@link ReferenceResolver#standardMenuSatisfiesDietGroup}, VD bữa chỉ có dưa hấu — ai ăn
 *       cũng được) → cũng dùng chung thực đơn chung, KHÔNG ép phải có thực đơn riêng.</li>
 *   <li>Có chế độ ăn riêng VÀ thực đơn chung vi phạm giới hạn của nhóm → chỉ nhận đúng thực đơn
 *       riêng (audience = mã nhóm) của đúng slot. Nếu thực đơn riêng đó CHƯA publish, HS này đơn
 *       giản CHƯA CÓ bản ghi {@link StudentDistribution} nào cho slot đó (an toàn hơn gán tạm vào
 *       thực đơn chung có thể vi phạm chế độ ăn của em).</li>
 * </ol>
 * Vì (2)/(3) chỉ xác định được SAU KHI thực đơn chung của slot đã tồn tại, và thực đơn chung/thực
 * đơn riêng có thể publish theo thứ tự bất kỳ, event nào tới trước chỉ "nhận" đúng phần HS thuộc
 * về nó tại thời điểm đó; event publish sau được phép SỬA ĐÈ {@code menuId} của bản ghi đã tạo
 * trước đó (trừ khi đã {@code SERVED} — lịch sử đã phục vụ giữ nguyên).
 *
 * <p>Idempotent theo {@code UNIQUE(student_id, date, meal_type)} (ADR-M09-3) — publish lại (hoặc
 * 2 module cùng chạy race) không tạo trùng bản ghi mới, chỉ sửa đè hoặc bỏ qua.
 */
@Component
public class DistributionPlanHandler {

    private static final Logger log = LoggerFactory.getLogger(DistributionPlanHandler.class);
    private static final DateTimeFormatter CODE_DATE = DateTimeFormatter.ofPattern("yyyyMMdd");

    private final StudentDistributionRepository distributionRepository;
    private final ReferenceResolver refs;
    private final CodeGenerator codeGenerator;

    public DistributionPlanHandler(StudentDistributionRepository distributionRepository,
                                    ReferenceResolver refs, CodeGenerator codeGenerator) {
        this.distributionRepository = distributionRepository;
        this.refs = refs;
        this.codeGenerator = codeGenerator;
    }

    @EventListener
    @Transactional
    public void onMenuPublished(Mf05Events.MenuPublished event) {
        Optional<UUID> schoolId = refs.schoolIdByCode(event.schoolCode());
        if (schoolId.isEmpty()) {
            log.warn("MenuPublished bỏ qua: không resolve được schoolCode={}", event.schoolCode());
            return;
        }
        Optional<UUID> menuId = refs.menuIdByCode(schoolId.get(), event.menuCode());
        if (menuId.isEmpty()) {
            log.warn("MenuPublished bỏ qua: không resolve được menuCode={} trong school={}",
                    event.menuCode(), event.schoolCode());
            return;
        }
        List<UUID> classroomIds = refs.classroomIdsOfGrade(schoolId.get(), event.gradeId());
        if (classroomIds.isEmpty()) {
            return;
        }
        // ADR-M09-8 — tra 1 lần, snapshot cho toàn bộ phiên sinh ra từ event này.
        Optional<CatererRef> caterer = refs.currentCatererOf(schoolId.get(), event.date());
        String codeScope = "mf09_distribution:" + CODE_DATE.format(event.date());
        String codePrefix = "DSTU-" + CODE_DATE.format(event.date());

        for (UUID classroomId : classroomIds) {
            String className = refs.classroomCode(classroomId);
            for (StudentRef student : refs.studentsOfClassroom(classroomId)) {
                Optional<String> dietCode = refs.approvedDietGroupCodeOfStudent(student.id());
                boolean needsPrivateMenu = dietCode.isPresent()
                        && !refs.standardMenuSatisfiesDietGroup(schoolId.get(), event.date(), event.gradeId(),
                                event.mealType(), dietCode.get());

                boolean matchesThisEvent = "ALL".equals(event.audience())
                        ? !needsPrivateMenu
                        : needsPrivateMenu && dietCode.equals(Optional.of(event.audience()));
                if (!matchesThisEvent) {
                    continue;
                }

                Optional<StudentDistribution> existing = distributionRepository
                        .findByStudentIdAndDateAndMealType(student.id(), event.date(), event.mealType());
                if (existing.isPresent()) {
                    StudentDistribution d = existing.get();
                    // Lịch sử đã phục vụ (SERVED) là bất biến — chỉ sửa đè bản ghi còn PENDING
                    // (VD thực đơn riêng publish sau thực đơn chung, sửa lại đúng menuId).
                    if (d.getStatus() == DistributionStatus.PENDING && !menuId.get().equals(d.getMenuId())) {
                        d.setMenuId(menuId.get());
                        distributionRepository.save(d);
                    }
                    continue;
                }

                StudentDistribution d = new StudentDistribution();
                d.setCode(codeGenerator.next(codeScope, codePrefix, 3));
                d.setSchoolId(schoolId.get());
                d.setDate(event.date());
                d.setMealType(event.mealType());
                d.setStudentId(student.id());
                d.setStudentName(student.name());
                d.setClassName(className);
                d.setClassroomId(classroomId);
                d.setMenuId(menuId.get());
                caterer.ifPresent(c -> {
                    d.setCatererPartnerId(c.partnerId());
                    d.setCatererName(c.name());
                });
                d.setStatus(DistributionStatus.PENDING);
                distributionRepository.save(d);
            }
        }
    }
}
