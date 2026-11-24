package com.kocatepe.kafe.dto;

public class YogunlukHucre {

    private final int saat;
    private final int siparisSayisi;
    private final int doluMasa;
    private final double oran;

    public YogunlukHucre(int saat, int siparisSayisi, int doluMasa, int haftaMax) {
        this.saat = saat;
        this.siparisSayisi = siparisSayisi;
        this.doluMasa = doluMasa;
        this.oran = haftaMax <= 0 ? 0 : Math.min(1.0, (double) siparisSayisi / haftaMax);
    }

    public int getSaat() {
        return saat;
    }

    public int getSiparisSayisi() {
        return siparisSayisi;
    }

    public int getDoluMasa() {
        return doluMasa;
    }

    public double getOran() {
        return oran;
    }

    public String getRenk() {
        double alfa = 0.06 + (oran * 0.88);
        return String.format("rgba(47,107,79,%.2f)", alfa);
    }

    public String getYaziRengi() {
        return oran > 0.55 ? "#ffffff" : "#2b211b";
    }

    public String getBaslik() {
        return String.format("%02d:00 - sipariş: %d, dolu masa: %d", saat, siparisSayisi, doluMasa);
    }
}
