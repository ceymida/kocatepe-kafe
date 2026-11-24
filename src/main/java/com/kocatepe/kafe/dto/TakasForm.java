package com.kocatepe.kafe.dto;

import jakarta.validation.constraints.NotNull;

public class TakasForm {

    @NotNull(message = "Takas edilecek vardiya seçilmelidir")
    private Long vardiyaId;

    @NotNull(message = "Hedef personel seçilmelidir")
    private Long hedefPersonelId;

    public Long getVardiyaId() {
        return vardiyaId;
    }

    public void setVardiyaId(Long vardiyaId) {
        this.vardiyaId = vardiyaId;
    }

    public Long getHedefPersonelId() {
        return hedefPersonelId;
    }

    public void setHedefPersonelId(Long hedefPersonelId) {
        this.hedefPersonelId = hedefPersonelId;
    }
}
