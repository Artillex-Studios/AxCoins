package com.artillexstudios.axcoins.utils;

import com.artillexstudios.axcoins.config.Config;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;

public final class NumberFormatter implements com.artillexstudios.axcoins.api.utils.NumberFormatter {
    private final int precision;

    public NumberFormatter(int precision) {
        this.precision = precision;
    }

    public String format(BigDecimal decimal) {
        return this.format(decimal, this.precision);
    }

    @Override
    public String format(BigDecimal decimal, int precision) {
        return this.format(decimal, "", precision);
    }

    private String format(BigDecimal decimal, String suffix, int precision) {
        for (Map.Entry<BigDecimal, String> entry : Config.numberFormatting.sorted.entrySet()) {
            if (decimal.compareTo(entry.getKey()) < 0) {
                continue;
            }

            return this.format(decimal.divide(entry.getKey(), precision, RoundingMode.HALF_UP).stripTrailingZeros(), suffix + entry.getValue(), precision);
        }

        return decimal.toString() + suffix;
    }

    public BigDecimal parseBigDecimal(String input) throws NumberFormatException {
        int i = 0;
        for (; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isDigit(c) && c != '.' && c != ',') {
                break;
            }
        }

        String numberParts = input.substring(0, i);
        BigDecimal decimal = new BigDecimal(numberParts);
        if (numberParts.length() == input.length()) {
            return decimal;
        }

        String numberFormatChars = input.substring(i);
        BigDecimal shortHandValue = Config.numberFormatting.shorthandValues.get(numberFormatChars);
        if (shortHandValue == null) {
            // Go character by character
            for (int j = 0; j < numberFormatChars.length(); j++) {
                char c = numberFormatChars.charAt(j);
                BigDecimal found = Config.numberFormatting.shorthandValues.get(String.valueOf(c));
                if (found == null) {
                    break;
                }

                if (shortHandValue == null) {
                    shortHandValue = found;
                } else {
                    shortHandValue = shortHandValue.multiply(found);
                }
            }
        }

        if (shortHandValue != null) {
            decimal = decimal.multiply(shortHandValue);
        }

        return decimal;
    }
}
