package com.artillexstudios.axcoins.command.argument;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberArguments {

    public static Argument<BigDecimal> bigDecimal(String name, BigDecimal minValue, BigDecimal maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            return null;
        });
    }

    public static Argument<BigInteger> bigInteger(String name, BigInteger minValue, BigInteger maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            return null;
        });
    }
}
