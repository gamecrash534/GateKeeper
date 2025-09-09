package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.config.ConfigManager;
import xyz.gamecrash.velocitywhitelist.util.MessageUtil;

public class OnCommand {
    private static final VelocityWhitelist plugin = VelocityWhitelist.getInstance();
    private static final ConfigManager configManager = plugin.getConfigManager();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("on")
            .requires(source -> source.hasPermission("whitelist.on"))
            .executes(OnCommand::execute)
            .build();

    }

    private static int execute(CommandContext<CommandSource> ctx) {
        if (configManager.isWhitelistEnabled()) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.whitelist-already-on"));
            return 1;
        }

        configManager.setWhitelistEnabled(true);
        plugin.getLoginListener().setEnabled(true);

        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.whitelist-on"));

        return 1;
    }
}
