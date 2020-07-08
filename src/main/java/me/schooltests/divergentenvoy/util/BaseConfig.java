package me.schooltests.divergentenvoy.util;

import com.sun.istack.internal.NotNull;
import com.sun.istack.internal.Nullable;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public abstract class BaseConfig <T extends JavaPlugin> {
    private T plugin;
    private String fileName = "config.yml";
    private String targetSection = "";
    private final YamlConfiguration defaultConfig = new YamlConfiguration();
    private final YamlConfiguration yamlConfig = new YamlConfiguration();

    public BaseConfig(T plugin) {
        this.plugin = plugin;
    }

    public BaseConfig(T plugin, String fileName) {
        this.plugin = plugin;
        this.fileName = fileName.endsWith(".yml") ? fileName : fileName + ".yml";
    }

    public T getPlugin() {
        return plugin;
    }

    public YamlConfiguration getDefaultConfig() {
        return defaultConfig;
    }

    public YamlConfiguration getYamlConfig() {
        return yamlConfig;
    }

    public final void loadConfigFile() throws ConfigurationException {
        final File file = new File(plugin.getDataFolder(), fileName);
        if (!file.exists()) {
            final boolean success = file.getParentFile().mkdirs();
            if (success) plugin.saveResource(fileName, false);
            else throw new ConfigurationException("Unable to create "  + fileName + "file");
        }

        try {
            yamlConfig.load(file);
            defaultConfig.load(new InputStreamReader(Objects.requireNonNull(plugin.getResource(fileName))));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }

        postLoad();
    }

    abstract public void postLoad();

    public void setTargetSection(@Nullable String configSection) {
        if (configSection != null && !configSection.isEmpty()) {
            targetSection = targetSection.endsWith(".") ? targetSection : targetSection + ".";
        } else {
            targetSection = "";
        }
    }

    public String getStringWithArguments(@NotNull String node, @NotNull Map<String, String> arguments) {
        String unformatted = String.valueOf(getOrDefault(targetSection + node));
        if (unformatted == null) return "";
        for (String key : arguments.keySet())
            unformatted = unformatted.replaceAll("\\{" + key + "}", arguments.get(key));

        return unformatted;
    }

    public Object getOrDefault(String node) {
        return yamlConfig.get(targetSection + node, defaultConfig.get(targetSection + node, null));
    }

    public String getStringOrDefault(String node) {
        return String.valueOf(getOrDefault(node));
    }

    public int getIntOrDefault(String node) {
        return yamlConfig.getInt(targetSection + node, defaultConfig.getInt(targetSection + node, 0));
    }

    public List<Integer> getIntegerListOrDefault(String node) {
        List<Integer> result = yamlConfig.getIntegerList(targetSection + node);
        return result.isEmpty() ? defaultConfig.getIntegerList(targetSection + node) : result;
    }

    public double getDoubleOrDefault(String node) {
        return yamlConfig.getDouble(targetSection + node, defaultConfig.getDouble(targetSection + node, 0));
    }

    public boolean getBooleanOrDefault(String node) {
        return yamlConfig.getBoolean(targetSection + node, defaultConfig.getBoolean(targetSection + node, true));
    }
}

class ConfigurationException extends RuntimeException {
    public ConfigurationException(String message) {
        super(message);
    }
}