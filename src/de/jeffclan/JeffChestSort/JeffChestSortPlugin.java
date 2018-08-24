package de.jeffclan.JeffChestSort;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import de.jeffclan.utils.Utils;

public class JeffChestSortPlugin extends JavaPlugin {

	Map<String, JeffChestSortPlayerSetting> PerPlayerSettings = new HashMap<String, JeffChestSortPlayerSetting>();
	JeffChestSortMessages messages;
	JeffChestSortOrganizer organizer;
	JeffChestSortUpdateChecker updateChecker;
	String sortingMethod;
	private int currentConfigVersion = 5;
	private boolean usingMatchingConfig = true;
	boolean debug = false;
	boolean verbose = true;
	private long updateCheckInterval = 86400; // in seconds. We check on startup and every 24 hours (if you never
	// restart your server)

	@Override
	public void onEnable() {

		/*
		 * if(debug) { System.out.println("======= ALL MATERIALS ======"); for(Material
		 * mat : Material.values()) {
		 * 
		 * System.out.println(mat.name().toLowerCase()); }
		 * System.out.println("============================"); }
		 */

		createConfig();
		saveDefaultCategories();
		verbose = getConfig().getBoolean("verbose");
		messages = new JeffChestSortMessages(this);
		organizer = new JeffChestSortOrganizer(this);
		updateChecker = new JeffChestSortUpdateChecker(this);
		sortingMethod = getConfig().getString("sorting-method");
		getServer().getPluginManager().registerEvents(new JeffChestSortListener(this), this);
		JeffChestSortCommandExecutor commandExecutor = new JeffChestSortCommandExecutor(this);
		this.getCommand("chestsort").setExecutor(commandExecutor);

		if (verbose) {
			getLogger().info("Current sorting method: " + sortingMethod);
			getLogger().info("Sorting enabled by default: " + getConfig().getBoolean("sorting-enabled-by-default"));
			getLogger().info("Auto generate category files: " + getConfig().getBoolean("auto-generate-category-files"));
			getLogger().info("Check for updates: " + getConfig().getString("check-for-updates"));
		}
		if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("true")) {
			Bukkit.getScheduler().scheduleSyncRepeatingTask(this, new Runnable() {
				public void run() {
					updateChecker.checkForUpdate();
				}
			}, 0L, updateCheckInterval * 20);
		} else if (getConfig().getString("check-for-updates", "true").equalsIgnoreCase("on-startup")) {
			updateChecker.checkForUpdate();
		}

		@SuppressWarnings("unused")
		Metrics metrics = new Metrics(this);

