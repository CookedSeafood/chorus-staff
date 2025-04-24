package net.cookedseafood.chorusstaff.command;

import com.mojang.brigadier.CommandDispatcher;
import net.cookedseafood.chorusstaff.ChorusStaff;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.server.command.CommandManager;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.Text;

public class ChorusStaffCommand {
	public static void register(CommandDispatcher<ServerCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
		dispatcher.register(
			CommandManager.literal("chorusstaff")
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
		source.sendFeedback(() -> Text.literal("Reloading Chorus Staff!"), true);
		return ChorusStaff.reload();
	}

	public static int executeVersion(ServerCommandSource source) {
		source.sendFeedback(() -> Text.literal("Chorus Staff " + ChorusStaff.VERSION_MAJOR + "." + ChorusStaff.VERSION_MINOR + "." + ChorusStaff.VERSION_PATCH), false);
		return 0;
	}
}
