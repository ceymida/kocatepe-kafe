package com.kocatepe.kafe.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.kocatepe.kafe.dto.CsvImportResult;
import com.kocatepe.kafe.service.CsvImportService;

@Controller
@RequestMapping("/csv")
public class CsvController {

    private final CsvImportService csvImportService;

    public CsvController(CsvImportService csvImportService) {
        this.csvImportService = csvImportService;
    }

    @GetMapping
    public String sayfa(Model model) {
        model.addAttribute("aktifMenu", "csv");
        return "csv/upload";
    }

    @PostMapping("/yukle")
    public String yukle(@RequestParam("dosya") MultipartFile dosya, RedirectAttributes redirectAttributes) {
        CsvImportResult sonuc = csvImportService.importEt(dosya);
        redirectAttributes.addFlashAttribute("sonuc", sonuc);
        if (sonuc.getHatalar().isEmpty()) {
            redirectAttributes.addFlashAttribute("mesaj",
                    "CSV yüklendi. Eklenen: " + sonuc.getEklenen() + ", güncellenen: " + sonuc.getGuncellenen());
        } else {
            redirectAttributes.addFlashAttribute("hata",
                    "CSV kısmen yüklendi. Atlanan satır: " + sonuc.getAtlanan());
        }
        return "redirect:/csv";
    }
}
