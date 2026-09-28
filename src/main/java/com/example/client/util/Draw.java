package com.example.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** 2D-overlay wireframe helpers built on DrawContext.fill. */
public final class Draw {
    private Draw() {}

    private static final int[][] EDGES = {
            {0, 1}, {2, 3}, {4, 5}, {6, 7}, {0, 2}, {1, 3}, {4, 6}, {5, 7}, {0, 4}, {1, 5}, {2, 6}, {3, 7}};

    public static void line(DrawContext ctx, double[] a, double[] b, int color) {
        if (a == null || b == null) return;
        double dx = b[0] - a[0], dy = b[1] - a[1];
        int steps = (int) Math.ceil(Math.max(Math.abs(dx), Math.abs(dy)));
        if (steps > 700) return; // way off-screen
        if (steps < 1) steps = 1;
        for (int i = 0; i <= steps; i++) {
            int x = (int) (a[0] + dx * i / steps), y = (int) (a[1] + dy * i / steps);
            ctx.fill(x, y, x + 1, y + 1, color);
        }
    }

    public static void ring(DrawContext ctx, MinecraftClient mc, float td,
                            double cx, double cy, double cz, double r, int seg, int color) {
        double[] prev = null;
        for (int i = 0; i <= seg; i++) {
            double a = i * 2 * Math.PI / seg;
            double[] p = Projector.project(mc, new Vec3d(cx + Math.cos(a) * r, cy, cz + Math.sin(a) * r), td);
            if (prev != null) line(ctx, prev, p, color);
            prev = p;
        }
    }

    private static void wire(DrawContext ctx, MinecraftClient mc, float td, Vec3d[] c, int color) {
        double[][] p = new double[8][];
        for (int i = 0; i < 8; i++) p[i] = Projector.project(mc, c[i], td);
        for (int[] e : EDGES) line(ctx, p[e[0]], p[e[1]], color);
    }

    public static void box(DrawContext ctx, MinecraftClient mc, float td, Box b, int color) {
        Vec3d[] c = new Vec3d[8];
        for (int i = 0; i < 8; i++) {
            c[i] = new Vec3d((i & 1) == 0 ? b.minX : b.maxX, (i & 2) == 0 ? b.minY : b.maxY, (i & 4) == 0 ? b.minZ : b.maxZ);
        }
        wire(ctx, mc, td, c, color);
    }

    public static void cube(DrawContext ctx, MinecraftClient mc, float td, Vec3d center, double half, double rotY, int color) {
        double cos = Math.cos(rotY), sin = Math.sin(rotY);
        Vec3d[] c = new Vec3d[8];
        for (int i = 0; i < 8; i++) {
            double lx = (i & 1) == 0 ? -half : half, ly = (i & 2) == 0 ? -half : half, lz = (i & 4) == 0 ? -half : half;
            c[i] = center.add(lx * cos - lz * sin, ly, lx * sin + lz * cos);
        }
        wire(ctx, mc, td, c, color);
    }
}
