package de.jeff_media.ChestSort.unsorted;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.inventory.Inventory;

public class UnsortedChest {
	private final String world;
	private final double x;
	private final double y;
	private final double z;

	public UnsortedChest(Location location) {
		this(location.getWorld().getName(), location.getX(), location.getY(), location.getZ());
	}

	public UnsortedChest(String world, double x, double y, double z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public UnsortedChest(Inventory inventory) {
		this(inventory.getLocation());
	}

	public static UnsortedChest fromConfig(Map<?, ?> map) {
		String world = (String) map.get("world");
		double x = (Double) map.get("x");
		double y = (Double) map.get("y");
		double z = (Double) map.get("z");
		return new UnsortedChest(world, x, y, z);
	}

	public static Map<?, ?> toConfig(UnsortedChest unsortedChest) {
		HashMap<Object, Object> map = new HashMap<>();
		map.put("world", unsortedChest.world);
		map.put("x", unsortedChest.x);
		map.put("y", unsortedChest.y);
		map.put("z", unsortedChest.z);
		return map;
	}

	public String getWorld() {
		return world;
	}

	public double getX() {
		return x;
	}

	public double getY() {
		return y;
	}

	public double getZ() {
		return z;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		UnsortedChest that = (UnsortedChest) o;
		return Double.compare(that.x, x) == 0 &&
				Double.compare(that.y, y) == 0 &&
				Double.compare(that.z, z) == 0 &&
				world.equals(that.world);
	}

	@Override
	public int hashCode() {
		return Objects.hash(world, x, y, z);
	}

	@Override
	public String toString() {
		return "Chest at {" +
				"world='" + world + '\'' +
				", x=" + x +
				", y=" + y +
				", z=" + z +
				'}';
	}
}
