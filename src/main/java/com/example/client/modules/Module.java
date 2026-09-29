package com.example.client.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import com.example.client.config.Setting;
import java.util.ArrayList;
import java.util.List;

public abstract class Module {
    public enum Category { COMBAT, MOVEMENT, VISUALS, BASE, UTILITY }

    public final String name;
    public final Category category;
    /** Optional modes; right-click the module in the GUI to cycle. */
    public final String[] modes;
    public int mode;
    private boolean enabled;

    public final List<Setting<?>> settings = new ArrayList<>();

    protected Module(String name, Category category, String... modes) {
        this.name = name;
        this.category = category;
        this.modes = modes;
    }

    protected Setting.IntSetting addInt(String name, java.util.function.Supplier<Integer> g, java.util.function.Consumer<Integer> s, int min, int max) {
        Setting.IntSetting st = new Setting.IntSetting(name, g, s, min, max);
        settings.add(st);
        return st;
    }

    protected Setting.BoolSetting addBool(String name, java.util.function.Supplier<Boolean> g, java.util.function.Consumer<Boolean> s) {
        Setting.BoolSetting st = new Setting.BoolSetting(name, g, s);
        settings.add(st);
        return st;
    }

    public boolean hasPanel() { return !settings.isEmpty() || modes.length > 0; }

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
    public void cycleMode() {
        if (modes.length == 0) return;
        if (mode < 0 || mode >= modes.length) mode = 0;
        else mode = (mode + 1) % modes.length;
    }
    public String modeName() { return modes.length == 0 ? "" : modes[mode]; }

    protected void onEnable() {}
    protected void onDisable() {}
    public void onTick(MinecraftClient mc) {}
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float tickDelta) {}
    public void onAttack(Entity target) {}
}
