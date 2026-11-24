package com.kocatepe.kafe.dto;

import java.time.LocalDate;

import com.kocatepe.kafe.entity.VardiyaTipi;

public class IhtiyacDurumu {

    private final LocalDate tarih;
    private final VardiyaTipi vardiyaTipi;
    private final int gerekli;
    private final int atanan;

    public IhtiyacDurumu(LocalDate tarih, VardiyaTipi vardiyaTipi, int gerekli, int atanan) {
        this.tarih = tarih;
        this.vardiyaTipi = vardiyaTipi;
        this.gerekli = gerekli;
        this.atanan = atanan;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public VardiyaTipi getVardiyaTipi() {
        return vardiyaTipi;
    }

    public int getGerekli() {
        return gerekli;
    }

    public int getAtanan() {
        return atanan;
    }

    public int getFark() {
        return atanan - gerekli;
    }

    /** EKSIK / FAZLA / UYGUN */
    public String getDurum() {
        if (atanan < gerekli) {
            return "EKSIK";
        }
        if (atanan > gerekli) {
            return "FAZLA";
        }
        return "UYGUN";
    }

    public String getOzet() {
        return gerekli + " gerekli / " + atanan + " atandi";
    }
}
