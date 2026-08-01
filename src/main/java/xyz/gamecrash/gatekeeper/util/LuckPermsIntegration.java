package xyz.gamecrash.gatekeeper.util;

import com.velocitypowered.api.proxy.Player;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.gamecrash.gatekeeper.GateKeeper;

public class LuckPermsIntegration {
    private final @Nullable LuckPerms api;
    private final Logger logger;

    public LuckPermsIntegration() {
        this.logger = GateKeeper.getInstance().getLogger();
        this.api = getApi();
    }

    public boolean isInGroup(Player plr, String group) {
        User user = api.getUserManager().getUser(plr.getUniqueId());
        return user.getInheritedGroups(user.getQueryOptions()).stream().anyMatch(g -> g.getName().equals(group));
    }

    private @Nullable LuckPerms getApi() {
        try {
            logger.info("Trying to obtain LuckPerms API instance");
            return LuckPermsProvider.get();
        } catch (IllegalStateException e) {
            logger.info("LuckPerms integration not present, skipping");
            return null;
        }
    }
}
