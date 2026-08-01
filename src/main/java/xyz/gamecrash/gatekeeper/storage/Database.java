package xyz.gamecrash.gatekeeper.storage;

import lombok.Getter;
import org.intellij.lang.annotations.Language;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.stream.Collectors;

public class Database {
    @Getter
    private Connection connection;
    private final GateKeeper plugin;

    public Database(GateKeeper plugin) {
        this.plugin = plugin;
    }

    public void connect() throws RuntimeException {
        try {
            File dbFile = new File(plugin.getDataDirectory().toFile(), "whitelist");
            loadJdbcDriver();
            connection = DriverManager.getConnection("jdbc:h2:" + dbFile.getAbsolutePath());
            executeUpdate("CREATE TABLE IF NOT EXISTS whitelist (uuid TEXT PRIMARY KEY, username TEXT);");
            executeUpdate("MERGE INTO whitelist VALUES ( ?, ? )", "groups", "");
            plugin.getLogger().info("Connected to the database");
        } catch (Exception e) {
            plugin.getLogger().error("Could not connect to the database", e);
            throw new RuntimeException();
        }
    }

    public void disconnect() {
        closeConnection();
    }

    public boolean addGroup(String group) {
        return executeUpdate("UPDATE whitelist SET username = username || CONCAT(';', ?) WHERE (uuid = 'groups');", group) == 1;
    }

    public boolean removeGroup(String group) {
        return executeUpdate("UPDATE whitelist SET username = REPLACE(username, CONCAT(';', ?)) WHERE (uuid = 'groups');", group) == 1;
    }

    public boolean isWhitelisted(UUID uuid) {
        return executeQuery("SELECT 1 FROM whitelist WHERE uuid = ?", uuid.toString());
    }

    public boolean isGroupWhitelisted(String group) {
        return executeQuery("SELECT 1 FROM whitelist WHERE uuid = 'groups' AND username LIKE %?%", group);
    }

    public boolean addToWhitelist(UUID uuid, String username) {
        return executeUpdate("MERGE INTO whitelist (uuid, username) KEY(uuid) VALUES (?, ?)", uuid.toString(), username) > 0;
    }

    public boolean removeFromWhitelist(UUID uuid) {
        return executeUpdate("DELETE FROM whitelist WHERE uuid = ?", uuid.toString()) > 0;
    }

    public void clearWhitelist() {
        executeUpdate("DELETE FROM whitelist");
    }

    public String getWhitelistUsername(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT username FROM whitelist WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet resultSet = statement.executeQuery()) {
                if (resultSet.next()) {
                    return resultSet.getString("username");
                } else {
                    return null;
                }
            }
        } catch (SQLException e) {
            plugin.getLogger().error("Could not retrieve username for UUID: " + uuid, e);
            return null;
        }
    }

    public Set<String> getWhitelistedGroups() {
        try (PreparedStatement stmt = connection.prepareStatement("SELECT username FROM whitelist WHERE uuid = 'groups'")) {
            ResultSet resultSet = stmt.executeQuery();

            return Arrays.stream(resultSet.getString("username")
                .split(";"))
                .collect(Collectors.toSet());
        } catch (SQLException e) {
            plugin.getLogger().error("Could not retrieve whitelisted groups", e);
            return Collections.emptySet();
        }
    }

    public List<String> getWhitelistUsernames() {
        try (PreparedStatement statement = connection.prepareStatement("SELECT username FROM whitelist");
             ResultSet resultSet = statement.executeQuery()) {
            List<String> whitelist = new ArrayList<>();
            while (resultSet.next()) {
                whitelist.add(resultSet.getString("username"));
            }
            return whitelist;
        } catch (SQLException e) {
            plugin.getLogger().error("Could not retrieve whitelist", e);
            return Collections.emptyList();
        }
    }

    public Map<UUID, String> getAllWhitelistEntries() {
        Map<UUID, String> entries = new HashMap<>();
        try (PreparedStatement statement = connection.prepareStatement("SELECT uuid, username FROM whitelist");
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                if (resultSet.getString("uuid").equals("groups")) continue;

                UUID uuid = UUID.fromString(resultSet.getString("uuid"));
                String username = resultSet.getString("username");
                entries.put(uuid, username);
            }
        } catch (SQLException e) {
            plugin.getLogger().error("Could not retrieve all whitelist entries", e);
        }
        return entries;
    }

    public boolean setWhitelistUsername(UUID uuid, String newUsername) {
        return executeUpdate("UPDATE whitelist SET username = ? WHERE uuid = ?", newUsername, uuid.toString()) > 0;
    }

    public boolean isConnected() {
        try {
            return connection != null && !connection.isClosed();
        } catch (SQLException e) {
            return false;
        }
    }

    private void loadJdbcDriver() throws ClassNotFoundException {
        Class.forName("org.h2.Driver");
    }

    private void closeConnection() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Disconnected from the database");
            } catch (SQLException e) {
                plugin.getLogger().error("Could not disconnect from the database", e);
            }
        }
    }

    private boolean executeQuery(@Language("H2") String sql, String... params) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, params);
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().error("Query execution failed", e);
            return false;
        }
    }

    private int executeUpdate(@Language("H2") String sql, String... params) {
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            setParameters(statement, params);
            return statement.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().error("Update execution failed", e);
            return 0;
        }
    }

    private void setParameters(PreparedStatement statement, String... params) throws SQLException {
        for (int i = 0; i < params.length; i++) {
            statement.setString(i + 1, params[i]);
        }
    }
}
