package com.artillexstudios.axcoins.currency.impl;

import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProvider;

public class ConfigCurrencyProvider implements CurrencyProvider<ConfigCurrency, CurrencyConfig> {

    @Override
    public ConfigCurrency provide(int id, CurrencyConfig config) {
        return new ConfigCurrency();
    }
}
