package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.storage.Database;
import xyz.gamecrash.velocitywhitelist.util.MessageUtil;
import xyz.gamecrash.velocitywhitelist.util.UuidUtils;

import java.util.UUID;

public class RemoveCommand {
    private static final VelocityWhitelist plugin = VelocityWhitelist.getInstance();
    private static final Database db = plugin.getDatabase();
    private static final ProxyServer server = plugin.getServer();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("remove")
            .requires(source -> source.hasPermission("whitelist.remove"))
            .executes(ctx -> {
                ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.usage.remove"));
                return 1;
            })
            .then(
                BrigadierCommand.requiredArgumentBuilder("username", StringArgumentType.greedyString())
                    .executes(RemoveCommand::execute)
            )
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        String argument = StringArgumentType.getString(ctx, "username");

        UUID uuid = UuidUtils.returnPlayerUUID(argument);
        if (uuid == null) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.player-not-found", argument));
            return 1;
        }
        if (!db.isWhitelisted(uuid)) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.not-whitelisted", argument));
            return 1;
        }

        db.removeFromWhitelist(uuid);
        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.removed-from-whitelist", argument));

        return 1;
    }
}
