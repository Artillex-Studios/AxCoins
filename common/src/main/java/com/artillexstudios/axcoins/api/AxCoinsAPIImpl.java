package com.artillexstudios.axcoins.api;

import com.artillexstudios.axcoins.AxCoinsPlugin;
import com.artillexstudios.axcoins.api.currency.Currencies;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProviders;
import com.artillexstudios.axcoins.api.user.UserRepository;
import com.artillexstudios.axcoins.api.utils.NumberFormatter;
import com.artillexstudios.axcoins.config.Config;

public final class AxCoinsAPIImpl implements AxCoinsAPI {

    @Override
    public UserRepository repository() {
        return AxCoinsPlugin.instance().userRepository();
    }

    @Override
    public Currencies currencies() {
        return AxCoinsPlugin.instance().currencies();
    }

    @Override
    public CurrencyProviders providers() {
        return AxCoinsPlugin.instance().currencyProviders() ;
    }

    @Override
    public NumberFormatter newFormatter() {
        return this.newFormatter(Config.numberFormatting.precision);
    }

    @Override
    public NumberFormatter newFormatter(int defaultPrecision) {
        return new com.artillexstudios.axcoins.utils.NumberFormatter(defaultPrecision);
    }
}
