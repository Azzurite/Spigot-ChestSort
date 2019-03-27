package de.jeffclan.JeffChestSort;

import java.util.Arrays;
import java.util.List;

import de.jeffclan.utils.Utils;
import org.bukkit.block.BlockState;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.InventoryHolder;

public class JeffChestSortCommandExecutor implements CommandExecutor {

	JeffChestSortPlugin plugin;

	JeffChestSortCommandExecutor(JeffChestSortPlugin plugin) {
		this.plugin = plugin;
	}

	@Override
	public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

		if (command.getName().equalsIgnoreCase("chestsort")) {

			if (!(sender instanceof Player)) {
				sender.sendMessage(plugin.messages.MSG_PLAYERSONLY);
				return true;
			}


			Player p = (Player) sender;


			// fix for Spigot's stupid /reload function
			plugin.listener.registerPlayerIfNeeded(p);

			JeffChestSortPlayerSetting setting = plugin.PerPlayerSettings.get(p.getUniqueId().toString());


			if (args.length != 0 && args[0].equalsIgnoreCase("chest")) {
				changeChestPreferences(p, Arrays.asList(args).subList(1,args.length));
				return true;
			} else {
				toggleChestSort(p, setting);
			}

			return true;

		}

		return false;
	}

	private void toggleChestSort(Player p, JeffChestSortPlayerSetting setting) {
		setting.sortingEnabled = !setting.sortingEnabled;
		setting.hasSeenMessage = true;

		if (setting.sortingEnabled) {
			p.sendMessage(plugin.messages.MSG_ACTIVATED);
		} else {
			p.sendMessage(plugin.messages.MSG_DEACTIVATED);
		}
	}

	private void changeChestPreferences(Player p, List<String> args) {
		BlockState targetBlock = p.getTargetBlock(null, 10).getState();
		if (!Utils.isChest(targetBlock)) {
			p.sendMessage("Target is not a chest, actually of type " + targetBlock.getType());
			return;
		}
		InventoryHolder inventoryHolder = (InventoryHolder) targetBlock;

		IgnoredChest ignoredChest = new IgnoredChest(inventoryHolder.getInventory().getLocation());

		if (args.isEmpty() || args.get(0).equalsIgnoreCase("toggle")) {
			if (plugin.ignoredChests.contains(ignoredChest)) {
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

	private void removeIgnoredChest(Player p, IgnoredChest chest) {
		boolean wasRemoved = plugin.ignoredChests.remove(chest);

		commitChange(p, chest, wasRemoved, " is already being sorted normally.", " will now be sorted again.");
	}

	private void addIgnoredChest(Player p, IgnoredChest chest) {
		boolean wasAdded = plugin.ignoredChests.add(chest);

		commitChange(p, chest, wasAdded, " is already not being sorted.", " will now not be sorted.");
	}

	private void commitChange(Player p, IgnoredChest chest, boolean success, String fail, String saveSuccess) {
		if (!success) {
			p.sendMessage(chest + fail);
			return;
		}
		if (plugin.saveIgnoredChests()) {
			p.sendMessage(chest + saveSuccess);
		} else {
			p.sendMessage("Error while changing chest sort ignore state. Please see the logs.");
		}
	}

}
