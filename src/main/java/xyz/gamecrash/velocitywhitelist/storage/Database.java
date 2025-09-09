package xyz.gamecrash.velocitywhitelist.storage;

import lombok.Getter;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;

import java.io.File;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class Database {
    @Getter
    private Connection connection;
    private final VelocityWhitelist plugin = VelocityWhitelist.getInstance();

    public void connect() {
        try {
            File dbFile = new File(plugin.getDataDirectory().toFile(), "whitelist.db");


            try {
                Class.forName("org.sqlite.JDBC");
            } catch (ClassNotFoundException e) {
                plugin.getLogger().error("SQLite JDBC driver not found");
                return;
            }
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());

            String sql = "CREATE TABLE IF NOT EXISTS whitelist (uuid TEXT PRIMARY KEY, username TEXT);";
            connection.createStatement().execute(sql);

            plugin.getLogger().info("Connected to the database");
        } catch (Exception e) {
            plugin.getLogger().error("Could not connect to the database", e);
        }
    }

    public void disconnect() {
        if (connection != null) {
            try {
                connection.close();
                plugin.getLogger().info("Disconnected from the database");
            } catch (SQLException e) {
                plugin.getLogger().error("Could not disconnect from the database", e);
            }
        }
    }

    public boolean isWhitelisted(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("SELECT 1 FROM whitelist WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            try (ResultSet result = statement.executeQuery()) {
                return result.next();
            }
        } catch (SQLException e) {
            plugin.getLogger().error("Could not check if UUID is whitelisted", e);
            return false;
        }
    }

    public boolean addToWhitelist(UUID uuid, String username) {
        try (PreparedStatement statement = connection.prepareStatement("INSERT OR IGNORE INTO WHITELIST (uuid, username) VALUES (?, ?)")) {
            statement.setString(1, uuid.toString());
            statement.setString(2, username);
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().error("Could not add UUID to whitelist", e);
            return false;
        }
    }

    public boolean removeFromWhitelist(UUID uuid) {
        try (PreparedStatement statement = connection.prepareStatement("DELETE FROM whitelist WHERE uuid = ?")) {
            statement.setString(1, uuid.toString());
            return statement.executeUpdate() > 0;
        } catch (SQLException e) {
            plugin.getLogger().error("Could not remove UUID from whitelist", e);
            return false;
        }
    }

    public void clearWhitelist() {
        try (Statement statement = connection.createStatement()) {
            statement.executeUpdate("DELETE FROM whitelist");
        } catch (SQLException e) {
            plugin.getLogger().error("Could not clear whitelist", e);
        }
    }

    public List<String> getWhitelist() {
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


}
