package xyz.gamecrash.velocitywhitelist.commands;

import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.tree.LiteralCommandNode;
import com.velocitypowered.api.command.BrigadierCommand;
import com.velocitypowered.api.command.CommandSource;
import com.velocitypowered.api.proxy.ProxyServer;
import xyz.gamecrash.velocitywhitelist.VelocityWhitelist;
import xyz.gamecrash.velocitywhitelist.storage.Database;

import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.random.RandomGenerator;

public class ClearCommand {
    private static final VelocityWhitelist plugin = VelocityWhitelist.getInstance();
    private static final Database db = plugin.getDatabase();
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

        ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <red>Please type in <white>/whitelist clear "
            + confirmation + "<red> to confirm clearing the whitelist. This action cannot be undone."
        );
        startConfirmClearTask();

        return 1;
    }

    private static int executeConfirm(CommandContext<CommandSource> ctx) {
        int userInput = IntegerArgumentType.getInteger(ctx, "confirmation");

        if (userInput != confirmation || confirmation == 0) {
            ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <red>Incorrect confirmation number");
            confirmation = 0;
            return 1;
        }

        db.clearWhitelist();
        ctx.getSource().sendRichMessage("<dark_gray>[<yellow>Whitelist<dark_gray>] <green>Whitelist cleared successfully");
        confirmation = 0;

        return 1;
    }

    private static void startConfirmClearTask() {
        server.getScheduler().buildTask(plugin, () -> {
            confirmation = 0;
        })
        .delay(15L, TimeUnit.SECONDS)
        .schedule();
    }
}
