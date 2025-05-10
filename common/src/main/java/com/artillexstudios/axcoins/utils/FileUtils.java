package com.artillexstudios.axcoins.utils;

import com.artillexstudios.axapi.utils.logging.LogUtils;
import com.artillexstudios.axcoins.AxCoinsPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Iterator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public final class FileUtils {
    public static final Path PLUGIN_DIRECTORY = AxCoinsPlugin.instance().getDataFolder().toPath();

    public static void copyFromResource(@NotNull String path) {
        try (ZipFile zip = new ZipFile(Paths.get(AxCoinsPlugin.instance().getClass().getProtectionDomain().getCodeSource().getLocation().toURI()).toFile())) {
            for (Iterator<? extends ZipEntry> it = zip.entries().asIterator(); it.hasNext(); ) {
                ZipEntry entry = it.next();
                if (!entry.getName().startsWith(path + "/")) continue;
                if (!entry.getName().endsWith(".yaml") && !entry.getName().endsWith(".yml")) continue;

                InputStream resource = AxCoinsPlugin.instance().getResource(entry.getName());
                if (resource == null) {
                    LogUtils.error("Could not find file {} in plugin's assets!", entry.getName());
                    continue;
                }
                Path to = PLUGIN_DIRECTORY.resolve(entry.getName());
                if (to.toFile().exists()) {
                    continue;
                }

                Files.createDirectories(to.getParent());
                Files.copy(resource, to);
            }
        } catch (IOException | URISyntaxException exception) {
            LogUtils.error("An unexpected error occurred while extracting directory {} from plugin's assets!", path, exception);
        }
    }
}