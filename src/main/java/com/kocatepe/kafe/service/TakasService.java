package com.kocatepe.kafe.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.kocatepe.kafe.config.TimeConfig;
import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.entity.Musaitlik;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.TakasDurum;
import com.kocatepe.kafe.entity.TakasTalebi;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.repository.MusaitlikRepository;
import com.kocatepe.kafe.repository.PersonelRepository;
import com.kocatepe.kafe.repository.TakasTalebiRepository;
import com.kocatepe.kafe.repository.VardiyaRepository;

@Service
public class TakasService {

    private final TakasTalebiRepository takasTalebiRepository;
    private final VardiyaRepository vardiyaRepository;
    private final PersonelRepository personelRepository;
    private final MusaitlikRepository musaitlikRepository;
    private final VardiyaKuralService kuralService;

    public TakasService(TakasTalebiRepository takasTalebiRepository, VardiyaRepository vardiyaRepository,
                        PersonelRepository personelRepository, MusaitlikRepository musaitlikRepository,
                        VardiyaKuralService kuralService) {
        this.takasTalebiRepository = takasTalebiRepository;
        this.vardiyaRepository = vardiyaRepository;
        this.personelRepository = personelRepository;
        this.musaitlikRepository = musaitlikRepository;
        this.kuralService = kuralService;
    }

    @Transactional
    public TakasTalebi talepOlustur(Personel talepEden, Long vardiyaId, Long hedefPersonelId) {
        Vardiya vardiya = vardiyaRepository.findById(vardiyaId)
                .orElseThrow(() -> new IllegalArgumentException("Vardiya bulunamadı."));
        if (!vardiya.getPersonel().getId().equals(talepEden.getId())) {
            throw new IllegalArgumentException("Yalnızca kendi vardiyanız için takas talebi oluşturabilirsiniz.");
        }
        Personel hedef = personelRepository.findById(hedefPersonelId)
                .orElseThrow(() -> new IllegalArgumentException("Hedef personel bulunamadı."));
        if (hedef.getId().equals(talepEden.getId())) {
            throw new IllegalArgumentException("Kendinizle takas yapamazsiniz.");
        }
        if (!hedef.isAktif()) {
            throw new IllegalArgumentException("Hedef personel aktif değil.");
        }
        boolean zatenVar = takasTalebiRepository
                .findByTalepEdenIdOrHedefPersonelIdOrderByOlusturmaTarihiDesc(talepEden.getId(), talepEden.getId())
                .stream()
                .anyMatch(t -> t.getDurum() == TakasDurum.BEKLIYOR
                        && t.getVardiya().getId().equals(vardiyaId));
        if (zatenVar) {
            throw new IllegalArgumentException("Bu vardiya için zaten bekleyen bir takas talebiniz var.");
        }

        // Hedefin ayni gun vardiyasi varsa gercek takas, yoksa vardiya devri yapilir.
        Vardiya hedefVardiya = vardiyaRepository.findByPersonelAndTarih(hedef, vardiya.getTarih()).orElse(null);

        KuralSonucu kontrol = hedefUygunluk(hedef, vardiya, hedefVardiya);
        if (!kontrol.gecerli()) {
            throw new IllegalArgumentException(String.join(" ", kontrol.hatalar()));
        }
        if (hedefVardiya != null) {
            // Gercek takas senaryosu: talep eden de hedefin vardiyasini alabilmeli.
            LocalDate haftaBas = TarihUtil.haftaBaslangici(vardiya.getTarih());
            List<Vardiya> hafta = vardiyaRepository
                    .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(haftaBas, haftaBas.plusDays(6));
            List<Vardiya> digerleri = hafta.stream()
                    .filter(v -> !(v.getPersonel().getId().equals(talepEden.getId())
                            && v.getTarih().equals(vardiya.getTarih())))
                    .toList();
            KuralSonucu talepEdenKontrol = kuralService.kontrol(
                    talepEden, vardiya.getTarih(), hedefVardiya.getVardiyaTipi(), digerleri);
            if (!talepEdenKontrol.gecerli()) {
                throw new IllegalArgumentException("Takas sonrası sizde kural ihlali oluşuyor: "
                        + String.join(" ", talepEdenKontrol.hatalar()));
            }
        }

        TakasTalebi talep = new TakasTalebi();
        talep.setTalepEden(talepEden);
        talep.setVardiya(vardiya);
        talep.setHedefPersonel(hedef);
        talep.setHedefVardiya(hedefVardiya);
        talep.setDurum(TakasDurum.BEKLIYOR);
        talep.setHedefOnayi(null);
        talep.setOlusturmaTarihi(LocalDateTime.now(TimeConfig.ZONE));
        return takasTalebiRepository.save(talep);
    }

