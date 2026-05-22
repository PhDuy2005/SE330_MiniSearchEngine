package com.NgonNguLapTrinhJava.MiniSearchEngine.service.index;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Posting;

import java.io.Serializable;
import java.util.*;

/**
 * Cấu trúc dữ liệu Inverted Index - xương sống của hệ thống tìm kiếm.
 *
 * Cấu trúc lưu trữ:
 *   term (String) → List<Posting> (danh sách doc chứa term + TF + positions)
 *
 * Cải tiến:
 *   - documentFrequency: cache sẵn DF(term) = số doc chứa term, tránh phải đếm lại mỗi query
 *   - documentMetadata: lưu sẵn metadata để Search hiển thị kết quả ngay mà không cần đọc file
 */
public class InvertedIndex implements Serializable {

    private static final long serialVersionUID = 1L;

    // term → danh sách Postings
    private final Map<String, List<Posting>> index;

    // term → số documents chứa term đó (Document Frequency - dùng tính IDF)
    private final Map<String, Integer> documentFrequency;

    // docId → metadata để Search hiển thị title, url, snippet
    private final Map<Long, DocumentMetadata> metadataStore;

    public InvertedIndex() {
        this.index             = new HashMap<>();
        this.documentFrequency = new HashMap<>();
        this.metadataStore     = new HashMap<>();
    }

    // ─── Indexing (gọi bởi Indexer khi build) ───────────────────────────────

    /**
     * Thêm một term xuất hiện tại vị trí position trong docId.
     * Tự động tạo Posting mới hoặc cập nhật Posting đã tồn tại.
     */
    public void addTerm(String term, Long docId, int position) {
        List<Posting> postings = index.computeIfAbsent(term, k -> new ArrayList<>());

        // Tìm Posting của docId này đã tồn tại chưa
        Posting existing = findPosting(postings, docId);
        if (existing == null) {
            Posting newPosting = new Posting(docId);
            newPosting.addOccurrence(position);
            postings.add(newPosting);
            // Cập nhật document frequency: lần đầu doc này chứa term
            documentFrequency.merge(term, 1, Integer::sum);
        } else {
            existing.addOccurrence(position);
            // DF không tăng vì cùng docId
        }
    }

    /**
     * Lưu metadata của document vào store.
     */
    public void addDocumentMetadata(DocumentMetadata metadata) {
        metadataStore.put(metadata.getDocId(), metadata);
    }

    // ─── Query (gọi bởi Search) ─────────────────────────────────────────────

    /**
     * Lấy danh sách Postings của một term.
     * Trả về empty list nếu term không tồn tại (không throw exception).
     */
    public List<Posting> getPostings(String term) {
        return index.getOrDefault(term, Collections.emptyList());
    }

    /**
     * Lấy Document Frequency của một term (số doc chứa term đó).
     * Dùng để tính IDF trong BM25: IDF = log((N - DF + 0.5) / (DF + 0.5))
     */
    public int getDocumentFrequency(String term) {
        return documentFrequency.getOrDefault(term, 0);
    }

    /**
     * Lấy metadata của một document theo docId.
     */
    public DocumentMetadata getDocumentMetadata(Long docId) {
        return metadataStore.get(docId);
    }

    /**
     * Lấy toàn bộ metadata store (dùng khi cần iterate tất cả docs).
     */
    public Map<Long, DocumentMetadata> getAllMetadata() {
        return Collections.unmodifiableMap(metadataStore);
    }

    /**
     * Tổng số terms đã được index (vocabulary size).
     */
    public int getVocabularySize() {
        return index.size();
    }

    /**
     * Kiểm tra term có trong index không.
     */
    public boolean containsTerm(String term) {
        return index.containsKey(term);
    }

    // ─── Private helpers ────────────────────────────────────────────────────

    private Posting findPosting(List<Posting> postings, Long docId) {
        for (Posting p : postings) {
            if (p.getDocId().equals(docId)) return p;
        }
        return null;
    }

    @Override
    public String toString() {
        return String.format("InvertedIndex{vocabularySize=%d, documents=%d}",
                getVocabularySize(), metadataStore.size());
    }
}
