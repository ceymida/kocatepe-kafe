package com.kocatepe.kafe.repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;

public interface MusaitlikRepository extends JpaRepository<Musaitlik, Long> {

    List<Musaitlik> findByPersonel(Personel personel);

    List<Musaitlik> findByPersonelId(Long personelId);

    Optional<Musaitlik> findByPersonelIdAndGun(Long personelId, DayOfWeek gun);
}
