package me.schooltests.divergentenvoy.config;

import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;
import me.schooltests.divergentenvoy.util.TimeUtil;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.HashMap;

public class MessagesConfig extends BaseConfig<DivergentEnvoy> {
    public MessagesConfig(DivergentEnvoy plugin) {
        super(plugin, "messages");
    }

    public void postLoad() { }

    private String c(String s) {
        return ChatColor.translateAlternateColorCodes('&', s);
    }

    public String PREFIX() { return c(getStringOrDefault("prefix")); }

    public String TIME_UNTIL_SPAWN(int seconds) {
        return PREFIX() + " " + c(getStringWithArguments("time-until-spawn", new HashMap<String, String>() {{
            put("time", TimeUtil.getTimestampFromSeconds(seconds));
        }}));
    }

    public String ENVOY_CLAIM_BROADCAST(Player player) {
        return PREFIX() + " " + c(getStringWithArguments("envoy-claim-broadcast", new HashMap<String, String>() {{
            put("player", player.getName());
            put("display_name", player.getDisplayName());
        }}));
    }

    public String ENVOY_START_BROADCAST() {
        return PREFIX() + " " + c(getStringOrDefault("envoy-begin-broadcast"));
    }

    public String ENVOY_END_BROADCAST() {
        return PREFIX() + " " + c(getStringOrDefault("envoy-end-broadcast"));
    }

    public String COMMAND_START_ENVOY(int seconds, String world, String region) {
        return PREFIX() + " " + c(getStringWithArguments("command-started-envoy", new HashMap<String, String>() {{
            put("duration", TimeUtil.getTimestampFromSeconds(seconds));
            put("world", world.toLowerCase());
            put("region", region.toLowerCase());
        }}));
    }

    public String COMMAND_NO_ACTIVE_ENVOY() {
        return PREFIX() + " " + c(getStringOrDefault("command-no-active-envoy"));
    }

    public String HELP_MESSAGE() {
        return c(getStringOrDefault("command-help-message"));
    }

    public String COMMAND_INVALID_ARG(String arg, String type) {
        return c(getStringWithArguments("command-invalid-arg", new HashMap<String, String>() {{
            put("arg", arg);
            put("type", type);
        }}));
    }
}
