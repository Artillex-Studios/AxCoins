package com.artillexstudios.axcoins.user;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.CurrencyResponse;
import com.artillexstudios.axcoins.api.event.CurrencyChangeEvent;
import com.artillexstudios.axcoins.api.logging.LogArguments;
import com.artillexstudios.axcoins.config.Config;
import com.artillexstudios.axcoins.database.DatabaseAccessor;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class User implements com.artillexstudios.axcoins.api.user.User {
    private static final StackWalker walker = StackWalker.getInstance();
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
        return this.accessor.fetch(this, currency).thenApply(amount -> {
            this.currencyCache.put(currency, amount);
            return amount;
        });
    }

    @Override
    public CompletableFuture<Boolean> has(Currency currency, BigDecimal amount) {
        return this.accessor.has(this, currency, amount);
    }

    @Override
    public CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount, HashMapContext context) {
        return this.accessor.give(this, currency, amount).thenApply(response -> {
            this.currencyCache.put(currency, response.amount());
            return response;
        }).thenApply(response -> {
            if (response.success()) {
                CurrencyChangeEvent event = new CurrencyChangeEvent(this, response.previous(), response.amount(), context);
                Bukkit.getPluginManager().callEvent(event);
            }
            return response;
        });
    }

    @Override
    public CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount) {
        return this.give(currency, amount, new HashMapContext()
                .with(LogArguments.RECEIVER, this.player.getUniqueId().toString())
                .with(LogArguments.SOURCE, Config.logging.storeStackTrace ? walkStack() : "API")
                .with(LogArguments.AMOUNT, amount)
                .with(LogArguments.DATE, new Date())
                .with(LogArguments.RECEIVER_IP_ADDRESS, this.player.getPlayer().getAddress().getAddress().toString())
        );
    }

    @Override
    public CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount, HashMapContext context) {
        return this.accessor.take(this, currency, amount).thenApply(response -> {
            this.currencyCache.put(currency, response.amount());
            return response;
        }).thenApply(response -> {
            if (response.success()) {
                CurrencyChangeEvent event = new CurrencyChangeEvent(this, response.previous(), response.amount(), context);
                Bukkit.getPluginManager().callEvent(event);
            }

            return response;
        });
    }

    @Override
    public CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount) {
        return this.take(currency, amount, new HashMapContext()
                .with(LogArguments.RECEIVER, this.player.getUniqueId().toString())
                .with(LogArguments.SOURCE, Config.logging.storeStackTrace ? walkStack() : "API")
                .with(LogArguments.AMOUNT, amount)
                .with(LogArguments.DATE, new Date())
                .with(LogArguments.RECEIVER_IP_ADDRESS, this.player.getPlayer().getAddress().getAddress().toString())
        );
    }

    @Override
    public CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount, HashMapContext context, boolean force) {
        return this.accessor.set(this, currency, amount, force).thenApply(response -> {
            this.currencyCache.put(currency, response.amount());
            return response;
        }).thenApply(response -> {
            if (response.success()) {
                CurrencyChangeEvent event = new CurrencyChangeEvent(this, response.previous(), response.amount(), context);
                Bukkit.getPluginManager().callEvent(event);
            }

            return response;
        });
    }

    @Override
    public CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount, boolean force) {
        return this.set(currency, amount, new HashMapContext()
                .with(LogArguments.RECEIVER, this.player.getUniqueId().toString())
                .with(LogArguments.SOURCE, Config.logging.storeStackTrace ? walkStack() : "API")
                .with(LogArguments.AMOUNT, amount)
                .with(LogArguments.DATE, new Date())
                .with(LogArguments.RECEIVER_IP_ADDRESS, this.player.getPlayer().getAddress().getAddress().toString()), force);
    }

    public static String walkStack() {
        return walker.walk(frames -> {
            return frames.filter(stackFrame -> !stackFrame.getClassName().contains("com.artillexstudios.axcoins"))
                    .map(frame -> frame.getClassName() + "." + frame.getMethodName() + " (Line " + frame.getLineNumber() + ")")
                    .collect(Collectors.joining("\n"));
        });
    }
}
