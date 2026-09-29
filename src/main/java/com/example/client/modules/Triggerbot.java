package com.example.client.modules;

import com.example.client.util.CombatUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;

/** Attacks whatever living entity is under your crosshair once your weapon is charged. */
public class Triggerbot extends Module {
    public Triggerbot() { super("Triggerbot", Category.COMBAT); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (CombatUtil.busy(mc) || mc.player.isUsingItem()) return;
        LivingEntity target = CombatUtil.crosshairLiving(mc);
        if (target != null && CombatUtil.charged(mc, 0.95f)) CombatUtil.attack(mc, target);
    }
}
