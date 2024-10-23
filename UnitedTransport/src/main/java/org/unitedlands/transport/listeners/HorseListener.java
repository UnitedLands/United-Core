package org.unitedlands.transport.listeners;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Horse;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.spigotmc.event.entity.EntityMountEvent;
import org.unitedlands.transport.UnitedTransport;

import java.util.Objects;

public class HorseListener implements Listener {
    private final UnitedTransport unitedTransport;


    public HorseListener(UnitedTransport unitedTransport) {
        this.unitedTransport = unitedTransport;
    }

    @EventHandler
    public void onHorseMount(EntityMountEvent event) {
        if (event.getMount() instanceof Horse horse) {
            if (!hasSpeedKey(horse)) {
                saveBaseHorseSpeed(horse);
            }
        }
    }

    @EventHandler
    public void onHorseMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!event.hasChangedBlock()) return;
        if (!player.isInsideVehicle()) return;
        if (player.getVehicle() instanceof Horse horse) {
            double horseSpeed = Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getBaseValue();
            double newHorseSpeed = getBoostedHorseSpeed(horse);
            if (horseSpeed != newHorseSpeed) {
                Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).setBaseValue(newHorseSpeed);
            }
        }
    }

    private double getBoostedHorseSpeed(Horse horse) {
        for (String entry : unitedTransport.getConfig().getStringList("horse-speed-modifier")) {
            String[] split = entry.split(";");
            String materialName = split[0];
            if (!getPathMaterial(horse).toString().equals(materialName)) {
                continue;
            }
            double speedModifier = Double.parseDouble(split[1]);
            double baseValue = getBaseHorseSpeed(horse);
            return baseValue + (baseValue * speedModifier);
        }
        return getBaseHorseSpeed(horse);
    }

    private double getBaseHorseSpeed(Horse horse) {
        NamespacedKey speedKey = getSpeedKey();
        PersistentDataContainer container = horse.getPersistentDataContainer();
        if (hasSpeedKey(horse)) {
            return container.get(speedKey, PersistentDataType.DOUBLE);
        }
        return 0;
    }

    private void saveBaseHorseSpeed(Horse horse) {
        NamespacedKey speedKey = getSpeedKey();
        PersistentDataContainer container = horse.getPersistentDataContainer();
        if (!hasSpeedKey(horse)) {
            container.set(speedKey, PersistentDataType.DOUBLE, Objects.requireNonNull(horse.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)).getBaseValue());
        }
    }

    private boolean hasSpeedKey(Horse horse) {
        NamespacedKey speedKey = getSpeedKey();
        PersistentDataContainer container = horse.getPersistentDataContainer();
        return container.has(speedKey, PersistentDataType.DOUBLE);
    }

    @NotNull
    private NamespacedKey getSpeedKey() {
        return new NamespacedKey(unitedTransport, "speed");
    }

    private Material getPathMaterial(Horse horse) {
        return horse.getLocation().getBlock().getRelative(0, -1, 0).getType();
    }
}
