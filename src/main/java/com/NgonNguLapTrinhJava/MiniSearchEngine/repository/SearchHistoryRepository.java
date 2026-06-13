package com.NgonNguLapTrinhJava.MiniSearchEngine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.SearchHistory;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {
}
