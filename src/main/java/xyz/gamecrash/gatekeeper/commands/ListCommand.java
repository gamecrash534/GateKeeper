package xyz.gamecrash.gatekeeper.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import net.kyori.adventure.text.Component;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.util.MessageUtil;
import xyz.gamecrash.gatekeeper.storage.WhitelistCache;

import java.util.List;

public class ListCommand {
    private static final GateKeeper plugin = GateKeeper.getInstance();
    private static final WhitelistCache cache = plugin.getWhitelistCache();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("list")
            .requires(source -> source.hasPermission("whitelist.list"))
            .executes(ListCommand::execute)
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        List<String> whitelist = cache.getAllUsernames();
        Component message = whitelist.isEmpty() ? MessageUtil.prefixedMessage("messages.info.list-empty") :
            MessageUtil.prefixedMessage("messages.info.list", String.join(", ", whitelist));

        ctx.getSource().sendMessage(message);
        return 1;
    }
}
