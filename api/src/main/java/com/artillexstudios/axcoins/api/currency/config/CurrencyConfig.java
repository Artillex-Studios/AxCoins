package com.artillexstudios.axcoins.api.currency.config;

import java.math.BigDecimal;

public interface CurrencyConfig {

    String name();

    String provider();

    BigDecimal startingValue();

    BigDecimal maximumValue();

    BigDecimal minimumValue();

    boolean allowDecimals();
}
