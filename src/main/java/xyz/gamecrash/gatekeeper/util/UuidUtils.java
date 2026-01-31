package xyz.gamecrash.gatekeeper.util;

import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
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
    private static final Logger logger = GateKeeper.getInstance().getLogger();

    private static final Pattern DASHED_UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");
    private static final Pattern UNDASHED_UUID_PATTERN = Pattern.compile("^[0-9a-fA-F]{32}$");

    public static @Nullable UUID returnPlayerUUID(String input) {
        UUID parsedUuid = parseUuid(input);
        if (parsedUuid != null) return parsedUuid;

        try {
            if (input.startsWith(floodgateIntegration.getBedrockPlayerPrefix())) return returnBedrockPlayerUUID(input);
            else return returnJavaPlayerUUID(input);
        } catch (Exception e) {
            logger.warn("Exception caught whilst trying to fetch UUID: '{}' \nIgnoring. No UUID will be returned instead - so no player will be found.", e.getMessage());
            return null;
        }
    }

    public static @Nullable UUID returnJavaPlayerUUID(String name) throws Exception {
        HttpURLConnection connection = createConnection("https://api.mojang.com/users/profiles/minecraft/" + name);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) return null;

            String responseBody = readResponse(connection, responseCode);
            return getUUIDFromResponse(responseBody);
        } finally {
            connection.disconnect();
        }
    }

    public static @Nullable UUID returnBedrockPlayerUUID(String name) throws Exception {
        String bedrockPrefix = floodgateIntegration.getBedrockPlayerPrefix();
        if (bedrockPrefix != null && name.startsWith(bedrockPrefix)) name = name.substring(bedrockPrefix.length());

        return floodgateIntegration.getUUID(name);
    }

    private static @Nullable UUID parseUuid(String input) {
        if (DASHED_UUID_PATTERN.matcher(input).matches()) {
            try {
                return UUID.fromString(input);
            } catch (IllegalArgumentException e) {
                return null;
            }
        } else if (UNDASHED_UUID_PATTERN.matcher(input).matches()) return com.velocitypowered.api.util.UuidUtils.fromUndashed(input);

        return null;
    }

    private static HttpURLConnection createConnection(String uri) throws Exception {
        URL url = new URI(uri).toURL();
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