package me.schooltests.divergentenvoy.config;

import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;

import java.util.List;

public class PluginConfig extends BaseConfig<DivergentEnvoy> {
    public String LANG_FILE;
    public String ENVOY_FOLDER;
    public String ENVOY_DROP_REGION;
    public int ENVOY_DURATION;
    public int PLAYERS_REQUIRED;
    public double ENVOY_DROP_RATIO;
    public boolean RATIO_ROUND_UP;
    public List<Integer> WARNING_TIMES;

    public PluginConfig(DivergentEnvoy plugin) {
        super(plugin, "config");
        loadConfigFile();
    }

    public void postLoad() {
        setTargetSection("file-system");
        LANG_FILE = getStringOrDefault("lang-file");
        ENVOY_FOLDER = getStringOrDefault("envoy-folder");

        setTargetSection("envoy");
        ENVOY_DROP_REGION = getStringOrDefault("region");
        ENVOY_DURATION = getIntOrDefault("duration");

        setTargetSection("envoy.spawning");
        PLAYERS_REQUIRED = getIntOrDefault("players-required");
        ENVOY_DROP_RATIO = getDoubleOrDefault("ratio");
        RATIO_ROUND_UP = getBooleanOrDefault("round-up");

        setTargetSection("warning-times");
        WARNING_TIMES = getIntegerListOrDefault("warning-times");

        setTargetSection(null);
    }
}
