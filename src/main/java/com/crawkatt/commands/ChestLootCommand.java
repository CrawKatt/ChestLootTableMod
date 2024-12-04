package com.crawkatt.commands;

import com.crawkatt.config.ConfigLoader;
import com.mojang.brigadier.CommandDispatcher;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ChestLootCommand {
    private static void register(CommandDispatcher<ServerCommandSource> dispatcher) {
        dispatcher.register(CommandManager.literal("chestloot")
                .then(CommandManager.literal("reload")
                        .executes(context -> {
                            ConfigLoader.loadConfig();
                            context.getSource().sendFeedback(Text.of("ChestLoot configuration reloaded!"), true);
                            return 1;
                        }))
        );
    }

    public static void registerCommands() {
        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> register(dispatcher));
    }
}