		metrics.addCustomChart(new Metrics.SimplePie("sorting_method", () -> sortingMethod));
		metrics.addCustomChart(new Metrics.SimplePie("config_version",
				() -> Integer.toString(getConfig().getInt("config-version", 0))));
		metrics.addCustomChart(
				new Metrics.SimplePie("check_for_updates", () -> getConfig().getString("check-for-updates", "true")));
		metrics.addCustomChart(new Metrics.SimplePie("show_message_when_using_chest",
				() -> Boolean.toString(getConfig().getBoolean("show-message-when-using-chest"))));
		metrics.addCustomChart(new Metrics.SimplePie("show_message_again_after_logout",
				() -> Boolean.toString(getConfig().getBoolean("show-message-again-after-logout"))));
		metrics.addCustomChart(new Metrics.SimplePie("sorting_enabled_by_default",
				() -> Boolean.toString(getConfig().getBoolean("sorting-enabled-by-default"))));
		metrics.addCustomChart(
				new Metrics.SimplePie("using_matching_config_version", () -> Boolean.toString(usingMatchingConfig)));

	}

	private void saveDefaultCategories() {
		String[] defaultCategories = { "900-valuables", "910-tools", "920-combat", "930-brewing", "940-food",
				"950-redstone", "960-wood", "970-stone", "980-plants", "981-corals" };

		if (getConfig().getBoolean("auto-generate-category-files", true) != true) {

			return;
		}

		for (String category : defaultCategories) {

			// getLogger().info("Saving default category file: " + category);

			FileOutputStream fopDefault = null;
			File fileDefault;

			try {
				InputStream in = getClass()
						.getResourceAsStream("/de/jeffclan/utils/categories/" + category + ".default.txt");

				fileDefault = new File(getDataFolder().getAbsolutePath() + File.separator + "categories"
						+ File.separator + category + ".txt");
				fopDefault = new FileOutputStream(fileDefault);

				// if file doesnt exists, then create it
				// if (!fileDefault.getAbsoluteFile().exists()) {
				fileDefault.createNewFile();
				// }

				// get the content in bytes
				byte[] contentInBytes = Utils.getBytes(in);

				fopDefault.write(contentInBytes);
				fopDefault.flush();
				fopDefault.close();

			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					if (fopDefault != null) {
						fopDefault.close();
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

	@Override
	public void onDisable() {
		for (Player p : getServer().getOnlinePlayers()) {
			unregisterPlayer(p);
		}
	}

	public boolean sortingEnabled(Player p) {
		return PerPlayerSettings.get(p.getUniqueId().toString()).sortingEnabled;
	}

	void createConfig() {
		this.saveDefaultConfig();

		// Config version prior to 5? Then it must have been generated by ChestSort 1.x
		if (getConfig().getInt("config-version", 0) < 5) {
			getLogger().warning("========================================================");
			getLogger().warning("You are using a config file that has been generated");
			getLogger().warning("prior to ChestSort version 2.0.0.");
			getLogger().warning("To allow everyone to use the new features, your config");
			getLogger().warning("has been renamed to config.old.yml and a new one has");
			getLogger().warning("been generated. Please examine the new config file to");
			getLogger().warning("see the new possibilities and adjust your settings.");
			getLogger().warning("========================================================");

			File configFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.yml");
			File oldConfigFile = new File(getDataFolder().getAbsolutePath() + File.separator + "config.old.yml");
			if (oldConfigFile.getAbsoluteFile().exists()) {
				oldConfigFile.getAbsoluteFile().delete();
			}
			configFile.getAbsoluteFile().renameTo(oldConfigFile.getAbsoluteFile());
			saveDefaultConfig();
			try {
				getConfig().load(configFile.getAbsoluteFile());
			} catch (IOException | InvalidConfigurationException e) {
				getLogger().warning("Could not load freshly generated config file!");
				e.printStackTrace();
			}
		} else if (getConfig().getInt("config-version", 0) != currentConfigVersion) {
			getLogger().warning("========================================================");
			getLogger().warning("YOU ARE USING AN OLD CONFIG FILE!");
			getLogger().warning("This is not a problem, as ChestSort will just use the");
			getLogger().warning("default settings for unset values. However, if you want");
			getLogger().warning("to configure the new options, please go to");
			getLogger().warning("https://www.spigotmc.org/resources/1-13-chestsort.59773/");
			getLogger().warning("and replace your config.yml with the new one. You can");
			getLogger().warning("then insert your old changes into the new file.");
			getLogger().warning("========================================================");
			usingMatchingConfig = false;
		}

		File playerDataFolder = new File(getDataFolder().getPath() + File.separator + "playerdata");
		if (!playerDataFolder.getAbsoluteFile().exists()) {
			playerDataFolder.mkdir();
		}
		File categoriesFolder = new File(getDataFolder().getPath() + File.separator + "categories");
		if (!categoriesFolder.getAbsoluteFile().exists()) {
			categoriesFolder.mkdir();
		}

		getConfig().addDefault("sorting-enabled-by-default", false);
		getConfig().addDefault("show-message-when-using-chest", true);
		getConfig().addDefault("show-message-when-using-chest-and-sorting-is-enabled", false);
		getConfig().addDefault("show-message-again-after-logout", true);
		getConfig().addDefault("sorting-method", "{category},{itemsFirst},{name},{color}");
		getConfig().addDefault("check-for-updates", "true");
		getConfig().addDefault("auto-generate-category-files", true);
		getConfig().addDefault("verbose", true);
	}

	void unregisterPlayer(Player p) {
		UUID uniqueId = p.getUniqueId();
		if (PerPlayerSettings.containsKey(uniqueId.toString())) {
			JeffChestSortPlayerSetting setting = PerPlayerSettings.get(p.getUniqueId().toString());
			File playerFile = new File(getDataFolder() + File.separator + "playerdata",
					p.getUniqueId().toString() + ".yml");
			YamlConfiguration playerConfig = YamlConfiguration.loadConfiguration(playerFile);
			playerConfig.set("sortingEnabled", setting.sortingEnabled);
			playerConfig.set("hasSeenMessage", setting.hasSeenMessage);
			try {
				playerConfig.save(playerFile);
			} catch (IOException e) {
				e.printStackTrace();
			}

			PerPlayerSettings.remove(uniqueId.toString());
		}
	}

}
