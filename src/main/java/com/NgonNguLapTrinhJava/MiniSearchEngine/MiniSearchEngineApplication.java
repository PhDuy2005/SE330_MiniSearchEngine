package com.NgonNguLapTrinhJava.MiniSearchEngine;

import java.util.Arrays;
import java.util.List;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Posting;
import com.NgonNguLapTrinhJava.MiniSearchEngine.service.index.Indexer;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.IndexPersistence;

@SpringBootApplication
public class MiniSearchEngineApplication {

    public static void main(String[] args) {
        SpringApplication.run(MiniSearchEngineApplication.class, args);
    }

    @Bean
    CommandLineRunner testIndexer() {
        return args -> {

            System.out.println("========== BUILD INDEX ==========");
            String indexPath = "lucene-index";

            Indexer loadedIndexer = new Indexer();
            System.out.println("Building index...");

            loadedIndexer.buildFromDirectory("data/");
            loadedIndexer.saveIndex(indexPath);

            System.out.println("\n========== ANALYZE QUERY TEST ==========");

            String query = "Cách làm bánh bò";
            List<String> analyzed = loadedIndexer.analyzeQuery(query);

            System.out.println("Original query : " + query);
            System.out.println("Analyzed tokens: " + analyzed);

            System.out.println("\n========== POSTINGS TEST ==========");

            String[] testTerms = {
                    "chó",
                    "bệnh",
            };
            Arrays.stream(testTerms)
                    .forEach(term -> {

                        List<Posting> postings = loadedIndexer.getPostingsForTerm(term);

                        System.out.println("\nTERM: '" + term + "'");
                        System.out.println("DF = "
                                + loadedIndexer.getDocumentFrequency(term));

                        System.out.println("Postings count = "
                                + postings.size());

                        postings.stream()
                                .forEach(System.out::println);
                    });

            System.out.println("\n========== GLOBAL STATISTICS ==========");

            System.out.println("Total documents = "
                    + loadedIndexer.getTotalDocuments());

            System.out.println("Average doc length = "
                    + loadedIndexer.getAverageDocumentLength());

            System.out.println("\n========== DOCUMENT TEST ==========");

            Long testDocId = 271L;

            System.out.println("Doc length = "
                    + loadedIndexer.getDocumentLength(testDocId));

            DocumentMetadata meta = loadedIndexer.getDocumentMetadata(testDocId);

            if (meta != null) {

                System.out.println("Doc title   = "
                        + meta.getTitle());

                System.out.println("Doc snippet = "
                        + meta.getSnippet());
            } else {
                System.out.println("Metadata not found.");
            }

            System.out.println("\n========== PHRASE POSITION TEST ==========");

            List<Posting> banhPostings = loadedIndexer.getPostingsForTerm("bánh");

            if (!banhPostings.isEmpty()) {

                Posting first = banhPostings.get(0);

                System.out.println("Sample posting:");
                System.out.println(first);

                System.out.println("Positions count = "
                        + first.getPositions().size());
            }

            System.out.println("\n========== INDEX TEST COMPLETE ==========");
        };
    }
}
