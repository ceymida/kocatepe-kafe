package com.kocatepe.kafe.repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaDurum;

public interface VardiyaRepository extends JpaRepository<Vardiya, Long> {

    List<Vardiya> findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(LocalDate baslangic, LocalDate bitis);

    List<Vardiya> findByTarihBetweenAndDurumOrderByTarihAsc(LocalDate baslangic, LocalDate bitis, VardiyaDurum durum);

    List<Vardiya> findByPersonelIdAndTarihBetweenOrderByTarihAsc(Long personelId, LocalDate baslangic, LocalDate bitis);

    List<Vardiya> findByPersonelIdAndTarihGreaterThanEqualAndDurumOrderByTarihAsc(Long personelId, LocalDate tarih, VardiyaDurum durum);

    Optional<Vardiya> findByPersonelIdAndTarih(Long personelId, LocalDate tarih);

    Optional<Vardiya> findByPersonelAndTarih(Personel personel, LocalDate tarih);

    List<Vardiya> findByPersonelIdAndTarihAfterOrderByTarihAsc(Long personelId, LocalDate tarih);

    List<Vardiya> findByPersonelIdAndTarihGreaterThanEqualOrderByTarihAsc(Long personelId, LocalDate tarih);

    long countByTarihBetweenAndDurum(LocalDate baslangic, LocalDate bitis, VardiyaDurum durum);
}
