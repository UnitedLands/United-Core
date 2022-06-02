package org.unitedlands.uniteditems.trees;

import dev.lone.itemsadder.api.CustomBlock;
import org.bukkit.Bukkit;
import org.unitedlands.uniteditems.UnitedItems;
import org.unitedlands.uniteditems.util.GenericLocation;
import org.unitedlands.uniteditems.util.Logger;
import org.unitedlands.uniteditems.util.SerializableData;
import org.unitedlands.uniteditems.util.TreeType;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.*;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.world.StructureGrowEvent;
import org.bukkit.inventory.ItemStack;

import java.io.Serial;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Random;

public class Tree implements Listener, Serializable {
	
	@Serial
	private static final long serialVersionUID = 1L;
	private static final HashMap<Location, TreeType> sapling = new HashMap<>();
	private final UnitedItems unitedItems;

	public Tree(UnitedItems unitedItems) {
		this.unitedItems = unitedItems;
	}

	@SuppressWarnings("unchecked")
	public static void loadSaplings() {
		HashMap<GenericLocation, String> safeSapling = (HashMap<GenericLocation, String>) SerializableData.Farming.readFromDatabase("sapling.dat");

		if(safeSapling != null) {
			Logger.log(String.format("&aLoading cached trees &6[&e%d&6]...", safeSapling.size()));
			safeSapling.forEach((x,y) -> {
				sapling.put(x.getLocation(), TreeType.valueOf(y));
			});
		} else {
			Logger.log("&aLoading cached saplings &6[&e0&6]...");
		}
	}
	
	@EventHandler
	public void onPlace(PlayerInteractEvent e) {
		Block b = e.getClickedBlock();
		ItemStack item = e.getItem();
		
		if(b == null) {
			return;
		}
		if(item == null) {
			return;
		}
		if(!e.getAction().equals(Action.RIGHT_CLICK_BLOCK)) {
			return;
		}
		if(b.getType().equals(Material.CAULDRON))
			return;
			
		TreeType tree = TreeType.isValidSeed(item);
		if(tree != null) {
			if(b.getType().equals(Material.GRASS_BLOCK) || b.getType().equals(Material.DIRT)) {
				Block above = b.getWorld().getBlockAt(b.getLocation().add(0, 1, 0));
				if(above.getType().equals(Material.AIR)) {
					above.setType(tree.getVanillaSapling());
					e.getItem().setAmount(e.getItem().getAmount()-1);
					sapling.put(above.getLocation(), tree);
				}
			} else {
				e.setCancelled(true);
			}
		}
	}
	
	@EventHandler
	public void onGrow(StructureGrowEvent e) {
		Location location = e.getLocation();
		TreeType tree = sapling.get(location);
		
		if(tree != null) {
			sapling.remove(location);
			for(BlockState block : e.getBlocks()) {
				if (block.getType().equals(tree.getStemBlock())) {
					Location blockLocation = block.getLocation();
					Bukkit.getScheduler().runTaskLater(unitedItems, () -> CustomBlock.place(tree.getStemReplaceBlockName(), blockLocation), 2);
				} else if (block.getType().equals(tree.getFruitBlock())) {
					Location blockLocation = block.getLocation();
					CustomBlock.place(tree.getFruitReplaceBlockName(), blockLocation);
				}
			}
		}
	}
	
	@EventHandler
	public void onDecay(LeavesDecayEvent e) {
		if(e.isCancelled())
			return;
		TreeType t = removeMappedLocation(e.getBlock().getLocation());
		if(t != null) {
			e.getBlock().setType(Material.AIR);
			e.setCancelled(true);
		}
	}
	
	@EventHandler
	public void onBreak(BlockBreakEvent e) {
		if(e.isCancelled())
			return;
		TreeType t = removeMappedLocation(e.getBlock().getLocation());
		if(t != null) {
			e.setDropItems(false);
		}
	}
	
	@EventHandler
	public void onDestroy(BlockExplodeEvent e) {
		if(e.isCancelled())
			return;
		for(Block b: e.blockList()) {
			removeMappedLocation(b.getLocation());
		}
	}
	
	@EventHandler
	public void onDestroy(EntityExplodeEvent e) {
		if(e.isCancelled())
			return;
		for(Block b: e.blockList()) {
			removeMappedLocation(b.getLocation());
		}
	}
	
	@EventHandler
	public void onDestroy(BlockPistonExtendEvent e) {
		if(e.isCancelled())
			return;
		for(Block b: e.getBlocks()) {
			removeMappedLocation(b.getLocation());
		}
	}
	
	public TreeType removeMappedLocation(Location l) {
		if(sapling.containsKey(l)) {
			TreeType ret = sapling.get(l);
			l.getWorld().dropItemNaturally(l, sapling.get(l).getSeed());
			sapling.remove(l);
			l.getBlock().setType(Material.AIR);
			return ret;
		}
		return null;
	}
	
	public static HashMap<GenericLocation, String> getSerializableSaplings(){
		HashMap<GenericLocation, String> safeSapling = new HashMap<GenericLocation, String>();
		sapling.forEach((x,y) -> {
			safeSapling.put(new GenericLocation(x), y.name());
		});
		return safeSapling;
	}
}
