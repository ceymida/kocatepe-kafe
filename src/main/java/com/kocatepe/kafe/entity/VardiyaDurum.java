package com.kocatepe.kafe.entity;

public enum VardiyaDurum {
    TASLAK("Taslak"),
    YAYINDA("Yayında");

    private final String etiket;

    VardiyaDurum(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }
}
