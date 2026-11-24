package com.kocatepe.kafe.dto;

import java.time.LocalDate;

public record ForecastNoktasi(LocalDate tarih, int saat, double tahminiSiparis) {
}
