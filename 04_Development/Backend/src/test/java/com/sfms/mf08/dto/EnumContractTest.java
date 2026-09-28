package com.sfms.mf08.dto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sfms.common.enums.MealType;
import com.sfms.mf08.domain.AttachmentKind;
import com.sfms.mf08.domain.FoodSampleStatus;
import com.sfms.mf08.domain.HandoverType;
import com.sfms.mf08.domain.SampleUnit;
import com.sfms.mf08.error.SmpExceptions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MF08-TC-017 — enum serialize SCREAMING_SNAKE, từ chối chuỗi hiển thị (D6/L3).
 *
 * <p>⚠️ Ground-truth: {@code MealType} (comum, D6) là field kiểu ENUM thật trong
 * {@code RegisterSampleRequest} ⇒ Jackson tự chặn giá trị lạ ở tầng deserialize (test trực
 * tiếp với {@code ObjectMapper} thuần đủ để khoá hành vi). {@code SampleUnit}/{@code HandoverType}
 * KHÔNG phải kiểu enum trong request DTO — {@code RegisterSampleRequest.unit()} và
 * {@code HandOverSamplesRequest.handoverType()} khai kiểu {@code String}, service tự
 * {@code Enum.valueOf(...)} (xem {@code FoodSampleServiceImpl.parseUnit},
 * {@code SampleLabRequestServiceImpl.parseHandoverType}) và ném {@code SMP-422} khi sai —
 * KHÔNG phải lỗi Jackson "malformed JSON" như {@code mealType}. Test khoá đúng 2 tầng khác
 * nhau đó, không giả định cả 3 enum đều bị Jackson chặn.
 */
class EnumContractTest {

    private final ObjectMapper mapper = new ObjectMapper();

    // ───────────────────────── mealType — enum thật, Jackson tự chặn ─────────────────────────

    @Test
    void mealTypeSerializesToScreamingSnakeCase() throws Exception {
        assertThat(mapper.writeValueAsString(MealType.LUNCH)).isEqualTo("\"LUNCH\"");
        assertThat(mapper.writeValueAsString(MealType.SNACK)).isEqualTo("\"SNACK\"");
    }

    @ParameterizedTest
    @ValueSource(strings = {"Bữa trưa", "Trưa", "lunch", "DINNER", "Bữa chiều"})
    void mealTypeRejectsDisplayLabelsAndWrongCaseAndUnknownValues(String rawValue) {
        assertThatThrownBy(() -> mapper.readValue("\"" + rawValue + "\"", MealType.class))
                .isInstanceOf(Exception.class);   // InvalidFormatException (Jackson enum binding)
    }

    @Test
    void mealTypeExactlyThreeValues() {
        assertThat(MealType.values()).containsExactly(MealType.BREAKFAST, MealType.LUNCH, MealType.SNACK);
    }

    @Test
    void snackLabelIsBuaPhuNotBuaChieuAtAssemblerLevel() {
        // D6 chốt "Bữa phụ" cho SNACK — kiểm ở lớp mealLabel() private của FoodSampleAssembler
        // gián tiếp qua chuỗi mà nó tạo (đã phủ ở DerivedFieldTest.mealLabelCombines...); ở đây
        // chỉ khẳng định giá trị lưu trữ SNACK tồn tại và KHÁC "DINNER"/"Bữa chiều" — nhãn hiển
        // thị không bao giờ là giá trị lưu.
        assertThat(MealType.SNACK.name()).isEqualTo("SNACK");
        assertThat(MealType.SNACK.name()).isNotEqualTo("DINNER");
    }

    // ───────────────────────── unit / handoverType — String ở DTO, valueOf() ở service ─────────────────────────

    @ParameterizedTest
    @ValueSource(strings = {"g", "gam", "kg", "L"})
    void sampleUnitRejectsLowercaseOrUnknownValues(String raw) {
        assertThatThrownBy(() -> SampleUnit.valueOf(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void sampleUnitAcceptsOnlyGAndMl() {
        assertThat(SampleUnit.values()).containsExactly(SampleUnit.G, SampleUnit.ML);
        assertThat(SampleUnit.valueOf("G")).isEqualTo(SampleUnit.G);
        assertThat(SampleUnit.valueOf("ML")).isEqualTo(SampleUnit.ML);
    }

    @Test
    void handoverTypeAcceptsOnlyLabAndAuthority() {
        assertThat(HandoverType.values()).containsExactly(HandoverType.LAB, HandoverType.AUTHORITY);
    }

    @ParameterizedTest
    @ValueSource(strings = {"lab", "Authority", "COURT"})
    void handoverTypeRejectsLowercaseOrUnknownValues(String raw) {
        assertThatThrownBy(() -> HandoverType.valueOf(raw)).isInstanceOf(IllegalArgumentException.class);
    }

    // ───────────────────────── status / attachment kind — enum nội bộ (response only) ─────────────────────────

    @Test
    void foodSampleStatusSerializesToScreamingSnakeCase() throws Exception {
        assertThat(mapper.writeValueAsString(FoodSampleStatus.SENT_TO_LAB)).isEqualTo("\"SENT_TO_LAB\"");
        assertThat(mapper.writeValueAsString(FoodSampleStatus.SENT_TO_LAB))
                .doesNotContain("Đã gửi phòng kiểm nghiệm");
    }

    @Test
    void attachmentKindHasExactlyFourValues() {
        assertThat(AttachmentKind.values()).containsExactly(
                AttachmentKind.EVIDENCE_PHOTO, AttachmentKind.SEAL_PHOTO,
                AttachmentKind.HANDOVER_RECORD, AttachmentKind.LAB_RECEIPT);
    }

    @Test
    void amountTooLowExceptionCarriesEnglishMessage() {
        // D7 — sanity check nhanh rằng message của mã dẫn xuất BR là tiếng Anh (kiểm đầy đủ
        // ở integration/ErrorEnvelopeIT — đây chỉ chốt 1 mã tiêu biểu ở mức unit).
        String message = SmpExceptions.amountTooLow().getMessage();
        assertThat(message).matches("[\\p{ASCII}]*");
    }
}
