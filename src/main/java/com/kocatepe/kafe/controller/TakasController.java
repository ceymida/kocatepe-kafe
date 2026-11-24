package com.kocatepe.kafe.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kocatepe.kafe.dto.KuralSonucu;
import com.kocatepe.kafe.dto.TakasForm;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.TakasTalebi;
import com.kocatepe.kafe.service.CurrentUserService;
import com.kocatepe.kafe.service.TakasService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/takas")
public class TakasController {

    private final TakasService takasService;
    private final CurrentUserService currentUserService;

    public TakasController(TakasService takasService, CurrentUserService currentUserService) {
        this.takasService = takasService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String liste(Model model) {
        boolean yonetici = currentUserService.yoneticiMi();
        model.addAttribute("aktifMenu", "takas");
        model.addAttribute("yonetici", yonetici);
        if (yonetici) {
            model.addAttribute("takaslar", takasService.yoneticiListesi());
        } else {
            Personel personel = currentUserService.personelGerekli();
            List<TakasTalebi> takaslar = takasService.personelListesi(personel);
            model.addAttribute("personel", personel);
            model.addAttribute("takaslar", takaslar);
        }
        return "takas/liste";
    }

    @GetMapping("/yeni")
    public String yeni(Model model, RedirectAttributes redirectAttributes) {
        if (currentUserService.yoneticiMi() || currentUserService.personel() == null) {
            redirectAttributes.addFlashAttribute("hata", "Takas talebi yalnızca çalışan hesapları tarafından oluşturulabilir.");
            return "redirect:/takas";
        }
        Personel personel = currentUserService.personelGerekli();
        LocalDate bugun = currentUserService.bugun();
        model.addAttribute("aktifMenu", "takas");
        model.addAttribute("form", new TakasForm());
        model.addAttribute("personel", personel);
        model.addAttribute("vardiyalar", takasService.takasEdilebilirVardiyalar(personel, bugun));
        model.addAttribute("hedefler", takasService.hedefAdaylari(personel));
        return "takas/form";
    }

    @PostMapping("/yeni")
    public String olustur(@Valid @ModelAttribute("form") TakasForm form, BindingResult bindingResult,
                          Model model, RedirectAttributes redirectAttributes) {
        Personel personel = currentUserService.personelGerekli();
        if (bindingResult.hasErrors()) {
            model.addAttribute("aktifMenu", "takas");
            model.addAttribute("personel", personel);
            model.addAttribute("vardiyalar", takasService.takasEdilebilirVardiyalar(personel, currentUserService.bugun()));
            model.addAttribute("hedefler", takasService.hedefAdaylari(personel));
            return "takas/form";
        }
        try {
            takasService.talepOlustur(personel, form.getVardiyaId(), form.getHedefPersonelId());
        } catch (IllegalArgumentException ex) {
            model.addAttribute("aktifMenu", "takas");
            model.addAttribute("personel", personel);
            model.addAttribute("vardiyalar", takasService.takasEdilebilirVardiyalar(personel, currentUserService.bugun()));
            model.addAttribute("hedefler", takasService.hedefAdaylari(personel));
            model.addAttribute("hata", ex.getMessage());
            return "takas/form";
        }
        redirectAttributes.addFlashAttribute("mesaj", "Takas talebi oluşturuldu. Hedef personelin ve yöneticinin onayı gerekiyor.");
        return "redirect:/takas";
    }

    @PostMapping("/{id}/kabul")
    public String kabul(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            takasService.hedefKabulEt(id, currentUserService.personelGerekli());
            redirectAttributes.addFlashAttribute("mesaj", "Takas talebini kabul ettiniz. Yönetici onayı bekleniyor.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("hata", ex.getMessage());
        }
        return "redirect:/takas";
    }

    @PostMapping("/{id}/reddet")
    public String reddet(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        try {
            takasService.hedefReddet(id, currentUserService.personelGerekli());
            redirectAttributes.addFlashAttribute("mesaj", "Takas talebi reddedildi.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("hata", ex.getMessage());
        }
        return "redirect:/takas";
    }

    @PostMapping("/{id}/onayla")
    public String onayla(@PathVariable Long id,
                         @RequestParam(value = "not", required = false) String not,
                         RedirectAttributes redirectAttributes) {
        try {
            KuralSonucu sonuc = takasService.yoneticiOnayla(id, not);
            if (!sonuc.gecerli()) {
                redirectAttributes.addFlashAttribute("hata",
                        "Takas kurallara uymuyor: " + String.join(" ", sonuc.hatalar()));
            } else {
                redirectAttributes.addFlashAttribute("mesaj", "Takas onaylandı ve vardiyalar güncellendi.");
                if (!sonuc.uyarilar().isEmpty()) {
                    redirectAttributes.addFlashAttribute("bilgi", "Uyarılar: " + String.join(" ", sonuc.uyarilar()));
                }
            }
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("hata", ex.getMessage());
        }
        return "redirect:/takas";
    }

    @PostMapping("/{id}/yonetici-reddet")
    public String yoneticiReddet(@PathVariable Long id,
                                 @RequestParam(value = "not", required = false) String not,
                                 RedirectAttributes redirectAttributes) {
        try {
            takasService.yoneticiReddet(id, not);
            redirectAttributes.addFlashAttribute("mesaj", "Takas talebi reddedildi.");
        } catch (IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("hata", ex.getMessage());
        }
        return "redirect:/takas";
    }
}
