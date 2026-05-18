package com.yanwai.util;

import java.time.LocalTime;

public class TimeUtil {

    public static boolean isLateNight() {
        LocalTime now = LocalTime.now();
        return now.isAfter(LocalTime.of(22, 0)) || now.isBefore(LocalTime.of(6, 0));
    }

    public static boolean isWorkDay() {
        int day = java.time.LocalDate.now().getDayOfWeek().getValue();
        return day >= 1 && day <= 5;
    }
}
