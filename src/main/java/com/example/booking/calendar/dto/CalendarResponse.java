package com.example.booking.calendar.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CalendarResponse {
    private String month; // Format: YYYY-MM
    private List<DayAvailability> days;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class DayAvailability {
        private LocalDate date;
        private Boolean available;
        private BigDecimal price;
        private String reason; // Why unavailable or special pricing
    }
}
