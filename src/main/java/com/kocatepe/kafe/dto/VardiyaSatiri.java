package com.kocatepe.kafe.dto;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Vardiya;

public class VardiyaSatiri {

    private final Personel personel;
    private final Map<LocalDate, Vardiya> vardiyalar = new LinkedHashMap<>();
    private final List<String> uyarilar;
    private final int toplamSaat;
    private final int vardiyaSayisi;

    public VardiyaSatiri(Personel personel, Map<LocalDate, Vardiya> vardiyalar,
                         List<String> uyarilar, int toplamSaat, int vardiyaSayisi) {
        this.personel = personel;
        if (vardiyalar != null) {
            this.vardiyalar.putAll(vardiyalar);
        }
        this.uyarilar = uyarilar;
        this.toplamSaat = toplamSaat;
        this.vardiyaSayisi = vardiyaSayisi;
    }

    public Personel getPersonel() {
        return personel;
    }

    public Map<LocalDate, Vardiya> getVardiyalar() {
        return vardiyalar;
    }

    public Vardiya vardiya(LocalDate gun) {
        return vardiyalar.get(gun);
    }

    public List<String> getUyarilar() {
        return uyarilar;
    }

    public int getToplamSaat() {
        return toplamSaat;
    }

    public int getVardiyaSayisi() {
        return vardiyaSayisi;
    }

    public boolean isLimitAsildi() {
        return toplamSaat > personel.getHaftalikMaxSaat();
    }
}
