package com.kocatepe.kafe.entity;

import java.time.LocalDateTime;

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

@Entity
@Table(name = "takas_talebi")
public class TakasTalebi {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(optional = false)
    @JoinColumn(name = "talep_eden_id", nullable = false)
    private Personel talepEden;

    @ManyToOne(optional = false)
    @JoinColumn(name = "vardiya_id", nullable = false)
    private Vardiya vardiya;

    @ManyToOne(optional = false)
    @JoinColumn(name = "hedef_personel_id", nullable = false)
    private Personel hedefPersonel;

    /** Hedef personelin ayni gun vardiyasi varsa gercek takas, yoksa vardiya devri yapilir. */
    @ManyToOne
    @JoinColumn(name = "hedef_vardiya_id")
    private Vardiya hedefVardiya;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TakasDurum durum = TakasDurum.BEKLIYOR;

    @Column(length = 500)
    private String yoneticiNotu;

    /** null: hedef personel henuz cevap vermedi, true: kabul etti. Red durumunda talep REDDEDILDI olur. */
    private Boolean hedefOnayi;

    @Column(nullable = false)
    private LocalDateTime olusturmaTarihi = LocalDateTime.now();

    public TakasTalebi() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Personel getTalepEden() {
        return talepEden;
    }

    public void setTalepEden(Personel talepEden) {
        this.talepEden = talepEden;
    }

    public Vardiya getVardiya() {
        return vardiya;
    }

    public void setVardiya(Vardiya vardiya) {
        this.vardiya = vardiya;
    }

    public Personel getHedefPersonel() {
        return hedefPersonel;
    }

    public void setHedefPersonel(Personel hedefPersonel) {
        this.hedefPersonel = hedefPersonel;
    }

    public Vardiya getHedefVardiya() {
        return hedefVardiya;
    }

    public void setHedefVardiya(Vardiya hedefVardiya) {
        this.hedefVardiya = hedefVardiya;
    }

    public TakasDurum getDurum() {
        return durum;
    }

    public void setDurum(TakasDurum durum) {
        this.durum = durum;
    }

    public String getYoneticiNotu() {
        return yoneticiNotu;
    }

    public void setYoneticiNotu(String yoneticiNotu) {
        this.yoneticiNotu = yoneticiNotu;
    }

    public Boolean getHedefOnayi() {
        return hedefOnayi;
    }

    public void setHedefOnayi(Boolean hedefOnayi) {
        this.hedefOnayi = hedefOnayi;
    }

    public boolean isHedefBekliyor() {
        return durum == TakasDurum.BEKLIYOR && hedefOnayi == null;
    }

    public boolean isHedefKabulEtti() {
        return Boolean.TRUE.equals(hedefOnayi);
    }

    public boolean isHedefReddetti() {
        return Boolean.FALSE.equals(hedefOnayi);
    }

    public LocalDateTime getOlusturmaTarihi() {
        return olusturmaTarihi;
    }

    public void setOlusturmaTarihi(LocalDateTime olusturmaTarihi) {
        this.olusturmaTarihi = olusturmaTarihi;
    }
}
