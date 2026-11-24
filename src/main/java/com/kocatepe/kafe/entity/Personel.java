package com.kocatepe.kafe.entity;

import java.time.DayOfWeek;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "personel")
public class Personel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String adSoyad;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Pozisyon pozisyon;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private CalismaTipi calismaTipi;

    @Enumerated(EnumType.STRING)
    private DayOfWeek haftalikOffGunu;

    @Column(nullable = false)
    private int haftalikMaxSaat;

    @Column(nullable = false)
    private boolean aktif = true;

    public Personel() {
    }

    public Personel(String adSoyad, Pozisyon pozisyon, CalismaTipi calismaTipi,
                    DayOfWeek haftalikOffGunu, int haftalikMaxSaat) {
        this.adSoyad = adSoyad;
        this.pozisyon = pozisyon;
        this.calismaTipi = calismaTipi;
        this.haftalikOffGunu = haftalikOffGunu;
        this.haftalikMaxSaat = haftalikMaxSaat;
        this.aktif = true;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getAdSoyad() {
        return adSoyad;
    }

    public void setAdSoyad(String adSoyad) {
        this.adSoyad = adSoyad;
    }

    public Pozisyon getPozisyon() {
        return pozisyon;
    }

    public void setPozisyon(Pozisyon pozisyon) {
        this.pozisyon = pozisyon;
    }

    public CalismaTipi getCalismaTipi() {
        return calismaTipi;
    }

    public void setCalismaTipi(CalismaTipi calismaTipi) {
        this.calismaTipi = calismaTipi;
    }

    public DayOfWeek getHaftalikOffGunu() {
        return haftalikOffGunu;
    }

    public void setHaftalikOffGunu(DayOfWeek haftalikOffGunu) {
        this.haftalikOffGunu = haftalikOffGunu;
    }

    public int getHaftalikMaxSaat() {
        return haftalikMaxSaat;
    }

    public void setHaftalikMaxSaat(int haftalikMaxSaat) {
        this.haftalikMaxSaat = haftalikMaxSaat;
    }

    public boolean isAktif() {
        return aktif;
    }

    public void setAktif(boolean aktif) {
        this.aktif = aktif;
    }
}
