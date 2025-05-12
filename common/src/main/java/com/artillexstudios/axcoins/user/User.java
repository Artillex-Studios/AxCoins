package com.artillexstudios.axcoins.user;

import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.CurrencyResponse;
import com.artillexstudios.axcoins.api.logging.LogContext;
import com.artillexstudios.axcoins.api.logging.arguments.LogArguments;
import com.artillexstudios.axcoins.config.Config;
import com.artillexstudios.axcoins.database.DatabaseAccessor;
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
    public CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount, LogContext context) {
        return this.accessor.give(this, currency, amount).thenApply(response -> {
            this.currencyCache.put(currency, response.amount());
            return response;
        }).thenApply(response -> {
            // TODO: Post populate the context
            return response;
        });
    }

    @Override
    public CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount) {
        return this.give(currency, amount, new LogContext.Builder()
                .withArgument(LogArguments.RECEIVER, this.player.getUniqueId().toString())
                .withArgument(LogArguments.SOURCE, Config.logging.storeStackTrace ? walkStack() :"API")
                .withArgument(LogArguments.AMOUNT, amount)
                .withArgument(LogArguments.DATE, new Date())
                .build()
        );
    }

    @Override
    public CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount, LogContext context) {
        return this.accessor.take(this, currency, amount).thenApply(response -> {
            this.currencyCache.put(currency, response.amount());
            return response;
        });
    }

    @Override
    public CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount) {
        return this.take(currency, amount, null);
    }

    @Override
    public CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount, LogContext context) {
        return this.accessor.set(this, currency, amount).thenApply(response -> {
            this.currencyCache.put(currency, response.amount());
            return response;
        });
    }

    @Override
    public CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount) {
        return this.set(currency, amount, null);
    }

    public static String walkStack() {
        return walker.walk(frames -> {
            return frames.filter(stackFrame -> !stackFrame.getClassName().contains("com.artillexstudios.axcoins"))
                    .map(frame -> frame.getClassName() + "." + frame.getMethodName() + " (Line " + frame.getLineNumber() + ")")
                    .collect(Collectors.joining("\n"));
        });
    }
}
