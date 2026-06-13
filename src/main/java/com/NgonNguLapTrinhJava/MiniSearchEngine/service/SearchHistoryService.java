package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.SearchHistory;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.enums.SearchHistoryType;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqSearchHistoryDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResSearchHistoryItemDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResSearchHistoryListDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.SearchHistoryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class SearchHistoryService {

    private final SearchHistoryRepository searchHistoryRepository;

    public ResSearchHistoryItemDTO create(ReqSearchHistoryDTO request) {
        validate(request);

        SearchHistory history = new SearchHistory();
        history.setType(request.getType());

        if (request.getType() == SearchHistoryType.QUERY) {
            history.setQuery(request.getQuery().trim());
        } else {
            history.setTitle(request.getTitle().trim());
            history.setUrl(request.getUrl().trim());
        }

        SearchHistory saved = searchHistoryRepository.save(history);
        return toItemDTO(saved);
    }

    public ResSearchHistoryListDTO getHistory(int page, int size) {
        if (page < 0) {
            throw new IllegalArgumentException("page must be greater than or equal to 0");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("size must be greater than 0");
        }

        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "visitedAt"));
        Page<SearchHistory> historyPage = searchHistoryRepository.findAll(pageable);

        ResSearchHistoryListDTO response = new ResSearchHistoryListDTO();
        response.setTotalResults(historyPage.getTotalElements());
        response.setTotalPages(historyPage.getTotalPages());
        response.setPage(page);
        response.setSize(size);
        response.setItems(historyPage.getContent().stream()
                .map(this::toItemDTO)
                .toList());
        return response;
    }

    private void validate(ReqSearchHistoryDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }
        if (request.getType() == null) {
            throw new IllegalArgumentException("type is required");
        }

        if (request.getType() == SearchHistoryType.QUERY) {
            if (!hasText(request.getQuery())) {
                throw new IllegalArgumentException("query is required when type is QUERY");
            }
            return;
        }

        if (!hasText(request.getTitle())) {
            throw new IllegalArgumentException("title is required when type is URL");
        }
        if (!hasText(request.getUrl())) {
            throw new IllegalArgumentException("url is required when type is URL");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private ResSearchHistoryItemDTO toItemDTO(SearchHistory history) {
        return new ResSearchHistoryItemDTO(
                history.getId(),
                history.getType(),
                history.getVisitedAt(),
                history.getQuery(),
                history.getTitle(),
                history.getUrl()
        );
    }
}
