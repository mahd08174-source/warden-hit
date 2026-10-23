package com.wardenhit;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.ButtonWidget;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

/** Settings menu, opened with the ` key. */
public class WardenScreen extends Screen {
	private TextFieldWidget rangeField;

	public WardenScreen() {
		super(Text.literal("Warden Hit"));
	}

	@Override
	protected void init() {
		Config cfg = Config.get();
		int cx = this.width / 2;
		int y = this.height / 2 - 68;

		addDrawableChild(ButtonWidget.builder(toggleText(), b -> {
			Config.get().enabled = !Config.get().enabled;
			b.setMessage(toggleText());
		}).dimensions(cx - 100, y, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(cooldownText(), b -> {
			Config.get().waitForCooldown = !Config.get().waitForCooldown;
			b.setMessage(cooldownText());
		}).dimensions(cx - 100, y + 26, 200, 20).build());

		addDrawableChild(ButtonWidget.builder(fallingText(), b -> {
			Config.get().onlyWhileFalling = !Config.get().onlyWhileFalling;
			b.setMessage(fallingText());
		}).dimensions(cx - 100, y + 52, 200, 20).build());

		rangeField = new TextFieldWidget(this.textRenderer, cx + 30, y + 78, 70, 20, Text.literal("Range"));
		rangeField.setTextPredicate(s -> s.matches("\\d{0,1}(\\.\\d{0,2})?"));
		rangeField.setText(trimNumber(cfg.range));
		rangeField.setChangedListener(s -> {
			try {
				double v = Double.parseDouble(s);
				if (v >= 1.0 && v <= 6.0) Config.get().range = v;
			} catch (NumberFormatException ignored) {
			}
		});
		addDrawableChild(rangeField);

		addDrawableChild(ButtonWidget.builder(Text.literal("Done"), b -> close())
				.dimensions(cx - 100, y + 110, 200, 20).build());
	}

	private Text toggleText() {
		return Text.literal("Auto-hit Warden: " + (Config.get().enabled ? "ON" : "OFF"));
	}

	private Text cooldownText() {
		return Text.literal("Wait for attack cooldown: " + (Config.get().waitForCooldown ? "ON" : "OFF"));
	}

	private Text fallingText() {
		return Text.literal("Only hit while falling (mace): " + (Config.get().onlyWhileFalling ? "ON" : "OFF"));
	}

	private static String trimNumber(double d) {
		String s = String.format(Locale.ROOT, "%.2f", d);
		s = s.replaceAll("0+$", "");
		if (s.endsWith(".")) s = s.substring(0, s.length() - 1);
		return s;
	}

	@Override
	public void render(DrawContext context, int mouseX, int mouseY, float delta) {
		super.render(context, mouseX, mouseY, delta);
		int cx = this.width / 2;
		int y = this.height / 2 - 68;
		context.drawCenteredTextWithShadow(this.textRenderer, this.title, cx, y - 20, 0xFFFFFFFF);
		context.drawTextWithShadow(this.textRenderer, Text.literal("Range (1-6 blocks)"), cx - 100, y + 84, 0xFFFFFFFF);
	}

	@Override
	public boolean keyPressed(KeyInput input) {
		if (input.key() == GLFW.GLFW_KEY_GRAVE_ACCENT) {
			close();
			return true;
		}
		return super.keyPressed(input);
	}

	@Override
	public void removed() {
		Config.save();
	}
}
