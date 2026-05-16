package com.NgonNguLapTrinhJava.MiniSearchEngine.controller;

import com.NgonNguLapTrinhJava.MiniSearchEngine.service.SearchService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SearchController {
    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }
}
