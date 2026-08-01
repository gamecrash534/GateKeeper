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

            map.putIfAbsent("groups", "");

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
        map.compute("groups", (k, v) -> {
            Set<String> groups = parseGroups(v);
            groups.add(group);
            return serializeGroups(groups);
        });
        return true;
    }

    public boolean removeGroup(String group) {
        map.compute("groups", (k, v) -> {
            Set<String> groups = parseGroups(v);
            groups.remove(group);
            return serializeGroups(groups);
        });
        return true;
    }

    public boolean isWhitelisted(UUID uuid) {
        return map.containsKey(uuid.toString());
    }

    public boolean isGroupWhitelisted(String group) {
        return getWhitelistedGroups().contains(group);
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
        return parseGroups(map.get("groups"));
    }

    public Collection<String> getWhitelistUsernames() {
        return map.entrySet().stream()
            .filter(e -> !e.getKey().equals("groups"))
            .flatMap(e -> e.getValue().lines())
            .collect(Collectors.toSet());
    }

    private Set<String> parseGroups(String rawGroups) {
        if (rawGroups == null || rawGroups.isBlank()) return new LinkedHashSet<>();

        return Arrays.stream(rawGroups.split(";"))
            .filter(group -> !group.isBlank())
            .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private String serializeGroups(Set<String> groups) {
        return String.join(";", groups);
    }
}
