package com.artillexstudios.axcoins.api.currency;

import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public interface Currencies {

    <T extends CurrencyConfig> CompletableFuture<Currency> register(T config);

    void register(Currency currency);

    void deregister(Currency currency);

    @Nullable
    Currency fetch(int id);

    @Nullable
    Currency fetch(String identifier);

    Collection<Currency> registered();

    Set<String> names();
}
