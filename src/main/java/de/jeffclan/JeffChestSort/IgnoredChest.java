package de.jeffclan.JeffChestSort;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.bukkit.Location;
import org.bukkit.event.inventory.InventoryEvent;

public class IgnoredChest {
	private final String world;
	private final double x;
	private final double y;
	private final double z;

	public IgnoredChest(Location location) {
		world = location.getWorld().getName();
		x = location.getX();
		y = location.getY();
		z = location.getZ();
	}

	public IgnoredChest(String world, double x, double y, double z) {
		this.world = world;
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public IgnoredChest(InventoryEvent event) {
		this(event.getInventory().getLocation());
	}

	public static IgnoredChest fromConfig(Map<?, ?> map) {
		String world = (String) map.get("world");
		double x = (Double) map.get("x");
		double y = (Double) map.get("y");
		double z = (Double) map.get("z");
		return new IgnoredChest(world, x, y, z);
	}

	public static Map<?, ?> toConfig(IgnoredChest ignoredChest) {
		HashMap<Object, Object> map = new HashMap<>();
		map.put("world", ignoredChest.world);
		map.put("x", ignoredChest.x);
		map.put("y", ignoredChest.y);
		map.put("z", ignoredChest.z);
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
		IgnoredChest that = (IgnoredChest) o;
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
