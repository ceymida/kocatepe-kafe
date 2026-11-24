package com.kocatepe.kafe.dto;

public class TahminHucre {

    private final int saat;
    private final double tahminiSiparis;
    private final int gerekliPersonel;

    public TahminHucre(int saat, double tahminiSiparis, int gerekliPersonel) {
        this.saat = saat;
        this.tahminiSiparis = tahminiSiparis;
        this.gerekliPersonel = gerekliPersonel;
    }

    public int getSaat() {
        return saat;
    }

    public double getTahminiSiparis() {
        return tahminiSiparis;
    }

    public int getGerekliPersonel() {
        return gerekliPersonel;
    }
}
