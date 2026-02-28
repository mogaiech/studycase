package com.justlife.studycase.utils;

import java.time.LocalTime;
import java.util.Set;

public final class Constant {

    public static final Set<Integer> ALLOWED_PROFESSIONAL_COUNTS = Set.of(1, 2, 3);
    public static final LocalTime WORK_START = LocalTime.of(8, 0);
    public static final LocalTime WORK_END = LocalTime.of(22, 0);
    public static final int BREAK_MINUTES = 30;
    public static final Set<Integer> VALID_DURATIONS = Set.of(2, 4);
}
