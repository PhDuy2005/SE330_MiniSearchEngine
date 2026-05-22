package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Posting implements Serializable {

    private static final Long serialVersionUID = 1L;

    private final Long docId;        // đổi int → long
    private int termFrequency;
    private final List<Integer> positions;

    public Posting(long docId) {     // đổi int → long
        this.docId = docId;
        this.termFrequency = 0;
        this.positions = new ArrayList<>();
    }

    public void addOccurrence(int position) {
        this.termFrequency++;
        this.positions.add(position);
    }

    public Long getDocId()              { return docId; }   // đổi int → long
    public int getTermFrequency()       { return termFrequency; }
    public List<Integer> getPositions() { return positions; }

    @Override
    public String toString() {
        return "Posting{docId=" + docId + ", TF=" + termFrequency + ", positions=" + positions + "}";
    }
}