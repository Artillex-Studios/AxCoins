package com.artillexstudios.axcoins.command;

import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.config.CurrencyConfig;
import com.artillexstudios.axcoins.api.user.User;
import dev.jorel.commandapi.CommandAPICommand;
import dev.jorel.commandapi.arguments.DoubleArgument;
import dev.jorel.commandapi.arguments.IntegerArgument;
import dev.jorel.commandapi.arguments.PlayerArgument;
import org.bukkit.entity.Player;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

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
            MessageUtils.sendMessage(sender, config.prefix(), config.balance());
        });

        CommandAPICommand paySubCommand = new CommandAPICommand("pay")
                .withArguments(new PlayerArgument("player"), config.allowDecimals() ? new DoubleArgument("amount", 0) : new IntegerArgument("amount", 0))
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

                    if (config.allowDecimals()) {
                        Double doubleAmount = args.getByClass("amount", Double.class);
                        if (doubleAmount == null) {
                            return;
                        }

                        BigDecimal amount = new BigDecimal(doubleAmount);
                        user.has(this.currency, amount).thenAccept(has -> {
                            if (!has) {
                                MessageUtils.sendMessage(sender, config.prefix(), config.insufficientFunds());
                                return;
                            }


                        });
                    }
                });

        command.withSubcommand(paySubCommand);
        command.register();

    }
}
