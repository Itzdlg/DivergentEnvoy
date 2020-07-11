package me.schooltests.divergentenvoy;

import me.schooltests.divergentenvoy.envoy.Envoy;
import org.bukkit.Bukkit;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * DivergentEnvoy API for interacting and beginning envoys.
 *
 * @author Itzdlg
 * @since 1.0.0
 */
public class DivergentEnvoyAPI {
    private DivergentEnvoy plugin;
    private BukkitTask envoyCountdownTimer = null;
    private Set<BukkitTask> envoyWarningTimers = new HashSet<>();
    private Envoy currentEnvoy = null;

    public DivergentEnvoyAPI(DivergentEnvoy plugin) {
        this.plugin = plugin;
    }

    /**
     * @return Instance of the instantiating plugin
     */
    public DivergentEnvoy getPlugin() {
        return plugin;
    }

    /**
     * Begins the schedulers required for counting
     * down to the envoy and giving warning messages,
     * then starts the envoy.
     *
     * @see Envoy
     * @since 1.0.0
     */
    public void beginEnvoyCountdown() {
        final int firstWarningTime = plugin.getPluginConfig().WARNING_TIMES.stream().mapToInt(v -> v).max().orElse(0);
        final int lastWarningTime = plugin.getPluginConfig().WARNING_TIMES.stream().mapToInt(v -> v).min().orElse(0);
        try {
            envoyWarningTimers.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                if (Bukkit.getOnlinePlayers().size() < plugin.getPluginConfig().PLAYERS_REQUIRED) {
                    endEnvoyCountdown();
                    beginEnvoyCountdown();
                }

                List<Integer> warnings = new ArrayList<>(plugin.getPluginConfig().WARNING_TIMES);
                warnings.remove(Integer.valueOf(lastWarningTime));
                for (int warningTime : warnings)
                    envoyWarningTimers.add(Bukkit.getScheduler().runTaskLater(plugin, () -> sendEnvoyWarningMessage(warningTime), (firstWarningTime - warningTime + 1) * 20));

                envoyWarningTimers.add(Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    sendEnvoyWarningMessage(lastWarningTime);

                    envoyCountdownTimer = Bukkit.getScheduler().runTaskLater(plugin, () -> {
                        Bukkit.broadcastMessage(plugin.getMessagesConfig().ENVOY_START_BROADCAST());
                        beginEnvoy(plugin.getPluginConfig().ENVOY_DURATION,
                            plugin.getPluginConfig().ENVOY_DROP_WORLD,
                            plugin.getPluginConfig().ENVOY_DROP_REGION);
                    }, lastWarningTime * 20);
                }, (firstWarningTime - lastWarningTime) * 20));
            }, (plugin.getPluginConfig().ENVOY_COOLDOWN - firstWarningTime) * 20));
        } catch (IllegalArgumentException e) {
            Bukkit.getLogger().severe("Invalid world or region provided for envoy spawn! (config.yml: envoy.world & envoy.region)");
            Bukkit.getPluginManager().disablePlugin(plugin);
        }
    }

    /**
     * Ends the envoy countdown and cancels all related schedulers.
     *
     * @since 1.0.0
     */
    public void endEnvoyCountdown() {
        if (envoyCountdownTimer != null) envoyCountdownTimer.cancel();
        for (BukkitTask t : envoyWarningTimers) t.cancel();
        envoyWarningTimers.clear();
    }

    private void sendEnvoyWarningMessage(int time) {
        Bukkit.broadcastMessage(plugin.getMessagesConfig().TIME_UNTIL_SPAWN(time));
    }

    /**
     * Begins an envoy with the specified parameters
     *
     * @see Envoy
     * @param duration Duration for the envoy
     * @param world World that the envoy drops in
     * @param region Region for the envoy drops
     * @throws IllegalArgumentException Invalid world or region name
     */
    public void beginEnvoy(int duration, String world, String region) throws IllegalArgumentException {
        endEnvoyCountdown();
        currentEnvoy = new Envoy(plugin)
                .setEnvoyDuration(duration)
                .setEnvoyWorld(world)
                .setEnvoyRegion(region);

        currentEnvoy.begin();
    }

    /**
     * Begins an envoy with the specified parameters
     *
     * @see Envoy
     * @param duration Duration for the envoy
     * @param world World that the envoy drops in
     * @param region Region for the envoy drops
     * @param numDrops Number of crates to spawn
     * @throws IllegalArgumentException Invalid world or region name
     */
    public void beginEnvoy(int duration, String world, String region, int numDrops) throws IllegalArgumentException {
        endEnvoyCountdown();
        currentEnvoy = new Envoy(plugin)
                .setEnvoyDuration(duration)
                .setEnvoyWorld(world)
                .setEnvoyRegion(region)
                .setEnvoyDropCount(numDrops);

        currentEnvoy.begin();
    }

    /**
     * @return Optional instance of Envoy
     * @see Envoy
     */
    public Optional<Envoy> getCurrentEnvoy() {
        return currentEnvoy == null ? Optional.empty() : Optional.of(currentEnvoy);
    }
}
