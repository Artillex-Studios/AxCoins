package com.artillexstudios.axcoins.user;

import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.LoadContext;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.exception.UserAlreadyLoadedException;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.database.DatabaseAccessor;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import java.util.Collection;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public final class UserRepository implements com.artillexstudios.axcoins.api.user.UserRepository {
    private final DatabaseAccessor accessor;
    private final ConcurrentHashMap<UUID, User> loadedUsers = new ConcurrentHashMap<>();
    private final Cache<UUID, User> tempUsers = Caffeine.newBuilder()
            .expireAfterAccess(5, TimeUnit.MINUTES)
            .maximumSize(200)
            .build();

    public UserRepository(DatabaseAccessor accessor) {
        this.accessor = accessor;
    }

    @Override
    public User getUserIfLoadedImmediately(UUID uuid) {
        User user = this.loadedUsers.get(uuid);
        if (user != null) {
            return user;
        }

        user = this.tempUsers.getIfPresent(uuid);
        return user;
    }

    @Override
    public CompletableFuture<User> loadUser(UUID uuid) throws UserAlreadyLoadedException {
        if (this.loadedUsers.containsKey(uuid)) {
            throw new UserAlreadyLoadedException();
        }

        User user = this.tempUsers.getIfPresent(uuid);
        if (user != null) {
            this.tempUsers.invalidate(uuid);
            this.loadedUsers.put(uuid, user);

            // Update the values of the currencies, they may be out of sync
            for (Currency currency : AxCoinsAPI.instance().currencies().registered()) {
                user.value(currency);
            }

            return CompletableFuture.completedFuture(user);
        }

        return this.getUser(uuid, LoadContext.FULL);
    }

    @Override
    public CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext) {
        User user = this.getUserIfLoadedImmediately(uuid);
        if (user != null) {
            return CompletableFuture.completedFuture(user);
        }

        // Deal with funny loading order
        return this.accessor.loadUser(uuid).thenApply(loaded -> {
            User temp;
            if (loadContext == LoadContext.FULL) {
                temp = this.loadedUsers.putIfAbsent(uuid, loaded);
            } else {
                temp = this.tempUsers.asMap().putIfAbsent(uuid, loaded);
            }

            return temp == null ? loaded : temp;
        });
    }

    @Override
    public Collection<User> onlineUsers() {
        return Collections.unmodifiableCollection(this.loadedUsers.values());
    }

    @Override
    public User disconnect(UUID uuid) {
        User user = this.loadedUsers.remove(uuid);

        if (user != null) {
            this.tempUsers.put(uuid, user);
        }

        return user;
    }
}