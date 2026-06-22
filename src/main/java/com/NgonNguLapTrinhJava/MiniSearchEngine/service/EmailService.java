package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailService.class);

    private final JavaMailSender mailSender;
    private final String mailFrom;

    public EmailService(JavaMailSender mailSender, @Value("${mini-search.mail.from}") String mailFrom) {
        this.mailSender = mailSender;
        this.mailFrom = mailFrom;
    }

    @Async("mailTaskExecutor")
    public void sendRegistrationOtp(String to, String name, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(to);
        message.setSubject("MiniSearchEngine verification OTP");
        message.setText(buildRegistrationOtpBody(name, otp));

        try {
            mailSender.send(message);
        } catch (MailException ex) {
            LOGGER.error("Failed to send registration OTP email to {}", to, ex);
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
