package com.kocatepe.kafe.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.dto.OtoPlanSonucu;
import com.kocatepe.kafe.dto.VardiyaHaftasi;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.VardiyaTipi;
import com.kocatepe.kafe.service.AutoScheduleService;
import com.kocatepe.kafe.service.CurrentUserService;
import com.kocatepe.kafe.service.VardiyaService;

@Controller
@RequestMapping("/vardiya")
public class VardiyaController {

    private final VardiyaService vardiyaService;
    private final AutoScheduleService autoScheduleService;
    private final CurrentUserService currentUserService;

    public VardiyaController(VardiyaService vardiyaService, AutoScheduleService autoScheduleService,
                             CurrentUserService currentUserService) {
        this.vardiyaService = vardiyaService;
        this.autoScheduleService = autoScheduleService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String hafta(@RequestParam(value = "hafta", required = false) String haftaMetni, Model model) {
        LocalDate referans = tarihOku(haftaMetni, currentUserService.bugun());
        boolean yonetici = currentUserService.yoneticiMi();
        VardiyaHaftasi hafta;
        if (yonetici) {
            hafta = vardiyaService.hafta(referans);
        } else {
            Personel personel = currentUserService.personelGerekli();
            hafta = vardiyaService.haftaCalisan(referans, personel);
            model.addAttribute("personel", personel);
        }
        model.addAttribute("aktifMenu", "vardiya");
        model.addAttribute("yonetici", yonetici);
        model.addAttribute("hafta", hafta);
        model.addAttribute("oncekiHafta", hafta.getHaftaBaslangici().minusDays(7));
        model.addAttribute("sonrakiHafta", hafta.getHaftaBaslangici().plusDays(7));
        model.addAttribute("bugun", currentUserService.bugun());
        return "vardiya/hafta";
    }

    @PostMapping("/kaydet")
    public String kaydet(@RequestParam("hafta") String haftaMetni,
                         @RequestParam Map<String, String> parametreler,
                         RedirectAttributes redirectAttributes) {
        LocalDate referans = tarihOku(haftaMetni, currentUserService.bugun());
        Set<String> hatalar = new LinkedHashSet<>();
        Set<String> uyarilar = new LinkedHashSet<>();

        for (Map.Entry<String, String> giris : parametreler.entrySet()) {
            String anahtar = giris.getKey();
            if (!anahtar.startsWith("tip_")) {
                continue;
            }
            String[] parcalar = anahtar.split("_", 3);
            if (parcalar.length != 3) {
                continue;
            }
            Long personelId;
            LocalDate tarih;
            try {
                personelId = Long.parseLong(parcalar[1]);
                tarih = LocalDate.parse(parcalar[2]);
            } catch (NumberFormatException | DateTimeParseException ex) {
                continue;
            }
            VardiyaTipi tip = null;
            if ("SABAH".equals(giris.getValue())) {
                tip = VardiyaTipi.SABAH;
            } else if ("AKSAM".equals(giris.getValue())) {
                tip = VardiyaTipi.AKSAM;
            }
            try {
                KuralSonucu sonuc = vardiyaService.hucreKaydet(personelId, tarih, tip);
                hatalar.addAll(sonuc.hatalar());
                uyarilar.addAll(sonuc.uyarilar());
            } catch (IllegalArgumentException ex) {
                hatalar.add(ex.getMessage());
            }
        }

        if (hatalar.isEmpty()) {
            redirectAttributes.addFlashAttribute("mesaj", "Vardiya değişiklikleri kaydedildi.");
        } else {
            redirectAttributes.addFlashAttribute("hata",
                    "Bazı atamalar kural ihlali nedeniyle yapılmadı: " + String.join(" ", hatalar));
        }
        if (!uyarilar.isEmpty()) {
            redirectAttributes.addFlashAttribute("bilgi", "Uyarılar: " + String.join(" ", uyarilar));
        }
        return "redirect:/vardiya?hafta=" + referans;
    }

    @PostMapping("/yayinla")
    public String yayinla(@RequestParam("hafta") String haftaMetni, RedirectAttributes redirectAttributes) {
        LocalDate referans = tarihOku(haftaMetni, currentUserService.bugun());
        KuralSonucu kurallar = vardiyaService.haftaKurallari(referans);
        if (!kurallar.gecerli()) {
            redirectAttributes.addFlashAttribute("hata",
                    "Hafta yayınlanamadı, önce kural ihlallerini düzeltin: "
                            + String.join(" ", kurallar.hatalar()));
            return "redirect:/vardiya?hafta=" + referans;
        }
        int sayi = vardiyaService.yayinla(referans);
        redirectAttributes.addFlashAttribute("mesaj", sayi + " vardiya YAYINDA durumuna alındı.");
        if (!kurallar.uyarilar().isEmpty()) {
            redirectAttributes.addFlashAttribute("bilgi", "Uyarılar: " + String.join(" ", kurallar.uyarilar()));
        }
        return "redirect:/vardiya?hafta=" + referans;
    }

    @PostMapping("/otomatik")
    public String otomatik(@RequestParam("hafta") String haftaMetni, RedirectAttributes redirectAttributes) {
        LocalDate referans = tarihOku(haftaMetni, currentUserService.bugun());
        OtoPlanSonucu sonuc = autoScheduleService.olustur(referans);
        redirectAttributes.addFlashAttribute("mesaj",
                sonuc.getOlusturulanVardiya() + " TASLAK vardiya oluşturuldu. Kontrol edip YAYINLA yapabilirsiniz.");
        if (!sonuc.getEksikler().isEmpty()) {
            redirectAttributes.addFlashAttribute("bilgi",
                    "Karşılanamayan ihtiyaçlar: " + String.join("; ", sonuc.getEksikler()));
        }
        return "redirect:/vardiya?hafta=" + referans;
    }

    private LocalDate tarihOku(String metin, LocalDate varsayilan) {
        if (metin == null || metin.isBlank()) {
            return varsayilan;
        }
        try {
            return LocalDate.parse(metin);
        } catch (DateTimeParseException ex) {
            return varsayilan;
        }
    }
}
