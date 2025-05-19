package com.artillexstudios.axcoins.api.utils;

import java.math.BigDecimal;

public final class BigDecimalUtils {

    public static BigDecimal clamp(BigDecimal value, BigDecimal min, BigDecimal max) {
        if (value.compareTo(min) < 0) {
            return min;
        } else if (value.compareTo(max) > 0) {
            return max;
        }

        return value;
    }
}
