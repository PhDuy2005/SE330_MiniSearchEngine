package com.NgonNguLapTrinhJava.MiniSearchEngine.util;


import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Document;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.ObjectMapper;

/**
 * Load documents từ nhiều file JSON (mỗi file 1 category).
 *
 * Hỗ trợ 2 cách dùng:
 *   1. Load 1 file cụ thể: loadFromFile("data/food.json")
 *   2. Load toàn bộ thư mục: loadFromDirectory("data/")  ← phù hợp với crawler output
 *
 * docId được gán tự động toàn cục (không reset theo file) để unique trên toàn corpus.
 */
public class JsonDocumentLoader {

    private final ObjectMapper objectMapper;

    public JsonDocumentLoader() {
        this.objectMapper = new ObjectMapper();
    }

    /**
     * Load tất cả file .json trong một thư mục.
     * docId sẽ là số tự tăng từ 0 trên toàn bộ corpus.
     *
     * @param directoryPath đường dẫn thư mục chứa các file JSON
     * @return danh sách tất cả Document từ mọi file
     */
    public List<Document> loadFromDirectory(String directoryPath) throws IOException {
        List<Document> allDocuments = new ArrayList<>();
        Long docIdCounter = Long.valueOf(0);

        Path dir = Paths.get(directoryPath);
        if (!Files.isDirectory(dir)) {
            throw new IOException("Not a directory: " + directoryPath);
        }

        // Lấy tất cả file .json, sort theo tên để đảm bảo thứ tự nhất quán
        List<Path> jsonFiles = new ArrayList<>();
        try (Stream<Path> stream = Files.list(dir)) {
            stream.filter(p -> p.toString().endsWith(".json"))
                  .sorted()
                  .forEach(jsonFiles::add);
        }

        if (jsonFiles.isEmpty()) {
            System.out.println("[Loader] Warning: No .json files found in " + directoryPath);
            return allDocuments;
        }

        for (Path jsonFile : jsonFiles) {
            System.out.printf("[Loader] Loading '%s'...%n", jsonFile.getFileName());
            List<Document> docs = loadFromFile(jsonFile.toString());

            // Gán docId toàn cục
            for (Document doc : docs) {
                doc.setId(docIdCounter++);
            }
            allDocuments.addAll(docs);
            System.out.printf("[Loader]   → Loaded %d documents (total so far: %d)%n",
                    docs.size(), allDocuments.size());
        }

        System.out.printf("[Loader] Done. Total documents loaded: %d%n", allDocuments.size());
        return allDocuments;
    }

    /**
     * Load một file JSON cụ thể.
     * Lưu ý: docId chưa được gán ở đây, gán sau bởi caller.
     *
     * @param filePath đường dẫn file JSON
     * @return danh sách Document trong file
     */
    public List<Document> loadFromFile(String filePath) throws IOException {
        File file = new File(filePath);
        if (!file.exists()) {
            throw new IOException("File not found: " + filePath);
        }

        try {
            List<Document> docs = objectMapper.readValue(file, new TypeReference<List<Document>>() {});
            // Lọc bỏ document không có content (tránh lỗi NullPointer ở Analyzer)
            docs.removeIf(d -> d.getContent() == null || d.getContent().isBlank());
            return docs;
        } catch (Exception e) {
            throw new IOException("Failed to parse JSON file: " + filePath + " → " + e.getMessage(), e);
        }
    }
}
