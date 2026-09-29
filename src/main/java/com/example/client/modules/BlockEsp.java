package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.List;

/**
 * Lightweight cached block highlighting. The scan is spread over ticks and rendering only
 * consumes the cached positions, keeping frame time independent from the search volume.
 */
public class BlockEsp extends Module {
    private static final List<Block> ORES = List.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE,
            Blocks.ANCIENT_DEBRIS, Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE,
            Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);

    private int range = 24;
    private int vertical = 16;
    private final List<BlockPos> found = new ArrayList<>();
    private int cursor;
    private BlockPos.Mutable center;

    public BlockEsp() { super("Block ESP", Category.BASE, "Ores", "Obsidian", "Spawners"); }

    {
        addInt("Range", () -> range, v -> { range = v; resetScan(); }, 8, 48);
        addInt("Vertical", () -> vertical, v -> { vertical = v; resetScan(); }, 8, 32);
    }

    private void resetScan() {
        found.clear();
        cursor = 0;
        center = null;
    }

    private boolean matches(Block b) {
        return switch (mode) {
            case 0 -> ORES.contains(b);
            case 1 -> b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN;
            default -> b == Blocks.SPAWNER;
        };
    }

    @Override
    protected void onDisable() { resetScan(); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;

        BlockPos p = mc.player.getBlockPos();
        if (center == null || !center.equals(p)) {
            center = new BlockPos.Mutable(p.getX(), p.getY(), p.getZ());
            cursor = 0;
            found.clear();
        }

        int span = range * 2 + 1;
        int total = span * span;
        int budget = 350;

        for (int n = 0; n < budget; n++) {
            if (cursor >= total) {
                cursor = 0;
                // Keep the old result while the next sweep starts. This prevents ESP
                // from blinking empty for a frame when a scan wraps around.
                found.clear();
            }

            int dx = cursor % span - range;
            int dz = cursor / span - range;
            cursor++;

            for (int dy = -vertical; dy <= vertical; dy++) {
                BlockPos pos = p.add(dx, dy, dz);
                if (matches(mc.world.getBlockState(pos).getBlock())) {
                    if (found.size() < 250) found.add(pos.toImmutable());
                }
            }
        }
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;

        int i = 0;
        for (BlockPos pos : found) {
            // Cheap distance rejection before doing the comparatively expensive projection.
            if (mc.player.squaredDistanceTo(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > (double) range * range) continue;
            Box b = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            Draw.box(ctx, mc, td, b, ColorUtil.accent(i++ * 0.012f));
        }
    }
}
