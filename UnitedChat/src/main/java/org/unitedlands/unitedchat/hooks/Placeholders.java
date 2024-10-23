package org.unitedlands.unitedchat.hooks;

import me.clip.placeholderapi.PlaceholderAPI;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import me.clip.placeholderapi.expansion.Relational;
import org.bukkit.NamespacedKey;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.player.ChatFeature;

public class Placeholders extends PlaceholderExpansion implements Relational {
    @Override
    public @NotNull String getIdentifier() {
        return "unitedchat";
    }

    @Override
    public @NotNull String getAuthor() {
        return "maroon28";
    }

    @Override
    public @NotNull String getVersion() {
        return "1.0.0";
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player one, Player two, String params) {
        // prefix_{vault_prefix}
        String[] args = PlaceholderAPI.setBracketPlaceholders(one, params).split("_");
        ChatFeature feature = ChatFeature.valueOf(args[0].toUpperCase());
        if (args.length < 2)
            return "";
        String innerPlaceholder = args[1];

        if (isChatFeatureEnabled(feature, two)) {
            if (feature == ChatFeature.PREFIXES)
               return innerPlaceholder; // prefixes are built in with a space
            return innerPlaceholder + " "; // other stuff isn't
        }
        else
            return ""; // disabled, return empty.
    }
    @Override
    public String onRequest(OfflinePlayer player, @NotNull String params) {
        // prefix_{vault_prefix}
        String[] args = PlaceholderAPI.setBracketPlaceholders(player, params).split("_");
        ChatFeature feature = ChatFeature.valueOf(args[0].toUpperCase());
        if (args.length < 2)
            return "";
        String innerPlaceholder = args[1];

        if (isChatFeatureEnabled(feature, player)) {
            if (feature == ChatFeature.PREFIXES)
                return innerPlaceholder; // prefixes are built in with a space
            return innerPlaceholder + " "; // other stuff isn't
        }
        else
            return ""; // disabled, return empty.
    }

    public boolean isChatFeatureEnabled(ChatFeature feature, OfflinePlayer player) {
        Player onlinePlayer = player.getPlayer();
        if (onlinePlayer == null) {
            return false;
        }

        PersistentDataContainer pdc = onlinePlayer.getPersistentDataContainer();
        NamespacedKey key = new NamespacedKey(UnitedChat.getPlugin(), feature.toString());

        // Features are on by default, which means it wasn't ever toggled before if there is no key.
        if (!pdc.has(key, PersistentDataType.INTEGER)) {
            return true;
        }

        Integer featureStatus = pdc.get(key, PersistentDataType.INTEGER);
        return featureStatus != null && featureStatus == 1;
    }

}
