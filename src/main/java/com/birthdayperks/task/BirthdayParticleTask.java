package com.birthdayperks.task;

import com.birthdayperks.PlayerBirthdayPerks;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class BirthdayParticleTask {

    private final PlayerBirthdayPerks plugin;
    private final Map<UUID, BukkitRunnable> activeTasks = new ConcurrentHashMap<>();

    public BirthdayParticleTask(PlayerBirthdayPerks plugin) {
        this.plugin = plugin;
    }

    public void startParticles(Player player) {
        FileConfiguration config = plugin.getRewardManager().getRewardsConfig();
        
        if (config == null || !config.getBoolean("particle.enabled", true)) {
            return;
        }

        // 停止已有的粒子任务
        stopParticles(player);

        String particleType = config.getString("particle.type", "HEART");
        int count = config.getInt("particle.count", 5);
        double radius = config.getDouble("particle.radius", 1.5);
        long interval = config.getLong("particle.interval", 10);
        int duration = config.getInt("particle.duration", -1);

        Particle particle;
        try {
            particle = Particle.valueOf(particleType.toUpperCase());
        } catch (IllegalArgumentException e) {
            particle = Particle.HEART;
        }

        final Particle finalParticle = particle;
        final long startTime = System.currentTimeMillis();
        final long durationMs = duration > 0 ? duration * 1000L : Long.MAX_VALUE;

        BukkitRunnable task = new BukkitRunnable() {
            double angle = 0;

            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(player.getUniqueId());
                    return;
                }

                // 检查持续时间
                if (duration > 0 && System.currentTimeMillis() - startTime > durationMs) {
                    cancel();
                    activeTasks.remove(player.getUniqueId());
                    return;
                }

                Location loc = player.getLocation().add(0, 1, 0);

                // 创建环绕效果
                for (int i = 0; i < count; i++) {
                    double offsetAngle = angle + (Math.PI * 2 * i / count);
                    double x = Math.cos(offsetAngle) * radius;
                    double z = Math.sin(offsetAngle) * radius;

                    Location particleLoc = loc.clone().add(x, 0, z);
                    player.getWorld().spawnParticle(finalParticle, particleLoc, 1, 0, 0, 0, 0);
                }

                angle += 0.2;
                if (angle > Math.PI * 2) {
                    angle = 0;
                }
            }
        };

        task.runTaskTimer(plugin, 0, interval);
        activeTasks.put(player.getUniqueId(), task);
        
        plugin.debug("为玩家 " + player.getName() + " 启动生日粒子效果");
    }

    public void stopParticles(Player player) {
        BukkitRunnable task = activeTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
            plugin.debug("停止玩家 " + player.getName() + " 的生日粒子效果");
        }
    }

    public void stopAllParticles() {
        activeTasks.values().forEach(BukkitRunnable::cancel);
        activeTasks.clear();
    }

    public boolean hasActiveParticles(Player player) {
        return activeTasks.containsKey(player.getUniqueId());
    }
}
