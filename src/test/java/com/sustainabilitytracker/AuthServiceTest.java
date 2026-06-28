package com.sustainabilitytracker;

import com.sustainabilitytracker.config.JwtProperties;
import com.sustainabilitytracker.dtos.requests.LoginRequest;
import com.sustainabilitytracker.dtos.responses.LoginResponse;
import com.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.exceptions.BadRequestException;
import com.sustainabilitytracker.repositories.UserRepository;
import com.sustainabilitytracker.security.JwtTokenProvider;
import com.sustainabilitytracker.services.AuthService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private HttpServletResponse response;

    @Mock
    private JwtProperties jwtProperties;

    @InjectMocks
    private AuthService authService;

    @Test
    void login_WithCorrectCredentials_ShouldSucceed() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.builder()
                .id(1L)
                .email("test@example.com")
                .role(Role.ADMIN)
                .isActive(true)
                .build();

//        when(jwtProperties.getRefreshTokenExpiration()).thenReturn(Math.toIntExact(7 * 24 * 60 * 60L));
//        when(jwtProperties.getAccessTokenExpiration()).thenReturn(Math.toIntExact(15 * 60L));


        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));
        when(jwtTokenProvider.generateAccessToken(user)).thenReturn("access.jwt.token");
        when(jwtTokenProvider.generateRefreshToken(user)).thenReturn("refresh.jwt.token");

        LoginResponse loginResponse = authService.login(request, response);

        assertNotNull(loginResponse);
        assertEquals("access.jwt.token", loginResponse.getAccessToken());

        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(jwtTokenProvider).generateAccessToken(user);
        verify(response).addCookie(any());
    }

    @Test
    void login_WithWrongPassword_ShouldThrowBadCredentialsException() {
        LoginRequest request = new LoginRequest("test@example.com", "wrongpass");

        doThrow(new BadRequestException("Bad credentials"))
                .when(authenticationManager).authenticate(any());

        assertThrows(BadRequestException.class,
                () -> authService.login(request, response));
    }

    @Test
    void login_InactiveAccount_ShouldThrowException() {
        LoginRequest request = new LoginRequest("test@example.com", "password123");
        User user = User.builder().email("test@example.com").isActive(false).build();

        when(userRepository.findByEmail(request.getEmail())).thenReturn(Optional.of(user));

        assertThrows(BadRequestException.class,
                () -> authService.login(request, response));
    }
}