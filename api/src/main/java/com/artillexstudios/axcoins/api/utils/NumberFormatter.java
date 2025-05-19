package com.artillexstudios.axcoins.api.utils;

import java.math.BigDecimal;

public interface NumberFormatter {

    String format(BigDecimal decimal);

    String format(BigDecimal decimal, int precision);

    BigDecimal parseBigDecimal(String input) throws NumberFormatException;
}
