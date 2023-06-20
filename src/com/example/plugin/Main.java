package com.example.plugin;

import org.bukkit.entity.Item;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class Main extends JavaPlugin implements Listener {

    private double lowTPSThreshold;
    private boolean disableMobAI;
    private int itemClearInterval;

    private long lastTickTime;
    private double tps;

    @Override
    public void onEnable() {
        saveDefaultConfig();

        lowTPSThreshold = getConfig().getDouble("low-tps-threshold", 15.0);
        disableMobAI = getConfig().getBoolean("disable-mob-ai", true);
        itemClearInterval = getConfig().getInt("item-clear-interval", 6000);

        getServer().getPluginManager().registerEvents(this, this);

        lastTickTime = System.currentTimeMillis();
        tps = 20.0;

        new BukkitRunnable() {
            @Override
            public void run() {
                long currentTime = System.currentTimeMillis();
                long tickDuration = currentTime - lastTickTime;
                lastTickTime = currentTime;

                tps = 0.95 * tps + (50.0 / tickDuration) * 0.05;

                if (tps < lowTPSThreshold) {
                    getServer().dispatchCommand(getServer().getConsoleSender(), "gamerule doMobSpawning false");
                    if (disableMobAI) {
                        disableMobAI();
                    }
                } else {
                    getServer().dispatchCommand(getServer().getConsoleSender(), "gamerule doMobSpawning true");
                    if (disableMobAI) {
                        enableMobAI();
                    }
                }
            }
        }.runTaskTimer(this, 0, 1);

        new BukkitRunnable() {
            @Override
            public void run() {
                removeGroundItems();
            }
        }.runTaskTimer(this, 0, itemClearInterval);
    }

    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (tps < lowTPSThreshold) {
            event.setCancelled(true);
        }
    }

    private void disableMobAI() {
        for (LivingEntity entity : getServer().getWorlds().get(0).getLivingEntities()) {
            if (!(entity instanceof Player)) {
                entity.setAI(false);
            }
        }
    }

    private void enableMobAI() {
        for (LivingEntity entity : getServer().getWorlds().get(0).getLivingEntities()) {
            if (!(entity instanceof Player)) {
                entity.setAI(true);
            }
        }
    }

    private void removeGroundItems() {
        for (Item item : getServer().getWorlds().get(0).getEntitiesByClass(Item.class)) {
            item.remove();
        }
    }
}