package com.artillexstudios.axcoins.api.user;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.CurrencyResponse;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

public interface User {

    /**
     * Get the id of the user.
     * @return The user's id.
     */
    int id();

    /**
     * Get the OfflinePlayer associated with this player.
     * @return An OfflinePlayer
     */
    OfflinePlayer player();

    /**
     * Get the cached amount of this currency.
     * WARNING! This data might be out of date. You should use the {@link  #value(Currency currency)} method
     * of this class to fetch the to-date amount.
     * This method is useful for placeholders, or other things that are not as sensitive to data being
     * up-to-date.
     * @param currency The currency to check the amount of.
     * @return A BigDecimal representing the current cached amount of the
     * currency of this user.
     */
    BigDecimal cached(Currency currency);

    default CompletableFuture<Boolean> canGive(Currency currency, BigDecimal amount) {
        return this.value(currency).thenApply(found -> {
            return found.add(amount).compareTo(currency.config().maximumValue()) <= 0;
        });
    }

    /**
     * Get the amount of this currency.
     * @param currency The currency to check the amount of.
     * @return A CompletableFuture that is completed when the data is fetched from the database.
     */
    CompletableFuture<BigDecimal> value(Currency currency);

    /**
     * Check if the user has an amount of currency.
     * @param currency The currency to check the amount of.
     * @param amount The amount.
     * @return A CompletableFuture that is completed when the data is fetched from the database.
     */
    CompletableFuture<Boolean> has(Currency currency, BigDecimal amount);

    /**
     * Give the user the amount of currency.
     * @param currency The currency to give.
     * @param amount The amount.
     * @param context The LogContext to use for logging purposes.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount, HashMapContext context);

    /**
     * Give the user the amount of currency.
     * @param currency The currency to give.
     * @param amount The amount.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    CompletableFuture<CurrencyResponse> give(Currency currency, BigDecimal amount);

    /**
     * Take amount of currency from the user.
     * @param currency The currency to take.
     * @param amount The amount.
     * @param context The LogContext to use for logging purposes.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount, HashMapContext context);

    /**
     * Take amount of currency from the user.
     * @param currency The currency to take.
     * @param amount The amount.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    CompletableFuture<CurrencyResponse> take(Currency currency, BigDecimal amount);

    /**
     * Set the amount of currency for the user.
     * @param currency The currency to set.
     * @param amount The amount.
     * @param context The LogContext to use for logging purposes.
     * @param force Whether to take into account the currencies limitations.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount, HashMapContext context, boolean force);

    /**
     * Set the amount of currency for the user.
     * @param currency The currency to set.
     * @param amount The amount.
     * @param force Whether to take into account the currencies limitations.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount, boolean force);

    /**
     * Set the amount of currency for the user.
     * @param currency The currency to set.
     * @param amount The amount.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    default CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount, HashMapContext context) {
        return this.set(currency, amount, context, false);
    }

    /**
     * Set the amount of currency for the user.
     * @param currency The currency to set.
     * @param amount The amount.
     * @return A CompletableFuture that is completed when the data is updated in the database.
     */
    default CompletableFuture<CurrencyResponse> set(Currency currency, BigDecimal amount) {
        return this.set(currency, amount, false);
    }
}
