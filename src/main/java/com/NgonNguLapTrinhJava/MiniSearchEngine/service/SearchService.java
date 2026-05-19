package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import org.springframework.stereotype.Service;

import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.DocumentRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class SearchService {
    private final DocumentRepository documentRepository;
}
