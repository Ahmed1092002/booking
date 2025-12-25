package com.example.booking.job;

import com.example.booking.booking.Booking;
import com.example.booking.booking.BookingRepository;
import com.example.booking.booking.BookingStatus;
import jakarta.annotation.PostConstruct;
import org.jobrunr.jobs.annotations.Job;
import org.jobrunr.scheduling.JobScheduler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Component
public class BookingCleanupJob {

    private final BookingRepository bookingRepository;
    private final JobScheduler jobScheduler;
    private final Logger logger = LoggerFactory.getLogger(BookingCleanupJob.class);

    public BookingCleanupJob(BookingRepository bookingRepository, JobScheduler jobScheduler) {
        this.bookingRepository = bookingRepository;
        this.jobScheduler = jobScheduler;
    }

    @PostConstruct
    public void scheduleRecurrently() {
        // Enqueue a recurring job: Run every day at midnight
        // Cron: 0 0 0 * * * (Standard Unix Cron)
        jobScheduler.scheduleRecurrently("cleanup-bookings", "0 0 0 * * *", this::cleanupUnconfirmedBookings);
    }

    @Job(name = "Auto-cancel expired pending bookings")
    @Transactional
    public void cleanupUnconfirmedBookings() {
        logger.info("Starting booking cleanup job (JobRunr)...");

        List<Booking> allBookings = bookingRepository.findAll();

        allBookings.stream()
                .filter(b -> b.getStatus() == BookingStatus.PENDING)
                .filter(b -> b.getCheckInDate().isBefore(LocalDate.now()))
                .forEach(b -> {
                    logger.info("Auto-cancelling expired booking: {}", b.getId());
                    b.setStatus(BookingStatus.CANCELLED);
                });

        logger.info("Booking cleanup job completed.");
    }
}
