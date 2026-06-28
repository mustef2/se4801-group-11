package com.sustainabilitytracker;

import com.sustainabilitytracker.config.JwtProperties;
import com.sustainabilitytracker.config.SecurityConfig;
import com.sustainabilitytracker.controllers.AuthController;
import com.sustainabilitytracker.dtos.requests.LoginRequest;
import com.sustainabilitytracker.dtos.responses.LoginResponse;
import com.sustainabilitytracker.repositories.UserRepository;
import com.sustainabilitytracker.security.JwtTokenProvider;
import com.sustainabilitytracker.services.AuthService;
import com.sustainabilitytracker.services.EmissionService;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthController.class)
@Import({JwtProperties.class, SecurityConfig.class})
@WithMockUser(username = "admin@test.com", roles = "ADMIN")
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AuthService authService;

    @MockBean
    private EmissionService emissionService;

    @MockBean
    private JwtTokenProvider jwtTokenProvider;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private UserDetailsService userDetailsService;

    @Test
    void login_Success_Returns200() throws Exception {
        // Given
        LoginResponse loginResponse = LoginResponse.builder()
                .accessToken("fake-access-token")
                .tokenType("Bearer")
                .email("test@example.com")
                .fullName("Test User")
                .role("ADMIN")
                .build();

        when(authService.login(any(LoginRequest.class), any(HttpServletResponse.class)))
                .thenReturn(loginResponse);

        String json = """
                {
                    "email": "test@example.com",
                    "password": "password123"
                }
                """;

        // When & Then
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("fake-access-token"))
                .andExpect(jsonPath("$.tokenType").value("Bearer"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}