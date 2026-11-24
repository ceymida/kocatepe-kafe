package com.kocatepe.kafe.dto;

import java.time.DayOfWeek;

import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Pozisyon;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class PersonelForm {

    private Long id;

    @NotBlank(message = "Ad soyad zorunludur")
    @Size(max = 120)
    private String adSoyad;

    @NotNull(message = "Pozisyon seçilmelidir")
    private Pozisyon pozisyon;

    @NotNull(message = "Çalışma tipi seçilmelidir")
    private CalismaTipi calismaTipi;

    private DayOfWeek haftalikOffGunu;

    @Min(value = 1, message = "Haftalık saat en az 1 olmalıdır")
    @Max(value = 80, message = "Haftalık saat en fazla 80 olabilir")
    private int haftalikMaxSaat;

    private boolean aktif = true;

    public static PersonelForm from(Personel personel) {
        PersonelForm form = new PersonelForm();
        form.id = personel.getId();
        form.adSoyad = personel.getAdSoyad();
        form.pozisyon = personel.getPozisyon();
        form.calismaTipi = personel.getCalismaTipi();
        form.haftalikOffGunu = personel.getHaftalikOffGunu();
        form.haftalikMaxSaat = personel.getHaftalikMaxSaat();
        form.aktif = personel.isAktif();
        return form;
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
