package com.artillexstudios.axcoins;

import com.artillexstudios.axapi.AxPlugin;
import com.artillexstudios.axapi.database.DatabaseHandler;
import com.artillexstudios.axapi.dependencies.DependencyManagerWrapper;
import com.artillexstudios.axapi.utils.AsyncUtils;
import com.artillexstudios.axapi.utils.featureflags.FeatureFlags;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.command.AxCoinsCommand;
import com.artillexstudios.axcoins.config.Config;
import com.artillexstudios.axcoins.currency.ConfigCurrencyLoader;
import com.artillexstudios.axcoins.currency.Currencies;
import com.artillexstudios.axcoins.currency.CurrencyConfigProviders;
import com.artillexstudios.axcoins.currency.CurrencyProviders;
import com.artillexstudios.axcoins.currency.DefaultCurrencyConfigProvider;
import com.artillexstudios.axcoins.currency.impl.ConfigCurrencyProvider;
import com.artillexstudios.axcoins.database.DatabaseAccessor;
import com.artillexstudios.axcoins.listener.PlayerListener;
import com.artillexstudios.axcoins.placeholders.Placeholders;
import com.artillexstudios.axcoins.user.UserRepository;
import com.artillexstudios.axcoins.utils.FileUtils;
import org.bukkit.Bukkit;

public final class AxCoinsPlugin extends AxPlugin {
    private DatabaseAccessor accessor;
    private Currencies currencies;
    private UserRepository userRepository;
    private CurrencyProviders currencyProviders;
    private CurrencyConfigProviders currencyConfigProviders;
    private ConfigCurrencyLoader loader;
    private static AxCoinsPlugin instance;

    @Override
    public void updateFlags(FeatureFlags flags) {
        flags.PLACEHOLDER_API_HOOK.set(true);
        flags.PLACEHOLDER_API_IDENTIFIER.set("axcoins");
    }

    @Override
    public void dependencies(DependencyManagerWrapper manager) {
        manager.dependency("dev{}jorel:commandapi-bukkit-shade:10.0.0", true);
        manager.dependency("com{}h2database:h2:2.3.232");
        manager.dependency("com{}zaxxer:HikariCP:5.1.0");

        manager.relocate("dev{}jorel{}commandapi", "com.artillexstudios.axcoins.libs.commandapi");
        manager.relocate("com{}zaxxer", "com.artillexstudios.axcoins.libs.hikaricp");
        manager.relocate("org{}h2", "com.artillexstudios.axcoins.libs.h2");
    }

    @Override
    public void load() {
        instance = this;

        FileUtils.copyFromResource("currencies");
        AxCoinsCommand.load(this);
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
        this.userRepository = new UserRepository(this.accessor);
        this.currencyConfigProviders = new CurrencyConfigProviders();

        this.currencyConfigProviders.register("default", new DefaultCurrencyConfigProvider());
        this.currencyProviders.register("config", new ConfigCurrencyProvider());

        this.loader = new ConfigCurrencyLoader(this.currencies, this.currencyConfigProviders);
        // TODO: call an event or something so people can register their own providers
        this.loader.loadAll().thenRun(() -> {
            if (Config.debug) {
                LogUtils.debug("Loaded all config currencies!");
            }
        });

        new Placeholders().load();

        AxCoinsCommand.enable();
        Bukkit.getPluginManager().registerEvents(new PlayerListener(this.userRepository), this);
    }

    @Override
    public void disable() {
        AxCoinsCommand.disable();
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

    // TODO: reload command
    public CurrencyConfigProviders currencyConfigProviders() {
        return this.currencyConfigProviders;
    }
}
