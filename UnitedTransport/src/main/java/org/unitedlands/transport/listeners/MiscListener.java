package org.unitedlands.transport.listeners;

import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;
import org.unitedlands.transport.UnitedTransport;

public class MiscListener implements Listener {

    private final UnitedTransport unitedTransport;


    public MiscListener(UnitedTransport unitedTransport) {
        this.unitedTransport = unitedTransport;
    }

    @EventHandler
    public void onPlayerReachBorder(PlayerMoveEvent event) {
        if (!event.hasChangedBlock())
            return;
        if (isEarthWorld(event.getTo().getWorld())) {
            return;
        }
        Player player = event.getPlayer();
        Location loc = event.getTo();

        if (isCorner(loc)) {
            Location newLoc = player.getLocation();
            newLoc.setZ(loc.getZ() * -1);
            newLoc.setX(loc.getX() * -1);
            player.teleportAsync(newLoc, PlayerTeleportEvent.TeleportCause.SPECTATE);
        } else if (isHorizontalEdge(loc)) {
            Location newLoc = player.getLocation();
            newLoc.setX(loc.getX() * -1);
            player.teleportAsync(newLoc, PlayerTeleportEvent.TeleportCause.SPECTATE);
        } else if (isVerticalEdge(loc)) {
            Location newLoc = player.getLocation();
            newLoc.setZ(loc.getZ() * -1);
            player.teleportAsync(newLoc, PlayerTeleportEvent.TeleportCause.SPECTATE);
        } else {
            return;
        }
        player.setFlying(player.isFlying());
        player.setVelocity(player.getVelocity());
    }

    // Ark of the Covenant in Judah
    @EventHandler
    public void onPlayerTouchArk(PlayerMoveEvent event) {
        if (!event.hasChangedBlock())
            return;
        if (isEarthWorld(event.getTo().getWorld()))
            return;
        Location to = event.getTo();
        if (to.getBlockY() < 90)
            return;
        if (to.getBlockX() != 7220)
            return;
        if (to.getBlockZ() != -6638)
            return;

        Player player = event.getPlayer();
        to.getWorld().spawnEntity(to, EntityType.LIGHTNING);

        player.playSound(to, Sound.ITEM_TRIDENT_THUNDER, 1f, 1f);
        player.playSound(to, Sound.BLOCK_END_PORTAL_SPAWN, 1f, 1f);
        player.addPotionEffect(new PotionEffect(PotionEffectType.CONFUSION, 200, 1));
        player.addPotionEffect(new PotionEffect(PotionEffectType.WITHER, 200, 1));
        Vector vector = to.toVector().subtract(event.getFrom().toVector()).add(new Vector(0, 30, 0));
        player.setVelocity(vector.multiply(-10));
    }

    private boolean isEarthWorld(World world) {
        return !world.getName().equalsIgnoreCase("world_earth");
    }

    private boolean isHorizontalEdge(Location location) {
        int x = Math.abs(location.getBlockX());
        return unitedTransport.getConfig().getInt("earth-edges.x") <= x;
    }

    private boolean isVerticalEdge(Location location) {
        int z = Math.abs(location.getBlockZ());
        return unitedTransport.getConfig().getInt("earth-edges.z") <= z;
    }

    private boolean isCorner(Location loc) {
        return isVerticalEdge(loc) && isHorizontalEdge(loc);
    }
}
