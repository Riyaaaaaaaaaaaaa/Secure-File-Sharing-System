package com.securefile.service;

import com.securefile.model.Role;
import com.securefile.model.User;
import com.securefile.repository.UserRepository;
import com.securefile.security.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuditService auditService;
    private final UserDetailsService userDetailsService;

    private static final int MAX_FAILED_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 15; // minutes

    public String register(String username, String email, String password, HttpServletRequest request) {
        if (userRepository.existsByUsername(username)) {
            throw new RuntimeException("Username already exists");
        }
        if (userRepository.existsByEmail(email)) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setRole(Role.ROLE_USER);
        user.setEnabled(true);
        user.setAccountNonLocked(true);
        user.setFailedAttempt(0);

        userRepository.save(user);
        auditService.logEvent(user, "REGISTER", "User registered successfully", request);
        
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return jwtService.generateToken(userDetails);
    }

    public String login(String username, String password, HttpServletRequest request) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        if (!user.isAccountNonLocked()) {
            if (isLockTimeExpired(user)) {
                user.setAccountNonLocked(true);
                user.setFailedAttempt(0);
                user.setLockTime(null);
                userRepository.save(user);
            } else {
                throw new RuntimeException("Account is locked. Please try again later.");
            }
        }

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            
            if (authentication.isAuthenticated()) {
                user.setFailedAttempt(0);
                userRepository.save(user);
                auditService.logEvent(user, "LOGIN", "User logged in successfully", request);
                UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                return jwtService.generateToken(userDetails);
            }
        } catch (Exception e) {
            if (user.isEnabled() && user.isAccountNonLocked()) {
                if (user.getFailedAttempt() < MAX_FAILED_ATTEMPTS - 1) {
                    user.setFailedAttempt(user.getFailedAttempt() + 1);
                    userRepository.save(user);
                } else {
                    user.setAccountNonLocked(false);
                    user.setLockTime(LocalDateTime.now());
                    userRepository.save(user);
                    throw new RuntimeException("Account is locked due to 3 failed attempts");
                }
            }
            throw new RuntimeException("Invalid username or password");
        }
        
        throw new RuntimeException("Invalid username or password");
    }

    private boolean isLockTimeExpired(User user) {
        if (user.getLockTime() == null) {
            return false;
        }
        LocalDateTime lockTime = user.getLockTime();
        LocalDateTime currentTime = LocalDateTime.now();
        return lockTime.plusMinutes(LOCK_TIME_DURATION).isBefore(currentTime);
    }
} 