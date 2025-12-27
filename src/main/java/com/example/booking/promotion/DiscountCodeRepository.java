package com.example.booking.promotion;

import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.Optional;

public interface DiscountCodeRepository extends JpaRepository<DiscountCode, Long> {

        Optional<DiscountCode> findByCodeAndActiveTrue(String code);

        boolean existsByCode(String code);

        @org.springframework.data.jpa.repository.Query("SELECT d FROM DiscountCode d WHERE " +
                        "(:code IS NULL OR LOWER(d.code) LIKE LOWER(CONCAT('%', CAST(:code AS string), '%'))) AND " +
                        "(:active IS NULL OR d.active = :active)")
        java.util.List<DiscountCode> searchDiscountCodes(
                        @org.springframework.data.repository.query.Param("code") String code,
                        @org.springframework.data.repository.query.Param("active") Boolean active);
}
