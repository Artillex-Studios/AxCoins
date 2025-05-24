package com.artillexstudios.axcoins.currency.impl;

import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;

public record ConfigCurrency(int id, CurrencyConfig config) implements Currency {

    @Override
    public String identifier() {
        return this.config.identifier();
    }
}
