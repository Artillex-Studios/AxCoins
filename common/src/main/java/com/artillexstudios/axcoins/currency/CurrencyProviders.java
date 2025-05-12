package com.artillexstudios.axcoins.currency;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProvider;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyProviders implements com.artillexstudios.axcoins.api.currency.provider.CurrencyProviders {
    private final ConcurrentHashMap<String, CurrencyProvider<?, ?>> identifierToCurrencyProviderMap = new ConcurrentHashMap<>();

    @Override
    public <T extends Currency, Z extends CurrencyConfig> void register(String identifier, CurrencyProvider<T, Z> provider) {
        if (this.identifierToCurrencyProviderMap.containsKey(identifier)) {
            LogUtils.warn("Failed to register currency provider with identifier {} as it is already loaded!", identifier);
            return;
        }

        this.identifierToCurrencyProviderMap.put(identifier, provider);
    }

    @Override
    public void deregister(String identifier) {
        if (!this.identifierToCurrencyProviderMap.containsKey(identifier)) {
            LogUtils.warn("Failed to deregister currency provider with identifier {} as it is not loaded!", identifier);
            return;
        }

        this.identifierToCurrencyProviderMap.remove(identifier);
    }

    @Override
    public @Nullable <T extends Currency, Z extends CurrencyConfig> CurrencyProvider<T, Z> fetch(String identifier) {
        return (CurrencyProvider<T, Z>) this.identifierToCurrencyProviderMap.get(identifier);
    }

    @Override
    public Collection<CurrencyProvider<?, ?>> providers() {
        return Collections.unmodifiableCollection(this.identifierToCurrencyProviderMap.values());
    }

    @Override
    public Set<String> identifiers() {
        return Collections.unmodifiableSet(this.identifierToCurrencyProviderMap.keySet());
    }
}
