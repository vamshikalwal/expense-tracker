package com.expenseTracker.util;

import java.time.LocalDate;

public class DateUtil {

    public static LocalDate getFirstDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(1);
    }

    public static LocalDate getLastDayOfMonth(LocalDate date) {
        return date.withDayOfMonth(date.lengthOfMonth());
    }

    public static LocalDate getFirstDayOfYear(LocalDate date) {
        return date.withDayOfYear(1);
    }

    public static LocalDate getLastDayOfYear(LocalDate date) {
        return date.withDayOfYear(date.lengthOfYear());
    }

    public static LocalDate getFirstDayOfWeek(LocalDate date) {
        return date.minusDays(date.getDayOfWeek().getValue() - 1);
    }

    public static LocalDate getLastDayOfWeek(LocalDate date) {
        return date.plusDays(7 - date.getDayOfWeek().getValue());
    }
}
