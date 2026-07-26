package com.smartfinance.util;

import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

/**
 * Helper methods for parsing and formatting dates.
 */
public final class DateUtil {

    public static final DateTimeFormatter DISPLAY = DateTimeFormatter.ofPattern("dd MMM yyyy");
    public static final DateTimeFormatter INPUT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter MONTH = DateTimeFormatter.ofPattern("yyyy-MM");

    private DateUtil() {
    }

    public static LocalDate parseDate(String input) {
        return LocalDate.parse(input.trim(), INPUT);
    }

    public static YearMonth parseMonth(String input) {
        return YearMonth.parse(input.trim(), MONTH);
    }

    public static LocalDate tryParseDate(String input) {
        try {
            return parseDate(input);
        } catch (DateTimeParseException e) {
            return null;
        }
    }
}
