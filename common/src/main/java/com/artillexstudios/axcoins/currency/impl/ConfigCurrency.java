package com.artillexstudios.axcoins.currency.impl;

import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;

public class ConfigCurrency implements Currency {

    @Override
    public int id() {
        return 1;
    }

    @Override
    public String identifier() {
        return "currency";
    }

    @Override
    public CurrencyConfig config() {
        return null;
    }
}
