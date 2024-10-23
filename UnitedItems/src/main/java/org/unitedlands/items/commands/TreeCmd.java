package org.unitedlands.items.commands;

import net.md_5.bungee.api.ChatColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.items.UnitedItems;
import org.unitedlands.items.trees.TreeType;

public class TreeCmd implements CommandExecutor {
    //
    public boolean onCommand(CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {
        if(!sender.hasPermission("united.custom.admin")) {
            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', UnitedItems.getGlobalMsg("NoPerm")));
            return false;
        }

        if(args.length == 0) {
            sender.sendMessage(ChatColor.translateAlternateColorCodes('&', """
                    &7&m---&r &f&lUnited Core &a&lTrees&r &7&m---
                    &e/tree help &f| &7&oUsage of the tree command
                    &e/tree seed &f<name> | &7&oGives a seed of the tree
                    &e/tree info &f<name> | &7&oPrints information for a given tree
                    &e/tree list &f| &7&oPrints all valid tree types
                    &e/tree give <player> <name> &f| &7&oGives the player a tree seed"""
            ));
        }

        if(args.length > 0) {
            if(args[0].equals("help")) {
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', """
                        &7&m---&r &f&lUnited Core &a&lTrees&r &7&m---
                        &e/tree help
                        &e/tree seed <name>
                        &e/tree info <name>
                        &e/tree list
                        &e/tree give <player> <name>"""
                ));
            }
            if(args[0].equals("list")) {
                StringBuilder trees = new StringBuilder();
                for(TreeType t : TreeType.values()) {
                    trees.append(String.format("&a%s\n", t.name()));
                }
                sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e&lTrees&r\n"+
                        trees)));
            }
            if(args.length > 1) {
                if(args[0].equals("seed")) {
                    TreeType.valueOf(args[1].toUpperCase());
                    if (sender instanceof Player p) {
                        p.getInventory().addItem(TreeType.valueOf(args[1].toUpperCase()).getSeed());
                        sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', UnitedItems.getMsg("ReceivedSapling")));
                    } else {
                        sender.sendMessage("Only a player can execute this command!");
                    }
                }
                if(args[0].equals("info")) {
                    try{
                        TreeType.valueOf("MANGO");
                    } catch (Exception e1){
                        sender.sendMessage("NE");
                        return false;
                    }

                    sender.sendMessage("MANGO");
                      /* sender.sendMessage(ChatColor.translateAlternateColorCodes('&', String.format("&e&l%s Tree&r\n"
                                        + "&eFruit: &7&o%s\n"
                                        + "&eFruit Block: &7&o%s\n"
                                        + "&eFruit Replacement Block: &7&o%s\n"
                                        + "&eStem Block: &7&o%s\n"
                                        + "&eStem Replacement Block: &7&o%s\n"
                                        + "&esSeed: &7&o%s",
                                args[1].toUpperCase(),
                                tree.getDrop().toString(),
                                tree.getFruitBlock(),
                                tree.getFruitReplaceBlock(),
                                tree.getStemBlock(),
                                tree.getStemReplaceBlock(),
                                tree.getSeed())));*/
                }
            }
            if(args.length > 2) {
                if(args[0].equals("give")) {
                    Player p = Bukkit.getPlayer(args[1]);
                    if(p != null){
                        try{
                            TreeType tree = TreeType.valueOf(args[2].toUpperCase());
                            sender.sendMessage(org.bukkit.ChatColor.translateAlternateColorCodes('&', UnitedItems.getMsg("ReceivedSapling")));
                            p.getInventory().addItem(tree.getSeed());
                        } catch (Exception e1){
                            e1.printStackTrace();
                        }

                    }
                }
            }
        }

        return true;
    }

}
