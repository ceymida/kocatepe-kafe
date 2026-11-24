package com.kocatepe.kafe.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.dto.IhtiyacDurumu;
import com.kocatepe.kafe.dto.OtoPlanSonucu;
import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Pozisyon;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaDurum;
import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.repository.MusaitlikRepository;
import com.kocatepe.kafe.repository.PersonelRepository;
import com.kocatepe.kafe.repository.VardiyaRepository;

/**
 * Basit acgozlu (greedy) sezgisel ile haftalik TASLAK vardiya uretir.
 *
 * Adimlar:
 * 1) Tahmini siparislerden gun/vardiya basina gerekli personel sayisini al.
 * 2) Mevcut YAYINDA vardiyalari sabit kabul et; yalnizca TASLAK kayitlari yeniden uret.
 * 3) Her gun ve vardiya icin:
 *    - once pozisyon dengesi (BARISTA, KASIYER, MUTFAK, STEWARD) icin birer uygun aday sec,
 *    - sonra kalan ihtiyaci en az dolu personelle doldur,
 *    - izin gunu, musaitlik, ayni gun ikinci vardiya ve haftalik limit tercihlerini uygula.
 * 4) Haftalik limit asimi yalnizca son care ise yapilir; sonucta eksikler raporlanir.
 */
@Service
public class AutoScheduleService {

    private static final List<Pozisyon> POZISYON_SIRASI =
            List.of(Pozisyon.BARISTA, Pozisyon.KASIYER, Pozisyon.MUTFAK, Pozisyon.STEWARD);

    private final PersonelRepository personelRepository;
    private final MusaitlikRepository musaitlikRepository;
    private final VardiyaRepository vardiyaRepository;
    private final StaffingService staffingService;

    public AutoScheduleService(PersonelRepository personelRepository, MusaitlikRepository musaitlikRepository,
                               VardiyaRepository vardiyaRepository, StaffingService staffingService) {
        this.personelRepository = personelRepository;
        this.musaitlikRepository = musaitlikRepository;
        this.vardiyaRepository = vardiyaRepository;
        this.staffingService = staffingService;
    }

    @Transactional
    public OtoPlanSonucu olustur(LocalDate herhangiGun) {
        LocalDate baslangic = TarihUtil.haftaBaslangici(herhangiGun);
        LocalDate bitis = baslangic.plusDays(6);

        // 1) Yalnizca taslaklar temizlenir, yayindaki vardiyalar korunur.
        List<Vardiya> eskiTaslaklar = vardiyaRepository
                .findByTarihBetweenAndDurumOrderByTarihAsc(baslangic, bitis, VardiyaDurum.TASLAK);
        vardiyaRepository.deleteAll(eskiTaslaklar);
        vardiyaRepository.flush();

        List<Vardiya> sabitVardiyalar = vardiyaRepository
                .findByTarihBetweenAndDurumOrderByTarihAsc(baslangic, bitis, VardiyaDurum.YAYINDA);

        List<Personel> personeller = personelRepository.findByAktifTrueOrderByAdSoyadAsc();
        Map<Long, Map<DayOfWeek, Boolean>> musaitlikler = new HashMap<>();
        for (Personel personel : personeller) {
            Map<DayOfWeek, Boolean> harita = new HashMap<>();
            for (Musaitlik musaitlik : musaitlikRepository.findByPersonelId(personel.getId())) {
                harita.put(musaitlik.getGun(), musaitlik.isMusait());
            }
            musaitlikler.put(personel.getId(), harita);
        }

        // Mevcut yayindaki vardiyalar: gun doluluk, haftalik saat ve vardiya sayilari.
        Map<Long, Set<LocalDate>> doluGunler = new HashMap<>();
        Map<Long, Integer> haftalikSaat = new HashMap<>();
        Map<String, List<Personel>> atananlar = new HashMap<>();
        for (Vardiya vardiya : sabitVardiyalar) {
            Long pid = vardiya.getPersonel().getId();
            doluGunler.computeIfAbsent(pid, k -> new HashSet<>()).add(vardiya.getTarih());
            haftalikSaat.merge(pid, vardiya.getVardiyaTipi().getSaat(), Integer::sum);
            atananlar.computeIfAbsent(anahtar(vardiya.getTarih(), vardiya.getVardiyaTipi()), k -> new ArrayList<>())
                    .add(vardiya.getPersonel());
        }

        // 2) Ihtiyaclar (yayindaki vardiyalar dusulmeden, toplam ihtiyac).
        Map<String, IhtiyacDurumu> ihtiyaclar = staffingService.vardiyaIhtiyaclari(baslangic, List.of());

        List<Vardiya> yeniVardiyalar = new ArrayList<>();
        List<String> eksikler = new ArrayList<>();

        for (int gunIndex = 0; gunIndex < 7; gunIndex++) {
            LocalDate tarih = baslangic.plusDays(gunIndex);
            for (VardiyaTipi tip : VardiyaTipi.values()) {
                String anahtar = anahtar(tarih, tip);
                List<Personel> vardiyadakiler = atananlar.getOrDefault(anahtar, new ArrayList<>());
                int hedef = ihtiyaclar.getOrDefault(anahtar,
                        new IhtiyacDurumu(tarih, tip, 2, 0)).getGerekli();

                // Ayni vardiyada mevcut pozisyonlar
                Set<Pozisyon> mevcutPozisyonlar = new HashSet<>();
                for (Personel p : vardiyadakiler) {
                    mevcutPozisyonlar.add(p.getPozisyon());
                }

                List<Personel> adaylar = new ArrayList<>(personeller);
                int pozisyonIndex = 0;
                while (vardiyadakiler.size() < hedef) {
                    Personel secilen = null;

                    // Once eksik pozisyonlari tamamlamaya calis.
                    while (pozisyonIndex < POZISYON_SIRASI.size()) {
                        Pozisyon pozisyon = POZISYON_SIRASI.get(pozisyonIndex);
                        if (mevcutPozisyonlar.contains(pozisyon)) {
                            pozisyonIndex++;
                            continue;
                        }
                        secilen = enUygunAday(adaylar, vardiyadakiler, pozisyon, tarih, tip,
                                doluGunler, haftalikSaat, musaitlikler);
                        if (secilen != null) {
                            break;
                        }
                        pozisyonIndex++;
                    }

                    if (secilen == null) {
                        secilen = enUygunAday(adaylar, vardiyadakiler, null, tarih, tip,
                                doluGunler, haftalikSaat, musaitlikler);
                    }
                    if (secilen == null) {
                        break;
                    }

                    Vardiya vardiya = new Vardiya(secilen, tarih, tip, VardiyaDurum.TASLAK);
                    yeniVardiyalar.add(vardiya);
                    vardiyadakiler.add(secilen);
                    mevcutPozisyonlar.add(secilen.getPozisyon());
                    doluGunler.computeIfAbsent(secilen.getId(), k -> new HashSet<>()).add(tarih);
                    haftalikSaat.merge(secilen.getId(), tip.getSaat(), Integer::sum);
                    if (secilen.getPozisyon() == POZISYON_SIRASI.get(Math.min(pozisyonIndex,
                            POZISYON_SIRASI.size() - 1))) {
                        pozisyonIndex++;
                    }
                }
                atananlar.put(anahtar, vardiyadakiler);
                if (vardiyadakiler.size() < hedef) {
                    eksikler.add(TarihUtil.gunAdi(tarih.getDayOfWeek()) + " " + tarih + " " + tip.getEtiket()
                            + ": " + (hedef - vardiyadakiler.size()) + " kişi eksik");
                }
            }
        }

        vardiyaRepository.saveAll(yeniVardiyalar);
        return new OtoPlanSonucu(yeniVardiyalar.size(), eksikler);
    }

