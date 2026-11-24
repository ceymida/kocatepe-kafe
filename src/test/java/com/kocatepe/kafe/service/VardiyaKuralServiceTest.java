package com.kocatepe.kafe.service;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Pozisyon;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaDurum;
import com.kocatepe.kafe.entity.VardiyaTipi;

class VardiyaKuralServiceTest {

    private final VardiyaKuralService service = new VardiyaKuralService(null, new AppProperties());

    @Test
    void tamZamanliIzinGununeVardiyaAtanamaz() {
        LocalDate pazartesi = LocalDate.of(2026, 11, 23);
        Personel personel = personel(1L, "Emre", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        List<Musaitlik> musaitlikler = musaitlikler(personel, DayOfWeek.MONDAY, false);

        KuralSonucu sonuc = service.kontrol(personel, pazartesi, VardiyaTipi.SABAH,
                List.of(), musaitlikler);

        assertThat(sonuc.gecerli()).isFalse();
        assertThat(sonuc.hatalar()).anyMatch(h -> h.contains("izin günüdür"));
        assertThat(sonuc.hatalar()).hasSize(1);
    }

    @Test
    void partTimeMusaitOlmayanGuneAtanamaz() {
        LocalDate cumartesi = LocalDate.of(2026, 11, 28);
        Personel personel = personel(2L, "Ceren", CalismaTipi.PART_TIME, null, 20);
        List<Musaitlik> musaitlikler = musaitlikler(personel, DayOfWeek.SATURDAY, false);

        KuralSonucu sonuc = service.kontrol(personel, cumartesi, VardiyaTipi.AKSAM,
                List.of(), musaitlikler);

        assertThat(sonuc.gecerli()).isFalse();
        assertThat(sonuc.hatalar()).anyMatch(h -> h.contains("müsait değil"));
    }

    @Test
    void ayniGuneIkinciVardiyaAtanamaz() {
        LocalDate cumartesi = LocalDate.of(2026, 11, 28);
        Personel personel = personel(3L, "Derya", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        Vardiya mevcut = new Vardiya(personel, cumartesi, VardiyaTipi.SABAH, VardiyaDurum.TASLAK);

        KuralSonucu sonuc = service.kontrol(personel, cumartesi, VardiyaTipi.AKSAM,
                List.of(mevcut), musaitlikler(personel, DayOfWeek.SATURDAY, true));

        assertThat(sonuc.gecerli()).isFalse();
        assertThat(sonuc.hatalar()).anyMatch(h -> h.contains("aynı gün zaten bir vardiya"));
    }

    @Test
    void haftalikLimitAsimiHataDegilUyariOlarakDoner() {
        Personel personel = personel(4L, "Melis", CalismaTipi.PART_TIME, null, 16);
        List<Musaitlik> musaitlikler = musaitlikler(personel, DayOfWeek.THURSDAY, true);

        Vardiya sali = new Vardiya(personel, LocalDate.of(2026, 11, 24), VardiyaTipi.SABAH, VardiyaDurum.TASLAK);
        Vardiya carsamba = new Vardiya(personel, LocalDate.of(2026, 11, 25), VardiyaTipi.SABAH, VardiyaDurum.TASLAK);

        KuralSonucu sonuc = service.kontrol(personel, LocalDate.of(2026, 11, 26), VardiyaTipi.SABAH,
                List.of(sali, carsamba), musaitlikler);

        assertThat(sonuc.gecerli()).isTrue();
        assertThat(sonuc.uyarilar()).anyMatch(u -> u.contains("haftalık limiti aşıyor"));
    }

    @Test
    void tamZamanliNormalGuneAtamaGecebilir() {
        Personel personel = personel(5L, "Selin", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        KuralSonucu sonuc = service.kontrol(personel, LocalDate.of(2026, 11, 24), VardiyaTipi.SABAH,
                List.of(), musaitlikler(personel, DayOfWeek.TUESDAY, true));

        assertThat(sonuc.gecerli()).isTrue();
        assertThat(sonuc.uyarilar()).isEmpty();
    }

    private Personel personel(Long id, String ad, CalismaTipi tip, DayOfWeek off, int maxSaat) {
        Personel personel = new Personel(ad, Pozisyon.BARISTA, tip, off, maxSaat);
        personel.setId(id);
        return personel;
    }

    private List<Musaitlik> musaitlikler(Personel personel, DayOfWeek gun, boolean musait) {
        List<Musaitlik> liste = new ArrayList<>();
        for (DayOfWeek d : DayOfWeek.values()) {
            liste.add(new Musaitlik(personel, d, d == gun && musait));
        }
        return liste;
    }
}
