package com.ender.loginstreakmod;

import net.fabricmc.api.ModInitializer;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;

public class ModMain implements ModInitializer {
    public static final String MOD_ID = "loginstreakmod";

    @Override
    public void onInitialize() {
        // Load milestone configuration at mod initialization
        ConfigManager.loadMilestones();
        System.out.println("[" + MOD_ID + "] Loaded milestone rewards.");

        // Register login event
        ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
            StreakTracker.onPlayerJoin(handler.getPlayer());
        });
    }
}