package com.artillexstudios.axcoins.user;

import com.artillexstudios.axcoins.api.currency.Currency;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

public class User implements com.artillexstudios.axcoins.api.user.User {
    private final OfflinePlayer player;
    private final ConcurrentHashMap<Currency, BigDecimal> currencyCache = new ConcurrentHashMap<>();

    public User(OfflinePlayer player, Map<Currency, BigDecimal> cache) {
        this.player = player;
        this.currencyCache.putAll(cache);
    }

    @Override
    public int id() {
        return 0;
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
        return null;
    }

    @Override
    public CompletableFuture<Boolean> has(Currency currency, BigDecimal amount) {
        return null;
    }

    @Override
    public CompletableFuture<BigDecimal> give(Currency currency, BigDecimal amount) {
        return null;
    }

    @Override
    public CompletableFuture<BigDecimal> take(Currency currency, BigDecimal amount) {
        return null;
    }

    @Override
    public CompletableFuture<BigDecimal> set(Currency currency, BigDecimal amount) {
        return null;
    }
}
