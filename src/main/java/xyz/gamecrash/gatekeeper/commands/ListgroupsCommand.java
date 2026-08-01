package xyz.gamecrash.gatekeeper.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.storage.Database;
import xyz.gamecrash.gatekeeper.util.LuckPermsIntegration;
import xyz.gamecrash.gatekeeper.util.MessageUtil;

import java.util.Set;

public class ListgroupsCommand {
    private static final GateKeeper plugin = GateKeeper.getInstance();
    private static final Database db = plugin.getDatabase();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("listgroups")
            .requires(source -> source.hasPermission("whitelist.listgroups"))
            .executes(ListgroupsCommand::execute)
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        if (!LuckPermsIntegration.present()) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.lp-integration-missing"));
            return 1;
        }

        Set<String> whitelist = db.getWhitelistedGroups();
        Component message = whitelist.isEmpty() ? MessageUtil.prefixedMessage("messages.info.list-empty") :
            MessageUtil.prefixedMessage("messages.info.listgroups", String.join(", ", whitelist));

        ctx.getSource().sendMessage(message);
        return 1;
    }
}
