package com.kocatepe.kafe.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.kocatepe.kafe.entity.TakasDurum;
import com.kocatepe.kafe.entity.TakasTalebi;

public interface TakasTalebiRepository extends JpaRepository<TakasTalebi, Long> {

    List<TakasTalebi> findAllByOrderByOlusturmaTarihiDesc();

    List<TakasTalebi> findByDurumOrderByOlusturmaTarihiDesc(TakasDurum durum);

    List<TakasTalebi> findByTalepEdenIdOrderByOlusturmaTarihiDesc(Long talepEdenId);

    List<TakasTalebi> findByHedefPersonelIdOrderByOlusturmaTarihiDesc(Long hedefPersonelId);

    List<TakasTalebi> findByTalepEdenIdOrHedefPersonelIdOrderByOlusturmaTarihiDesc(Long talepEdenId, Long hedefPersonelId);
}
