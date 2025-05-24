package com.artillexstudios.axcoins.api.currency.provider;

import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;

import java.io.File;

public interface CurrencyConfigProvider<T extends CurrencyConfig> {

    T provide(File file);
}
