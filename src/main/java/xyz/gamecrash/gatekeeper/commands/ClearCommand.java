package xyz.gamecrash.gatekeeper.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import xyz.gamecrash.gatekeeper.GateKeeper;
import xyz.gamecrash.gatekeeper.util.MessageUtil;
import xyz.gamecrash.gatekeeper.cache.WhitelistCache;

import java.util.Random;
import java.util.concurrent.TimeUnit;

public class ClearCommand {
    private static final GateKeeper plugin = GateKeeper.getInstance();
    private static final WhitelistCache cache = plugin.getWhitelistCache();
    private static final ProxyServer server = plugin.getServer();

    private static int confirmation;

    public static LiteralCommandNode<CommandSource> build() {
        return BrigadierCommand.literalArgumentBuilder("clear")
            .requires(source -> source.hasPermission("whitelist.clear"))
            .executes(ClearCommand::execute)
            .then(BrigadierCommand.requiredArgumentBuilder("confirmation", IntegerArgumentType.integer())
                .executes(ClearCommand::executeConfirm)
            )
            .build();
    }

    private static int execute(CommandContext<CommandSource> ctx) {
        confirmation = new Random().nextInt(100, 999);

        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.whitelist-clear", String.valueOf(confirmation)));
        startConfirmExpirationTask();

        return 1;
    }

    private static int executeConfirm(CommandContext<CommandSource> ctx) {
        int userInput = IntegerArgumentType.getInteger(ctx, "confirmation");

        if (userInput != confirmation || confirmation == 0) {
            ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.errors.incorrect-confirmation"));
            confirmation = 0;
            return 1;
        }

        cache.clearWhitelist();
        ctx.getSource().sendMessage(MessageUtil.prefixedMessage("messages.info.whitelist-clear-confirmed"));
        confirmation = 0;

        return 1;
    }

    private static void startConfirmExpirationTask() {
        server.getScheduler().buildTask(plugin, () -> {
            confirmation = 0;
        })
        .delay(15L, TimeUnit.SECONDS)
        .schedule();
    }
}
