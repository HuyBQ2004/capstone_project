package com.sfms.mf05.mapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sfms.common.reference.ReferenceResolver;
import com.sfms.iam.security.AuthUser;
import com.sfms.mf05.domain.Menu;
import com.sfms.mf05.dto.MenuDtos.MenuResponse;
import com.sfms.mf05.fixture.Mf05UnitFixtures;
import com.sfms.mf05.repository.DietGroupRepository;
import com.sfms.mf05.repository.DishRepository;
import com.sfms.mf05.repository.MenuRepository;
import com.sfms.mf05.repository.MenuTemplateRepository;
import com.sfms.mf05.service.AllergyConflictChecker;
import com.sfms.mf05.service.DietCoverageValidator;
import com.sfms.mf05.service.MenuAssembler;
import com.sfms.mf05.service.MenuServiceImpl;
import com.sfms.mf10.port.NotificationPort;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * MF05-TC-024 (viết lại theo D4, ghi đè C9) — response ngày/giờ luôn ISO-8601, backend vẫn
 * PARSE khoan dung cả 2 dạng khi NHẬN qua {@code menuDateIso} (D4 chưa yêu cầu FE đổi payload
 * ở đợt này). Test dùng {@code ObjectMapper} cấu hình như Spring Boot mặc định (không có
 * {@code @JsonFormat} nào override — grep xác nhận D4 "không còn annotation format ngày").
 */
class MenuDateFormatTest {

    private static ObjectMapper springBootDefaultObjectMapper() {
        return new ObjectMapper()
                .registerModule(new JavaTimeModule())
                .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    }

    @Test
    void noJsonFormatAnnotationRemainsUnderMf05() throws IOException {
        Path mf05Root = Path.of("src/main/java/com/sfms/mf05");
        try (var stream = Files.walk(mf05Root)) {
            var offenders = stream.filter(p -> p.toString().endsWith(".java"))
                    .filter(p -> {
                        try {
                            return Files.readString(p).contains("@JsonFormat");
                        } catch (IOException e) {
                            throw new UncheckedIOException(e);
                        }
                    })
                    .toList();
            assertThat(offenders).as("D4 — không còn @JsonFormat trong com.sfms.mf05").isEmpty();
        }
    }

    @Test
    void responseSerializesInstantAndLocalDateAsIso8601() throws Exception {
        ObjectMapper mapper = springBootDefaultObjectMapper();
        MenuResponse response = new MenuResponse("MENU-2026-30", "Tuan test", "Thuc don test", "Khối 3",
                LocalDate.of(2026, 8, 5), "PENDING_APPROVAL", 2, 0, java.util.List.of(), null, null,
                Instant.parse("2026-07-28T09:00:00Z"), null, null, null, null,
                Instant.parse("2026-08-05T03:30:00Z"), null, "ALL", null, null, 300, java.util.List.of());

        String json = mapper.writeValueAsString(response);

        assertThat(json).contains("\"menuDateIso\":\"2026-08-05\"");
        assertThat(json).contains("\"submittedAt\":\"2026-07-28T09:00:00Z\"");
        assertThat(json).contains("\"approvedAt\":\"2026-08-05T03:30:00Z\"");
    }

    @Test
    void updateAcceptsBothDdMmYyyyAndIsoForMenuDateIso() {
        MenuRepository menuRepository = mock(MenuRepository.class);
        DishRepository dishRepository = mock(DishRepository.class);
        DietGroupRepository dietGroupRepository = mock(DietGroupRepository.class);
        MenuTemplateRepository menuTemplateRepository = mock(MenuTemplateRepository.class);
        MenuAssembler assembler = mock(MenuAssembler.class);
        AllergyConflictChecker allergyConflictChecker = mock(AllergyConflictChecker.class);
        DietCoverageValidator dietCoverageValidator = mock(DietCoverageValidator.class);
        ReferenceResolver referenceResolver = mock(ReferenceResolver.class);
        NotificationPort notificationPort = mock(NotificationPort.class);
        Clock clock = Clock.fixed(Instant.parse("2026-07-30T02:00:00Z"), ZoneOffset.UTC);

        when(allergyConflictChecker.check(any(Menu.class))).thenReturn(java.util.List.of());
        when(referenceResolver.approvedSpecialDietStudentCountsByGrade(any(), any())).thenReturn(Map.of());
        when(referenceResolver.gradeHeadcount(any(), any())).thenReturn(0L);

        MenuServiceImpl service = new MenuServiceImpl(menuRepository, dishRepository, dietGroupRepository,
                menuTemplateRepository, assembler, allergyConflictChecker, dietCoverageValidator, referenceResolver,
                mock(com.sfms.common.code.CodeGenerator.class), clock, springBootDefaultObjectMapper(),
                mock(org.springframework.context.ApplicationEventPublisher.class), notificationPort);

        AuthUser actor = Mf05UnitFixtures.caterer();
        Menu isoMenu = Mf05UnitFixtures.newMenu(m -> {});
        when(assembler.resolveSchoolId(actor)).thenReturn(Mf05UnitFixtures.SCHOOL_ID);
        when(assembler.loadInScope("MENU-ISO", Mf05UnitFixtures.SCHOOL_ID)).thenReturn(isoMenu);
        service.update("MENU-ISO", Map.of("menuDateIso", "2026-08-05"), actor);
        assertThat(isoMenu.getMenuDate()).isEqualTo(LocalDate.of(2026, 8, 5));

        Menu vnMenu = Mf05UnitFixtures.newMenu(m -> {});
        when(assembler.loadInScope("MENU-VN", Mf05UnitFixtures.SCHOOL_ID)).thenReturn(vnMenu);
        service.update("MENU-VN", Map.of("menuDateIso", "05/08/2026"), actor);
        assertThat(vnMenu.getMenuDate()).isEqualTo(LocalDate.of(2026, 8, 5));
    }
}
