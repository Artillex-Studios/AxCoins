package com.artillexstudios.axcoins.command.argument;

import com.artillexstudios.axapi.utils.StringUtils;
import com.artillexstudios.axcoins.api.AxCoinsAPI;
import com.artillexstudios.axcoins.api.utils.BigDecimalUtils;
import com.artillexstudios.axcoins.api.utils.BigIntegerUtils;
import com.artillexstudios.axcoins.api.utils.NumberFormatter;
import com.artillexstudios.axcoins.config.Language;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberArguments {
    public static final NumberFormatter formatter = AxCoinsAPI.instance().newFormatter();

    public static Argument<BigDecimal> bigDecimal(String name, BigDecimal minValue, BigDecimal maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            try {
                return BigDecimalUtils.clamp(formatter.parseBigDecimal(parser.input()), minValue, maxValue);
            } catch (NumberFormatException exception) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.invalidNumberFormat));
            }
        });
    }

    public static Argument<BigInteger> bigInteger(String name, BigInteger minValue, BigInteger maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            try {
                return BigIntegerUtils.clamp(formatter.parseBigDecimal(parser.input()).toBigInteger(), minValue, maxValue);
            } catch (NumberFormatException exception) {
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(StringUtils.format(Language.invalidNumberFormat));
            }
        });
    }
}
