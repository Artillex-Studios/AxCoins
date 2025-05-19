package com.artillexstudios.axcoins.api.utils;

import java.math.BigInteger;

public final class BigIntegerUtils {

    public static BigInteger clamp(BigInteger value, BigInteger min, BigInteger max) {
        if (value.compareTo(min) < 0) {
            return min;
        } else if (value.compareTo(max) > 0) {
            return max;
        }

        return value;
    }
}
