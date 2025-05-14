package com.artillexstudios.axcoins.command.argument;

import com.artillexstudios.axcoins.config.Config;
import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberArguments {

    // TODO: Check bounds
    public static Argument<BigDecimal> bigDecimal(String name, BigDecimal minValue, BigDecimal maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            try {
                return parseBigDecimal(parser.input());
            } catch (NumberFormatException exception) {
                // TODO: Message
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(Component.empty());
            }
        });
    }

    // TODO: Check bounds
    public static Argument<BigInteger> bigInteger(String name, BigInteger minValue, BigInteger maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            try {
                return parseBigDecimal(parser.input()).toBigInteger();
            } catch (NumberFormatException exception) {
                // TODO: Message
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(Component.empty());
            }
        });
    }

    public static BigDecimal parseBigDecimal(String input) throws NumberFormatException {
        int i = 0;
        for (; i < input.length(); i++) {
            char c = input.charAt(i);
            if (!Character.isDigit(c) && c != '.' && c != ',') {
                break;
            }
        }

        String numberParts = input.substring(0, i);
        BigDecimal decimal = new BigDecimal(numberParts);
        if (numberParts.length() == input.length()) {
            return decimal;
        }

        String numberFormatChars = input.substring(i);
        BigDecimal shortHandValue = Config.numberFormatting.shorthandValues.get(numberFormatChars);
        if (shortHandValue == null) {
            // Go character by character
            for (int j = 0; j < numberFormatChars.length(); j++) {
                char c = input.charAt(j);
                BigDecimal found = Config.numberFormatting.shorthandValues.get(String.valueOf(c));
                if (found == null) {
                    break;
                }

                if (shortHandValue == null) {
                    shortHandValue = found;
                } else {
                    shortHandValue = shortHandValue.multiply(found);
                }
            }
        }

        if (shortHandValue != null) {
            decimal = decimal.multiply(shortHandValue);
        }

        return decimal;
    }
}
