package xyz.gamecrash.gatekeeper.config;

import org.spongepowered.configurate.CommentedConfigurationNode;
import org.spongepowered.configurate.ConfigurateException;
import org.spongepowered.configurate.yaml.NodeStyle;
import org.spongepowered.configurate.yaml.YamlConfigurationLoader;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;

public class ConfigManager {
    private final GateKeeper plugin;

    private YamlConfigurationLoader loader;
    private final Path configPath;
    private CommentedConfigurationNode config;

    public ConfigManager(GateKeeper plugin) {
        this.plugin = plugin;

        configPath = plugin.getDataDirectory().resolve("config.yml");
        if (!configPath.toFile().exists()) {
            plugin.getLogger().info("Config.yml does not exist. Copying default config.");
            copyDefaultConfig();
        }
    }

    public void loadConfiguration() {
        loader = YamlConfigurationLoader.builder()
            .path(configPath)
            .indent(2)
            .nodeStyle(NodeStyle.BLOCK)
            .build();
        try {
            config = loader.load();
        } catch (ConfigurateException e) {
            plugin.getLogger().error("Failed to load config.yml!", e);
            return;
        }
        plugin.getLogger().info("Loaded configuration from config.yml");
    }

    public boolean isWhitelistEnabled() {
        return config.node("enabled").getBoolean(true);
    }

    public void setWhitelistEnabled(boolean enabled) {
        try {
            config.node("enabled").set(enabled);
            loader.save(config);
        } catch (IOException e) {
            plugin.getLogger().error("Failed to save config.yml!", e);
        }
    }

    public String getConfigMessage(String... path) {
        return config.node(path).getString();
    }

    private void copyDefaultConfig() {
        try {
            URL resourceLocation = getClass().getResource("/config.yml");
            if (resourceLocation == null) {
                plugin.getLogger().error("Could not find default config.yml as a resource. Cannot create default config.");
            }
            File configFile = configPath.toFile();
            Files.copy(resourceLocation.openStream(), configFile.toPath());
            plugin.getLogger().info("Default config.yml copied successfully.");
        } catch (IOException e) {
            plugin.getLogger().error("Failed to copy default config.yml file.", e);
        }
    }
}
