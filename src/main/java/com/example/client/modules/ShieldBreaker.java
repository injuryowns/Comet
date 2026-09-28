package com.example.client.modules;

import com.example.client.util.CombatUtil;
import com.example.client.util.InvUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.registry.tag.ItemTags;

/** When the entity you're aiming at is blocking with a shield, swaps to an axe and hits it. */
public class ShieldBreaker extends Module {
    private int prevSlot = -1;

    public ShieldBreaker() { super("Shield Breaker", Category.COMBAT); }

    private void restore(MinecraftClient mc) {
        if (prevSlot != -1 && mc.player != null) InvUtil.select(mc, prevSlot);
        prevSlot = -1;
    }

    @Override
    protected void onDisable() { restore(MinecraftClient.getInstance()); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (CombatUtil.busy(mc)) { restore(mc); return; }
        LivingEntity target = CombatUtil.crosshairLiving(mc);
        if (target == null || !target.isBlocking()) { restore(mc); return; }

        int axe = InvUtil.findHotbar(mc, s -> s.isIn(ItemTags.AXES));
        if (axe == -1) return;
        if (prevSlot == -1) prevSlot = InvUtil.selected(mc);
        InvUtil.select(mc, axe);
        // Swapping resets the cooldown, so this waits for the axe to recharge.
        if (CombatUtil.charged(mc, 0.9f)) CombatUtil.attack(mc, target);
    }
}
