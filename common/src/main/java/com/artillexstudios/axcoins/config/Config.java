package com.artillexstudios.axcoins.config;

import com.artillexstudios.axapi.config.YamlConfiguration;
import com.artillexstudios.axapi.config.annotation.Comment;
import com.artillexstudios.axapi.config.annotation.ConfigurationPart;
import com.artillexstudios.axapi.config.annotation.Ignored;
import com.artillexstudios.axapi.config.annotation.PostProcess;
import com.artillexstudios.axapi.config.annotation.Serializable;
import com.artillexstudios.axapi.database.DatabaseConfig;
import com.artillexstudios.axapi.libs.snakeyaml.DumperOptions;
import com.artillexstudios.axapi.utils.YamlUtils;
import com.artillexstudios.axcoins.AxCoinsPlugin;
import com.artillexstudios.axcoins.utils.FileUtils;

import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class Config implements ConfigurationPart {
    private static final Config INSTANCE = new Config();
    public static DatabaseConfig database = new DatabaseConfig();
    public static Logging logging = new Logging();
    public static NumberFormatting numberFormatting = new NumberFormatting();

    @Serializable
    public static class Logging {
        @Comment("""
                If the plugin should store logs about transactions.
                """)
        public boolean enabled = true;
        @Comment("""
                If the plugin should store the stacktrace
                for API calls with no logging arguments.
                """)
        public boolean storeStackTrace = true;
    }

    @Serializable
    public static class NumberFormatting {
        public Map<String, BigDecimal> shorthandValues = Map.of("k", new BigDecimal("1000"), "m", new BigDecimal("1000000"), "b", new BigDecimal("1000000000"));
        @Comment("""
                When using formatting, how many precision digits should there be?
                For example, when transforming 1354320 with a precision of 4, the result is:
                1.3543m
                Whereas with a precision of two:
                1.35m
                """)
        public int precision = 3;
        @Ignored
        public Map<BigDecimal, String> sorted = new LinkedHashMap<>();

        @PostProcess
        public void postProcess() {
            this.sorted.clear();
            List<BigDecimal> values = new ArrayList<>();
            this.shorthandValues.forEach((key, value) -> {
                values.add(value);
            });
            values.sort(Comparator.reverseOrder());
            for (BigDecimal value : values) {
                for (Map.Entry<String, BigDecimal> entry : this.shorthandValues.entrySet()) {
                    if (entry.getValue().equals(value)) {
                        this.sorted.put(value, entry.getKey());
                        break;
                    }
                }
            }
        }
    }

    @Comment("""
            What the table prefix of the database should be.
            This is useful, if you want to connect multiple servers to the same database
            but with separate currency systems.
            """)
    public static String tablePrefix = "";
    @Comment("""
            The pool size of the asynchronous executor
            we use to process some things asynchronously,
            like database queries.
            """)
    public static int asyncProcessorPoolSize = 1;
    @Comment("""
            What language file should we load from the lang folder?
            You can create your own aswell! We would appreciate if you
            contributed to the plugin by creating a pull request with your translation!
            """)
    public static String language = "en_US";
    @Comment("""
            If we should send debug messages in the console
            You shouldn't enable this, unless you want to see what happens in the code.
            """)
    public static boolean debug = false;
    @Comment("Do not touch!")
    public static int configVersion = 1;
    private YamlConfiguration<?> config = null;

    public static boolean reload() {
        return INSTANCE.refreshConfig();
    }

    private boolean refreshConfig() {
        Path path = FileUtils.PLUGIN_DIRECTORY.resolve("config.yml");
        if (Files.exists(path)) {
            if (!YamlUtils.suggest(path.toFile())) {
                return false;
            }
        }

        if (this.config == null) {
            this.config = YamlConfiguration.of(path, Config.class)
                    .configVersion(1, "config-version")
                    .withDefaults(AxCoinsPlugin.instance().getResource("config.yml"))
                    .withDumperOptions(options -> {
                        options.setPrettyFlow(true);
                        options.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
                    }).build();
        }

        this.config.load();
        return true;
    }
}
