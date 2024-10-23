package org.unitedlands.pvp;

import org.bukkit.Bukkit;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;
import org.unitedlands.pvp.commands.PvPCmd;
import org.unitedlands.pvp.hooks.Placeholders;
import org.unitedlands.pvp.listeners.PlayerListener;
import org.unitedlands.pvp.listeners.TownyListener;

import java.util.Objects;

public final class UnitedPvP extends JavaPlugin {

    @Override
    public void onEnable() {
        saveDefaultConfig();
        registerListeners();
    }

    private void registerListeners() {

        PluginManager pluginManager = Bukkit.getServer().getPluginManager();
        pluginManager.registerEvents(new PlayerListener(this), this);
        pluginManager.registerEvents(new TownyListener(this), this);
        Objects.requireNonNull(getCommand("pvp")).setExecutor(new PvPCmd());

        // PlaceholderAPI Expansion Register
        if (pluginManager.getPlugin("PlaceholderAPI") != null) {
            new Placeholders().register();
        }

    }


}
