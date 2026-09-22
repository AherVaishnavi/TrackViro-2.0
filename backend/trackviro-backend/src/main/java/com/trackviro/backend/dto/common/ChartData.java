package com.trackviro.backend.dto.common;

import java.util.ArrayList;
import java.util.List;

/**
 * Replaces every List<Object[]> returned by ExpenseRepository's
 * summary queries (categorySummary, getEmpMonthlySummary, getDeptSummary,
 * getMonthlyTrend, getTopSpenders, etc.) so charts get typed JSON
 * instead of a raw Object array.
 */
public record ChartData(List<String> labels, List<Double> data) {

    public static ChartData from(List<Object[]> rows) {
        List<String> labels = new ArrayList<>();
        List<Double> data = new ArrayList<>();
        if (rows != null) {
            for (Object[] row : rows) {
                labels.add(row[0] == null ? "Unknown" : String.valueOf(row[0]));
                data.add(row[1] == null ? 0.0 : ((Number) row[1]).doubleValue());
            }
        }
        return new ChartData(labels, data);
    }
}
