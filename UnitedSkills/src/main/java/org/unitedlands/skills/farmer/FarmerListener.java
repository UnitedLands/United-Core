package org.unitedlands.skills.farmer;

import com.destroystokyo.paper.ParticleBuilder;
import com.gamingmesh.jobs.Jobs;
import com.gamingmesh.jobs.container.JobProgression;
import com.gamingmesh.jobs.container.JobsPlayer;
import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import de.Linus122.SafariNet.Main;
import dev.lone.itemsadder.api.CustomCrop;
import dev.lone.itemsadder.api.CustomStack;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextComponent;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.block.Block;
import org.bukkit.block.data.Ageable;
import org.bukkit.block.data.BlockData;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Cow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockDropItemEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.PlayerInteractAtEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.skill.ActiveSkill;
import org.unitedlands.skills.skill.Skill;
import org.unitedlands.skills.skill.SkillType;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class FarmerListener implements Listener {

    private final UnitedSkills unitedSkills;
    private Player player;
    TownyAPI towny = TownyAPI.getInstance();
    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final HashMap<UUID, Long> durations = new HashMap<>();

    public FarmerListener(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }

    @EventHandler
    public void onInteractWithHoe(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        ActiveSkill skill = new ActiveSkill(player, SkillType.GREEN_THUMB, cooldowns, durations);
        if (skill.getLevel() == 0) {
            return;
        }
        if (!player.getInventory().getItemInMainHand().getType().toString().contains("HOE")) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        if (skill.isActive()) {
            return;
        }
        skill.activate();
    }

    @EventHandler
    public void onCropPlant(BlockPlaceEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.FERTILISER);
        if (skill.getLevel() == 0) {
            return;
        }
        Block block = event.getBlock();
        if (!isCrop(block.getType())) {
            return;
        }
        Ageable crop = (Ageable) block.getBlockData();
        CustomStack customStack = CustomStack.byItemStack(event.getItemInHand());

        ParticleBuilder greenParticle = new ParticleBuilder(Particle.VILLAGER_HAPPY);
        greenParticle.count(25).location(block.getLocation().toCenterLocation());

        if (skill.isSuccessful()) {
            int newAge = Math.min(skill.getLevel() + 1, crop.getMaximumAge() - 1);
            if (customStack != null) {
                unitedSkills.getServer().getScheduler().runTask(unitedSkills, () -> {
                    CustomCrop customCrop = CustomCrop.byAlreadyPlaced(block);
                    if (customCrop != null) {
                        final int age = Math.min(skill.getLevel() + 1, customCrop.getMaxAge() - 1);
                        customCrop.setAge(age);
                    }
                    greenParticle.spawn();
                });
                return;
            }
            crop.setAge(newAge);
            block.setBlockData(crop);
            greenParticle.spawn();
        }

    }

    @EventHandler
    public void onEntityInteract(PlayerInteractAtEntityEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.FUNGAL);
        if (!skill.isMaxLevel()) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        Entity entity = event.getRightClicked();
        if (!(entity instanceof Cow)) {
            return;
        }
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        ItemStack handItem = player.getInventory().getItemInMainHand();

        if (isHoldingMushrooms(handItem, offhandItem) && isInOwnTownOrWilderness()) {
            Location location = entity.getLocation();
            sendMushroomParticles(entity.getLocation());
            entity.remove();
            entity.getWorld().spawnEntity(location, EntityType.MUSHROOM_COW);
            runFungalSkill(offhandItem, handItem);
        }
    }

    @EventHandler
    public void onBlockInteract(PlayerInteractEvent event) {
        player = event.getPlayer();
        if (!isFarmer()) {
            return;
        }
        Skill skill = new Skill(player, SkillType.FUNGAL);
        if (skill.getLevel() == 0) {
            return;
        }
        if (!player.isSneaking()) {
            return;
        }
        Block block = event.getClickedBlock();
        if (block == null) {
            return;
        }
        ItemStack offhandItem = player.getInventory().getItemInOffHand();
        ItemStack handItem = player.getInventory().getItemInMainHand();
        if (block.getType().equals(Material.GRASS_BLOCK) || block.getType().equals(Material.DIRT)) {
            if (isHoldingMushrooms(handItem, offhandItem) && isInOwnTownOrWilderness()) {
                event.setCancelled(true);
                sendMushroomParticles(block.getLocation());
                block.setType(Material.MYCELIUM);
                runFungalSkill(offhandItem, handItem);
            }
        }
    }

    private void runFungalSkill(ItemStack offhandItem, ItemStack handItem) {
        if (offhandItem.getAmount() > 1) {
            offhandItem.setAmount(offhandItem.getAmount() - 1);
            player.getInventory().setItemInOffHand(offhandItem);
        } else {
            player.getInventory().setItemInOffHand(new ItemStack(Material.AIR));
        }
        if (handItem.getAmount() > 1) {
            handItem.setAmount(handItem.getAmount() - 1);
        } else {
            player.getInventory().remove(handItem);
        }
    }

    private void sendMushroomParticles(Location location) {
        ParticleBuilder mushroomParticles = new ParticleBuilder(Particle.BLOCK_CRACK);
        BlockData mushroomData = Material.RED_MUSHROOM.createBlockData();
        mushroomParticles
                .data(mushroomData)
                .count(100)
                .offset(0, 1, 0)
                .location(location)
                .spawn();
    }

    private boolean isHoldingMushrooms(ItemStack handItem, ItemStack offhandItem ) {
        final Material offhandItemType = offhandItem.getType();
        final Material handItemType = handItem.getType();
        if (offhandItemType.equals(Material.RED_MUSHROOM) && handItemType.equals(Material.BROWN_MUSHROOM)) {
            return true;
        } else if (offhandItemType.equals(Material.BROWN_MUSHROOM) && handItemType.equals(Material.RED_MUSHROOM)) {
            return true;
        }
        return false;
    }

    private boolean isInOwnTownOrWilderness() {
        Resident resident = towny.getResident(player);
        Location location = player.getLocation();
        if (towny.isWilderness(location)) {
            return true;
        }
        Town town = towny.getTownBlock(location).getTownOrNull();
        TextComponent canOnlyUseInTown = Component.text("You can only use this skill in your town!", NamedTextColor.RED);

        if (resident.getTownOrNull() == null) {
            player.sendActionBar(canOnlyUseInTown);
            return false;
        }
        boolean isInTown = resident.getTownOrNull().equals(town);
        if (!isInTown) {
            player.sendActionBar(canOnlyUseInTown);
        }
        return isInTown;
    }

    @EventHandler
    public void onItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        player = event.getPlayer();
        if (!isPlantFood(item.getType())) {
            return;
        }
        if (!isFarmer()) {
            return;
        }
        Skill skill  = new Skill(player, SkillType.VEGETARIAN);
        int level = skill.getLevel();
        if (level == 0) {
            return;
        }
        float saturation = player.getSaturation();
        int foodLevel = player.getFoodLevel();
        player.setSaturation(saturation + level);
        player.setFoodLevel(foodLevel + level);
    }

    @EventHandler
    public void onCropBreak(BlockBreakEvent event) {
        Material material = event.getBlock().getType();
        player = event.getPlayer();
        if (!isCrop(material)) {
            return;
        }
        if (!isFarmer()) {
            return;
        }
        ActiveSkill skill  = new ActiveSkill(player, SkillType.GREEN_THUMB, cooldowns, durations);
        int level = skill.getLevel();
        if (level == 0) {
            return;
        }
        Block block = event.getBlock();
        BlockData dataPlant = block.getBlockData();
        if (!(dataPlant instanceof Ageable plant))  {
            return;
        }
        CustomCrop customCrop = CustomCrop.byAlreadyPlaced(block);
        if (skill.isActive()) {
            if (customCrop != null) {
                unitedSkills.getServer().getScheduler().runTask(unitedSkills, () -> {
                    CustomStack customCropSeed = customCrop.getSeed();
                    ItemStack item = customCropSeed.getItemStack();
                    if (Utils.takeItem(player, item)) {
                        CustomCrop.place(customCropSeed.getNamespacedID(), block.getLocation());
                        player.getInventory().addItem(item);
                        }
                });
                return;
            }
            ItemStack item = new ItemStack(getCropSeeds(material));
            if (Utils.takeItem(player, item)) {
                unitedSkills.getServer().getScheduler().runTask(unitedSkills, () -> {
                    block.setType(plant.getMaterial());
                    plant.setAge(0);
                    block.setBlockData(plant);
                });
            }
        }
    }

    private Material getCropSeeds(@NotNull Material material) {
        if (material == Material.POTATOES) return Material.POTATO;
        if (material == Material.CARROTS) return Material.CARROT;
        if (material == Material.BEETROOTS) return Material.BEETROOT_SEEDS;
        if (material == Material.WHEAT) return Material.WHEAT_SEEDS;
        if (material == Material.MELON_STEM) return Material.MELON_SEEDS;
        if (material == Material.PUMPKIN_STEM) return Material.PUMPKIN_SEEDS;
        return material;
    }

    @EventHandler
    public void onCropDrop(BlockDropItemEvent event) {
        @NotNull Material material = event.getBlockState().getType();
        player = event.getPlayer();
        if (!isCrop(material)) {
            return;
        }
        if (!isFarmer()) {
            return;
        }

        ActiveSkill skill = new ActiveSkill(player, SkillType.GREEN_THUMB, cooldowns, durations);

        if (skill.isSuccessful() && skill.isActive()) {
            if (!isMaxAge(event.getBlock())) {
                return;
            }
            for (Item item : event.getItems()) {
                Utils.multiplyItem(player, item.getItemStack(), 2);
            }
        }

        Skill expertHarvester = new Skill(player, SkillType.EXPERT_HARVESTER);
        if (expertHarvester.isSuccessful()) {
            for (Item item : event.getItems()) {
                if (item.getName().contains("Seeds")) {
                    return;
                }
                if (!isMaxAge(event.getBlock())) {
                    return;
                }
                Utils.multiplyItem(player, item.getItemStack(), 1);
            }
        }
    }

    private boolean isMaxAge(@NotNull Block block) {
        BlockData dataPlant = block.getBlockData();
        if (!(dataPlant instanceof Ageable plant))  {
            return false;
        }
        if (!isCrop(block.getType())) {
            return false;
        }
        return plant.getAge() == plant.getMaximumAge();
    }

    private boolean isCrop(@NotNull Material material) {
        FileConfiguration configuration = getConfig();
        List<String> cropNames = configuration.getStringList("crop-names");
        return cropNames.contains(material.toString());
    }

    private boolean isPlantFood(Material material) {
        FileConfiguration configuration = getConfig();
        return configuration.getStringList("plant-foods").contains(material.toString());
    }

    private boolean isFarmer() {
        JobsPlayer jobsPlayer = Jobs.getPlayerManager().getJobsPlayer(player);
        for (JobProgression job : jobsPlayer.getJobProgression()) {
            return job.getJob().getName().equals("Farmer");
        }
        return false;
    }
    @NotNull
    private FileConfiguration getConfig() {
        return unitedSkills.getConfig();
    }

}
