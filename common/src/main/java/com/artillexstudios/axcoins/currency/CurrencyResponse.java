package com.artillexstudios.axcoins.currency;

import java.math.BigDecimal;

public record CurrencyResponse(BigDecimal amount, boolean success) implements com.artillexstudios.axcoins.api.currency.CurrencyResponse {
}