    @Transactional
    public void hedefKabulEt(Long talepId, Personel hedef) {
        TakasTalebi talep = getir(talepId);
        hedefKontrol(talep, hedef);
        if (talep.getHedefOnayi() != null) {
            throw new IllegalArgumentException("Bu talebe zaten cevap verildi.");
        }
        talep.setHedefOnayi(true);
        takasTalebiRepository.save(talep);
    }

    @Transactional
    public void hedefReddet(Long talepId, Personel hedef) {
        TakasTalebi talep = getir(talepId);
        hedefKontrol(talep, hedef);
        if (talep.getHedefOnayi() != null) {
            throw new IllegalArgumentException("Bu talebe zaten cevap verildi.");
        }
        talep.setHedefOnayi(false);
        talep.setDurum(TakasDurum.REDDEDILDI);
        talep.setYoneticiNotu("Hedef personel talebi reddetti.");
        takasTalebiRepository.save(talep);
    }

    @Transactional
    public KuralSonucu yoneticiOnayla(Long talepId, String not) {
        TakasTalebi talep = getir(talepId);
        if (talep.getDurum() != TakasDurum.BEKLIYOR) {
            throw new IllegalArgumentException("Yalnızca bekleyen talepler onaylanabilir.");
        }
        if (!Boolean.TRUE.equals(talep.getHedefOnayi())) {
            throw new IllegalArgumentException("Hedef personel talebi kabul etmeden yönetici onayı verilemez.");
        }

        Vardiya talepVardiyasi = vardiyaRepository.findById(talep.getVardiya().getId())
                .orElseThrow(() -> new IllegalArgumentException("Vardiya artık mevcut değil."));
        if (!talepVardiyasi.getPersonel().getId().equals(talep.getTalepEden().getId())) {
            throw new IllegalArgumentException("Vardiya sahibi değişti, talep geçersiz.");
        }

        Personel talepEden = talep.getTalepEden();
        Personel hedef = talep.getHedefPersonel();
        LocalDate tarih = talepVardiyasi.getTarih();
        LocalDate haftaBas = TarihUtil.haftaBaslangici(tarih);
        List<Vardiya> hafta = vardiyaRepository
                .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(haftaBas, haftaBas.plusDays(6));

        Vardiya hedefVardiya = vardiyaRepository.findByPersonelAndTarih(hedef, tarih).orElse(null);
        List<String> uyarilar = new ArrayList<>();

        if (hedefVardiya != null) {
            // Ayni gun iki vardiya yer degistirir; her iki taraf icin de o gunun mevcut vardiyasi haric tutulur.
            List<Vardiya> hedefDigerleri = hafta.stream()
                    .filter(v -> !(v.getPersonel().getId().equals(hedef.getId()) && v.getTarih().equals(tarih)))
                    .toList();
            KuralSonucu hedefKontrol = kuralService.kontrol(hedef, tarih, talepVardiyasi.getVardiyaTipi(), hedefDigerleri);
            List<Vardiya> talepEdenDigerleri = hafta.stream()
                    .filter(v -> !(v.getPersonel().getId().equals(talepEden.getId()) && v.getTarih().equals(tarih)))
                    .toList();
            KuralSonucu talepEdenKontrol = kuralService.kontrol(talepEden, tarih, hedefVardiya.getVardiyaTipi(), talepEdenDigerleri);

            List<String> hatalar = new ArrayList<>();
            hatalar.addAll(hedefKontrol.hatalar());
            hatalar.addAll(talepEdenKontrol.hatalar());
            if (!hatalar.isEmpty()) {
                return new KuralSonucu(hatalar, List.of());
            }
            uyarilar.addAll(hedefKontrol.uyarilar());
            uyarilar.addAll(talepEdenKontrol.uyarilar());

            talepVardiyasi.setPersonel(hedef);
            hedefVardiya.setPersonel(talepEden);
            vardiyaRepository.save(talepVardiyasi);
            vardiyaRepository.save(hedefVardiya);
        } else {
            // Hedefin o gun vardiyasi yok: vardiya devredilir, talep eden o gun bos kalir.
            KuralSonucu hedefKontrol = kuralService.kontrol(hedef, tarih, talepVardiyasi.getVardiyaTipi(), hafta);
            if (!hedefKontrol.gecerli()) {
                return hedefKontrol;
            }
            uyarilar.addAll(hedefKontrol.uyarilar());
            talepVardiyasi.setPersonel(hedef);
            vardiyaRepository.save(talepVardiyasi);
        }

        talep.setDurum(TakasDurum.ONAYLANDI);
        talep.setYoneticiNotu(not == null || not.isBlank() ? "Yönetici tarafından onaylandı." : not.trim());
        takasTalebiRepository.save(talep);
        return new KuralSonucu(List.of(), uyarilar);
    }

