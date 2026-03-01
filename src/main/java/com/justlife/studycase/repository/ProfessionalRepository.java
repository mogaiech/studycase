package com.justlife.studycase.repository;

import com.justlife.studycase.entity.ProfessionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {

    /**
     * Find professionals who have no overlapping bookings in the given time window (including a 30-minute break).
     */
    @Query("""
            SELECT cp FROM ProfessionalEntity cp
            WHERE cp.id NOT IN (
                SELECT p.id FROM BookingEntity bp
                JOIN bp.professionals p
                WHERE bp.status = 'CONFIRMED'
                AND bp.startDateTime < :windowEnd
                AND bp.endDateTime   > :windowStart
            )
            """)
    List<ProfessionalEntity> findAvailableProfessionals(
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd);

    /**
     * Find professionals from a specific vehicle who have no overlapping bookings in the given time window (including 30-minute break).
     */
    @Query("""
            SELECT cp FROM ProfessionalEntity cp
            WHERE cp.vehicle.id = :vehicleId
            AND cp.id NOT IN (
                SELECT p.id FROM BookingEntity bp
                JOIN bp.professionals p
                WHERE bp.status = 'CONFIRMED'
                AND bp.startDateTime < :windowEnd
                AND bp.endDateTime   > :windowStart
            )
            """)
    List<ProfessionalEntity> findAvailableProfessionalsByVehicle(
            @Param("vehicleId") Long vehicleId,
            @Param("windowStart") LocalDateTime windowStart,
            @Param("windowEnd") LocalDateTime windowEnd);
}


