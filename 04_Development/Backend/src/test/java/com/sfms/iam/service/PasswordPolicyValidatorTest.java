package com.sfms.iam.service;

import com.sfms.common.error.ApiException;
import com.sfms.iam.domain.SecurityPolicy;
import com.sfms.iam.fixture.ExpectedMessages;
import com.sfms.iam.fixture.UserFixture;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.http.HttpStatus;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * MF01-TC-002 — chính sách độ mạnh MK đọc từ {@code SecurityPolicy} (không
 * hardcode).
 *
 * <p>
 * <b>Severity:</b> HIGH
 * <p>
 * <b>Oracle:</b> BR-006 · MSG-104 nguyên văn · TDS §10 {@code IAM-006policy}
 * HTTP 422
 * · TDS §17 <b>C3</b> (đọc tham số từ {@code SecurityPolicy}, không hardcode)
 */
class PasswordPolicyValidatorTest {

    private final PasswordPolicyValidator validator = new PasswordPolicyValidator();

    @Test
    @DisplayName("(a) P@ssw0rd123 — hợp lệ, không ném")
    void validPasswordPasses() {
        assertThatCode(() -> validator.validate("P@ssw0rd123", UserFixture.policyDefault()))
                .doesNotThrowAnyException();
    }

    /** Equivalence Partitioning — mỗi dòng vi phạm đúng một tiêu chí của BR-006. */
    @ParameterizedTest(name = "({0}) {1} — {2}")
    @CsvSource({
            "b, password,   thiếu hoa/số/đặc biệt",
            "c, P@ss1,      dài 5 < 8",
            "d, P@SSWORD1,  thiếu chữ thường",
            "e, Password1,  thiếu ký tự đặc biệt",
            "f, P@ssword,   thiếu số"
    })
    @DisplayName("(b–f) Vi phạm chính sách ⇒ IAM-006policy (422) + MSG-104 nguyên văn")
    void invalidPasswordsRejected(String label, String password, String violation) {
        assertThatThrownBy(() -> validator.validate(password, UserFixture.policyDefault()))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> {
                    ApiException api = (ApiException) ex;
                    assertThat(api.getCode()).isEqualTo("IAM-006policy");
                    assertThat(api.getStatus()).isEqualTo(HttpStatus.UNPROCESSABLE_CONTENT);
                    // D7 — nguyên văn, dùng isEqualTo chứ không contains
                    assertThat(api.getMessage()).isEqualTo(ExpectedMessages.MSG_104);
                    // §2 L1 — MSG-063 là Warning, không bao giờ là expected của TC chặn
                    assertThat(api.getMessage())
                            .isNotEqualTo(ExpectedMessages.MSG_063_NEVER_EXPECTED);
                });
    }

    /**
     * Bước 4 của TestSpec — <b>bằng chứng giá trị đến từ DB</b>.
     *
     * <p>
     * Cùng một mật khẩu 11 ký tự: hợp lệ với {@code passwordMinLength = 8}, bị từ
     * chối với
     * {@code = 12}. Nếu độ dài bị hardcode thì nhánh thứ hai vẫn qua ⇒ vi phạm C3.
     */
    @Test
    @DisplayName("Bước 4 — đổi passwordMinLength=12 ⇒ 'P@ssw0rd123' (11 ký tự) bị từ chối")
    void minLengthComesFromPolicyNotFromCode() {
        SecurityPolicy stricter = UserFixture.policyDefault();
        stricter.setPasswordMinLength(12);

        assertThatCode(() -> validator.validate("P@ssw0rd123", UserFixture.policyDefault()))
                .doesNotThrowAnyException();

        assertThatThrownBy(() -> validator.validate("P@ssw0rd123", stricter))
                .isInstanceOf(ApiException.class)
                .satisfies(ex -> assertThat(((ApiException) ex).getCode()).isEqualTo("IAM-006policy"));
    }

    @Test
    @DisplayName("Tắt từng cờ policy ⇒ tiêu chí tương ứng không còn bị ép")
    void individualFlagsAreHonoured() {
        SecurityPolicy noSpecial = UserFixture.policyDefault();
        noSpecial.setRequireSpecialChar(false);
        assertThatCode(() -> validator.validate("Password1", noSpecial)).doesNotThrowAnyException();

        SecurityPolicy noNumber = UserFixture.policyDefault();
        noNumber.setRequireNumber(false);
        assertThatCode(() -> validator.validate("P@ssword", noNumber)).doesNotThrowAnyException();

        SecurityPolicy noMixedCase = UserFixture.policyDefault();
        noMixedCase.setRequireMixedCase(false);
        assertThatCode(() -> validator.validate("p@ssw0rd", noMixedCase)).doesNotThrowAnyException();
    }
}
