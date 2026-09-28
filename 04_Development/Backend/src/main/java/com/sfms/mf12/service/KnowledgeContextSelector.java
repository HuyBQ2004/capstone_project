package com.sfms.mf12.service;

import org.springframework.stereotype.Component;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Chọn ĐOẠN tài liệu liên quan nhất tới câu hỏi, trong một NGÂN SÁCH ký tự.
 *
 * <p><b>Vì sao phải có.</b> Trước 09/08/2026 {@code AiGatewayImpl} nhét NGUYÊN VĂN tối đa 8 tài
 * liệu đang bật vào mỗi lượt hỏi — giới hạn theo <i>số tài liệu</i>, không theo <i>dung lượng</i>.
 * Kho tri thức thật của dự án ({@code 00_Policies/}) là <b>442KB</b>: chỉ riêng Thông tư
 * 13/2016 đã 168KB. Nạp vào rồi thì mỗi câu hỏi sẽ kéo theo hàng trăm nghìn ký tự — mô hình
 * local đang chạy {@code n_ctx = 4096} thì vỡ ngữ cảnh ngay, còn Gemini thì chậm và tốn quota
 * gấp bội cho cùng một câu trả lời. Tức là: <b>nạp đủ tài liệu vào sẽ làm HỎNG chính tính năng
 * mà tài liệu đó phục vụ</b>, nếu không có lớp chọn lọc này.
 *
 * <p><b>Cách chọn.</b> Cắt tài liệu theo đoạn văn, chấm điểm mỗi đoạn bằng số từ khoá của câu
 * hỏi xuất hiện trong đó, lấy dần từ cao xuống thấp cho tới hết ngân sách. Đây là truy hồi bằng
 * TỪ KHOÁ — thô nhưng đủ dùng và không cần hạ tầng gì thêm.
 *
 * <p><b>Chưa làm (có chủ đích):</b> truy hồi bằng vector (embedding + pgvector — thư viện đã có
 * trong {@code requirements.txt} của rag_service). Nó tốt hơn hẳn cho câu hỏi diễn đạt khác
 * chữ trong luật, nhưng cần thêm bảng embedding, một lượt gọi mô hình embedding mỗi lần nạp tài
 * liệu, và một quyết định về việc chạy embedding ở đâu. Ghi lại để không ai tưởng bản này là
 * đích cuối.
 */
@Component
public class KnowledgeContextSelector {

    /**
     * Trần ký tự cho TOÀN BỘ ngữ cảnh gửi kèm một lượt hỏi.
     *
     * <p>~6000 ký tự tiếng Việt ≈ 2000–2500 token, vừa trong {@code n_ctx = 4096} của mô hình
     * local mà vẫn chừa chỗ cho câu hỏi + câu trả lời. Gemini rộng hơn nhiều nhưng giữ chung một
     * con số cho cả hai: ngữ cảnh dài không làm câu trả lời đúng hơn, chỉ chậm và đắt hơn.
     */
    public static final int DEFAULT_BUDGET_CHARS = 6000;

    /** Đoạn ngắn hơn mức này thường là tiêu đề/số điều lẻ — ghép vào đoạn sau thay vì tính riêng. */
    private static final int MIN_CHUNK_CHARS = 200;

    /** Đoạn dài hơn mức này bị cắt nhỏ, để một điều luật dài không ăn hết ngân sách. */
    private static final int MAX_CHUNK_CHARS = 1200;

    /**
     * Từ quá ngắn hoặc quá phổ biến thì không phân biệt được đoạn nào liên quan. Không dùng danh
     * sách stopword tiếng Việt đầy đủ — chỉ cần loại những từ nối gặp trong hầu hết mọi câu.
     */
    private static final Set<String> STOPWORDS = Set.of(
            "la", "va", "cua", "cho", "trong", "voi", "khi", "nao", "gi", "the", "nhu", "duoc",
            // ⚠️ `Set.of` ném IllegalArgumentException khi có phần tử TRÙNG, và vì đây là hằng
            // static nên lỗi biến thành "Could not initialize class" — che mất nguyên nhân thật.
            // Giữ danh sách này đã sắp xếp và không trùng.
            "co", "khong", "phai", "tai", "den", "tu", "mot", "cac", "nhung", "bao", "nhieu",
            "gom", "hay", "hoac", "thi", "ma", "ve", "theo", "can", "se", "da");

    /**
     * Cắt tài liệu thành đoạn để LƯU vào {@code ai_knowledge_chunks} (V59) — dùng chung đúng một
     * cách cắt với truy hồi từ khoá, để hai đường không cho ra hai tập đoạn khác nhau.
     */
    public List<String> chunkForIndexing(String document) {
        return chunk(document);
    }

    public List<String> select(String prompt, List<String> documents) {
        return select(prompt, documents, DEFAULT_BUDGET_CHARS);
    }

