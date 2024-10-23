package org.unitedlands.upkeep.commands;

import com.palmergames.bukkit.towny.TownyAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI;
import com.palmergames.bukkit.towny.TownyCommandAddonAPI.CommandType;
import com.palmergames.bukkit.towny.TownyMessaging;
import com.palmergames.bukkit.towny.object.AddonCommand;
import com.palmergames.bukkit.towny.object.Nation;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabExecutor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.upkeep.util.NationMetaController;


/*
    * This class is responsible for handling the /townyadmin powerlevel command.
    * This command is used to set a nation as a power nation.
    * A power nation is a nation that is considered to be a part of the United Lands.
    * This command is only available to players with the permission "united.upkeep.admin".
    * The command takes two arguments: the first argument is a boolean value (true or false) that determines whether the nation is an official nation or not.
    * The second argument is the name of the nation.
    * If the nation is not found, an error message is sent to the player.
    * If the nation is found, the nation is set as an official nation or removed as an official nation based on the value of the first argument.
    * usage: /townyadmin powerlevel <nation> <minor|major> <true|false>
 */
public class OfficialNationCommand implements TabExecutor {
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        return switch (args.length) {
            case 2 -> Arrays.asList("minor", "major");
            case 3 -> Arrays.asList("true", "false");
            default -> Collections.emptyList();
        };
    }

    public OfficialNationCommand() {
        TownyCommandAddonAPI.addSubCommand(new AddonCommand(CommandType.TOWNYADMIN_NATION, "powerlevel", this));
    }

    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String s, @NotNull String[] args) {
        if (!sender.hasPermission("united.upkeep.admin")) {
            TownyMessaging.sendErrorMsg("No permission to use this command!");
        } else {
            Nation nation = TownyAPI.getInstance().getNation(args[0]);
            if (nation == null) {
                TownyMessaging.sendErrorMsg(sender, "Invalid nation name!");
            } else {
                if(args[2].equalsIgnoreCase("minor"))
                    NationMetaController.setOfficialNation(nation, args[3].equalsIgnoreCase("true"), "minor");
                else if(args[2].equalsIgnoreCase("major"))
                    NationMetaController.setOfficialNation(nation, args[3].equalsIgnoreCase("true"), "major");
                TownyMessaging.sendPrefixedNationMessage(nation,"Successfully " + (args[3].equalsIgnoreCase("true") ? "set " : "removed ")+ args[1] + " as an official " + args[1] + " nation!");
            }
        }
        return true;
    }
}
