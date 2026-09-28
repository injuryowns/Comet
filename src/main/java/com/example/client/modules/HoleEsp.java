package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/** Outlines classic "holes": bedrock/obsidian floors two-plus blocks deep with open air above, within render range. */
public class HoleEsp extends Module {
    private static final int RANGE = 24, MIN_DEPTH = 2;

    public HoleEsp() { super("Hole ESP", Category.BASE); }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        BlockPos p = mc.player.getBlockPos();
        List<BlockPos> holes = new ArrayList<>();
        for (int dx = -RANGE; dx <= RANGE; dx++) {
            for (int dz = -RANGE; dz <= RANGE; dz++) {
                BlockPos floor = findFloor(mc, p.add(dx, 0, dz));
                if (floor != null) holes.add(floor);
                if (holes.size() > 40) break; // cap for frame cost
            }
        }
        for (BlockPos h : holes) {
            Box b = new Box(h.getX(), h.getY() + 1, h.getZ(), h.getX() + 1, h.getY() + 1 + MIN_DEPTH, h.getZ() + 1);
            Draw.box(ctx, mc, td, b, ColorUtil.alpha(ColorUtil.accent(0.4f), 0.7f));
        }
    }

    /** Bedrock/obsidian at y, MIN_DEPTH of air directly above it, open to the sky. */
    private BlockPos findFloor(MinecraftClient mc, BlockPos column) {
        for (int y = mc.player.getBlockY() - 6; y <= mc.player.getBlockY() + 2; y++) {
            BlockPos floor = new BlockPos(column.getX(), y, column.getZ());
            var state = mc.world.getBlockState(floor);
            if (!state.isOf(Blocks.BEDROCK) && !state.isOf(Blocks.OBSIDIAN)) continue;
            boolean open = true;
            for (int i = 1; i <= MIN_DEPTH; i++) {
                if (!mc.world.getBlockState(floor.up(i)).isAir()) { open = false; break; }
            }
            if (open) return floor;
        }
        return null;
    }
}
