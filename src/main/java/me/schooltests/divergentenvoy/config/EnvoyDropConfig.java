package me.schooltests.divergentenvoy.config;

import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;

public class EnvoyDropConfig extends BaseConfig<DivergentEnvoy> {
    public EnvoyDropConfig(DivergentEnvoy plugin, String crateName) {
        super(plugin, "/" + plugin.getPluginConfig().ENVOY_FOLDER + "/" + crateName.toLowerCase());
    }

    public void postLoad() { }
}