    /**
     * Adaylari su oncelikle secer: haftalik limiti asmayanlar, en az yuklenmis olanlar.
     * Ayni gun baska vardiyasi olan veya o gun musait olmayan adaylar elenir.
     */
    private Personel enUygunAday(List<Personel> tumAdaylar, List<Personel> vardiyadakiler,
                                 Pozisyon istenenPozisyon, LocalDate tarih, VardiyaTipi tip,
                                 Map<Long, Set<LocalDate>> doluGunler,
                                 Map<Long, Integer> haftalikSaat,
                                 Map<Long, Map<DayOfWeek, Boolean>> musaitlikler) {
        return tumAdaylar.stream()
                .filter(p -> istenenPozisyon == null || p.getPozisyon() == istenenPozisyon)
                .filter(p -> vardiyadakiler.stream().noneMatch(v -> v.getId().equals(p.getId())))
                .filter(p -> !doluGunler.getOrDefault(p.getId(), Set.of()).contains(tarih))
                .filter(p -> musaitMi(p, tarih.getDayOfWeek(), musaitlikler))
                .filter(p -> p.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI
                        || haftalikSaat.getOrDefault(p.getId(), 0) + tip.getSaat() <= p.getHaftalikMaxSaat())
                .sorted(Comparator
                        .comparing((Personel p) -> haftalikSaat.getOrDefault(p.getId(), 0)
                                + tip.getSaat() > p.getHaftalikMaxSaat())
                        .thenComparingInt(p -> haftalikSaat.getOrDefault(p.getId(), 0))
                        .thenComparing(Personel::getId))
                .findFirst()
                .orElseGet(() -> tumAdaylar.stream()
                        // Hicbir aday limit icinde degilse, yine de en az yuklenmis adayi sec (uyari olarak kalir).
                        .filter(p -> istenenPozisyon == null || p.getPozisyon() == istenenPozisyon)
                        .filter(p -> vardiyadakiler.stream().noneMatch(v -> v.getId().equals(p.getId())))
                        .filter(p -> !doluGunler.getOrDefault(p.getId(), Set.of()).contains(tarih))
                        .filter(p -> musaitMi(p, tarih.getDayOfWeek(), musaitlikler))
                        .min(Comparator.comparingInt(p -> haftalikSaat.getOrDefault(p.getId(), 0)))
                        .orElse(null));
    }

    private boolean musaitMi(Personel personel, DayOfWeek gun,
                             Map<Long, Map<DayOfWeek, Boolean>> musaitlikler) {
        if (personel.getHaftalikOffGunu() != null && personel.getHaftalikOffGunu() == gun) {
            return false;
        }
        Boolean musait = musaitlikler.getOrDefault(personel.getId(), Map.of()).get(gun);
        if (musait != null) {
            return musait;
        }
        // Part-time icin musaitlik kaydi yoksa musait sayilmaz.
        return personel.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI;
    }

    private String anahtar(LocalDate tarih, VardiyaTipi tip) {
        return tarih + "_" + tip.name();
    }
}
