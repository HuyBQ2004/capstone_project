package com.sfms.mf12.service;

import com.sfms.mf12.error.AisExceptions;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.extractor.XWPFWordExtractor;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

/**
 * Bóc VĂN BẢN THUẦN ra khỏi file tài liệu để nạp vào kho tri thức AI.
 *
 * <p><b>Vì sao có.</b> Trước 09/08/2026 màn `system-admin/ai-knowledge` chỉ có ô Textarea: muốn
 * nạp Nghị định 15/2018 hay QĐ 1246/QĐ-BYT thì phải tự mở file, bôi đen, dán tay. Hệ quả đo
 * được: kho tri thức RỖNG, và khi chạy mô hình local nhỏ thì trợ lý <b>từ chối trả lời</b> câu
 * nghiệp vụ ("chưa có thông tin trong tài liệu tham chiếu") — vì `SYSTEM_PROMPT` buộc chỉ dựa
 * trên tài liệu tham chiếu. Kho tri thức trống làm hỏng chính tính năng nó phục vụ.
 *
 * <p><b>Không thêm thư viện nào:</b> PDFBox và POI đã có sẵn trong `pom.xml` từ MF08 (xuất sổ
 * Mẫu số 5). Đây là dùng lại chiều ngược của cùng hai thư viện đó.
 *
 * <p><b>Cố ý KHÔNG lưu file gốc.</b> Kho tri thức cần VĂN BẢN để nhét vào ngữ cảnh RAG, không
 * cần bản scan. Lưu thêm file chỉ đẻ ra một bản sao phải đồng bộ và một chỗ nữa để rò rỉ.
 */
@Component
public class KnowledgeTextExtractor {

    /** Trần 10MB — cùng mức với tài liệu tuân thủ MF04, và dưới `client_max_body_size 15m` của nginx. */
    public static final long MAX_BYTES = 10 * 1024 * 1024L;

    /**
     * Ký tự tối thiểu để coi là bóc được. PDF chỉ chứa ảnh scan sẽ bóc ra gần như rỗng — phải
     * BÁO RÕ thay vì tạo một tài liệu tri thức trống rồi để người dùng tưởng đã nạp xong.
     */
    private static final int MIN_USEFUL_CHARS = 20;

    public String extract(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw AisExceptions.unsupportedKnowledgeFile("file rỗng");
        }
        if (file.getSize() > MAX_BYTES) {
            throw AisExceptions.knowledgeFileTooLarge();
        }

        String name = file.getOriginalFilename() == null ? "" : file.getOriginalFilename().toLowerCase();
        String text;
        try (InputStream in = file.getInputStream()) {
            if (name.endsWith(".pdf")) {
                text = fromPdf(file);
            } else if (name.endsWith(".docx")) {
                text = fromDocx(in);
            } else if (name.endsWith(".txt") || name.endsWith(".md") || name.endsWith(".csv")) {
                text = new String(in.readAllBytes(), StandardCharsets.UTF_8);
            } else {
                // .doc (định dạng cũ, nhị phân) KHÔNG hỗ trợ: POI đọc được nhưng cần thêm
                // artifact `poi-scratchpad`. Nói thẳng thay vì im lặng bóc ra rác.
                throw AisExceptions.unsupportedKnowledgeFile("chỉ nhận .pdf, .docx, .txt, .md, .csv");
            }
        } catch (java.io.IOException e) {
            throw AisExceptions.unsupportedKnowledgeFile("không đọc được nội dung file");
        }

        text = normalize(text);
        if (text.length() < MIN_USEFUL_CHARS) {
            throw AisExceptions.unsupportedKnowledgeFile(
                    "không bóc được văn bản — file có thể là bản scan (ảnh) chưa qua OCR");
        }
        return text;
    }

    private String fromPdf(MultipartFile file) throws java.io.IOException {
        try (PDDocument doc = Loader.loadPDF(file.getBytes())) {
            return new PDFTextStripper().getText(doc);
        }
    }

    private String fromDocx(InputStream in) throws java.io.IOException {
        try (XWPFDocument doc = new XWPFDocument(in); XWPFWordExtractor ex = new XWPFWordExtractor(doc)) {
            return ex.getText();
        }
    }

    /**
     * Gom khoảng trắng thừa. PDF hay xuống dòng giữa câu theo bề rộng trang; giữ nguyên thì
     * tốn token ngữ cảnh mà không thêm nghĩa nào.
     */
    private String normalize(String raw) {
        return raw == null ? "" : raw.replace("\r", "")
                .replaceAll("[ \t]+", " ")
                .replaceAll("\n{3,}", "\n\n")
                .trim();
    }
}
