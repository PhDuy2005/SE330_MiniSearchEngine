package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity;

import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "documents")
@Getter
@Setter
@AllArgsConstructor
public class Document {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "url", columnDefinition = "TEXT", nullable = false, unique = true)
    private String link;

    @Column(name = "title", columnDefinition = "TEXT", nullable = false)
    private String title;

    @Column(name = "summary", columnDefinition = "TEXT")
    private String summary;

    @Column(name = "topic", nullable = false)
    private String topic;

    @Column(name = "content", columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    @Column(name = "crawledAt", updatable = false)
    private LocalDateTime createdAt;

    public Document() {}

    public Long getId()                { return id; }
    public void setId(Long id)         { this.id = id; }
    public String getLink()             { return link; }
    public String getTitle()           { return title; }
    public String getSummary()         { return summary; }
    public String getTopic()          { return topic; }
    public LocalDateTime getCreatedAt(){ return createdAt; }

    public String getFullText() {
        StringBuilder sb = new StringBuilder();
        if (title != null)   sb.append(title).append(" ");
        if (content != null) sb.append(content);
        return sb.toString();
    }
}
