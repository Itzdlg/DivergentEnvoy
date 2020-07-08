package me.schooltests.divergentenvoy;

import me.schooltests.divergentenvoy.config.MessagesConfig;
import me.schooltests.divergentenvoy.config.PluginConfig;
import org.bukkit.plugin.java.JavaPlugin;

public class DivergentEnvoy extends JavaPlugin {
    private final PluginConfig pluginConfig = new PluginConfig(this);
    private final MessagesConfig messagesConfig = new MessagesConfig(this);
    private DivergentEnvoyAPI API;

    @Override
    public void onLoad() {
        API = new DivergentEnvoyAPI(this);
    }

    @Override
    public void onEnable() {
        
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