package com.artillexstudios.axcoins.currency;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.AxCoinsPlugin;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public final class Currencies implements com.artillexstudios.axcoins.api.currency.Currencies {
    private final ConcurrentHashMap<String, Currency> identifierToCurrencyMap = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Integer, Currency> idToCurrencyMap = new ConcurrentHashMap<>();

    @Override
    public <T extends CurrencyConfig> CompletableFuture<Currency> register(T config) {
        return AxCoinsPlugin.instance().accessor().loadCurrency(config).thenApply(currency -> {
            this.register(currency);
            return currency;
        });
    }

    @Override
    public void register(Currency currency) {
        if (this.identifierToCurrencyMap.containsKey(currency.identifier())) {
            LogUtils.warn("Failed to register currency with identifier {} as it is already loaded!", currency.identifier());
            return;
        }

        this.identifierToCurrencyMap.put(currency.identifier(), currency);
        this.idToCurrencyMap.put(currency.id(), currency);
    }

    @Override
    public void deregister(Currency currency) {
        if (!this.identifierToCurrencyMap.containsKey(currency.identifier())) {
            LogUtils.warn("Failed to deregister currency with identifier {} as it is not loaded!", currency.identifier());
            return;
        }

        this.identifierToCurrencyMap.remove(currency.identifier());
        this.idToCurrencyMap.remove(currency.id());
    }

    @Nullable
    @Override
    public Currency fetch(int id) {
        return this.idToCurrencyMap.get(id);
    }

    @Nullable
    @Override
    public Currency fetch(String identifier) {
        return this.identifierToCurrencyMap.get(identifier);
    }

    @Override
    public Collection<Currency> registered() {
        return Collections.unmodifiableCollection(this.identifierToCurrencyMap.values());
    }

    @Override
    public Set<String> names() {
        return Collections.unmodifiableSet(this.identifierToCurrencyMap.keySet());
    }
}
