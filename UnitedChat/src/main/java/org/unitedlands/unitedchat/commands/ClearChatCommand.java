package org.unitedlands.unitedchat.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

public class ClearChatCommand implements CommandExecutor {

    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!sender.hasPermission("united.chat.admin")) {
            sender.sendMessage("You don't have permission to execute this command!");
            return false;
        }

        for (int i = 0; i < 150; i++) {
            Bukkit.broadcast(Component.newline());
        }

        return true;
    }

}
