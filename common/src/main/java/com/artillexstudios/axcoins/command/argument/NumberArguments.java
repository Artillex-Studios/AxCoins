package com.artillexstudios.axcoins.command.argument;

import dev.jorel.commandapi.arguments.Argument;
import dev.jorel.commandapi.arguments.CustomArgument;
import dev.jorel.commandapi.arguments.StringArgument;
import net.kyori.adventure.text.Component;

import java.math.BigDecimal;
import java.math.BigInteger;

public class NumberArguments {

    public static Argument<BigDecimal> bigDecimal(String name, BigDecimal minValue, BigDecimal maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            String input = parser.currentInput();
            int i = 0;
            for (; i < input.length(); i++) {
                char c = input.charAt(i);
                if (!Character.isDigit(c) && c != '.' && c != ',') {
                    break;
                }
            }
            String numberParts = input.substring(i);
            try {
                BigDecimal decimal = new BigDecimal(numberParts);
                if (numberParts.length() == input.length()) {
                    return decimal;
                }

            } catch (NumberFormatException exception) {
                // TODO: Message
                throw CustomArgument.CustomArgumentException.fromAdventureComponent(Component.empty());
            }


            return null;
        });
    }

    public static Argument<BigInteger> bigInteger(String name, BigInteger minValue, BigInteger maxValue) {
        return new CustomArgument<>(new StringArgument(name), parser -> {
            return null;
        });
    }
}
