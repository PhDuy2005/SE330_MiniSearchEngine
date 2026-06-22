package com.NgonNguLapTrinhJava.MiniSearchEngine.controller;

import org.apache.coyote.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.User;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqLoginDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqResendOtpDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqRegisterDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.requestDTO.ReqVerifyOtpDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResLoginDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResUserDTO;
import com.NgonNguLapTrinhJava.MiniSearchEngine.service.UserService;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.SecurityUtil;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.annotation.ApiMessage;
import com.NgonNguLapTrinhJava.MiniSearchEngine.util.annotation.BusinessException;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthenticationManager authenticationManager;
    private final SecurityUtil securityUtil;
    private final UserService userService;

    @Value("${mini-search.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public AuthController(AuthenticationManager authenticationManager, SecurityUtil securityUtil,
            UserService userService) {
        this.authenticationManager = authenticationManager;
        this.securityUtil = securityUtil;
        this.userService = userService;
    }

    @PostMapping("/register")
    @ApiMessage("Register user successfully. OTP sent to email")
    public ResponseEntity<ResUserDTO> register(@Valid @RequestBody ReqRegisterDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.register(request));
    }

    @PostMapping("/verify-otp")
    @ApiMessage("Verify OTP successfully")
    public ResponseEntity<ResUserDTO> verifyOtp(@Valid @RequestBody ReqVerifyOtpDTO request) {
        return ResponseEntity.ok(userService.verifyRegistrationOtp(request));
    }

    @PostMapping("/resend-otp")
    @ApiMessage("Resend OTP successfully")
    public ResponseEntity<Void> resendOtp(@Valid @RequestBody ReqResendOtpDTO request) {
        userService.resendRegistrationOtp(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/login")
    @ApiMessage("Login successfully")
    public ResponseEntity<ResLoginDTO> login(@Valid @RequestBody ReqLoginDTO request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (AuthenticationException ex) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        User currentUser = userService.handleFindByEmail(request.getEmail());
        if (currentUser == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }

        ResLoginDTO response = userService.toLoginDTO(currentUser);
        String accessToken = securityUtil.createAccessToken(currentUser.getEmail(), response);
        String refreshToken = securityUtil.createRefreshToken(currentUser.getEmail(), response);

        response.setAccessToken(accessToken);
        userService.updateUserRefreshToken(refreshToken, currentUser.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildRefreshTokenCookie(refreshToken, refreshTokenExpiration).toString())
                .body(response);
    }

    @GetMapping("/account")
    @ApiMessage("Get account successfully")
    public ResponseEntity<ResLoginDTO.UserGetAccount> getAccount() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));
        User currentUser = userService.handleFindByEmail(email);
        if (currentUser == null) {
            throw new BusinessException(HttpStatus.UNAUTHORIZED, "User is not authenticated");
        }
        return ResponseEntity.ok(userService.toAccountDTO(currentUser));
    }

    @GetMapping("/refresh")
    @ApiMessage("Refresh token successfully")
    public ResponseEntity<ResLoginDTO> refreshToken(
            @CookieValue(name = "refresh_token", required = false) String refreshToken) throws BadRequestException {
        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("No refresh token provided");
        }

        Jwt decodedToken;
        try {
            decodedToken = securityUtil.checkValidRefreshToken(refreshToken);
        } catch (Exception ex) {
            throw new BadRequestException("Invalid refresh token");
        }

        String email = decodedToken.getSubject();
        User currentUser = userService.handleFindByEmailAndRefreshToken(email, refreshToken);
        if (currentUser == null) {
            throw new BadRequestException("Invalid refresh token");
        }

        ResLoginDTO response = userService.toLoginDTO(currentUser);
        String accessToken = securityUtil.createAccessToken(currentUser.getEmail(), response);
        String newRefreshToken = securityUtil.createRefreshToken(currentUser.getEmail(), response);

        response.setAccessToken(accessToken);
        userService.updateUserRefreshToken(newRefreshToken, currentUser.getEmail());

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        buildRefreshTokenCookie(newRefreshToken, refreshTokenExpiration).toString())
                .body(response);
    }

    @PostMapping("/logout")
    @ApiMessage("Logout successfully")
    public ResponseEntity<Void> logout() {
        String email = SecurityUtil.getCurrentUserLogin()
                .orElseThrow(() -> new BusinessException(HttpStatus.UNAUTHORIZED, "User is not authenticated"));

        userService.handleLogOutUser(email);

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildDeleteRefreshTokenCookie().toString())
                .build();
    }

    private ResponseCookie buildRefreshTokenCookie(String refreshToken, Long maxAge) {
        return ResponseCookie.from("refresh_token", refreshToken)
                .httpOnly(true)
                .path("/")
                .maxAge(maxAge)
                .sameSite("Lax")
                .build();
    }

    private ResponseCookie buildDeleteRefreshTokenCookie() {
        return ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .path("/")
                .maxAge(0)
                .sameSite("Lax")
                .build();
    }
}
