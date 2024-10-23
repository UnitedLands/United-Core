package org.unitedlands.unitedchat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.unitedchat.commands.ChatToggleCommand;
import org.unitedlands.unitedchat.commands.ClearChatCommand;
import org.unitedlands.unitedchat.commands.GradientCommand;
import org.unitedlands.unitedchat.hooks.Placeholders;
import org.unitedlands.unitedchat.player.PlayerListener;

import java.util.Objects;

public class UnitedChat extends JavaPlugin {

    private static UnitedChat PLUGIN;

    public UnitedChat() {
        PLUGIN = this;
    }

    public static UnitedChat getPlugin() {
        return PLUGIN;
    }

    @Override
    public void onEnable() {
        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") == null) {
            getLogger().warning("[Exception] PlaceholderAPI is required!");
            Bukkit.getPluginManager().disablePlugin(this);
        }
        new Placeholders().register();
        getServer().getPluginManager().registerEvents(new PlayerListener(), this);
        Objects.requireNonNull(getCommand("gradient")).setExecutor(new GradientCommand());
        Objects.requireNonNull(getCommand("cc")).setExecutor(new ClearChatCommand());
        Objects.requireNonNull(getCommand("chat")).setExecutor(new ChatToggleCommand());
        saveDefaultConfig();

    }

    public static Component getMessage(String name) {
        String prefix = PLUGIN.getConfig().getString("messages.prefix");
        String message = PLUGIN.getConfig().getString("messages." + name);
        return MiniMessage.miniMessage().deserialize(prefix + message);
    }

    public static Component getMessage(String message, TagResolver.Single... resolvers) {
        String prefix = PLUGIN.getConfig().getString("messages.prefix");
        String configuredMessage = PLUGIN.getConfig().getString("messages." + message);
        if (configuredMessage == null) {
            return MiniMessage.miniMessage().deserialize("<red>Message <yellow>" + message + "<red> could not be found in the config file!");
        }
        return MiniMessage.miniMessage().deserialize(prefix + configuredMessage, resolvers);
    }
}