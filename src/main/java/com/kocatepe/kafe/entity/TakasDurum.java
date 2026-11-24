package com.kocatepe.kafe.entity;

public enum TakasDurum {
    BEKLIYOR("Bekliyor"),
    ONAYLANDI("Onaylandı"),
    REDDEDILDI("Reddedildi");

    private final String etiket;

    TakasDurum(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }
}
