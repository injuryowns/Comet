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

import java.util.List;

/** Outlines valuable ores/blocks through walls within render range. Right-click: Ores / Obsidian / Spawners. */
public class BlockEsp extends Module {
    private static final int RANGE = 24;
    private static final List<Block> ORES = List.of(
            Blocks.DIAMOND_ORE, Blocks.DEEPSLATE_DIAMOND_ORE, Blocks.ANCIENT_DEBRIS,
            Blocks.EMERALD_ORE, Blocks.DEEPSLATE_EMERALD_ORE, Blocks.GOLD_ORE, Blocks.DEEPSLATE_GOLD_ORE);

    public BlockEsp() { super("Block ESP", Category.BASE, "Ores", "Obsidian", "Spawners"); }

    private boolean matches(Block b) {
        return switch (mode) {
            case 0 -> ORES.contains(b);
            case 1 -> b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN;
            default -> b == Blocks.SPAWNER;
        };
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        BlockPos p = mc.player.getBlockPos();
        int found = 0;
        for (int dx = -RANGE; dx <= RANGE && found < 60; dx += 1) {
            for (int dy = -12; dy <= 12 && found < 60; dy += 1) {
                for (int dz = -RANGE; dz <= RANGE && found < 60; dz += 1) {
                    BlockPos pos = p.add(dx, dy, dz);
                    if (!matches(mc.world.getBlockState(pos).getBlock())) continue;
                    Box b = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    Draw.box(ctx, mc, td, b, ColorUtil.accent(found * 0.02f));
                    found++;
                }
            }
        }
    }
}
