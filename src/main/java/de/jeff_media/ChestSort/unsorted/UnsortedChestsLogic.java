package de.jeff_media.ChestSort.unsorted;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.jeff_media.ChestSort.ChestSortPlugin;
import de.jeff_media.ChestSort.utils.Utils;
import org.bukkit.block.BlockState;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;

import static java.util.stream.Collectors.*;

public class UnsortedChestsLogic {
	private final ChestSortPlugin plugin;
	private final File unsortedChestsFile;
	private Set<UnsortedChest> unsortedChests;

	public UnsortedChestsLogic(ChestSortPlugin plugin) {
		this.plugin = plugin;
		unsortedChestsFile = Paths.get(plugin.getDataFolder().getAbsolutePath(), "ignored_chests.yaml").toFile();
		loadIgnoredChests();
	}

	private void loadIgnoredChests() {
		if (unsortedChestsFile.exists()) {
			YamlConfiguration config = YamlConfiguration.loadConfiguration(unsortedChestsFile);
			List<Map<?, ?>> cfgChests = config.getMapList("ignoredChests");
			unsortedChests = cfgChests.stream().map(UnsortedChest::fromConfig).collect(toSet());
		} else {
			unsortedChests = new HashSet<>();
		}
	}

	private boolean saveIgnoredChests() {
		List<Map<?, ?>> toSave = unsortedChests.stream().map(UnsortedChest::toConfig).collect(toList());

		YamlConfiguration yaml = new YamlConfiguration();
		yaml.set("ignoredChests", toSave);
		try {
			yaml.save(unsortedChestsFile);
			return true;
		} catch (IOException e) {
			plugin.getLogger().severe("Error while saving ignored chests: " + e.getClass().getSimpleName() + " " +  e.getMessage());
			return false;
		}
	}

	private void removeIgnoredChest(Player p, UnsortedChest chest) {
		boolean wasRemoved = unsortedChests.remove(chest);

		commitChange(p, chest, wasRemoved, " is already being sorted normally.", " will now be sorted again.");
	}

	private void addIgnoredChest(Player p, UnsortedChest chest) {
		boolean wasAdded = unsortedChests.add(chest);

		commitChange(p, chest, wasAdded, " is already not being sorted.", " will now not be sorted.");
	}

	private void commitChange(Player p, UnsortedChest chest, boolean success, String fail, String saveSuccess) {
		if (!success) {
			p.sendMessage(chest + fail);
			return;
		}
		if (saveIgnoredChests()) {
			p.sendMessage(chest + saveSuccess);
		} else {
			p.sendMessage("Error while changing chest sort ignore state. Please see the logs.");
		}
	}

	public void changeChestPreferences(Player p, List<String> args) {
		BlockState targetBlock = p.getTargetBlock(null, 10).getState();
		if (!Utils.isChest(targetBlock)) {
			p.sendMessage("Target is not a chest, actually of type " + targetBlock.getType());
			return;
		}
		InventoryHolder inventoryHolder = (InventoryHolder) targetBlock;

		UnsortedChest ignoredChest = new UnsortedChest(inventoryHolder.getInventory().getLocation());

		if (args.isEmpty() || args.get(0).equalsIgnoreCase("toggle")) {
			if (unsortedChests.contains(ignoredChest)) {
				removeIgnoredChest(p, ignoredChest);
			} else {
				addIgnoredChest(p, ignoredChest);
			}
		} else if (args.get(0).equalsIgnoreCase("disable")) {
			removeIgnoredChest(p, ignoredChest);
		} else if (args.get(0).equalsIgnoreCase("enable")) {
			addIgnoredChest(p, ignoredChest);
		}
	}

	public boolean isUnsortedChest(Inventory inv) {
		return unsortedChests.contains(new UnsortedChest(inv));
	}
}
