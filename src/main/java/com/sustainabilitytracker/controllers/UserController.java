package com.sustainabilitytracker.controllers;

import com.sustainabilitytracker.dtos.requests.RegisterUserRequest;
import com.sustainabilitytracker.dtos.responses.UserResponse;
import com.sustainabilitytracker.services.AuthService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

@RestController
@AllArgsConstructor
@RequestMapping("/users")
public class UserController {
    private final AuthService authService;
//    private final PasswordEncoder passwordEncoder;

    @GetMapping
    public ResponseEntity<?> getAllUser(){
        System.out.println("CLICKED!!");
        return ResponseEntity.status(HttpStatus.OK).body("OK");
    }

    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(
            UriComponentsBuilder uriBuilder,
            @Valid @RequestBody RegisterUserRequest request){
        var userResponse = authService.create(request);
        var uri = uriBuilder.path("/users/{id}").buildAndExpand(userResponse.getId()).toUri();
        return ResponseEntity.created(uri).body(userResponse);
    }
}
