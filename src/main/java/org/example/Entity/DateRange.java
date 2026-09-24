package org.example.Entity;

import java.time.LocalDate;

public record DateRange(LocalDate fromDate, LocalDate toDate) {
    public boolean includes(LocalDate date) {
        if (date == null) return false;
        boolean afterOrEqualFrom = (fromDate == null) || !date.isBefore(fromDate);
        boolean beforeOrEqualTo = (toDate == null) || !date.isAfter(toDate);
        return afterOrEqualFrom && beforeOrEqualTo;
    }
}
