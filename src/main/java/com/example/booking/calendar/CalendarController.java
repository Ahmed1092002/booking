package com.example.booking.calendar;

import com.example.booking.calendar.dto.BlockDateRequest;
import com.example.booking.calendar.dto.CalendarResponse;
import com.example.booking.calendar.dto.SeasonalPricingRequest;
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

@RestController
@RequestMapping("/api/calendar")
@Tag(name = "Calendar", description = "Room availability calendar and seasonal pricing management")
public class CalendarController {

    private final CalendarService calendarService;
    private final CurrentUserService currentUserService;

    public CalendarController(CalendarService calendarService, CurrentUserService currentUserService) {
        this.calendarService = calendarService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/rooms/{roomId}/block")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Block room dates", description = "Block specific dates for a room (maintenance, holidays, etc.). Only room owner can block dates.")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Dates blocked successfully", content = @Content(schema = @Schema(implementation = BlockedDate.class))),
            @ApiResponse(responseCode = "401", description = "Unauthorized"),
            @ApiResponse(responseCode = "403", description = "Forbidden - Not the room owner"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<BlockedDate> blockDates(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Valid @RequestBody BlockDateRequest request) {
        User seller = currentUserService.getCurrentUser();
        BlockedDate blockedDate = calendarService.blockDates(seller, roomId, request);
        return new ResponseEntity<>(blockedDate, HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{roomId}/block/{blockedDateId}")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Unblock room dates", description = "Remove a date block from a room")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Dates unblocked successfully"),
            @ApiResponse(responseCode = "404", description = "Room or blocked date not found")
    })
    public ResponseEntity<Void> unblockDates(
            @PathVariable Long roomId,
            @PathVariable Long blockedDateId) {
        User seller = currentUserService.getCurrentUser();
        calendarService.unblockDates(seller, roomId, blockedDateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/seasonal-pricing")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Add seasonal pricing", description = "Set special pricing for specific date ranges (peak season, holidays, etc.)")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Seasonal pricing added", content = @Content(schema = @Schema(implementation = SeasonalPricing.class))),
            @ApiResponse(responseCode = "400", description = "Invalid date range or price"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<SeasonalPricing> addSeasonalPricing(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Valid @RequestBody SeasonalPricingRequest request) {
        User seller = currentUserService.getCurrentUser();
        SeasonalPricing pricing = calendarService.addSeasonalPricing(seller, roomId, request);
        return new ResponseEntity<>(pricing, HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{roomId}/seasonal-pricing/{pricingId}")
    @PreAuthorize("hasRole('SELLER')")
    @SecurityRequirement(name = "bearerAuth")
    @Operation(summary = "Delete seasonal pricing", description = "Remove a seasonal pricing rule")
    public ResponseEntity<Void> deleteSeasonalPricing(
            @PathVariable Long roomId,
            @PathVariable Long pricingId) {
        User seller = currentUserService.getCurrentUser();
        calendarService.deleteSeasonalPricing(seller, roomId, pricingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}")
    @Operation(summary = "Get monthly calendar", description = "Retrieve room availability calendar for a specific month including blocked dates and seasonal pricing")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Calendar retrieved", content = @Content(schema = @Schema(implementation = CalendarResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid month format (use YYYY-MM)"),
            @ApiResponse(responseCode = "404", description = "Room not found")
    })
    public ResponseEntity<CalendarResponse> getMonthlyCalendar(
            @Parameter(description = "Room ID") @PathVariable Long roomId,
            @Parameter(description = "Month in YYYY-MM format", example = "2025-01") @RequestParam String month) {
        CalendarResponse calendar = calendarService.getMonthlyCalendar(roomId, month);
        return ResponseEntity.ok(calendar);
    }
}
