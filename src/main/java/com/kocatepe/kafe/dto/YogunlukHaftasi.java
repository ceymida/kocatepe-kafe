package com.kocatepe.kafe.dto;

import java.time.LocalDate;
import java.util.List;

public class YogunlukHaftasi {

    private final LocalDate haftaBaslangici;
    private final List<YogunlukSatiri> satirlar;
    private final List<Integer> saatler;
    private final int toplamSiparis;
    private final int maxSiparis;
    private final LocalDate enYogunGun;

    public YogunlukHaftasi(LocalDate haftaBaslangici, List<YogunlukSatiri> satirlar, List<Integer> saatler,
                           int toplamSiparis, int maxSiparis, LocalDate enYogunGun) {
        this.haftaBaslangici = haftaBaslangici;
        this.satirlar = satirlar;
        this.saatler = saatler;
        this.toplamSiparis = toplamSiparis;
        this.maxSiparis = maxSiparis;
        this.enYogunGun = enYogunGun;
    }

    public LocalDate getHaftaBaslangici() {
        return haftaBaslangici;
    }

    public LocalDate getHaftaBitisi() {
        return haftaBaslangici.plusDays(6);
    }

    public List<YogunlukSatiri> getSatirlar() {
        return satirlar;
    }

    public List<Integer> getSaatler() {
        return saatler;
    }

    public int getToplamSiparis() {
        return toplamSiparis;
    }

    public int getMaxSiparis() {
        return maxSiparis;
    }

    public LocalDate getEnYogunGun() {
        return enYogunGun;
    }

    public int getGunlukOrtalama() {
        return satirlar.isEmpty() ? 0 : toplamSiparis / satirlar.size();
    }
}