    @Transactional
    public void yoneticiReddet(Long talepId, String not) {
        TakasTalebi talep = getir(talepId);
        if (talep.getDurum() != TakasDurum.BEKLIYOR) {
            throw new IllegalArgumentException("Yalnızca bekleyen talepler reddedilebilir.");
        }
        talep.setDurum(TakasDurum.REDDEDILDI);
        talep.setYoneticiNotu(not == null || not.isBlank() ? "Yönetici tarafından reddedildi." : not.trim());
        takasTalebiRepository.save(talep);
    }

    public List<TakasTalebi> yoneticiListesi() {
        return takasTalebiRepository.findAllByOrderByOlusturmaTarihiDesc();
    }

    public List<TakasTalebi> personelListesi(Personel personel) {
        return takasTalebiRepository.findByTalepEdenIdOrHedefPersonelIdOrderByOlusturmaTarihiDesc(
                personel.getId(), personel.getId());
    }

    private KuralSonucu hedefUygunluk(Personel hedef, Vardiya vardiya, Vardiya hedefVardiya) {
        LocalDate tarih = vardiya.getTarih();
        LocalDate haftaBas = TarihUtil.haftaBaslangici(tarih);
        List<Vardiya> hafta = vardiyaRepository
                .findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(haftaBas, haftaBas.plusDays(6));
        List<Vardiya> digerleri = hafta.stream()
                .filter(v -> !(v.getPersonel().getId().equals(hedef.getId()) && v.getTarih().equals(tarih)))
                .toList();
        // hedefVardiya ayni gun varsa takas senaryosu: o gun zaten calisiyor.
        return kuralService.kontrol(hedef, tarih, vardiya.getVardiyaTipi(), digerleri);
    }

    private void hedefKontrol(TakasTalebi talep, Personel hedef) {
        if (!talep.getHedefPersonel().getId().equals(hedef.getId())) {
            throw new IllegalArgumentException("Bu talep size ait değil.");
        }
        if (talep.getDurum() != TakasDurum.BEKLIYOR) {
            throw new IllegalArgumentException("Bu talep artık bekleyen durumda değil.");
        }
    }

    private TakasTalebi getir(Long talepId) {
        return takasTalebiRepository.findById(talepId)
                .orElseThrow(() -> new IllegalArgumentException("Takas talebi bulunamadı."));
    }

    public List<Musaitlik> musaitlikler(Personel personel) {
        return musaitlikRepository.findByPersonelId(personel.getId());
    }

    public List<Vardiya> takasEdilebilirVardiyalar(Personel personel, LocalDate bugun) {
        return vardiyaRepository.findByPersonelIdAndTarihGreaterThanEqualOrderByTarihAsc(personel.getId(), bugun);
    }

    public List<Personel> hedefAdaylari(Personel personel) {
        return personelRepository.findByAktifTrueOrderByAdSoyadAsc().stream()
                .filter(p -> !p.getId().equals(personel.getId()))
                .toList();
    }

    /** Test/bilgilendirme icin: hedefin ayni gun vardiyasi var mi? */
    public Vardiya hedefVardiyasi(Personel hedef, LocalDate tarih) {
        return vardiyaRepository.findByPersonelAndTarih(hedef, tarih).orElse(null);
    }

    public VardiyaTipi vardiyaTipi(Vardiya vardiya) {
        return vardiya.getVardiyaTipi();
    }
}
