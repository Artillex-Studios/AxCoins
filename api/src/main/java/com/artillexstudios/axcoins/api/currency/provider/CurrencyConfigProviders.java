package com.artillexstudios.axcoins.api.currency.provider;

import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface CurrencyConfigProviders {

    <T extends CurrencyConfig> void register(String identifier, CurrencyConfigProvider<T> provider);

    void deregister(String identifier);

    @Nullable
    <T extends CurrencyConfig> CurrencyConfigProvider<T> fetch(String identifier);

    Collection<CurrencyConfigProvider<?>> providers();

    Set<String> identifiers();
}
