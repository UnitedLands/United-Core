package org.unitedlands.war.books.data;

import com.palmergames.bukkit.towny.object.Nation;
import com.palmergames.bukkit.towny.object.Town;
import io.github.townyadvanced.eventwar.objects.WarType;
import io.github.townyadvanced.eventwar.objects.WarTypeEnum;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

import java.util.UUID;

public class WarTarget {
    private final OfflinePlayer targetMayor;
    private final Town town;
    private final Nation nation;
    private final WarType type;


    public WarTarget(Town town) {
        this.town = town;
        targetMayor = Bukkit.getOfflinePlayer(town.getMayor().getUUID());
        type = WarTypeEnum.TOWNWAR.getType();
        if (town.hasNation())
            nation = town.getNationOrNull();
        else
            nation = null;
    }

    public WarTarget(Nation nation) {
        this.nation = nation;
        this.town = nation.getCapital();
        type = WarTypeEnum.NATIONWAR.getType();
        targetMayor = Bukkit.getOfflinePlayer(town.getMayor().getUUID());
    }

    public OfflinePlayer targetMayor() {
        return targetMayor;
    }

    public Town town() {
        return town;
    }

    public Nation nation() {
        return nation;
    }

    public UUID uuid() {
        if (type.isNationWar())
            return nation.getUUID();
        else
            return town.getUUID();
    }
}