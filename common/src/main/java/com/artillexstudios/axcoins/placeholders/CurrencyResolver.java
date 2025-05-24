package com.artillexstudios.axcoins.placeholders;

import com.artillexstudios.axapi.placeholders.PlaceholderArgumentResolver;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currency;
import org.jetbrains.annotations.Nullable;

public class CurrencyResolver implements PlaceholderArgumentResolver<Currency> {

    @Nullable
    @Override
    public Currency resolve(String string) {
        return AxCoinsAPI.instance().currencies().fetch(string);
    }
}
