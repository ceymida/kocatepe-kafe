package com.kocatepe.kafe.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kocatepe.kafe.entity.Personel;

public interface PersonelRepository extends JpaRepository<Personel, Long> {

    List<Personel> findAllByOrderByAdSoyadAsc();

    List<Personel> findByAktifTrueOrderByAdSoyadAsc();

    Optional<Personel> findByAdSoyad(String adSoyad);
}
