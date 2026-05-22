package com.NgonNguLapTrinhJava.MiniSearchEngine.service.index;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Document;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Posting;
import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.IndexedDataRepository;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.IndexPersistence;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.JsonDocumentLoader;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.VietnameseAnalyzer;

import java.util.List;

/**
 * Orchestrator của toàn bộ pipeline Index:
 *
 *   JSON files → Load → Analyze → Build InvertedIndex → Save .bin
 *
 * Đây là class chính người dùng tương tác, cũng implement IndexedDataRepository
 * để bàn giao trực tiếp cho phía Search.
 */
public class Indexer implements IndexedDataRepository {

    private final VietnameseAnalyzer analyzer;
    private final JsonDocumentLoader loader;
    private InvertedIndex            invertedIndex;
    private IndexStatistics          statistics;

    public Indexer() {
        this.analyzer     = new VietnameseAnalyzer();
        this.loader       = new JsonDocumentLoader();
        this.invertedIndex = new InvertedIndex();
        this.statistics   = new IndexStatistics();
    }

    // ─── Build Index từ đầu ─────────────────────────────────────────────────

    /**
     * Build toàn bộ index từ thư mục chứa các file JSON.
     *
     * @param dataDirectory thư mục chứa file .json của crawler
     */
    public void buildFromDirectory(String dataDirectory) throws Exception {
        System.out.println("=== INDEXER: Starting build from directory: " + dataDirectory);
        long start = System.currentTimeMillis();

        // Reset index
        this.invertedIndex = new InvertedIndex();
        this.statistics    = new IndexStatistics();

        // Bước 1: Load documents
        List<Document> documents = loader.loadFromDirectory(dataDirectory);
        if (documents.isEmpty()) {
            System.out.println("[Indexer] Warning: No documents to index.");
            return;
        }

        // Bước 2: Index từng document
        System.out.printf("[Indexer] Indexing %d documents...%n", documents.size());
        for (Document doc : documents) {
            indexDocument(doc);
        }

        long elapsed = System.currentTimeMillis() - start;
        System.out.printf("=== INDEXER: Build complete in %dms%n", elapsed);
        System.out.printf("    Documents: %d | Vocabulary: %d | AvgDocLen: %.1f%n",
                statistics.getTotalDocuments(),
                invertedIndex.getVocabularySize(),
                statistics.getAverageDocumentLength());
    }

    /**
     * Index một document đơn lẻ vào InvertedIndex.
     */
    private void indexDocument(Document doc) {
        // Lưu metadata để Search hiển thị kết quả
        invertedIndex.addDocumentMetadata(DocumentMetadata.fromDocument(doc));

        // Analyze text: title + content → danh sách token
        List<String> tokens = analyzer.analyze(doc.getFullText());

        // Ghi nhận độ dài document vào statistics
        statistics.recordDocument(doc.getId(), tokens.size());

        // Thêm từng token vào inverted index kèm vị trí
        for (int pos = 0; pos < tokens.size(); pos++) {
            invertedIndex.addTerm(tokens.get(pos), doc.getId(), pos);
        }
    }

    // ─── Persistence ────────────────────────────────────────────────────────

    /**
     * Lưu index đã build xuống thư mục chỉ định.
     */
    public void saveIndex(String outputDirectory) throws Exception {
        IndexPersistence.saveIndex(outputDirectory, invertedIndex, statistics);
    }

    /**
     * Load index từ thư mục (dùng khi Search khởi động).
     */
    public void loadIndex(String indexDirectory) throws Exception {
        this.invertedIndex = IndexPersistence.loadIndex(indexDirectory);
        this.statistics    = IndexPersistence.loadStatistics(indexDirectory);
        System.out.printf("[Indexer] Loaded: %s | %s%n", invertedIndex, statistics);
    }

    // ─── IndexedDataRepository (API cho Search) ─────────────────────────────

    @Override
    public List<String> analyzeQuery(String queryString) {
        return analyzer.analyze(queryString);
    }

    @Override
    public List<Posting> getPostingsForTerm(String term) {
        return invertedIndex.getPostings(term);
    }

    @Override
    public int getDocumentFrequency(String term) {
        return invertedIndex.getDocumentFrequency(term);
    }

    @Override
    public int getTotalDocuments() {
        return statistics.getTotalDocuments();
    }

    @Override
    public double getAverageDocumentLength() {
        return statistics.getAverageDocumentLength();
    }

    @Override
    public int getDocumentLength(Long docId) {
        return statistics.getDocumentLength(docId);
    }

    @Override
    public DocumentMetadata getDocumentMetadata(Long docId) {
        return invertedIndex.getDocumentMetadata(docId);
    }

    // ─── Getters cho test / debug ────────────────────────────────────────────

    public InvertedIndex getInvertedIndex() { return invertedIndex; }
    public IndexStatistics getStatistics()  { return statistics; }
}
