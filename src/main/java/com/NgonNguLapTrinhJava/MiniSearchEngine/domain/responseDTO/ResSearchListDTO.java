package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ResSearchListDTO {
    String query;
    long totalResults;
    int page;
    int size;
    
    List<ResSearchItemDTO> items;
}
