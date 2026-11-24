package com.kocatepe.kafe.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Kafe is kurallari. Tum degerler application.yml uzerinden degistirilebilir.
 */
@ConfigurationProperties(prefix = "kafe")
public class AppProperties {

    private int openingHour = 9;
    private int closingHour = 23;
    private int fullTimeMaxSaat = 45;
    private int partTimeMaxSaat = 20;
    private int minPersonelSaat = 2;
    private int siparisBasinaPersonel = 15;
    private int toplamMasa = 30;
    private int tahminHaftaSayisi = 8;
    private int sentetikHaftaSayisi = 12;
    private long sentetikSeed = 42L;
    private boolean seedEnabled = true;

    public int getOpeningHour() {
        return openingHour;
    }

    public void setOpeningHour(int openingHour) {
        this.openingHour = openingHour;
    }

    public int getClosingHour() {
        return closingHour;
    }

    public void setClosingHour(int closingHour) {
        this.closingHour = closingHour;
    }

    public int getFullTimeMaxSaat() {
        return fullTimeMaxSaat;
    }

    public void setFullTimeMaxSaat(int fullTimeMaxSaat) {
        this.fullTimeMaxSaat = fullTimeMaxSaat;
    }

    public int getPartTimeMaxSaat() {
        return partTimeMaxSaat;
    }

    public void setPartTimeMaxSaat(int partTimeMaxSaat) {
        this.partTimeMaxSaat = partTimeMaxSaat;
    }

    public int getMinPersonelSaat() {
        return minPersonelSaat;
    }

    public void setMinPersonelSaat(int minPersonelSaat) {
        this.minPersonelSaat = minPersonelSaat;
    }

    public int getSiparisBasinaPersonel() {
        return siparisBasinaPersonel;
    }

    public void setSiparisBasinaPersonel(int siparisBasinaPersonel) {
        this.siparisBasinaPersonel = siparisBasinaPersonel;
    }

    public int getToplamMasa() {
        return toplamMasa;
    }

    public void setToplamMasa(int toplamMasa) {
        this.toplamMasa = toplamMasa;
    }

    public int getTahminHaftaSayisi() {
        return tahminHaftaSayisi;
    }

    public void setTahminHaftaSayisi(int tahminHaftaSayisi) {
        this.tahminHaftaSayisi = tahminHaftaSayisi;
    }

    public int getSentetikHaftaSayisi() {
        return sentetikHaftaSayisi;
    }

    public void setSentetikHaftaSayisi(int sentetikHaftaSayisi) {
        this.sentetikHaftaSayisi = sentetikHaftaSayisi;
    }

    public long getSentetikSeed() {
        return sentetikSeed;
    }

    public void setSentetikSeed(long sentetikSeed) {
        this.sentetikSeed = sentetikSeed;
    }

    public boolean isSeedEnabled() {
        return seedEnabled;
    }

    public void setSeedEnabled(boolean seedEnabled) {
        this.seedEnabled = seedEnabled;
    }
}
