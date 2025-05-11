package com.artillexstudios.axcoins.user;

import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.CurrencyResponse;
import com.artillexstudios.axcoins.database.DatabaseAccessor;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class User implements com.artillexstudios.axcoins.api.user.User {
    private final int id;
    private final OfflinePlayer player;
    private final DatabaseAccessor accessor;
    private final ConcurrentHashMap<Currency, BigDecimal> currencyCache = new ConcurrentHashMap<>();

    public User(int id, OfflinePlayer player, Map<Currency, BigDecimal> cache, DatabaseAccessor accessor) {
        this.id = id;
        this.player = player;
        this.currencyCache.putAll(cache);
        this.accessor = accessor;
    }

    @Override
    public int id() {
        return this.id;
    }

    @Override
    public OfflinePlayer player() {
        return this.player;
    }

    @Override
    public BigDecimal cached(Currency currency) {
        return this.currencyCache.getOrDefault(currency, BigDecimal.ZERO);
    }

    @Override
    public CompletableFuture<BigDecimal> value(Currency currency) {
        return this.accessor.fetch(this, currency);
    }

    @Override
    public CompletableFuture<Boolean> has(Currency currency, BigDecimal amount) {
        return this.accessor.has(this, currency, amount);
    }

    @Override
    public CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount) {
        return this.accessor.give(this, currency, amount);
    }

    @Override
    public CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount) {
        return this.accessor.take(this, currency, amount);
    }

    @Override
    public CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount) {
        return this.accessor.set(this, currency, amount);
    }
}
