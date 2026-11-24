package com.kocatepe.kafe.controller;

import java.time.DayOfWeek;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.service.CurrentUserService;
import com.kocatepe.kafe.service.MusaitlikService;

@Controller
@RequestMapping("/musaitlik")
public class MusaitlikController {

    private final CurrentUserService currentUserService;
    private final MusaitlikService musaitlikService;

    public MusaitlikController(CurrentUserService currentUserService, MusaitlikService musaitlikService) {
        this.currentUserService = currentUserService;
        this.musaitlikService = musaitlikService;
    }

    @GetMapping
    public String sayfa(Model model, RedirectAttributes redirectAttributes) {
        if (currentUserService.personel() == null) {
            redirectAttributes.addFlashAttribute("hata", "Bu hesap bir personel kaydıyla eşleşmiyor.");
            return "redirect:/";
        }
        Personel personel = currentUserService.personelGerekli();
        model.addAttribute("aktifMenu", "musaitlik");
        model.addAttribute("personel", personel);
        model.addAttribute("gunler", DayOfWeek.values());
        model.addAttribute("musaitlikler", musaitlikService.musaitlikHaritasi(personel));
        return "musaitlik/index";
    }

    @PostMapping
    public String guncelle(@RequestParam(value = "gunler", required = false) List<DayOfWeek> gunler,
                           RedirectAttributes redirectAttributes) {
        if (currentUserService.personel() == null) {
            redirectAttributes.addFlashAttribute("hata", "Bu hesap bir personel kaydıyla eşleşmiyor.");
            return "redirect:/";
        }
        Personel personel = currentUserService.personelGerekli();
        musaitlikService.guncelle(personel, gunler);
        redirectAttributes.addFlashAttribute("mesaj", "Müsaitlik bilgileriniz güncellendi.");
        return "redirect:/musaitlik";
    }
}
