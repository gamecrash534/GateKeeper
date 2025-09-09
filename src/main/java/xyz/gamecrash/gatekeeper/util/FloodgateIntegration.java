package xyz.gamecrash.gatekeeper.util;

import org.geysermc.floodgate.api.FloodgateApi;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.util.UUID;
import java.util.concurrent.CompletableFuture;

public class FloodgateIntegration {
    private FloodgateApi api;
    private Logger logger = GateKeeper.getInstance().getLogger();

    public FloodgateIntegration() {
        try {
            api = FloodgateApi.getInstance();
            logger.info("Floodgate integration enabled");
        } catch (NoClassDefFoundError e) {
            api = null;
            logger.info("Floodgate integration not found, skipping");
        }
    }

    public @Nullable UUID getUUID(String playerName) {
        if (api == null) return null;

        if (playerName.startsWith(getBedrockPlayerPrefix()))
            playerName = playerName.substring(getBedrockPlayerPrefix().length());

        CompletableFuture<UUID> future = api.getUuidFor(playerName);
        return future.join();
    }

    public String getBedrockPlayerPrefix() {
        if (api == null) return null;
        return api.getPlayerPrefix();
    }
}
