package xyz.gamecrash.velocitywhitelist;

import com.google.inject.Inject;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import xyz.gamecrash.velocitywhitelist.storage.Database;
import xyz.gamecrash.velocitywhitelist.util.FloodgateIntegration;

import java.nio.file.Path;

@Plugin(id = "velocitywhitelist", name = "VelocityWhitelist", version = "1.0-SNAPSHOT", description = "Velocity Whitelist Plugin", url = "gamecrash.xyz", authors = {"game.crash"})
public class VelocityWhitelist {
    @Getter
    private static VelocityWhitelist instance;

    @Getter
    private final Logger logger;
    @Getter
    private final ProxyServer server;
    @Getter
    private final Path dataDirectory;
    @Getter
    private final Database database;
    private final FloodgateIntegration floodgateIntegration;

    @Inject
    public VelocityWhitelist(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Enabling plugin");
        logger.info("Database file {}", dataDirectory.resolve("whitelist.db").toFile().exists() ? "found" : "not found, creating new one");
        database = new Database();
        floodgateIntegration = new FloodgateIntegration();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Plugin");
        database.connect();
    }
}
