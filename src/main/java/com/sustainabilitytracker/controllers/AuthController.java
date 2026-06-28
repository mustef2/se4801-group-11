package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.config.JwtProperties;
import com.sustainabilitytracker.dtos.UserDto;
import com.sustainabilitytracker.dtos.requests.ChangePasswordRequest;
import com.sustainabilitytracker.dtos.requests.LoginRequest;
import com.sustainabilitytracker.dtos.responses.JwtResponse;
import com.sustainabilitytracker.dtos.responses.LoginResponse;
import com.sustainabilitytracker.mapper.UserMapper;
import com.sustainabilitytracker.repositories.UserRepository;
import com.sustainabilitytracker.security.JwtTokenProvider;
import com.sustainabilitytracker.services.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

@RestController
@AllArgsConstructor
@RequestMapping("/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.login(request, response));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }

    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refresh(
            @CookieValue(value = "refreshToken", required = false) String refreshToken,
            HttpServletResponse response) {
        return ResponseEntity.ok(authService.refresh(refreshToken, response));
    }

}
