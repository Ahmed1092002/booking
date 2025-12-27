package com.example.booking.promotion.dto;

import com.example.booking.promotion.DiscountType;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UpdateDiscountCodeRequest {

    @Size(min = 3, max = 50, message = "Code must be between 3 and 50 characters")
    private String code;

    @Size(max = 255, message = "Description must be less than 255 characters")
    private String description;

    private DiscountType type;

    @Positive(message = "Value must be positive")
    private BigDecimal value;

    private LocalDate validFrom;

    private LocalDate validUntil;

    private Integer maxUses;

    private BigDecimal minBookingAmount;

    private Boolean active;
}
