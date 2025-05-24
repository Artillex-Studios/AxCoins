package com.artillexstudios.axcoins.command;

import com.artillexstudios.axapi.placeholders.PlaceholderHandler;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.command.argument.NumberArguments;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public class CurrencyCommand {
    private final Currency currency;

    public CurrencyCommand(Currency currency) {
        this.currency = currency;
    }

    public void register() {
        CurrencyConfig config = this.currency.config();
        if (config.commands().isEmpty()) {
            return;
        }

        List<String> copy = new ArrayList<>(config.commands());
        CommandAPICommand command = new CommandAPICommand(copy.removeFirst())
                .withAliases(copy.toArray(new String[0]));

        if (config.permission() != null) {
            command.withPermission(config.permission());
        }

        command.executesPlayer((sender, args) -> {
            User user = AxCoinsAPI.instance().getUserIfLoadedImmediately(sender);
            if (user == null) {
                // TODO: Not yet loaded message
                return;
            }

            // TODO: Placeholders
            MessageUtils.sendMessage(sender, config.prefix(), PlaceholderHandler.parse(config.balance(), user));
        });

        // TODO: use correct max value for pay
        CommandAPICommand paySubCommand = new CommandAPICommand("pay")
                .withArguments(new PlayerArgument("player"), config.allowDecimals() ? NumberArguments.bigDecimal("amount", BigDecimal.ZERO, BigDecimal.valueOf(Long.MAX_VALUE)) : NumberArguments.bigInteger("amount", BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE)))
                .executesPlayer((sender, args) -> {
                    User user = AxCoinsAPI.instance().getUserIfLoadedImmediately(sender);
                    if (user == null) {
                        // TODO: Not yet loaded message
                        return;
                    }

                    Player player = args.getByClass("player", Player.class);
                    if (player == null) {
                        return;
                    }

                    if (player.getUniqueId().equals(sender.getUniqueId())) {
                        MessageUtils.sendMessage(sender, config.prefix(), config.cantSendSelf());
                        return;
                    }

                    User receiver = AxCoinsAPI.instance().getUserIfLoadedImmediately(player);
                    if (receiver == null) {
                        // TODO: Other not yet loaded message
                        return;
                    }

                    BigDecimal amount;
                    if (config.allowDecimals()) {
                        amount = args.getByClass("amount", BigDecimal.class);
                    } else {
                        BigInteger value = args.getByClass("amount", BigInteger.class);
                        if (value == null) {
                            return;
                        }

                        amount = new BigDecimal(value);
                    }

                    if (amount == null) {
                        return;
                    }

                    user.has(this.currency, amount).thenAccept(has -> {
                        if (!has) {
                            MessageUtils.sendMessage(sender, config.prefix(), config.insufficientFunds());
                            return;
                        }

                        receiver.canGive(this.currency, amount).thenAccept(canGive -> {
                            if (!canGive) {
                                // TODO: Failed because too much
                                return;
                            }

                            user.take(this.currency, amount).thenAccept(response -> {
                                if (!response.success()) {
                                    // TODO: Failed message
                                    return;
                                }

                                receiver.give(this.currency, amount).thenAccept(giveResponse -> {
                                    if (!response.success()) {
                                        // TODO: Send message about giving back
                                        user.give(this.currency, amount);
                                        return;
                                    }

                                    // TODO: Success
                                });
                            });
                        });
                    });
                });

        // TODO: Permission
        // TODO: Other player's balance checking
        CommandAPICommand balanceCommand = new CommandAPICommand("balance")
                .executesPlayer((sender, args) -> {
                    User user = AxCoinsAPI.instance().getUserIfLoadedImmediately(sender);
                    if (user == null) {
                        // TODO: Not yet loaded message
                        return;
                    }

                    // TODO: Placeholders
                    MessageUtils.sendMessage(sender, config.prefix(), config.balance());
                });

        CommandAPICommand adminCommand = new CommandAPICommand("admin");

        CommandAPICommand adminGiveCommand = new CommandAPICommand("give")
                .withPermission("axcoins.%s.admin.give".formatted(this.currency.identifier()))
                .withArguments(new AsyncOfflinePlayerArgument("player"), this.currency.config().allowDecimals() ? NumberArguments.bigDecimal("amount", BigDecimal.ZERO, BigDecimal.valueOf(Long.MAX_VALUE)) : NumberArguments.bigInteger("amount", BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE)))
                .executes((sender, args) -> {
                    CompletableFuture<OfflinePlayer> player = args.getUnchecked("player");
                    if (player == null) {
                        return;
                    }

                    BigDecimal amount;
                    if (config.allowDecimals()) {
                        amount = args.getByClass("amount", BigDecimal.class);
                    } else {
                        BigInteger value = args.getByClass("amount", BigInteger.class);
                        if (value == null) {
                            return;
                        }

                        amount = new BigDecimal(value);
                    }

                    if (amount == null) {
                        return;
                    }

                    player.thenAccept(offlinePlayer -> {
                        AxCoinsAPI.instance().getUser(offlinePlayer).thenAccept(user -> {
                            user.give(this.currency, amount).thenAccept(response -> {
                                // TODO: Fail and success messages
                                if (!response.success()) {
                                    return;
                                }
                            });
                        });
                    });
                });

        CommandAPICommand adminGiveAllCommand = new CommandAPICommand("giveall")
                .withPermission("axcoins.%s.admin.giveall".formatted(this.currency.identifier()))
                .withArguments(this.currency.config().allowDecimals() ? NumberArguments.bigDecimal("amount", BigDecimal.ZERO, BigDecimal.valueOf(Long.MAX_VALUE)) : NumberArguments.bigInteger("amount", BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE)))
                .executes((sender, args) -> {
                    BigDecimal amount;
                    if (config.allowDecimals()) {
                        amount = args.getByClass("amount", BigDecimal.class);
                    } else {
                        BigInteger value = args.getByClass("amount", BigInteger.class);
                        if (value == null) {
                            return;
                        }

                        amount = new BigDecimal(value);
                    }

                    if (amount == null) {
                        return;
                    }

                    AxCoinsAPI.instance().repository().onlineUsers().forEach(user -> {
                        user.give(this.currency, amount).thenAccept(response -> {
                            // TODO: Fail and success messages
                            if (!response.success()) {
                                return;
                            }
                        });
                    });
                });

        CommandAPICommand adminTakeCommand = new CommandAPICommand("take")
                .withPermission("axcoins.%s.admin.take".formatted(this.currency.identifier()))
                .withArguments(new AsyncOfflinePlayerArgument("player"), this.currency.config().allowDecimals() ? NumberArguments.bigDecimal("amount", BigDecimal.ZERO, BigDecimal.valueOf(Long.MAX_VALUE)) : NumberArguments.bigInteger("amount", BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE)))
                .executes((sender, args) -> {
                    CompletableFuture<OfflinePlayer> player = args.getUnchecked("player");
                    if (player == null) {
                        return;
                    }

                    BigDecimal amount;
                    if (config.allowDecimals()) {
                        amount = args.getByClass("amount", BigDecimal.class);
                    } else {
                        BigInteger value = args.getByClass("amount", BigInteger.class);
                        if (value == null) {
                            return;
                        }

                        amount = new BigDecimal(value);
                    }

                    if (amount == null) {
                        return;
                    }

                    player.thenAccept(offlinePlayer -> {
                        AxCoinsAPI.instance().getUser(offlinePlayer).thenAccept(user -> {
                            user.has(this.currency, amount).thenAccept(result -> {
                                if (!result) {
                                    // TODO: Not enough money message
                                    return;
                                }

                                user.take(this.currency, amount).thenAccept(response -> {
                                    // TODO: Fail and success messages
                                    if (!response.success()) {
                                        return;
                                    }
                                });
                            });
                        });
                    });
                });

        CommandAPICommand adminSetCommand = new CommandAPICommand("set")
                .withPermission("axcoins.%s.admin.set".formatted(this.currency.identifier()))
                .withArguments(new AsyncOfflinePlayerArgument("player"), this.currency.config().allowDecimals() ? NumberArguments.bigDecimal("amount", BigDecimal.ZERO, BigDecimal.valueOf(Long.MAX_VALUE)) : NumberArguments.bigInteger("amount", BigInteger.ZERO, BigInteger.valueOf(Long.MAX_VALUE)))
                .executes((sender, args) -> {
                    CompletableFuture<OfflinePlayer> player = args.getUnchecked("player");
                    if (player == null) {
                        return;
                    }

                    BigDecimal amount;
                    if (config.allowDecimals()) {
                        amount = args.getByClass("amount", BigDecimal.class);
                    } else {
                        BigInteger value = args.getByClass("amount", BigInteger.class);
                        if (value == null) {
                            return;
                        }

                        amount = new BigDecimal(value);
                    }

                    if (amount == null) {
                        return;
                    }

                    player.thenAccept(offlinePlayer -> {
                        AxCoinsAPI.instance().getUser(offlinePlayer).thenAccept(user -> {
                            user.set(this.currency, amount).thenAccept(response -> {
                                // TODO: Fail and success messages
                                if (!response.success()) {
                                    return;
                                }
                            });
                        });
                    });
                });

        adminCommand.withSubcommand(adminGiveCommand);
        adminCommand.withSubcommand(adminSetCommand);
        adminCommand.withSubcommand(adminTakeCommand);
        adminCommand.withSubcommand(adminGiveAllCommand);

        command.withSubcommand(paySubCommand);
        command.withSubcommand(balanceCommand);
        command.withSubcommand(adminCommand);
        command.register();

    }
}
