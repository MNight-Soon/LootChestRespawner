package com.mnightsoon.lootchestrespawner;

import com.mnightsoon.lootchestrespawner.config.ModConfig;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(LootChestRespawner.MODID)
public class LootChestRespawner {
    public static final String MODID = "lootchestrespawner";
    public static final Logger LOGGER = LoggerFactory.getLogger(MODID);

    public LootChestRespawner() {
        IEventBus modBus = MinecraftForge.EVENT_BUS;
        ModLoadingContext.get().registerConfig(net.minecraftforge.fml.config.ModConfig.Type.COMMON, ModConfig.SPEC);
        MinecraftForge.EVENT_BUS.register(this);
        LOGGER.info("Loot Chest Respawner initialized");
    }
}
