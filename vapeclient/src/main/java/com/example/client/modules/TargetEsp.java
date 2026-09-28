package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.CombatUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** Highlights whoever you last hit or are aiming at. Right-click: Box / Ring. */
public class TargetEsp extends Module {
    private LivingEntity target;
    private long last;

    public TargetEsp() { super("Target ESP", Category.VISUALS, "Box", "Ring"); }

    @Override
    protected void onDisable() { target = null; }

    @Override
    public void onAttack(Entity e) {
        if (e instanceof LivingEntity le) { target = le; last = System.currentTimeMillis(); }
    }

    @Override
    public void onTick(MinecraftClient mc) {
        LivingEntity c = CombatUtil.crosshairLiving(mc);
        long now = System.currentTimeMillis();
        if (c != null) { target = c; last = now; }
        if (target != null && (!target.isAlive() || now - last > 2500)) target = null;
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (target == null || !Projector.usable(mc)) return;
        Vec3d p = Projector.lerpPos(target, td);

        if (mode == 0) {
            Box b = target.getBoundingBox().offset(p.x - target.getX(), p.y - target.getY(), p.z - target.getZ());
            Draw.box(ctx, mc, td, b, ColorUtil.accent(0));
        } else {
            double h = target.getHeight(), r = target.getWidth() * 0.8 + 0.2;
            double t = (System.currentTimeMillis() % 1400) / 1400.0;
            for (int k = 0; k < 3; k++) {
                double y = p.y + h * (0.5 + 0.5 * Math.sin((t - k * 0.06) * 2 * Math.PI));
                Draw.ring(ctx, mc, td, p.x, y, p.z, r, 24, ColorUtil.alpha(ColorUtil.accent(k * 0.1f), 1f - k * 0.3f));
            }
        }
    }
}
