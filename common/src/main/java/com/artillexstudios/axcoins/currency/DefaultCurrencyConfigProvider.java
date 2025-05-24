package com.artillexstudios.axcoins.currency;

import com.artillexstudios.axcoins.api.currency.provider.CurrencyConfigProvider;
import com.artillexstudios.axcoins.currency.impl.CurrencyConfig;

import java.io.File;

public class DefaultCurrencyConfigProvider implements CurrencyConfigProvider<CurrencyConfig> {

    @Override
    public CurrencyConfig provide(File file) {
        return new CurrencyConfig(file);
    }
}
