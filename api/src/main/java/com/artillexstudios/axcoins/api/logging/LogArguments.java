package com.artillexstudios.axcoins.api.logging;

import com.artillexstudios.axapi.context.ContextKey;

import java.math.BigDecimal;
import java.util.Date;

public class LogArguments {
    public static ContextKey<Date> DATE = ContextKey.of("date", Date.class);
    public static ContextKey<BigDecimal> AMOUNT = ContextKey.of("amount", BigDecimal.class);
    public static ContextKey<String> SOURCE = ContextKey.of("source", String.class);
    public static ContextKey<String> SOURCE_IP_ADDRESS = ContextKey.of("source_ip_address", String.class);
    public static ContextKey<BigDecimal> SOURCE_PREVIOUS = ContextKey.of("source_previous", BigDecimal.class);
    public static ContextKey<BigDecimal> SOURCE_NEW = ContextKey.of("source_new", BigDecimal.class);
    public static ContextKey<String> RECEIVER = ContextKey.of("receiver", String.class);
    public static ContextKey<String> RECEIVER_IP_ADDRESS = ContextKey.of("receiver_ip_address", String.class);
    public static ContextKey<BigDecimal> RECEIVER_PREVIOUS = ContextKey.of("receiver_previous", BigDecimal.class);
    public static ContextKey<BigDecimal> RECEIVER_NEW = ContextKey.of("receiver_new", BigDecimal.class);
    public static ContextKey<String> TYPE = ContextKey.of("type", String.class);
}
