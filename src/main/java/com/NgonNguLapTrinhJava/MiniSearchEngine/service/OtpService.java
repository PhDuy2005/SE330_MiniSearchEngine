package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class OtpService {

    private final SecureRandom secureRandom = new SecureRandom();
    private final PasswordEncoder passwordEncoder;
    private final long otpValidityInMinutes;

    public OtpService(PasswordEncoder passwordEncoder,
            @Value("${mini-search.otp.validity-in-minutes}") long otpValidityInMinutes) {
        this.passwordEncoder = passwordEncoder;
        this.otpValidityInMinutes = otpValidityInMinutes;
    }

    public String generateOtp() {
        return String.format("%06d", secureRandom.nextInt(1_000_000));
    }

    public String hashOtp(String otp) {
        return passwordEncoder.encode(otp);
    }

    public boolean matches(String rawOtp, String hashedOtp) {
        return rawOtp != null && hashedOtp != null && passwordEncoder.matches(rawOtp, hashedOtp);
    }

    public LocalDateTime calculateExpirationTime() {
        return LocalDateTime.now().plusMinutes(otpValidityInMinutes);
    }
}
