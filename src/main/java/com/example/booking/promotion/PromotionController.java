package com.example.booking.promotion;

import com.example.booking.promotion.dto.CreateDiscountCodeRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/promotions")
@Tag(name = "Promotions", description = "Discount codes and loyalty points management")
public class PromotionController {

    private final PromotionService promotionService;
    private final CurrentUserService currentUserService;

    public PromotionController(PromotionService promotionService, CurrentUserService currentUserService) {
        this.promotionService = promotionService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/discount-codes")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Create discount code", description = "Create a new discount code. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Discount code created", content = @Content(schema = @Schema(implementation = DiscountCode.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<DiscountCode> createDiscountCode(@Valid @RequestBody CreateDiscountCodeRequest request) {
        DiscountCode discountCode = DiscountCode.builder()
                .code(request.getCode().toUpperCase())
                .description(request.getDescription())
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

    @GetMapping("/discount-codes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get discount code by ID", description = "Retrieve a single discount code. Admin only.")
    public ResponseEntity<DiscountCode> getDiscountCode(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.getDiscountCodeById(id));
    }

    @PutMapping("/discount-codes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Update discount code", description = "Update an existing discount code. Admin only.")
    public ResponseEntity<DiscountCode> updateDiscountCode(
            @PathVariable Long id,
            @Valid @RequestBody com.example.booking.promotion.dto.UpdateDiscountCodeRequest request) {
        return ResponseEntity.ok(promotionService.updateDiscountCode(id, request));
    }

    @DeleteMapping("/discount-codes/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete discount code", description = "Delete a discount code. Admin only.")
    public ResponseEntity<Void> deleteDiscountCode(@PathVariable Long id) {
        promotionService.deleteDiscountCode(id);
        return ResponseEntity.noContent().build();
    }

    @PatchMapping("/discount-codes/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Toggle status", description = "Toggle discount code active/inactive status")
    public ResponseEntity<DiscountCode> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(promotionService.toggleDiscountCodeStatus(id));
    }

    @GetMapping("/discount-codes")
    @PreAuthorize("hasRole('ADMIN')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get all discount codes", description = "Retrieve all created discount codes with optional filtering. Admin only.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Discount codes retrieved", content = @Content(schema = @Schema(implementation = DiscountCode.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Admin role required")
    })
    public ResponseEntity<java.util.List<DiscountCode>> getAllDiscountCodes(
            @Parameter(description = "Search by code") @RequestParam(required = false) String search,
            @Parameter(description = "Filter by active status") @RequestParam(required = false) Boolean active) {
        return ResponseEntity.ok(promotionService.getAllDiscountCodes(search, active));
    }

    @PostMapping("/validate-code")
    @Operation(summary = "Validate discount code", description = "Validate if a discount code is active and applicable for the given amount")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Code is valid", content = @Content(schema = @Schema(implementation = DiscountCode.class))),
            @ApiResponse(responseCode = "400", description = "Invalid or expired code")
    })
    public ResponseEntity<DiscountCode> validateCode(
            @Parameter(description = "Discount code", example = "SUMMER2024") @RequestParam String code,
            @Parameter(description = "Booking amount", example = "1000.00") @RequestParam BigDecimal amount) {
        DiscountCode discount = promotionService.validateDiscountCode(code, amount);
        return ResponseEntity.ok(discount);
    }

    @GetMapping("/loyalty-points")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Get my loyalty points", description = "Get loyalty points balance for the authenticated user")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Points retrieved", content = @Content(schema = @Schema(implementation = LoyaltyPoints.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<LoyaltyPoints> getMyLoyaltyPoints() {
        User user = currentUserService.getCurrentUser();
        LoyaltyPoints points = promotionService.getUserLoyaltyPoints(user.getId());
        return ResponseEntity.ok(points);
    }

    @PostMapping("/loyalty-points/redeem")
    @PreAuthorize("isAuthenticated()")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Redeem loyalty points", description = "Redeem loyalty points for discount. Returns discount amount.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Points redeemed successfully"),
            @ApiResponse(responseCode = "400", description = "Insufficient points"),
            @ApiResponse(responseCode = "401", description = "Unauthorized")
    })
    public ResponseEntity<BigDecimal> redeemPoints(
            @Parameter(description = "Points to redeem", example = "100") @RequestParam int points) {
        User user = currentUserService.getCurrentUser();
        BigDecimal discount = promotionService.redeemLoyaltyPoints(user, points);
        return ResponseEntity.ok(discount);
    }
}
