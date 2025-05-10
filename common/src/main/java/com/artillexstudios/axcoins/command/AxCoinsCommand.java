package com.artillexstudios.axcoins.command;

import dev.jorel.commandapi.CommandTree;

public class AxCoinsCommand {

    public static void register() {
        new CommandTree("axcoins")
                .withAliases("axc")
                .register();
    }
}
