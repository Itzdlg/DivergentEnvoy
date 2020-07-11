package me.schooltests.divergentenvoy;

import me.schooltests.divergentenvoy.config.MessagesConfig;
import me.schooltests.divergentenvoy.config.PluginConfig;
import me.schooltests.divergentenvoy.envoy.Envoy;
import me.schooltests.divergentenvoy.envoy.EnvoyCommand;
import me.schooltests.divergentenvoy.envoy.EnvoyDrop;
import org.bukkit.Bukkit;
import org.bukkit.plugin.ServicePriority;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Optional;

public class DivergentEnvoy extends JavaPlugin {
    private PluginConfig pluginConfig;
    private MessagesConfig messagesConfig;
    private DivergentEnvoyAPI API;

    @Override
    public void onEnable() {
        // Register instance of the plugin to the Bukkit Services Manager
        Bukkit.getServicesManager().register(DivergentEnvoy.class, this, this, ServicePriority.Normal);

        // Register default envoy crates
        new EnvoyDrop(this, "basic", "crates/basic.yml");
        new EnvoyDrop(this, "titan", "crates/titan.yml");

        // Load configs
        pluginConfig = new PluginConfig(this);
        messagesConfig = new MessagesConfig(this);

        // Load the API
        API = new DivergentEnvoyAPI(this);

        // Register ommands
        getCommand("envoy").setExecutor(new EnvoyCommand(this));

        // Start countdown
        API.beginEnvoyCountdown();
    }

    @Override
    public void onDisable() {
        // Take care of active envoy
        Optional<Envoy> currentEnvoy = API.getCurrentEnvoy();
        if (API.getCurrentEnvoy().isPresent())
            currentEnvoy.get().end(false);
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