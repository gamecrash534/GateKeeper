package xyz.gamecrash.gatekeeper.commands;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.storage.Database;
import xyz.gamecrash.gatekeeper.util.LuckPermsIntegration;
import xyz.gamecrash.gatekeeper.util.MessageUtil;

public class RemovegroupCommand {
    private static final GateKeeper plugin = GateKeeper.getInstance();
    private static final Database db = plugin.getDatabase();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("removegroup")
            .requires(source -> source.hasPermission("whitelist.removegroup"))
            .executes(ctx -> {
                ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.usage.removegroup"));
                return 1;
            })
            .then(
                BrigadierCommand.requiredArgumentBuilder("username", StringArgumentType.greedyString())
                    .executes(RemovegroupCommand::execute)
            )
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        if (!LuckPermsIntegration.present()) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.lp-integration-missing"));
            return 1;
        }

        String argument = StringArgumentType.getString(ctx, "username");
        if (!db.isGroupWhitelisted(argument)) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.group-not-whitelisted", argument));
            return 1;
        }

        db.removeGroup(argument);
        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.removed-group-from-whitelist", argument));

        return 1;
    }
}
