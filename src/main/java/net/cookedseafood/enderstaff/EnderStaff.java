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
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
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
	public static final byte VERSION_MINOR = 1;
	public static final byte VERSION_PATCH = 0;

	public static final short STEP_PER_DISTANCE = Short.MIN_VALUE / Byte.MIN_VALUE;
	public static final double DISTANCE_PER_STEP = 1.0 / STEP_PER_DISTANCE;

	public static final float MANA_CONSUMPTION = 1;
	public static final byte TELEPORT_DISTANCE = 8;
	public static final boolean IS_PARTICLE_VISIBLE = true;
	public static final double PARTICLE_X_WIDTH_SCALE = 0.5;
	public static final double PARTICLE_Y_OFFSET = 0.25;
	public static final double PARTICLE_Z_WIDTH_SCALE = 0.5;
	public static final int PARTICLE_COUNT = 128;
	public static final double PARTICLE_OFFSET_X_OFFSET = 0.5;
	public static final double PARTICLE_OFFSET_X_MULTIPLIER = 2.0;
	public static final double PARTICLE_OFFSET_Z_OFFSET = 0.5;
	public static final double PARTICLE_OFFSET_Z_MULTIPLIER = 2.0;
	public static final double PARTICLE_SPEED = 1.0;
	public static final boolean IS_LANTENCY_COMPENSATION = true;
	public static final int MAX_LANTENCY_COMPENSATION_PREDICT_MILLISECONDS = 200;

	public static float manaConsumption;
	public static byte teleportDistance;
	public static boolean isParticleVisible;
	public static double particleXWidthScale;
	public static double particleYOffset;
	public static double particleZWidthScale;
	public static int particleCount;
	public static double particleOffsetXOffset;
	public static double particleOffsetXMultiplier;
	public static double particleOffsetZOffset;
	public static double particleOffsetZMultiplier;
	public static double particleSpeed;
	public static boolean isLantencyCompensation;
	public static int maxLantencyCompensationPredictMilliseconds;

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
			if (!"ender-staff".equals(stack.getCustomId())) {
				return ActionResult.PASS;
			}

			if (!ManaPreferenceComponentInstance.MANA_PREFERENCE.get(player).isEnabled()) {
				return ActionResult.FAIL;
			}

			if (!ServerManaBarComponentInstance.SERVER_MANA_BAR.get(player).getServerManaBar().consum(manaConsumption)) {
				return ActionResult.FAIL;
			}

			usedBy((ServerPlayerEntity)player, (ServerWorld)world);

			return ActionResult.SUCCESS;
		});
	}

	/**
	 * Teleport the entity up to 8 blocks forward. The teleport will end up at a block center
	 * before a block with collision or fluids is found.
	 * 
	 * <p>The entity will NOT be teleported if the destination blockPos equals current blockPos.
	 * 
	 * @param entity
	 * @param world
	 * @return true if the entity was teleported.
	 */
	public static boolean usedBy(Entity entity, ServerWorld world) {
		Vec3d pos = entity.getEyePos();
		Vec3d delta = Vec3d.fromPolar(entity.getPitch(), entity.getYaw()).multiply(DISTANCE_PER_STEP);
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
			return false;
		}

		if (isParticleVisible) {
			spawnTeleportParticles(world, entity);
		}

		if (entity instanceof ServerPlayerEntity && isLantencyCompensation) {
			float predictTicks = Math.max(((ServerPlayerEntity)entity).networkHandler.getLatency(), maxLantencyCompensationPredictMilliseconds) / 50;
			entity.teleport(
				entity.getServer().getWorld(world.getRegistryKey()),
				blockPos.getX() + 0.5 + entity.getXDelta() * predictTicks,
				blockPos.getY() + entity.getYDelta() * predictTicks,
				blockPos.getZ() + 0.5 + entity.getZDelta() * predictTicks,
				EnumSet.noneOf(PositionFlag.class),
				entity.getYaw() + entity.getYawDelta() * predictTicks,
				entity.getPitch() + entity.getPitchDelta() * predictTicks,
				false
			);
		} else {
			entity.teleport(
				entity.getServer().getWorld(world.getRegistryKey()),
				blockPos.getX() + 0.5,
				blockPos.getY(),
				blockPos.getZ() + 0.5,
				EnumSet.noneOf(PositionFlag.class),
				entity.getYaw(),
				entity.getPitch(),
				false
			);
		}

		if (isParticleVisible) {
			spawnTeleportParticles(world, entity);
		}

		return true;
	}

	public static void spawnTeleportParticles(ServerWorld world, Entity entity) {
		world.spawnParticles(
			ParticleTypes.PORTAL,
			entity.getParticleX(particleXWidthScale),
			entity.getRandomBodyY() - particleYOffset,
			entity.getParticleZ(particleZWidthScale),
			particleCount,
			(entity.getRandom().nextDouble() - particleOffsetXOffset) * particleOffsetXMultiplier,
			-entity.getRandom().nextDouble(),
			(entity.getRandom().nextDouble() - particleOffsetZOffset) * particleOffsetZMultiplier,
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

		if (config.has("particleXWidthScale")) {
			particleXWidthScale = config.get("particleXWidthScale").getAsDouble();
			counter.increment();
		} else {
			particleXWidthScale = PARTICLE_X_WIDTH_SCALE;
		}

		if (config.has("particleYOffset")) {
			particleYOffset = config.get("particleYOffset").getAsDouble();
			counter.increment();
		} else {
			particleYOffset = PARTICLE_Y_OFFSET;
		}

		if (config.has("particleZWidthScale")) {
			particleZWidthScale = config.get("particleZWidthScale").getAsDouble();
			counter.increment();
		} else {
			particleZWidthScale = PARTICLE_Z_WIDTH_SCALE;
		}

		if (config.has("particleCount")) {
			particleCount = config.get("particleCount").getAsInt();
			counter.increment();
		} else {
			particleCount = PARTICLE_COUNT;
		}

		if (config.has("particleOffsetXOffset")) {
			particleOffsetXOffset = config.get("particleOffsetXOffset").getAsDouble();
			counter.increment();
		} else {
			particleOffsetXOffset = PARTICLE_OFFSET_X_OFFSET;
		}

		if (config.has("particleOffsetXMultiplier")) {
			particleOffsetXMultiplier = config.get("particleOffsetXMultiplier").getAsDouble();
			counter.increment();
		} else {
			particleOffsetXMultiplier = PARTICLE_OFFSET_X_MULTIPLIER;
		}

		if (config.has("particleOffsetZOffset")) {
			particleOffsetZOffset = config.get("particleOffsetZOffset").getAsDouble();
			counter.increment();
		} else {
			particleOffsetZOffset = PARTICLE_OFFSET_Z_OFFSET;
		}

		if (config.has("particleOffsetZMultiplier")) {
			particleOffsetZMultiplier = config.get("particleOffsetZMultiplier").getAsDouble();
			counter.increment();
		} else {
			particleOffsetZMultiplier = PARTICLE_OFFSET_Z_MULTIPLIER;
		}

		if (config.has("particleSpeed")) {
			particleSpeed = config.get("particleSpeed").getAsDouble();
			counter.increment();
		} else {
			particleSpeed = PARTICLE_SPEED;
		}

		if (config.has("isLantencyCompensation")) {
			isLantencyCompensation = config.get("isLantencyCompensation").getAsBoolean();
			counter.increment();
		} else {
			isLantencyCompensation = IS_LANTENCY_COMPENSATION;
		}

		if (config.has("maxLantencyCompensationPredictMilliseconds")) {
			maxLantencyCompensationPredictMilliseconds = config.get("maxLantencyCompensationPredictMilliseconds").getAsInt();
			counter.increment();
		} else {
			maxLantencyCompensationPredictMilliseconds = MAX_LANTENCY_COMPENSATION_PREDICT_MILLISECONDS;
		}

		recalculate();
		return counter.intValue();
	}

	public static void reset() {
		manaConsumption = MANA_CONSUMPTION;
		teleportDistance = TELEPORT_DISTANCE;
		isParticleVisible = IS_PARTICLE_VISIBLE;
		particleXWidthScale = PARTICLE_X_WIDTH_SCALE;
		particleYOffset = PARTICLE_Y_OFFSET;
		particleZWidthScale = PARTICLE_Z_WIDTH_SCALE;
		particleCount = PARTICLE_COUNT;
		particleOffsetXOffset = PARTICLE_OFFSET_X_OFFSET;
		particleOffsetXMultiplier = PARTICLE_OFFSET_X_MULTIPLIER;
		particleOffsetZOffset = PARTICLE_OFFSET_Z_OFFSET;
		particleOffsetZMultiplier = PARTICLE_OFFSET_Z_MULTIPLIER;
		particleSpeed = PARTICLE_SPEED;
		isLantencyCompensation = IS_LANTENCY_COMPENSATION;
		maxLantencyCompensationPredictMilliseconds = MAX_LANTENCY_COMPENSATION_PREDICT_MILLISECONDS;
	}

	public static void recalculate() {
		teleportStep = (short)(teleportDistance * STEP_PER_DISTANCE);
	}
}
