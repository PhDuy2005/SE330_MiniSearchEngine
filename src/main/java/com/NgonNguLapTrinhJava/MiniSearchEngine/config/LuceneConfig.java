package com.NgonNguLapTrinhJava.MiniSearchEngine.config;

import java.io.IOException;
import java.nio.file.Paths;

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;

import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class LuceneConfig {

    @Bean
    public Directory luceneDirectory() throws IOException {

        return FSDirectory.open(
                Paths.get("lucene-index")
        );
    }

    @Bean
    public Analyzer analyzer() {

        return new StandardAnalyzer();
    }
}