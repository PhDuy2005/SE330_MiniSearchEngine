package com.NgonNguLapTrinhJava.MiniSearchEngine.util;

import java.time.Instant;
import java.util.Map;
import java.util.Optional;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.stereotype.Service;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.responseDTO.ResLoginDTO;

@Service
public class SecurityUtil {

    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    private final JwtEncoder jwtEncoder;

    @Value("${mini-search.jwt.base64-secret}")
    private String jwtKey;

    @Value("${mini-search.jwt.access-token-validity-in-seconds}")
    private Long accessTokenExpiration;

    @Value("${mini-search.jwt.refresh-token-validity-in-seconds}")
    private Long refreshTokenExpiration;

    public SecurityUtil(JwtEncoder jwtEncoder) {
        this.jwtEncoder = jwtEncoder;
    }

    public String createAccessToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(accessTokenExpiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expirationTime)
                .subject(email)
                .claim("user", createUserClaim(dto))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String createRefreshToken(String email, ResLoginDTO dto) {
        Instant now = Instant.now();
        Instant expirationTime = now.plusSeconds(refreshTokenExpiration);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expirationTime)
                .subject(email)
                .claim("user", createUserClaim(dto))
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public Jwt checkValidRefreshToken(String token) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withSecretKey(getSecretKey())
                .macAlgorithm(JWT_ALGORITHM)
                .build();
        return jwtDecoder.decode(token);
    }

    public static Optional<String> getCurrentUserLogin() {
        SecurityContext securityContext = SecurityContextHolder.getContext();
        return Optional.ofNullable(extractPrincipal(securityContext.getAuthentication()));
    }

    private static String extractPrincipal(Authentication authentication) {
        if (authentication == null) {
            return null;
        }
        if (authentication.getPrincipal() instanceof UserDetails springSecurityUser) {
            return springSecurityUser.getUsername();
        }
        if (authentication.getPrincipal() instanceof Jwt jwt) {
            return jwt.getSubject();
        }
        if (authentication.getPrincipal() instanceof String principal) {
            return principal;
        }
        return null;
    }

    private Map<String, Object> createUserClaim(ResLoginDTO dto) {
        if (dto == null || dto.getUser() == null) {
            throw new IllegalArgumentException("User information is required to create token");
        }

        ResLoginDTO.UserLogin user = dto.getUser();
        return Map.of(
                "id", user.getId(),
                "email", user.getEmail(),
                "name", user.getName());
    }

    private SecretKey getSecretKey() {
        byte[] keyBytes = java.util.Base64.getDecoder().decode(jwtKey);
        return new SecretKeySpec(keyBytes, 0, keyBytes.length, JWT_ALGORITHM.getName());
    }
}
