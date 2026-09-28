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
 * Outlines valuable ores/blocks through walls. Right-click: Ores / Obsidian / Spawners.
 * Scans a small chunk of the search area each tick (not the whole thing every frame) so it stays
 * cheap even at a large range, then just redraws the cached list every frame.
 */
public class BlockEsp extends Module {
    private static final List<Block> ORES = List.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.ANCIENT_DEBRIS,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);

    private int range = 24;
    private final List<BlockPos> found = new ArrayList<>();
    private int cursor; // resumes the sweep across ticks instead of redoing it all at once

    public BlockEsp() { super("Block ESP", Category.BASE, "Ores", "Obsidian", "Spawners"); }

    { addInt("Range", () -> range, v -> { range = v; found.clear(); cursor = 0; }, 8, 48); }

    private boolean matches(Block b) {
        return switch (mode) {
            case 0 -> ORES.contains(b);
            case 1 -> b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN;
            default -> b == Blocks.SPAWNER;
        };
    }

    @Override
    protected void onDisable() { found.clear(); cursor = 0; }

    @Override
    public void onTick(MinecraftClient mc) {
        BlockPos p = mc.player.getBlockPos();
        int span = range * 2 + 1, total = span * span;
        int budget = 400; // columns scanned this tick

        for (int n = 0; n < budget; n++) {
            if (cursor >= total) { cursor = 0; found.clear(); }
            int dx = cursor % span - range, dz = cursor / span - range;
            cursor++;
            for (int dy = -12; dy <= 12; dy++) {
                BlockPos pos = p.add(dx, dy, dz);
                if (matches(mc.world.getBlockState(pos).getBlock())) found.add(pos);
            }
            if (found.size() > 150) break; // don't let one huge deposit blow up the list
        }
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        int i = 0;
        for (BlockPos pos : found) {
            Box b = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            Draw.box(ctx, mc, td, b, ColorUtil.accent(i++ * 0.02f));
        }
    }
}
