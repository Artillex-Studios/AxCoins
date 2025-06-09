package com.artillexstudios.axcoins.api.currency.config;

public interface MessagesConfig {

    String prefix();

    String balance();

    String balanceOther();

    String cooldown();

    String insufficientFunds();

    String cantSendSelf();

    String giveFailed();

    String giveSuccess();

    String receiverTooMuch();

    String paySuccess();

    String receiveSuccess();
}
