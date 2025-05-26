package com.artillexstudios.axcoins.placeholders;

import com.artillexstudios.axapi.placeholders.PlaceholderArgument;
import com.artillexstudios.axapi.placeholders.PlaceholderArguments;
import com.artillexstudios.axapi.placeholders.PlaceholderHandler;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.currency.Currency;
import com.artillexstudios.axcoins.api.user.User;
import com.artillexstudios.axcoins.api.utils.NumberFormatter;
import org.bukkit.entity.Player;

public class Placeholders {
    private final NumberFormatter formatter = AxCoinsAPI.instance().newFormatter();

    public void load() {
        PlaceholderHandler.registerTransformer(Player.class, User.class, player -> AxCoinsAPI.instance().getUserIfLoadedImmediately(player));

        PlaceholderHandler.register("balance_raw_<currency>", new PlaceholderArguments(new PlaceholderArgument<>("currency", CurrencyResolver.class)), ctx -> {
            Currency currency = ctx.argument("currency");
            Player player = ctx.resolve(Player.class);
            User user = AxCoinsAPI.instance().getUserIfLoadedImmediately(player);
            return user.cached(currency).toString();
        }, TagPlaceholderFormatter.INSTANCE);

        PlaceholderHandler.register("balance_short_<currency>", new PlaceholderArguments(new PlaceholderArgument<>("currency", CurrencyResolver.class)), ctx -> {
            Currency currency = ctx.argument("currency");
            User user = ctx.resolve(User.class);
            return this.formatter.format(user.cached(currency));
        }, TagPlaceholderFormatter.INSTANCE);

        PlaceholderHandler.register("balance_short_<currency>_<precision>", new PlaceholderArguments(new PlaceholderArgument<>("currency", CurrencyResolver.class), new PlaceholderArgument<>("precision", IntegerResolver.class)), ctx -> {
            Currency currency = ctx.argument("currency");
            Integer precision = ctx.argument("precision");
            User user = ctx.resolve(User.class);
            return this.formatter.format(user.cached(currency), precision);
        }, TagPlaceholderFormatter.INSTANCE);
    }
}
