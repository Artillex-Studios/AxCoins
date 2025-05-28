package com.artillexstudios.axcoins.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Hidden;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;

import java.io.File;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class CurrencyConfiguration implements ConfigurationPart {
    public String name = "coins";
    public String provider = "config";
    public String configProvider = "default";
    public BigDecimal startingValue = BigDecimal.ZERO;
    public BigDecimal maximumValue = BigDecimal.valueOf(-1L);
    public BigDecimal minimumValue = BigDecimal.ZERO;
    public boolean allowDecimals = false;
    public boolean enablePay = true;
    public List<String> commands = new ArrayList<>(List.of("coins", "coin"));
    public String permission = "axcoins.coin";
    public BigDecimal minimumPayAmount = BigDecimal.ONE;
    public BigDecimal maximumPayAmount = BigDecimal.valueOf(-1L);
    @Hidden
    public Messages messages = new Messages();

    @Serializable
    public static final class Messages {
        public String prefix = null;
        public String balance = null;
        public String insufficientFunds = null;
        public String cantSendSelf = null;
        public String giveFailed = null;
        public String giveSuccess = null;
    }

    public static CurrencyConfiguration load(File file) {
        if (file.exists()) {
            if (!YamlUtils.suggest(file)) {
                LogUtils.error("Failed to load configuration for file {}! Defaulting to default settings for the currency!", file);
                return new CurrencyConfiguration();
            }
        }

        YamlConfiguration<CurrencyConfiguration> configuration = YamlConfiguration.of(file.toPath(), CurrencyConfiguration.class)
                .withDumperOptions(options -> {
                    options.setPrettyFlow(true);
                    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                }).build();

        return configuration.create();
    }

    public static String configurationProvider(File file) {
        YamlConfiguration<?> config = YamlConfiguration.of(file.toPath())
                .withDumperOptions(options -> {
                    options.setPrettyFlow(true);
                    options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                }).build();
        config.load();

        return config.getString("config-provider");
    }
}
