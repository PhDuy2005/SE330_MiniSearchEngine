package com.NgonNguLapTrinhJava.MiniSearchEngine.util;

import org.springframework.core.MethodParameter;
import org.springframework.core.io.Resource;
import org.springframework.http.MediaType;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.servlet.mvc.method.annotation.ResponseBodyAdvice;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.RestResponse;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.annotation.ApiMessage;

import jakarta.servlet.http.HttpServletResponse;

@RestControllerAdvice
public class FormatRestResponse implements ResponseBodyAdvice<Object> {

    @Override
    public boolean supports(MethodParameter returnType, Class converterType) {

        return true;
    }

    @Override
    public Object beforeBodyWrite(Object body,
            MethodParameter returnType,
            MediaType selectedContentType,
            Class selectedConverterType,
            ServerHttpRequest request,
            ServerHttpResponse response) {
        HttpServletResponse httpServletResponse = ((ServletServerHttpResponse) response).getServletResponse();
        int code = httpServletResponse.getStatus();

        RestResponse<Object> restResponse = new RestResponse<>();
        restResponse.setStatusCode(code);

        String path = request.getURI().getPath();
        if (path.startsWith("/v3/api-docs") || path.startsWith("/swagger-ui")) {
            return body;
        }

        if (body instanceof String || body instanceof Resource) {
            return body;
        }

        // case error
        if (code >= 400) {
            return body;
        }

        else {
            // case success
            restResponse.setData(body);
            ApiMessage apiMessage = returnType.getMethodAnnotation(ApiMessage.class);
            restResponse.setMessage(apiMessage != null ? apiMessage.value() : "CALL API SUCCESS");
        }
        return restResponse;
    }

    // Static helper methods
    public static <T> RestResponse<T> success(T data) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .data(data)
                .message("Success")
                .build();
    }

    public static <T> RestResponse<T> success(T data, String message) {
        return RestResponse.<T>builder()
                .statusCode(200)
                .data(data)
                .message(message)
                .build();
    }

    public static <T> RestResponse<T> error(String message) {
        return RestResponse.<T>builder()
                .statusCode(400)
                .error(message)
                .message(message)
                .build();
    }

}