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
    private static final BigDecimal NEGATIVE_ONE = BigDecimal.valueOf(-1L);
    private final Currencies currencies;
    private final DatabaseHandler handler;
    private final DatabaseQuery<List<UserDTO>> userSelect;
    private final DatabaseQuery<Integer> userInsert;
    private final DatabaseQuery<?> accountInsert;
    private final DatabaseQuery<Integer> currencySelect;
    private final DatabaseQuery<Integer> currencyInsert;
    private final DatabaseQuery<Integer> currencySet;
    private final DatabaseQuery<String> currencyAmountSelect;

    public DatabaseAccessor(Currencies currencies, DatabaseHandler handler) {
        this.currencies = currencies;
        this.handler = handler;
        this.handler.addTransformer(BigDecimal.class, bigDecimal -> List.of(bigDecimal.toString()));
        this.userSelect = this.handler.query("user_select", new ListHandler<>(new TransformerHandler<>(UserDTO.class)));
        this.userInsert = this.handler.query("user_insert");
        this.accountInsert = this.handler.query("account_insert");
        this.currencySelect = this.handler.query("currency_select");
        this.currencyInsert = this.handler.query("currency_insert");
        this.currencySet = this.handler.query("currency_set");
        this.currencyAmountSelect = this.handler.query("currency_amount_select");
    }

    public CompletableFuture<?> load() {
        return CompletableFuture.runAsync(() -> {
            this.handler.query("setup")
                    .create().update();
        }, AsyncUtils.executor()).exceptionally(throwable -> {
            LogUtils.error("Exception!", throwable);
            return null;
        });
    }

    public CompletableFuture<User> loadUser(UUID uuid) {
        return CompletableFuture.supplyAsync(() -> {
            List<UserDTO> userDTO = this.userSelect.create()
                    .query(uuid);

            if (userDTO != null && !userDTO.isEmpty()) {
                if (Config.debug) {
                    LogUtils.debug("UserDTO is not empty!");
                }

                Map<Currency, BigDecimal> cache = HashMap.newHashMap(userDTO.size());
                for (UserDTO dto : userDTO) {
                    Currency currency = this.currencies.fetch(dto.currencyId());
                    if (Config.debug) {
                        LogUtils.debug("Currency: {}", currency);
                    }
                    if (currency == null) {
                        continue;
                    }

                    cache.put(currency, new BigDecimal(dto.value()));
                }

                if (Config.debug) {
                    LogUtils.debug("Cache: {}", cache);
                }
                // Get the currencies which do not have values for the user.
                for (Currency currency : this.currencies.registered()) {
                    if (cache.containsKey(currency)) {
                        continue;
                    }

                    cache.put(currency, currency.config().startingValue());
                    this.createAccount(userDTO.getFirst().id(), currency, currency.config().startingValue());
                }

                return new com.artillexstudios.axcoins.user.User(userDTO.getFirst().id(), Bukkit.getOfflinePlayer(uuid), cache, this);
            }

            OfflinePlayer player = Bukkit.getOfflinePlayer(uuid);
            Integer userId = this.userInsert.create().execute(uuid);
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
                    .query(config.identifier());

            if (id == null) {
                if (Config.debug) {
                    LogUtils.debug("Inserting currency {}!", config.identifier());
                }

                id = this.currencyInsert.create()
                        .execute(config.identifier());
            }

            if (id == null) {
                LogUtils.error("Failed to insert currency {}!", config.identifier());
                return null;
            }

            if (Config.debug) {
                LogUtils.debug("Found currency {} with id {}!", config.identifier(), id);
            }
            return fetched.provide(id, config);
        }, AsyncUtils.executor());
    }

    public CompletableFuture<BigDecimal> fetch(User user, Currency currency) {
        return CompletableFuture.supplyAsync(() -> {
            BigDecimal read = this.optimisticRead(user, currency, amount -> {
                return amount;
            });

            return read == null ? BigDecimal.ZERO : read;
        }, AsyncUtils.executor());
    }

    public CompletableFuture<Boolean> has(User user, Currency currency, BigDecimal compareTo) {
        return CompletableFuture.supplyAsync(() -> {
            Boolean read = this.optimisticRead(user, currency, amount -> {
                return amount.subtract(compareTo).compareTo(currency.config().minimumValue()) >= 0;
            });

            return read != null && read;
        }, AsyncUtils.executor());
    }

    public CompletableFuture<CurrencyResponse> give(User user, Currency currency, BigDecimal currencyAmount) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyResponse read = this.optimisticRead(user, currency, found -> {
                BigDecimal bigDecimal = found.add(currencyAmount);
                if (NEGATIVE_ONE.compareTo(currency.config().maximumValue()) != 0 && bigDecimal.compareTo(currency.config().maximumValue()) > 0) {
                    return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, found, false);
                }

                return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, found.add(currencyAmount), true);
            });

            return read == null ? new com.artillexstudios.axcoins.currency.CurrencyResponse(BigDecimal.ZERO, BigDecimal.ZERO, false) : read;
        }, AsyncUtils.executor());
    }

    public CompletableFuture<CurrencyResponse> take(User user, Currency currency, BigDecimal currencyAmount) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyResponse read = this.optimisticRead(user, currency, found -> {
                BigDecimal bigDecimal = found.subtract(currencyAmount);
                if (bigDecimal.compareTo(currency.config().minimumValue()) < 0) {
                    return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, found, false);
                }

                return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, bigDecimal, true);
            });

            return read == null ? new com.artillexstudios.axcoins.currency.CurrencyResponse(BigDecimal.ZERO,BigDecimal.ZERO, false) : read;
        }, AsyncUtils.executor());
    }

    public CompletableFuture<CurrencyResponse> set(User user, Currency currency, BigDecimal currencyAmount, boolean force) {
        return CompletableFuture.supplyAsync(() -> {
            CurrencyResponse read = this.optimisticRead(user, currency, found -> {
                if (currencyAmount.compareTo(currency.config().minimumValue()) < 0 && !force) {
                    return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, found, false);
                }

                if (NEGATIVE_ONE.compareTo(currency.config().maximumValue()) != 0 && currencyAmount.compareTo(currency.config().maximumValue()) > 0 && !force) {
                    return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, found, false);
                }

                return new com.artillexstudios.axcoins.currency.CurrencyResponse(found, currencyAmount, true);
            });

            return read == null ? new com.artillexstudios.axcoins.currency.CurrencyResponse(BigDecimal.ZERO, BigDecimal.ZERO, false) : read;
        }, AsyncUtils.executor());
    }

    // This is a very complex method, I know
    // It had to be done though, as this is way more feasible than the one I had previously, and this is
    // more likely to run.
    // This may need a rewrite! (But I don't know what's the best way to rewrite this, so it will have to wait.)
    public <T> T optimisticRead(User user, Currency currency, Function<BigDecimal, T> supplier) {
        if (Config.debug) {
            LogUtils.debug("Optimistic read begin!");
        }

        int iteration = 0;
        while (true) {
            if (Config.debug) {
                LogUtils.debug("Optimistic read, iteration: {}", iteration);
            }
            String amount = this.currencyAmountSelect.create()
                    .query(user.id(), currency.id());

            if (amount == null) {
                return null;
            }
            BigDecimal currentState = new BigDecimal(amount);
            if (Config.debug) {
                LogUtils.debug("Found amount: {}", amount);
            }
            long start = System.currentTimeMillis();
            T value = supplier.apply(currentState);
            long took = System.currentTimeMillis() - start;
            if (Config.debug) {
                LogUtils.debug("Read for {}ms! Response: {}", took, value);
            }

            if (value instanceof BigDecimal decimal) {
                if (decimal.equals(currentState)) {
                    if (Config.debug) {
                        LogUtils.debug("No changes were made!");
                    }
                    // No need to write anything, we are all set!
                    return value;
                }

                boolean success = this.currencySet.create()
                        .update(decimal.toString(), user.id(), currency.id(), amount) == 1;

                if (success) {
                    if (Config.debug) {
                        LogUtils.debug("Success!");
                    }
                    return value;
                }

                if (Config.debug) {
                    LogUtils.debug("Retrying!");
                }
            } else if (value instanceof CurrencyResponse response) {
                if (!response.success()) {
                    if (Config.debug) {
                        LogUtils.debug("Response failed!");
                    }
                    // We failed somewhere in the java code, just give back the value and run
                    // we don't need to update the database
                    return value;
                }

                if (response.amount().equals(currentState)) {
                    if (Config.debug) {
                        LogUtils.debug("No changes were made!");
                    }
                    // No need to write anything, we are all set!
                    return value;
                }

                boolean success = this.currencySet.create()
                        .update(response.amount().toString(), user.id(), currency.id(), amount) == 1;

                if (success) {
                    if (Config.debug) {
                        LogUtils.debug("Success!");
                    }
                    return value;
                }

                if (Config.debug) {
                    LogUtils.debug("Retrying!");
                }
            } else {
                if (Config.debug) {
                    LogUtils.debug("Other class, just returning it.");
                }
                return value;
            }

            try {
                Thread.sleep(200);
            } catch (InterruptedException exception) {
                LogUtils.error("Failed to sleep thread!", exception);
            }
            iteration++;
        }
    }

    public void createAccount(Integer id, Currency currency, BigDecimal amount) {
        this.accountInsert.create().update(id, currency.id(), amount.toString());
    }
}
