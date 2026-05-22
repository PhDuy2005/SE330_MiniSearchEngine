package com.NgonNguLapTrinhJava.MiniSearchEngine.repository;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Posting;

import java.util.List;

/**
 * Contract (API) bàn giao cho phía Search.
 *
 * Người làm Search chỉ cần inject interface này, không cần biết
 * bên trong lưu trữ như thế nào (HashMap, Trie, hay file .bin).
 *
 * Cải tiến so với bản gốc:
 *   - getDocumentFrequency: để Search tự tính IDF không cần access vào InvertedIndex
 *   - getDocumentMetadata: để Search hiển thị title/url/snippet cho người dùng
 */
public interface IndexedDataRepository {

    // ── 1. Phân tích query ──────────────────────────────────────────────────
    /**
     * Phân tích câu truy vấn của người dùng thành các token sạch.
     * Dùng cùng pipeline với lúc index để đảm bảo nhất quán.
     *
     * Ví dụ: "Cách làm Bánh Bò BÔNG" → ["cách", "làm", "bánh", "bò", "bông"]
     */
    List<String> analyzeQuery(String queryString);

    // ── 2. Tra cứu Inverted Index ───────────────────────────────────────────
    /**
     * Lấy danh sách Postings (docId + TF + positions) của một term.
     * Trả về empty list nếu term không tồn tại.
     */
    List<Posting> getPostingsForTerm(String term);

    /**
     * Lấy Document Frequency: số documents chứa term đó.
     * Dùng để tính IDF = log((N - DF + 0.5) / (DF + 0.5) + 1)
     */
    int getDocumentFrequency(String term);

    // ── 3. Thống kê BM25 ───────────────────────────────────────────────────
    /**
     * N: tổng số documents đã index.
     */
    int getTotalDocuments();

    /**
     * avgdl: độ dài trung bình của documents (tính theo số token sau analyze).
     */
    double getAverageDocumentLength();

    /**
     * Độ dài (số token) của document theo docId.
     */
    int getDocumentLength(Long docId);

    // ── 4. Metadata để hiển thị kết quả ────────────────────────────────────
    /**
     * Lấy metadata (title, url, snippet, topic) của document theo docId.
     * Trả về null nếu docId không tồn tại.
     */
    DocumentMetadata getDocumentMetadata(Long docId);
}
