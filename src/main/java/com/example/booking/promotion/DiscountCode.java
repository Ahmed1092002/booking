package com.example.booking.promotion;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "discount_codes")
public class DiscountCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DiscountType type; // PERCENTAGE, FIXED_AMOUNT

    @Column(name = "discount_value", nullable = false)
    private BigDecimal discountValue;

    @Column(name = "valid_from", nullable = false)
    private LocalDate validFrom;

    @Column(name = "valid_until", nullable = false)
    private LocalDate validUntil;

    @Column(name = "max_uses")
    private Integer maxUses;

    @Column(name = "current_uses")
    private Integer currentUses = 0;

    @Column(name = "min_booking_amount")
    private BigDecimal minBookingAmount;

    @Column(nullable = false)
    private Boolean active = true;
}
