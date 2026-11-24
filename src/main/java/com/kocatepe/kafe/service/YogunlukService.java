package com.kocatepe.kafe.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.ForecastNoktasi;
import com.kocatepe.kafe.dto.TahminHaftasi;
import com.kocatepe.kafe.dto.TahminHucre;
import com.kocatepe.kafe.dto.TahminSatiri;
import com.kocatepe.kafe.dto.YogunlukHaftasi;
import com.kocatepe.kafe.dto.YogunlukHucre;
import com.kocatepe.kafe.dto.YogunlukSatiri;
import com.kocatepe.kafe.entity.YogunlukKaydi;
import com.kocatepe.kafe.repository.YogunlukKaydiRepository;

@Service
public class YogunlukService {

    private final YogunlukKaydiRepository yogunlukRepository;
    private final ForecastService forecastService;
    private final StaffingService staffingService;
    private final AppProperties properties;

    public YogunlukService(YogunlukKaydiRepository yogunlukRepository, ForecastService forecastService,
                           StaffingService staffingService, AppProperties properties) {
        this.yogunlukRepository = yogunlukRepository;
        this.forecastService = forecastService;
        this.staffingService = staffingService;
        this.properties = properties;
    }

    public List<Integer> saatler() {
        List<Integer> saatler = new ArrayList<>();
        for (int saat = properties.getOpeningHour(); saat < properties.getClosingHour(); saat++) {
            saatler.add(saat);
        }
        return saatler;
    }

    public YogunlukHaftasi hafta(LocalDate herhangiGun) {
        LocalDate baslangic = TarihUtil.haftaBaslangici(herhangiGun);
        LocalDate bitis = baslangic.plusDays(6);
        List<YogunlukKaydi> kayitlar = yogunlukRepository
                .findByTarihBetweenOrderByTarihAscSaatAsc(baslangic, bitis);

        Map<LocalDate, Map<Integer, YogunlukKaydi>> indeks = new HashMap<>();
        int max = 0;
        int toplam = 0;
        for (YogunlukKaydi kayit : kayitlar) {
            indeks.computeIfAbsent(kayit.getTarih(), k -> new HashMap<>()).put(kayit.getSaat(), kayit);
            max = Math.max(max, kayit.getSiparisSayisi());
            toplam += kayit.getSiparisSayisi();
        }

        List<YogunlukSatiri> satirlar = new ArrayList<>();
        LocalDate enYogunGun = null;
        int enYogunToplam = -1;
        for (int i = 0; i < 7; i++) {
            LocalDate tarih = baslangic.plusDays(i);
            Map<Integer, YogunlukKaydi> gunKayitlari = indeks.getOrDefault(tarih, Map.of());
            List<YogunlukHucre> hucreler = new ArrayList<>();
            int gunToplam = 0;
            for (int saat : saatler()) {
                YogunlukKaydi kayit = gunKayitlari.get(saat);
                int siparis = kayit == null ? 0 : kayit.getSiparisSayisi();
                int doluMasa = kayit == null ? 0 : kayit.getDoluMasa();
                hucreler.add(new YogunlukHucre(saat, siparis, doluMasa, max));
                gunToplam += siparis;
            }
            satirlar.add(new YogunlukSatiri(tarih, hucreler, gunToplam));
            if (gunToplam > enYogunToplam) {
                enYogunToplam = gunToplam;
                enYogunGun = tarih;
            }
        }
        return new YogunlukHaftasi(baslangic, satirlar, saatler(), toplam, max, enYogunGun);
    }

    public TahminHaftasi tahminHaftasi(LocalDate herhangiGun) {
        LocalDate baslangic = TarihUtil.haftaBaslangici(herhangiGun);
        LocalDate bitis = baslangic.plusDays(6);
        List<ForecastNoktasi> tahminler = forecastService.tahminEt(baslangic, bitis);

        Map<LocalDate, List<TahminHucre>> indeks = new HashMap<>();
        double toplam = 0;
        for (ForecastNoktasi tahmin : tahminler) {
            int gerekli = staffingService.saatlikIhtiyac(tahmin.tahminiSiparis());
            indeks.computeIfAbsent(tahmin.tarih(), k -> new ArrayList<>())
                    .add(new TahminHucre(tahmin.saat(), tahmin.tahminiSiparis(), gerekli));
            toplam += tahmin.tahminiSiparis();
        }

        List<TahminSatiri> satirlar = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            LocalDate tarih = baslangic.plusDays(i);
            satirlar.add(new TahminSatiri(tarih, indeks.getOrDefault(tarih, List.of())));
        }
        return new TahminHaftasi(baslangic, satirlar, saatler(), Math.round(toplam * 10.0) / 10.0);
    }
}
