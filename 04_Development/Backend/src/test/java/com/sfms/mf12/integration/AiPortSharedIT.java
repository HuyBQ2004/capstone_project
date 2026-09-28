package com.sfms.mf12.integration;

import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.regex.Pattern;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * MF12-TC-INT-002 — {@code AiAssistPort} dùng chung: MF05 KHÔNG import SDK/Gemini trực tiếp
 * (ADR-M05-03 + ADR-M12-01, cổng AI duy nhất). Vế "MF05 gọi qua port sinh {@code ai_requests}
 * {@code module='MF05_MENU'}" đã phủ đủ ở {@code mf05.web.MenuAiSuggestIT} (cùng nội dung
 * MF12-TC-INT-022) — không lặp lại ở đây, chỉ còn vế static-check tĩnh.
 */
class AiPortSharedIT extends AbstractIamIntegrationTest {

    private static final Pattern FORBIDDEN =
            Pattern.compile("generativelanguage|GeminiClient|GEMINI_API_KEY", Pattern.CASE_INSENSITIVE);

    @Test
    void mf05DoesNotImportGeminiDirectly() throws IOException {
        Path mf05Root = Path.of("src/main/java/com/sfms/mf05");
        assertThat(Files.exists(mf05Root)).isTrue();

        try (var stream = Files.walk(mf05Root)) {
            stream.filter(p -> p.toString().endsWith(".java")).forEach(this::assertNoGeminiReference);
        }
    }

    private void assertNoGeminiReference(Path file) {
        try {
            String content = Files.readString(file);
            assertThat(FORBIDDEN.matcher(content).find())
                    .as(file + " KHÔNG được tham chiếu Gemini trực tiếp (ADR-M05-03/ADR-M12-01)")
                    .isFalse();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
