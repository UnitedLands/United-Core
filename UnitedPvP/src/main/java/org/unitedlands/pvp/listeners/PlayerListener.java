package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.event.player.PlayerKilledPlayerEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import de.jeff_media.angelchest.AngelChest;
import de.jeff_media.angelchest.events.AngelChestSpawnEvent;
import net.kyori.adventure.text.TextReplacementConfig;
import org.apache.commons.lang.time.DurationFormatUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Arrow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityCombustEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerBucketEmptyEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.unitedlands.pvp.UnitedPvP;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.util.Utils;

import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


public class PlayerListener implements Listener {
    private final UnitedPvP unitedPvP;
    private final TownyAPI towny = TownyAPI.getInstance();
    HashMap<EnderCrystal, UUID> crystalMap = new HashMap<>();

    public PlayerListener(UnitedPvP unitedPvP) {
        this.unitedPvP = unitedPvP;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        PvpPlayer pvpPlayer = new PvpPlayer(player);
        // Create the file if they haven't played before, or don't have a file.
        if (!player.hasPlayedBefore() || !pvpPlayer.getPlayerFile().exists()) {
            pvpPlayer.createFile();
            return; // No need to run the rest of the logic here, so just move on.
        }

        tryNeutralityRemoval(player);

        long lastChangeTime = pvpPlayer.getLastHostilityChangeTime();
        int dayDifference = getDaysPassed(lastChangeTime);

        if (dayDifference == 0) return;

        // Update the hostility for each full day we log.
        for (int i = 0; i < dayDifference; i++) {
            pvpPlayer.updatePlayerHostility();
        }

    }

    private void tryNeutralityRemoval(Player player) {
        Town town = Objects.requireNonNull(towny.getResident(player.getUniqueId())).getTownOrNull();
        // Player doesn't have a town
        if (town == null) return;
        PvpPlayer pvpPlayer = new PvpPlayer(player);
        // Method was called, but player in question was not hostile.
        if (!pvpPlayer.isHostile() || !pvpPlayer.isAggressive()) return;

        // Kick them out and notify them
        if (town.isNeutral()) {
            town.setNeutral(false);
            player.sendMessage(Utils.getMessage("kicked-out-of-neutrality"));
            // Notify the mayor if they're online
            Resident mayor = town.getMayor();
            if (mayor.isOnline()) {
                Objects.requireNonNull(mayor.getPlayer()).sendMessage(Utils.getMessage("kicked-out-of-neutrality-mayor"));
            }
        }
        if (town.hasNation()) {
            Nation nation = town.getNationOrNull();
            if (Objects.requireNonNull(nation).isNeutral()) {
                nation.setNeutral(false);
                Player king = nation.getKing().getPlayer();
                if (king != null) {
                    king.sendMessage(Utils.getMessage("kicked-out-of-neutrality-king"));
                }
            }
        }
    }

    @EventHandler
    public void onDamage(EntityDamageByEntityEvent event) {

        if (Utils.isPvP(event)) {
            Player target = (Player) event.getEntity();
            Player damager = getAttacker(event.getDamager());

            PvpPlayer pvpDamager = new PvpPlayer(damager);
            PvpPlayer pvpTarget = new PvpPlayer(target);

            if (pvpTarget.isImmune()) {
                event.setCancelled(true);
                damager.sendMessage(Utils.getMessage("target-immune"));
            }

            if (pvpDamager.isImmune()) {
                event.setCancelled(true);
                TextReplacementConfig timeReplacer = TextReplacementConfig.builder()
                        .match("<time>")
                        .replacement(DurationFormatUtils.formatDuration( TimeUnit.DAYS.toMillis(1) - pvpDamager.getImmunityTime(), "HH:mm:ss"))
                        .build();
                damager.sendMessage(Utils.getMessage("you-are-immune").replaceText(timeReplacer));
            }
        }
    }

