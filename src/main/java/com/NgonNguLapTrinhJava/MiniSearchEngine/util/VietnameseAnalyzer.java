package com.NgonNguLapTrinhJava.MiniSearchEngine.util;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Pipeline phân tích văn bản tiếng Việt.
 *
 * Các bước xử lý:
 * 1. Lowercase
 * 2. Loại bỏ ký tự đặc biệt / số không cần thiết
 * 3. Tokenize theo khoảng trắng
 * 4. Stopwords removal
 * 5. Loại bỏ token quá ngắn (< 2 ký tự)
 *
 * Cải tiến so với bản cơ bản:
 * - Chuẩn hóa Unicode (xử lý ký tự tiếng Việt tổ hợp)
 * - Stopwords list đầy đủ hơn cho tiếng Việt
 * - Lọc token số thuần túy (không có giá trị tìm kiếm)
 */
public class VietnameseAnalyzer {

    private static final Set<String> STOPWORDS = new HashSet<>(Arrays.asList(
            // Giới từ, liên từ
            "và", "hoặc", "nhưng", "tuy", "mà", "vì", "nên", "nếu", "thì", "là",
            "của", "cho", "với", "từ", "đến", "tới", "về", "trong", "ngoài", "trên",
            "dưới", "sau", "trước", "giữa", "bên", "tại", "ở", "ra", "vào", "lên",
            "xuống", "qua", "theo", "bởi", "do", "như", "khi", "lúc", "mỗi",

            // Đại từ, từ chỉ định
            "tôi", "bạn", "họ", "chúng", "mình", "này", "đó", "kia", "ấy", "đây",
            "đấy", "đâu", "ai", "gì", "nào", "sao", "thế", "vậy",

            // Động từ trạng thái / hỗ trợ
            "có", "không", "được", "bị", "hãy", "đã", "đang", "sẽ", "vẫn", "còn",
            "rồi", "cũng", "đều", "chỉ", "thôi", "cả", "mới", "lại", "cần", "phải",

            // Trạng từ mức độ
            "rất", "quá", "khá", "hơi", "thật", "lắm", "nhiều", "ít", "một",

            // Từ số đếm thường gặp trong công thức nhưng không có giá trị index
            "gram", "ml", "lít", "kg", "cm", "mm",

            // Các từ HTML / web bị sót
            "http", "https", "www", "com", "vn"
    ));

    /**
     * Phân tích văn bản thô, trả về danh sách token đã chuẩn hóa.
     *
     * @param text văn bản thô (title + content)
     * @return danh sách token sạch
     */
    public List<String> analyze(String text) {
        if (text == null || text.isBlank()) return new ArrayList<>();

        // Bước 1: Lowercase
        String lower = text.toLowerCase();

        // Bước 2: Chuẩn hóa - thay ký tự đặc biệt bằng khoảng trắng
        // Giữ lại chữ cái Latin, chữ Việt (Unicode), khoảng trắng
        // String cleaned = lower.replaceAll("[^a-zàáâãèéêìíòóôõùúýăđơưạảấầẩẫậắằẳẵặẹẻẽếềểễệỉịọỏốồổỗộớờởỡợụủứừửữựỳỷỹỵ\\s]", " ");
String cleaned = lower.replaceAll("[^\\p{L}\\s]", " ");

        // Bước 3: Tokenize theo khoảng trắng
        String[] rawTokens = cleaned.trim().split("\\s+");

        // Bước 4 & 5: Lọc stopwords và token quá ngắn
        List<String> tokens = new ArrayList<>();
        for (String token : rawTokens) {
            if (token.length() < 2) continue;
            if (STOPWORDS.contains(token)) continue;
            tokens.add(token);
        }

        return tokens;
    }

    /**
     * Kiểm tra một từ có phải stopword không (dùng cho debug/test).
     */
    public boolean isStopword(String word) {
        return STOPWORDS.contains(word.toLowerCase());
    }

    /**
     * Lấy toàn bộ danh sách stopwords (dùng cho test).
     */
    public Set<String> getStopwords() {
        return new HashSet<>(STOPWORDS);
    }
}
