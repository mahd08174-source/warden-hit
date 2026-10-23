package com.wardenhit;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/** Settings saved to config/warden-hit.json. */
public class Config {
	private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
	private static Config instance = new Config();

	/** Master switch for the auto-hit. */
	public boolean enabled = true;
	/** Only swing while falling far enough for a mace smash (fall distance > 1.5). */
	public boolean onlyWhileFalling = true;
	/** While falling, hold the hit until the Warden is about one tick away so the whole fall counts toward smash damage. */
	public boolean maxSmashTiming = true;
	/** Max distance (blocks) from your eyes to the Warden's hitbox. */
	public double range = 3.0;
	/** Only swing once the attack cooldown is full (full damage hits). */
	public boolean waitForCooldown = true;

	public static Config get() {
		return instance;
	}

	private static Path path() {
		return FabricLoader.getInstance().getConfigDir().resolve("warden-hit.json");
	}

	public static void load() {
		try {
			Path p = path();
			if (Files.exists(p)) {
				Config loaded = GSON.fromJson(Files.readString(p), Config.class);
				if (loaded != null) {
					instance = loaded;
				}
			}
		} catch (Exception e) {
			instance = new Config();
		}
		instance.clamp();
	}

	public static void save() {
		instance.clamp();
		try {
			Files.writeString(path(), GSON.toJson(instance));
		} catch (IOException ignored) {
		}
	}

	private void clamp() {
		range = Math.max(1.0, Math.min(6.0, range));
	}
}
