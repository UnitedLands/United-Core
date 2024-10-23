package org.unitedlands.pvp.commands;

import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.pvp.player.PvpPlayer;
import org.unitedlands.pvp.util.Utils;

import java.util.Objects;

import static org.unitedlands.pvp.util.Utils.getMessage;
import static org.unitedlands.pvp.util.Utils.sendMessageList;

public class PvPCmd implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {

        Player player = (Player) sender;
        PvpPlayer pvpPlayer = new PvpPlayer(player);
        if (args.length == 0) {
            sendMessageList(player, "messages.help-message");
            return true;
        }

        if (args.length == 3) {
            if (args[0].equalsIgnoreCase("setHostility")) {
                if (player.hasPermission("united.pvp.admin")) {
                    pvpPlayer = new PvpPlayer(Objects.requireNonNull(Bukkit.getPlayer(args[1])));
                    pvpPlayer.setHostility(Integer.parseInt(args[2]));
                    pvpPlayer.updatePlayerHostility();
                    player.sendMessage("Hostility set to " + args[2]);
                } else {
                    player.sendMessage(getMessage("no-permission"));
                }
                return true;
            }
        }


        if (args.length == 2) {
            if (args[0].equals("degrade")) {
                if (args[1].equals("on")) {
                    pvpPlayer.setDegradable(true);
                    player.sendMessage(getMessage("pvp-degrade-enabled"));
                    return true;
                }
                if (args[1].equals("off")) {
                    pvpPlayer.setDegradable(false);
                    player.sendMessage(getMessage("pvp-degrade-disabled"));
                    return true;
                }
            }
        }

        if (args[0].equals("status")) {
            returnPvPStatus(player);
            return true;
        }
        if (args[0].equalsIgnoreCase("mute")) {
            setNotification(player, !hasNotification(player));
        }
        if (args[0].equals("on")) {
            if (pvpPlayer.isImmune()) {
                pvpPlayer.expireImmunity();
                player.sendMessage(getMessage("immunity-removed"));
                return true;
            }
            player.sendMessage(getMessage("you-are-not-immune"));
        }
        return false;
    }

    private void returnPvPStatus(Player player) {
        PvpPlayer pvpPlayer = new PvpPlayer(player);
        String status = pvpPlayer.getStatus().name().toLowerCase();
        TextReplacementConfig statusReplacer = TextReplacementConfig.builder()
                .match("<status>")
                .replacement(status)
                .build();
        TextReplacementConfig hostilityReplacer = TextReplacementConfig.builder()
                .match("<hostility>")
                .replacement(String.valueOf(pvpPlayer.getHostility()))
                .build();

        player.sendMessage(getMessage("pvp-status")
                .replaceText(statusReplacer)
                .replaceText(hostilityReplacer));
    }

    private void setNotification(Player player, boolean value) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(Utils.getUnitedPvP(), "neutrality-notif");
        pdc.set(key, PersistentDataType.BYTE, value ? (byte) 0 : (byte) 1);
        if (value)
            player.sendMessage(Utils.getMessage("muted-notif"));
        else
            player.sendMessage(Utils.getMessage("unmuted-notif"));
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
}




