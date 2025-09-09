package xyz.gamecrash.gatekeeper.util;

import org.jetbrains.annotations.Nullable;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.io.BufferedReader;
import java.io.InputStream;
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
            if (javaUUID != null) return javaUUID;
            return floodgateIntegration.getUUID(name);
        } catch (Exception e) {
            return null;
        }
    }

    public static @Nullable UUID returnJavaPlayerUUID(String name) throws Exception {
        URL url = new URI("https://api.mojang.com/users/profiles/minecraft/" + name).toURL();

        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setDoOutput(true);

        try {
            int responseCode = connection.getResponseCode();
            if (responseCode == HttpURLConnection.HTTP_NOT_FOUND) return null;

            InputStream inputStream = responseCode >= 400 ? connection.getErrorStream() : connection.getInputStream();
            StringBuilder stringbuilder = new StringBuilder();
            try (BufferedReader in = new BufferedReader(new InputStreamReader(inputStream))) {
                String line;
                while ((line = in.readLine()) != null) {
                    stringbuilder.append(line);
                }
            }
            String responseBody = stringbuilder.toString();

            Matcher matcher = Pattern
                .compile("\"id\"\\s*:\\s*\"([0-9a-fA-F-]+)\"")
                .matcher(responseBody);
            if (matcher.find()) return com.velocitypowered.api.util.UuidUtils.fromUndashed(matcher.group(1));
            return null;
        } finally {
            connection.disconnect();
        }
    }
}
