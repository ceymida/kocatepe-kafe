package com.kocatepe.kafe.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.dto.IhtiyacDurumu;
import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.dto.VardiyaHaftasi;
import com.kocatepe.kafe.dto.VardiyaSatiri;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaDurum;
import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.repository.PersonelRepository;
import com.kocatepe.kafe.repository.VardiyaRepository;

@Service
public class VardiyaService {

    private final VardiyaRepository vardiyaRepository;
    private final PersonelRepository personelRepository;
    private final PersonelService personelService;
    private final VardiyaKuralService kuralService;
    private final StaffingService staffingService;

    public VardiyaService(VardiyaRepository vardiyaRepository, PersonelRepository personelRepository,
                          PersonelService personelService, VardiyaKuralService kuralService,
                          StaffingService staffingService) {
        this.vardiyaRepository = vardiyaRepository;
        this.personelRepository = personelRepository;
        this.personelService = personelService;
        this.kuralService = kuralService;
        this.staffingService = staffingService;
    }

    public LocalDate haftaBaslangici(LocalDate herhangiGun) {
        return TarihUtil.haftaBaslangici(herhangiGun);
    }

    public VardiyaHaftasi hafta(LocalDate herhangiGun) {
        LocalDate baslangic = haftaBaslangici(herhangiGun);
        List<Vardiya> vardiyalar = vardiyaRepository
                .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(baslangic, baslangic.plusDays(6));

        // Aktif personel + bu hafta vardiyasi olan pasif personel tabloda gorunsun.
        Map<Long, Personel> personeller = new LinkedHashMap<>();
        for (Personel personel : personelService.aktifler()) {
            personeller.put(personel.getId(), personel);
        }
        for (Vardiya vardiya : vardiyalar) {
            personeller.putIfAbsent(vardiya.getPersonel().getId(), vardiya.getPersonel());
        }

        Map<Long, List<Vardiya>> personelVardiyalari = new HashMap<>();
        for (Vardiya vardiya : vardiyalar) {
            personelVardiyalari.computeIfAbsent(vardiya.getPersonel().getId(), k -> new ArrayList<>())
                    .add(vardiya);
        }

        List<VardiyaSatiri> satirlar = new ArrayList<>();
        for (Personel personel : personeller.values()) {
            List<Vardiya> kisiVardiyalari = personelVardiyalari.getOrDefault(personel.getId(), List.of());
            Map<LocalDate, Vardiya> gunluk = new LinkedHashMap<>();
            for (Vardiya vardiya : kisiVardiyalari) {
                gunluk.put(vardiya.getTarih(), vardiya);
            }
            int toplamSaat = kuralService.haftalikSaat(personel, kisiVardiyalari);
            List<String> uyarilar = new ArrayList<>();
            if (toplamSaat > personel.getHaftalikMaxSaat()) {
                uyarilar.add("Haftalık limit asildi (" + toplamSaat + "/" + personel.getHaftalikMaxSaat() + " saat).");
            }
            satirlar.add(new VardiyaSatiri(personel, gunluk, uyarilar, toplamSaat, kisiVardiyalari.size()));
        }
        return haftaOlustur(baslangic, vardiyalar, satirlar);
    }

    public VardiyaHaftasi haftaCalisan(LocalDate herhangiGun, Personel personel) {
        LocalDate baslangic = haftaBaslangici(herhangiGun);
        List<Vardiya> vardiyalar = vardiyaRepository
                .findByPersonelIdAndTarihBetweenOrderByTarihAsc(personel.getId(), baslangic, baslangic.plusDays(6));
        Map<LocalDate, Vardiya> gunluk = new LinkedHashMap<>();
        for (Vardiya vardiya : vardiyalar) {
            gunluk.put(vardiya.getTarih(), vardiya);
        }
        int toplamSaat = kuralService.haftalikSaat(personel, vardiyalar);
        List<String> uyarilar = new ArrayList<>();
        if (toplamSaat > personel.getHaftalikMaxSaat()) {
            uyarilar.add("Haftalık limit asildi (" + toplamSaat + "/" + personel.getHaftalikMaxSaat() + " saat).");
        }
        VardiyaSatiri satir = new VardiyaSatiri(personel, gunluk, uyarilar, toplamSaat, vardiyalar.size());
        return haftaOlustur(baslangic, vardiyalar, List.of(satir));
    }

