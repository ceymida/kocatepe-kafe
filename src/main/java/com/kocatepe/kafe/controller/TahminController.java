package com.kocatepe.kafe.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kocatepe.kafe.dto.TahminHaftasi;
import com.kocatepe.kafe.service.CurrentUserService;
import com.kocatepe.kafe.service.YogunlukService;

@Controller
@RequestMapping("/tahmin")
public class TahminController {

    private final YogunlukService yogunlukService;
    private final CurrentUserService currentUserService;

    public TahminController(YogunlukService yogunlukService, CurrentUserService currentUserService) {
        this.yogunlukService = yogunlukService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String hafta(@RequestParam(value = "hafta", required = false) String haftaMetni, Model model) {
        LocalDate varsayilan = currentUserService.bugun().plusWeeks(1);
        LocalDate referans = tarihOku(haftaMetni, varsayilan);
        TahminHaftasi hafta = yogunlukService.tahminHaftasi(referans);
        model.addAttribute("aktifMenu", "tahmin");
        model.addAttribute("hafta", hafta);
        model.addAttribute("oncekiHafta", hafta.getHaftaBaslangici().minusDays(7));
        model.addAttribute("sonrakiHafta", hafta.getHaftaBaslangici().plusDays(7));
        model.addAttribute("buHafta", currentUserService.bugun());
        return "yogunluk/tahmin";
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
