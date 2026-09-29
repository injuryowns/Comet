package com.example.client.modules;

import com.example.client.util.CombatUtil;
import com.example.client.util.InvUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.LivingEntity;
import net.minecraft.item.Items;

/**
 * While you're falling, swaps to the mace so it recharges mid-air, smashes whatever you're aiming at
 * once it's ready, then swaps back after you land.
 */
public class MaceMacro extends Module {
    private static final double MIN_FALL = 1.5;
    private int prevSlot = -1;

    public MaceMacro() { super("Mace Macro", Category.COMBAT); }

    private void restore(MinecraftClient mc) {
        if (prevSlot != -1 && mc.player != null) InvUtil.select(mc, prevSlot);
        prevSlot = -1;
    }

    @Override
    protected void onDisable() { restore(MinecraftClient.getInstance()); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (CombatUtil.busy(mc)) { restore(mc); return; }

        boolean falling = !mc.player.isOnGround() && mc.player.fallDistance >= MIN_FALL;
        if (!falling) { restore(mc); return; }

        int mace = InvUtil.findHotbar(mc, s -> s.isOf(Items.MACE));
        if (mace == -1) return;
        if (prevSlot == -1) prevSlot = InvUtil.selected(mc);
        InvUtil.select(mc, mace);

        LivingEntity target = CombatUtil.crosshairLiving(mc);
        if (target != null && CombatUtil.charged(mc, 0.9f)) CombatUtil.attack(mc, target);
    }
}
