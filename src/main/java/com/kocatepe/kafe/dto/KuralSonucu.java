package com.kocatepe.kafe.dto;

import java.util.List;

public record KuralSonucu(List<String> hatalar, List<String> uyarilar) {

    public boolean gecerli() {
        return hatalar.isEmpty();
    }

    public static KuralSonucu bos() {
        return new KuralSonucu(List.of(), List.of());
    }
}
