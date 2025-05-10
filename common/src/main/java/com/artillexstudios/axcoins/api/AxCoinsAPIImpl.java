package com.artillexstudios.axcoins.api;

import com.artillexstudios.axcoins.api.currency.Currencies;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProviders;
import com.artillexstudios.axcoins.api.user.UserRepository;

public final class AxCoinsAPIImpl implements AxCoinsAPI {

    @Override
    public UserRepository repository() {
        return null;
    }

    @Override
    public Currencies currencies() {
        return null;
    }

    @Override
    public CurrencyProviders providers() {
        return null;
    }
}
