package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RestResponse<T> {
    private int statusCode;
    private String error;

    private Object message;
    private T data;
}