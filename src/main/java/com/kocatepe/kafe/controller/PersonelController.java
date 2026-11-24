package com.kocatepe.kafe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.DayOfWeek;

import com.kocatepe.kafe.dto.PersonelForm;
import com.kocatepe.kafe.entity.CalismaTipi;
import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.Pozisyon;
import com.kocatepe.kafe.service.PersonelService;

import jakarta.validation.Valid;

@Controller
@RequestMapping("/personel")
public class PersonelController {

    private final PersonelService personelService;

    public PersonelController(PersonelService personelService) {
        this.personelService = personelService;
    }

    @GetMapping
    public String liste(Model model) {
        model.addAttribute("aktifMenu", "personel");
        model.addAttribute("personeller", personelService.tumu());
        return "personel/liste";
    }

    @GetMapping("/yeni")
    public String yeni(Model model) {
        model.addAttribute("aktifMenu", "personel");
        model.addAttribute("form", personelService.yeniForm());
        model.addAttribute("baslik", "Yeni Personel");
        ortakModel(model);
        return "personel/form";
    }

    @PostMapping("/yeni")
    public String yeniKaydet(@Valid @ModelAttribute("form") PersonelForm form, BindingResult bindingResult,
                             Model model, RedirectAttributes redirectAttributes) {
        return kaydet(form, bindingResult, model, redirectAttributes, "personel/form", "Yeni Personel");
    }

    @GetMapping("/{id}/duzenle")
    public String duzenle(@PathVariable Long id, Model model) {
        model.addAttribute("aktifMenu", "personel");
        model.addAttribute("form", personelService.form(id));
        model.addAttribute("baslik", "Personel Duzenle");
        ortakModel(model);
        return "personel/form";
    }

    @PostMapping("/{id}/duzenle")
    public String duzenleKaydet(@PathVariable Long id, @Valid @ModelAttribute("form") PersonelForm form,
                                BindingResult bindingResult, Model model,
                                RedirectAttributes redirectAttributes) {
        form.setId(id);
        return kaydet(form, bindingResult, model, redirectAttributes, "personel/form", "Personel Duzenle");
    }

    @PostMapping("/{id}/durum")
    public String durum(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        Personel personel = personelService.durumDegistir(id);
        redirectAttributes.addFlashAttribute("mesaj",
                personel.getAdSoyad() + (personel.isAktif() ? " aktif hale getirildi." : " pasife alındı."));
        return "redirect:/personel";
    }

    private String kaydet(PersonelForm form, BindingResult bindingResult, Model model,
                          RedirectAttributes redirectAttributes, String sablon, String baslik) {
        model.addAttribute("aktifMenu", "personel");
        model.addAttribute("baslik", baslik);
        ortakModel(model);
        if (bindingResult.hasErrors()) {
            return sablon;
        }
        try {
            personelService.kaydet(form);
        } catch (IllegalArgumentException ex) {
            bindingResult.reject("genel", ex.getMessage());
            return sablon;
        }
        redirectAttributes.addFlashAttribute("mesaj", "Personel kaydedildi.");
        return "redirect:/personel";
    }

    private void ortakModel(Model model) {
        model.addAttribute("pozisyonlar", Pozisyon.values());
        model.addAttribute("calismaTipleri", CalismaTipi.values());
        model.addAttribute("gunler", DayOfWeek.values());
    }
}
