package com.kocatepe.kafe.dto;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public class VardiyaHaftasi {

    private final LocalDate haftaBaslangici;
    private final List<LocalDate> gunler;
    private final List<VardiyaSatiri> satirlar;
    private final Map<String, IhtiyacDurumu> ihtiyaclar;
    private final Map<LocalDate, IhtiyacDurumu> sabahIhtiyaclari;
    private final Map<LocalDate, IhtiyacDurumu> aksamIhtiyaclari;
    private final int toplamVardiya;
    private final int yayindakiVardiya;

    public VardiyaHaftasi(LocalDate haftaBaslangici, List<LocalDate> gunler, List<VardiyaSatiri> satirlar,
                          Map<String, IhtiyacDurumu> ihtiyaclar,
                          Map<LocalDate, IhtiyacDurumu> sabahIhtiyaclari,
                          Map<LocalDate, IhtiyacDurumu> aksamIhtiyaclari,
                          int toplamVardiya, int yayindakiVardiya) {
        this.haftaBaslangici = haftaBaslangici;
        this.gunler = gunler;
        this.satirlar = satirlar;
        this.ihtiyaclar = ihtiyaclar;
        this.sabahIhtiyaclari = sabahIhtiyaclari;
        this.aksamIhtiyaclari = aksamIhtiyaclari;
        this.toplamVardiya = toplamVardiya;
        this.yayindakiVardiya = yayindakiVardiya;
    }

    public LocalDate getHaftaBaslangici() {
        return haftaBaslangici;
    }

    public LocalDate getHaftaBitisi() {
        return haftaBaslangici.plusDays(6);
    }

    public List<LocalDate> getGunler() {
        return gunler;
    }

    public List<VardiyaSatiri> getSatirlar() {
        return satirlar;
    }

    public Map<String, IhtiyacDurumu> getIhtiyaclar() {
        return ihtiyaclar;
    }

    public Map<LocalDate, IhtiyacDurumu> getSabahIhtiyaclari() {
        return sabahIhtiyaclari;
    }

    public IhtiyacDurumu sabahIhtiyaci(LocalDate gun) {
        return sabahIhtiyaclari.get(gun);
    }

    public Map<LocalDate, IhtiyacDurumu> getAksamIhtiyaclari() {
        return aksamIhtiyaclari;
    }

    public IhtiyacDurumu aksamIhtiyaci(LocalDate gun) {
        return aksamIhtiyaclari.get(gun);
    }

    public int getToplamVardiya() {
        return toplamVardiya;
    }

    public int getYayindakiVardiya() {
        return yayindakiVardiya;
    }

    public boolean isTamamenYayinda() {
        return toplamVardiya > 0 && yayindakiVardiya == toplamVardiya;
    }

    public boolean isTaslakVar() {
        return toplamVardiya > yayindakiVardiya;
    }
}
