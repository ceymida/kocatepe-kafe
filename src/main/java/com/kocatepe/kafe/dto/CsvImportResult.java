package com.kocatepe.kafe.dto;

import java.util.ArrayList;
import java.util.List;

public class CsvImportResult {

    private int eklenen;
    private int guncellenen;
    private int atlanan;
    private final List<String> hatalar = new ArrayList<>();

    public int getEklenen() {
        return eklenen;
    }

    public void setEklenen(int eklenen) {
        this.eklenen = eklenen;
    }

    public int getGuncellenen() {
        return guncellenen;
    }

    public void setGuncellenen(int guncellenen) {
        this.guncellenen = guncellenen;
    }

    public int getAtlanan() {
        return atlanan;
    }

    public void setAtlanan(int atlanan) {
        this.atlanan = atlanan;
    }

    public List<String> getHatalar() {
        return hatalar;
    }

    public int getToplam() {
        return eklenen + guncellenen;
    }
}
