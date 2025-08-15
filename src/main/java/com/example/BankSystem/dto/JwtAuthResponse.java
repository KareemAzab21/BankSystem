package com.example.BankSystem.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private Long userId;
    private String username;

    public JwtAuthResponse(String accessToken) {
        this.accessToken = accessToken;
    }

    public JwtAuthResponse(String accessToken, Long userId, String username) {
        this.accessToken = accessToken;
        this.userId = userId;
        this.username = username;
    }
}
