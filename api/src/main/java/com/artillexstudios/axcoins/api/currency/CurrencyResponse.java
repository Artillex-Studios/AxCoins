package com.artillexstudios.axcoins.api.currency;

import java.math.BigDecimal;

public interface CurrencyResponse {

    BigDecimal amount();

    boolean success();
}
