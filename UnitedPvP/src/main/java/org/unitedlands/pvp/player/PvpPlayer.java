package org.unitedlands.pvp.player;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.UnitedPvP;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class PvpPlayer {
    private final UnitedPvP unitedPvP = getPlugin();
    private final OfflinePlayer player;
    private final File file;
    private final FileConfiguration playerConfig;

    public PvpPlayer(@NotNull Player player) {
        this.player = player;
        file = null;
        playerConfig = getFileConfiguration();
    }

    public PvpPlayer(OfflinePlayer player) {
        this.player = player;
        file = null;
        playerConfig = getFileConfiguration();
    }

    public void createFile() {
        File playerDataFile = getPlayerFile();
        if (!playerDataFile.exists()) {
            playerDataFile.getParentFile().mkdirs();
            try {
                playerDataFile.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(playerDataFile);
            fileConfiguration.set("name", player.getName());
            fileConfiguration.set("hostility", 1);
            fileConfiguration.set("status", Status.DEFENSIVE.toString());
            fileConfiguration.set("last-hostility-change-time", System.currentTimeMillis());
            fileConfiguration.set("can-degrade", true);
            fileConfiguration.set("immunity-time", System.currentTimeMillis());
            saveConfig(fileConfiguration);
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    private String getFilePath() {
        return File.separator + "players" + File.separator + player.getUniqueId() + ".yml";
    }

    public void saveConfig(FileConfiguration fileConfig) {
        try {
            fileConfig.save(getPlayerFile());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public FileConfiguration getFileConfiguration() {
        File playerStatsFile = getPlayerFile();
        FileConfiguration fileConfiguration = new YamlConfiguration();
        try {
            fileConfiguration.load(playerStatsFile);
        } catch (IOException | InvalidConfigurationException e) {
            createFile();
            return getFileConfiguration();
        }
        return fileConfiguration;
    }

    public void updatePlayerHostility() {
        if (isHostile()) {
            setStatus(getStatus());
            updateLastHostilityChangeTime();
            return;
        }
        if (isDegradable()) {
            if (isAggressive()) {
                setHostility(getHostility() - 1);
                setStatus(getStatus());
                return;
            }
            if (isDefensive()) {
                if (getHostility() == 1) {
                    return;
                }
                setHostility(getHostility() - 1);
                setStatus(getStatus());
            }
        }
    }
    public int getHostility() {
        return playerConfig.getInt("hostility");
    }

    public void setHostility(int value) {
        if (value >= 21) {
            playerConfig.set("hostility", 21);
            saveConfig(playerConfig);
            return;
        }
        // Don't go lower than 1
        playerConfig.set("hostility", Math.max(value, 1));
        // Update the time.
        updateLastHostilityChangeTime();
        saveConfig(playerConfig);
    }

    public void setStatus(Status status) {
        playerConfig.set("status", status.toString());
        saveConfig(playerConfig);
    }
    public boolean isDefensive() {
        return getStatus().equals(Status.DEFENSIVE);
    }
    public boolean isHostile() {
        return getStatus().equals(Status.HOSTILE);
    }
    public boolean isAggressive() {
        return getStatus().equals(Status.AGGRESSIVE);
    }
    public Status getStatus() {
        int hostility = getHostility();
        if (hostility >= Status.HOSTILE.getStartingValue()) {
            return Status.HOSTILE;
        } else if (hostility >= Status.AGGRESSIVE.getStartingValue()) {
            return Status.AGGRESSIVE;
        } else {
            return Status.DEFENSIVE;
        }
    }

    public String getIconHex(int hostility) {
        FileConfiguration config = unitedPvP.getConfig();
        return config.getString("hostility-color-stages." + hostility);
    }

    public boolean isDegradable() {
        return playerConfig.getBoolean("can-degrade");
    }

    public void setDegradable(boolean value) {
        playerConfig.set("can-degrade", value);
        saveConfig(playerConfig);
    }
    public static UnitedPvP getPlugin() {
        return (UnitedPvP) Bukkit.getPluginManager().getPlugin("UnitedPvP");
    }

    @NotNull
    public File getPlayerFile() {
        if (this.file != null) {
            return file;
        }
        return new File(unitedPvP.getDataFolder(), getFilePath());
    }

    public void updateLastHostilityChangeTime() {
        playerConfig.set("last-hostility-change-time", System.currentTimeMillis());
        saveConfig(playerConfig);
    }

    public boolean isImmune() {
        // If the time is 0, it was force set by the plugin
        // so its disabled.
        if (getImmunityTime() == 0) {
            return false;
        }
        // if the time passed is bigger than 1 day (in millis)
        // then their immunity stamp is no longer valid.
        return getImmunityTime() < TimeUnit.DAYS.toMillis(1);
    }

    public long getImmunityTime() {
        return System.currentTimeMillis() - playerConfig.getLong("immunity-time");
    }
    
    public void expireImmunity() {
        playerConfig.set("immunity-time", 0);
        saveConfig(playerConfig);
    }

    public long getLastHostilityChangeTime() {
        return playerConfig.getLong("last-hostility-change-time");
    }
}
