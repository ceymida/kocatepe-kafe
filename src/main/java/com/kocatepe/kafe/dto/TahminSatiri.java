package com.kocatepe.kafe.dto;

import java.time.LocalDate;
import java.util.List;

public class TahminSatiri {

    private final LocalDate tarih;
    private final List<TahminHucre> saatler;

    public TahminSatiri(LocalDate tarih, List<TahminHucre> saatler) {
        this.tarih = tarih;
        this.saatler = saatler;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public List<TahminHucre> getSaatler() {
        return saatler;
    }

    public double getToplamTahmin() {
        return Math.round(saatler.stream().mapToDouble(TahminHucre::getTahminiSiparis).sum() * 10.0) / 10.0;
    }

    public int getMaksPersonel() {
        return saatler.stream().mapToInt(TahminHucre::getGerekliPersonel).max().orElse(0);
    }
}
