package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Vec3d;

/** Six spinning wireframe cubes orbiting you. */
public class FloatingCubes extends Module {
    public FloatingCubes() { super("Floating Cubes", Category.VISUALS); }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        double t = System.currentTimeMillis() / 1000.0;
        Vec3d base = Projector.lerpPos(mc.player, td);
        for (int i = 0; i < 6; i++) {
            double a = t * 0.9 + i * Math.PI / 3;
            Vec3d c = base.add(Math.cos(a) * 1.3, 1.0 + 0.25 * Math.sin(t * 1.5 + i), Math.sin(a) * 1.3);
            Draw.cube(ctx, mc, td, c, 0.1, t * 2 + i, ColorUtil.accent(i * 0.15f));
        }
    }
}
