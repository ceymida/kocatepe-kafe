package com.kocatepe.kafe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.EnumSet;
import java.util.Set;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Pozisyon;
import com.kocatepe.kafe.entity.TakasTalebi;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaDurum;
import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.repository.MusaitlikRepository;
import com.kocatepe.kafe.repository.PersonelRepository;
import com.kocatepe.kafe.repository.VardiyaRepository;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class TakasServiceTest {

    private static final LocalDate CUMARTESI = LocalDate.of(2026, 11, 28);

    @Autowired
    private TakasService takasService;

    @Autowired
    private PersonelRepository personelRepository;

    @Autowired
    private MusaitlikRepository musaitlikRepository;

    @Autowired
    private VardiyaRepository vardiyaRepository;

    @Test
    void onaylananTakastaIkiVardiyaYerDegistirir() {
        Personel emre = personel("Test Emre", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        Personel ceren = personel("Test Ceren", CalismaTipi.PART_TIME, null, 20);
        tamZamanliMusaitlik(emre, DayOfWeek.MONDAY);
        partTimeMusaitlik(ceren, DayOfWeek.SATURDAY);

        Vardiya emreVardiyasi = vardiyaRepository.save(
                new Vardiya(emre, CUMARTESI, VardiyaTipi.SABAH, VardiyaDurum.TASLAK));
        Vardiya cerenVardiyasi = vardiyaRepository.save(
                new Vardiya(ceren, CUMARTESI, VardiyaTipi.AKSAM, VardiyaDurum.TASLAK));

        TakasTalebi talep = takasService.talepOlustur(emre, emreVardiyasi.getId(), ceren.getId());
        assertThat(talep.getHedefVardiya()).isNotNull();

        takasService.hedefKabulEt(talep.getId(), ceren);
        KuralSonucu sonuc = takasService.yoneticiOnayla(talep.getId(), "Kurallar uygun.");

        assertThat(sonuc.gecerli()).isTrue();
        assertThat(vardiyaRepository.findById(emreVardiyasi.getId()).orElseThrow().getPersonel().getId())
                .isEqualTo(ceren.getId());
        assertThat(vardiyaRepository.findById(cerenVardiyasi.getId()).orElseThrow().getPersonel().getId())
                .isEqualTo(emre.getId());
    }

    @Test
    void hedefinAyniGunVardiyasiYoksaVardiyaDevredilir() {
        Personel emre = personel("Test Emre", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        Personel ceren = personel("Test Ceren", CalismaTipi.PART_TIME, null, 20);
        tamZamanliMusaitlik(emre, DayOfWeek.MONDAY);
        partTimeMusaitlik(ceren, DayOfWeek.SATURDAY);

        Vardiya emreVardiyasi = vardiyaRepository.save(
                new Vardiya(emre, CUMARTESI, VardiyaTipi.SABAH, VardiyaDurum.TASLAK));

        TakasTalebi talep = takasService.talepOlustur(emre, emreVardiyasi.getId(), ceren.getId());
        assertThat(talep.getHedefVardiya()).isNull();

        takasService.hedefKabulEt(talep.getId(), ceren);
        KuralSonucu sonuc = takasService.yoneticiOnayla(talep.getId(), null);

        assertThat(sonuc.gecerli()).isTrue();
        assertThat(vardiyaRepository.findById(emreVardiyasi.getId()).orElseThrow().getPersonel().getId())
                .isEqualTo(ceren.getId());
        assertThat(vardiyaRepository.findByPersonelAndTarih(emre, CUMARTESI)).isEmpty();
    }

    @Test
    void hedefMusaitDegilseTalepOlusturulamaz() {
        Personel emre = personel("Test Emre", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        Personel ceren = personel("Test Ceren", CalismaTipi.PART_TIME, null, 20);
        tamZamanliMusaitlik(emre, DayOfWeek.MONDAY);
        partTimeMusaitlik(ceren, DayOfWeek.SUNDAY);

        Vardiya emreVardiyasi = vardiyaRepository.save(
                new Vardiya(emre, CUMARTESI, VardiyaTipi.SABAH, VardiyaDurum.TASLAK));

        assertThatThrownBy(() -> takasService.talepOlustur(emre, emreVardiyasi.getId(), ceren.getId()))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("müsait değil");
    }

    @Test
    void hedefKabulEtmedenYoneticiOnaylayamaz() {
        Personel emre = personel("Test Emre", CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, 45);
        Personel ceren = personel("Test Ceren", CalismaTipi.PART_TIME, null, 20);
        tamZamanliMusaitlik(emre, DayOfWeek.MONDAY);
        partTimeMusaitlik(ceren, DayOfWeek.SATURDAY);

        Vardiya emreVardiyasi = vardiyaRepository.save(
                new Vardiya(emre, CUMARTESI, VardiyaTipi.SABAH, VardiyaDurum.TASLAK));
        TakasTalebi talep = takasService.talepOlustur(emre, emreVardiyasi.getId(), ceren.getId());

        assertThatThrownBy(() -> takasService.yoneticiOnayla(talep.getId(), null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("kabul etmeden");
    }

    private Personel personel(String ad, CalismaTipi tip, DayOfWeek off, int maxSaat) {
        return personelRepository.save(new Personel(ad, Pozisyon.BARISTA, tip, off, maxSaat));
    }

    private void tamZamanliMusaitlik(Personel personel, DayOfWeek offGunu) {
        for (DayOfWeek gun : DayOfWeek.values()) {
            musaitlikRepository.save(new Musaitlik(personel, gun, gun != offGunu));
        }
    }

    private void partTimeMusaitlik(Personel personel, DayOfWeek... musaitGunler) {
        Set<DayOfWeek> musait = EnumSet.noneOf(DayOfWeek.class);
        musait.addAll(Set.of(musaitGunler));
        for (DayOfWeek gun : DayOfWeek.values()) {
            musaitlikRepository.save(new Musaitlik(personel, gun, musait.contains(gun)));
        }
    }
}
