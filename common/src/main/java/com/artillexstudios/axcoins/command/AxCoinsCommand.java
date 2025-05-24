package com.artillexstudios.axcoins.command;

import com.artillexstudios.axapi.AxPlugin;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import dev.jorel.commandapi.CommandTree;

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
                .register();
    }
}
