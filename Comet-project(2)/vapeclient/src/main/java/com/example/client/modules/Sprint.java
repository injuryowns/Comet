package com.example.client.modules;

import net.minecraft.client.MinecraftClient;

public class Sprint extends Module {
    public Sprint() { super("Sprint", Category.MOVEMENT); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null) return;
        if (mc.player.input.playerInput.forward() && !mc.player.horizontalCollision) {
            mc.player.setSprinting(true);
        }
    }
}
