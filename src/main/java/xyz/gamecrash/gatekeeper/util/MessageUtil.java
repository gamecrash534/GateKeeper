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
        String message = plugin.getConfigManager().getConfigMessage(path);
        if (message == null) {
            return Component.text("Missing message for key: " + String.join(".", path));
        }
        return fromString(message);
    }

    public static String fromConfigKeyRaw(String... path) {
        String message = plugin.getConfigManager().getConfigMessage(path);
        if (message == null) {
            return "Missing message for key: " + String.join(".", path);
        }
        return message;
    }

    public static Component prefixedMessage(String path) {
        Component prefix = fromConfigKey("prefix");
        Component message = fromConfigKey(path.split("\\."));
        return prefix.append(message);
    }

    public static Component prefixedMessage(String path, String... replacements) {
        Component prefix = fromConfigKey("prefix");
        String message = fromConfigKeyRaw(path.split("\\."));
        for (int i = 0; i < replacements.length; i++) {
            message = message.replace("{" + i + "}", replacements[i]);
        }

        return prefix.append(fromString(message));
    }
}
