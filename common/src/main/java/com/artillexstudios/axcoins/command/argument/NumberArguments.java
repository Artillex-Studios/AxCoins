package com.artillexstudios.axcoins.command.argument;

import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.utils.BigDecimalUtils;
import com.artillexstudios.axcoins.api.utils.BigIntegerUtils;
import com.artillexstudios.axcoins.api.utils.NumberFormatter;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberArguments {
    public static final NumberFormatter formatter = AxCoinsAPI.instance().newFormatter();

    public static Argument<BigDecimal> bigDecimal(String name, BigDecimal minValue, BigDecimal maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            try {
                return BigDecimalUtils.clamp(formatter.parseBigDecimal(parser.input()), minValue, maxValue);
            } catch (NumberFormatException exception) {
                // TODO: Message
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(Component.empty());
            }
        });
    }

    public static Argument<BigInteger> bigInteger(String name, BigInteger minValue, BigInteger maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            try {
                return BigIntegerUtils.clamp(formatter.parseBigDecimal(parser.input()).toBigInteger(), minValue, maxValue);
            } catch (NumberFormatException exception) {
                // TODO: Message
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(Component.empty());
            }
        });
    }
}
