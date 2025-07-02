package com.artillexstudios.axcoins.api.event;

import com.artillexstudios.axapi.context.HashMapContext;
import com.artillexstudios.axcoins.api.user.User;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;
import org.jetbrains.annotations.NotNull;

import java.math.BigDecimal;

public class CurrencyChangeEvent extends Event {
    private static final HandlerList HANDLER_LIST = new HandlerList();
    private final User user;
    private final BigDecimal before;
    private final BigDecimal after;
    private final HashMapContext context;

    public CurrencyChangeEvent(User user, BigDecimal before, BigDecimal after, HashMapContext context) {
        this.user = user;
        this.before = before;
        this.after = after;
        this.context = context;
    }

    public HashMapContext context() {
        return this.context;
    }

    public User user() {
        return this.user;
    }

    public BigDecimal before() {
        return this.before;
    }

    public BigDecimal after() {
        return this.after;
    }

    public static HandlerList getHandlerList() {
        return HANDLER_LIST;
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return HANDLER_LIST;
    }
}
