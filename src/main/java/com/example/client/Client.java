package com.example.client;

import com.example.client.gui.ClickGui;
import com.example.client.modules.Module;
import com.example.client.modules.ModuleManager;
import com.example.client.util.ColorUtil;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.event.player.AttackEntityCallback;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Identifier;
import org.lwjgl.glfw.GLFW;

import java.util.Comparator;
import java.util.List;

public class Client implements ClientModInitializer {
    public static final ModuleManager MODULES = new ModuleManager();

    private static KeyBinding guiKey;

    /** A broken module turns itself off instead of crashing the game. */
    private static void guard(Module m, Runnable r) {
        try {
            r.run();
        } catch (Throwable t) {
            System.err.println("[Comet] " + m.name + " crashed and was turned off: " + t);
            t.printStackTrace();
            m.disable();
        }
    }

    @Override
    public void onInitializeClient() {
        Settings.load();

        // 1.21.9+ keybinds take a Category object instead of a String.
        KeyBinding.Category cat = KeyBinding.Category.create(Identifier.of("comet", "main"));
        guiKey = KeyBindingHelper.registerKeyBinding(
                new KeyBinding("key.comet.gui", InputUtil.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT, cat));

        ClientTickEvents.END_CLIENT_TICK.register(mc -> {
            while (guiKey.wasPressed()) mc.setScreen(new ClickGui());
            if (mc.player == null) return;
            for (Module m : MODULES.modules) if (m.isEnabled()) guard(m, () -> m.onTick(mc));
        });

        AttackEntityCallback.EVENT.register((player, world, hand, entity, hit) -> {
            if (world.isClient() && player == MinecraftClient.getInstance().player) {
                for (Module m : MODULES.modules) if (m.isEnabled()) guard(m, () -> m.onAttack(entity));
            }
            return ActionResult.PASS;
        });

        HudRenderCallback.EVENT.register((ctx, tick) -> {
            MinecraftClient mc = MinecraftClient.getInstance();
            if (mc.player == null) return;
            float td = tick.getTickProgress(false);

            for (Module m : MODULES.modules) if (m.isEnabled()) guard(m, () -> m.onRender2D(ctx, mc, td));

            // Watermark
            int ww = mc.textRenderer.getWidth("Comet");
            ctx.fill(2, 2, ww + 10, 14, 0x90101014);
            ctx.fill(2, 2, 4, 14, ColorUtil.accent(0));
            ctx.drawTextWithShadow(mc.textRenderer, "Comet", 7, 4, ColorUtil.accent(0.3f));

            if (!Settings.arraylist) return;
            List<Module> on = MODULES.modules.stream()
                    .filter(Module::isEnabled)
                    .sorted(Comparator.comparingInt((Module m) -> mc.textRenderer.getWidth(m.name)).reversed())
                    .toList();
            int y = 4, w = ctx.getScaledWindowWidth(), i = 0;
            for (Module m : on) {
                int tw = mc.textRenderer.getWidth(m.name);
                int col = ColorUtil.accent(i++ * 0.12f);
                ctx.fill(w - tw - 8, y - 1, w - 2, y + 10, 0x90101014);
                ctx.fill(w - 2, y - 1, w, y + 10, col);
                ctx.drawTextWithShadow(mc.textRenderer, m.name, w - tw - 5, y + 1, col);
                y += 11;
            }
        });
    }
}
