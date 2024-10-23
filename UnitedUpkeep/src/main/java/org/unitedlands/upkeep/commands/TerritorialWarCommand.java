package org.unitedlands.upkeep.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.command.BaseCommand;
import com.palmergames.bukkit.towny.object.AddonCommand;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import com.palmergames.bukkit.towny.object.Resident;
import com.palmergames.bukkit.towny.object.Town;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.upkeep.UnitedUpkeep;
import org.unitedlands.upkeep.util.TerritorialMetaController;


/*
 * This class is responsible for handling the /townyadmin officialnation command.
 * This command is used to set a nation as an official nation.
 * An official nation is a nation that is considered to be a part of the UnitedLands.
 * This command is only available to players with the permission "united.upkeep.admin".
 * The command takes two arguments: the first argument is a boolean value (true or false) that determines whether the nation is an official nation or not.
 * The second argument is the name of the nation.
 * If the nation is not found, an error message is sent to the player.
 * If the nation is found, the nation is set as an official nation or removed as an official nation based on the value of the first argument.
 * usage: /townyadmin officialnation <true|false> <nation>
 * new usage: /townyadmin powerlevel <nation> <minor|major> <true|false>
 */
public class TerritorialWarCommand implements TabExecutor {

    private final UnitedUpkeep unitedUpkeep;

    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return switch (args.length) {
            case 1-> BaseCommand.getTownyStartingWith(args[0], "n");
            case 2 -> Arrays.asList("minor", "major");
            case 3 -> Arrays.asList("true", "false");
            default -> Collections.emptyList();
        };
    }

    public TerritorialWarCommand(UnitedUpkeep unitedUpkeep) {
        this.unitedUpkeep = unitedUpkeep;
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.TOWN_TOGGLE, "territorialWars", this));
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
            if(sender instanceof Player) {
                Town town = TownyAPI.getInstance().getTown((Player) (sender));
                Resident resident = TownyAPI.getInstance().getResident((Player) (sender));
                if(town == null) {
                    TownyMessaging.sendErrorMsg(sender, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("errors.noTown"))));
                    return true;
                }
                if(!town.isMayor(resident)) {
                    TownyMessaging.sendErrorMsg(sender, ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("errors.notMayor"))));
                    return true;
                }
                if(town.isNeutral()) {
                    TownyMessaging.sendErrorMsg(sender,ChatColor.translateAlternateColorCodes('&', Objects.requireNonNull(this.unitedUpkeep.getConfig().getString("errors.neutralTown"))));
                    return true;
                }
                TerritorialMetaController.toggleTerritorialWars(town);
                TownyMessaging.sendPrefixedTownMessage(town,ChatColor.translateAlternateColorCodes('&', (Objects.requireNonNull(TerritorialMetaController.toggledTerritorialWars(town) ? (this.unitedUpkeep.getConfig().getString("messages.enabledTerritorial")) : (this.unitedUpkeep.getConfig().getString("messages.disabledTerritorial"))))));
            } else {
                TownyMessaging.sendErrorMsg("You must be a player to use this command!");
            }
            return true;
    }
}
