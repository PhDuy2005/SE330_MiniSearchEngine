package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity;

import java.io.Serializable;

public class DocumentMetadata implements Serializable {

    private static final long serialVersionUID = 1L;

    private final Long docId;
    private final String link;
    private final String title;
    private final String topic;
    private final String snippet;

    public DocumentMetadata(Long docId, String link, String title, String topic, String snippet) {
        this.docId   = docId;
        this.link     = link;
        this.title   = title;
        this.topic  = topic;
        this.snippet = snippet;
    }

    public static DocumentMetadata fromDocument(Document doc) {
        String summary = doc.getSummary();
        String content = doc.getContent();

        String snippet;
        if (summary != null && summary.length() > 160) {
            snippet = summary.substring(0, 160) + "...";
        } else if (content != null && content.length() > 160) {
            snippet = content.substring(0, 160) + "...";
        } else {
            snippet = "";
        }
        return new DocumentMetadata(doc.getId(), doc.getLink(), doc.getTitle(), doc.getTopic(), snippet);
    }

    public Long getDocId()      { return docId; }
    public String getLink()     { return link; }
    public String getTitle()   { return title; }
    public String getTopic()  { return topic; }
    public String getSnippet() { return snippet; }

    @Override
    public String toString() {
        return "DocumentMetadata{docId=" + docId + ", title='" + title + "', link='" + link + "'}";
    }
}