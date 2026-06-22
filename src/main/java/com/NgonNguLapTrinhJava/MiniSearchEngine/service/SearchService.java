package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.DocumentMetadata;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Posting;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResSearchItemDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResSearchListDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.IndexedDataRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class SearchService {

    private final IndexedDataRepository indexedDataRepository;

    public ResSearchListDTO search(String query, int page, int size) {
        if (query == null || query.trim().isEmpty()) {
            return emptyResponse(query, page, size);
        }

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 10;
        }

        List<String> terms = indexedDataRepository.analyzeQuery(query);
        
        terms = removeQueryStopWords(terms);

        Set<String> uniqueTerms = new LinkedHashSet<>(terms);

        if (uniqueTerms.isEmpty()) {
            return emptyResponse(query, page, size);
        }

        Map<Long, Double> scoreMap = new HashMap<>();
        Map<Long, Set<String>> matchedTermsMap = new HashMap<>();

        for (String term : uniqueTerms) {
            List<Posting> postings = indexedDataRepository.getPostingsForTerm(term);
            if (postings == null || postings.isEmpty()) {
                continue;
            }

            for (Posting posting : postings) {
                Long docId = posting.getDocId();

                double bm25Score = calculateBM25(term, posting);

                scoreMap.merge(docId, bm25Score, Double::sum);

                matchedTermsMap
                        .computeIfAbsent(docId, k -> new HashSet<>())
                        .add(term);
            }
        }

        for (Long docId : new ArrayList<>(scoreMap.keySet())) {
            DocumentMetadata metadata = indexedDataRepository.getDocumentMetadata(docId);

            if (metadata == null) {
                continue;
            }

            double baseScore = scoreMap.get(docId);
            Set<String> matchedTerms = matchedTermsMap.getOrDefault(docId, Set.of());

            double finalScore = applyRankingBoost(
                    query,
                    uniqueTerms,
                    matchedTerms,
                    metadata,
                    baseScore
            );

            scoreMap.put(docId, finalScore);
        }

        List<Map.Entry<Long, Double>> rankedDocs = new ArrayList<>(scoreMap.entrySet());
        rankedDocs.sort((a, b) -> Double.compare(b.getValue(), a.getValue()));

        int total = rankedDocs.size();

        int fromIndex = Math.min(page * size, total);
        int toIndex = Math.min(fromIndex + size, total);

        List<ResSearchItemDTO> items = new ArrayList<>();

        long rank = fromIndex + 1L;

        for (Map.Entry<Long, Double> entry : rankedDocs.subList(fromIndex, toIndex)) {
            Long docId = entry.getKey();
            Double score = entry.getValue();

            DocumentMetadata metadata = indexedDataRepository.getDocumentMetadata(docId);

            if (metadata == null) {
                continue;
            }

            ResSearchItemDTO item = new ResSearchItemDTO();

            item.setRank(rank++);
            item.setIndex(docId);
            item.setTitle(metadata.getTitle());
            item.setUrl(metadata.getLink());
            item.setSummary(metadata.getSnippet());
            item.setContent(metadata.getSnippet());
            item.setScore(score);

            items.add(item);
        }

        ResSearchListDTO response = new ResSearchListDTO();
        response.setQuery(query);
        response.setTotalResults((long) total);
        response.setPage(page);
        response.setSize(size);
        response.setItems(items);

        return response;
    }

    private double calculateBM25(String term, Posting posting) {
        double k1 = 1.5;
        double b = 0.75;

        int totalDocuments = indexedDataRepository.getTotalDocuments();
        int documentFrequency = indexedDataRepository.getDocumentFrequency(term);
        int termFrequency = posting.getTermFrequency();

        int documentLength = indexedDataRepository.getDocumentLength(posting.getDocId());
        double averageDocumentLength = indexedDataRepository.getAverageDocumentLength();

        if (totalDocuments == 0 || documentFrequency == 0 || documentLength == 0 || averageDocumentLength == 0) {
            return 0.0;
        }

        double idf = Math.log(1 + (totalDocuments - documentFrequency + 0.5) / (documentFrequency + 0.5));

        double numerator = termFrequency * (k1 + 1);
        double denominator = termFrequency + k1 * (1 - b + b * documentLength / averageDocumentLength);

        return idf * numerator / denominator;
    }

    private double applyRankingBoost(
            String originalQuery,
            Set<String> uniqueTerms,
            Set<String> matchedTerms,
            DocumentMetadata metadata,
            double baseScore
    ) {
        double score = baseScore;

        String normalizedQuery = normalize(originalQuery);
        String title = normalize(metadata.getTitle());
        String snippet = normalize(metadata.getSnippet());

        int totalQueryTerms = uniqueTerms.size();
        int matchedTermCount = matchedTerms.size();

        if (totalQueryTerms > 0) {
            double coverageRatio = (double) matchedTermCount / totalQueryTerms;

            // Document match càng nhiều từ khóa trong query thì càng được cộng nhẹ.
            score += baseScore * 0.25 * coverageRatio;

            // Nếu match đủ toàn bộ từ khóa quan trọng thì cộng thêm một ít.
            if (matchedTermCount == totalQueryTerms) {
                score += 1.0;
            }
        }

        // Nếu title chứa nguyên cụm query thì cộng điểm mạnh hơn.
        if (!normalizedQuery.isEmpty() && title.contains(normalizedQuery)) {
            score += 3.0;
        }

        // Nếu snippet chứa nguyên cụm query thì cộng vừa phải.
        if (!normalizedQuery.isEmpty() && snippet.contains(normalizedQuery)) {
            score += 1.5;
        }

        // Cộng nhẹ nếu từng từ khóa xuất hiện trong title hoặc snippet.
        for (String term : uniqueTerms) {
            String normalizedTerm = normalize(term);

            if (normalizedTerm.isEmpty()) {
                continue;
            }

            if (title.contains(normalizedTerm)) {
                score += 0.5;
            }

            if (snippet.contains(normalizedTerm)) {
                score += 0.2;
            }
        }

        return score;
    }

    private List<String> removeQueryStopWords(List<String> terms) {
        Set<String> stopWords = Set.of(
                "bị", "có", "là", "và", "của", "các", "những",
                "một", "này", "đó", "với", "trong", "khi", "cho",
                "được", "đã", "sẽ", "thì", "mà", "ở", "từ", "về"
        );

        List<String> result = new ArrayList<>();

        for (String term : terms) {
            if (term == null) {
                continue;
            }

            String normalizedTerm = term.trim().toLowerCase();

            if (!normalizedTerm.isEmpty() && !stopWords.contains(normalizedTerm)) {
                result.add(normalizedTerm);
            }
        }

        return result;
    }

    private String normalize(String text) {
        if (text == null) {
            return "";
        }

        return text
                .toLowerCase()
                .replaceAll("[^\\p{L}\\p{N}\\s]", " ")
                .replaceAll("\\s+", " ")
                .trim();
    }

    private ResSearchListDTO emptyResponse(String query, int page, int size) {
        ResSearchListDTO response = new ResSearchListDTO();
        response.setQuery(query);
        response.setTotalResults(0L);
        response.setPage(page);
        response.setSize(size);
        response.setItems(new ArrayList<>());
        return response;
    }
}