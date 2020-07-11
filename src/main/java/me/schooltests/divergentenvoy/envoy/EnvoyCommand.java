package me.schooltests.divergentenvoy.envoy;

import com.sk89q.worldguard.bukkit.WorldGuardPlugin;
import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;
import me.schooltests.divergentenvoy.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class EnvoyCommand implements CommandExecutor {
    private DivergentEnvoy plugin;
    public EnvoyCommand(DivergentEnvoy plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        switch (args[0].toLowerCase()) {
            case "begin":
            case "start":
                if (plugin.getAPI().getCurrentEnvoy().isPresent())
                    plugin.getAPI().getCurrentEnvoy().get().end();

                int duration = plugin.getPluginConfig().ENVOY_DURATION;
                String world = plugin.getPluginConfig().ENVOY_DROP_WORLD;
                String region = plugin.getPluginConfig().ENVOY_DROP_REGION;
                Integer numDrops = null;

                if (args.length >= 2) {
                    try {
                        int result = TimeUtil.getSecondsFromTimestamp(args[1], ",");
                        if (result >= 15) duration = result;
                    } catch (NumberFormatException e) {
                        sender.sendMessage(plugin.getMessagesConfig().COMMAND_INVALID_ARG("duration", "timestamp"));
                    }
                }

                if (args.length >= 3 && Bukkit.getWorlds().stream().noneMatch(w -> w.getName().equalsIgnoreCase(args[2])))
                    world = args[2].toLowerCase();

                if (args.length >= 4 && WorldGuardPlugin.inst().getRegionManager(Bukkit.getWorld(world)).hasRegion(args[3]))
                    region = args[3].toLowerCase();

                try {
                    if (args.length >= 5 && Integer.parseInt(args[4]) >= 1)
                        numDrops = Integer.parseInt(args[4]);
                } catch (NumberFormatException e) {
                    sender.sendMessage(plugin.getMessagesConfig().COMMAND_INVALID_ARG("# of crates", "number"));
                }

                if (numDrops == null) plugin.getAPI().beginEnvoy(duration, world, region);
                else plugin.getAPI().beginEnvoy(duration, world, region, numDrops);

                sender.sendMessage(plugin.getMessagesConfig().COMMAND_START_ENVOY(duration, world, region));
                break;
            case "end":
                if (plugin.getAPI().getCurrentEnvoy().isPresent()) {
                    Bukkit.broadcastMessage(plugin.getMessagesConfig().ENVOY_END_BROADCAST());
                    plugin.getAPI().getCurrentEnvoy().get().end();
                } else {
                    sender.sendMessage(plugin.getMessagesConfig().COMMAND_NO_ACTIVE_ENVOY());
                }

                break;
            case "reload":
                try {
                    plugin.getPluginConfig().loadConfigFile();
                    plugin.getMessagesConfig().loadConfigFile();

                    sender.sendMessage(ChatColor.RED + "Configuration reloaded");
                } catch (BaseConfig.ConfigurationException e) {
                    sender.sendMessage(ChatColor.RED + "Something went terribly wrong! Tell an administrator to check server logs.");
                    e.printStackTrace();
                }

                break;
            case "help":
            default:
                sender.sendMessage(plugin.getMessagesConfig().PREFIX() + " &7(1/1)");
                sender.sendMessage(plugin.getMessagesConfig().HELP_MESSAGE());
        }

        return true;
    }
}