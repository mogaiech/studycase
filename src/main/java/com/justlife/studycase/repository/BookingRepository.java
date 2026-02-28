package com.justlife.studycase.repository;

import com.justlife.studycase.entity.BookingEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface BookingRepository extends JpaRepository<BookingEntity, Long> {

    /**
     * Find all confirmed bookings for a professional on a given date.
     */
    @Query("""
            SELECT b FROM BookingEntity b
            JOIN b.professionals p
            WHERE b.status = 'CONFIRMED'
            AND p.id = :professionalId
            AND b.startDateTime >= :dayStart
            AND b.startDateTime < :dayEnd
            ORDER BY b.startDateTime ASC
            """)
    List<BookingEntity> findBookingsForProfessionalOnDate(
            @Param("professionalId") Long professionalId,
            @Param("dayStart") LocalDateTime dayStart,
            @Param("dayEnd") LocalDateTime dayEnd);

    /**
     * Find confirmed bookings for a specific professional that overlap with the given time window.
     */
    @Query("""
            SELECT b FROM BookingEntity b
            JOIN b.professionals p
            WHERE b.status = 'CONFIRMED'
            AND p.id = :professionalId
            AND b.startDateTime < :windowEnd
            AND b.endDateTime   > :windowStart
            """)
    List<BookingEntity> findOverlappingBookingsForProfessional(
            @Param("professionalId") Long professionalId,
            @Param("windowStart")    LocalDateTime windowStart,
            @Param("windowEnd")      LocalDateTime windowEnd);
}
