package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.NgonNguLapTrinhJava.MiniSearchEngine.util.annotation.BusinessException;

@Service
public class EmailService {

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender, @Value("${mini-search.mail.from}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    public void sendRegistrationOtp(String to, String name, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("MiniSearchEngine verification OTP");
        message.setText(buildRegistrationOtpBody(name, otp));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            throw new BusinessException(HttpStatus.INTERNAL_SERVER_ERROR, "Cannot send OTP email");
        }
    }

    private String buildRegistrationOtpBody(String name, String otp) {
        String displayName = name == null || name.isBlank() ? "there" : name.trim();
        return """
                Hi %s,

                Your MiniSearchEngine verification code is: %s

                This code will expire soon. If you did not create an account, please ignore this email.
                """.formatted(displayName, otp);
    }
}
