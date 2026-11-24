package com.kocatepe.kafe.config;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.springframework.stereotype.Component;

import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.service.TarihUtil;

/** Thymeleaf sablonlarinda kullanilan kucuk bicimlendirme yardimcilari. */
@Component("yardim")
public class ViewHelpers {

    private static final DateTimeFormatter TARIH_SAAT = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm");

    public String gunAdi(DayOfWeek gun) {
        return TarihUtil.gunAdi(gun);
    }

    public String kisaGun(DayOfWeek gun) {
        return TarihUtil.gunAdiKisa(gun);
    }

    public String tarih(LocalDate tarih) {
        return tarih == null ? "-" : tarih.format(TarihUtil.TARIH);
    }

    /** HTML form alan adlarinda ve URL parametrelerinde kullanilan ISO tarih. */
    public String iso(LocalDate tarih) {
        return tarih == null ? "" : tarih.toString();
    }

    public String tarihSaat(LocalDateTime tarih) {
        return tarih == null ? "-" : tarih.format(TARIH_SAAT);
    }

    public String gunTarih(LocalDate tarih) {
        return tarih == null ? "-" : TarihUtil.gunAdi(tarih.getDayOfWeek()) + " " + tarih.format(TarihUtil.TARIH);
    }

    public String vardiyaSaat(VardiyaTipi tip) {
        if (tip == null) {
            return "-";
        }
        return tip.getBaslangic() + "-" + tip.getBitis();
    }
}
