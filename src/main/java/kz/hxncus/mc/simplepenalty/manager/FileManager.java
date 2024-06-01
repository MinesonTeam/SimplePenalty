package kz.hxncus.mc.simplepenalty.manager;

import kz.hxncus.mc.simplepenalty.SimplePenalty;
import kz.hxncus.mc.simplepenalty.util.Constants;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Getter
@EqualsAndHashCode
public class FileManager {
    private static SimplePenalty plugin;
    private File langsDir;

    public FileManager(SimplePenalty plugin) {
        FileManager.plugin = plugin;
        loadDirs();
        loadFiles();
        if (isConfigOutdated()) {
            updateFiles();
        }
    }

    private void loadDirs() {
        langsDir = new File(plugin.getDataFolder(), "langs");
        if (!langsDir.exists()) {
            langsDir.mkdirs();
        }
    }

    private void loadFiles() {
        for (String filePath : Constants.FILES) {
            if (!new File(plugin.getDataFolder(), filePath).exists()) {
                plugin.saveResource(filePath, false);
            }
        }
    }

    private boolean isConfigOutdated() {
        FileConfiguration embeddedConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(plugin.getResource("config.yml")));
        return plugin.getConfig().getInt(Constants.VERSION, 0) != embeddedConfig.getInt(Constants.VERSION, 0);
    }

    private void updateFiles() {
        plugin.getLogger().warning("Plugin version is outdated! Updating...");
        for (String filePath : Constants.FILES) {
            File file = new File(plugin.getDataFolder(), filePath);
            FileConfiguration config = YamlConfiguration.loadConfiguration(file);

            try (InputStream resource = plugin.getResource(filePath.replace('\\', '/'))) {
                if (resource == null) {
                    continue;
                }
                FileConfiguration embeddedConfig = YamlConfiguration.loadConfiguration(new InputStreamReader(resource));
                config.options().copyDefaults(true);
                config.setDefaults(embeddedConfig);
                if ("config.yml".equals(filePath)) {
                    config.set(Constants.VERSION, embeddedConfig.getInt(Constants.VERSION, 0));
                }
                removeNonexistentKeys(config, embeddedConfig);
                saveConfig(config, file);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        plugin.getLogger().info("Files has been updated!");
    }

    private static void removeNonexistentKeys(FileConfiguration config, FileConfiguration embeddedConfig) {
        for (String key : config.getKeys(true)) {
            if (embeddedConfig.get(key) == null) {
                config.set(key, null);
            }
        }
    }

    private static void saveConfig(FileConfiguration config, File file) {
        try {
            config.save(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