    private VardiyaHaftasi haftaOlustur(LocalDate baslangic, List<Vardiya> vardiyalar, List<VardiyaSatiri> satirlar) {
        List<LocalDate> gunler = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            gunler.add(baslangic.plusDays(i));
        }
        Map<String, IhtiyacDurumu> ihtiyaclar = staffingService.vardiyaIhtiyaclari(baslangic, vardiyalar);
        Map<LocalDate, IhtiyacDurumu> sabah = new LinkedHashMap<>();
        Map<LocalDate, IhtiyacDurumu> aksam = new LinkedHashMap<>();
        for (LocalDate gun : gunler) {
            sabah.put(gun, ihtiyaclar.get(StaffingService.anahtar(gun, VardiyaTipi.SABAH)));
            aksam.put(gun, ihtiyaclar.get(StaffingService.anahtar(gun, VardiyaTipi.AKSAM)));
        }
        int yayindaki = (int) vardiyalar.stream().filter(v -> v.getDurum() == VardiyaDurum.YAYINDA).count();
        return new VardiyaHaftasi(baslangic, gunler, satirlar, ihtiyaclar, sabah, aksam,
                vardiyalar.size(), yayindaki);
    }

    @Transactional
    public KuralSonucu hucreKaydet(Long personelId, LocalDate tarih, VardiyaTipi tip) {
        Personel personel = personelRepository.findById(personelId)
                .orElseThrow(() -> new IllegalArgumentException("Personel bulunamadı: " + personelId));
        Optional<Vardiya> mevcut = vardiyaRepository.findByPersonelAndTarih(personel, tarih);

        if (tip == null) {
            mevcut.ifPresent(vardiyaRepository::delete);
            return KuralSonucu.bos();
        }

        if (mevcut.isPresent() && mevcut.get().getVardiyaTipi() == tip) {
            return KuralSonucu.bos();
        }

        LocalDate haftaBas = haftaBaslangici(tarih);
        List<Vardiya> haftaVardiyalari = vardiyaRepository
                .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(haftaBas, haftaBas.plusDays(6));
        // Ayni gun mevcut kaydi kural kontrolunden cikar (guncelleme senaryosu).
        List<Vardiya> digerleri = haftaVardiyalari.stream()
                .filter(v -> !(v.getPersonel().getId().equals(personelId) && v.getTarih().equals(tarih)))
                .toList();

        KuralSonucu sonuc = kuralService.kontrol(personel, tarih, tip, digerleri);
        if (!sonuc.gecerli()) {
            return sonuc;
        }

        if (mevcut.isPresent()) {
            Vardiya vardiya = mevcut.get();
            vardiya.setVardiyaTipi(tip);
            vardiyaRepository.save(vardiya);
        } else {
            vardiyaRepository.save(new Vardiya(personel, tarih, tip, VardiyaDurum.TASLAK));
        }
        return sonuc;
    }

    /**
     * Haftadaki mevcut vardiyalari mevcut musaitlik/izin durumuna gore yeniden dogrular.
     * Yayinlama oncesi kullanilir; yalnizca gercek hatalar yayinlamayi engeller.
     */
    public KuralSonucu haftaKurallari(LocalDate herhangiGun) {
        LocalDate baslangic = haftaBaslangici(herhangiGun);
        List<Vardiya> vardiyalar = vardiyaRepository
                .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(baslangic, baslangic.plusDays(6));
        List<String> hatalar = new ArrayList<>();
        List<String> uyarilar = new ArrayList<>();
        for (Vardiya vardiya : vardiyalar) {
            List<Vardiya> digerleri = vardiyalar.stream()
                    .filter(v -> !v.getId().equals(vardiya.getId()))
                    .toList();
            KuralSonucu sonuc = kuralService.kontrol(
                    vardiya.getPersonel(), vardiya.getTarih(), vardiya.getVardiyaTipi(), digerleri);
            hatalar.addAll(sonuc.hatalar());
            uyarilar.addAll(sonuc.uyarilar());
        }
        return new KuralSonucu(hatalar, uyarilar);
    }

    @Transactional
    public int yayinla(LocalDate herhangiGun) {
        LocalDate baslangic = haftaBaslangici(herhangiGun);
        List<Vardiya> vardiyalar = vardiyaRepository
                .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(baslangic, baslangic.plusDays(6));
        int sayi = 0;
        for (Vardiya vardiya : vardiyalar) {
            if (vardiya.getDurum() != VardiyaDurum.YAYINDA) {
                vardiya.setDurum(VardiyaDurum.YAYINDA);
                sayi++;
            }
        }
        return sayi;
    }

    public List<Vardiya> calisanYaklasanVardiyalar(Personel personel, LocalDate bugun) {
        return vardiyaRepository.findByPersonelIdAndTarihGreaterThanEqualAndDurumOrderByTarihAsc(
                personel.getId(), bugun, VardiyaDurum.YAYINDA);
    }
}
