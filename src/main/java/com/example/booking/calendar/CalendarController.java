package com.example.booking.calendar;

import com.example.booking.calendar.dto.BlockDateRequest;
import com.example.booking.calendar.dto.CalendarResponse;
import com.example.booking.calendar.dto.SeasonalPricingRequest;
import com.example.booking.security.CurrentUserService;
import com.example.booking.user.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/calendar")
public class CalendarController {

    private final CalendarService calendarService;
    private final CurrentUserService currentUserService;

    public CalendarController(CalendarService calendarService, CurrentUserService currentUserService) {
        this.calendarService = calendarService;
        this.currentUserService = currentUserService;
    }

    @PostMapping("/rooms/{roomId}/block")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<BlockedDate> blockDates(
            @PathVariable Long roomId,
            @Valid @RequestBody BlockDateRequest request) {
        User seller = currentUserService.getCurrentUser();
        BlockedDate blockedDate = calendarService.blockDates(seller, roomId, request);
        return new ResponseEntity<>(blockedDate, HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{roomId}/block/{blockedDateId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> unblockDates(
            @PathVariable Long roomId,
            @PathVariable Long blockedDateId) {
        User seller = currentUserService.getCurrentUser();
        calendarService.unblockDates(seller, roomId, blockedDateId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/rooms/{roomId}/seasonal-pricing")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<SeasonalPricing> addSeasonalPricing(
            @PathVariable Long roomId,
            @Valid @RequestBody SeasonalPricingRequest request) {
        User seller = currentUserService.getCurrentUser();
        SeasonalPricing pricing = calendarService.addSeasonalPricing(seller, roomId, request);
        return new ResponseEntity<>(pricing, HttpStatus.CREATED);
    }

    @DeleteMapping("/rooms/{roomId}/seasonal-pricing/{pricingId}")
    @PreAuthorize("hasRole('SELLER')")
    public ResponseEntity<Void> deleteSeasonalPricing(
            @PathVariable Long roomId,
            @PathVariable Long pricingId) {
        User seller = currentUserService.getCurrentUser();
        calendarService.deleteSeasonalPricing(seller, roomId, pricingId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/rooms/{roomId}")
    public ResponseEntity<CalendarResponse> getMonthlyCalendar(
            @PathVariable Long roomId,
            @RequestParam String month) { // Format: YYYY-MM
        CalendarResponse calendar = calendarService.getMonthlyCalendar(roomId, month);
        return ResponseEntity.ok(calendar);
    }
}
