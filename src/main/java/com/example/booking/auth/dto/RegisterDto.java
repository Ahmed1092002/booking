package com.example.booking.auth.dto;

import lombok.Data;
import java.util.Set;

@Data
public class RegisterDto {
    private String fullName;
    private String email;
    private String password;
    private Set<String> roles; // "ROLE_SELLER", "ROLE_CUSTOMER"
}
