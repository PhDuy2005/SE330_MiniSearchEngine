package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import java.util.Optional;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.User;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqRegisterDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResLoginDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResUserDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.repository.UserRepository;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.annotation.BusinessException;

@Service
@Validated
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, ObjectProvider<PasswordEncoder> passwordEncoderProvider) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoderProvider.getIfAvailable(BCryptPasswordEncoder::new);
    }

    @Transactional
    public ResUserDTO register(ReqRegisterDTO request) {
        if (request == null) {
            throw new IllegalArgumentException("Request body is required");
        }

        String email = normalizeEmail(request.getEmail());
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException(HttpStatus.CONFLICT, "Email already exists");
        }

        User user = new User();
        user.setName(request.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setAccountStatus("ACTIVE");
        user.setFailedLoginAttempts(0);

        return toUserDTO(userRepository.save(user));
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

    private String normalizeEmail(String email) {
        return email == null ? null : email.trim().toLowerCase();
    }
}
