package com.justlife.studycase.repository;

import com.justlife.studycase.dto.Professional;
import com.justlife.studycase.entity.ProfessionalEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface ProfessionalRepository extends JpaRepository<ProfessionalEntity, Long> {

    /**
     * Find professionals who have no overlapping bookings in the given time window
     * (taking in account 30-minute break before and after each booking).
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
    List<ProfessionalEntity> findAvailableProfessionals(LocalDateTime windowStart, LocalDateTime windowEnd);
}
