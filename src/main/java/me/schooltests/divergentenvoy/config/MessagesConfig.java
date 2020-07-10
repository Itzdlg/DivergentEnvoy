package me.schooltests.divergentenvoy.config;

import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;

public class MessagesConfig extends BaseConfig<DivergentEnvoy> {
    public MessagesConfig(DivergentEnvoy plugin) {
        super(plugin, "messages");
    }
    public void postLoad() { }


}
