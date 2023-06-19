package com.example.plugin;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntitySpawnEvent;
import org.bukkit.event.entity.ItemSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;

public class tomasalias extends JavaPlugin implements Listener {

    private static final double TPS_THRESHOLD = 18.0;
    private boolean disableSpawning = false;
    private final Set<LivingEntity> entitiesWithDisabledAI = new HashSet<>();
    private boolean itemRemovalEnabled;
    private long itemDespawnTicks;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        loadConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        startMonitoringTPS();
    }

    private void loadConfig() {
        FileConfiguration config = getConfig();
        itemRemovalEnabled = config.getBoolean("item-removal.enabled", true);
        itemDespawnTicks = config.getLong("item-removal.despawn-ticks", 6000);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (disableSpawning) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntitySpawn(EntitySpawnEvent event) {
        if (event.getEntity() instanceof LivingEntity && disableSpawning) {
            LivingEntity livingEntity = (LivingEntity) event.getEntity();
            livingEntity.setAI(false);
            entitiesWithDisabledAI.add(livingEntity);
        }
    }

    private void startMonitoringTPS() {
        new BukkitRunnable() {
            @Override
            public void run() {
                double currentTPS = getServerTPS();
                if (currentTPS < TPS_THRESHOLD) {
                    disableSpawning = true;
                } else {
                    disableSpawning = false;
                    enableAIForEntities();
                }
            }
        }.runTaskTimer(this, 0L, 20L); // Check TPS every second (20 ticks)
    }

    private void enableAIForEntities() {
        entitiesWithDisabledAI.removeIf(livingEntity -> {
            if (!livingEntity.isDead()) {
                livingEntity.setAI(true);
                return true;
            }
            return false;
        });
    }

    private double getServerTPS() {
        double recentTps = getServerTPS();
        return recentTps; // Get the most recent TPS value
    }

@EventHandler
public void onItemSpawn(ItemSpawnEvent event) {
    if (itemRemovalEnabled) {
        new BukkitRunnable() {
            @Override
            public void run() {
                event.getEntity().remove();
            }
        }.runTaskLater(this, itemDespawnTicks);
    }
}
}