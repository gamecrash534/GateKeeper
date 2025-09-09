package xyz.gamecrash.gatekeeper.storage;

import lombok.Getter;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Database {
    @Getter
    private Connection connection;
    private final GateKeeper plugin;

    public Database(GateKeeper plugin) {
        this.plugin = plugin;
    }

    public void connect() {
        try {
            File dbFile = new File(plugin.getDataDirectory().toFile(), "whitelist.db");
            loadJdbcDriver();
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
            executeUpdate("CREATE TABLE IF NOT EXISTS whitelist (uuid TEXT PRIMARY KEY, username TEXT);");
            plugin.getLogger().info("Connected to the database");
        } catch (Exception e) {
            plugin.getLogger().error("Could not connect to the database", e);
        }
    }

    public void disconnect() {
        closeConnection();
    }

    public boolean isWhitelisted(UUID uuid) {
        return executeQuery("SELECT 1 FROM whitelist WHERE uuid = ?", uuid.toString());
    }

    public boolean addToWhitelist(UUID uuid, String username) {
        return executeUpdate("INSERT OR IGNORE INTO whitelist (uuid, username) VALUES (?, ?)", uuid.toString(), username) > 0;
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

    public boolean setWhitelistUsername(UUID uuid, String newUsername) {
        return executeUpdate("UPDATE whitelist SET username = ? WHERE uuid = ?", newUsername, uuid.toString()) > 0;
    }

    private void loadJdbcDriver() throws ClassNotFoundException {
        Class.forName("org.sqlite.JDBC");
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

    private boolean executeQuery(String sql, String... params) {
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

    private int executeUpdate(String sql, String... params) {
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