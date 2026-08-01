package xyz.gamecrash.gatekeeper.util;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.group.Group;
import net.luckperms.api.model.user.User;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import xyz.gamecrash.gatekeeper.GateKeeper;

import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class LuckPermsIntegration {
    private static @Nullable LuckPerms api;
    private final Logger logger;

    public LuckPermsIntegration() {
        this.logger = GateKeeper.getInstance().getLogger();
        api = getApi();
    }

    public static boolean isInGroup(UUID uuid, Set<String> groups) {
        if (api == null) return false;

        User user = api.getUserManager().getUser(uuid);
        return !Collections.disjoint(user.getInheritedGroups(user.getQueryOptions()).stream().map(Group::getName).toList(), groups);
    }

    public static boolean present() {
        return api != null;
    }

    private @Nullable LuckPerms getApi() {
        try {
            logger.info("Enabling LuckPerms integration");
            return LuckPermsProvider.get();
        } catch (IllegalStateException | NoClassDefFoundError e) {
            logger.info("LuckPerms integration not present, skipping");
            return null;
        }
    }
}
