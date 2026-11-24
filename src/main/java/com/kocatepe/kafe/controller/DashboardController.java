package com.kocatepe.kafe.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import com.kocatepe.kafe.entity.Personel;
import com.kocatepe.kafe.entity.TakasDurum;
import com.kocatepe.kafe.entity.TakasTalebi;
import com.kocatepe.kafe.entity.Vardiya;
import com.kocatepe.kafe.entity.VardiyaDurum;
import com.kocatepe.kafe.repository.PersonelRepository;
import com.kocatepe.kafe.repository.TakasTalebiRepository;
import com.kocatepe.kafe.repository.VardiyaRepository;
import com.kocatepe.kafe.service.CurrentUserService;

@Controller
public class DashboardController {

    private final CurrentUserService currentUserService;
    private final PersonelRepository personelRepository;
    private final VardiyaRepository vardiyaRepository;
    private final TakasTalebiRepository takasTalebiRepository;

    public DashboardController(CurrentUserService currentUserService, PersonelRepository personelRepository,
                               VardiyaRepository vardiyaRepository, TakasTalebiRepository takasTalebiRepository) {
        this.currentUserService = currentUserService;
        this.personelRepository = personelRepository;
        this.vardiyaRepository = vardiyaRepository;
        this.takasTalebiRepository = takasTalebiRepository;
    }

    @GetMapping("/")
    public String panel(Model model) {
        LocalDate bugun = currentUserService.bugun();
        boolean yonetici = currentUserService.yoneticiMi();
        model.addAttribute("aktifMenu", "panel");
        model.addAttribute("yonetici", yonetici);
        model.addAttribute("bugun", bugun);

        if (yonetici) {
            List<Vardiya> bugunkuVardiyalar = vardiyaRepository.findByTarihBetweenOrderByTarihAscPersonelAdSoyadAsc(bugun, bugun);
            long bekleyenTakas = takasTalebiRepository.findByDurumOrderByOlusturmaTarihiDesc(TakasDurum.BEKLIYOR).size();
            model.addAttribute("aktifPersonel", personelRepository.findByAktifTrueOrderByAdSoyadAsc().size());
            model.addAttribute("bugunkuVardiyalar", bugunkuVardiyalar);
            model.addAttribute("bekleyenTakas", bekleyenTakas);
            long taslak = vardiyaRepository.countByTarihBetweenAndDurum(bugun, bugun.plusDays(7), VardiyaDurum.TASLAK);
            model.addAttribute("taslakVardiya", taslak);
        } else {
            Personel personel = currentUserService.personelGerekli();
            List<Vardiya> yaklasan = vardiyaRepository
                    .findByPersonelIdAndTarihGreaterThanEqualAndDurumOrderByTarihAsc(
                            personel.getId(), bugun, VardiyaDurum.YAYINDA);
            model.addAttribute("personel", personel);
            model.addAttribute("yaklasanVardiyalar", yaklasan.stream().limit(8).toList());
            List<TakasTalebi> gelenTalepler = takasTalebiRepository
                    .findByHedefPersonelIdOrderByOlusturmaTarihiDesc(personel.getId());
            model.addAttribute("gelenTalepler", gelenTalepler.stream()
                    .filter(t -> t.getDurum() == TakasDurum.BEKLIYOR && t.getHedefOnayi() == null)
                    .toList());
        }
        return "dashboard";
    }
}
