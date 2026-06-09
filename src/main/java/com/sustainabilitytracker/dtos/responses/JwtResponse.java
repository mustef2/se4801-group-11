package com.sustainabilitytracker.dtos.responses;

import lombok.Data;

@Data
public class JwtResponse {
    private String accessToken;

    public JwtResponse(String token) {
        accessToken=token;
    }
}

