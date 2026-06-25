package com.sustainabilitytracker.services;

import com.sustainabilitytracker.sustainabilitytracker.config.JwtProperties;
import com.sustainabilitytracker.sustainabilitytracker.dtos.request.ChangePasswordRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.request.LoginRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.request.RegisterUserRequest;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.LoginResponse;
import com.sustainabilitytracker.sustainabilitytracker.dtos.response.UserResponse;
import com.sustainabilitytracker.sustainabilitytracker.entities.Company;
import com.sustainabilitytracker.sustainabilitytracker.entities.Department;
import com.sustainabilitytracker.sustainabilitytracker.entities.User;
import com.sustainabilitytracker.sustainabilitytracker.enums.Role;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.BadRequestException;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.DuplicateResourceException;
import com.sustainabilitytracker.sustainabilitytracker.exceptions.ResourceNotFoundException;
import com.sustainabilitytracker.sustainabilitytracker.mappers.UserMapper;
import com.sustainabilitytracker.sustainabilitytracker.repositories.CompanyRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.DepartmentRepository;
import com.sustainabilitytracker.sustainabilitytracker.repositories.UserRepository;
import com.sustainabilitytracker.sustainabilitytracker.security.JwtTokenProvider;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final DepartmentRepository departmentRepository;
    private final AuthenticationManager authenticationManager;
    private final JwtTokenProvider jwtTokenProvider;
    private final JwtProperties jwtProperties;
    private final PasswordEncoder passwordEncoder;
    private final UserMapper userMapper;

    @Transactional
    public UserResponse create(RegisterUserRequest request) {

        String normalizedEmail = request.getEmail()
                .toLowerCase().trim();

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException(
                    "User with email '" + normalizedEmail + "' already exists"
            );
        }

        User user = userMapper.toEntity(request);
        user.setEmail(normalizedEmail);
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setIsFirstLogin(true);
        user.setIsActive(true);

        Role role = request.getRole();

        if (role == Role.DEPT_MANAGER || role == Role.EMPLOYEE) {

            if (request.getDepartmentId() == null) {
                throw new BadRequestException(
                        "Department is required for DEPT_MANAGER and EMPLOYEE"
                );
            }

            Department department = departmentRepository
                    .findById(request.getDepartmentId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Department not found with id: "
                                    + request.getDepartmentId()
                    ));

            user.assignToDepartment(department);
            user.setCompany(department.getCompany());

        } else if (role == Role.SUSTAINABILITY_MANAGER) {

            if (request.getCompanyId() == null) {
                throw new BadRequestException(
                        "Company is required for SUSTAINABILITY_MANAGER"
                );
            }

            Company company = companyRepository
                    .findById(request.getCompanyId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Company not found with id: " + request.getCompanyId()
                    ));

            user.setCompany(company);
            user.setDepartment(null);

        } else if (role == Role.AUDITOR) {

            user.setCompany(null);
            user.setDepartment(null);

        } else if (role == Role.ADMIN) {

            if (request.getCompanyId() != null
                    || request.getDepartmentId() != null) {
                throw new BadRequestException(
                        "ADMIN should not have companyId or departmentId"
                );
            }

            user.setCompany(null);
            user.setDepartment(null);
        }

        User savedUser = userRepository.save(user);

        log.info("User created: {} with role: {}",
                savedUser.getEmail(), savedUser.getRole());

        return userMapper.toResponse(savedUser);
    }

    public LoginResponse login(LoginRequest request,
                               HttpServletResponse response) {
        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getEmail().toLowerCase().trim(),
                            request.getPassword()
                    )
            );
        } catch (BadCredentialsException ex) {
            throw new BadRequestException("Invalid email or password");
        }

        User user = userRepository
                .findByEmail(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "User not found"
                ));

        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Account is deactivated");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        String refreshToken = jwtTokenProvider.generateRefreshToken(user);

        Cookie cookie = new Cookie("refreshToken", refreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtProperties.getRefreshTokenExpiration());
        cookie.setSecure(jwtProperties.isSecureCookie());
        response.addCookie(cookie);

        log.info("User logged in: {}", user.getEmail());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .companyId(user.getCompany() != null
                        ? user.getCompany().getId() : null)
                .departmentId(user.getDepartment() != null
                        ? user.getDepartment().getId() : null)
                .isFirstLogin(Boolean.TRUE.equals(user.getIsFirstLogin()))
                .build();
    }

    @Transactional
    public void changePassword(ChangePasswordRequest request) {

        User user = getCurrentUser();

        if (!passwordEncoder.matches(
                request.getCurrentPassword(), user.getPassword())) {
            throw new BadRequestException("Current password is wrong");
        }

        if (passwordEncoder.matches(
                request.getNewPassword(), user.getPassword())) {
            throw new BadRequestException(
                    "New password must be different from current password"
            );
        }

        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match");
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        user.setIsFirstLogin(false);

        userRepository.save(user);

        log.info("Password changed for user: {}", user.getEmail());
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll()
                .stream()
                .map(userMapper::toResponse)
                .collect(Collectors.toList());
    }

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder
                .getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UsernameNotFoundException(
                    "No authenticated user found"
            );
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User user) {
            return user;
        }

        if (principal instanceof UserDetails userDetails) {
            return userRepository.findByEmail(userDetails.getUsername())
                    .orElseThrow(() -> new UsernameNotFoundException(
                            "User not found: " + userDetails.getUsername()
                    ));
        }

        throw new UsernameNotFoundException("Unable to get current user");
    }

    @Transactional(readOnly = true)
    public LoginResponse refresh(String refreshToken, HttpServletResponse response) {

        if (refreshToken == null || refreshToken.isBlank()) {
            throw new BadRequestException("Refresh token is missing");
        }

        // Validate refresh token
        if (!jwtTokenProvider.isTokenValid(refreshToken)) {
            throw new BadRequestException("Invalid or expired refresh token");
        }

        // Extract user ID and fetch user
        Long userId = jwtTokenProvider.extractUserIdFromToken(refreshToken);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        // Security checks
        if (!Boolean.TRUE.equals(user.getIsActive())) {
            throw new BadRequestException("Account is deactivated");
        }

        // Generate new access token
        String newAccessToken = jwtTokenProvider.generateAccessToken(user);

        // Refresh token rotation (Security Best Practice)
        String newRefreshToken = jwtTokenProvider.generateRefreshToken(user);

        // Update refresh token cookie
        Cookie cookie = new Cookie("refreshToken", newRefreshToken);
        cookie.setHttpOnly(true);
        cookie.setPath("/auth/refresh");
        cookie.setMaxAge(jwtProperties.getRefreshTokenExpiration());
        cookie.setSecure(jwtProperties.isSecureCookie());   // false in dev, true in prod
        response.addCookie(cookie);

        log.info("Refresh token used for user: {}", user.getEmail());

        // Return response (same format as login)
        return LoginResponse.builder()
                .accessToken(newAccessToken)
                .tokenType("Bearer")
                .email(user.getEmail())
                .fullName(user.getFullName())
                .role(user.getRole().name())
                .companyId(user.getCompany() != null ? user.getCompany().getId() : null)
                .departmentId(user.getDepartment() != null ? user.getDepartment().getId() : null)
                .isFirstLogin(Boolean.TRUE.equals(user.getIsFirstLogin()))
                .build();
    }
}
