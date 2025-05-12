package com.artillexstudios.axcoins;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.config.Config;
import com.artillexstudios.axcoins.currency.Currencies;
import com.artillexstudios.axcoins.currency.CurrencyProviders;
import com.artillexstudios.axcoins.database.DatabaseAccessor;
import com.artillexstudios.axcoins.listener.PlayerListener;
import com.artillexstudios.axcoins.user.UserRepository;
import com.artillexstudios.axcoins.utils.FileUtils;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;

public final class AxCoinsPlugin extends AxPlugin {
    private DatabaseAccessor accessor;
    private Currencies currencies;
    private UserRepository userRepository;
    private CurrencyProviders currencyProviders;
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
        Config.reload();
        AsyncUtils.setup(Config.asyncProcessorPoolSize);
        this.currencies = new Currencies();
        this.currencyProviders = new CurrencyProviders();
        this.accessor = new DatabaseAccessor(this.currencies, new DatabaseHandler(this, Config.database));
        this.accessor.load().thenRun(() -> {
            if (Config.debug) {
                LogUtils.debug("Loaded DatabaseAccessor!");
            }
        });

        CommandAPI.onEnable();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this.userRepository), this);
    }

    @Override
    public void disable() {
        CommandAPI.onDisable();
    }

    public static AxCoinsPlugin instance() {
        return instance;
    }

    public DatabaseAccessor accessor() {
        return this.accessor;
    }

    public Currencies currencies() {
        return this.currencies;
    }

    public UserRepository userRepository() {
        return this.userRepository;
    }

    public CurrencyProviders currencyProviders() {
        return this.currencyProviders;
    }
}
