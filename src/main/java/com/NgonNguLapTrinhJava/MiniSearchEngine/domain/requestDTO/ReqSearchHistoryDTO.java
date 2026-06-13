package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.enums.SearchHistoryType;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class ReqSearchHistoryDTO {
    private SearchHistoryType type;
    private String query;
    private String title;
    private String url;
}
