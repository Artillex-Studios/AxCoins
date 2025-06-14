package com.artillexstudios.axcoins.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Header;
import com.artillexstudios.axapi.config.annotation.Ignored;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.AxCoinsPlugin;
import com.artillexstudios.axcoins.utils.FileUtils;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

@Header("""
        """)
public class Language implements ConfigurationPart {
    private static final Path LANGUAGE_DIRECTORY = FileUtils.PLUGIN_DIRECTORY.resolve("language");
    private static final Language INSTANCE = new Language();
    public static Reload reload = new Reload();
    @Comment(""" 
            These are settings for all of the currencies, each of them can
            be overwritten in the currencies' config file. If you add a messages: {}
            section to the file of the currency, reload the configs using the command,
            you will see all the keys that you can set, just like this, but with values set to
            null. You can freely overwrite them there, values which are null will use the option
            from this file.
            """)
    public static Currencies currencies = new Currencies();

    @Serializable
    public static class Reload {
        public String success = "<#00FF00>Successfully reloaded the configurations of the plugin in <white><time></white>ms!";
        public String fail = "<#FF0000>There were some issues while reloading file(s): <white><files></white>! Please check out the console for more information! <br>Reload done in: <white><time></white>ms!";
    }

    @Serializable
    public static final class Currencies {
        public String prefix = "<#DDCC00><b>AxCoins</b> <gray>» ";
        public String balance = "<white>Your current balance is <amount_5>";
        public String cooldown = "<#FF0000>You are still on cooldown for <cooldown> seconds!";
        public String balanceOther = "<white><player>'s current balance is <amount_%currency%_5>";
        public String insufficientFunds = "<#FF0000>You don't have enough coins for that!";
        public String cantSendSelf = "<#FF0000>You can't send coins to yourself!";
        public String giveFailed = "<#FF0000>Failed to give <amount> <currency> to <player>!";
        public String giveSuccess = "<#00FF00>Successfully gave <white><player> <amount></white> of currency <white><currency></white>! Their new balance is: <white><balance_short_%currency%_5></white>.";
        public String receiverTooMuch = "<#FF0000>Can't send currency, as the new amount would exceed the max of the currency!";
        public String paySuccess = "<#00FF00>You successfully paid <white><amount></white> to <white><player></white>! Your new balance is <amount_%currency%_5>!";
        public String receiveSuccess = "<#00FF00><white><player></white> paid you <white><amount></white>! Your new balance is <amount_%currency%_5>!";
    }

    public static String invalidNumberFormat = "<#FF0000>Invalid number format <white><number></white>! Try something like <white>1000</white>!";
    public static String notYetLoaded = "<#FF0000>Your user data has not loaded yet! Please try again in a bit!";
    public static String otherNotYetLoaded = "<#FF0000>The other user's data has not loaded yet! Please try again in a bit!";

    @Comment("Do not touch!")
    public static int configVersion = 1;
    @Ignored
    public static String lastLanguage;
    private YamlConfiguration<?> config = null;

    public static boolean reload() {
        if (Config.debug) {
            LogUtils.debug("Reload called on language!");
        }
        FileUtils.copyFromResource("language");

        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        if (Config.debug) {
            LogUtils.debug("Refreshing language");
        }
        Path path = LANGUAGE_DIRECTORY.resolve(Config.language + ".yml");
        boolean shouldDefault = false;
        if (Files.exists(path)) {
            if (Config.debug) {
                LogUtils.debug("File exists");
            }
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        } else {
            shouldDefault = true;
            path = LANGUAGE_DIRECTORY.resolve("en_US.yml");
            LogUtils.error("No language configuration was found with the name {}! Defaulting to en_US...", Config.language);
        }

        // The user might have changed the config
        if (this.config == null || (lastLanguage != null && lastLanguage.equalsIgnoreCase(Config.language))) {
            lastLanguage = shouldDefault ? "en_US" : Config.language;
            if (Config.debug) {
                LogUtils.debug("Set lastLanguage to {}", lastLanguage);
            }
            InputStream defaults = AxCoinsPlugin.instance().getResource("language/" + lastLanguage + ".yml");
            if (defaults == null) {
                if (Config.debug) {
                    LogUtils.debug("Defaults are null, defaulting to en_US.yml");
                }
                defaults = AxCoinsPlugin.instance().getResource("language/en_US.yml");
            }

            if (Config.debug) {
                LogUtils.debug("Loading config from file {} with defaults {}", path, defaults);
            }

            this.config = YamlConfiguration.of(path, Language.class)
                    .configVersion(1, "config-version")
                    .withDefaults(defaults)
                    .withDumperOptions(options -> {
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                        options.setSplitLines(false);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
