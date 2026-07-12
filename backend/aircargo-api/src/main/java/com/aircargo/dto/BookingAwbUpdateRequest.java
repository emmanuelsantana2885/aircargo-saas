package com.aircargo.dto;

import jakarta.validation.constraints.NotBlank;

public class BookingAwbUpdateRequest {

    @NotBlank(message = "awbNumber must not be blank")
    private String awbNumber;

    public String getAwbNumber() {
        return awbNumber;
    }

    public void setAwbNumber(String awbNumber) {
        this.awbNumber = awbNumber;
    }
}
