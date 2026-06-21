package com.NgonNguLapTrinhJava.MiniSearchEngine.service;

import java.util.Collections;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.NgonNguLapTrinhJava.MiniSearchEngine.domain.entity.User;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserService userService;

    public CustomUserDetailsService(UserService userService) {
        this.userService = userService;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userService.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with email: " + email));

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getEmail())
                .password(user.getPassword())
                .authorities(Collections.emptyList())
                .disabled(!isActive(user))
                .accountLocked(isLocked(user))
                .build();
    }

    private boolean isActive(User user) {
        String status = user.getAccountStatus();
        return status == null || "ACTIVE".equalsIgnoreCase(status);
    }

    private boolean isLocked(User user) {
        String status = user.getAccountStatus();
        return "LOCKED".equalsIgnoreCase(status);
    }
}
