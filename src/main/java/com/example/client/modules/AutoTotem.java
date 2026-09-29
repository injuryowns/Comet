package com.example.client.modules;

import com.example.client.util.InvUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.Items;
import net.minecraft.screen.slot.SlotActionType;

/** Keeps a totem of undying in your offhand, refilling it from anywhere in your inventory (not just the hotbar) after a pop. */
public class AutoTotem extends Module {
    private int delay;

    public AutoTotem() { super("Auto Totem", Category.COMBAT); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.interactionManager == null || mc.currentScreen != null) return;
        if (delay > 0) { delay--; return; }
        if (mc.player.getOffHandStack().isOf(Items.TOTEM_OF_UNDYING)) return;

        int idx = InvUtil.findAny(mc, s -> s.isOf(Items.TOTEM_OF_UNDYING));
        if (idx == -1) return;

        // SWAP with button 40 exchanges the clicked slot with the offhand.
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, InvUtil.screenSlot(idx), 40, SlotActionType.SWAP, mc.player);
        delay = 2;
    }
}
