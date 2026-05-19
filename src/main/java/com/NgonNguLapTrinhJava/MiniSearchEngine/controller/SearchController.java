package com.NgonNguLapTrinhJava.MiniSearchEngine.controller;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqSearchDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResSearchItemDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.service.SearchService;

import lombok.AllArgsConstructor;
import lombok.Data;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    @GetMapping("/search")
    public ResponseEntity<ResSearchItemDTO> search(@RequestBody ReqSearchDTO request) {
        // TODO: Implement search logic using searchService
        ResSearchItemDTO dummyResult = new ResSearchItemDTO();
        return ResponseEntity.ok(dummyResult);
    }

    @GetMapping("/test")
    public ResponseEntity<SomeTestObject> test() {
        SomeTestObject obj = new SomeTestObject(1, "Hello World");
        return ResponseEntity.ok(obj);
    }

    @Data
    @AllArgsConstructor
    private static class SomeTestObject {
        int id;
        String name;
    }
}
