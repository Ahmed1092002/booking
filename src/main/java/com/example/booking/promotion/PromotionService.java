package com.example.booking.promotion;

import com.example.booking.exception.BadRequestException;
import com.example.booking.exception.ResourceNotFoundException;
import com.example.booking.user.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
public class PromotionService {

    private final DiscountCodeRepository discountCodeRepository;
    private final LoyaltyPointsRepository loyaltyPointsRepository;

    public PromotionService(DiscountCodeRepository discountCodeRepository,
            LoyaltyPointsRepository loyaltyPointsRepository) {
        this.discountCodeRepository = discountCodeRepository;
        this.loyaltyPointsRepository = loyaltyPointsRepository;
    }

    @Transactional
    public DiscountCode createDiscountCode(DiscountCode discountCode) {
        if (discountCodeRepository.existsByCode(discountCode.getCode())) {
            throw new BadRequestException("Discount code already exists");
        }
        return discountCodeRepository.save(discountCode);
    }

    @Transactional(readOnly = true)
    public DiscountCode validateDiscountCode(String code, BigDecimal bookingAmount) {
        DiscountCode discount = discountCodeRepository.findByCodeAndActiveTrue(code)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or inactive discount code"));

        LocalDate today = LocalDate.now();
        if (today.isBefore(discount.getValidFrom()) || today.isAfter(discount.getValidUntil())) {
            throw new BadRequestException("Discount code is not valid for current date");
        }

        if (discount.getMaxUses() != null && discount.getCurrentUses() >= discount.getMaxUses()) {
            throw new BadRequestException("Discount code has reached maximum uses");
        }

        if (discount.getMinBookingAmount() != null &&
                bookingAmount.compareTo(discount.getMinBookingAmount()) < 0) {
            throw new BadRequestException("Booking amount does not meet minimum requirement");
        }

        return discount;
    }

    @Transactional
    public BigDecimal applyDiscount(String code, BigDecimal originalAmount) {
        DiscountCode discount = validateDiscountCode(code, originalAmount);

        BigDecimal discountAmount;
        if (discount.getType() == DiscountType.PERCENTAGE) {
            discountAmount = originalAmount.multiply(discount.getDiscountValue()).divide(BigDecimal.valueOf(100));
        } else {
            discountAmount = discount.getDiscountValue();
        }

        // Increment usage
        discount.setCurrentUses(discount.getCurrentUses() + 1);
        discountCodeRepository.save(discount);

        return originalAmount.subtract(discountAmount).max(BigDecimal.ZERO);
    }

    @Transactional
    public void addLoyaltyPoints(User user, BigDecimal amountSpent) {
        LoyaltyPoints points = loyaltyPointsRepository.findByUserId(user.getId())
                .orElseGet(() -> {
                    LoyaltyPoints newPoints = LoyaltyPoints.builder()
                            .user(user)
                            .totalPoints(0)
                            .availablePoints(0)
                            .build();
                    return loyaltyPointsRepository.save(newPoints);
                });

        // Earn 1 point per $1 spent
        int earnedPoints = amountSpent.intValue();
        points.setTotalPoints(points.getTotalPoints() + earnedPoints);
        points.setAvailablePoints(points.getAvailablePoints() + earnedPoints);

        loyaltyPointsRepository.save(points);
    }

    @Transactional
    public BigDecimal redeemLoyaltyPoints(User user, int pointsToRedeem) {
        LoyaltyPoints points = loyaltyPointsRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("No loyalty points found"));

        if (points.getAvailablePoints() < pointsToRedeem) {
            throw new BadRequestException("Insufficient loyalty points");
        }

        // 100 points = $10 discount
        BigDecimal discount = BigDecimal.valueOf(pointsToRedeem).divide(BigDecimal.valueOf(10));

        points.setAvailablePoints(points.getAvailablePoints() - pointsToRedeem);
        loyaltyPointsRepository.save(points);

        return discount;
    }

    @Transactional(readOnly = true)
    public LoyaltyPoints getUserLoyaltyPoints(Long userId) {
        return loyaltyPointsRepository.findByUserId(userId)
                .orElseGet(() -> LoyaltyPoints.builder()
                        .totalPoints(0)
                        .availablePoints(0)
                        .build());
    }

    @Transactional(readOnly = true)
    public java.util.List<DiscountCode> getAllDiscountCodes() {
        return discountCodeRepository.findAll();
    }
}
