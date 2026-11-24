package com.kocatepe.kafe.service;

import java.time.LocalDate;
import java.util.List;

import com.kocatepe.kafe.dto.ForecastNoktasi;

/**
 * Yogunluk tahmin servisi. Basit ortalamadan farkli bir yonteme gecmek icin
 * yalnizca yeni bir implementasyon eklemek yeterlidir.
 */
public interface ForecastService {

    List<ForecastNoktasi> tahminEt(LocalDate baslangic, LocalDate bitis);
}
