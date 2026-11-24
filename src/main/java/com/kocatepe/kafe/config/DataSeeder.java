package com.kocatepe.kafe.config;

import java.text.Normalizer;
import java.time.Clock;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Kullanici;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Pozisyon;
import com.kocatepe.kafe.entity.Rol;
import com.kocatepe.kafe.repository.KullaniciRepository;
import com.kocatepe.kafe.repository.MusaitlikRepository;
import com.kocatepe.kafe.repository.PersonelRepository;
import com.kocatepe.kafe.service.SyntheticDataGenerator;

/**
 * Uygulama ilk acilisinda ORNEK veriyi yukler.
 * kafe.seed-enabled=false ile tamamen kapatilabilir.
 */
@Component
public class DataSeeder implements ApplicationRunner {

    private static final String VARSAYILAN_SIFRE = "1234";

    private final PersonelRepository personelRepository;
    private final KullaniciRepository kullaniciRepository;
    private final MusaitlikRepository musaitlikRepository;
    private final SyntheticDataGenerator syntheticDataGenerator;
    private final PasswordEncoder passwordEncoder;
    private final AppProperties properties;
    private final Clock clock;

    public DataSeeder(PersonelRepository personelRepository, KullaniciRepository kullaniciRepository,
                      MusaitlikRepository musaitlikRepository, SyntheticDataGenerator syntheticDataGenerator,
                      PasswordEncoder passwordEncoder, AppProperties properties, Clock clock) {
        this.personelRepository = personelRepository;
        this.kullaniciRepository = kullaniciRepository;
        this.musaitlikRepository = musaitlikRepository;
        this.syntheticDataGenerator = syntheticDataGenerator;
        this.passwordEncoder = passwordEncoder;
        this.properties = properties;
        this.clock = clock;
    }

    private record Seed(String adSoyad, Pozisyon pozisyon, CalismaTipi calismaTipi,
                        DayOfWeek offGunu, List<DayOfWeek> musaitGunler) {
    }

    private List<Seed> seedListesi() {
        return List.of(
                new Seed("Emre Kaya", Pozisyon.BARISTA, CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, List.of()),
                new Seed("Selin Aydın", Pozisyon.BARISTA, CalismaTipi.TAM_ZAMANLI, DayOfWeek.TUESDAY, List.of()),
                new Seed("Burak Demir", Pozisyon.STEWARD, CalismaTipi.TAM_ZAMANLI, DayOfWeek.WEDNESDAY, List.of()),
                new Seed("Zeynep Yılmaz", Pozisyon.STEWARD, CalismaTipi.TAM_ZAMANLI, DayOfWeek.THURSDAY, List.of()),
                new Seed("Can Öztürk", Pozisyon.MUTFAK, CalismaTipi.TAM_ZAMANLI, DayOfWeek.MONDAY, List.of()),
                new Seed("Elif Arslan", Pozisyon.KASIYER, CalismaTipi.TAM_ZAMANLI, DayOfWeek.TUESDAY, List.of()),
                new Seed("Mert Çelik", Pozisyon.MUTFAK, CalismaTipi.TAM_ZAMANLI, DayOfWeek.WEDNESDAY, List.of()),
                new Seed("Derya Şahin", Pozisyon.BARISTA, CalismaTipi.TAM_ZAMANLI, DayOfWeek.THURSDAY, List.of()),
                new Seed("Ahmet Koç", Pozisyon.STEWARD, CalismaTipi.PART_TIME, null,
                        List.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
                new Seed("Ceren Polat", Pozisyon.BARISTA, CalismaTipi.PART_TIME, null,
                        List.of(DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
                new Seed("Oğuz Tekin", Pozisyon.STEWARD, CalismaTipi.PART_TIME, null,
                        List.of(DayOfWeek.WEDNESDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)),
                new Seed("Melis Erdem", Pozisyon.KASIYER, CalismaTipi.PART_TIME, null,
                        List.of(DayOfWeek.THURSDAY, DayOfWeek.FRIDAY, DayOfWeek.SATURDAY))
        );
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (!properties.isSeedEnabled()) {
            return;
        }
        List<Seed> seedler = seedListesi();
        seedPersonelVeKullanicilar(seedler);
        seedMusaitlik(seedler);
        LocalDate bugun = LocalDate.now(clock);
        syntheticDataGenerator.generateIfEmpty(bugun);
    }

    private void seedPersonelVeKullanicilar(List<Seed> seedler) {
        herDurumdaAdminOlustur();
        if (personelRepository.count() > 0) {
            return;
        }

        String sifreHash = passwordEncoder.encode(VARSAYILAN_SIFRE);
        for (Seed seed : seedler) {
            int maxSaat = seed.calismaTipi() == CalismaTipi.TAM_ZAMANLI
                    ? properties.getFullTimeMaxSaat() : properties.getPartTimeMaxSaat();
            Personel personel = personelRepository.save(
                    new Personel(seed.adSoyad(), seed.pozisyon(), seed.calismaTipi(), seed.offGunu(), maxSaat));
            String kullaniciAdi = kullaniciAdi(seed.adSoyad());
            if (!kullaniciRepository.existsByKullaniciAdi(kullaniciAdi)) {
                kullaniciRepository.save(new Kullanici(kullaniciAdi, sifreHash, Rol.CALISAN, personel));
            }
        }
    }

    private void herDurumdaAdminOlustur() {
        if (!kullaniciRepository.existsByKullaniciAdi("admin")) {
            kullaniciRepository.save(new Kullanici("admin",
                    passwordEncoder.encode("admin"), Rol.YONETICI, null));
        }
    }

    private void seedMusaitlik(List<Seed> seedler) {
        if (musaitlikRepository.count() > 0) {
            return;
        }
        Map<String, List<DayOfWeek>> musaitGunler = new HashMap<>();
        for (Seed seed : seedler) {
            musaitGunler.put(seed.adSoyad(), seed.musaitGunler());
        }

        List<Musaitlik> kayitlar = new ArrayList<>();
        for (Personel personel : personelRepository.findAll()) {
            List<DayOfWeek> musait = musaitGunler.get(personel.getAdSoyad());
            for (DayOfWeek gun : DayOfWeek.values()) {
                boolean gunMusait;
                if (personel.getCalismaTipi() == CalismaTipi.TAM_ZAMANLI) {
                    gunMusait = personel.getHaftalikOffGunu() != gun;
                } else {
                    gunMusait = musait != null && musait.contains(gun);
                }
                kayitlar.add(new Musaitlik(personel, gun, gunMusait));
            }
        }
        musaitlikRepository.saveAll(kayitlar);
    }

    public static String kullaniciAdi(String adSoyad) {
        String normalize = Normalizer.normalize(adSoyad.toLowerCase(Locale.ROOT), Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('ı', 'i')
                .replace('ş', 's')
                .replace('ğ', 'g')
                .replace('ç', 'c')
                .replace('ö', 'o')
                .replace('ü', 'u')
                .replaceAll("[^a-z0-9 ]", "")
                .trim();
        return normalize.replaceAll("\\s+", ".") + "@kocatepe.com";
    }
}
