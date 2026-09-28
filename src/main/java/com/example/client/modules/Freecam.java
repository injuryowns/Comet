package com.example.client.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Detaches the camera from your body: you keep flying the view around, but your real player stays
 * put and stops sending movement to the server, so your body just stands there.
 */
public class Freecam extends Module {
    private Vec3d frozenPos;
    private float frozenYaw, frozenPitch;

    public Freecam() { super("Freecam", Category.BASE); }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player != null) {
            frozenPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            frozenYaw = mc.player.getYaw();
            frozenPitch = mc.player.getPitch();
        }
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p != null && frozenPos != null) {
            p.setPosition(frozenPos.x, frozenPos.y, frozenPos.z);
            p.setYaw(frozenYaw);
            p.setPitch(frozenPitch);
        }
        frozenPos = null;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || frozenPos == null) return;
        // Hold the real player still and desynced from the camera every tick this is on.
        mc.player.setPosition(frozenPos.x, frozenPos.y, frozenPos.z);
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setYaw(frozenYaw);
        mc.player.setPitch(frozenPitch);
    }
}
