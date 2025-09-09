package xyz.gamecrash.velocitywhitelist.listener;

import com.velocitypowered.api.event.ResultedEvent;
import com.velocitypowered.api.event.Subscribe;
import com.velocitypowered.api.event.connection.LoginEvent;
import com.velocitypowered.api.proxy.Player;
import lombok.Setter;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.storage.Database;
import xyz.gamecrash.velocitywhitelist.util.MessageUtil;

public class LoginListener {
    private final VelocityWhitelist plugin;
    private final Database db;
    @Setter private boolean isEnabled;

    public LoginListener(VelocityWhitelist plugin) {
        this.plugin = plugin;
        this.db = plugin.getDatabase();
        this.isEnabled = plugin.getConfigManager().isWhitelistEnabled();
    }

    @Subscribe
    public void onLogin(LoginEvent e) {
        if (!isEnabled) return;

        Player player = e.getPlayer();

        if (!db.isWhitelisted(player.getUniqueId())) {
            e.setResult(ResultedEvent.ComponentResult.denied(
                MessageUtil.fromConfigKey("messages", "disconnect-reason")
            ));
            plugin.getLogger().info("Player {} tried to join but is not whitelisted. UUID: {}", player.getUsername(), player.getUniqueId());
        }
    }

}
