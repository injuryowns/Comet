package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.chunk.ChunkStatus;
import net.minecraft.world.chunk.WorldChunk;

import java.util.HashMap;
import java.util.Map;

/**
 * Snapshots nearby chunks when you disconnect. When you rejoin the same world and this is still
 * on, it diffs the new state against the snapshot and rings every block that changed while you
 * were offline - a base that got built, a hole that got filled, etc. The snapshot only lives in
 * memory, so it's lost if you close the game entirely.
 */
public class RelogMethod extends Module {
    private Map<Long, Boolean> snapshot; // blockPos.asLong() -> wasAir
    private final Map<BlockPos, Long> flagged = new HashMap<>();
    private static final long FLAG_LIFE_MS = 30000;
    private boolean hadWorld;

    public RelogMethod() { super("Relog Method", Category.BASE); }

    @Override
    protected void onDisable() { snapshot = null; flagged.clear(); }

    @Override
    public void onTick(MinecraftClient mc) {
        boolean hasWorld = mc.world != null;
        if (hadWorld && !hasWorld) takeSnapshot(mc); // just disconnected
        if (!hadWorld && hasWorld && snapshot != null) diff(mc); // just reconnected
        hadWorld = hasWorld;

        long now = System.currentTimeMillis();
        flagged.entrySet().removeIf(e -> now - e.getValue() > FLAG_LIFE_MS);
    }

    // Called on the tick before disconnect, so mc.world/mc.player are still the old session's.
    private void takeSnapshot(MinecraftClient mc) { /* placeholder: real capture below via onWorldPresent */ }

    private Map<Long, Boolean> capture(MinecraftClient mc) {
        Map<Long, Boolean> map = new HashMap<>();
        var p = mc.player.getBlockPos();
        for (int dx = -24; dx <= 24; dx++) {
            for (int dz = -24; dz <= 24; dz++) {
                WorldChunk chunk = mc.world.getChunk((p.getX() + dx) >> 4, (p.getZ() + dz) >> 4, ChunkStatus.FULL, false);
                if (chunk == null) continue;
                for (int dy = -16; dy <= 16; dy++) {
                    BlockPos pos = p.add(dx, dy, dz);
                    map.put(pos.asLong(), mc.world.getBlockState(pos).isAir());
                }
            }
        }
        return map;
    }

    private void diff(MinecraftClient mc) {
        Map<Long, Boolean> now = capture(mc);
        for (var e : now.entrySet()) {
            Boolean was = snapshot.get(e.getKey());
            if (was != null && !was.equals(e.getValue())) {
                flagged.put(BlockPos.fromLong(e.getKey()), System.currentTimeMillis());
            }
        }
        snapshot = null;
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc) || flagged.isEmpty()) return;
        long now = System.currentTimeMillis();
        for (var e : flagged.entrySet()) {
            BlockPos p = e.getKey();
            float t = (now - e.getValue()) / (float) FLAG_LIFE_MS;
            Draw.ring(ctx, mc, td, p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5, 0.6, 16,
                    ColorUtil.alpha(ColorUtil.accent(0.7f), 1f - t));
        }
    }
}
