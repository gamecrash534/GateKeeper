package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;

public class WhitelistCommand {
    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("whitelist")
            .requires(source -> source.hasPermission("whitelist"))
            .executes(ctx -> {
                ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <green>Usage: /whitelist [add|remove] <username>");
                return 1;
            })
            .then(AddCommand.build())
            .then(RemoveCommand.build())
            .then(ListCommand.build())
            .then(ClearCommand.build())
            .build();
    }
}