    public List<String> select(String prompt, List<String> documents, int budgetChars) {
        if (documents == null || documents.isEmpty() || budgetChars <= 0) {
            return List.of();
        }
        Set<String> keywords = keywordsOf(prompt);

        List<String> allChunks = new ArrayList<>();
        for (String doc : documents) {
            allChunks.addAll(chunk(doc));
        }
        List<String> flatChunks = allChunks.stream().map(c -> deaccent(c.toLowerCase())).toList();

        /*
         * TRỌNG SỐ THEO ĐỘ HIẾM (IDF). Đếm số từ khớp thì hỏng ngay trên kho tri thức thật: đo
         * 09/08/2026, câu "Kiểm thực ba bước gồm những bước nào?" lấy về đoạn nói về THU HỒI SẢN
         * PHẨM và GHI NHÃN, vì "kiem"/"thuc" có mặt trong gần như mọi đoạn của mọi văn bản luật
         * nên lấn át "buoc" — từ thật sự phân biệt. Từ càng hiếm càng nặng điểm.
         */
        java.util.Map<String, Double> weight = new java.util.HashMap<>();
        for (String k : keywords) {
            long df = flatChunks.stream().filter(c -> c.contains(k)).count();
            // +1 để không chia 0; từ có mặt ở mọi đoạn ⇒ trọng số ~0.
            weight.put(k, Math.log((flatChunks.size() + 1.0) / (df + 1.0)));
        }
        // Cụm từ liền nhau là tín hiệu mạnh hơn hẳn các từ rời: "ba buoc" khác hẳn "ba" + "buoc".
        List<String> phrases = phrasesOf(prompt);

        List<Scored> chunks = new ArrayList<>();
        for (int i = 0; i < allChunks.size(); i++) {
            chunks.add(new Scored(allChunks.get(i), score(flatChunks.get(i), weight, phrases)));
        }
        // Câu hỏi không khớp từ nào (vd. hỏi bằng từ khác hẳn trong luật): vẫn gửi phần đầu kho
        // tri thức thay vì gửi rỗng — rỗng thì trợ lý từ chối trả lời, đúng lỗi đã đo được.
        chunks.sort(Comparator.comparingDouble((Scored s) -> s.score).reversed());

        List<String> picked = new ArrayList<>();
        int used = 0;
        for (Scored s : chunks) {
            if (used + s.text.length() > budgetChars) {
                continue;
            }
            picked.add(s.text);
            used += s.text.length();
            if (used >= budgetChars) {
                break;
            }
        }
        return picked;
    }

    private double score(String flatChunk, java.util.Map<String, Double> weight, List<String> phrases) {
        double s = 0;
        for (var e : weight.entrySet()) {
            if (flatChunk.contains(e.getKey())) {
                s += e.getValue();
            }
        }
        // Thưởng đậm khi khớp nguyên cụm — đủ để đoạn đúng vượt lên trên đám đoạn chỉ khớp lẻ tẻ.
        for (String p : phrases) {
            if (flatChunk.contains(p)) {
                s += 5.0;
            }
        }
        return s;
    }

    /** Các cụm 2–3 từ liền nhau trong câu hỏi (đã bỏ dấu, đã loại từ nối). */
    private List<String> phrasesOf(String prompt) {
        List<String> words = new ArrayList<>();
        for (String raw : deaccent(prompt == null ? "" : prompt.toLowerCase()).split("[^\\p{Alnum}]+")) {
            if (!raw.isBlank() && !STOPWORDS.contains(raw)) {
                words.add(raw);
            }
        }
        List<String> out = new ArrayList<>();
        for (int i = 0; i + 1 < words.size(); i++) {
            out.add(words.get(i) + " " + words.get(i + 1));
            if (i + 2 < words.size()) {
                out.add(words.get(i) + " " + words.get(i + 1) + " " + words.get(i + 2));
            }
        }
        return out;
    }

    private Set<String> keywordsOf(String prompt) {
        if (prompt == null || prompt.isBlank()) {
            return Set.of();
        }
        Set<String> out = new HashSet<>();
        for (String raw : deaccent(prompt.toLowerCase()).split("[^\\p{Alnum}]+")) {
            if (raw.length() >= 3 && !STOPWORDS.contains(raw)) {
                out.add(raw);
            }
        }
        return out;
    }

    /**
     * Bỏ dấu để so khớp. Người dùng gõ "luu mau" hay "lưu mẫu" đều phải khớp cùng một điều luật —
     * gõ thiếu dấu là chuyện thường trên điện thoại.
     */
    private String deaccent(String s) {
        return Normalizer.normalize(s, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd');
    }

    private List<String> chunk(String doc) {
        if (doc == null || doc.isBlank()) {
            return List.of();
        }
        List<String> out = new ArrayList<>();
        StringBuilder buf = new StringBuilder();
        for (String para : doc.split("\n\\s*\n")) {
            String p = para.strip();
            if (p.isEmpty()) {
                continue;
            }
            if (buf.length() + p.length() + 1 <= MAX_CHUNK_CHARS) {
                if (buf.length() > 0) {
                    buf.append('\n');
                }
                buf.append(p);
            } else {
                if (buf.length() >= MIN_CHUNK_CHARS) {
                    out.add(buf.toString());
                    buf.setLength(0);
                }
                // Đoạn đơn lẻ dài hơn trần: cắt cứng theo độ dài, thà mất mạch câu còn hơn để
                // một điều luật dài chiếm trọn ngân sách rồi loại hết đoạn liên quan khác.
                if (p.length() > MAX_CHUNK_CHARS) {
                    for (int i = 0; i < p.length(); i += MAX_CHUNK_CHARS) {
                        out.add(p.substring(i, Math.min(p.length(), i + MAX_CHUNK_CHARS)));
                    }
                } else {
                    buf.append(p);
                }
            }
        }
        if (buf.length() > 0) {
            out.add(buf.toString());
        }
        return out.isEmpty() ? List.of(doc.substring(0, Math.min(doc.length(), MAX_CHUNK_CHARS))) : out;
    }

    private record Scored(String text, double score) {
    }

    /** Chỉ để test đọc được hằng số mà không phải đoán. */
    public static List<Integer> limits() {
        return Arrays.asList(DEFAULT_BUDGET_CHARS, MIN_CHUNK_CHARS, MAX_CHUNK_CHARS);
    }
}
