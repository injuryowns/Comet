package com.example.client.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

public abstract class Module {
    public enum Category { COMBAT, MOVEMENT, VISUALS, BASE, UTILITY }

    public final String name;
    public final Category category;
    /** Optional modes; right-click the module in the GUI to cycle. */
    public final String[] modes;
    public int mode;
    private boolean enabled;

    protected Module(String name, Category category, String... modes) {
        this.name = name;
        this.category = category;
        this.modes = modes;
    }

    public void toggle() {
        enabled = !enabled;
        if (enabled) onEnable(); else onDisable();
    }

    /** Turns the module off without ever throwing (used when a module crashes). */
    public void disable() {
        if (!enabled) return;
        enabled = false;
        try { onDisable(); } catch (Throwable ignored) {}
    }

    public boolean isEnabled() { return enabled; }
    public void cycleMode() { if (modes.length > 0) mode = (mode + 1) % modes.length; }
    public String modeName() { return modes.length == 0 ? "" : modes[mode]; }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float tickDelta) {}
    public void onAttack(Entity target) {}
}
