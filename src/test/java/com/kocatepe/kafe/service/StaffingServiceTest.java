package com.kocatepe.kafe.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.dto.IhtiyacDurumu;
import com.kocatepe.kafe.dto.ForecastNoktasi;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaTipi;

@ExtendWith(MockitoExtension.class)
class StaffingServiceTest {

    @Mock
    private ForecastService forecastService;

    private StaffingService service;

    @BeforeEach
    void setUp() {
        AppProperties properties = new AppProperties();
        properties.setMinPersonelSaat(2);
        properties.setSiparisBasinaPersonel(15);
        service = new StaffingService(forecastService, properties);
    }

    @Test
    void saatlikIhtiyacKatsayiVeMinimumuUygular() {
        assertThat(service.saatlikIhtiyac(100)).isEqualTo(7);
        assertThat(service.saatlikIhtiyac(10)).isEqualTo(2);
        assertThat(service.saatlikIhtiyac(30)).isEqualTo(2);
        assertThat(service.saatlikIhtiyac(31)).isEqualTo(3);
    }

    @Test
    void vardiyaIhtiyaciVardiyaAraligindakiEnYuksekSaatiAlir() {
        LocalDate pazartesi = LocalDate.of(2026, 11, 23);
        when(forecastService.tahminEt(any(), any())).thenReturn(List.of(
                new ForecastNoktasi(pazartesi, 9, 15),
                new ForecastNoktasi(pazartesi, 12, 45),
                new ForecastNoktasi(pazartesi, 15, 20),
                new ForecastNoktasi(pazartesi, 19, 60)
        ));

        var ihtiyaclar = service.vardiyaIhtiyaclari(pazartesi, List.<Vardiya>of());

        IhtiyacDurumu sabah = ihtiyaclar.get(StaffingService.anahtar(pazartesi, VardiyaTipi.SABAH));
        IhtiyacDurumu aksam = ihtiyaclar.get(StaffingService.anahtar(pazartesi, VardiyaTipi.AKSAM));

        assertThat(sabah.getGerekli()).isEqualTo(3); // 45/15
        assertThat(aksam.getGerekli()).isEqualTo(4); // 60/15
        assertThat(sabah.getDurum()).isEqualTo("EKSIK");
    }
}
