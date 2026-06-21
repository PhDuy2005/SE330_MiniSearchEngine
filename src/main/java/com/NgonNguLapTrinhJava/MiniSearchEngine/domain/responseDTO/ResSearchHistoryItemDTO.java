package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO;

import java.time.LocalDateTime;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.enums.SearchHistoryType;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResSearchHistoryItemDTO {
    private Long id;
    private Long userId;
    private SearchHistoryType type;
    private LocalDateTime visitedAt;
    private String query;
    private String title;
    private String url;
}
