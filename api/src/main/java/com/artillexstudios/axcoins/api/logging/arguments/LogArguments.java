package com.artillexstudios.axcoins.api.logging.arguments;

import java.math.BigDecimal;
import java.util.Date;

public class LogArguments {
    public static LogArgument<Date> DATE = null;
    public static LogArgument<BigDecimal> AMOUNT = null;
    public static LogArgument<String> SOURCE = null;
    public static LogArgument<String> SOURCE_IP_ADDRESS = null;
    public static LogArgument<BigDecimal> SOURCE_PREVIOUS = null;
    public static LogArgument<BigDecimal> SOURCE_NEW = null;
    public static LogArgument<String> RECEIVER = null;
    public static LogArgument<String> RECEIVER_IP_ADDRESS = null;
    public static LogArgument<BigDecimal> RECEIVER_PREVIOUS = null;
    public static LogArgument<BigDecimal> RECEIVER_NEW = null;
}
