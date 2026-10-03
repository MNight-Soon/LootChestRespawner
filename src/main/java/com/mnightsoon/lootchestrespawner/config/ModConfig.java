package com.mnightsoon.lootchestrespawner.config;

import net.minecraftforge.common.ForgeConfigSpec;

public final class ModConfig {
    public static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SPEC;

    public static final ForgeConfigSpec.BooleanValue ENABLE_RESPAWN;
    public static final ForgeConfigSpec.IntValue RESPAWN_COOLDOWN_DAYS;
    public static final ForgeConfigSpec.IntValue CHECK_INTERVAL_SECONDS;
    public static final ForgeConfigSpec.BooleanValue RESPAWN_FOR_VANILLA_CHESTS;
    public static final ForgeConfigSpec.BooleanValue RESPAWN_FOR_LOOTR_CHESTS;

    static {
        BUILDER.comment("Loot Chest Respawner configuration");

        ENABLE_RESPAWN = BUILDER
            .comment("Enable the chest respawn feature.")
            .define("enableRespawn", true);

        RESPAWN_COOLDOWN_DAYS = BUILDER
            .comment("Number of in-game days before loot is regenerated on a chest.")
            .defineInRange("respawnCooldownDays", 5, 1, 1000);

        CHECK_INTERVAL_SECONDS = BUILDER
            .comment("How often the server scans loaded chests in seconds.")
            .defineInRange("checkIntervalSeconds", 60, 5, 3600);

        RESPAWN_FOR_VANILLA_CHESTS = BUILDER
            .comment("Allow regular vanilla chests to respawn loot.")
            .define("respawnForVanillaChests", true);

        RESPAWN_FOR_LOOTR_CHESTS = BUILDER
            .comment("Allow Lootr chests to respawn when the Lootr mod is installed.")
            .define("respawnForLootrChests", true);

        SPEC = BUILDER.build();
    }

    public static long getRespawnCooldownTicks() {
        return (long) RESPAWN_COOLDOWN_DAYS.get() * 20L * 60L * 60L * 24L;
    }

    private ModConfig() {
    }
}
