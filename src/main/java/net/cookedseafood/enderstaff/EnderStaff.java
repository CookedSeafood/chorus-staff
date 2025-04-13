package net.cookedseafood.enderstaff;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.EnumSet;
import net.cookedseafood.enderstaff.command.EnderStaffCommand;
import net.cookedseafood.pentamana.component.ManaPreferenceComponentInstance;
import net.cookedseafood.pentamana.component.ServerManaBarComponentInstance;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.google.gson.Gson;
import com.google.gson.JsonObject;

public class EnderStaff implements ModInitializer {
	public static final String MOD_ID = "ender-staff";

	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	public static final byte VERSION_MAJOR = 1;
	public static final byte VERSION_MINOR = 0;
	public static final byte VERSION_PATCH = 3;

	public static final short STEP_PER_DISTANCE = Short.MIN_VALUE / Byte.MIN_VALUE;
	public static final double DISTANCE_PER_STEP = 1.0 / STEP_PER_DISTANCE;

	public static final float MANA_CONSUMPTION = 1;
	public static final byte TELEPORT_DISTANCE = 8;
	public static final boolean IS_PARTICLE_VISIBLE = true;
	public static final int PARTICLE_COUNT = 128;
	public static final double PARTICLE_SPEED = 1.0;

	public static float manaConsumption;
	public static byte teleportDistance;
	public static boolean isParticleVisible;
	public static int particleCount;
	public static double particleSpeed;

	public static short teleportStep;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.

		CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> EnderStaffCommand.register(dispatcher, registryAccess));

		ServerLifecycleEvents.SERVER_STARTED.register(server -> reload());

		UseItemCallback.EVENT.register((player, world, hand) -> {
			if (Hand.OFF_HAND.equals(hand)) {
				return ActionResult.PASS;
			}

			ItemStack stack = player.getMainHandStack();
			if (!"Ender Staff".equals(stack.getItemName().getString())) {
				return ActionResult.PASS;
			}

			if (!ManaPreferenceComponentInstance.MANA_PREFERENCE.get(player).isEnabled()) {
				return ActionResult.FAIL;
			}

			if (!ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().consum(manaConsumption)) {
				return ActionResult.FAIL;
			}

			Vec3d pos = player.getEyePos();
			Vec3d delta = Vec3d.fromPolar(player.getPitch(), player.getYaw()).multiply(DISTANCE_PER_STEP);
			BlockPos blockPos = BlockPos.ofFloored(pos);
			BlockPos startBlockPos = blockPos;

			for (short step = 0; step < teleportStep; ++step) {
				pos = pos.add(delta);
				BlockPos nextBlockPos = BlockPos.ofFloored(pos);
				if (blockPos.equals(nextBlockPos)) {
					continue;
				}

				if (!world.getBlockState(nextBlockPos).getCollisionShape(world, nextBlockPos).isEmpty() || !world.getFluidState(nextBlockPos).isEmpty()) {
					break;
				}

				blockPos = nextBlockPos;
			}

			if (blockPos.equals(startBlockPos)) {
				return ActionResult.FAIL;
			}

			if (isParticleVisible) {
				spawnTeleportParticles((ServerWorld)world, player);
			}
			player.teleport(
				player.getServer().getWorld(world.getRegistryKey()),
				blockPos.getX() + 0.5,
				blockPos.getY(),
				blockPos.getZ() + 0.5,
				EnumSet.noneOf(PositionFlag.class),
				player.getYaw(),
				player.getPitch(),
				false
			);
			if (isParticleVisible) {
				spawnTeleportParticles((ServerWorld)world, player);
			}
			return ActionResult.SUCCESS;
		});
	}

	public static void spawnTeleportParticles(ServerWorld world, PlayerEntity player) {
		world.spawnParticles(
			ParticleTypes.PORTAL,
			player.getParticleX(0.5),
			player.getRandomBodyY() - 0.25,
			player.getParticleZ(0.5),
			particleCount,
			(player.getRandom().nextDouble() - 0.5) * 2.0,
			-player.getRandom().nextDouble(),
			(player.getRandom().nextDouble() - 0.5) * 2.0,
			particleSpeed
		);
	}

	public static int reload() {
		String configString;
		try {
			configString = FileUtils.readFileToString(new File("./config/ender-staff.json"), StandardCharsets.UTF_8);
		} catch (IOException e) {
			reset();
			recalculate();
			return 1;
		}

		JsonObject config = new Gson().fromJson(configString, JsonObject.class);
		MutableInt counter = new MutableInt(0);

		if (config.has("manaConsumption")) {
			manaConsumption = config.get("manaConsumption").getAsFloat();
			counter.increment();
		} else {
			manaConsumption = MANA_CONSUMPTION;
		}

		if (config.has("teleportDistance")) {
			teleportDistance = config.get("teleportDistance").getAsByte();
			counter.increment();
		} else {
			teleportDistance = TELEPORT_DISTANCE;
		}

		if (config.has("isParticleVisible")) {
			isParticleVisible = config.get("isParticleVisible").getAsBoolean();
			counter.increment();
		} else {
			isParticleVisible = IS_PARTICLE_VISIBLE;
		}

		if (config.has("particleCount")) {
			particleCount = config.get("particleCount").getAsInt();
			counter.increment();
		} else {
			particleCount = PARTICLE_COUNT;
		}

		if (config.has("particleSpeed")) {
			particleSpeed = config.get("particleSpeed").getAsDouble();
			counter.increment();
		} else {
			particleSpeed = PARTICLE_SPEED;
		}

		recalculate();
		return counter.intValue();
	}

	public static void reset() {
		manaConsumption = MANA_CONSUMPTION;
		teleportDistance = TELEPORT_DISTANCE;
		isParticleVisible = IS_PARTICLE_VISIBLE;
		particleCount = PARTICLE_COUNT;
		particleSpeed = PARTICLE_SPEED;
	}

	public static void recalculate() {
		teleportStep = (short) (teleportDistance * STEP_PER_DISTANCE);
	}
}
