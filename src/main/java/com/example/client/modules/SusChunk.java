package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.block.BlockState;
import net.minecraft.block.CropBlock;
import net.minecraft.block.StemBlock;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.state.property.Properties;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkSectionPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.Chunk;

import java.util.HashSet;
import java.util.Set;

/**
 * Scans loaded chunks for a cluster of fully-grown crops (wheat/carrot/potato/beetroot/nether
 * wart/stem age at max) and outlines chunks that have several, since real farms tend to hold many
 * at once. This is a heuristic, not a guarantee - it will also flag a player's active farm.
 */
public class SusChunk extends Module {
    private static final int RADIUS = 4;      // chunks around the player to scan
    private static final int THRESHOLD = 24;  // grown crops in one chunk to flag it

    private final Set<Long> flagged = new HashSet<>();
    private int cooldown;

    public SusChunk() { super("Sus Chunk", Category.BASE); }

    @Override
    protected void onDisable() { flagged.clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || cooldown-- > 0) return;
        cooldown = 20; // rescan every second

        flagged.clear();
        int pcx = mc.player.getChunkPos().x, pcz = mc.player.getChunkPos().z;
        for (int cx = pcx - RADIUS; cx <= pcx + RADIUS; cx++) {
            for (int cz = pcz - RADIUS; cz <= pcz + RADIUS; cz++) {
                Chunk chunk = mc.world.getChunk(cx, cz, net.minecraft.world.chunk.ChunkStatus.FULL, false);
                if (chunk == null) continue;
                if (countGrown(chunk) >= THRESHOLD) flagged.add(ChunkSectionPos.asLong(cx, 0, cz));
            }
        }
    }

    private int countGrown(Chunk chunk) {
        int count = 0;
        int minY = chunk.getBottomY(), maxY = chunk.getTopYInclusive();
        for (int x = 0; x < 16 && count < THRESHOLD; x++) {
            for (int z = 0; z < 16 && count < THRESHOLD; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockState s = chunk.getBlockState(new BlockPos(chunk.getPos().getStartX() + x, y, chunk.getPos().getStartZ() + z));
                    if (grown(s)) count++;
                }
            }
        }
        return count;
    }

    private boolean grown(BlockState s) {
        if (s.getBlock() instanceof CropBlock c) return c.isMature(s);
        if (s.getBlock() instanceof StemBlock && s.contains(Properties.AGE_7)) return s.get(Properties.AGE_7) == 7;
        if (s.isOf(net.minecraft.block.Blocks.NETHER_WART) && s.contains(Properties.AGE_3)) return s.get(Properties.AGE_3) == 3;
        return false;
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc) || flagged.isEmpty()) return;
        for (long packed : flagged) {
            int cx = ChunkSectionPos.unpackX(packed), cz = ChunkSectionPos.unpackZ(packed);
            double bx = cx * 16, bz = cz * 16, y = mc.player.getY();
            Vec3d[] corners = {
                    new Vec3d(bx, y, bz), new Vec3d(bx + 16, y, bz),
                    new Vec3d(bx + 16, y, bz + 16), new Vec3d(bx, y, bz + 16)
            };
            for (int i = 0; i < 4; i++) {
                double[] a = Projector.project(mc, corners[i], td);
                double[] b = Projector.project(mc, corners[(i + 1) % 4], td);
                Draw.line(ctx, a, b, ColorUtil.alpha(ColorUtil.accent(0.25f), 0.8f));
            }
        }
    }
}
