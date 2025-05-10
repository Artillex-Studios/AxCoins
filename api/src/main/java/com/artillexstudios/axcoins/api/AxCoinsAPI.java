package com.artillexstudios.axcoins.api;

import com.artillexstudios.axcoins.api.currency.Currencies;
import com.artillexstudios.axcoins.api.currency.provider.CurrencyProviders;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.api.user.UserRepository;
import net.kyori.adventure.util.Services;
import org.bukkit.OfflinePlayer;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface AxCoinsAPI {

    static AxCoinsAPI instance() {
        return Holder.INSTANCE;
    }

    default User getUserIfLoadedImmediately(OfflinePlayer player) {
        return this.getUserIfLoadedImmediately(player.getUniqueId());
    }

    default User getUserIfLoadedImmediately(UUID uuid) {
        return this.repository().getUserIfLoadedImmediately(uuid);
    }

    default CompletableFuture<User> getUser(OfflinePlayer player) {
        return this.getUser(player.getUniqueId());
    }

    default CompletableFuture<User> getUser(UUID uuid) {
        return this.repository().getUser(uuid, LoadContext.TEMPORARY);
    }

    UserRepository repository();

    Currencies currencies();

    CurrencyProviders providers();

    final class Holder {
        private static final AxCoinsAPI INSTANCE = Services.service(AxCoinsAPI.class).orElseThrow();
    }
}
