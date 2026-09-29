package com.example.client.modules;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

import java.util.Random;

/** Extra client-side particles. Right-click: Hit (burst when you attack) / Trail (behind you as you move). */
public class Particles extends Module {
    private final Random rng = new Random();

    public Particles() { super("Particles", Category.VISUALS, "Hit", "Trail"); }

    @Override
    public void onAttack(Entity e) {
        if (mode != 0) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.particleManager == null) return;
        double cy = e.getY() + e.getHeight() * 0.5;
        for (int i = 0; i < 14; i++) {
            mc.particleManager.addParticle(i % 2 == 0 ? ParticleTypes.CRIT : ParticleTypes.ENCHANTED_HIT,
                    e.getX(), cy, e.getZ(),
                    (rng.nextDouble() - 0.5) * 0.8, rng.nextDouble() * 0.5, (rng.nextDouble() - 0.5) * 0.8);
        }
    }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mode != 1 || mc.particleManager == null) return;
        Vec3d v = mc.player.getVelocity();
        if (v.horizontalLengthSquared() < 0.003) return;
        mc.particleManager.addParticle(ParticleTypes.END_ROD,
                mc.player.getX() + (rng.nextDouble() - 0.5) * 0.4, mc.player.getY() + 0.1, mc.player.getZ() + (rng.nextDouble() - 0.5) * 0.4,
                0, 0.01, 0);
    }
}
