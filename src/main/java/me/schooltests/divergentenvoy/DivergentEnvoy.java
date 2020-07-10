package me.schooltests.divergentenvoy;

import me.schooltests.divergentenvoy.config.MessagesConfig;
import me.schooltests.divergentenvoy.config.PluginConfig;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

public class DivergentEnvoy extends JavaPlugin {
    private final PluginConfig pluginConfig = new PluginConfig(this);
    private final MessagesConfig messagesConfig = new MessagesConfig(this);
    private DivergentEnvoyAPI API;

    @Override
    public void onEnable() {
        Bukkit.getServicesManager().register(DivergentEnvoy.class, this, this, ServicePriority.Normal);
        API = new DivergentEnvoyAPI(this);

        Bukkit.getPluginManager().registerEvents(new Listener() {
            @EventHandler
            public void onJoin(PlayerJoinEvent e) {
                Bukkit.getScheduler().runTaskLater(Bukkit.getServicesManager().getRegistration(DivergentEnvoy.class).getPlugin(), () -> {
                    API.beginEnvoy(pluginConfig.ENVOY_DURATION, pluginConfig.ENVOY_DROP_WORLD, pluginConfig.ENVOY_DROP_REGION);
                    }, 40);
            }
        }, this);
    }

    public PluginConfig getPluginConfig() {
        return pluginConfig;
    }

    public MessagesConfig getMessagesConfig() {
        return messagesConfig;
    }

    public DivergentEnvoyAPI getAPI() {
        return API;
    }
}