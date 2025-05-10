package com.artillexstudios.axcoins;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axcoins.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;

public final class AxCoinsPlugin extends AxPlugin {
    private static AxCoinsPlugin instance;

    @Override
    public void dependencies(DependencyManagerWrapper manager) {

    }

    @Override
    public void load() {
        instance = this;

        FileUtils.copyFromResource("currencies");
        CommandAPI.onLoad(new CommandAPIBukkitConfig(this)
                .skipReloadDatapacks(true)
                .setNamespace("axcoins")
        );
    }

    @Override
    public void enable() {
        CommandAPI.onEnable();
    }

    @Override
    public void disable() {
        CommandAPI.onDisable();
    }

    public static AxCoinsPlugin instance() {
        return instance;
    }
}
