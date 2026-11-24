package com.kocatepe.kafe.dto;

import java.util.List;

public class OtoPlanSonucu {

    private final int olusturulanVardiya;
    private final List<String> eksikler;

    public OtoPlanSonucu(int olusturulanVardiya, List<String> eksikler) {
        this.olusturulanVardiya = olusturulanVardiya;
        this.eksikler = eksikler;
    }

    public int getOlusturulanVardiya() {
        return olusturulanVardiya;
    }

    public List<String> getEksikler() {
        return eksikler;
    }
}
