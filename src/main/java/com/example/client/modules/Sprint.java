package com.example.client.modules;

import net.minecraft.client.MinecraftClient;

/**
 * Auto-sprint. Uses the vanilla "move forward" keybind directly rather than a raw input accessor,
 * since that's the API least likely to have moved between versions.
 */
public class Sprint extends Module {
    private boolean underwater = true;

    public Sprint() { super("Sprint", Category.MOVEMENT); }

    { addBool("Sprint underwater", () -> underwater, v -> underwater = v); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        boolean swimming = mc.player.isTouchingWater() || mc.player.isSubmergedInWater();
        if (swimming && !underwater) return;
        if (mc.options.forwardKey.isPressed() && mc.player.hungerManager.getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
