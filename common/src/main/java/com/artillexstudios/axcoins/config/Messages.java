package com.artillexstudios.axcoins.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axcoins.utils.FileUtils;

import java.nio.file.Files;
import java.nio.file.Path;

public class Messages implements ConfigurationPart {
    private static final Messages INSTANCE = new Messages();
    public static String prefix = "<#DDCC00><b>AxCoins</b> <gray>» ";
    public static String balance = "<white>Your current balance is %balance_short_%currency%_5%";
    public static String insufficientFunds = "<#FF0000>You don't have enough coins for that!";
    public static String cantSendSelf = "<#FF0000>You can't send coins to yourself!";

    @Comment("Do not touch!")
    public static int configVersion = 1;
    private YamlConfiguration<?> config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("messages.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
