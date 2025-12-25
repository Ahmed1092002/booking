package com.example.booking.promotion;

import com.example.booking.promotion.dto.CreateDiscountCodeRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/promotions")
public class PromotionController {

    private final PromotionService promotionService;
    private final CurrentUserService currentUserService;

    public PromotionController(PromotionService promotionService, CurrentUserService currentUserService) {
        this.promotionService = promotionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/discount-codes")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DiscountCode> createDiscountCode(@Valid @RequestBody CreateDiscountCodeRequest request) {
        DiscountCode discountCode = DiscountCode.builder()
                .code(request.getCode().toUpperCase())
                .type(request.getType())
                .discountValue(request.getValue())
                .validFrom(request.getValidFrom())
                .validUntil(request.getValidUntil())
                .maxUses(request.getMaxUses())
                .minBookingAmount(request.getMinBookingAmount())
                .currentUses(0)
                .active(true)
                .build();

        DiscountCode created = promotionService.createDiscountCode(discountCode);
        return new ResponseEntity<>(created, HttpStatus.CREATED);
    }

    @PostMapping("/validate-code")
    public ResponseEntity<DiscountCode> validateCode(
            @RequestParam String code,
            @RequestParam BigDecimal amount) {
        DiscountCode discount = promotionService.validateDiscountCode(code, amount);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/loyalty-points")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<LoyaltyPoints> getMyLoyaltyPoints() {
        User user = currentUserService.getCurrentUser();
        LoyaltyPoints points = promotionService.getUserLoyaltyPoints(user.getId());
        return ResponseEntity.ok(points);
    }

    @PostMapping("/loyalty-points/redeem")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<BigDecimal> redeemPoints(@RequestParam int points) {
        User user = currentUserService.getCurrentUser();
        BigDecimal discount = promotionService.redeemLoyaltyPoints(user, points);
        return ResponseEntity.ok(discount);
    }
}
