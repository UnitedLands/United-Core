package org.unitedlands.pvp.listeners;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyUniverse;
import com.palmergames.bukkit.towny.event.NewDayEvent;
import com.palmergames.bukkit.towny.event.PlayerEnterTownEvent;
import com.palmergames.bukkit.towny.event.nation.toggle.NationToggleNeutralEvent;
import com.palmergames.bukkit.towny.event.town.toggle.TownToggleNeutralEvent;
import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.TextReplacementConfig;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.intellij.lang.annotations.RegExp;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.UnitedPvP;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.util.Utils;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class TownyListener implements Listener {
    private final TownyAPI towny = TownyAPI.getInstance();
    private final UnitedPvP unitedPvP;
    Player player;
    public TownyListener(UnitedPvP unitedPvP) {
        this.unitedPvP = unitedPvP;
    }


    @EventHandler
    public void onLeaderJoin(PlayerJoinEvent event) {
        this.player = event.getPlayer();
        Resident resident = towny.getResident(player);
        if (!Objects.requireNonNull(resident).hasTown())
            return;

        if (!hasNotification(player))
            return;

        if (resident.isMayor()) {
            List<String> hostilePlayers = getHostileResidents(Objects.requireNonNull(resident.getTownOrNull()).getResidents());
            if (!hostilePlayers.isEmpty())
                sendListedMessage("cannot-be-neutral", "<players>", hostilePlayers);
        }
        if (resident.isKing()) {
            List<String> hostileTowns = getHostileTowns(Objects.requireNonNull(resident.getNationOrNull()));
            if (!hostileTowns.isEmpty())
                sendListedMessage("cannot-be-neutral-nation", "<towns>", hostileTowns);
        }
    }

    @EventHandler
    public void onNewTownyDay(NewDayEvent event) {
        // Save the time stamp for the towny day.
        // Used for comparison in the future.
        unitedPvP.getConfig().set("last-towny-day-time", System.currentTimeMillis());
        unitedPvP.saveConfig();
        // Force update the hostilities of any new players online.
        unitedPvP.getServer().getScheduler().runTask(unitedPvP, this::updatePlayerHostilities);
        TownyUniverse.getInstance().getTowns().forEach(town -> {
            tryNeutralityRemoval(town);
            tryNationNeutralityRemoval(town);
        });
    }

    @EventHandler
    public void onTownNeutralityChange(TownToggleNeutralEvent event) {
        List<Resident> residents = event.getTown().getResidents();
        List<String> hostileResidents = getHostileResidents(residents);

        // No hostile residents in the list, so they're viable to toggle neutrality
        if (hostileResidents.isEmpty()) return;

        event.setCancelled(true);
        event.getTown().setNeutral(false);
        sendListedMessage("cannot-be-neutral", "<players>", hostileResidents);
    }

    private void sendListedMessage(String message, @RegExp String pattern, List<String> list) {
        TextReplacementConfig playerReplacer = TextReplacementConfig
                .builder()
                .match(pattern)
                // Join all found hostile residents in the list.
                .replacement(String.join("<light_gray>,<yellow> ", list))
                .build();
        player.sendMessage(Utils.getMessage(message).replaceText(playerReplacer));
    }

    @NotNull
    private List<String> getHostileResidents(List<Resident> residents) {
        List<String> hostileResidents = new ArrayList<>();
        for (var resident: residents) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(resident.getUUID());
            PvpPlayer pvpPlayer = new PvpPlayer(offlinePlayer);
            if (pvpPlayer.isHostile() || pvpPlayer.isAggressive()) {
                hostileResidents.add(offlinePlayer.getName());
            }
        }
        return hostileResidents;
    }

    private boolean hasNotification(Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Utils.getUnitedPvP(), "neutrality-notif");
        if (pdc.has(key)) {
            byte stored = pdc.get(key, PersistentDataType.BYTE);
            return stored == 1; // 1 is on, 0 is off.
        }
        return true; // true by default.
    }

    @EventHandler
    public void onNationNeutralityChange(NationToggleNeutralEvent event) {
        Nation nation = event.getNation();
        List<String> hostileTowns = getHostileTowns(nation);
        if (hostileTowns.isEmpty())
            return;
        sendListedMessage("cannot-be-neutral-nation", "<towns>", hostileTowns);
        nation.setNeutral(false);
        event.setCancelled(true);
    }

    @NotNull
    private List<String> getHostileTowns(Nation nation) {
        List<String> hostileTowns = new ArrayList<>();
        for (Town town: nation.getTowns()) {
            if (!town.isNeutral())
                hostileTowns.add(town.getFormattedName());
        }
        return hostileTowns;
    }

    @EventHandler
    public void onTownEnter(PlayerEnterTownEvent event) {
        player = event.getPlayer();
        Resident outlaw = towny.getResident(player);
        Town town = event.getEnteredtown();

        if (town.hasOutlaw(outlaw)) {
            player.showTitle(getOutlawWarningTitle(town));
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1, 0.4F);
            BossBar outlawedBossbar = getOutlawedBossbar(town);
            player.showBossBar(outlawedBossbar);
            startBossbarCountdown(outlawedBossbar);
        }
    }

    private void startBossbarCountdown(BossBar bossBar) {
        double timeDecrease =  (double) 1 / 15;
        unitedPvP.getServer().getScheduler().runTaskTimer(unitedPvP, task -> {
            if (bossBar.progress() <= 0.0) {
                player.hideBossBar(bossBar);
                task.cancel();
                return;
            }
            bossBar.progress((float) Math.max(0.0, bossBar.progress() - timeDecrease));
        }, 0, 20L);
    }

    private void tryNeutralityRemoval(Town town) {
        if (!town.isNeutral())
            return;

        town.getResidents().forEach(resident -> {
            if (!town.isNeutral())
                return;
            PvpPlayer pvpPlayer = new PvpPlayer(Bukkit.getOfflinePlayer(resident.getUUID()));
            if (!pvpPlayer.isHostile())
                return;
            town.setNeutral(false);
            if (town.getMayor().isOnline()) {
                Objects.requireNonNull(town.getMayor().getPlayer()).sendMessage(Utils.getMessage("kicked-out-of-neutrality-mayor"));
            }
        });
    }
    private void tryNationNeutralityRemoval(Town town) {
        if (!town.isNeutral() && town.hasNation()) {
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

    private void updatePlayerHostilities() {
        List<PvpPlayer> pvpPlayers = getOnlinePvpPlayers();
        for (PvpPlayer pvpPlayer : pvpPlayers) {
            pvpPlayer.updatePlayerHostility();
        }
    }

    private List<PvpPlayer> getOnlinePvpPlayers() {
        List<PvpPlayer> pvpPlayers = new ArrayList<>();
        for (Player player : Bukkit.getOnlinePlayers()) {
            PvpPlayer pvpPlayer = new PvpPlayer(player);
            pvpPlayers.add(pvpPlayer);
        }
        return pvpPlayers;
    }


    private Title getOutlawWarningTitle(Town town) {
        Title.Times times = Title.Times.times(Duration.ofMillis(500), Duration.ofMillis(3000), Duration.ofMillis(1000));

        final Component mainTitle = Component.text("Warning!", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component subtitle = Component
                .text("You are outlawed in ", NamedTextColor.RED)
                .append(Component.text(town.getName(), NamedTextColor.YELLOW))
                .append(Component.text("!", NamedTextColor.RED));
        return Title.title(mainTitle, subtitle, times);
    }

    private BossBar getOutlawedBossbar(Town town) {
        final Component header = Component.text("DANGER ZONE: ", NamedTextColor.DARK_RED, TextDecoration.BOLD);
        final Component townName = Component.text("You're an outlaw in " + town.getName(), NamedTextColor.RED)
                .decoration(TextDecoration.BOLD, false);

        return BossBar.bossBar(header.append(townName), 1, BossBar.Color.RED, BossBar.Overlay.NOTCHED_12);
    }
}
