package xyz.gamecrash.gatekeeper.storage;

import lombok.Getter;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.util.LuckPermsIntegration;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class WhitelistCache {
    private final GateKeeper plugin;

    private final Map<UUID, String> uuidToUsernameCache = new ConcurrentHashMap<>();
    private final Set<UUID> whitelistedUuids = ConcurrentHashMap.newKeySet();
    private final Set<String> whitelistedGroups = ConcurrentHashMap.newKeySet();
    private long lastCacheUpdate = 0;
    private long cacheTtl;
    private final ScheduledExecutorService cacheRefreshExecutor;
    @Getter private boolean cacheEnabled;
    private final Database db;

    public WhitelistCache(GateKeeper plugin) {
        this.plugin = plugin;
        cacheRefreshExecutor = Executors.newSingleThreadScheduledExecutor(r -> {
            Thread t = new Thread(r, "WhitelistCache-Refresh");
            t.setDaemon(true);
            return t;
        });
        db = plugin.getDatabase();
    }

    public void initializeCache() {
        cacheEnabled = plugin.getConfigManager().isCacheEnabled();
        if (!cacheEnabled) return;

        plugin.getLogger().info("Initializing whitelist cache...");

        cacheTtl = plugin.getConfigManager().getCacheTtlMinutes() * 60 * 1000L;
        refreshCache();
        scheduleAutoRefresh();

        plugin.getLogger().info("Whitelist cache initialized with {} entries", whitelistedUuids.size());
    }

    public void refreshCache() {
        if (!cacheEnabled) return;

        if (!db.isConnected()) {
            plugin.getLogger().warn("Database not connected, skipping cache refresh");
            return;
        }

        try {
            Map<UUID, String> freshData = db.getAllWhitelistEntries();

            clearCache();

            for (Map.Entry<UUID, String> entry : freshData.entrySet()) {
                UUID uuid = entry.getKey();
                String username = entry.getValue();

                uuidToUsernameCache.put(uuid, username);
                whitelistedUuids.add(uuid);
            }
            
            whitelistedGroups.addAll(db.getWhitelistedGroups());

            lastCacheUpdate = System.currentTimeMillis();
            plugin.getLogger().debug("Cache refreshed with {} entries", whitelistedUuids.size());

        } catch (Exception e) {
            plugin.getLogger().error("Failed to refresh cache", e);
        }
    }

    public boolean isWhitelisted(UUID uuid) {
        if (!cacheEnabled) return db.isWhitelisted(uuid);

        if (isCacheStale()) refreshCache();

        return whitelistedUuids.contains(uuid) || LuckPermsIntegration.isInGroup(uuid, whitelistedGroups);
    }

    public boolean isGroupWhitelisted(String group) {
        if (!cacheEnabled) return db.isGroupWhitelisted(group);

        if (isCacheStale()) refreshCache();

        return whitelistedGroups.contains(group);
    }

    public String getUsername(UUID uuid) {
        if (!cacheEnabled) {
            return db.getWhitelistUsername(uuid);
        }

        if (isCacheStale()) refreshCache();

        return uuidToUsernameCache.get(uuid);
    }

    public boolean addGroup(String group) {
        boolean success = db.addGroup(group);

        if (success && cacheEnabled) {
            whitelistedGroups.add(group);
            plugin.getLogger().debug("Added group {} to cache", group);
        }

        return success;
    }

    public boolean removeGroup(String group) {
        boolean success = db.removeGroup(group);

        if (success && cacheEnabled) {
            whitelistedGroups.remove(group);
            plugin.getLogger().debug("Removed group {} to cache", group);
        }

        return success;
    }

    public boolean addToWhitelist(UUID uuid, String username) {
        boolean success = db.addToWhitelist(uuid, username);

        if (success && cacheEnabled) {
            uuidToUsernameCache.put(uuid, username);
            whitelistedUuids.add(uuid);
            plugin.getLogger().debug("Added {} ({}) to cache", username, uuid);
        }

        return success;
    }

    public boolean removeFromWhitelist(UUID uuid) {
        String username = cacheEnabled ? uuidToUsernameCache.get(uuid) : null;
        boolean success = db.removeFromWhitelist(uuid);

        if (success && cacheEnabled) {
            uuidToUsernameCache.remove(uuid);
            whitelistedUuids.remove(uuid);
            plugin.getLogger().debug("Removed {} ({}) from cache", username, uuid);
        }

        return success;
    }

    public void clearWhitelist() {
        db.clearWhitelist();

        if (cacheEnabled) {
            clearCache();
            plugin.getLogger().debug("Cleared whitelist cache");
        }
    }

    public boolean updateUsername(UUID uuid, String newUsername) {
        String oldUsername = cacheEnabled ? uuidToUsernameCache.get(uuid) : null;
        boolean success = db.setWhitelistUsername(uuid, newUsername);

        if (success && cacheEnabled) {
            uuidToUsernameCache.put(uuid, newUsername);

            plugin.getLogger().debug("Updated username in cache: {} -> {} ({})", oldUsername, newUsername, uuid);
        }

        return success;
    }

    public List<String> getAllUsernames() {
        if (!cacheEnabled) {
            return db.getWhitelistUsernames();
        }

        if (isCacheStale()) refreshCache();

        return new ArrayList<>(uuidToUsernameCache.values());
    }

    private boolean isCacheStale() {
        return (System.currentTimeMillis() - lastCacheUpdate) > cacheTtl;
    }

    private void clearCache() {
        uuidToUsernameCache.clear();
        whitelistedUuids.clear();
    }

    private void scheduleAutoRefresh() {
        if (!cacheEnabled) return;

        long refreshIntervalMinutes = plugin.getConfigManager().getCacheRefreshIntervalMinutes();

        cacheRefreshExecutor.scheduleAtFixedRate(
            this::refreshCache,
            refreshIntervalMinutes,
            refreshIntervalMinutes,
            TimeUnit.MINUTES
        );

        plugin.getLogger().info("Scheduled automatic cache refresh every {} minutes", refreshIntervalMinutes);
    }

    public void shutdown() {
        if (cacheRefreshExecutor != null && !cacheRefreshExecutor.isShutdown()) {
            cacheRefreshExecutor.shutdown();
            try {
                if (!cacheRefreshExecutor.awaitTermination(5, TimeUnit.SECONDS)) {
                    cacheRefreshExecutor.shutdownNow();
                }
            } catch (InterruptedException e) {
                cacheRefreshExecutor.shutdownNow();
                Thread.currentThread().interrupt();
            }
        }
        clearCache();
        plugin.getLogger().info("Whitelist cache shutdown completed");
    }
}
