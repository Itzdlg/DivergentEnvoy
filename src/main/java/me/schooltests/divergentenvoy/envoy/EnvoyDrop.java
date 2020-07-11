package me.schooltests.divergentenvoy.envoy;

import me.schooltests.divergentenvoy.DivergentEnvoy;
import me.schooltests.divergentenvoy.util.BaseConfig;
import me.schooltests.divergentenvoy.util.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;

public class EnvoyDrop {
    public String id;
    public DivergentEnvoy plugin;
    private BaseConfig<DivergentEnvoy> envoyDropConfig;

    public String FRIENDLY_NAME;
    public boolean BROADCAST_CLAIM;
    public int NUM_REWARDS;
    public double CHANCE;
    public Material BLOCK_MATERIAL;
    public int DROP_HEIGHT;
    public String HOLOGRAM;

    final Set<Reward> possibleRewards = new HashSet<>();
    private Location dropLocation;

    public EnvoyDrop(DivergentEnvoy plugin, String id) {
        this(plugin, id, "crates/basic.yml");
    }

    public EnvoyDrop(DivergentEnvoy plugin, String id, String defaultResourceName) {
        this.id = id;
        this.envoyDropConfig = new BaseConfig<DivergentEnvoy>(plugin, id, "crates", defaultResourceName) {
            @Override
            public void postLoad() {
                FRIENDLY_NAME = getStringOrDefault("friendly-name");
                BROADCAST_CLAIM = getBooleanOrDefault("broadcast-on-claim");
                CHANCE = getDoubleOrDefault("chance");
                NUM_REWARDS = getIntOrDefault("reward-count");

                setTargetSection("block");
                BLOCK_MATERIAL = Material.matchMaterial(getStringOrDefault("material"));
                DROP_HEIGHT = getIntOrDefault("drop-height");
                HOLOGRAM = getStringOrDefault("hologram");

                setTargetSection(null);
                if (BLOCK_MATERIAL == null) BLOCK_MATERIAL = Material.BEACON;
            }
        };

        calculatePossibleRewards();
    }

    public Location getDropLocation() {
        return dropLocation;
    }

    public void setDropLocation(Location dropLocation) {
        this.dropLocation = dropLocation;
    }

    public void claimReward(Player p) {
        Reward reward = getRewardFromChance(possibleRewards);
        assert reward != null;

        for (ItemStack i : reward.getItemRewards()) p.getInventory().addItem(i.clone());
        for (PotionEffect pE : reward.getEffectRewards()) p.addPotionEffect(pE);
        for (String command : reward.getCommandRewards()) {
            String finalCommand;
            finalCommand = command.replace("{player}", p.getName());

            // Replaces {item.reward.<>} placeholders
            if (envoyDropConfig.getYamlConfig().contains("rewards." + reward.getRewardID() + ".items")) {
                for (String ymlItem : envoyDropConfig.getDirectChildren("rewards." + reward.getRewardID() + ".items")) {
                    if (command.contains("{item." + ymlItem)) {
                        String placeholder = "{item." + ymlItem + ".";
                        for (ItemStack itemReward : reward.getItemRewards()) {
                            Material m = itemReward.getType();
                            Material m2 = Material.matchMaterial(envoyDropConfig.getYamlConfig().getString("rewards." + reward.getRewardID() + ".items." + ymlItem + ".type"));
                            if (m.equals(m2)) finalCommand = finalCommand.replace(placeholder + "amount}", String.valueOf(itemReward.getAmount()))
                                    .replace(placeholder + "name}", itemReward.getItemMeta().getDisplayName());
                        }
                    }
                }
            }

            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), finalCommand);
        }
    }

    private @Nullable Reward getRewardFromChance(Set<Reward> rewards) {
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        final Map<Reward, Integer> chances = new HashMap<>();
        for (Reward r : rewards) chances.put(r, (int) (r.getChance() * 100));

        int total = chances.values().stream().mapToInt(Integer::intValue).sum();
        int count = random.nextInt(total) + 1;

        while (count > 0) {
            for (Reward r : chances.keySet()) {
                count -= chances.get(r);
                if (count <= 0) return r;
            }
        }

        return null;
    }

    private void calculatePossibleRewards() {
        final Set<String> rewardIDs = envoyDropConfig.getDirectChildren("rewards");
        final YamlConfiguration yml = envoyDropConfig.getYamlConfig();
        for (String rewardID : rewardIDs) {
            String rewardSection = "rewards." + rewardID + ".";
            Reward reward = new Reward(rewardID, yml.getDouble(rewardSection + "chance", .5));
            if (yml.contains(rewardSection + "items")) {
                for (String item : yml.getConfigurationSection(rewardSection + "items").getKeys(false)) {
                    String itemSection = "rewards." + rewardID + ".items." + item + ".";
                    final int min = yml.getInt(itemSection + "min-amount", 32);
                    final int max = yml.getInt(itemSection + "max-amount", 64);
                    int amount;

                    if (yml.contains(itemSection + "amount")) amount = yml.getInt(itemSection + "amount");
                    else amount = ThreadLocalRandom.current().nextInt(min, max + 1);

                    final ItemStack itemStack = new ItemStack(Material.matchMaterial(yml.getString(itemSection + "type")), amount);
                    if (yml.contains(itemSection + "durability")) itemStack.setDurability((short) yml.getInt(itemSection + "durability"));
                    final ItemMeta meta = itemStack.getItemMeta();

                    if (yml.contains(itemSection + "name"))
                        meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', yml.getString(itemSection + "name")));

                    if (yml.contains(itemSection + "lore")) {
                        for (String ymlLore : yml.getStringList(itemSection + "lore"))
                            if (meta.hasLore()) meta.getLore().add(ChatColor.translateAlternateColorCodes('&', ymlLore));
                            else
                                meta.setLore(Collections.singletonList(ChatColor.translateAlternateColorCodes('&', ymlLore)));
                    }

                    if (yml.contains(itemSection + "enchants")) {
                        for (String ymlEnchant : yml.getStringList(itemSection + "enchants")) {
                            final String name = ymlEnchant.split(" ")[0];
                            final int power = Integer.parseInt(ymlEnchant.split(" ")[1]);
                            final Enchantment ench = Enchantment.getByName(name.toUpperCase().trim());
                            if (ench != null) meta.addEnchant(ench, power, true);
                        }
                    }

                    itemStack.setItemMeta(meta);
                    reward.add(itemStack);
                }
            }

            if (yml.contains(rewardSection + "effects")) {
                for (String ymlEffect : yml.getStringList(rewardSection + "effects")) {
                    final String name = ymlEffect.split(" ")[0];
                    final int amp = Integer.parseInt(ymlEffect.split(" ")[1]);
                    final int seconds = TimeUtil.getSecondsFromTimestamp(ymlEffect.split(" for ")[1]);
                    final PotionEffectType type = PotionEffectType.getByName(name.toLowerCase());
                    if (type != null) reward.add(new PotionEffect(type, seconds * 20, amp, true, true));
                }
            }

            if (yml.contains(rewardSection + "commands")) for (String ymlCommand : yml.getStringList(rewardSection + "commands")) reward.add(ymlCommand);

            possibleRewards.add(reward);
        }
    }
}