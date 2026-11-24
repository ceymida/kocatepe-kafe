package com.kocatepe.kafe.service;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.config.AppProperties;
import com.kocatepe.kafe.config.DataSeeder;
import com.kocatepe.kafe.dto.PersonelForm;
import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Kullanici;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Rol;
import com.kocatepe.kafe.repository.KullaniciRepository;
import com.kocatepe.kafe.repository.MusaitlikRepository;
import com.kocatepe.kafe.repository.PersonelRepository;

@Service
public class PersonelService {

    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;
    private final MusaitlikRepository musaitlikRepository;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;

    public PersonelService(PersonelRepository personelRepository, KullaniciRepository kullaniciRepository,
                           MusaitlikRepository musaitlikRepository, PasswordEncoder passwordEncoder,
                           AppProperties properties) {
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.musaitlikRepository = musaitlikRepository;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
    }

    public List<Personel> tumu() {
        return personelRepository.findAllByOrderByAdSoyadAsc();
    }

    public List<Personel> aktifler() {
        return personelRepository.findByAktifTrueOrderByAdSoyadAsc();
    }

    public Personel getir(Long id) {
        return personelRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Personel bulunamadı: " + id));
    }

    public PersonelForm form(Long id) {
        return PersonelForm.from(getir(id));
    }

    public PersonelForm yeniForm() {
        PersonelForm form = new PersonelForm();
        form.setCalismaTipi(CalismaTipi.TAM_ZAMANLI);
        form.setHaftalikMaxSaat(properties.getFullTimeMaxSaat());
        form.setAktif(true);
        return form;
    }

    @Transactional
    public Personel kaydet(PersonelForm form) {
        if (form.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI && form.getHaftalikOffGunu() == null) {
            throw new IllegalArgumentException("Tam zamanlı personel için haftalık izin günü seçilmelidir");
        }
        if (form.getCalismaTipi() == CalismaTipi.PART_TIME) {
            form.setHaftalikOffGunu(null);
        }

        if (form.getId() == null) {
            Personel personel = new Personel();
            uygula(personel, form);
            personel = personelRepository.save(personel);
            kullaniciOlustur(personel);
            musaitlikOlustur(personel);
            return personel;
        }

        Personel personel = getir(form.getId());
        DayOfWeek eskiOff = personel.getHaftalikOffGunu();
        uygula(personel, form);
        personel = personelRepository.save(personel);
        musaitlikGuncelle(personel, eskiOff);
        return personel;
    }

    @Transactional
    public Personel durumDegistir(Long id) {
        Personel personel = getir(id);
        personel.setAktif(!personel.isAktif());
        return personelRepository.save(personel);
    }

    private void uygula(Personel personel, PersonelForm form) {
        personel.setAdSoyad(form.getAdSoyad().trim());
        personel.setPozisyon(form.getPozisyon());
        personel.setCalismaTipi(form.getCalismaTipi());
        personel.setHaftalikOffGunu(form.getHaftalikOffGunu());
        personel.setHaftalikMaxSaat(form.getHaftalikMaxSaat());
        personel.setAktif(form.isAktif());
    }

    private void kullaniciOlustur(Personel personel) {
        String temel = DataSeeder.kullaniciAdi(personel.getAdSoyad());
        String aday = temel;
        int ek = 2;
        while (kullaniciRepository.existsByKullaniciAdi(aday)) {
            aday = temel.replace("@", ek + "@");
            ek++;
        }
        kullaniciRepository.save(new Kullanici(aday,
                passwordEncoder.encode("1234"), Rol.CALISAN, personel));
    }

    private void musaitlikOlustur(Personel personel) {
        for (DayOfWeek gun : DayOfWeek.values()) {
            boolean musait = personel.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI
                    && personel.getHaftalikOffGunu() != gun;
            musaitlikRepository.save(new Musaitlik(personel, gun, musait));
        }
    }

    private void musaitlikGuncelle(Personel personel, DayOfWeek eskiOff) {
        List<Musaitlik> kayitlar = musaitlikRepository.findByPersonelId(personel.getId());
        if (kayitlar.isEmpty()) {
            musaitlikOlustur(personel);
            return;
        }
        for (Musaitlik musaitlik : kayitlar) {
            if (personel.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI) {
                if (musaitlik.getGun() == personel.getHaftalikOffGunu()) {
                    musaitlik.setMusait(false);
                } else if (eskiOff != null && musaitlik.getGun() == eskiOff) {
                    musaitlik.setMusait(true);
                }
            }
        }
    }
}
