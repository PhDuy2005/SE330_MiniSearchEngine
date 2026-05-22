package com.NgonNguLapTrinhJava.MiniSearchEngine.service.index;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Thống kê toàn bộ kho dữ liệu - bắt buộc để tính BM25.
 *
 * BM25 cần:
 *   score(q, d) = Σ IDF(t) * (TF(t,d) * (k1+1)) / (TF(t,d) + k1*(1 - b + b*|d|/avgdl))
 *
 * Trong đó:
 *   N     = tổng số documents
 *   avgdl = độ dài trung bình của document
 *   |d|   = độ dài document đang xét
 */
public class IndexStatistics implements Serializable {

    private static final long serialVersionUID = 1L;

    // Tổng số documents đã index
    private int totalDocuments;

    // Độ dài từng document (docId -> số từ sau khi analyze)
    private final Map<Long, Integer> documentLengths;

    // Tổng số từ của toàn bộ corpus (dùng tính avgdl)
    private long totalTermCount;

    public IndexStatistics() {
        this.documentLengths = new HashMap<>();
        this.totalDocuments  = 0;
        this.totalTermCount  = 0;
    }

    /**
     * Ghi nhận độ dài của một document sau khi analyze.
     */
    public void recordDocument(Long docId, int termCount) {
        documentLengths.put(docId, termCount);
        totalDocuments++;
        totalTermCount += termCount;
    }

    // ─── Getters cho phía Search ────────────────────────────────────────────

    /** N: tổng số documents */
    public int getTotalDocuments() {
        return totalDocuments;
    }

    /** avgdl: độ dài trung bình của documents */
    public double getAverageDocumentLength() {
        if (totalDocuments == 0) return 0.0;
        return (double) totalTermCount / totalDocuments;
    }

    /** Độ dài (số từ) của document theo docId */
    public int getDocumentLength(Long docId) {
        return documentLengths.getOrDefault(docId, 0);
    }

    /** Toàn bộ map độ dài (dùng khi cần iterate) */
    public Map<Long, Integer> getAllDocumentLengths() {
        return documentLengths;
    }

    @Override
    public String toString() {
        return String.format("IndexStatistics{N=%d, avgdl=%.2f, totalTerms=%d}",
                totalDocuments, getAverageDocumentLength(), totalTermCount);
    }
}
