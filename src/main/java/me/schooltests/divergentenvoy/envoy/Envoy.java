package me.schooltests.divergentenvoy.envoy;

import com.sk89q.worldguard.bukkit.BukkitUtil;
import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import com.sk89q.worldguard.protection.regions.ProtectedRegion;
import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.DivergentEnvoyAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerArmorStandManipulateEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class Envoy {
    private DivergentEnvoy plugin;
    private DivergentEnvoyAPI API;

    private final Set<EnvoyDrop> envoyDrops = new HashSet<>();
    private final Map<EnvoyDrop, ArmorStand> envoyHolograms = new HashMap<>();
    private BukkitTask endCountdown;
    private Listener envoyListener;
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
            final EnvoyDrop drop = getEnvoyDropFromChance();
            assert drop != null;

            Location randomLocation;
            do {
                final Location minLoc = BukkitUtil.toLocation(world, region.getMinimumPoint());
                final Location maxLoc = BukkitUtil.toLocation(world, region.getMaximumPoint());
                randomLocation = new Location(world, random.nextInt(minLoc.getBlockX(), maxLoc.getBlockX()), 0, random.nextInt(minLoc.getBlockZ(), maxLoc.getBlockZ()));
            } while (!region.contains(BukkitUtil.toVector(randomLocation)));

            final Location dropLocation = world.getHighestBlockAt(randomLocation).getLocation();
            world.spawnFallingBlock(addVector(dropLocation, 0, drop.DROP_HEIGHT, 0), drop.BLOCK_MATERIAL, (byte) 0x0);
            drop.setDropLocation(dropLocation);

            if (!drop.HOLOGRAM.equalsIgnoreCase("") && !drop.HOLOGRAM.equalsIgnoreCase("none")) {
                ArmorStand armorStand = (ArmorStand) world.spawnEntity(addVector(dropLocation, .5, 0, .5), EntityType.ARMOR_STAND);
                armorStand.setGravity(false);
                armorStand.setCanPickupItems(false);
                armorStand.setCustomName(ChatColor.translateAlternateColorCodes('&', drop.HOLOGRAM));
                armorStand.setCustomNameVisible(true);
                armorStand.setVisible(false);
                armorStand.setSmall(true);
                envoyHolograms.put(drop, armorStand);
            }

            envoyDrops.add(drop);
            numLeft -= 1;
        }

        envoyListener = new Listener() {
            @EventHandler
            public void onClick(PlayerInteractEvent e) {
                if ((e.getAction() == Action.LEFT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_BLOCK) && e.getPlayer().getWorld().getUID().equals(world.getUID())) {
                    for (EnvoyDrop drop : envoyDrops) {
                        if (e.getClickedBlock().getType().equals(drop.BLOCK_MATERIAL)
                                && e.getClickedBlock().getLocation().equals(drop.getDropLocation())) {
                            for (int i = 0; i < drop.NUM_REWARDS; i++)
                                drop.claimReward(e.getPlayer());

                            if (drop.BROADCAST_CLAIM) Bukkit.broadcastMessage(plugin.getMessagesConfig().ENVOY_CLAIM_BROADCAST(e.getPlayer()));

                            if (envoyHolograms.containsKey(drop)) {
                                envoyHolograms.get(drop).remove();
                                envoyHolograms.remove(drop);
                            }

                            drop.getDropLocation().getBlock().setType(Material.AIR);
                            envoyDrops.remove(drop);

                            if (envoyDrops.isEmpty()) {
                                Bukkit.broadcastMessage(plugin.getMessagesConfig().ENVOY_END_BROADCAST());
                                end();
                            }

                            e.setCancelled(true);
                            break;
                        }
                    }
                }
            }

            @EventHandler
            public void hologramInteract(PlayerArmorStandManipulateEvent e) {
                if (envoyHolograms.containsValue(e.getRightClicked())) e.setCancelled(true);
            }
        };

        Bukkit.getPluginManager().registerEvents(envoyListener, plugin);
        endCountdown = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Bukkit.broadcastMessage(plugin.getMessagesConfig().ENVOY_END_BROADCAST());
            end();
        }, duration * 20);
    }

    public void end() {
        end(true);
    }

    public void end(boolean startAgain) {
        for (EnvoyDrop drop : envoyDrops) {
            world.getBlockAt(drop.getDropLocation()).setType(Material.AIR);
            if (envoyHolograms.containsKey(drop)) envoyHolograms.get(drop).remove();
        }

        envoyDrops.clear();
        envoyHolograms.clear();

        HandlerList.unregisterAll(envoyListener);
        endCountdown.cancel();
        if (startAgain) plugin.getAPI().beginEnvoyCountdown();
    }

    private int getNumDrops() {
        if (numDrops == 0) {
            final int numPlayers = Bukkit.getOnlinePlayers().size();
            final double ratio = plugin.getPluginConfig().ENVOY_DROP_RATIO;
            final double unrounded = numPlayers * ratio;

            return (int) (unrounded > 0 ? Math.ceil(unrounded) : 1);
        } else return numDrops;
    }

    private Location addVector(Location l, double x, double y, double z) {
        return new Location(l.getWorld(), l.getBlockX() + x, l.getBlockY() + y, l.getBlockZ() + z);
    }

    private EnvoyDrop getEnvoyDropFromChance() {
        ThreadLocalRandom random = ThreadLocalRandom.current();
        Map<EnvoyDrop, Integer> chances = new HashMap<>();
        File dir = new File(plugin.getDataFolder().toString() + File.separator + "crates");
        File[] ymlFiles = dir.listFiles((pathname -> pathname.getName().endsWith(".yml")));
        if (ymlFiles != null) {
            for (File ymlFile : ymlFiles) {
                String fileName = ymlFile.getName().replace(".yml", "");
                EnvoyDrop envoyDrop = new EnvoyDrop(plugin, fileName);
                chances.put(envoyDrop, (int) (envoyDrop.CHANCE * 100));
            }

            int total = chances.values().stream().mapToInt(Integer::intValue).sum();
            int count = random.nextInt(total) + 1;

            while (count > 0) {
                for (EnvoyDrop drop : chances.keySet()) {
                    count -= chances.get(drop);
                    if (count <= 0) return drop;
                }
            }

        }

        return null;
    }
}
