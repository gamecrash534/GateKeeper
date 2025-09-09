package xyz.gamecrash.gatekeeper;

import com.google.inject.Inject;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandManager;
import com.velocitypowered.api.command.CommandMeta;
import com.velocitypowered.api.event.proxy.ProxyInitializeEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.proxy.ProxyShutdownEvent;
import com.velocitypowered.api.plugin.Plugin;
import com.velocitypowered.api.plugin.annotation.DataDirectory;
import com.velocitypowered.api.proxy.ProxyServer;
import lombok.Getter;
import org.slf4j.Logger;
import xyz.gamecrash.gatekeeper.commands.WhitelistCommand;
import xyz.gamecrash.gatekeeper.config.ConfigManager;
import xyz.gamecrash.gatekeeper.listener.LoginListener;
import xyz.gamecrash.gatekeeper.storage.Database;
import xyz.gamecrash.gatekeeper.util.FloodgateIntegration;

import java.io.IOException;
import java.nio.file.Path;

@Plugin(id = "gatekeeper", name = "GateKeeper", version = "1.0-SNAPSHOT", description = "Velocity Whitelist Plugin", authors = {"game.crash"})
public class GateKeeper {
    @Getter private static GateKeeper instance;

    @Getter private final Logger logger;
    @Getter private final ProxyServer server;
    @Getter private final Path dataDirectory;
    @Getter private final ConfigManager configManager;
    @Getter private final Database database;
    @Getter private final FloodgateIntegration floodgateIntegration;
    @Getter private LoginListener loginListener;

    @Inject
    public GateKeeper(ProxyServer server, Logger logger, @DataDirectory Path dataDirectory) {
        instance = this;

        this.server = server;
        this.logger = logger;
        this.dataDirectory = dataDirectory;

        logger.info("Enabling plugin");

        if (!dataDirectory.toFile().exists()) {
           dataDirectory.toFile().mkdirs();
            logger.info("Created missing plugin data directory");
        }

        configManager = new ConfigManager(this);

        logger.info("Database file {}", dataDirectory.resolve("whitelist.db").toFile().exists() ? "found" : "not found, creating new one");
        try {
            dataDirectory.resolve("whitelist.db").toFile().createNewFile();
        } catch (IOException e) {
            logger.error("Could not create whitelist.db: " + e.getMessage());
        }
        database = new Database(this);
        floodgateIntegration = new FloodgateIntegration();
    }

    @Subscribe
    public void onProxyInitialization(ProxyInitializeEvent event) {
        logger.info("Initializing Plugin");

        configManager.loadConfiguration();
        database.connect();

        CommandManager commandManager = server.getCommandManager();
        CommandMeta meta = commandManager.metaBuilder("vwhitelist")
            .aliases("velocitywhitelist", "gatekeeper")
            .plugin(this)
            .build();
        commandManager.register(meta, new BrigadierCommand(WhitelistCommand.build()));
        logger.info("Commands registered");

        loginListener = new LoginListener(this);
        server.getEventManager().register(this, loginListener); // currently the plugin would crash otherwise, as it tries to retrieve a config value
        logger.info("Listeners registered");
    }

    @Subscribe
    public void onProxyShutdown(ProxyShutdownEvent event) {
        logger.info("Shutting down Plugin");
        database.disconnect();
        logger.info("That was it lol");
    }
}
