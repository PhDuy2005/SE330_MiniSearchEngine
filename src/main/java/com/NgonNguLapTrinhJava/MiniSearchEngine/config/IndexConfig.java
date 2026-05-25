package com.NgonNguLapTrinhJava.MiniSearchEngine.config;

import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.IndexedDataRepository;
import com.NgonNguLapTrinhJava.MiniSearchEngine.service.index.Indexer;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.IndexPersistence;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class IndexConfig {

    private static final String DATA_PATH = "data";
    private static final String INDEX_PATH = "lucene-index";

    @Bean
    public IndexedDataRepository indexedDataRepository() {
        Indexer indexer = new Indexer();

        try {
            if (IndexPersistence.indexExists(INDEX_PATH)) {
                indexer.loadIndex(INDEX_PATH);
                System.out.println("Loaded existing index from: " + INDEX_PATH);
            } else {
                indexer.buildFromDirectory(DATA_PATH);
                indexer.saveIndex(INDEX_PATH);
                System.out.println("Built new index from: " + DATA_PATH);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize search index", e);
        }

        return indexer;
    }
}