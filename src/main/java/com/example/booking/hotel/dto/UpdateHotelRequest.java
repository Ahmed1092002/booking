package com.example.booking.hotel.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateHotelRequest {

    private String name;

    private String city;

    private String address;

    private String googleMapUrl;

    private Set<String> amenities;
}
