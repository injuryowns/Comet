package com.example.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * World -> screen projection done by hand (yaw/pitch/FOV), so overlays are drawn with plain
 * DrawContext calls instead of the version-specific world render pipeline. Accurate in first
 * person only; there is no depth test, so overlays show through walls.
 */
public final class Projector {
    private Projector() {}

    public static boolean usable(MinecraftClient mc) {
        return mc.player != null && mc.world != null && mc.currentScreen == null
                && mc.options.getPerspective().isFirstPerson();
    }

    public static Vec3d lerpPos(Entity e, float td) {
        return new Vec3d(MathHelper.lerp(td, e.lastRenderX, e.getX()),
                MathHelper.lerp(td, e.lastRenderY, e.getY()),
                MathHelper.lerp(td, e.lastRenderZ, e.getZ()));
    }

    public static Vec3d camera(MinecraftClient mc, float td) {
        PlayerEntity p = mc.player;
        return lerpPos(p, td).add(0, p.getEyeHeight(p.getPose()), 0);
    }

    /** Scaled-GUI screen coordinates, or null if the point is behind the camera. */
    public static double[] project(MinecraftClient mc, Vec3d point, float td) {
        PlayerEntity pl = mc.player;
        Vec3d cam = camera(mc, td);
        double yaw = Math.toRadians(pl.getYaw(td)), pitch = Math.toRadians(pl.getPitch(td));
        double cy = Math.cos(yaw), sy = Math.sin(yaw), cp = Math.cos(pitch), sp = Math.sin(pitch);

        double fx = -sy * cp, fy = -sp, fz = cy * cp;          // forward
        double rx = -cy, ry = 0, rz = -sy;                     // right
        double ux = ry * fz - rz * fy, uy = rz * fx - rx * fz, uz = rx * fy - ry * fx; // up = right x forward

        Vec3d d = point.subtract(cam);
        double z = d.x * fx + d.y * fy + d.z * fz;
        if (z < 0.05) return null;
        double x = d.x * rx + d.y * ry + d.z * rz;
        double y = d.x * ux + d.y * uy + d.z * uz;

        int w = mc.getWindow().getScaledWidth(), h = mc.getWindow().getScaledHeight();
        double fov = Math.toRadians(mc.options.getFov().getValue());
        double scale = (h / 2.0) / Math.tan(fov / 2.0);
        return new double[]{w / 2.0 + x / z * scale, h / 2.0 - y / z * scale};
    }
}
