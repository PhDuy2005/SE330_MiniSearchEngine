package com.NgonNguLapTrinhJava.MiniSearchEngine;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.service.index.Indexer;

@SpringBootApplication
public class MiniSearchEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(MiniSearchEngineApplication.class, args);
	}

	@Bean
    CommandLineRunner testIndexer() {
        return args -> {
            Indexer indexer = new Indexer();
            indexer.buildFromDirectory("data/");
            indexer.saveIndex("output/index");

            // Test query
            // Test các từ khác nhau
System.out.println("\n=== QUERY TESTS ===");

// Từ phổ biến
System.out.println("'bánh' → " + indexer.getPostingsForTerm("bánh").size() + " docs");
System.out.println("'nấu' → " + indexer.getPostingsForTerm("nấu").size() + " docs");

// Từ hiếm
System.out.println("'sầu riêng' không tách được → thử 'sầu': " 
    + indexer.getPostingsForTerm("sầu").size() + " docs");

// Từ không tồn tại
System.out.println("'blockchain' → " 
    + indexer.getPostingsForTerm("blockchain").size() + " docs");

// Metadata của doc đầu tiên
DocumentMetadata meta = indexer.getDocumentMetadata(0L);
System.out.println("\nDoc 0: " + meta.getTitle());
System.out.println("Snippet: " + meta.getSnippet());

            System.out.println("N=" + indexer.getTotalDocuments());
            System.out.println("avgdl=" + indexer.getAverageDocumentLength());
        };
    }
}
