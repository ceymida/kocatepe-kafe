package com.kocatepe.kafe.service;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.kocatepe.kafe.entity.Kullanici;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Rol;
import com.kocatepe.kafe.repository.KullaniciRepository;

@Service
public class CurrentUserService {

    private final KullaniciRepository kullaniciRepository;
    private final Clock clock;

    public CurrentUserService(KullaniciRepository kullaniciRepository, Clock clock) {
        this.kullaniciRepository = kullaniciRepository;
        this.clock = clock;
    }

    public LocalDate bugun() {
        return LocalDate.now(clock);
    }

    public Kullanici kullanici() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new IllegalStateException("Oturum bulunamadı");
        }
        String kullaniciAdi = authentication.getName();
        return kullaniciRepository.findByKullaniciAdi(kullaniciAdi)
                .orElseThrow(() -> new IllegalStateException("Kullanıcı bulunamadı: " + kullaniciAdi));
    }

    public Personel personel() {
        return kullanici().getPersonel();
    }

    public Personel personelGerekli() {
        Personel personel = personel();
        if (personel == null) {
            throw new IllegalStateException("Bu kullanıcıya bağlı personel kaydı yok");
        }
        return personel;
    }

    public boolean yoneticiMi() {
        Kullanici kullanici = kullanici();
        return kullanici.getRol() == Rol.YONETICI;
    }

    public List<String> roller() {
        return List.of(kullanici().getRol().name());
    }
}
