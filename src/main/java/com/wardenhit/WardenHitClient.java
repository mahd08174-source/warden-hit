package com.wardenhit;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.entity.mob.WardenEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

import java.util.List;

public class WardenHitClient implements ClientModInitializer {
	public static KeyBinding menuKey;

	@Override
	public void onInitializeClient() {
		Config.load();

		KeyBinding.Category category = KeyBinding.Category.create(Identifier.of("wardenhit", "main"));

		menuKey = KeyBindingHelper.registerKeyBinding(new KeyBinding(
				"key.wardenhit.menu", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_GRAVE_ACCENT, category));

		ClientTickEvents.END_CLIENT_TICK.register(WardenHitClient::tick);
	}

	private static void tick(MinecraftClient client) {
		if (client.player == null || client.world == null || client.interactionManager == null) {
			while (menuKey.wasPressed()) { }
			return;
		}

		while (menuKey.wasPressed()) {
			if (client.currentScreen == null) {
				client.setScreen(new WardenScreen());
			}
		}

		Config cfg = Config.get();
		if (!cfg.enabled || client.currentScreen != null) {
			return;
		}

		ClientPlayerEntity player = client.player;
		if (!player.isAlive() || player.isSpectator()) {
			return;
		}
		if (cfg.waitForCooldown && player.getAttackCooldownProgress(0.0f) < 1.0f) {
			return;
		}

		WardenEntity target = findWarden(client, player, cfg.range);
		if (target == null) {
			return;
		}

		// No rotation: just send the attack with whatever is in the main hand.
		client.interactionManager.attackEntity(player, target);
		player.swingHand(Hand.MAIN_HAND);
	}

	private static WardenEntity findWarden(MinecraftClient client, ClientPlayerEntity player, double range) {
		Vec3d eye = player.getEyePos();
		List<WardenEntity> wardens = client.world.getEntitiesByClass(
				WardenEntity.class, player.getBoundingBox().expand(range + 2.0), w -> w.isAlive());

		WardenEntity best = null;
		double bestDist = range * range;
		for (WardenEntity w : wardens) {
			double d = distanceToBox(eye, w.getBoundingBox());
			if (d <= bestDist) {
				bestDist = d;
				best = w;
			}
		}
		return best;
	}

	/** Squared distance from a point to the closest point of a box. */
	private static double distanceToBox(Vec3d p, Box b) {
		double dx = Math.max(Math.max(b.minX - p.x, 0.0), p.x - b.maxX);
		double dy = Math.max(Math.max(b.minY - p.y, 0.0), p.y - b.maxY);
		double dz = Math.max(Math.max(b.minZ - p.z, 0.0), p.z - b.maxZ);
		return dx * dx + dy * dy + dz * dz;
	}
}
