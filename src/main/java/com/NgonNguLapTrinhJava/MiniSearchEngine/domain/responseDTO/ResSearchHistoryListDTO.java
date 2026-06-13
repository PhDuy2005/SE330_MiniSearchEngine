package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResSearchHistoryListDTO {
    private long totalResults;
    private int totalPages;
    private int page;
    private int size;
    private List<ResSearchHistoryItemDTO> items;
}
