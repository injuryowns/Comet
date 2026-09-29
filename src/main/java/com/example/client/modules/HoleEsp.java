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

/** Outlines bedrock/obsidian floors with open air above ("holes"). Sweeps across ticks instead of scanning fully every frame. */
public class HoleEsp extends Module {
    private static final int MIN_DEPTH = 2;
    private int range = 24;
    private final List<BlockPos> found = new ArrayList<>();
    private int cursor;

    public HoleEsp() { super("Hole ESP", Category.BASE); }

    { addInt("Range", () -> range, v -> { range = v; found.clear(); cursor = 0; }, 8, 48); }

    @Override
    protected void onDisable() { found.clear(); cursor = 0; }

    @Override
    public void onTick(MinecraftClient mc) {
        int py = mc.player.getBlockY();
        int span = range * 2 + 1, total = span * span;
        int budget = 300;

        for (int n = 0; n < budget; n++) {
            if (cursor >= total) { cursor = 0; found.clear(); }
            int dx = cursor % span - range, dz = cursor / span - range;
            cursor++;
            BlockPos floor = findFloor(mc, mc.player.getBlockPos().add(dx, 0, dz), py);
            if (floor != null) found.add(floor);
            if (found.size() > 100) break;
        }
    }

    private BlockPos findFloor(MinecraftClient mc, BlockPos column, int py) {
        for (int y = py - 6; y <= py + 2; y++) {
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

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        for (BlockPos h : found) {
            Box b = new Box(h.getX(), h.getY() + 1, h.getZ(), h.getX() + 1, h.getY() + 1 + MIN_DEPTH, h.getZ() + 1);
            Draw.box(ctx, mc, td, b, ColorUtil.alpha(ColorUtil.accent(0.4f), 0.7f));
        }
    }
}
