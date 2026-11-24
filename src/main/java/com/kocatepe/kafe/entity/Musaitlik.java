package com.kocatepe.kafe.entity;

import java.time.DayOfWeek;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "musaitlik", uniqueConstraints = @UniqueConstraint(columnNames = {"personel_id", "gun"}))
public class Musaitlik {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "personel_id", nullable = false)
    private Personel personel;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DayOfWeek gun;

    @Column(nullable = false)
    private boolean musait;

    public Musaitlik() {
    }

    public Musaitlik(Personel personel, DayOfWeek gun, boolean musait) {
        this.personel = personel;
        this.gun = gun;
        this.musait = musait;
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

    public DayOfWeek getGun() {
        return gun;
    }

    public void setGun(DayOfWeek gun) {
        this.gun = gun;
    }

    public boolean isMusait() {
        return musait;
    }

    public void setMusait(boolean musait) {
        this.musait = musait;
    }
}
