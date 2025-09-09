package xyz.gamecrash.gatekeeper.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import xyz.gamecrash.gatekeeper.util.MessageUtil;

public class WhitelistCommand {
    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("whitelist")
            .requires(source -> source.hasPermission("whitelist"))
            .executes(ctx -> {
                ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.usage.whitelist"));
                return 1;
            })
            .then(AddCommand.build())
            .then(RemoveCommand.build())
            .then(ListCommand.build())
            .then(ClearCommand.build())
            .then(OnCommand.build())
            .then(OffCommand.build())
            .build();
    }
}
