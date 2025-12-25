package com.example.booking.calendar;

import com.example.booking.booking.Booking;
import com.example.booking.booking.BookingRepository;
import com.example.booking.booking.BookingStatus;
import com.example.booking.calendar.dto.BlockDateRequest;
import com.example.booking.calendar.dto.CalendarResponse;
import com.example.booking.calendar.dto.SeasonalPricingRequest;
import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ForbiddenException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.hotel.Room;
import com.example.booking.hotel.RoomRepository;
import com.example.booking.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.List;

@Service
public class CalendarService {

    private final RoomRepository roomRepository;
    private final BlockedDateRepository blockedDateRepository;
    private final SeasonalPricingRepository seasonalPricingRepository;
    private final BookingRepository bookingRepository;

    public CalendarService(RoomRepository roomRepository,
            BlockedDateRepository blockedDateRepository,
            SeasonalPricingRepository seasonalPricingRepository,
            BookingRepository bookingRepository) {
        this.roomRepository = roomRepository;
        this.blockedDateRepository = blockedDateRepository;
        this.seasonalPricingRepository = seasonalPricingRepository;
        this.bookingRepository = bookingRepository;
    }

    @Transactional
    public BlockedDate blockDates(User seller, Long roomId, BlockDateRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Validate seller owns the hotel
        if (!room.getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only block dates for your own rooms");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        // Check for overlapping blocked dates
        List<BlockedDate> overlapping = blockedDateRepository.findOverlappingBlockedDates(
                roomId, request.getStartDate(), request.getEndDate());
        if (!overlapping.isEmpty()) {
            throw new BadRequestException("Dates overlap with existing blocked period");
        }

        BlockedDate blockedDate = BlockedDate.builder()
                .room(room)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .reason(request.getReason())
                .notes(request.getNotes())
                .build();

        return blockedDateRepository.save(blockedDate);
    }

    @Transactional
    public void unblockDates(User seller, Long roomId, Long blockedDateId) {
        BlockedDate blockedDate = blockedDateRepository.findById(blockedDateId)
                .orElseThrow(() -> new ResourceNotFoundException("Blocked date not found"));

        // Validate seller owns the hotel
        if (!blockedDate.getRoom().getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only unblock dates for your own rooms");
        }

        blockedDateRepository.delete(blockedDate);
    }

    @Transactional
    public SeasonalPricing addSeasonalPricing(User seller, Long roomId, SeasonalPricingRequest request) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Validate seller owns the hotel
        if (!room.getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only set pricing for your own rooms");
        }

        // Validate dates
        if (request.getEndDate().isBefore(request.getStartDate())) {
            throw new BadRequestException("End date must be after start date");
        }

        SeasonalPricing pricing = SeasonalPricing.builder()
                .room(room)
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .pricePerNight(request.getPricePerNight())
                .seasonName(request.getSeasonName())
                .build();

        return seasonalPricingRepository.save(pricing);
    }

    @Transactional
    public void deleteSeasonalPricing(User seller, Long roomId, Long pricingId) {
        SeasonalPricing pricing = seasonalPricingRepository.findById(pricingId)
                .orElseThrow(() -> new ResourceNotFoundException("Seasonal pricing not found"));

        // Validate seller owns the hotel
        if (!pricing.getRoom().getHotel().getSeller().getId().equals(seller.getId())) {
            throw new ForbiddenException("You can only delete pricing for your own rooms");
        }

        seasonalPricingRepository.delete(pricing);
    }

    @Transactional(readOnly = true)
    public CalendarResponse getMonthlyCalendar(Long roomId, String month) {
        Room room = roomRepository.findById(roomId)
                .orElseThrow(() -> new ResourceNotFoundException("Room not found"));

        // Parse month (format: YYYY-MM)
        YearMonth yearMonth = YearMonth.parse(month);
        LocalDate startDate = yearMonth.atDay(1);
        LocalDate endDate = yearMonth.atEndOfMonth();

        // Get all blocked dates for the month
        List<BlockedDate> blockedDates = blockedDateRepository.findOverlappingBlockedDates(
                roomId, startDate, endDate);

        // Get all bookings for the month
        List<Booking> bookings = bookingRepository.findOverlappingBookings(roomId, startDate, endDate);

        // Get all seasonal pricing for the month
        List<SeasonalPricing> seasonalPricings = seasonalPricingRepository.findOverlappingSeasonalPricing(
                roomId, startDate, endDate);

        // Build calendar response
        List<CalendarResponse.DayAvailability> days = new ArrayList<>();
        LocalDate currentDate = startDate;

        while (!currentDate.isAfter(endDate)) {
            CalendarResponse.DayAvailability day = buildDayAvailability(
                    currentDate, room, blockedDates, bookings, seasonalPricings);
            days.add(day);
            currentDate = currentDate.plusDays(1);
        }

        return new CalendarResponse(month, days);
    }

    private CalendarResponse.DayAvailability buildDayAvailability(
            LocalDate date,
            Room room,
            List<BlockedDate> blockedDates,
            List<Booking> bookings,
            List<SeasonalPricing> seasonalPricings) {

        // Check if date is blocked
        for (BlockedDate blocked : blockedDates) {
            if (!date.isBefore(blocked.getStartDate()) && !date.isAfter(blocked.getEndDate())) {
                return new CalendarResponse.DayAvailability(
                        date, false, null, blocked.getReason().toString());
            }
        }

        // Check if date is booked
        for (Booking booking : bookings) {
            if (booking.getStatus() != BookingStatus.CANCELLED &&
                    !date.isBefore(booking.getCheckInDate()) && date.isBefore(booking.getCheckOutDate())) {
                return new CalendarResponse.DayAvailability(
                        date, false, null, "BOOKED");
            }
        }

        // Get price (seasonal or regular)
        BigDecimal price = room.getPricePerNight();
        String reason = null;

        for (SeasonalPricing pricing : seasonalPricings) {
            if (!date.isBefore(pricing.getStartDate()) && !date.isAfter(pricing.getEndDate())) {
                price = pricing.getPricePerNight();
                reason = pricing.getSeasonName();
                break;
            }
        }

        return new CalendarResponse.DayAvailability(date, true, price, reason);
    }
}
