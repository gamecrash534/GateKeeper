package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.config.ConfigManager;
import xyz.gamecrash.velocitywhitelist.listener.LoginListener;
import xyz.gamecrash.velocitywhitelist.util.MessageUtil;

public class OffCommand {
    private static final VelocityWhitelist plugin = VelocityWhitelist.getInstance();
    private static final ConfigManager configManager = plugin.getConfigManager();
    private static final LoginListener loginListener = plugin.getLoginListener();

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("off")
            .requires(source -> source.hasPermission("whitelist.off"))
            .executes(OffCommand::execute)
            .build();

    }

    private static int execute(CommandContext<CommandSource> ctx) {
        if (!configManager.isWhitelistEnabled()) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.whitelist-already-off"));
            return 1;
        }
        configManager.setWhitelistEnabled(false);
        plugin.getLoginListener().setEnabled(false);

        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.whitelist-off"));
        return 1;
    }
}
