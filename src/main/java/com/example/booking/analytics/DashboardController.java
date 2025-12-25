package com.example.booking.analytics;

import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/dashboard")
@Tag(name = "Dashboard", description = "Analytics dashboard endpoints for sellers")
@SecurityRequirement(name = "bearerAuth")
public class DashboardController {

    private final DashboardService dashboardService;
    private final CurrentUserService currentUserService;

    public DashboardController(DashboardService dashboardService, CurrentUserService currentUserService) {
        this.dashboardService = dashboardService;
        this.currentUserService = currentUserService;
    }

    @GetMapping("/seller")
    @PreAuthorize("hasRole('SELLER')")
    @Operation(summary = "Get seller dashboard analytics", description = "Retrieves comprehensive analytics data for the authenticated seller including revenue, bookings, and performance metrics")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Dashboard data retrieved successfully", content = @Content(mediaType = "application/json", schema = @Schema(implementation = Map.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized - Invalid or missing token"),
            @ApiResponse(responseCode = "403", description = "Forbidden - User does not have SELLER role")
    })
    public ResponseEntity<Map<String, Object>> getSellerDashboard() {
        User seller = currentUserService.getCurrentUser();
        return ResponseEntity.ok(dashboardService.getSellerDashboard(seller.getId()));
    }
}
