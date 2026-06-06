package com.NgonNguLapTrinhJava.MiniSearchEngine.util;

import vn.pipeline.VnCoreNLP;
import vn.pipeline.Annotation;
import vn.corenlp.tokenizer.Tokenizer;

import java.util.*;

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
            // Bỏ "có", "không" vì có thể mang nghĩa trong cụm từ
            "bị", "hãy", "đã", "đang", "sẽ", "vẫn", "còn",
            "rồi", "cũng", "đều", "chỉ", "thôi", "cả", "phải",
            // Trạng từ mức độ
            "rất", "quá", "khá", "hơi", "thật", "lắm",
            // Web artifacts
            "http", "https", "www", "com", "vn"));

    private VnCoreNLP pipeline;
    private boolean vnCoreAvailable = false;

    public VietnameseAnalyzer() {
        try {
            String modelDir = extractModelsToTempDir();

            // Set system property để VnCoreNLP tìm model đúng chỗ
            System.setProperty("user.dir", modelDir);

            String[] annotators = { "wseg" };
            this.pipeline = new VnCoreNLP(annotators);
            this.vnCoreAvailable = true;
            System.out.println("[VietnameseAnalyzer] VnCoreNLP loaded.");
        } catch (Exception e) {
            System.err.println("[VietnameseAnalyzer] Fallback: " + e.getMessage());
            this.vnCoreAvailable = false;
        }
    }

    private String extractModelsToTempDir() throws Exception {
        String tempDir = System.getProperty("java.io.tmpdir") + "/vncorenlp";
        String modelPath = tempDir + "/models/wordsegmenter";
        new java.io.File(modelPath).mkdirs();

        for (String file : List.of("vi-vocab", "wordsegmenter.rdr")) {
            java.io.File dest = new java.io.File(modelPath + "/" + file);
            if (!dest.exists()) {
                try (var in = getClass().getResourceAsStream(
                        "/vncorenlp/models/wordsegmenter/" + file)) {
                    if (in == null)
                        throw new Exception("Model file not found in classpath: " + file);
                    java.nio.file.Files.copy(in, dest.toPath());
                }
            }
        }
        System.out.println("[VietnameseAnalyzer] Models extracted to: " + tempDir);
        return tempDir;
    }

    public List<String> analyze(String text) {
        if (text == null || text.isBlank())
            return new ArrayList<>();

        return vnCoreAvailable
                ? analyzeWithVnCore(text)
                : analyzeWithFallback(text);
    }

    // ─── VnCoreNLP path ──────────────────────────────────────────────────────

    private List<String> analyzeWithVnCore(String text) {
        try {
            Annotation annotation = new Annotation(text.toLowerCase());
            pipeline.annotate(annotation);

            List<String> tokens = new ArrayList<>();

            // getSentences() → List<Sentence>
            // sentence.getWords() → List<Word> ← đây là chỗ sai trước
            for (vn.pipeline.Sentence sentence : annotation.getSentences()) {
                for (vn.pipeline.Word word : sentence.getWords()) {
                    String token = word.getForm(); // "thủ_tướng", "học_sinh"
                    if (shouldSkip(token))
                        continue;
                    tokens.add(token);
                }
            }
            return tokens;

        } catch (Exception e) {
            System.err.println("[VietnameseAnalyzer] VnCore failed, fallback: " + e.getMessage());
            return analyzeWithFallback(text);
        }
    }

    // ─── Fallback path (whitespace tokenizer) ────────────────────────────────

    private List<String> analyzeWithFallback(String text) {
        String lower = text.toLowerCase();
        String cleaned = lower.replaceAll("[^\\p{L}\\s]", " ");

        List<String> tokens = new ArrayList<>();
        for (String token : cleaned.trim().split("\\s+")) {
            if (shouldSkip(token))
                continue;
            tokens.add(token);
        }
        return tokens;
    }

    // ─── Shared filter ───────────────────────────────────────────────────────

    private boolean shouldSkip(String token) {
        if (token == null || token.length() < 2)
            return true;
        if (STOPWORDS.contains(token))
            return true;
        // Bỏ token toàn số (năm 2024 → "2024" không có giá trị search)
        if (token.matches("\\d+"))
            return true;
        return false;
    }

    // ─── Debug helpers ───────────────────────────────────────────────────────

    public boolean isStopword(String word) {
        return STOPWORDS.contains(word.toLowerCase());
    }

    public boolean isVnCoreAvailable() {
        return vnCoreAvailable;
    }
}