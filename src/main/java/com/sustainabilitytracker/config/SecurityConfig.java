package com.sustainabilitytracker.config;

import com.sustainabilitytracker.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import com.sustainabilitytracker.filters.JwtAuthenticationFilter;


@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    //    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final UserDetailsService userDetailsService;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http, JwtAuthenticationFilter jwtAuthenticationFilter) {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(auth -> auth
                        // PUBLIC ENDPOINTS (No login needed)
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/swagger-ui/**", "/v3/api-docs/**").permitAll()

                        // USER MANAGEMENT (Admin only)
                        .requestMatchers(HttpMethod.POST, "/users/register")
                        .hasRole(Role.ADMIN.name())
                        .requestMatchers(HttpMethod.GET, "/users")
                        .hasRole(Role.ADMIN.name())

                        // COMPANY MANAGEMENT (Admin only)
                        .requestMatchers("/companies/**")
                        .hasRole(Role.ADMIN.name())

                        // DEPARTMENT MANAGEMENT (Admin only)
                        .requestMatchers("/departments/**")
                        .hasRole(Role.ADMIN.name())

                        // ENVIRONMENT DATA - SUBMIT (POST)
                        // Employee, Dept Manager, Sustainability Mgr
                        .requestMatchers(HttpMethod.POST,
                                "/emissions",
                                "/energies",
                                "/water",
                                "/waste",
                                "/social")
                        .hasAnyRole(
                                Role.EMPLOYEE.name(),
                                Role.DEPT_MANAGER.name(),
                                Role.SUSTAINABILITY_MANAGER.name()
                        )

                        // ENVIRONMENT DATA - SUBMIT FOR APPROVAL
                        // Employee, Dept Manager
                        .requestMatchers(HttpMethod.PUT,
                                "/emissions/*/submit",
                                "/energies/*/submit",
                                "/water/*/submit",
                                "/waste/*/submit",
                                "/social/*/submit")
                        .hasAnyRole(
                                Role.EMPLOYEE.name(),
                                Role.DEPT_MANAGER.name()
                        )

                        // ENVIRONMENT DATA - APPROVE
                        // Dept Manager, Sustainability Manager
                        .requestMatchers(HttpMethod.PUT,
                                "/emissions/*/approve",
                                "/energies/*/approve",
                                "/water/*/approve",
                                "/waste/*/approve",
                                "/social/*/approve")
                        .hasAnyRole(
                                Role.DEPT_MANAGER.name(),
                                Role.SUSTAINABILITY_MANAGER.name()
                        )

                        // ENVIRONMENT DATA - REJECT
                        // Dept Manager, Sustainability Manager
                        .requestMatchers(HttpMethod.PUT,
                                "/emissions/*/reject",
                                "/energies/*/reject",
                                "/water/*/reject",
                                "/waste/*/reject",
                                "/social/*/reject")
                        .hasAnyRole(
                                Role.DEPT_MANAGER.name(),
                                Role.SUSTAINABILITY_MANAGER.name()
                        )

                        // ENVIRONMENT DATA - VIEW (GET)
                        // All roles (filtered in service layer)
                        .requestMatchers(HttpMethod.GET,
                                "/emissions/company/*",
                                "/energies/company/*",
                                "/water/company/*",
                                "/waste/company/*",
                                "/social/company/*")
                        .hasAnyRole(
                                Role.EMPLOYEE.name(),
                                Role.DEPT_MANAGER.name(),
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        // ENVIRONMENT DATA - SUMMARY (GET)
                        .requestMatchers(HttpMethod.GET,
                                "/emissions/company/*/summary",
                                "/energies/company/*/summary",
                                "/water/company/*/summary",
                                "/waste/company/*/summary",
                                "/social/company/*/summary")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        // GOVERNANCE (Sustainability Mgr only)
                        .requestMatchers(HttpMethod.POST, "/governance")
                        .hasRole(Role.SUSTAINABILITY_MANAGER.name())

                        .requestMatchers(HttpMethod.PUT, "/governance/*/submit")
                        .hasRole(Role.SUSTAINABILITY_MANAGER.name())

                        .requestMatchers(HttpMethod.PUT, "/governance/*/approve")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name()
                        )

                        .requestMatchers(HttpMethod.PUT, "/governance/*/reject")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/governance/company/*")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/governance/company/*/summary")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        // SCORE CALCULATION
                        .requestMatchers(HttpMethod.POST, "/scores/calculate")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/scores/latest/*")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.DEPT_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/scores/history/*")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.DEPT_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        // REPORTS
                        .requestMatchers(HttpMethod.POST, "/reports")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/reports/company/*")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/reports/*/download")
                        .hasAnyRole(
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        // AUDIT
                        .requestMatchers(HttpMethod.GET, "/audits/pending")
                        .hasAnyRole(
                                Role.AUDITOR.name(),
                                Role.ADMIN.name()
                        )

                        .requestMatchers(HttpMethod.PUT, "/audits/reports/*/review")
                        .hasRole(Role.AUDITOR.name())

                        .requestMatchers(HttpMethod.GET, "/audits/reports/*/history")
                        .hasAnyRole(
                                Role.AUDITOR.name(),
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name()
                        )

                        // DASHBOARD
                        .requestMatchers(HttpMethod.GET, "/dashboard/company/*")
                        .hasAnyRole(
                                Role.DEPT_MANAGER.name(),
                                Role.SUSTAINABILITY_MANAGER.name(),
                                Role.ADMIN.name(),
                                Role.AUDITOR.name()
                        )

                        .requestMatchers(HttpMethod.GET, "/dashboard/admin")
                        .hasRole(Role.ADMIN.name())

                        // NOTIFICATIONS
                        .requestMatchers("/notifications/**")
                        .authenticated()

                        .anyRequest().authenticated()
                )
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling(ex -> {
                    ex.authenticationEntryPoint((req, res, ex1) -> res.setStatus(HttpStatus.UNAUTHORIZED.value()));
                    ex.accessDeniedHandler((req, res, ex1) -> res.setStatus(HttpStatus.FORBIDDEN.value()));
                });

        return http.build();
    }

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config
    ) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}