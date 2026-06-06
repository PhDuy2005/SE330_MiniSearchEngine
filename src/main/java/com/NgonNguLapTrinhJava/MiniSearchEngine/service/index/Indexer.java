package com.NgonNguLapTrinhJava.MiniSearchEngine.service.index;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Document;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Posting;
import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.IndexedDataRepository;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.IndexPersistence;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.JsonDocumentLoader;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.VietnameseAnalyzer;

import java.util.ArrayList;
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
    // Trong Indexer.indexDocument()
private void indexDocument(Document doc) {
    invertedIndex.addDocumentMetadata(DocumentMetadata.fromDocument(doc));

    // Title có trọng số cao hơn → repeat 3 lần
    List<String> titleTokens   = analyzer.analyze(repeat(doc.getTitle(), 3));
    // Summary trọng số trung bình → repeat 1 lần
    List<String> summaryTokens = analyzer.analyze(repeat(doc.getSummary(), 1));
    // Content bình thường
    List<String> contentTokens = analyzer.analyze(doc.getContent());

    List<String> allTokens = new ArrayList<>();
    allTokens.addAll(titleTokens);
    allTokens.addAll(summaryTokens);
    allTokens.addAll(contentTokens);

    statistics.recordDocument(doc.getId(), allTokens.size());
    for (int pos = 0; pos < allTokens.size(); pos++) {
        invertedIndex.addTerm(allTokens.get(pos), doc.getId(), pos);
    }
}

private String repeat(String text, int times) {
    if (text == null) return "";
    return (text + " ").repeat(times).trim();
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
