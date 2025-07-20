package net.cookedseafood.chorusstaff;

import java.util.EnumSet;
import net.cookedseafood.chorusstaff.command.ChorusStaffCommand;
import net.cookedseafood.chorusstaff.data.ChorusStaffConfig;
import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.command.v2.CommandRegistrationCallback;
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerLifecycleEvents;
import net.fabricmc.fabric.api.event.player.UseItemCallback;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PositionFlag;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ChorusStaff implements ModInitializer {
    public static final String MOD_ID = "chorus-staff";

    // This logger is used to write text to the console and the log file.
    // It is considered best practice to use your mod id as the logger's name.
    // That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static final byte VERSION_MAJOR = 1;
    public static final byte VERSION_MINOR = 6;
    public static final byte VERSION_PATCH = 0;

    public static final String MOD_NAMESPACE = "chorus_staff";
    public static final String CHORUS_STAFF_CUSTOM_ID = Identifier.of(MOD_NAMESPACE, "chorus_staff").toString();
    public static final short STEP_PER_DISTANCE = Short.MIN_VALUE / Byte.MIN_VALUE;
    public static final double DISTANCE_PER_STEP = 1.0 / STEP_PER_DISTANCE;

    @Override
    public void onInitialize() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        CommandRegistrationCallback.EVENT.register((dispatcher, registryAccess, environment) -> ChorusStaffCommand.register(dispatcher, registryAccess));

        ServerLifecycleEvents.SERVER_STARTED.register(server -> ChorusStaffConfig.reload());

        UseItemCallback.EVENT.register((player, world, hand) -> {
            if (Hand.OFF_HAND.equals(hand)) {
                return ActionResult.PASS;
            }

            ItemStack stack = player.getMainHandStack();
            if (!CHORUS_STAFF_CUSTOM_ID.equals(stack.getCustomId())) {
                return ActionResult.PASS;
            }

            return usedBy((ServerPlayerEntity)player, (ServerWorld)world) ? ActionResult.SUCCESS : ActionResult.FAIL;
        });
    }

    /**
     * Teleport the entity up to 8 blocks forward. The teleport will end up at a block center
     * before a block with collision or fluids is found.
     * 
     * <p>The entity will NOT be teleported if the destination blockPos equals current blockPos.</p>
     * 
     * @param entity
     * @param world
     * @return {@code true} if the entity was teleported
     */
    public static boolean usedBy(LivingEntity entity, ServerWorld world) {
        if (!entity.consumMana(ChorusStaffConfig.manaConsumption)) {
            return false;
        }

        Vec3d pos = entity.getEyePos();
        Vec3d delta = Vec3d.fromPolar(entity.getPitch(), entity.getYaw()).multiply(DISTANCE_PER_STEP);
        BlockPos blockPos = BlockPos.ofFloored(pos);
        BlockPos startBlockPos = blockPos;

        for (short step = 0; step < ChorusStaffConfig.teleportStep; ++step) {
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

        if (ChorusStaffConfig.isParticleVisible) {
            spawnTeleportParticles(world, entity);
        }

        if (entity instanceof ServerPlayerEntity && ChorusStaffConfig.shouldLantencyCompensation) {
            float predictTicks = Math.max(((ServerPlayerEntity)entity).networkHandler.getLatency(), ChorusStaffConfig.maxLantencyCompensationPredictMilliseconds) / 50;
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

        if (ChorusStaffConfig.isParticleVisible) {
            spawnTeleportParticles(world, entity);
        }

        return true;
    }

    public static void spawnTeleportParticles(ServerWorld world, Entity entity) {
        world.spawnParticles(
            ParticleTypes.PORTAL,
            entity.getParticleX(ChorusStaffConfig.particleXWidthScale),
            entity.getRandomBodyY() - ChorusStaffConfig.particleYOffset,
            entity.getParticleZ(ChorusStaffConfig.particleZWidthScale),
            ChorusStaffConfig.particleCount,
            (entity.getRandom().nextDouble() - ChorusStaffConfig.particleOffsetXOffset) * ChorusStaffConfig.particleOffsetXMultiplier,
            -entity.getRandom().nextDouble(),
            (entity.getRandom().nextDouble() - ChorusStaffConfig.particleOffsetZOffset) * ChorusStaffConfig.particleOffsetZMultiplier,
            ChorusStaffConfig.particleSpeed
        );
    }
}
