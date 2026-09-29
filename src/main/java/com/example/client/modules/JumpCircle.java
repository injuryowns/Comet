package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

import java.util.ArrayList;
import java.util.List;

/** Spawns an expanding, fading ring under your feet every time you jump. */
public class JumpCircle extends Module {
    private record Ring(Vec3d pos, long start) {}
    private static final long LIFE_MS = 700;

    private final List<Ring> rings = new ArrayList<>();
    private boolean wasGround = true;

    public JumpCircle() { super("Jump Circle", Category.VISUALS); }

    @Override
    protected void onDisable() { rings.clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        boolean ground = mc.player.isOnGround();
        if (wasGround && !ground && mc.player.getVelocity().y > 0.1) {
            rings.add(new Ring(new Vec3d(mc.player.getX(), mc.player.getY() + 0.02, mc.player.getZ()), System.currentTimeMillis()));
        }
        wasGround = ground;
        long now = System.currentTimeMillis();
        rings.removeIf(r -> now - r.start() > LIFE_MS);
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        long now = System.currentTimeMillis();
        for (Ring r : rings) {
            float t = (now - r.start()) / (float) LIFE_MS;
            double radius = 0.3 + 1.5 * t;
            int color = ColorUtil.alpha(ColorUtil.accent(t), 1f - t);
            Draw.ring(ctx, mc, td, r.pos().x, r.pos().y, r.pos().z, radius, 28, color);
        }
    }
}
