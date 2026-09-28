package com.example.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.item.ItemStack;

import java.util.function.Predicate;

public final class InvUtil {
    private InvUtil() {}

    public static int selected(MinecraftClient mc) { return mc.player.getInventory().getSelectedSlot(); }

    public static void select(MinecraftClient mc, int slot) {
        if (slot >= 0 && slot < 9 && slot != selected(mc)) mc.player.getInventory().setSelectedSlot(slot);
    }

    /** First hotbar slot (0-8) whose stack matches, or -1. */
    public static int findHotbar(MinecraftClient mc, Predicate<ItemStack> test) {
        for (int i = 0; i < 9; i++) if (test.test(mc.player.getInventory().getStack(i))) return i;
        return -1;
    }

    /** First slot anywhere in the main inventory (hotbar 0-8 + storage 9-35) whose stack matches, or -1. */
    public static int findAny(MinecraftClient mc, Predicate<ItemStack> test) {
        for (int i = 0; i < 36; i++) if (test.test(mc.player.getInventory().getStack(i))) return i;
        return -1;
    }

    /**
     * Player inventory index (0-35) -> PlayerScreenHandler slot id.
     * Hotbar (0-8) sits at 36-44 in the screen handler; main storage (9-35) keeps the same id.
     */
    public static int screenSlot(int invIndex) { return invIndex < 9 ? 36 + invIndex : invIndex; }

    /** Moves the item at inventory index `invIndex` into the given hotbar slot (0-8) via a SWAP click. */
    public static void swapToHotbar(MinecraftClient mc, int invIndex, int hotbarSlot) {
        mc.interactionManager.clickSlot(mc.player.playerScreenHandler.syncId, screenSlot(invIndex),
                hotbarSlot, net.minecraft.screen.slot.SlotActionType.SWAP, mc.player);
    }
}
