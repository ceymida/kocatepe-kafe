package com.kocatepe.kafe.entity;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

@Entity
@Table(name = "vardiya", indexes = {
        @Index(name = "idx_vardiya_tarih", columnList = "tarih"),
        @Index(name = "idx_vardiya_personel_tarih", columnList = "personel_id, tarih")
})
public class Vardiya {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personel_id", nullable = false)
    private Personel personel;

    @Column(nullable = false)
    private LocalDate tarih;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VardiyaTipi vardiyaTipi;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private VardiyaDurum durum = VardiyaDurum.TASLAK;

    public Vardiya() {
    }

    public Vardiya(Personel personel, LocalDate tarih, VardiyaTipi vardiyaTipi, VardiyaDurum durum) {
        this.personel = personel;
        this.tarih = tarih;
        this.vardiyaTipi = vardiyaTipi;
        this.durum = durum;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Personel getPersonel() {
        return personel;
    }

    public void setPersonel(Personel personel) {
        this.personel = personel;
    }

    public LocalDate getTarih() {
        return tarih;
    }

    public void setTarih(LocalDate tarih) {
        this.tarih = tarih;
    }

    public VardiyaTipi getVardiyaTipi() {
        return vardiyaTipi;
    }

    public void setVardiyaTipi(VardiyaTipi vardiyaTipi) {
        this.vardiyaTipi = vardiyaTipi;
    }

    public VardiyaDurum getDurum() {
        return durum;
    }

    public void setDurum(VardiyaDurum durum) {
        this.durum = durum;
    }
}
