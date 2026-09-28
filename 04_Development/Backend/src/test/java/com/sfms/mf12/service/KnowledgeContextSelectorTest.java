package com.sfms.mf12.service;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Chọn đoạn tài liệu cho ngữ cảnh AI — {@link KnowledgeContextSelector}.
 *
 * <p><b>Vì sao bộ test này tồn tại.</b> Trước 09/08/2026 luồng hỏi–đáp nhét NGUYÊN VĂN tối đa 8
 * tài liệu đang bật vào mỗi lượt gọi (giới hạn theo <i>số tài liệu</i>, không theo dung lượng).
 * Kho tri thức thật của dự án ({@code 00_Policies/}) nặng <b>442KB</b> — riêng Thông tư 13/2016
 * đã 168KB. Nạp đủ tài liệu vào là làm hỏng chính tính năng mà tài liệu đó phục vụ: mô hình
 * local chạy {@code n_ctx = 4096} sẽ vỡ ngữ cảnh.
 *
 * <p>Ba điều bị khoá ở đây, vì cả ba đều là chỗ dễ vô tình phá khi tối ưu sau này.
 */
class KnowledgeContextSelectorTest {

    private final KnowledgeContextSelector selector = new KnowledgeContextSelector();

    private static String para(String head, int repeat) {
        return (head + " ").repeat(repeat);
    }

    @Test
    @DisplayName("Không bao giờ vượt ngân sách ký tự — dù kho tri thức lớn hơn nhiều lần")
    void neverExceedsBudget() {
        String huge = String.join("\n\n", java.util.Collections.nCopies(400, para("noi dung luat an toan thuc pham", 30)));

        List<String> picked = selector.select("luu mau thuc pham bao nhieu gio", List.of(huge), 6000);

        int total = picked.stream().mapToInt(String::length).sum();
        assertThat(total).isLessThanOrEqualTo(6000);
        assertThat(picked).isNotEmpty();
    }

    @Test
    @DisplayName("Đoạn chứa từ khoá của câu hỏi được chọn trước đoạn không liên quan")
    void relevantChunkWinsOverNoise() {
        String noise = para("quy dinh ve bien ban hop hoi dong truong", 40);
        String relevant = "Dieu 8.2: mau thuc pham phai luu it nhat 24 gio o nhiet do tu 2 den 8 do C. "
                + para("chi tiet ve luu mau", 20);

        List<String> picked = selector.select("Mẫu thực phẩm lưu tối thiểu bao nhiêu giờ?",
                List.of(noise, relevant), 1500);

        assertThat(picked).isNotEmpty();
        assertThat(picked.get(0)).contains("luu it nhat 24 gio");
    }

    @Test
    @DisplayName("Gõ THIẾU DẤU vẫn khớp — người dùng gõ trên điện thoại thường bỏ dấu")
    void matchesWithoutDiacritics() {
        String doc = "Điều 8.2: mẫu thực phẩm phải lưu ít nhất 24 giờ. " + para("noi dung khac", 30);

        List<String> withTones = selector.select("lưu mẫu thực phẩm", List.of(doc), 2000);
        List<String> withoutTones = selector.select("luu mau thuc pham", List.of(doc), 2000);

        assertThat(withoutTones).isEqualTo(withTones);
        assertThat(withoutTones).isNotEmpty();
    }

    @Test
    @DisplayName("Câu hỏi không khớp từ nào vẫn trả về ngữ cảnh, KHÔNG trả rỗng")
    void neverReturnsEmptyForUnmatchedQuestion() {
        // Rỗng thì trợ lý trả lời "chưa có thông tin trong tài liệu tham chiếu" — đúng lỗi đã đo
        // được khi kho tri thức trống. Thà gửi đoạn đầu còn hơn im lặng bỏ trắng ngữ cảnh.
        String doc = "Điều 8.2: mẫu thực phẩm phải lưu ít nhất 24 giờ.\n\n" + para("noi dung khac", 30);

        List<String> picked = selector.select("zzz qqq xyz", List.of(doc), 2000);

        assertThat(picked).isNotEmpty();
    }

    @Test
    @DisplayName("Từ PHỔ BIẾN không lấn át từ phân biệt — ca thật đã đo được là sai")
    void commonWordsDoNotDrownOutTheRareOne() {
        /*
         * Đo 09/08/2026 trên kho tri thức thật (00_Policies, 333k ký tự): câu "Kiểm thực ba bước
         * gồm những bước nào?" lấy về đoạn nói về THU HỒI SẢN PHẨM và GHI NHÃN. Lý do:
         * "kiem"/"thuc" có mặt ở gần như mọi đoạn của mọi văn bản luật nên đếm-số-từ-khớp cho
         * chúng cùng trọng số với "buoc" — từ thật sự phân biệt. Bộ test này khoá lại cách chấm
         * điểm theo độ hiếm + thưởng cụm từ.
         */
        List<String> noise = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            noise.add("Quy dinh ve kiem tra nha nuoc doi voi thuc pham xuat khau va ghi nhan thuc pham. "
                    + para("noi dung kiem tra thuc pham", 10));
        }
        String answer = "Kiem thuc ba buoc: buoc 1 kiem tra truoc khi che bien, buoc 2 kiem tra trong "
                + "qua trinh che bien, buoc 3 kiem tra truoc khi an.";
        List<String> docs = new java.util.ArrayList<>(noise);
        docs.add(answer);

        List<String> picked = selector.select("Kiểm thực ba bước gồm những bước nào?", docs, 2000);

        assertThat(picked.get(0)).contains("Kiem thuc ba buoc");
    }

    @Test
    @DisplayName("Kho tri thức rỗng ⇒ trả rỗng, không ném lỗi")
    void emptyKnowledgeIsSafe() {
        assertThat(selector.select("bat ky", List.of())).isEmpty();
        assertThat(selector.select("bat ky", null)).isEmpty();
    }
}
