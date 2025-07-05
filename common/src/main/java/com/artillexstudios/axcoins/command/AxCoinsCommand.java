package com.artillexstudios.axcoins.command;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.handler.ListHandler;
import com.artillexstudios.axapi.database.handler.TransformerHandler;
import com.artillexstudios.axapi.utils.MessageUtils;
import com.artillexstudios.axcoins.AxCoinsPlugin;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.config.Config;
import com.artillexstudios.axcoins.config.Language;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;
import dev.jorel.commandapi.arguments.LiteralArgument;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class AxCoinsCommand {

    public static void load(AxPlugin plugin) {
        CommandAPI.onLoad(new CommandAPIBukkitConfig(plugin)
                .skipReloadDatapacks(true)
                .setNamespace("axcoins")
        );
    }

    public static void enable() {
        CommandAPI.onEnable();
    }

    public static void disable() {
        CommandAPI.onDisable();
    }

    public static void register() {
        new CommandTree("axcoins")
                .withAliases("axc")
                .then(new LiteralArgument("reload")
                        .withPermission("axcoins.command.reload")
                        .executes((sender, args) -> {
                            long start = System.nanoTime();
                            List<String> failed = new ArrayList<>();

                            if (!Config.reload()) {
                                failed.add("config.yml");
                            }

                            if (!Language.reload()) {
                                failed.add("language/" + Language.lastLanguage + ".yml");
                            }

                            if (failed.isEmpty()) {
                                MessageUtils.sendMessage(sender, Language.currencies.prefix, Language.reload.success, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)));
                            } else {
                                MessageUtils.sendMessage(sender, Language.currencies.prefix, Language.reload.fail, Placeholder.unparsed("time", Long.toString((System.nanoTime() - start) / 1_000_000)), Placeholder.unparsed("files", String.join(", ", failed)));
                            }
                        })
                )
                .then(new LiteralArgument("convert")
                        .withPermission("axcoins.command.convert")
                        .executes((sender, args) -> {
                            List<PointData> data = AxCoinsPlugin.instance().handler().rawQuery("SELECT uuid, points FROM playerpoints_points;", new ListHandler<>(new TransformerHandler<>(PointData.class)))
                                    .create()
                                    .query();

                            Currency currency = AxCoinsAPI.instance().currencies().registered().stream().findFirst().orElseThrow();
                            for (PointData user : data) {
                                AxCoinsAPI.instance().getUser(UUID.fromString(user.uuid)).thenAccept(loadedUser -> {
                                    loadedUser.set(currency, BigDecimal.valueOf(user.points));
                                });
                            }
                        })
                )
                .register();
    }

    public record PointData(String uuid, Integer points) {

    }
}
