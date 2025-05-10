package com.artillexstudios.axcoins.api.currency.provider;

import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;

public interface CurrencyProvider<T extends Currency, Z extends CurrencyConfig> {

    T provide(Z config);
}
