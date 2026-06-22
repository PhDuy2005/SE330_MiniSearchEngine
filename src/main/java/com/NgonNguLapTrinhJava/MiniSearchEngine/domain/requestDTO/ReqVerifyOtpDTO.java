package com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ReqVerifyOtpDTO {

    @Email(message = "Email is invalid")
    @NotBlank(message = "Email is required")
    private String email;

    @NotBlank(message = "OTP is required")
    @Pattern(regexp = "\\d{6}", message = "OTP must be 6 digits")
    private String otp;
}
