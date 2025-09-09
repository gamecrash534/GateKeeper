package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.storage.Database;

import java.util.List;

public class ListCommand {
    private static final VelocityWhitelist plugin = VelocityWhitelist.getInstance();
    private static final Database db = plugin.getDatabase();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("list")
            .requires(source -> source.hasPermission("whitelist.list"))
            .executes(ListCommand::execute)
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        List<String> whitelist = db.getWhitelistUsernames();
        String message = "<dark_gray>[<yellow>Whitelist<dark_gray>] " + (whitelist.isEmpty() ? "<gray>The whitelist is currently empty." :
            "<green>Whitelisted players: <white>" + String.join(", ", whitelist));

        ctx.getSource().sendRichMessage(message);
        return 1;
    }
}
