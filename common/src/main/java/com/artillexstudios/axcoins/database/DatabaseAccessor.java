package com.artillexstudios.axcoins.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currencies;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProvider;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.database.dto.UserDTO;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class DatabaseAccessor {
    private final Currencies currencies;
    private final DatabaseHandler handler;
    private final DatabaseQuery<List<UserDTO>> userSelect;
    private final DatabaseQuery<Integer> userInsert;
    private final DatabaseQuery<?> accountInsert;

    public DatabaseAccessor(Currencies currencies, DatabaseHandler handler) {
        this.currencies = currencies;
        this.handler = handler;
        this.userSelect = this.handler.query("user_select", new ListHandler<>(new TransformerHandler<>(UserDTO.class)));
        this.userInsert = this.handler.query("user_insert");
        this.accountInsert = this.handler.query("account_insert");
    }

    public CompletableFuture<?> load() {
        return CompletableFuture.runAsync(() -> {
            this.handler.query("setup")
                    .execute();
        }, AsyncUtils.executor());
    }

    public CompletableFuture<User> loadUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<UserDTO> userDTO = this.userSelect.create()
                    .query(uuid);

            if (userDTO != null && !userDTO.isEmpty()) {
                Map<Currency, BigDecimal> cache = HashMap.newHashMap(userDTO.size());
                for (UserDTO dto : userDTO) {
                    Currency currency = this.currencies.fetch(dto.currencyId());
                    if (currency == null) {
                        continue;
                    }

                    cache.put(currency, new BigDecimal(dto.value()));
                }
                // Get the currencies which do not have values for the user.
                for (Currency currency : this.currencies.registered()) {
                    if (!cache.containsKey(currency)) {
                        continue;
                    }

                    cache.put(currency, currency.config().startingValue());
                    this.createAccount(userDTO.getFirst().id(), currency, currency.config().startingValue());
                }

                return new com.artillexstudios.axcoins.user.User(Bukkit.getOfflinePlayer(uuid), cache);
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Integer userId = this.userInsert.create().execute(player.getName(), uuid);
            if (userId == null) {
                LogUtils.error("Failed to create account for user!");
                return null;
            }

            Collection<Currency> registered = this.currencies.registered();
            Map<Currency, BigDecimal> amounts = HashMap.newHashMap(registered.size());
            for (Currency currency : registered) {
                amounts.put(currency, currency.config().startingValue());
            }

            for (Currency currency : registered) {
                this.createAccount(userId, currency, currency.config().startingValue());
            }

            return (User) new com.artillexstudios.axcoins.user.User(player, amounts);
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to create user account!", throwable);
            return null;
        });
    }

    public <T extends CurrencyConfig> CompletableFuture<Currency> loadCurrency(T config) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyProvider<Currency, CurrencyConfig> fetched = AxCoinsAPI.instance().providers().fetch(config.provider());
            if (fetched == null) {
                return null;
            }

            // TODO: Provide the ID
            return fetched.provide(0, config);
        }, AsyncUtils.executor());
    }

    public void createAccount(Integer id, Currency currency, BigDecimal amount) {
        this.accountInsert.create().update(id, currency.id(), amount.toString());
    }
}
