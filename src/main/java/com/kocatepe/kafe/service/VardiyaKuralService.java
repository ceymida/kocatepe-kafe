package com.kocatepe.kafe.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.repository.MusaitlikRepository;

/**
 * Vardiya atama kurallari.
 * Hatalar atamayi engeller, uyarilar yalnizca bilgilendirir (haftalik limit gibi).
 */
@Service
public class VardiyaKuralService {

    private final MusaitlikRepository musaitlikRepository;
    private final AppProperties properties;

    public VardiyaKuralService(MusaitlikRepository musaitlikRepository, AppProperties properties) {
        this.musaitlikRepository = musaitlikRepository;
        this.properties = properties;
    }

    public KuralSonucu kontrol(Personel personel, LocalDate tarih, VardiyaTipi tip, List<Vardiya> haftaVardiyalari) {
        List<Musaitlik> musaitlikler = musaitlikRepository.findByPersonelId(personel.getId());
        return kontrol(personel, tarih, tip, haftaVardiyalari, musaitlikler);
    }

    /** Test edilebilirlik icin musaitlik listesi disaridan verilebilir. */
    public KuralSonucu kontrol(Personel personel, LocalDate tarih, VardiyaTipi tip,
                               List<Vardiya> haftaVardiyalari, List<Musaitlik> musaitlikler) {
        List<String> hatalar = new ArrayList<>();
        List<String> uyarilar = new ArrayList<>();

        if (!personel.isAktif()) {
            hatalar.add(personel.getAdSoyad() + " pasif durumda, vardiya atanamaz.");
        }

        DayOfWeek gun = tarih.getDayOfWeek();

        boolean izinGunuHatasi = personel.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI
                && personel.getHaftalikOffGunu() != null
                && personel.getHaftalikOffGunu() == gun;
        if (izinGunuHatasi) {
            hatalar.add(personel.getAdSoyad() + " için " + TarihUtil.gunAdi(gun) + " izin günüdür.");
        }

        // Izin gunu hatasi zaten eklendiyse ayni gun icin ikinci bir "müsait değil" hatasi uretme.
        if (!izinGunuHatasi) {
            Optional<Musaitlik> musaitlik = musaitlikler.stream()
                    .filter(m -> m.getGun() == gun)
                    .findFirst();
            if (musaitlik.isPresent()) {
                if (!musaitlik.get().isMusait()) {
                    hatalar.add(personel.getAdSoyad() + " " + TarihUtil.gunAdi(gun) + " günü müsait değil.");
                }
            } else if (personel.getCalismaTipi() == CalismaTipi.PART_TIME) {
                hatalar.add(personel.getAdSoyad() + " için " + TarihUtil.gunAdi(gun) + " müsaitlik kaydı yok.");
            }
        }

        boolean ayniGunVardiya = haftaVardiyalari.stream()
                .anyMatch(v -> v.getPersonel().getId().equals(personel.getId())
                        && v.getTarih().equals(tarih));
        if (ayniGunVardiya) {
            hatalar.add(personel.getAdSoyad() + " için aynı gün zaten bir vardiya var.");
        }

        int mevcutSaat = haftaVardiyalari.stream()
                .filter(v -> v.getPersonel().getId().equals(personel.getId()))
                .mapToInt(v -> v.getVardiyaTipi().getSaat())
                .sum();
        int yeniToplam = mevcutSaat + tip.getSaat();
        if (yeniToplam > personel.getHaftalikMaxSaat()) {
            uyarilar.add(personel.getAdSoyad() + " haftalık limiti aşıyor: "
                    + yeniToplam + " saat > " + personel.getHaftalikMaxSaat() + " saat.");
        }

        return new KuralSonucu(hatalar, uyarilar);
    }

    public int haftalikSaat(Personel personel, List<Vardiya> haftaVardiyalari) {
        return haftaVardiyalari.stream()
                .filter(v -> v.getPersonel().getId().equals(personel.getId()))
                .mapToInt(v -> v.getVardiyaTipi().getSaat())
                .sum();
    }

    public AppProperties getProperties() {
        return properties;
    }
}
