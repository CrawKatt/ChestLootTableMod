package com.crawkatt;

import com.crawkatt.commands.ChestLootCommand;
import com.crawkatt.config.ConfigLoader;
import com.crawkatt.events.BlockPlaceHandler;
import com.crawkatt.events.ChestLootHandler;
import net.fabricmc.api.ModInitializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChestLootMod implements ModInitializer {
	public static final String MOD_ID = "loot-table-mod";
	public static final Logger LOGGER = LoggerFactory.getLogger(ChestLootMod.MOD_ID);

	@Override
	public void onInitialize() {
		ConfigLoader.loadConfig();

		ChestLootHandler.registerEvents();
		BlockPlaceHandler.registerEvents();
		ChestLootCommand.registerCommands();
	}
}