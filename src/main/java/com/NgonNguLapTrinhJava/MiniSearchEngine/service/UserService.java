package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.User;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqResendOtpDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqRegisterDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqVerifyOtpDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResLoginDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResUserDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.UserRepository;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.annotation.BusinessException;

@Service
@Validated
public class UserService {

    private static final String ACCOUNT_STATUS_ACTIVE = "ACTIVE";
    private static final String ACCOUNT_STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final OtpService otpService;

    public UserService(UserRepository userRepository, ObjectProvider<PasswordEncoder> passwordEncoderProvider,
            EmailService emailService, OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoderProvider.getIfAvailable(BCryptPasswordEncoder::new);
        this.emailService = emailService;
        this.otpService = otpService;
    }

    @Transactional
    public ResUserDTO register(ReqRegisterDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email đã được đăng ký");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus(ACCOUNT_STATUS_PENDING_VERIFICATION);
        user.setFailedLoginAttempts(0);
        user.setRefreshToken(null);

        String otp = assignNewRegistrationOtp(user);
        User savedUser = userRepository.save(user);
        emailService.sendRegistrationOtp(savedUser.getEmail(), savedUser.getName(), otp);

        return toUserDTO(savedUser);
    }

    @Transactional
    public ResUserDTO verifyRegistrationOtp(ReqVerifyOtpDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ hoặc đã hết hạn"));

        if (isActive(user)) {
            return toUserDTO(user);
        }

        if (user.getEmailVerificationOtp() == null || user.getEmailVerificationOtpExpiresAt() == null) {
            throw new BusinessException(HttpStatus.BAD_REQUEST,
                    "Mã OTP đã hết hạn hoặc chưa được gửi. Vui lòng yêu cầu mã OTP mới.");
        }

        if (user.getEmailVerificationOtpExpiresAt().isBefore(LocalDateTime.now())) {
            clearRegistrationOtp(user);
            userRepository.save(user);
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ");
        }

        if (!otpService.matches(request.getOtp(), user.getEmailVerificationOtp())) {
            throw new BusinessException(HttpStatus.BAD_REQUEST, "Mã OTP không hợp lệ");
        }

        user.setAccountStatus(ACCOUNT_STATUS_ACTIVE);
        clearRegistrationOtp(user);

        return toUserDTO(userRepository.save(user));
    }

    @Transactional
    public void resendRegistrationOtp(ReqResendOtpDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        User user = findByEmail(request.getEmail())
                .orElseThrow(() -> new BusinessException(HttpStatus.BAD_REQUEST,
                        "Email không tồn tại. Vui lòng đăng ký trước khi yêu cầu mã OTP mới."));

        if (isActive(user)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Tài khoản đã được xác minh");
        }

        String otp = assignNewRegistrationOtp(user);
        User savedUser = userRepository.save(user);
        emailService.sendRegistrationOtp(savedUser.getEmail(), savedUser.getName(), otp);
    }

    @Transactional(readOnly = true)
    public User handleFindByEmail(String email) {
        return findByEmail(email).orElse(null);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return Optional.empty();
        }
        return userRepository.findByEmail(normalizedEmail);
    }

    @Transactional(readOnly = true)
    public User handleFindByEmailAndRefreshToken(String email, String refreshToken) {
        String normalizedEmail = normalizeEmail(email);
        if (normalizedEmail == null || normalizedEmail.isBlank() || refreshToken == null || refreshToken.isBlank()) {
            return null;
        }
        return userRepository.findByEmailAndRefreshToken(normalizedEmail, refreshToken).orElse(null);
    }

    @Transactional(readOnly = true)
    public ResUserDTO getAccountByEmail(String email) {
        User user = findByEmail(email)
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));
        return toUserDTO(user);
    }

    @Transactional
    public void updateUserRefreshToken(String refreshToken, String email) {
        findByEmail(email).ifPresent(user -> {
            user.setRefreshToken(refreshToken);
            userRepository.save(user);
        });
    }

    @Transactional
    public void handleLogOutUser(String email) {
        findByEmail(email).ifPresent(user -> {
            user.setRefreshToken(null);
            userRepository.save(user);
        });
    }

    public ResLoginDTO toLoginDTO(User user) {
        if (user == null) {
            return null;
        }

        return ResLoginDTO.builder()
                .user(new ResLoginDTO.UserLogin(
                        user.getId(),
                        user.getEmail(),
                        user.getName()))
                .build();
    }

    public ResLoginDTO.UserGetAccount toAccountDTO(User user) {
        if (user == null) {
            return null;
        }

        return new ResLoginDTO.UserGetAccount(
                new ResLoginDTO.UserLogin(
                        user.getId(),
                        user.getEmail(),
                        user.getName()));
    }

    public ResUserDTO toUserDTO(User user) {
        if (user == null) {
            return null;
        }

        return ResUserDTO.builder()
                .id(user.getId())
                .email(user.getEmail())
                .name(user.getName())
                .accountStatus(user.getAccountStatus())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }

    private String assignNewRegistrationOtp(User user) {
        String otp = otpService.generateOtp();
        user.setEmailVerificationOtp(otpService.hashOtp(otp));
        user.setEmailVerificationOtpExpiresAt(otpService.calculateExpirationTime());
        return otp;
    }

    private void clearRegistrationOtp(User user) {
        user.setEmailVerificationOtp(null);
        user.setEmailVerificationOtpExpiresAt(null);
    }

    private boolean isActive(User user) {
        return user != null && ACCOUNT_STATUS_ACTIVE.equalsIgnoreCase(user.getAccountStatus());
    }

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
