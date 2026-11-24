package com.kocatepe.kafe.controller;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import com.kocatepe.kafe.dto.YogunlukHaftasi;
import com.kocatepe.kafe.service.CurrentUserService;
import com.kocatepe.kafe.service.YogunlukService;

@Controller
@RequestMapping("/yogunluk")
public class YogunlukController {

    private final YogunlukService yogunlukService;
    private final CurrentUserService currentUserService;

    public YogunlukController(YogunlukService yogunlukService, CurrentUserService currentUserService) {
        this.yogunlukService = yogunlukService;
        this.currentUserService = currentUserService;
    }

    @GetMapping
    public String hafta(@RequestParam(value = "hafta", required = false) String haftaMetni, Model model) {
        LocalDate referans = tarihOku(haftaMetni, currentUserService.bugun());
        YogunlukHaftasi hafta = yogunlukService.hafta(referans);
        model.addAttribute("aktifMenu", "yogunluk");
        model.addAttribute("hafta", hafta);
        model.addAttribute("oncekiHafta", hafta.getHaftaBaslangici().minusDays(7));
        model.addAttribute("sonrakiHafta", hafta.getHaftaBaslangici().plusDays(7));
        model.addAttribute("bugun", currentUserService.bugun());
        return "yogunluk/index";
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
