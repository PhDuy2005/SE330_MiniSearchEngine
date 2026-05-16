package com.NgonNguLapTrinhJava.MiniSearchEngine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.Document;

@Repository
public interface DocumentRepository extends JpaRepository<Document, Long> {

}
