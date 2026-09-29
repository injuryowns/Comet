package com.example.client.modules;

import com.example.client.util.CombatUtil;
import com.example.client.util.InvUtil;
import net.minecraft.block.BlockState;
import net.minecraft.block.Blocks;
import net.minecraft.block.RespawnAnchorBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Hold right-click while aiming at a respawn anchor: charges it with glowstone, then swaps to a
 * non-glowstone item and detonates it. Only explodes outside the Nether. Don't sneak while using it.
 * SafeAnchor (subclass) also places a block between you and the anchor before detonating.
 */
public class AnchorMacro extends Module {
    private final boolean safe;
    private BlockPos anchor;
    private int step, delay, timeout;

    public AnchorMacro() { this("Anchor Macro", false); }

    protected AnchorMacro(String name, boolean safe) {
        super(name, Category.COMBAT);
        this.safe = safe;
    }

    private void reset() { anchor = null; step = 0; }

    @Override
    protected void onDisable() { reset(); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (CombatUtil.busy(mc) || mc.player.isSneaking()) { reset(); return; }
        if (delay > 0) { delay--; return; }

        if (anchor == null) {
            if (!mc.options.useKey.isPressed()) return;
            if (mc.crosshairTarget instanceof BlockHitResult b && b.getType() == HitResult.Type.BLOCK
                    && mc.world.getBlockState(b.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)) {
                anchor = b.getBlockPos();
                timeout = 40;
                step = mc.world.getBlockState(anchor).get(RespawnAnchorBlock.CHARGES) == 0 ? 0 : 1;
            } else return;
        }

        BlockState state = mc.world.getBlockState(anchor);
        if (!state.isOf(Blocks.RESPAWN_ANCHOR) || --timeout <= 0) { reset(); return; }
        int charges = state.get(RespawnAnchorBlock.CHARGES);

        switch (step) {
            case 0 -> { // charge
                int glow = InvUtil.findHotbar(mc, s -> s.isOf(Items.GLOWSTONE));
                if (glow == -1) { reset(); return; }
                InvUtil.select(mc, glow);
                use(mc, anchor, Direction.UP);
                step = 1;
                delay = 1;
            }
            case 1 -> { // wait for the server to confirm the charge
                if (charges > 0) step = safe ? 2 : 3;
            }
            case 2 -> { // safe mode: block between you and the anchor
                placeShield(mc);
                step = 3;
                delay = 1;
            }
            case 3 -> { // detonate with a non-glowstone item
                int slot = InvUtil.findHotbar(mc, s -> s.isOf(Items.TOTEM_OF_UNDYING));
                if (slot == -1) slot = InvUtil.findHotbar(mc, s -> !s.isOf(Items.GLOWSTONE));
                if (slot == -1) { reset(); return; }
                InvUtil.select(mc, slot);
                use(mc, anchor, Direction.UP);
                reset();
                delay = 4;
            }
        }
    }

    private void placeShield(MinecraftClient mc) {
        int slot = InvUtil.findHotbar(mc, s -> s.isOf(Items.OBSIDIAN));
        if (slot == -1) slot = InvUtil.findHotbar(mc, s -> s.isOf(Items.CRYING_OBSIDIAN));
        if (slot == -1) slot = InvUtil.findHotbar(mc, s -> s.getItem() instanceof BlockItem
                && !s.isOf(Items.GLOWSTONE) && !s.isOf(Items.RESPAWN_ANCHOR));
        if (slot == -1) return;

        double dx = mc.player.getX() - (anchor.getX() + 0.5);
        double dz = mc.player.getZ() - (anchor.getZ() + 0.5);
        Direction toPlayer = Direction.getFacing(dx, 0, dz);
        if (!mc.world.getBlockState(anchor.offset(toPlayer)).isReplaceable()) return;

        InvUtil.select(mc, slot);
        use(mc, anchor, toPlayer); // right-click the anchor's face that points at you
    }

    private void use(MinecraftClient mc, BlockPos pos, Direction face) {
        Vec3d at = Vec3d.ofCenter(pos).add(face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, new BlockHitResult(at, face, pos, false));
        mc.player.swingHand(Hand.MAIN_HAND);
    }
}
