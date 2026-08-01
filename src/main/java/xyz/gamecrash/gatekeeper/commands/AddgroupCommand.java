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

public class AddgroupCommand {
    private static final GateKeeper plugin = GateKeeper.getInstance();
    private static final Database db = plugin.getDatabase();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("addgroup")
            .requires(source -> source.hasPermission("whitelist.addgroup"))
            .executes(ctx -> {
                ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.usage.addgroup"));
                return 1;
            })
            .then(
                BrigadierCommand.requiredArgumentBuilder("group", StringArgumentType.greedyString())
                    .executes(AddgroupCommand::execute)
            )
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        if (!LuckPermsIntegration.present()) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.lp-integration-missing"));
            return 1;
        }

        String argument = StringArgumentType.getString(ctx, "group");
        if (db.isGroupWhitelisted(argument)) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.group-already-whitelisted", argument));
            return 1;
        }

        db.addGroup(argument);
        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.added-group-to-whitelist", argument));

        return 1;
    }
}
