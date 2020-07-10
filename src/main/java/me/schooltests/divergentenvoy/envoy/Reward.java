package me.schooltests.divergentenvoy.envoy;

import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public final class Reward {
    private String rewardID;
    private double chance;
    private final List<ItemStack> itemRewards = new ArrayList<>();
    private final List<String> commandRewards = new ArrayList<>();
    private final List<PotionEffect> effectRewards = new ArrayList<>();

    public Reward(String rewardID, double chance) {
        this.rewardID = rewardID;
        this.chance = chance;
    }

    public String getRewardID() {
        return rewardID;
    }

    public double getChance() {
        return chance;
    }

    public List<ItemStack> getItemRewards() {
        return itemRewards;
    }

    public List<String> getCommandRewards() {
        return commandRewards;
    }

    public List<PotionEffect> getEffectRewards() {
        return effectRewards;
    }

    public void add(ItemStack... items) {
        itemRewards.addAll(Arrays.asList(items));
    }

    public void add(String... commands) {
        commandRewards.addAll(Arrays.asList(commands));
    }

    public void add(PotionEffect... potionEffects) {
        effectRewards.addAll(Arrays.asList(potionEffects));
    }
}
