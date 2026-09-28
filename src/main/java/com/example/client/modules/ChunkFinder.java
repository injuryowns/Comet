package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.Chunk;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Looks for hidden entrances: spots that were solid ground a moment ago and are open now (a hole
 * just got uncovered), or air that just got a block placed over it (a hole just got covered). It
 * remembers each chunk's last scan and diffs against the new one, so it only catches CHANGES it
 * personally witnessed - it can't see a hole that was already covered before you arrived.
 */
public class ChunkFinder extends Module {
    private final Map<Long, Map<Long, Boolean>> lastAir = new HashMap<>(); // chunkKey -> (posKey -> wasAir)
    private final Map<BlockPos, Long> flagged = new HashMap<>();
    private static final long FLAG_LIFE_MS = 15000;
    private int cooldown;

    public ChunkFinder() { super("Chunk Finder", Category.BASE); }

    @Override
    protected void onDisable() { lastAir.clear(); flagged.clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.world == null || cooldown-- > 0) return;
        cooldown = 10;

        int pcx = mc.player.getChunkPos().x, pcz = mc.player.getChunkPos().z;
        for (int cx = pcx - 3; cx <= pcx + 3; cx++) {
            for (int cz = pcz - 3; cz <= pcz + 3; cz++) {
                scanChunk(mc, cx, cz);
            }
        }
        long now = System.currentTimeMillis();
        flagged.entrySet().removeIf(e -> now - e.getValue() > FLAG_LIFE_MS);
    }

    private void scanChunk(MinecraftClient mc, int cx, int cz) {
        Chunk chunk = mc.world.getChunk(cx, cz, ChunkStatus.FULL, false);
        if (chunk == null) return;
        long chunkKey = (((long) cx) << 32) | (cz & 0xFFFFFFFFL);
        Map<Long, Boolean> prev = lastAir.get(chunkKey);
        Map<Long, Boolean> cur = new HashMap<>();

        int baseX = chunk.getPos().getStartX(), baseZ = chunk.getPos().getStartZ();
        int minY = Math.max(chunk.getBottomY(), mc.player.getBlockY() - 20);
        int maxY = Math.min(chunk.getTopYInclusive(), mc.player.getBlockY() + 20);

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                for (int y = minY; y <= maxY; y++) {
                    BlockPos pos = new BlockPos(baseX + x, y, baseZ + z);
                    boolean air = mc.world.getBlockState(pos).isAir();
                    long key = pos.asLong();
                    cur.put(key, air);
                    if (prev != null) {
                        Boolean was = prev.get(key);
                        if (was != null && was != air) flagged.put(pos, System.currentTimeMillis());
                    }
                }
            }
        }
        lastAir.put(chunkKey, cur);
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc) || flagged.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (var e : flagged.entrySet()) {
            BlockPos p = e.getKey();
            float t = (now - e.getValue()) / (float) FLAG_LIFE_MS;
            Draw.ring(ctx, mc, td, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 0.6, 16,
                    ColorUtil.alpha(ColorUtil.accent(0.5f), 1f - t));
        }
    }
}
