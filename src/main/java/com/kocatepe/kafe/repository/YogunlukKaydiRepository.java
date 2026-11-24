package com.kocatepe.kafe.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kocatepe.kafe.entity.YogunlukKaydi;

public interface YogunlukKaydiRepository extends JpaRepository<YogunlukKaydi, Long> {

    List<YogunlukKaydi> findByTarihBetweenOrderByTarihAscSaatAsc(LocalDate baslangic, LocalDate bitis);

    Optional<YogunlukKaydi> findByTarihAndSaat(LocalDate tarih, int saat);

    long countByTarihBetween(LocalDate baslangic, LocalDate bitis);
}
