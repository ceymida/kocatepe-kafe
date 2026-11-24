package com.kocatepe.kafe.entity;

import java.time.LocalTime;

public enum VardiyaTipi {
    SABAH("Sabah", LocalTime.of(9, 0), LocalTime.of(17, 0)),
    AKSAM("Akşam", LocalTime.of(15, 0), LocalTime.of(23, 0));

    private final String etiket;
    private final LocalTime baslangic;
    private final LocalTime bitis;

    VardiyaTipi(String etiket, LocalTime baslangic, LocalTime bitis) {
        this.etiket = etiket;
        this.baslangic = baslangic;
        this.bitis = bitis;
    }

    public String getEtiket() {
        return etiket;
    }

    public LocalTime getBaslangic() {
        return baslangic;
    }

    public LocalTime getBitis() {
        return bitis;
    }

    /** 8 saat, 1 saat mola dahil. Haftalik limit hesabinda 8 saat sayilir. */
    public int getSaat() {
        return 8;
    }
}
