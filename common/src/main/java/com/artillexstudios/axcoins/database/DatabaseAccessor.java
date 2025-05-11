package com.artillexstudios.axcoins.database;

import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.database.DatabaseQuery;
import com.artillexstudios.axapi.database.RunnableQuery;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currencies;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.CurrencyResponse;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProvider;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.config.Config;
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
import java.util.function.Function;

public class DatabaseAccessor {
    private final Currencies currencies;
    private final DatabaseHandler handler;
    private final DatabaseQuery<List<UserDTO>> userSelect;
    private final DatabaseQuery<Integer> userInsert;
    private final DatabaseQuery<?> accountInsert;
    private final DatabaseQuery<Integer> currencySelect;
    private final DatabaseQuery<Integer> currencySet;
    private final DatabaseQuery<String> currencyAmountSelect;
    private final RunnableQuery<?> lock;
    private final RunnableQuery<?> unlock;

    public DatabaseAccessor(Currencies currencies, DatabaseHandler handler) {
        this.currencies = currencies;
        this.handler = handler;
        this.handler.addTransformer(BigDecimal.class, bigDecimal -> List.of(bigDecimal.toString()));
        this.userSelect = this.handler.query("user_select", new ListHandler<>(new TransformerHandler<>(UserDTO.class)));
        this.userInsert = this.handler.query("user_insert");
        this.accountInsert = this.handler.query("account_insert");
        this.currencySelect = this.handler.query("currency_select");
        this.currencySet = this.handler.query("currency_set");
        this.currencyAmountSelect = this.handler.query("currency_amount_select");
        this.lock = this.handler.query("lock").create();
        this.unlock = this.handler.query("unlock").create();
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

                return new com.artillexstudios.axcoins.user.User(userDTO.getFirst().id(), Bukkit.getOfflinePlayer(uuid), cache, this);
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

            return (User) new com.artillexstudios.axcoins.user.User(userId, player, amounts, this);
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Failed to create user account!", throwable);
            return null;
        });
    }

    public <T extends CurrencyConfig> CompletableFuture<Currency> loadCurrency(T config) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyProvider<Currency, CurrencyConfig> fetched = AxCoinsAPI.instance().providers().fetch(config.provider());
            if (fetched == null) {
                if (Config.debug) {
                    LogUtils.debug("No provider found with id {}!", config.provider());
                }
                return null;
            }

            Integer id = this.currencySelect.create()
                    .query(config.name());

            if (id == null) {
                if (Config.debug) {
                    LogUtils.debug("Inserting currency {}!", config.name());
                }

                id = this.currencySet.create()
                        .execute(config.name());
            }

            if (id == null) {
                LogUtils.error("Failed to insert currency {}!", config.name());
                return null;
            }

            if (Config.debug) {
                LogUtils.debug("Found currency {} with id {}!", config.name(), id);
            }
            return fetched.provide(id, config);
        }, AsyncUtils.executor());
    }

    public CompletableFuture<BigDecimal> fetch(User user, Currency currency) {
        return CompletableFuture.supplyAsync(() -> {
            return this.transaction(user, currency, lock -> {
                String amount = this.currencyAmountSelect.create()
                        .query(user.id(), currency.id(), lock);

                return new BigDecimal(amount);
            });
        }, AsyncUtils.executor());
    }

    public CompletableFuture<Boolean> has(User user, Currency currency, BigDecimal compareTo) {
        return CompletableFuture.supplyAsync(() -> {
            return this.transaction(user, currency, lock -> {
                String amount = this.currencyAmountSelect.create()
                        .query(user.id(), currency.id(), lock);

                BigDecimal found = new BigDecimal(amount);
                return found.subtract(compareTo).compareTo(currency.config().minimumValue()) >= 0;
            });
        }, AsyncUtils.executor());
    }

    public CompletableFuture<CurrencyResponse> give(User user, Currency currency, BigDecimal currencyAmount) {
        return CompletableFuture.supplyAsync(() -> {
            return this.transaction(user, currency, lock -> {
                String amount = this.currencyAmountSelect.create()
                        .query(user.id(), currency.id());

                BigDecimal bigDecimal;
                BigDecimal found = bigDecimal = new BigDecimal(amount);
                bigDecimal = bigDecimal.add(currencyAmount);
                boolean success = this.currencySet.create()
                        .update(bigDecimal.toString(), user.id(), currency.id(), lock) == 1;
                if (Config.debug) {
                    LogUtils.debug("Give success: {}", success);
                }

                return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, success);
            });
        }, AsyncUtils.executor());
    }

    public CompletableFuture<CurrencyResponse> take(User user, Currency currency, BigDecimal currencyAmount) {
        return CompletableFuture.supplyAsync(() -> {
            return this.transaction(user, currency, lock -> {
                String amount = this.currencyAmountSelect.create()
                        .query(user.id(), currency.id());

                BigDecimal bigDecimal;
                BigDecimal found = bigDecimal = new BigDecimal(amount);
                bigDecimal = bigDecimal.subtract(currencyAmount);
                if (bigDecimal.compareTo(currency.config().minimumValue()) < 0) {
                    return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, false);
                }

                boolean success = this.currencySet.create()
                        .update(bigDecimal.toString(), user.id(), currency.id(), lock) == 1;
                if (Config.debug) {
                    LogUtils.debug("Take success: {}", success);
                }

                return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, success);
            });
        }, AsyncUtils.executor());
    }

    public CompletableFuture<CurrencyResponse> set(User user, Currency currency, BigDecimal currencyAmount) {
        return CompletableFuture.supplyAsync(() -> {
            return this.transaction(user, currency, lock -> {
                String amount = this.currencyAmountSelect.create()
                        .query(user.id(), currency.id());

                BigDecimal found = new BigDecimal(amount);
                boolean success = this.currencySet.create()
                        .update(currencyAmount.toString(), user.id(), currency.id(), lock) == 1;
                if (Config.debug) {
                    LogUtils.debug("Take success: {}", success);
                }

                return new com.artillexstudios.axcoins.currency.CurrencyResponse(success ? currencyAmount : found, success);
            });
        }, AsyncUtils.executor());
    }

    public <T> T transaction(User user, Currency currency, Function<String, T> supplier) {
        long timeStamp;
        String uuid = UUID.randomUUID().toString();
        while (true) {
            long currentTime = System.currentTimeMillis();
            if (this.lock.update(uuid, currentTime, currentTime, user.id(), currency.id()) == 1) {
                timeStamp = currentTime;
                break;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException exception) {
                LogUtils.error("Failed to sleep thread!", exception);
            }
        }

        T value = supplier.apply(uuid);

        this.unlock.update(uuid, timeStamp, user.id(), currency.id());
        return value;
    }

    public void createAccount(Integer id, Currency currency, BigDecimal amount) {
        this.accountInsert.create().update(id, currency.id(), amount.toString());
    }
}
