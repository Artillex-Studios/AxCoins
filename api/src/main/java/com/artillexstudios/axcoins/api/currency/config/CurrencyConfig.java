package com.artillexstudios.axcoins.api.currency.config;

import org.jspecify.annotations.Nullable;

import java.math.BigDecimal;
import java.util.List;

public interface CurrencyConfig extends MessagesConfig {

    String name();

    String provider();

    BigDecimal startingValue();

    BigDecimal maximumValue();

    BigDecimal minimumValue();

    boolean allowDecimals();

    boolean enablePay();

    List<String> commands();

    @Nullable
    String permission();

    BigDecimal minimumPayAmount();

    BigDecimal maximumPayAmount();
}
