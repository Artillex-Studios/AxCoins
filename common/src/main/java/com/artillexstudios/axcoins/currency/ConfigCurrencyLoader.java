package com.artillexstudios.axcoins.currency;

import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyConfigProvider;
import com.artillexstudios.axcoins.config.CurrencyConfiguration;
import com.artillexstudios.axcoins.currency.impl.CurrencyConfig;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;

public final class ConfigCurrencyLoader {
    private final Currencies currencies;
    private final CurrencyConfigProviders providers;

    public ConfigCurrencyLoader(Currencies currencies, CurrencyConfigProviders providers) {
        this.currencies = currencies;
        this.providers = providers;
    }

    public CompletableFuture<?> loadAll() {
        Collection<File> files = FileUtils.listFiles(com.artillexstudios.axcoins.utils.FileUtils.PLUGIN_DIRECTORY.resolve("currencies/").toFile(), new String[]{"yml", "yaml"}, true);
        List<CompletableFuture<?>> futures = new ArrayList<>();
        for (File file : files) {
            if (!YamlUtils.suggest(file)) {
                continue;
            }

            CurrencyConfiguration configuration = CurrencyConfiguration.load(file);
            CurrencyConfigProvider<CurrencyConfig> configProvider = this.providers.fetch(configuration.configProvider);
            if (configProvider == null) {
                LogUtils.warn("Unknown config provider {}!", configuration.configProvider);
                continue;
            }

            futures.add(this.currencies.register(configProvider.provide(file)));
        }

        return CompletableFuture.allOf(futures.toArray(new CompletableFuture[0]));
    }

    public void reloadAll() {
        for (Currency currency : this.currencies.registered()) {
            currency.config().refresh();
        }
    }
}
