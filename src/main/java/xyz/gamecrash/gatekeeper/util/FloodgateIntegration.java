package xyz.gamecrash.gatekeeper.util;

import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.util.UUID;

public class FloodgateIntegration {
    private final FloodgateApi api;
    private final Logger logger;

    public FloodgateIntegration() {
        this.logger = GateKeeper.getInstance().getLogger();
        this.api = initializeFloodgateApi();
    }

    public @Nullable UUID getUUID(String playerName) {
        if (api == null) return null;

        try {
            return api.getUuidFor(playerName).join();
        } catch (Exception e) {
            return api.getUuidFor(playerName.replaceAll("_", " ")).join();
        }

    }

    public String getBedrockPlayerPrefix() {
        return api != null ? api.getPlayerPrefix() : null;
    }

    private FloodgateApi initializeFloodgateApi() {
        try {
            logger.info("Floodgate integration enabled");
            return FloodgateApi.getInstance();
        } catch (NoClassDefFoundError e) {
            logger.info("Floodgate integration not found, skipping");
            return null;
        }
    }
}
