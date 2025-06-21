package net.cookedseafood.chorusstaff.data;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import net.cookedseafood.chorusstaff.ChorusStaff;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.mutable.MutableInt;

public abstract class ChorusStaffConfig {
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
    public static final boolean SHOULD_LANTENCY_COMPENSATION = true;
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
    public static boolean shouldLantencyCompensation;
    public static int maxLantencyCompensationPredictMilliseconds;
    public static short teleportStep;

    public static int reload() {
        String configString;
        try {
            configString = FileUtils.readFileToString(new File("./config/chorus-staff.json"), StandardCharsets.UTF_8);
        } catch (IOException e) {
            reset();
            reCalc();
            return 1;
        }

        JsonObject config = new Gson().fromJson(configString, JsonObject.class);
        if (config == null) {
            reset();
            reCalc();
            return 1;
        }

        return reload(config);
    }

    public static int reload(JsonObject config) {
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

        if (config.has("shouldLantencyCompensation")) {
            shouldLantencyCompensation = config.get("shouldLantencyCompensation").getAsBoolean();
            counter.increment();
        } else {
            shouldLantencyCompensation = SHOULD_LANTENCY_COMPENSATION;
        }

        if (config.has("maxLantencyCompensationPredictMilliseconds")) {
            maxLantencyCompensationPredictMilliseconds = config.get("maxLantencyCompensationPredictMilliseconds").getAsInt();
            counter.increment();
        } else {
            maxLantencyCompensationPredictMilliseconds = MAX_LANTENCY_COMPENSATION_PREDICT_MILLISECONDS;
        }

        reCalc();
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
        shouldLantencyCompensation = SHOULD_LANTENCY_COMPENSATION;
        maxLantencyCompensationPredictMilliseconds = MAX_LANTENCY_COMPENSATION_PREDICT_MILLISECONDS;
    }

    public static void reCalc() {
        teleportStep = (short)(teleportDistance * ChorusStaff.STEP_PER_DISTANCE);
    }
}
