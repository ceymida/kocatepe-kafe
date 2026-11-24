package com.kocatepe.kafe.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.ForecastNoktasi;
import com.kocatepe.kafe.entity.YogunlukKaydi;
import com.kocatepe.kafe.repository.YogunlukKaydiRepository;

/**
 * Son N haftanin ayni gun-saat kayitlarinin, yeni haftalara daha fazla agirlik veren
 * hareketli ortalamasi. Karmasik ML yoktur.
 *
 * Agirlik: 1 hafta once -> N, N hafta once -> 1.
 */
@Service
public class WeightedMovingAverageForecastService implements ForecastService {

    private final YogunlukKaydiRepository yogunlukRepository;
    private final AppProperties properties;

    public WeightedMovingAverageForecastService(YogunlukKaydiRepository yogunlukRepository,
                                                AppProperties properties) {
        this.yogunlukRepository = yogunlukRepository;
        this.properties = properties;
    }

    @Override
    public List<ForecastNoktasi> tahminEt(LocalDate baslangic, LocalDate bitis) {
        int haftaSayisi = properties.getTahminHaftaSayisi();
        LocalDate gecmisBaslangic = baslangic.minusWeeks(haftaSayisi).minusDays(1);
        LocalDate gecmisBitis = bitis.minusDays(1);

        List<YogunlukKaydi> gecmis = gecmisBitis.isBefore(gecmisBaslangic)
                ? List.of()
                : yogunlukRepository.findByTarihBetweenOrderByTarihAscSaatAsc(gecmisBaslangic, gecmisBitis);

        // (gun, saat) -> tarih -> siparis
        Map<Integer, Map<Integer, Map<LocalDate, Integer>>> indeks = new HashMap<>();
        double toplamSiparis = 0;
        int toplamKayit = 0;
        for (YogunlukKaydi kayit : gecmis) {
            indeks.computeIfAbsent(kayit.getTarih().getDayOfWeek().getValue(), k -> new HashMap<>())
                    .computeIfAbsent(kayit.getSaat(), k -> new HashMap<>())
                    .put(kayit.getTarih(), kayit.getSiparisSayisi());
            toplamSiparis += kayit.getSiparisSayisi();
            toplamKayit++;
        }
        double genelOrtalama = toplamKayit == 0 ? 20.0 : toplamSiparis / toplamKayit;

        List<ForecastNoktasi> sonuc = new ArrayList<>();
        for (LocalDate tarih = baslangic; !tarih.isAfter(bitis); tarih = tarih.plusDays(1)) {
            for (int saat = properties.getOpeningHour(); saat < properties.getClosingHour(); saat++) {
                Map<LocalDate, Integer> ayniGunSaat = indeks
                        .getOrDefault(tarih.getDayOfWeek().getValue(), Map.of())
                        .getOrDefault(saat, Map.of());

                double agirlikliToplam = 0;
                double agirlikToplam = 0;
                for (int hafta = 1; hafta <= haftaSayisi; hafta++) {
                    Integer siparis = ayniGunSaat.get(tarih.minusWeeks(hafta));
                    if (siparis != null) {
                        double agirlik = haftaSayisi - hafta + 1;
                        agirlikliToplam += siparis * agirlik;
                        agirlikToplam += agirlik;
                    }
                }
                double tahmin = agirlikToplam > 0 ? agirlikliToplam / agirlikToplam : genelOrtalama;
                sonuc.add(new ForecastNoktasi(tarih, saat, Math.round(tahmin * 10.0) / 10.0));
            }
        }
        return sonuc;
    }
}
