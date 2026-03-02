package com.justlife.studycase.utils;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public final class TestDates {

    private TestDates() {}

    public static final LocalDateTime TODAY = LocalDateTime.now();
    public static final LocalDate WORK_DATE = nextWorkday(LocalDate.now().plusDays(1));
    public static final LocalDateTime WORK_START = WORK_DATE.atTime(10, 0);
    public static final LocalDateTime BEFORE_WORK_HOURS = WORK_DATE.atTime(6, 0);
    public static final LocalDateTime LATE_START = WORK_DATE.atTime(19, 0);
    public static final LocalDateTime NEXT_DAY = nextWorkday(WORK_DATE.plusDays(2)).atTime(14, 0);
    public static final LocalDate FRIDAY_DATE = nextFriday();
    public static final LocalDateTime FRIDAY = FRIDAY_DATE.atTime(10, 0);
    public static final String WORK_DATE_STR = WORK_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE);
    public static final String FRIDAY_DATE_STR = FRIDAY_DATE.format(DateTimeFormatter.ISO_LOCAL_DATE);

    private static LocalDate nextWorkday(LocalDate date) {
        while (date.getDayOfWeek() == DayOfWeek.FRIDAY) {
            date = date.plusDays(1);
        }
        return date;
    }

    private static LocalDate nextFriday() {
        LocalDate today = LocalDate.now();
        LocalDate friday = today.with(DayOfWeek.FRIDAY);
        return friday.isAfter(today) ? friday : friday.plusWeeks(1);
    }
}

