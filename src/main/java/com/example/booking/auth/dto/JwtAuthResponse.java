package com.example.booking.auth.dto;

import com.example.booking.user.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JwtAuthResponse {
    private String accessToken;
    private String tokenType = "Bearer";
    private UserResponseDto user;

    public JwtAuthResponse(String accessToken, UserResponseDto user) {
        this.accessToken = accessToken;
        this.user = user;
    }
}
