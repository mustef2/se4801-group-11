package com.sustainabilitytracker;

import com.sustainabilitytracker.config.SecurityConfig;
import com.sustainabilitytracker.controllers.EmissionController;
import com.sustainabilitytracker.dtos.responses.EmissionResponse;
import com.sustainabilitytracker.dtos.responses.EmissionSummaryResponse;
import com.sustainabilitytracker.repositories.UserRepository;
import com.sustainabilitytracker.security.JwtTokenProvider;
import com.sustainabilitytracker.services.EmissionService;
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

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmissionController.class)
@WithMockUser(roles = "ADMIN")
@Import(SecurityConfig.class)
@AutoConfigureMockMvc(addFilters = false)
class EmissionControllerTest {

    @Autowired
    private MockMvc mockMvc;

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
    void submitEmission_ShouldReturn201Created() throws Exception {
        EmissionResponse response = new EmissionResponse();
        response.setId(1L);

        when(emissionService.submitEmission(any())).thenReturn(response);

        mockMvc.perform(post("/emissions")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"companyId\":1,\"departmentId\":1,\"co2Amount\":1250.75,\"recordedAt\":\"2025-06-01\",\"scope\":\"SCOPE1\"}"))
                .andExpect(status().isCreated());
    }

    @Test
    void submitForApproval_ShouldReturn200() throws Exception {
        when(emissionService.submitForApproval(1L)).thenReturn(new EmissionResponse());

        mockMvc.perform(put("/emissions/1/submit"))
                .andExpect(status().isOk());
    }

    @Test
    void approveEmission_ShouldReturn200() throws Exception {
        when(emissionService.approveEmission(1L)).thenReturn(new EmissionResponse());

        mockMvc.perform(put("/emissions/1/approve"))
                .andExpect(status().isOk());
    }

    @Test
    void rejectEmission_ShouldReturn200() throws Exception {
        when(emissionService.rejectEmission(1L, "Invalid data")).thenReturn(new EmissionResponse());

        String json = """
                {
                    "reason": "Invalid data"
                }
                """;

        mockMvc.perform(put("/emissions/1/reject")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());
    }

    @Test
    void getEmissionsByCompany_ShouldReturn200() throws Exception {
        when(emissionService.getEmissionByCompany(1L))
                .thenReturn(List.of(new EmissionResponse()));

        mockMvc.perform(get("/emissions/company/1"))
                .andExpect(status().isOk());
    }

    @Test
    void getEmissionSummary_ShouldReturn200() throws Exception {
        when(emissionService.getEmissionSummary(anyLong(), any(LocalDate.class), any(LocalDate.class)))
                .thenReturn(new EmissionSummaryResponse());

        mockMvc.perform(get("/emissions/company/1/summary")
                        .param("startDate", "2025-06-01")
                        .param("endDate", "2025-06-30"))
                .andExpect(status().isOk());
    }
}