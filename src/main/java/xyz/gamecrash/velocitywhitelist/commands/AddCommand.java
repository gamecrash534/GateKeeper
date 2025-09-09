package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.storage.Database;
import xyz.gamecrash.velocitywhitelist.util.UuidUtils;

import java.util.UUID;

public class AddCommand {
    private static final VelocityWhitelist plugin = VelocityWhitelist.getInstance();
    private static final Database db = plugin.getDatabase();
    private static final ProxyServer server = plugin.getServer();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("add")
            .requires(source -> source.hasPermission("whitelist.add"))
            .executes(ctx -> {
                ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <green>Usage: /whitelist add <username>");
                return 1;
            })
            .then(
                BrigadierCommand.requiredArgumentBuilder("username", StringArgumentType.greedyString())
                    .executes(AddCommand::execute)
            )
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        String argument = StringArgumentType.getString(ctx, "username");

        UUID uuid = UuidUtils.returnPlayerUUID(argument);
        if (uuid == null) {
            ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <red>Could not find player " + argument);
            return 1;
        }
        if (db.isWhitelisted(uuid)) {
            ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <red>" + argument + " is already whitelisted");
            return 1;
        }

        db.addToWhitelist(uuid, argument);
        ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <green>Added " + argument + " to the whitelist");

        return 1;
    }

}
