package com.kocatepe.kafe.dto;

import java.time.LocalDate;
import java.util.List;

public class YogunlukSatiri {

    private final LocalDate tarih;
    private final List<YogunlukHucre> saatler;
    private final int toplamSiparis;

    public YogunlukSatiri(LocalDate tarih, List<YogunlukHucre> saatler, int toplamSiparis) {
        this.tarih = tarih;
        this.saatler = saatler;
        this.toplamSiparis = toplamSiparis;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public List<YogunlukHucre> getSaatler() {
        return saatler;
    }

    public int getToplamSiparis() {
        return toplamSiparis;
    }

    public int getOrtalama() {
        return saatler.isEmpty() ? 0 : Math.round((float) toplamSiparis / saatler.size());
    }
}
