package com.kocatepe.kafe.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.entity.YogunlukKaydi;
import com.kocatepe.kafe.repository.YogunlukKaydiRepository;

/**
 * ORNEK (sentetik) yogunluk verisi uretir.
 * Gercek veri geldiginde bu sinif devre disi kalir; CSV import servisi ayni tabloyu doldurur.
 * Sabit seed sayesinde ayni tarih araligi icin tekrar uretilebilir sonuc verir.
 */
@Service
public class SyntheticDataGenerator {

    private static final int[] HAFTA_ICI_TEMEL = {
            0, 0, 0, 0, 0, 0, 0, 0, 0,
            9,   // 09
            13,  // 10
            22,  // 11
            40,  // 12
            36,  // 13
            23,  // 14
            14,  // 15
            16,  // 16
            29,  // 17
            40,  // 18
            38,  // 19
            29,  // 20
            18,  // 21
            9    // 22
    };

    private final YogunlukKaydiRepository yogunlukRepository;
    private final AppProperties properties;

    public SyntheticDataGenerator(YogunlukKaydiRepository yogunlukRepository, AppProperties properties) {
        this.yogunlukRepository = yogunlukRepository;
        this.properties = properties;
    }

    @Transactional
    public int generateIfEmpty(LocalDate bugun) {
        LocalDate baslangic = bugun.minusWeeks(properties.getSentetikHaftaSayisi()).plusDays(1);
        if (yogunlukRepository.countByTarihBetween(baslangic, bugun) > 0) {
            return 0;
        }
        return generate(baslangic, bugun, properties.getSentetikSeed());
    }

    @Transactional
    public int generate(LocalDate baslangic, LocalDate bitis, long seed) {
        List<YogunlukKaydi> kayitlar = new ArrayList<>();
        for (LocalDate tarih = baslangic; !tarih.isAfter(bitis); tarih = tarih.plusDays(1)) {
            // Her gun icin tarihe bagli seed: uygulama yeniden baslasa da desen bozulmaz.
            Random random = new Random(seed * 31 + tarih.toEpochDay());
            boolean haftaSonu = tarih.getDayOfWeek() == DayOfWeek.SATURDAY
                    || tarih.getDayOfWeek() == DayOfWeek.SUNDAY;
            double gunCarpani = haftaSonu ? 1.5 : 1.0;

            // Ara sira sapmalar: sakin gun (%6) veya cok yogun gun (%6)
            double sapma = 1.0;
            double sapmaZar = random.nextDouble();
            if (sapmaZar < 0.06) {
                sapma = 0.65;
            } else if (sapmaZar > 0.94) {
                sapma = 1.35;
            }

            for (int saat = properties.getOpeningHour(); saat < properties.getClosingHour(); saat++) {
                double temel = HAFTA_ICI_TEMEL[saat];
                double carpan = gunCarpani * sapma;

                // Cuma aksami daha yogun
                if (tarih.getDayOfWeek() == DayOfWeek.FRIDAY && saat >= 17) {
                    carpan *= 1.3;
                }

                // +-%18 rastgele gurultu
                double gurultu = 1.0 + (random.nextDouble() * 0.36 - 0.18);
                int siparis = (int) Math.round(temel * carpan * gurultu);
                siparis = Math.max(5, Math.min(60, siparis));

                int doluMasa = (int) Math.round((siparis / 60.0 * properties.getToplamMasa())
                        * (0.8 + random.nextDouble() * 0.4));
                doluMasa = Math.max(0, Math.min(properties.getToplamMasa(), doluMasa));

                kayitlar.add(new YogunlukKaydi(tarih, saat, siparis, doluMasa));
            }
        }
        yogunlukRepository.saveAll(kayitlar);
        return kayitlar.size();
    }
}
