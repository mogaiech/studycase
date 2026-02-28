package com.justlife.studycase.utils;

import com.justlife.studycase.dto.AvailabilityRequest;
import com.justlife.studycase.exception.BusinessException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
public class ValidatorTest {

    private Validator validator;

    private static final LocalDate DATE = LocalDate.of(2026, 3, 2);

    @BeforeEach
    void setUp() {
        validator = new Validator();
    }

    @Nested
    @DisplayName("Validate date")
    class ValidateNotFriday {
        @Test
        @DisplayName("Should reject when date is in the past")
        void shouldRejectPastDate() {
            LocalDate yesterday = LocalDate.now().minusDays(1);

            assertThatThrownBy(() -> validator.validatePastDate(yesterday))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("past");
        }

        @Test
        @DisplayName("Should reject when date is on Friday")
        void shouldRejectFriday() {
            LocalDate friday = LocalDate.of(2026, 2, 27); // Friday
            assertThatThrownBy(() -> validator.validateNotFriday(friday))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("Friday");
        }

        @Test
        @DisplayName("Should pass for working days")
        void shouldAcceptNonFriday() {
            LocalDate monday    = LocalDate.of(2026, 3, 2);
            LocalDate tuesday   = LocalDate.of(2026, 3, 3);
            LocalDate wednesday = LocalDate.of(2026, 3, 4);
            LocalDate thursday  = LocalDate.of(2026, 3, 5);
            LocalDate saturday  = LocalDate.of(2026, 3, 7);
            LocalDate sunday    = LocalDate.of(2026, 3, 8);

            assertThatNoException().isThrownBy(() -> {
                validator.validateNotFriday(monday);
                validator.validateNotFriday(tuesday);
                validator.validateNotFriday(wednesday);
                validator.validateNotFriday(thursday);
                validator.validateNotFriday(saturday);
                validator.validateNotFriday(sunday);
            });
        }
    }

    @Nested
    @DisplayName("Validate within working hours")
    class ValidateWithinWorkingHours {

        @Test
        @DisplayName("Should pass for a slot entirely within working hours (10:00–12:00)")
        void shouldPassForValidSlot() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(10, 0));
            LocalDateTime end   = LocalDateTime.of(DATE, LocalTime.of(12, 0));

            assertThatNoException().isThrownBy(() -> validator.validateWithinWorkingHours(start, end));
        }

        @Test
        @DisplayName("Should pass when slot starts exactly at 08:00 (boundary)")
        void shouldPassWhenStartsAtWorkStart() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(8, 0));
            LocalDateTime end   = LocalDateTime.of(DATE, LocalTime.of(10, 0));

            assertThatNoException().isThrownBy(() -> validator.validateWithinWorkingHours(start, end));
        }

        @Test
        @DisplayName("Should pass when slot ends exactly at 22:00 (boundary)")
        void shouldPassWhenEndsAtWorkEnd() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(20, 0));
            LocalDateTime end   = LocalDateTime.of(DATE, LocalTime.of(22, 0));

            assertThatNoException().isThrownBy(() -> validator.validateWithinWorkingHours(start, end));
        }

        @Test
        @DisplayName("Should reject start before 08:00")
        void shouldRejectStartBeforeWorkHours() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(7, 30));
            LocalDateTime end   = LocalDateTime.of(DATE, LocalTime.of(9, 30));

            assertThatThrownBy(() -> validator.validateWithinWorkingHours(start, end))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("08:00");
        }

        @Test
        @DisplayName("Should reject end after 22:00")
        void shouldRejectEndAfterWorkHours() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(21, 0));
            LocalDateTime end   = LocalDateTime.of(DATE, LocalTime.of(23, 0));

            assertThatThrownBy(() -> validator.validateWithinWorkingHours(start, end))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("22:00");
        }

        @Test
        @DisplayName("Should reject a 2h slot starting at 21:00 (ends at 23:00)")
        void shouldRejectSlotEndingAfterWorkEnd() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.of(21, 0));
            LocalDateTime end   = start.plusHours(2); // 23:00

            assertThatThrownBy(() -> validator.validateWithinWorkingHours(start, end))
                    .isInstanceOf(BusinessException.class);
        }

        @Test
        @DisplayName("Should reject a slot starting at midnight (00:00)")
        void shouldRejectMidnightStart() {
            LocalDateTime start = LocalDateTime.of(DATE, LocalTime.MIDNIGHT);
            LocalDateTime end   = start.plusHours(2);

            assertThatThrownBy(() -> validator.validateWithinWorkingHours(start, end))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("08:00");
        }
    }

    @Nested
    @DisplayName("Validate duration")
    class ValidateDuration {

        @ParameterizedTest(name = "Should pass for valid duration: {0}h")
        @ValueSource(ints = {2, 4})
        @DisplayName("Should pass for valid durations")
        void shouldPassForValidDurations(int duration) {
            assertThatNoException().isThrownBy(() -> validator.validateDuration(duration));
        }

        @ParameterizedTest(name = "Should reject invalid duration: {0}h")
        @ValueSource(ints = {0, 1, 3, 5, 6, 8, 24})
        @DisplayName("Should reject invalid durations")
        void shouldRejectInvalidDurations(int duration) {
            assertThatThrownBy(() -> validator.validateDuration(duration))
                    .isInstanceOf(BusinessException.class)
                    .hasMessageContaining("2 or 4");
        }

        @Test
        @DisplayName("Should reject negative duration")
        void shouldRejectNegativeDuration() {
            assertThatThrownBy(() -> validator.validateDuration(-1))
                    .isInstanceOf(BusinessException.class);
        }
    }
}
