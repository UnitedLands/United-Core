package org.unitedlands.skills.commands;

import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.skills.UnitedSkills;
import org.unitedlands.skills.Utils;
import org.unitedlands.skills.points.PlayerConfiguration;

public class PointsCommand implements CommandExecutor {
    private final UnitedSkills unitedSkills;

    public PointsCommand(UnitedSkills unitedSkills) {
        this.unitedSkills = unitedSkills;
    }
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("united.skills.admin")) {
            sender.sendMessage(Utils.getMessage("no-permission"));
            return true;
        }
        if (args.length < 4) {
            sender.sendMessage(Utils.getMessage("points-usage"));
            return true;
        }

        OfflinePlayer player = Bukkit.getOfflinePlayer(args[1]);
        String jobName = args[2];
        int amount = Integer.parseInt(args[3]);
        switch (args[0]) {
            case "add" -> {
                addPlayerPoints(player, jobName, amount);
                sender.sendMessage(Utils.getMessage("points-added"));
                return true;
            }
            case "remove" -> {
                removePlayerPoints(player, jobName, amount);
                sender.sendMessage(Utils.getMessage("points-removed"));
                return true;
            }
            default -> sender.sendMessage(Utils.getMessage("points-usage"));
        }

        return false;
    }

    private void addPlayerPoints(OfflinePlayer player, String jobName, int amount) {
        PlayerConfiguration playerConfiguration = getPlayerConfiguration(player);
        playerConfiguration.increaseJobPoints(jobName, amount);
    }

    private void removePlayerPoints(OfflinePlayer player, String jobName, int amount) {
        PlayerConfiguration playerConfiguration = getPlayerConfiguration(player);
        playerConfiguration.decreaseJobPoints(jobName, amount);
    }

    @NotNull
    private PlayerConfiguration getPlayerConfiguration(OfflinePlayer player) {
        return new PlayerConfiguration(unitedSkills, player);
    }

}
