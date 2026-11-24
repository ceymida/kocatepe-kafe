package com.kocatepe.kafe.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

/**
 * Sentetik (ornek) yogunluk verisi. Gercek veri geldiginde CSV import ayni tabloyu doldurur.
 */
@Entity
@Table(name = "yogunluk_kaydi", uniqueConstraints = @UniqueConstraint(columnNames = {"tarih", "saat"}))
public class YogunlukKaydi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private LocalDate tarih;

    @Column(nullable = false)
    private int saat;

    @Column(nullable = false)
    private int siparisSayisi;

    @Column(nullable = false)
    private int doluMasa;

    public YogunlukKaydi() {
    }

    public YogunlukKaydi(LocalDate tarih, int saat, int siparisSayisi, int doluMasa) {
        this.tarih = tarih;
        this.saat = saat;
        this.siparisSayisi = siparisSayisi;
        this.doluMasa = doluMasa;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }

    public int getSaat() {
        return saat;
    }

    public void setSaat(int saat) {
        this.saat = saat;
    }

    public int getSiparisSayisi() {
        return siparisSayisi;
    }

    public void setSiparisSayisi(int siparisSayisi) {
        this.siparisSayisi = siparisSayisi;
    }

    public int getDoluMasa() {
        return doluMasa;
    }

    public void setDoluMasa(int doluMasa) {
        this.doluMasa = doluMasa;
    }
}
