package com.kocatepe.kafe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.ForecastNoktasi;
import com.kocatepe.kafe.entity.YogunlukKaydi;
import com.kocatepe.kafe.repository.YogunlukKaydiRepository;

@ExtendWith(MockitoExtension.class)
class WeightedMovingAverageForecastServiceTest {

    @Mock
    private YogunlukKaydiRepository repository;

    private WeightedMovingAverageForecastService service;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.setTahminHaftaSayisi(8);
        properties.setOpeningHour(9);
        properties.setClosingHour(23);
        service = new WeightedMovingAverageForecastService(repository, properties);
    }

    @Test
    void yeniHaftalaraDahaFazlaAgirlikVerir() {
        LocalDate hedef = LocalDate.of(2026, 11, 23); // Pazartesi
        List<YogunlukKaydi> gecmis = new ArrayList<>();
        // 1 hafta once 80, 8 hafta once 10 siparis: yeni veri daha agir.
        for (int hafta = 1; hafta <= 8; hafta++) {
            LocalDate tarih = hedef.minusWeeks(hafta);
            gecmis.add(new YogunlukKaydi(tarih, 9, 90 - hafta * 10, 15));
        }
        when(repository.findByTarihBetweenOrderByTarihAscSaatAsc(any(), any())).thenReturn(gecmis);

        List<ForecastNoktasi> tahminler = service.tahminEt(hedef, hedef);

        assertThat(tahminler).hasSize(14);
        double tahmin = tahminler.stream()
                .filter(t -> t.tarih().equals(hedef) && t.saat() == 9)
                .findFirst().orElseThrow().tahminiSiparis();

        // Agirlikli ortalama ~56.7; basit ortalama 45.
        assertThat(tahmin).isGreaterThan(50);
        assertThat(tahmin).isLessThan(60);
    }

    @Test
    void gecmisVeriYoksaMakulVarsayilanDoner() {
        when(repository.findByTarihBetweenOrderByTarihAscSaatAsc(any(), any())).thenReturn(List.of());
        List<ForecastNoktasi> tahminler = service.tahminEt(
                LocalDate.of(2026, 11, 23), LocalDate.of(2026, 11, 23));

        assertThat(tahminler).hasSize(14);
        assertThat(tahminler).allMatch(t -> t.tahminiSiparis() > 0);
    }
}
