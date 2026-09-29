package com.example.client.modules;

import com.example.client.util.CombatUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/**
 * Hold right-click with end crystals in your main hand: places a crystal on the obsidian/bedrock
 * you're aiming at, and breaks any crystal you're aiming at.
 */
public class CrystalMacro extends Module {
    private int delay;

    public CrystalMacro() { super("Crystal Macro", Category.COMBAT); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (CombatUtil.busy(mc) || !mc.options.useKey.isPressed()) return;
        if (delay > 0) { delay--; return; }
        if (!mc.player.getMainHandStack().isOf(Items.END_CRYSTAL)) return;

        HitResult hit = mc.crosshairTarget;
        if (hit instanceof EntityHitResult e && e.getEntity() instanceof EndCrystalEntity crystal) {
            CombatUtil.attack(mc, crystal);
            delay = 1;
        } else if (hit instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK) {
            BlockPos pos = b.getBlockPos();
            BlockState state = mc.world.getBlockState(pos);
            if (!state.isOf(Blocks.OBSIDIAN) && !state.isOf(Blocks.BEDROCK)) return;

            BlockPos up = pos.up();
            if (!mc.world.isAir(up)) return;
            Box space = new Box(up.getX(), up.getY(), up.getZ(), up.getX() + 1, up.getY() + 2, up.getZ() + 1);
            if (!mc.world.getOtherEntities(null, space, en -> !en.isSpectator()).isEmpty()) return;

            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, b);
            mc.player.swingHand(Hand.MAIN_HAND);
            delay = 1;
        }
    }
}
