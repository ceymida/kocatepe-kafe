package com.kocatepe.kafe.dto;

import java.time.LocalDate;
import java.util.List;

public class TahminHaftasi {

    private final LocalDate haftaBaslangici;
    private final List<TahminSatiri> satirlar;
    private final List<Integer> saatler;
    private final double toplamTahmin;

    public TahminHaftasi(LocalDate haftaBaslangici, List<TahminSatiri> satirlar, List<Integer> saatler,
                         double toplamTahmin) {
        this.haftaBaslangici = haftaBaslangici;
        this.satirlar = satirlar;
        this.saatler = saatler;
        this.toplamTahmin = toplamTahmin;
    }

    public LocalDate getHaftaBaslangici() {
        return haftaBaslangici;
    }

    public LocalDate getHaftaBitisi() {
        return haftaBaslangici.plusDays(6);
    }

    public List<TahminSatiri> getSatirlar() {
        return satirlar;
    }

    public List<Integer> getSaatler() {
        return saatler;
    }

    public double getToplamTahmin() {
        return toplamTahmin;
    }

    public double getGunlukOrtalama() {
        return satirlar.isEmpty() ? 0 : Math.round(toplamTahmin / satirlar.size() * 10.0) / 10.0;
    }
}
