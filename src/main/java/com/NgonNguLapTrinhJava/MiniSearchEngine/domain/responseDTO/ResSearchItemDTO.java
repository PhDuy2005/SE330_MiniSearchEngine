package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResSearchItemDTO {
    Long rank;
    Long index;

    String title;
    String url;
    String summary;
    String content;

    private Double score;
}
