package xyz.gamecrash.gatekeeper.util;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import xyz.gamecrash.gatekeeper.GateKeeper;

public class MessageUtil {
    private static final GateKeeper plugin = GateKeeper.getInstance();

    public static Component fromString(String message) {
        return MiniMessage.miniMessage().deserialize(message);
    }

    public static Component fromConfigKey(String... path) {
        String message = getConfigMessage(path);
        return message != null ? fromString(message) : missingMessage(path);
    }

    public static String fromConfigKeyRaw(String... path) {
        String message = getConfigMessage(path);
        return message != null ? message : "Missing message for key: " + String.join(".", path);
    }

    public static Component prefixedMessage(String path) {
        return fromConfigKey("prefix").append(fromConfigKey(path.split("\\.")));
    }

    public static Component prefixedMessage(String path, String... replacements) {
        String message = fromConfigKeyRaw(path.split("\\."));
        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", replacements[i]);
        }
        return fromConfigKey("prefix").append(fromString(message));
    }

    private static String getConfigMessage(String... path) {
        return plugin.getConfigManager().getConfigMessage(path);
    }

    private static Component missingMessage(String... path) {
        return Component.text("Missing message for key: " + String.join(".", path));
    }
}