    @EventHandler
    public void onPlayerExplosionDamage(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof EnderCrystal) {
            event.setDamage(event.getDamage() * 0.1);
        }
    }

    @EventHandler
    public void onPlayerKillPlayer(PlayerKilledPlayerEvent event) {
        Player killer = event.getKiller();
        Player victim = event.getVictim();

        PvpPlayer killerPvP = new PvpPlayer(killer);
        PvpPlayer victimPvP = new PvpPlayer(victim);
        // If both players are town mates or nation mates, it was likely just a small quarrel or a duel
        // No need to do anything.
        if (areRelated(killer, victim))
            return;

        // If an outlaw is killed, don't increase the hostility
        if (isOutlawKill(event))
            return;

        // If both are defensive, the killer is being hostile. Increase their hostility.
        if (killerPvP.isDefensive() && victimPvP.isDefensive()) {
            killerPvP.setHostility(killerPvP.getHostility() + 1);
        }

        // If the killer is already hostile/aggressive, and they kill a defensive player
        // that signifies a higher level of hostility, therefore increase by 2 points.
        if ((killerPvP.isAggressive() || killerPvP.isHostile()) && victimPvP.isDefensive()) {
            killerPvP.setHostility(killerPvP.getHostility() + 2);
            tryNeutralityRemoval(killer);
            return;
        }
        // if the killer is aggressive or hostile, killer becomes more hostile.
        if (killerPvP.isAggressive() || killerPvP.isHostile()) {
            killerPvP.setHostility(killerPvP.getHostility() + 1);
            tryNeutralityRemoval(killer);
        }
    }

    private boolean isOutlawKill(PlayerKilledPlayerEvent event) {
        var killerRes = event.getKillerRes();
        var victimRes = event.getVictimRes();

        var killerTown = killerRes.getTownOrNull();
        if (killerTown == null) return false;

        return killerTown.hasOutlaw(victimRes);
    }

    @EventHandler
    public void onGraveSpawn(AngelChestSpawnEvent event) {
        AngelChest chest = event.getAngelChest();
        Location graveLocation = chest.getBlock().getLocation();
        OfflinePlayer player = chest.getPlayer();
        if (isInOutlawTown(player, graveLocation))
            chest.setProtected(false);
    }

    private boolean isInOutlawTown(OfflinePlayer player, Location graveLocation) {
        Resident resident = towny.getResident(player.getUniqueId());
        if (resident == null) return false;
        // Make sure they're not in the wilderness
        if (towny.isWilderness(graveLocation)) return false;
        Town town = Objects.requireNonNull(towny.getTownBlock(graveLocation)).getTownOrNull();
        // This shouldn't happen but adding it to be safe.
        if (town == null) return false;

        return town.hasOutlaw(resident);
    }


    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!event.getAction().equals(Action.RIGHT_CLICK_BLOCK)) return;
        if (!Objects.requireNonNull(event.getClickedBlock()).getType().equals(Material.OBSIDIAN)) return;
        // The player clicked an obsidian but didn't have a crystal
        if (!event.getMaterial().equals(Material.END_CRYSTAL)) return;

        Bukkit.getScheduler().runTask(unitedPvP, () -> {
            List<Entity> entities = event.getPlayer().getNearbyEntities(4, 4, 4);
            for (Entity entity : entities) {
                // Get the nearest crystal
                if (entity instanceof EnderCrystal crystal) {
                    Block belowCrystal = crystal.getLocation().getBlock().getRelative(BlockFace.DOWN);
                    // Check if the block below the newly spawned crystal is the same as the block the player
                    // clicked in the event.
                    if (event.getClickedBlock().equals(belowCrystal)) {
                        // Save it.
                        crystalMap.put(crystal, event.getPlayer().getUniqueId());
                        break;
                    }
                }
            }
        });
    }

    @EventHandler
    public void onPlayerDeathByCrystal(PlayerDeathEvent event) {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            // This crystal is not registered.
            if (!crystalMap.containsKey(crystal)) return;
            Player originalPlacer = Bukkit.getPlayer(crystalMap.get(crystal));
            // Player might be null
            if (originalPlacer == null) return;
            // Player might've killed themselves by accident, don't do anything;
            if (originalPlacer.equals(event.getPlayer())) return;

            // Increase the hostility of whoever placed the crystal.
            PvpPlayer pvpPlacer = new PvpPlayer(originalPlacer);
            pvpPlacer.setHostility(pvpPlacer.getHostility() + 1);
        }
    }

    @EventHandler
    public void onCrystalExplode(EntityCombustEvent event) {
        if (event.getEntity() instanceof EnderCrystal crystal) {
            // Remove the crystal from the map a second after it explodes, to have time to detect player deaths etc.
            unitedPvP.getServer().getScheduler().runTaskLater(unitedPvP, () -> crystalMap.remove(crystal), 20L);
        }
    }

    @EventHandler
    public void onLavaPlace(PlayerBucketEmptyEvent event) {
        if (!event.getBucket().equals(Material.LAVA_BUCKET)) return;
        Player player = event.getPlayer();
        var nearby = event.getBlock().getLocation().getNearbyEntities(2, 2, 2);
        for (Entity entity : nearby) {
            if (entity instanceof Player nearbyPlayer) {
                PvpPlayer pvpPlayer = new PvpPlayer(nearbyPlayer);
                if (pvpPlayer.isImmune()) {
                    event.setCancelled(true);
                    player.sendMessage(Utils.getMessage("target-immune"));
                }
            }
        }
    }

    @EventHandler
    public void onArrowShoot(EntityShootBowEvent event) {
        if (!(event.getProjectile() instanceof Arrow arrow)) return;
        if(arrow.getCustomEffects().size() > 1) arrow.clearCustomEffects();
    }

    private Player getAttacker(Entity damager) {
        if (damager instanceof Projectile) {
            return (Player) ((Projectile) damager).getShooter();
        }
        return (Player) damager;
    }

    private int getDaysPassed(long playerTime) {
        // Player time should always have a value, else it wasn't registered.
        if (playerTime == 0) {
            return 0;
        }
        long timeDifference = System.currentTimeMillis() - playerTime;
        return Math.toIntExact(TimeUnit.MILLISECONDS.toDays(timeDifference));
    }

    private boolean areRelated(Player first, Player second) {
        var firstTown = Objects.requireNonNull(towny.getResident(first)).getTownOrNull();
        var secondTown = Objects.requireNonNull(towny.getResident(second)).getTownOrNull();
        if (firstTown == null || secondTown == null) return false;
        if (firstTown.hasNation() && secondTown.hasNation()) {
            return Objects.equals(firstTown.getNationOrNull(), secondTown.getNationOrNull());
        }
        return firstTown.equals(secondTown);
    }

}
