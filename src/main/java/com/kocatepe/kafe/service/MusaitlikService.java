package com.kocatepe.kafe.service;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.repository.MusaitlikRepository;

@Service
public class MusaitlikService {

    private final MusaitlikRepository musaitlikRepository;

    public MusaitlikService(MusaitlikRepository musaitlikRepository) {
        this.musaitlikRepository = musaitlikRepository;
    }

    public Map<DayOfWeek, Boolean> musaitlikHaritasi(Personel personel) {
        List<Musaitlik> kayitlar = musaitlikRepository.findByPersonelId(personel.getId());
        Map<DayOfWeek, Boolean> sonuc = new LinkedHashMap<>();
        for (DayOfWeek gun : DayOfWeek.values()) {
            boolean musait = kayitlar.stream()
                    .filter(m -> m.getGun() == gun)
                    .map(Musaitlik::isMusait)
                    .findFirst()
                    .orElse(false);
            sonuc.put(gun, musait);
        }
        return sonuc;
    }

    @Transactional
    public void guncelle(Personel personel, List<DayOfWeek> musaitGunler) {
        List<DayOfWeek> secilen = musaitGunler == null ? List.of() : musaitGunler;
        List<Musaitlik> kayitlar = new ArrayList<>(musaitlikRepository.findByPersonelId(personel.getId()));
        for (DayOfWeek gun : DayOfWeek.values()) {
            boolean musait = secilen.contains(gun);
            Musaitlik kayit = kayitlar.stream()
                    .filter(m -> m.getGun() == gun)
                    .findFirst()
                    .orElse(null);
            if (kayit == null) {
                kayit = new Musaitlik(personel, gun, musait);
                kayitlar.add(kayit);
            } else {
                kayit.setMusait(musait);
            }
        }
        musaitlikRepository.saveAll(kayitlar);
    }
}
