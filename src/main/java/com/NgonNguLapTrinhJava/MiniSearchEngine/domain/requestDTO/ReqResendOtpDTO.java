package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReqResendOtpDTO {

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;
}
