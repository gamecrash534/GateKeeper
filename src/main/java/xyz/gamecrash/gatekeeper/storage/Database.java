package xyz.gamecrash.gatekeeper.storage;

import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;

public class Database {
    private MVStore store;
    private MVMap<String, String> map;
    private final GateKeeper plugin;

    public Database(GateKeeper plugin) {
        this.plugin = plugin;
    }

    public void connect() throws RuntimeException {
        try {
            Path dbPath = plugin.getDataDirectory().resolve("database");

            store = new MVStore.Builder()
                .fileName(dbPath.toString())
                .compress()
                .open();

            map = store.openMap("whitelist");

            plugin.getLogger().info("Connected to the database");
        } catch (Exception e) {
            plugin.getLogger().error("Could not connect to the database", e);
            throw new RuntimeException();
        }
    }

    public void disconnect() {
        store.close();
    }

    public boolean addGroup(String group) {
        map.compute("groups", (k, v) -> v + group + ";");
        return true;
    }

    public boolean removeGroup(String group) {
        map.compute("groups", (k, v) -> v.replace(group + ";", ""));
        return true;
    }

    public boolean isWhitelisted(UUID uuid) {
        return map.containsKey(uuid.toString());
    }

    public boolean isGroupWhitelisted(String group) {
        return map.get("groups").contains(group);
    }

    public boolean addToWhitelist(UUID uuid, String username) {
        return map.put(uuid.toString(), username) == null;
    }

    public boolean removeFromWhitelist(UUID uuid) {
        return map.remove(uuid.toString()) != null;
    }

    public void clearWhitelist() {
        map.clear();
    }

    public String getWhitelistUsername(UUID uuid) {
        return map.get(uuid.toString());
    }

    public Set<String> getWhitelistedGroups() {
        return Arrays.stream(map.get("groups")
            .split(";"))
            .collect(Collectors.toSet());
    }

    public Collection<String> getWhitelistUsernames() {
        return map.values();
    }

    public Map<UUID, String> getAllWhitelistEntries() {
        return map.entrySet().stream()
            .collect(
                Collectors.toMap(e -> UUID.fromString(e.getKey()), Map.Entry::getValue)
            );
    }

    public boolean setWhitelistUsername(UUID uuid, String newUsername) {
        return map.put(uuid.toString(), newUsername) != null;
    }

    public boolean isConnected() {
        return !store.isClosed();
    }
}
