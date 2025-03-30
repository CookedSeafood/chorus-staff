package net.cookedseafood.enderstaff.command;

import com.mojang.brigadier.CommandDispatcher;
import net.cookedseafood.enderstaff.EnderStaff;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class EnderStaffCommand {
	public EnderStaffCommand() {
	}

	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(
			CommandManager.literal("enderstaff")
			.then(
				CommandManager.literal("reload")
				.requires(source -> source.hasPermissionLevel(2))
				.executes(context -> executeReload((ServerCommandSource)context.getSource()))
			)
			.then(
				CommandManager.literal("version")
				.executes(context -> executeVersion((ServerCommandSource)context.getSource()))
			)
		);
	}

	public static int executeReload(ServerCommandSource source) {
		source.sendFeedback(() -> Text.literal("Reloading Ender Staff!"), true);
		return EnderStaff.reload();
	}

	public static int executeVersion(ServerCommandSource source) {
		source.sendFeedback(() -> Text.literal("Ender Staff " + EnderStaff.VERSION_MAJOR + "." + EnderStaff.VERSION_MINOR + "." + EnderStaff.VERSION_PATCH), false);
		return 0;
	}
}
