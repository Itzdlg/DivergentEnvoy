package me.schooltests.divergentenvoy.config;

import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;
import me.schooltests.divergentenvoy.util.TimeUtil;

import java.util.ArrayList;
import java.util.List;

public class PluginConfig extends BaseConfig<DivergentEnvoy> {
    public String ENVOY_DROP_WORLD;
    public String ENVOY_DROP_REGION;
    public int ENVOY_COOLDOWN;
    public int ENVOY_DURATION;
    public int PLAYERS_REQUIRED;
    public double ENVOY_DROP_RATIO;
    public boolean RATIO_ROUND_UP;
    public List<Integer> WARNING_TIMES;

    public PluginConfig(DivergentEnvoy plugin) {
        super(plugin, "config");
    }

    public void postLoad() {
        setTargetSection("envoy");
        ENVOY_DROP_WORLD = getStringOrDefault("world");
        ENVOY_DROP_REGION = getStringOrDefault("region");
        ENVOY_COOLDOWN = TimeUtil.getSecondsFromTimestamp(getStringOrDefault("cooldown"));
        ENVOY_DURATION = TimeUtil.getSecondsFromTimestamp(getStringOrDefault("duration"));

        setTargetSection("envoy.spawning");
        PLAYERS_REQUIRED = getIntOrDefault("players-required");
        ENVOY_DROP_RATIO = getDoubleOrDefault("ratio");
        RATIO_ROUND_UP = getBooleanOrDefault("round-up");

        setTargetSection(null);
        WARNING_TIMES = new ArrayList<>();
        for (String timestamp : getStringListOrDefault("warning-times")) {
            WARNING_TIMES.add(TimeUtil.getSecondsFromTimestamp(timestamp));
        }
    }
}
