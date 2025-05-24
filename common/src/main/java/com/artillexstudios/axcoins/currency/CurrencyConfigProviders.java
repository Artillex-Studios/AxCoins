package com.artillexstudios.axcoins.currency;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyConfigProvider;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class CurrencyConfigProviders implements com.artillexstudios.axcoins.api.currency.provider.CurrencyConfigProviders {
    private final ConcurrentHashMap<String, CurrencyConfigProvider<?>> identifierToConfigProviderMap = new ConcurrentHashMap<>();

    @Override
    public <T extends CurrencyConfig> void register(String identifier, CurrencyConfigProvider<T> provider) {
        if (this.identifierToConfigProviderMap.containsKey(identifier)) {
            LogUtils.warn("Failed to register currency config provider with identifier {} as it is already loaded!", identifier);
            return;
        }

        this.identifierToConfigProviderMap.put(identifier, provider);
    }

    @Override
    public void deregister(String identifier) {
        if (!this.identifierToConfigProviderMap.containsKey(identifier)) {
            LogUtils.warn("Failed to deregister currency config provider with identifier {} as it is not loaded!", identifier);
            return;
        }

        this.identifierToConfigProviderMap.remove(identifier);
    }

    @Override
    public @Nullable <T extends CurrencyConfig> CurrencyConfigProvider<T> fetch(String identifier) {
        return (CurrencyConfigProvider<T>) this.identifierToConfigProviderMap.get(identifier);
    }

    @Override
    public Collection<CurrencyConfigProvider<?>> providers() {
        return Collections.unmodifiableCollection(this.identifierToConfigProviderMap.values());
    }


    @Override
    public Set<String> identifiers() {
        return Collections.unmodifiableSet(this.identifierToConfigProviderMap.keySet());
    }
}
