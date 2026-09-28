package com.example.client.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.util.math.Vec3d;

/**
 * Freezes your real player where it stands (so it stops sending movement to the server) while
 * letting you fly a separate camera position around with WASD/space/shift. Mouse look still turns
 * your real player's yaw/pitch, which CameraMixin reads to aim this free camera, so looking around
 * feels normal - only your position becomes independent of your body.
 */
public class Freecam extends Module {
    private Vec3d bodyPos;              // where the real player gets frozen
    public static volatile Vec3d camPos; // read by CameraMixin; null = camera follows the player normally
    private double speed = 0.6;

    public Freecam() { super("Freecam", Category.BASE); }

    { addInt("Speed x10", () -> (int) Math.round(speed * 10), v -> speed = v / 10.0, 1, 30); }

    @Override
    protected void onEnable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        bodyPos = new Vec3d(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        camPos = mc.player.getCameraPosVec(1.0f);
    }

    @Override
    protected void onDisable() {
        MinecraftClient mc = MinecraftClient.getInstance();
        ClientPlayerEntity p = mc.player;
        if (p != null && bodyPos != null) p.setPosition(bodyPos.x, bodyPos.y, bodyPos.z);
        bodyPos = null;
        camPos = null;
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || bodyPos == null) return;

        // Keep the real body frozen and out of the server's movement/physics.
        mc.player.setPosition(bodyPos.x, bodyPos.y, bodyPos.z);
        mc.player.setVelocity(Vec3d.ZERO);

        // Fly the camera using the player's current look direction (mouse still controls this).
        double yaw = Math.toRadians(mc.player.getYaw());
        double pitch = Math.toRadians(mc.player.getPitch());
        double fx = -Math.sin(yaw), fz = Math.cos(yaw);

        double dx = 0, dy = 0, dz = 0;
        if (mc.options.forwardKey.isPressed()) { dx += fx; dz += fz; }
        if (mc.options.backKey.isPressed()) { dx -= fx; dz -= fz; }
        if (mc.options.rightKey.isPressed()) { dx += fz; dz -= fx; }
        if (mc.options.leftKey.isPressed()) { dx -= fz; dz += fx; }
        if (mc.options.jumpKey.isPressed()) dy += 1;
        if (mc.options.sneakKey.isPressed()) dy -= 1;

        double len = Math.sqrt(dx * dx + dy * dy + dz * dz);
        if (len > 0.0001) {
            camPos = camPos.add(dx / len * speed, dy / len * speed, dz / len * speed);
        }
    }
}
