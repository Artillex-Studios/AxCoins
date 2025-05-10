package com.artillexstudios.axcoins.api.currency.provider;

import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface CurrencyProviders {

    <T extends Currency, Z extends CurrencyConfig> void register(String identifier, CurrencyProvider<T, Z> provider);

    void deregister(String identifier);

    @Nullable
    <T extends Currency, Z extends CurrencyConfig> CurrencyProvider<T, Z> fetch(String identifier);

    Collection<CurrencyProvider<?, ?>> providers();

    Set<String> identifiers();
}
