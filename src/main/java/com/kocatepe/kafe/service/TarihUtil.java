package com.kocatepe.kafe.service;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.Map;

public final class TarihUtil {

    public static final DateTimeFormatter TARIH = DateTimeFormatter.ofPattern("dd.MM.yyyy");
    public static final DateTimeFormatter TARIH_UZUN = DateTimeFormatter.ofPattern("dd MMMM yyyy");

    private static final Map<DayOfWeek, String> GUN_ADLARI = Map.of(
            DayOfWeek.MONDAY, "Pazartesi",
            DayOfWeek.TUESDAY, "Salı",
            DayOfWeek.WEDNESDAY, "Çarşamba",
            DayOfWeek.THURSDAY, "Perşembe",
            DayOfWeek.FRIDAY, "Cuma",
            DayOfWeek.SATURDAY, "Cumartesi",
            DayOfWeek.SUNDAY, "Pazar"
    );

    private static final Map<DayOfWeek, String> GUN_ADLARI_KISA = Map.of(
            DayOfWeek.MONDAY, "Pzt",
            DayOfWeek.TUESDAY, "Sal",
            DayOfWeek.WEDNESDAY, "Çar",
            DayOfWeek.THURSDAY, "Per",
            DayOfWeek.FRIDAY, "Cum",
            DayOfWeek.SATURDAY, "Cmt",
            DayOfWeek.SUNDAY, "Paz"
    );

    private TarihUtil() {
    }

    public static LocalDate haftaBaslangici(LocalDate tarih) {
        return tarih.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    public static LocalDate haftaBitisi(LocalDate tarih) {
        return haftaBaslangici(tarih).plusDays(6);
    }

    public static String gunAdi(DayOfWeek gun) {
        return gun == null ? "-" : GUN_ADLARI.get(gun);
    }

    public static String gunAdiKisa(DayOfWeek gun) {
        return gun == null ? "-" : GUN_ADLARI_KISA.getOrDefault(gun, "-");
    }
}
