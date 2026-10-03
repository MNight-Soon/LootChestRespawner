package com.mnightsoon.lootchestrespawner.event;

import com.mnightsoon.lootchestrespawner.LootChestRespawner;
import com.mnightsoon.lootchestrespawner.config.ModConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Locale;
import java.util.Random;

@Mod.EventBusSubscriber(modid = LootChestRespawner.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public final class ChestRespawnHandler {
    private static final List<ResourceLocation> FALLBACK_LOOT_TABLES = List.of(
        new ResourceLocation("minecraft:chests/simple_dungeon"),
        new ResourceLocation("minecraft:chests/abandoned_mineshaft"),
        new ResourceLocation("minecraft:chests/stronghold_corridor"),
        new ResourceLocation("minecraft:chests/stronghold_crossing"),
        new ResourceLocation("minecraft:chests/jungle_temple"),
        new ResourceLocation("minecraft:chests/desert_pyramid"),
        new ResourceLocation("minecraft:chests/woodland_mansion")
    );

    private static final Random RANDOM = new Random();
    private static long lastScanTick = 0L;

    private ChestRespawnHandler() {
    }

    @SubscribeEvent
    public static void onPlayerRightClickChest(PlayerInteractEvent.RightClickBlock event) {
        if (!(event.getLevel() instanceof ServerLevel level)) {
            return;
        }

        BlockPos pos = event.getPos();
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof ChestBlockEntity chest)) {
            return;
        }

        if (!shouldHandleChest(chest)) {
            return;
        }

        CompoundTag tag = chest.saveWithFullMetadata();
        tag.putLong("lootchestrespawner:last_open_tick", level.getGameTime());
        chest.load(tag);
        chest.setChanged();
    }

    @SubscribeEvent
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) {
            return;
        }

        if (!ModConfig.ENABLE_RESPAWN.get()) {
            return;
        }

        long currentTick = event.getServer().getTickCount();
        int scanInterval = ModConfig.CHECK_INTERVAL_SECONDS.get() * 20;
        if (lastScanTick == 0L || currentTick - lastScanTick >= scanInterval) {
            lastScanTick = currentTick;
            for (ServerLevel level : event.getServer().getAllLevels()) {
                scanLevel(level);
            }
        }
    }

    private static void scanLevel(ServerLevel level) {
        for (LevelChunk chunk : level.getChunkSource().getLoadedChunks()) {
            if (chunk == null) {
                continue;
            }

            for (BlockEntity blockEntity : chunk.getBlockEntities().values()) {
                if (!(blockEntity instanceof ChestBlockEntity chest)) {
                    continue;
                }

                if (!shouldHandleChest(chest)) {
                    continue;
                }

                if (shouldRespawnNow(level, chest)) {
                    respawnChest(level, chest);
                }
            }
        }
    }

    private static boolean shouldHandleChest(ChestBlockEntity chest) {
        if (chest == null) {
            return false;
        }

        String className = chest.getClass().getName().toLowerCase(Locale.ROOT);
        boolean isLootrChest = className.contains("lootr");

        if (isLootrChest && !ModConfig.RESPAWN_FOR_LOOTR_CHESTS.get()) {
            return false;
        }

        if (!isLootrChest && !ModConfig.RESPAWN_FOR_VANILLA_CHESTS.get()) {
            return false;
        }

        return true;
    }

    private static boolean shouldRespawnNow(ServerLevel level, ChestBlockEntity chest) {
        CompoundTag tag = chest.saveWithFullMetadata();
        long lastOpen = tag.getLong("lootchestrespawner:last_open_tick");
        if (lastOpen <= 0L) {
            return false;
        }

        long cooldownTicks = ModConfig.getRespawnCooldownTicks();
        return level.getGameTime() >= lastOpen + cooldownTicks;
    }

    private static void respawnChest(ServerLevel level, ChestBlockEntity chest) {
        ResourceLocation currentTable = chest.getLootTable();
        ResourceLocation tableToUse = currentTable != null ? currentTable : chooseFallbackTable();
        if (tableToUse == null) {
            return;
        }

        chest.setLootTable(tableToUse, level.getRandom().nextLong());
        chest.unpackLootTable(null);
        chest.setChanged();

        CompoundTag tag = chest.saveWithFullMetadata();
        tag.putLong("lootchestrespawner:last_open_tick", level.getGameTime());
        chest.load(tag);
        chest.setChanged();
    }

    private static ResourceLocation chooseFallbackTable() {
        if (FALLBACK_LOOT_TABLES.isEmpty()) {
            return null;
        }

        return FALLBACK_LOOT_TABLES.get(RANDOM.nextInt(FALLBACK_LOOT_TABLES.size()));
    }
}
