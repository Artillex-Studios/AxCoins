package com.artillexstudios.axcoins.api.currency;

import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;

public interface Currency {

    int id();

    String identifier();

    CurrencyConfig config();
}
