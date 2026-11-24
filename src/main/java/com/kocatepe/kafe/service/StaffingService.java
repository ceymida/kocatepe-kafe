package com.kocatepe.kafe.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.ForecastNoktasi;
import com.kocatepe.kafe.dto.IhtiyacDurumu;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaTipi;

/**
 * Tahmini siparis sayisindan saatlik personel ihtiyaci uretir.
 * Katsayi ve minimum personel sayisi config'den gelir.
 */
@Service
public class StaffingService {

    private final ForecastService forecastService;
    private final AppProperties properties;

    public StaffingService(ForecastService forecastService, AppProperties properties) {
        this.forecastService = forecastService;
        this.properties = properties;
    }

    public int saatlikIhtiyac(double tahminiSiparis) {
        int hesaplanan = (int) Math.ceil(tahminiSiparis / properties.getSiparisBasinaPersonel());
        return Math.max(properties.getMinPersonelSaat(), hesaplanan);
    }

    /** gun -> saat -> gerekli personel */
    public Map<LocalDate, Map<Integer, Integer>> saatlikIhtiyacHaritasi(LocalDate baslangic, LocalDate bitis) {
        List<ForecastNoktasi> tahminler = forecastService.tahminEt(baslangic, bitis);
        Map<LocalDate, Map<Integer, Integer>> sonuc = new HashMap<>();
        for (ForecastNoktasi tahmin : tahminler) {
            sonuc.computeIfAbsent(tahmin.tarih(), k -> new HashMap<>())
                    .put(tahmin.saat(), saatlikIhtiyac(tahmin.tahminiSiparis()));
        }
        return sonuc;
    }

    /**
     * Haftadaki her gun ve vardiya tipi icin, o vardiya araligindaki en yuksek saatlik ihtiyaci
     * baz alarak kac personel gerektigini hesaplar ve mevcut vardiya sayisiyla karsilastirir.
     */
    public Map<String, IhtiyacDurumu> vardiyaIhtiyaclari(LocalDate haftaBaslangici, List<Vardiya> haftaVardiyalari) {
        Map<LocalDate, Map<Integer, Integer>> ihtiyaclar = saatlikIhtiyacHaritasi(
                haftaBaslangici, haftaBaslangici.plusDays(6));

        Map<String, IhtiyacDurumu> sonuc = new HashMap<>();
        for (int gun = 0; gun < 7; gun++) {
            LocalDate tarih = haftaBaslangici.plusDays(gun);
            for (VardiyaTipi tip : VardiyaTipi.values()) {
                int gerekli = 0;
                for (int saat = tip.getBaslangic().getHour(); saat < tip.getBitis().getHour(); saat++) {
                    gerekli = Math.max(gerekli, ihtiyaclar
                            .getOrDefault(tarih, Map.of())
                            .getOrDefault(saat, properties.getMinPersonelSaat()));
                }
                int atanan = (int) haftaVardiyalari.stream()
                        .filter(v -> v.getTarih().equals(tarih) && v.getVardiyaTipi() == tip)
                        .count();
                sonuc.put(anahtar(tarih, tip), new IhtiyacDurumu(tarih, tip, gerekli, atanan));
            }
        }
        return sonuc;
    }

    public static String anahtar(LocalDate tarih, VardiyaTipi tip) {
        return tarih + "_" + tip.name();
    }
}
