package com.kocatepe.kafe.entity;

public enum CalismaTipi {
    TAM_ZAMANLI("Tam zamanlı"),
    PART_TIME("Part-time");

    private final String etiket;

    CalismaTipi(String etiket) {
        this.etiket = etiket;
    }

    public String getEtiket() {
        return etiket;
    }
}
