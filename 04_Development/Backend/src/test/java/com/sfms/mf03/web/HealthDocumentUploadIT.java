package com.sfms.mf03.web;

import com.fasterxml.jackson.databind.JsonNode;
import com.sfms.iam.support.AbstractIamIntegrationTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MvcResult;

import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Quyết định nhóm 04/08/2026 — {@code POST /students/{code}/health-documents}: evidence upload
 * còn thiếu cho BR-031 (SEVERE bắt buộc {@code proofFileId} là UUID tài liệu có thật). U-9905
 * (PARENT) liên kết HS-3A-001 (seed V5, xem {@code Mf03SecurityIT}).
 */
class HealthDocumentUploadIT extends AbstractIamIntegrationTest {

    @Test
    @DisplayName("Parent uploads PDF evidence for own linked student -> 201, id dùng thẳng làm proofFileId")
    void parentUploadsEvidenceForOwnStudent() throws Exception {
        byte[] pdfBytes = ("%PDF-1.4\n" + "fake but valid header").getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "allergy-proof.pdf", "application/pdf", pdfBytes);

        MvcResult result = mvc.perform(multipart("/api/v1/students/HS-3A-001/health-documents")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9905")))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode body = parse(result);
        assertThat(body.get("id").asText()).isNotBlank();
        assertThat(body.get("fileName").asText()).isEqualTo("allergy-proof.pdf");
        assertThat(body.get("contentType").asText()).isEqualTo("application/pdf");
        assertThat(body.get("sizeBytes").asLong()).isEqualTo(pdfBytes.length);

        // proofFileId dùng được thẳng ở khai báo dị ứng mức SEVERE (BR-031) — round-trip thật,
        // không chỉ kiểm response của endpoint upload.
        String documentId = body.get("id").asText();
        mvc.perform(post("/api/v1/allergy-declarations")
                        .header("Authorization", authHeaderFor("U-9905"))
                        .contentType(org.springframework.http.MediaType.APPLICATION_JSON)
                        .content(toJson(Map.of(
                                "studentId", "HS-3A-001",
                                "category", "Thực phẩm",
                                "allergen", "Đậu phộng (synthetic, HealthDocumentUploadIT)",
                                "severity", "SEVERE",
                                "proofFileId", documentId))))
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("Parent uploads file disguised as PDF (wrong magic bytes) -> 422 STU-009 MSG-009")
    void rejectsFileWithFakeMagicBytes() throws Exception {
        byte[] notActuallyPdf = "not a real pdf".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "fake.pdf", "application/pdf", notActuallyPdf);

        MvcResult result = mvc.perform(multipart("/api/v1/students/HS-3A-001/health-documents")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9905")))
                .andReturn();

        assertThat(result.getResponse().getStatus()).isEqualTo(422);
        JsonNode body = parse(result);
        assertThat(body.get("error").get("code").asText()).isEqualTo("STU-009");
        assertThat(body.get("error").get("message").asText())
                .isEqualTo("File upload failed: Unsupported file type. Only PDF, JPG, and PNG are allowed.");
    }

    @Test
    @DisplayName("Parent linked to a DIFFERENT student -> 403 STU-005 (C2 scope enforcement)")
    void rejectsUploadForUnlinkedStudent() throws Exception {
        byte[] pdfBytes = "%PDF-1.4\nx".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("file", "proof.pdf", "application/pdf", pdfBytes);

        // U-9908 (PARENT, seed V5) chỉ liên kết HS-3A-002 — không liên kết HS-3A-001.
        mvc.perform(multipart("/api/v1/students/HS-3A-001/health-documents")
                        .file(file)
                        .header("Authorization", authHeaderFor("U-9908")))
                .andExpect(status().isForbidden());
    }
}
