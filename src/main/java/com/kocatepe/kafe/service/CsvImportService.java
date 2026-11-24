package com.kocatepe.kafe.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.CsvImportResult;
import com.kocatepe.kafe.entity.YogunlukKaydi;
import com.kocatepe.kafe.repository.YogunlukKaydiRepository;

/**
 * Gercek yogunluk verisi icin CSV import.
 * Beklenen format: tarih,saat,siparisSayisi,doluMasa
 * Ornek: 2026-11-20,14,42,18
 * Baslik satiri opsiyoneldir; noktali virgul ve sekmeli ayrim da desteklenir.
 */
@Service
public class CsvImportService {

    private static final List<DateTimeFormatter> TARIH_FORMATLARI = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE,
            DateTimeFormatter.ofPattern("dd.MM.yyyy"),
            DateTimeFormatter.ofPattern("dd/MM/yyyy"),
            DateTimeFormatter.ofPattern("yyyy/MM/dd")
    );

    private final YogunlukKaydiRepository yogunlukRepository;
    private final AppProperties properties;

    public CsvImportService(YogunlukKaydiRepository yogunlukRepository, AppProperties properties) {
        this.yogunlukRepository = yogunlukRepository;
        this.properties = properties;
    }

    @Transactional
    public CsvImportResult importEt(MultipartFile dosya) {
        CsvImportResult sonuc = new CsvImportResult();
        if (dosya == null || dosya.isEmpty()) {
            sonuc.getHatalar().add("Dosya boş.");
            return sonuc;
        }

        try (BufferedReader reader = new BufferedReader(
                new InputStreamReader(dosya.getInputStream(), StandardCharsets.UTF_8))) {
            String satir;
            int satirNo = 0;
            while ((satir = reader.readLine()) != null) {
                satirNo++;
                if (satir.isBlank()) {
                    continue;
                }
                if (satir.startsWith("\uFEFF")) {
                    satir = satir.substring(1);
                }
                String[] parcalar = satir.split("[;,\\t]");
                if (parcalar.length < 4) {
                    sonuc.getHatalar().add(satirNo + ". satır: en az 4 alan bekleniyor.");
                    sonuc.setAtlanan(sonuc.getAtlanan() + 1);
                    continue;
                }
                String tarihMetni = parcalar[0].trim();
                if (tarihMetni.toLowerCase(Locale.ROOT).contains("tarih")) {
                    continue; // baslik satiri
                }
                try {
                    LocalDate tarih = tarihParse(tarihMetni);
                    int saat = Integer.parseInt(parcalar[1].trim());
                    int siparis = Integer.parseInt(parcalar[2].trim());
                    int doluMasa = Integer.parseInt(parcalar[3].trim());
                    if (saat < properties.getOpeningHour() || saat >= properties.getClosingHour()) {
                        throw new IllegalArgumentException("Saat çalışma aralığı dışında");
                    }
                    if (siparis < 0) {
                        throw new IllegalArgumentException("Sipariş sayısı negatif olamaz");
                    }
                    if (doluMasa < 0 || doluMasa > properties.getToplamMasa()) {
                        throw new IllegalArgumentException("Dolu masa 0-" + properties.getToplamMasa() + " aralığında olmalı");
                    }
                    YogunlukKaydi kayit = yogunlukRepository.findByTarihAndSaat(tarih, saat).orElse(null);
                    if (kayit == null) {
                        yogunlukRepository.save(new YogunlukKaydi(tarih, saat, siparis, doluMasa));
                        sonuc.setEklenen(sonuc.getEklenen() + 1);
                    } else {
                        kayit.setSiparisSayisi(siparis);
                        kayit.setDoluMasa(doluMasa);
                        sonuc.setGuncellenen(sonuc.getGuncellenen() + 1);
                    }
                } catch (IllegalArgumentException | DateTimeParseException ex) {
                    sonuc.getHatalar().add(satirNo + ". satır: " + ex.getMessage());
                    sonuc.setAtlanan(sonuc.getAtlanan() + 1);
                }
            }
        } catch (IOException ex) {
            sonuc.getHatalar().add("Dosya okunamadı: " + ex.getMessage());
        }
        return sonuc;
    }

    private LocalDate tarihParse(String metin) {
        for (DateTimeFormatter formatter : TARIH_FORMATLARI) {
            try {
                return LocalDate.parse(metin, formatter);
            } catch (DateTimeParseException ignored) {
                // sonraki formati dene
            }
        }
        throw new IllegalArgumentException("Tarih formatı anlaşılamadı: " + metin);
    }
}
