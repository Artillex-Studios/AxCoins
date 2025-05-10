package com.artillexstudios.axcoins.api.user;

import com.artillexstudios.axcoins.api.LoadContext;
import com.artillexstudios.axcoins.api.exception.UserAlreadyLoadedException;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public interface UserRepository {

    User getUserIfLoadedImmediately(UUID uuid);

    CompletableFuture<User> loadUser(UUID uuid) throws UserAlreadyLoadedException;

    CompletableFuture<User> getUser(UUID uuid, LoadContext loadContext);

    User disconnect(UUID uuid);
}
