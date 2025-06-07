package com.artillexstudios.axcoins.command;

import com.artillexstudios.axapi.placeholders.PlaceholderHandler;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.command.argument.NumberArguments;
import com.artillexstudios.axcoins.config.Language;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.AsyncOfflinePlayerArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
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
                MessageUtils.sendMessage(sender, config.prefix(), Language.notYetLoaded);
                return;
            }

            MessageUtils.sendMessage(sender, config.prefix(), PlaceholderHandler.parse(config.balance(), user));
        });

        CommandAPICommand paySubCommand = new CommandAPICommand("pay")
                .withArguments(new AsyncOfflinePlayerArgument("player"), config.allowDecimals() ? NumberArguments.bigDecimal("amount", this.currency.config().minimumValue(), this.currency.config().maximumValue()) : NumberArguments.bigInteger("amount", this.currency.config().minimumValue().toBigInteger(), this.currency.config().maximumValue().toBigInteger()))
                .executesPlayer((sender, args) -> {
                    User user = AxCoinsAPI.instance().getUserIfLoadedImmediately(sender);
                    if (user == null) {
                        MessageUtils.sendMessage(sender, config.prefix(), Language.notYetLoaded);
                        return;
                    }

                    CompletableFuture<OfflinePlayer> playerFuture = args.getUnchecked("player");
                    if (playerFuture == null) {
                        return;
                    }

                    playerFuture.thenAccept(player -> {
                        if (player.getUniqueId().equals(sender.getUniqueId())) {
                            MessageUtils.sendMessage(sender, config.prefix(), config.cantSendSelf());
                            return;
                        }

                        User receiver = AxCoinsAPI.instance().getUserIfLoadedImmediately(player);
                        if (receiver == null) {
                            MessageUtils.sendMessage(sender, config.prefix(), Language.otherNotYetLoaded);
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
                                    MessageUtils.sendMessage(sender, config.prefix(), config.receiverTooMuch());
                                    return;
                                }

                                user.take(this.currency, amount).thenAccept(response -> {
                                    if (!response.success()) {
                                        MessageUtils.sendMessage(sender, config.prefix(), config.insufficientFunds());
                                        return;
                                    }

                                    receiver.give(this.currency, amount).thenAccept(giveResponse -> {
                                        if (!response.success()) {
                                            MessageUtils.sendMessage(sender, config.prefix(), config.receiverTooMuch());
                                            user.give(this.currency, amount);
                                            return;
                                        }

                                        MessageUtils.sendMessage(sender, config.prefix(), config.paySuccess());
                                        Player onlinePlayer = player.getPlayer();
                                        if (onlinePlayer != null) {
                                            MessageUtils.sendMessage(onlinePlayer, config.prefix(), config.receiveSuccess());
                                        }
                                    });
                                });
                            });
                        });
                    });
                });


        CommandAPICommand balanceCommand = new CommandAPICommand("balance")
                .withOptionalArguments(new AsyncOfflinePlayerArgument("player")
                        .withPermission("axcoins.%s.admin.set".formatted(this.currency.identifier()))
                )
                .executesPlayer((sender, args) -> {
                    Optional<CompletableFuture<OfflinePlayer>> playerFuture = args.getOptionalUnchecked("player");
                    if (playerFuture.isEmpty()) {
                        playerFuture = Optional.of(CompletableFuture.completedFuture(sender));
                    }
                    playerFuture.orElseThrow().thenAccept(player -> {
                        AxCoinsAPI.instance().getUser(player).thenAccept(user -> {
                            if (user == null) {
                                MessageUtils.sendMessage(sender, config.prefix(), Language.notYetLoaded);
                                return;
                            }

                            MessageUtils.sendMessage(sender, config.prefix(), PlaceholderHandler.parse(config.balance(), user));
                        });
                    });
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
                                if (!response.success()) {
                                    MessageUtils.sendMessage(sender, config.prefix(), config.giveFailed(), Placeholder.unparsed("player", offlinePlayer.getName()),
                                            Placeholder.unparsed("amount", NumberArguments.formatter.format(amount)),
                                            Placeholder.parsed("currency", this.currency.config().name())
                                    );
                                    return;
                                }

                                MessageUtils.sendMessage(sender, config.prefix(), config.giveSuccess(), Placeholder.unparsed("player", offlinePlayer.getName()),
                                        Placeholder.unparsed("amount", NumberArguments.formatter.format(amount)),
                                        Placeholder.parsed("currency", this.currency.config().name()),
                                        Placeholder.parsed("balance", NumberArguments.formatter.format(response.amount()))
                                );
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
