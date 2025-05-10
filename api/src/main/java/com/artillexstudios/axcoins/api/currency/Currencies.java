package com.artillexstudios.axcoins.api.currency;

import org.jspecify.annotations.Nullable;

import java.util.Collection;
import java.util.Set;

public interface Currencies {

    void register(Currency currency);

    void deregister(Currency currency);

    @Nullable
    Currency fetch(int id);

    @Nullable
    Currency fetch(String identifier);

    Collection<Currency> registered();

    Set<String> names();
}
