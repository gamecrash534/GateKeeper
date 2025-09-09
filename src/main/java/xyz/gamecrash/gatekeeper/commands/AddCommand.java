package xyz.gamecrash.gatekeeper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.storage.Database;
import xyz.gamecrash.gatekeeper.util.MessageUtil;
import xyz.gamecrash.gatekeeper.util.UuidUtils;

import java.util.UUID;

public class AddCommand {
    private static final GateKeeper plugin = GateKeeper.getInstance();
    private static final Database db = plugin.getDatabase();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("add")
            .requires(source -> source.hasPermission("whitelist.add"))
            .executes(ctx -> {
                ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.usage.add"));
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
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.player-not-found", argument));
            return 1;
        }
        if (db.isWhitelisted(uuid)) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.already-whitelisted", argument));
            return 1;
        }

        db.addToWhitelist(uuid, argument);
        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.added-to-whitelist", argument));

        return 1;
    }

}
