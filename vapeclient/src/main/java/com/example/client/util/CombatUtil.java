package com.example.client.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.decoration.ArmorStandEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;

public final class CombatUtil {
    private CombatUtil() {}

    /** The living entity under the crosshair, or null. */
    public static LivingEntity crosshairLiving(MinecraftClient mc) {
        if (mc.crosshairTarget instanceof EntityHitResult hit
                && hit.getEntity() instanceof LivingEntity le
                && le.isAlive() && le != mc.player && !(le instanceof ArmorStandEntity)) {
            return le;
        }
        return null;
    }

    public static boolean charged(MinecraftClient mc, float threshold) {
        return mc.player.getAttackCooldownProgress(0.0f) >= threshold;
    }

    public static void attack(MinecraftClient mc, Entity target) {
        mc.interactionManager.attackEntity(mc.player, target);
        mc.player.swingHand(Hand.MAIN_HAND);
    }

    public static boolean busy(MinecraftClient mc) {
        return mc.player == null || mc.world == null || mc.interactionManager == null || mc.currentScreen != null;
    }
}
