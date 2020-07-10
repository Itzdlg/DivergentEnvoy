package me.schooltests.divergentenvoy.envoy;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.DivergentEnvoyAPI;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Envoy {
    private DivergentEnvoy plugin;
    private DivergentEnvoyAPI API;

    private final Set<EnvoyDrop> envoyDrops = new HashSet<>();
    private final Set<Listener> envoyListeners = new HashSet<>();
    private int numDrops = 0;
    private int duration;
    private World world;
    private ProtectedRegion region;

    public Envoy(DivergentEnvoy plugin) {
        this.plugin = plugin;
        this.API = plugin.getAPI();
    }

    public Envoy setEnvoyDuration(int seconds) {
        this.duration = seconds;
        return this;
    }

    public Envoy setEnvoyDropCount(int number) throws IllegalArgumentException {
        if (number >= 1) this.numDrops = number;
        else throw new IllegalArgumentException("A number over 0 must be provided!");
        return this;
    }

    public Envoy setEnvoyWorld(String world) throws IllegalArgumentException {
        final World w = Bukkit.getWorld(world);
        if (w == null) throw new IllegalArgumentException("A valid world name MUST be provided!");
        else this.world = w;

        return this;
    }

    public Envoy setEnvoyRegion(String region) {
        final ProtectedRegion r = WorldGuardPlugin.inst().getRegionManager(world).getRegion(region);
        if (r == null) throw new IllegalArgumentException("A valid region name MUST be provided!");
        else this.region = r;

        return this;
    }

    public void begin() {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        int numLeft = getNumDrops();
        while (numLeft > 0) {
            final EnvoyDrop drop = new EnvoyDrop(plugin, "basic");
            final Location l = new Location(world, random.nextInt(5, 30), 100 ,random.nextInt(5, 30));
            world.getBlockAt(l).setType(drop.BLOCK_MATERIAL);
            drop.setDropLocation(l);

            Listener claimListener = new Listener() {
                @EventHandler
                public void onClick(PlayerInteractEvent e) {
                    if (e.getAction() == Action.LEFT_CLICK_BLOCK
                            && e.getClickedBlock().getType().equals(drop.BLOCK_MATERIAL)
                            && e.getClickedBlock().getLocation().equals(drop.getDropLocation()))
                        for (int i = 0; i < drop.NUM_REWARDS; i++)
                            drop.claimReward(e.getPlayer());
                }
            };

            Bukkit.getPluginManager().registerEvents(claimListener, plugin);
            envoyListeners.add(claimListener);
            numLeft -= 1;
        }
    }

    public void end() {

    }

    private int getNumDrops() {
        if (numDrops == 0) {
            final int numPlayers = Bukkit.getOnlinePlayers().size();
            final double ratio = plugin.getPluginConfig().ENVOY_DROP_RATIO;
            final double unrounded = numPlayers * ratio;

            return (int) (unrounded > 0 ? Math.ceil(unrounded) : 1);
        } else return numDrops;
    }

    /*
    private @Nullable Reward getEnvoyDropFromChance(Set<String> envoyDrops) {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Map<String, Integer> chances = new HashMap<>();
        for (String drop : envoyDrops) {

        }

        int total = chances.values().stream().mapToInt(Integer::intValue).sum();
        int count = random.nextInt(total) + 1;

        while (count > 0) {
            for (Reward r : chances.keySet()) {
                count -= chances.get(r);
                if (count <= 0) return r;
            }
        }

        return null;
    }

     */
}
