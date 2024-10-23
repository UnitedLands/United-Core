package org.unitedlands.protection.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.object.TownBlock;
import java.util.List;

import com.palmergames.bukkit.towny.object.TownyPermission;
import com.palmergames.bukkit.towny.utils.PlayerCacheUtil;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.protection.UnitedProtection;

public class PlayerListener implements Listener {
    private final TownyAPI towny = TownyAPI.getInstance();
    private final UnitedProtection unitedProtection;
    private Player player;

    public PlayerListener(UnitedProtection unitedProtection) {
        this.unitedProtection = unitedProtection;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        this.player = event.getPlayer();
        Block block = event.getBlock();
        if (this.isProtectedBlock(block)) {
            this.player.sendMessage(this.getMessage("break-deny-message"));
            event.setCancelled(true);
        }

    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        this.player = event.getPlayer();
        Block block = event.getBlock();
        if (this.isProtectedBlock(block)) {
            this.player.sendMessage(this.getMessage("place-deny-message"));
            event.setCancelled(true);
        }

    }

    private int getProtectedY() {
        return this.getConfiguration().getInt("protect-anything-below");
    }

    private List<String> getWhitelistedBlocks() {
        return this.getConfiguration().getStringList("block-whitelist");
    }

    private @NotNull FileConfiguration getConfiguration() {
        return this.unitedProtection.getConfig();
    }

    private boolean isWhitelistedBlock(Block block) {
        return this.getWhitelistedBlocks().contains(block.getType().toString());
    }

    private boolean isInTown(Block block) {
        Location blockLocation = block.getLocation();
        if (this.towny.isWilderness(blockLocation)) return false;
        TownBlock townBlock = this.towny.getTownBlock(blockLocation);
        if (townBlock == null) return false;
        return PlayerCacheUtil.getCachePermission(this.player, blockLocation, block.getType(), TownyPermission.ActionType.DESTROY);
    }

    private String getMessage(String message) {
        message = this.getConfiguration().getString(message);
        assert message != null;
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private boolean isProtectedBlock(Block block) {
        int blockY = block.getLocation().getBlockY();
        String worldName = block.getWorld().getName();
        if (!worldName.equals("world_earth")) return false;
        if (this.isWhitelistedBlock(block)) return false;
        if (this.player.hasPermission("united.protection.bypass")) return false;

        return blockY < this.getProtectedY() && !this.isInTown(block);
    }
}
