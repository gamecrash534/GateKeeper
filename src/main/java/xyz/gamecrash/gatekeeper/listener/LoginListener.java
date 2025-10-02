package xyz.gamecrash.gatekeeper.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Setter;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.util.MessageUtil;
import xyz.gamecrash.gatekeeper.storage.WhitelistCache;

public class LoginListener {
    private final GateKeeper plugin;
    private final WhitelistCache cache;
    @Setter private boolean isEnabled;

    public LoginListener(GateKeeper plugin) {
        this.plugin = plugin;
        this.cache = plugin.getWhitelistCache();
        this.isEnabled = plugin.getConfigManager().isWhitelistEnabled();
    }

    @Subscribe
    public void onLogin(LoginEvent e) {
        if (!isEnabled) return;

        Player player = e.getPlayer();

        if (!cache.isWhitelisted(player.getUniqueId())) {
            e.setResult(ResultedEvent.ComponentResult.denied(
                MessageUtil.fromConfigKey("messages", "disconnect-reason")
            ));
            plugin.getLogger().info("Player {} tried to join but is not whitelisted. UUID: {}", player.getUsername(), player.getUniqueId());
        } else {
            updateUsername(player);
        }
    }

    private void updateUsername(Player player) {
        String currentUsername = player.getUsername();
        String storedUsername = cache.getUsername(player.getUniqueId());

        if (storedUsername == null || !storedUsername.equals(currentUsername)) {
            cache.updateUsername(player.getUniqueId(), currentUsername);
            plugin.getLogger().info("Updated username for UUID {}: {} -> {}",
                player.getUniqueId(), storedUsername, currentUsername);
        }
    }
}
