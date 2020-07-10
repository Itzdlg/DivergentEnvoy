package me.schooltests.divergentenvoy;

import me.schooltests.divergentenvoy.envoy.Envoy;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class DivergentEnvoyAPI {
    private DivergentEnvoy plugin;
    private BukkitTask envoyCountdownTimer = null;
    private Set<BukkitTask> envoyWarningTimers = new HashSet<>();
    private Envoy currentEnvoy = null;

    public DivergentEnvoyAPI(DivergentEnvoy plugin) {
        this.plugin = plugin;
    }

    public DivergentEnvoy getPlugin() {
        return plugin;
    }

    public void beginEnvoyCountdown() {
        final int firstWarningTime = plugin.getPluginConfig().WARNING_TIMES.stream().mapToInt(v -> v).max().orElse(0);
        final int lastWarningTime = plugin.getPluginConfig().WARNING_TIMES.stream().mapToInt(v -> v).min().orElse(0);
        try {
            envoyCountdownTimer = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                List<Integer> warnings = plugin.getPluginConfig().WARNING_TIMES;
                warnings.remove(lastWarningTime);
                for (int warningTime : warnings)
                    envoyWarningTimers.add(Bukkit.getScheduler().runTaskLater(plugin, () -> sendEnvoyWarningMessage(warningTime), (firstWarningTime - warningTime + 1) * 20));

                envoyWarningTimers.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendEnvoyWarningMessage(firstWarningTime - lastWarningTime);
                    beginEnvoy(plugin.getPluginConfig().ENVOY_DURATION,
                            plugin.getPluginConfig().ENVOY_DROP_WORLD,
                            plugin.getPluginConfig().ENVOY_DROP_REGION);
                }, (firstWarningTime - lastWarningTime) * 20));
            }, (plugin.getPluginConfig().ENVOY_COOLDOWN - firstWarningTime) * 20);
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("Invalid world or region provided for envoy spawn! (config.yml: envoy.world & envoy.region)");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    public void endEnvoyCountdown() {
        if (envoyCountdownTimer != null) envoyCountdownTimer.cancel();
        for (BukkitTask t : envoyWarningTimers) t.cancel();
        envoyWarningTimers.clear();
    }

    private void sendEnvoyWarningMessage(int time) {

    }

    public void beginEnvoy(int duration, String world, String region) throws IllegalArgumentException {
        endEnvoyCountdown();
        currentEnvoy = new Envoy(plugin)
                .setEnvoyDuration(duration)
                .setEnvoyWorld(world)
                .setEnvoyRegion(region);

        currentEnvoy.begin();
    }

    public void beginEnvoy(int duration, String world, String region, int numDrops) throws IllegalArgumentException {
        endEnvoyCountdown();
        currentEnvoy = new Envoy(plugin)
                .setEnvoyDuration(duration)
                .setEnvoyWorld(world)
                .setEnvoyRegion(region)
                .setEnvoyDropCount(numDrops);

        currentEnvoy.begin();
    }

    public Envoy getCurrentEnvoy() {
        return currentEnvoy;
    }
}
