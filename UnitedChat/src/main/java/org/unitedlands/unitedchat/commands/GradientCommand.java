package org.unitedlands.unitedchat.commands;

import net.kyori.adventure.text.TextReplacementConfig;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.unitedlands.unitedchat.UnitedChat;
import org.unitedlands.unitedchat.player.ChatPlayer;

import java.util.Objects;

import static org.unitedlands.unitedchat.UnitedChat.getMessage;

public class GradientCommand implements CommandExecutor {
    private Player player;
    private ChatPlayer chatPlayer;

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String label, String[] args) {

        if (!(sender instanceof Player)) {
            return false;
        }
        this.player = (Player) sender;

        if (!player.hasPermission("united.chat.gradient")) {
            player.sendMessage(getMessage("no-perm"));
            return false;
        }

        chatPlayer = new ChatPlayer(player.getUniqueId());

        if (args.length != 1) {
            player.sendMessage(getMessage("gradient-command"));
            return true;
        }

        switch (args[0]) {
            case "info" -> sendGradientInfo();
            case "on" -> enableGradient();
            case "off" -> disableGradient();
            default -> setGradient(args[0]);
        }

        return false;
    }

    private void setGradient(String arg) {
        if (Objects.requireNonNull(getPresetSection()).contains(arg)) {
            setGradientPreset(arg);
            return;
        }
        if (arg.contains("#")) {
            if (player.hasPermission("united.chat.gradient.all")) {
                chatPlayer.setGradient(arg);
                player.sendMessage(getMessage("gradient-changed"));
            }
        } else {
            player.sendMessage(getMessage("no-perm"));
        }
    }

    private void setGradientPreset(String presetName) {
        if (Objects.requireNonNull(getPresetSection()).getString(presetName) == null) {
            player.sendMessage(getMessage("gradient-unknown-preset"));
            return;
        }

        if (player.hasPermission("united.chat.gradient." + presetName)) {
            String preset = getPresetSection().getString(presetName);
            chatPlayer.setGradient(preset);
            player.sendMessage(getMessage("gradient-changed"));
        } else {
            player.sendMessage(getMessage("no-perm"));
        }
    }

    private void disableGradient() {
        if (chatPlayer.isGradientEnabled()) {
            chatPlayer.setGradientEnabled(false);
            player.sendMessage(getMessage("gradient-off"));
        } else {
            player.sendMessage(getMessage("gradient-is-off"));
        }
    }

    private void enableGradient() {
        if (!chatPlayer.isGradientEnabled()) {
            chatPlayer.setGradientEnabled(true);
            player.sendMessage(getMessage("gradient-on"));
        } else {
            player.sendMessage(getMessage("gradient-is-on"));
        }
    }

    private void sendGradientInfo() {
        String gradient = chatPlayer.getGradient().replace(":", "\n- ");
        TextReplacementConfig gradientPlaceholder = TextReplacementConfig.builder().match("<gradient>").replacement(gradient).build();
        player.sendMessage(getMessage("current-gradient").replaceText(gradientPlaceholder));
    }

    @Nullable
    private ConfigurationSection getPresetSection() {
        return UnitedChat.getPlugin().getConfig().getConfigurationSection("Presets");
    }

}
