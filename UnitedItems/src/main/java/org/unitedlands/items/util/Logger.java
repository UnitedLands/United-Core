package org.unitedlands.items.util;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;

public class Logger {
    public static void log(String msg) {
        msg = ChatColor.translateAlternateColorCodes('&', "&b[&c&lUnited&f&lItems&b]&r " + msg);
        Bukkit.getConsoleSender().sendMessage(msg);
    }

}
