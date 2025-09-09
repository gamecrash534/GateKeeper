package xyz.gamecrash.gatekeeper.util;

import org.jetbrains.annotations.Nullable;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URL;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UuidUtils {
    private static final FloodgateIntegration floodgateIntegration = GateKeeper.getInstance().getFloodgateIntegration();

    public static @Nullable UUID returnPlayerUUID(String name) {
        try {
            UUID javaUUID = returnJavaPlayerUUID(name);
            return javaUUID != null ? javaUUID : floodgateIntegration.getUUID(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable UUID returnJavaPlayerUUID(String name) throws Exception {
        HttpURLConnection connection = createConnection(name);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) return null;

            String responseBody = readResponse(connection, responseCode);
            return getUUIDFromResponse(responseBody);
        } finally {
            connection.disconnect();
        }
    }

    private static HttpURLConnection createConnection(String name) throws Exception {
        URL url = new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);
        return connection;
    }

    private static String readResponse(HttpURLConnection connection, int responseCode) throws Exception {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
                responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream()))) {
            StringBuilder response = new StringBuilder();
            String line;
            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            return response.toString();
        }
    }

    private static UUID getUUIDFromResponse(String responseBody) {
        Matcher matcher = Pattern.compile("\"id\"\\s*:\\s*\"([0-9a-fA-F-]+)\"").matcher(responseBody);
        return matcher.find() ? com.velocitypowered.api.util.UuidUtils.fromUndashed(matcher.group(1)) : null;
    }
}