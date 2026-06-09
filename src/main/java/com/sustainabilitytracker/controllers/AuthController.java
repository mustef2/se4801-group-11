package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.config.JwtProperties;
import com.sustainabilitytracker.dtos.UserDto;
import com.sustainabilitytracker.dtos.requests.ChangePasswordRequest;
import com.sustainabilitytracker.dtos.requests.LoginRequest;
import com.sustainabilitytracker.dtos.responses.JwtResponse;
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

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final AuthService authService;
    private final UserMapper userMapper;

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(
            @RequestBody LoginRequest request,
            HttpServletResponse response){
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        var accessToken = jwtTokenProvider.generateAccessToken(user);
        var refreshToken = jwtTokenProvider.generateRefreshToken(user);

        var cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtProperties.getRefreshTokenExpiration());
        cookie.setSecure(true);
        response.addCookie(cookie);

        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @PostMapping("/change-password")
    public ResponseEntity<Void> changePassword(@Valid @RequestBody ChangePasswordRequest request ) {
        authService.changePassword(request);
        return ResponseEntity.ok().build();
    }


    @PostMapping("/refresh")
    public ResponseEntity<JwtResponse> refresh(@CookieValue(value = "refreshToken") String refreshToken){
//        var jwt = jwtService.parseToken(refreshToken);
        if (refreshToken == null || jwtTokenProvider.isTokenValid(refreshToken)) return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();

        var user = userRepository.findById(Long.valueOf(jwtTokenProvider.extractUserIdFromToken(refreshToken))).orElseThrow();
        var accessToken = jwtTokenProvider.generateAccessToken(user);

        return ResponseEntity.ok(new JwtResponse(accessToken));
    }

    @GetMapping("/me")
    public ResponseEntity<UserDto> me(){
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        var userId =(Long) authentication.getPrincipal();

        var user = userRepository.findById(userId).orElse(null);
        if (user == null) return ResponseEntity.notFound().build();
        var userDto = userMapper.toDto(user);
        return ResponseEntity.ok(userDto);
    }
}

