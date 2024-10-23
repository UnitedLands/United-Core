package org.unitedlands.items.util;

import org.bukkit.Bukkit;
import org.bukkit.Location;

import java.io.Serializable;

public class GenericLocation implements Serializable {
	
	private final int x;
	private final int y;
	private final int z;
	private final String worldName;

	public GenericLocation(Location l) {
		this.x = l.getBlockX();
		this.y = l.getBlockY();
		this.z = l.getBlockZ();
		this.worldName = l.getWorld().getName();
	}

	public Location getLocation() {
		return new Location(Bukkit.getWorld(worldName), x, y, z);
	}
}
