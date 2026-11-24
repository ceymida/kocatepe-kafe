package com.kocatepe.kafe.entity;

public enum Pozisyon {
    BARISTA("Barista"),
    STEWARD("Steward"),
    MUTFAK("Mutfak"),
    KASIYER("Kasiyer");

    private final String etiket;

    Pozisyon(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }
}
