package com.artillexstudios.axcoins.currency.impl;

import com.artillexstudios.axcoins.config.CurrencyConfiguration;
import com.artillexstudios.axcoins.config.Language;
import org.apache.commons.io.FilenameUtils;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.math.BigDecimal;
import java.util.List;

public class CurrencyConfig implements com.artillexstudios.axcoins.api.currency.config.CurrencyConfig {
    private final File file;
    private CurrencyConfiguration configuration;

    public CurrencyConfig(File file) {
        this.file = file;
        this.configuration = CurrencyConfiguration.load(file);
    }

    @Override
    public void refresh() {
        this.configuration = CurrencyConfiguration.load(this.file);
    }

    @Override
    public String identifier() {
        return FilenameUtils.getBaseName(this.file.getName());
    }

    @Override
    public String name() {
        return this.configuration.name;
    }

    @Override
    public String provider() {
        return this.configuration.provider;
    }

    @Override
    public String configProvider() {
        return this.configuration.configProvider;
    }

    @Override
    public BigDecimal startingValue() {
        return this.configuration.startingValue;
    }

    @Override
    public BigDecimal maximumValue() {
        return this.configuration.maximumValue;
    }

    @Override
    public BigDecimal minimumValue() {
        return this.configuration.minimumValue;
    }

    @Override
    public boolean allowDecimals() {
        return this.configuration.allowDecimals;
    }

    @Override
    public boolean enablePay() {
        return this.configuration.enablePay;
    }

    @Override
    public List<String> commands() {
        return this.configuration.commands;
    }

    @Override
    public @Nullable String permission() {
        return this.configuration.permission;
    }

    @Override
    public BigDecimal minimumPayAmount() {
        return this.configuration.minimumPayAmount;
    }

    @Override
    public BigDecimal maximumPayAmount() {
        return this.configuration.maximumPayAmount;
    }

    @Override
    public String prefix() {
        String prefix = this.configuration.messages.prefix;
        return prefix == null ? Language.currencies.prefix : prefix;
    }

    @Override
    public String balance() {
        String balance = this.configuration.messages.balance;
        return balance == null ? Language.currencies.balance.replace("%currency%", this.identifier()) : balance;
    }

    @Override
    public String insufficientFunds() {
        String insufficientFunds = this.configuration.messages.insufficientFunds;
        return insufficientFunds == null ? Language.currencies.insufficientFunds : insufficientFunds;
    }

    @Override
    public String cantSendSelf() {
        String cantSendSelf = this.configuration.messages.cantSendSelf;
        return cantSendSelf == null ? Language.currencies.cantSendSelf : cantSendSelf;
    }

    @Override
    public String giveFailed() {
        String giveFailed = this.configuration.messages.giveFailed;
        return giveFailed == null ? Language.currencies.giveFailed : giveFailed;
    }

    @Override
    public String giveSuccess() {
        return "";
    }
}
