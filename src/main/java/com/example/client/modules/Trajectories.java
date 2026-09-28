package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ChargedProjectilesComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

/** Predicts the flight path of the projectile in your main hand (bow, crossbow, trident, pearl, snowball, egg, potion). */
public class Trajectories extends Module {
    private record Proj(double speed, double gravity) {}

    public Trajectories() { super("Trajectories", Category.VISUALS); }

    private Proj current(MinecraftClient mc) {
        ItemStack s = mc.player.getMainHandStack();
        if (s.isOf(Items.BOW)) {
            if (!mc.player.isUsingItem()) return null;
            float f = mc.player.getItemUseTime() / 20f;
            f = (f * f + f * 2f) / 3f;
            if (f < 0.1f) return null;
            return new Proj(Math.min(f, 1f) * 3.0, 0.05);
        }
        if (s.isOf(Items.CROSSBOW)) {
            ChargedProjectilesComponent c = s.get(DataComponentTypes.CHARGED_PROJECTILES);
            return (c == null || c.isEmpty()) ? null : new Proj(3.15, 0.05);
        }
        if (s.isOf(Items.TRIDENT)) return mc.player.isUsingItem() ? new Proj(2.5, 0.05) : null;
        if (s.isOf(Items.ENDER_PEARL) || s.isOf(Items.SNOWBALL) || s.isOf(Items.EGG)) return new Proj(1.5, 0.03);
        if (s.isOf(Items.SPLASH_POTION) || s.isOf(Items.LINGERING_POTION)) return new Proj(0.5, 0.05);
        return null;
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        Proj p = current(mc);
        if (p == null) return;

        double yaw = Math.toRadians(mc.player.getYaw(td)), pitch = Math.toRadians(mc.player.getPitch(td));
        Vec3d dir = new Vec3d(-Math.sin(yaw) * Math.cos(pitch), -Math.sin(pitch), Math.cos(yaw) * Math.cos(pitch));
        Vec3d pos = Projector.camera(mc, td).add(-Math.cos(yaw) * 0.16, -0.1, -Math.sin(yaw) * 0.16);
        Vec3d vel = dir.multiply(p.speed());

        List<Vec3d> pts = new ArrayList<>();
        pts.add(pos);
        Vec3d landing = null;
        for (int i = 0; i < 120; i++) {
            Vec3d next = pos.add(vel);
            BlockHitResult hit = mc.world.raycast(new RaycastContext(pos, next,
                    RaycastContext.ShapeType.COLLIDER, RaycastContext.FluidHandling.NONE, mc.player));
            if (hit.getType() != HitResult.Type.MISS) {
                landing = hit.getPos();
                pts.add(landing);
                break;
            }
            pts.add(next);
            pos = next;
            vel = vel.multiply(0.99).add(0, -p.gravity(), 0);
        }

        double[] prev = Projector.project(mc, pts.get(0), td);
        for (int i = 1; i < pts.size(); i++) {
            double[] cur = Projector.project(mc, pts.get(i), td);
            Draw.line(ctx, prev, cur, ColorUtil.alpha(ColorUtil.accent(i * 0.01f), 0.9f));
            prev = cur;
        }
        if (landing != null) Draw.box(ctx, mc, td, Box.of(landing, 0.3, 0.3, 0.3), ColorUtil.accent(0.5f));
    }
}
