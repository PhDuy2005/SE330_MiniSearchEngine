package com.NgonNguLapTrinhJava.MiniSearchEngine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.SearchHistory;

@Repository
public interface SearchHistoryRepository extends JpaRepository<SearchHistory, Long> {

    Page<SearchHistory> findByUserId(Long userId, Pageable pageable);
}
