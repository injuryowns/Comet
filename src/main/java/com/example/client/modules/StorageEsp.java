package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Highlights loaded storage block entities. Uses ClientWorld#getBlockEntities instead of
 * brute-force probing every block in a cube, which is dramatically cheaper on large ranges.
 */
public class StorageEsp extends Module {
    private int range = 32;
    private final List<BlockPos> found = new ArrayList<>();
    private long nextRefresh;

    public StorageEsp() { super("Storage ESP", Category.BASE); }

    { addInt("Range", () -> range, v -> { range = v; found.clear(); nextRefresh = 0; }, 8, 64); }

    private boolean isStorage(BlockEntity be) {
        return be instanceof ChestBlockEntity
                || be instanceof BarrelBlockEntity
                || be instanceof ShulkerBoxBlockEntity
                || be instanceof HopperBlockEntity
                || be instanceof AbstractFurnaceBlockEntity
                || be instanceof EnderChestBlockEntity;
    }

    @Override
    protected void onDisable() { found.clear(); nextRefresh = 0; }

    @Override
    public void onTick(MinecraftClient mc) {
        if (mc.player == null || mc.world == null) return;
        long now = System.currentTimeMillis();
        if (now < nextRefresh) return;
        nextRefresh = now + 150;

        BlockPos center = mc.player.getBlockPos();
        double maxSq = (double) range * range;
        ArrayList<BlockPos> next = new ArrayList<>();

        // ClientWorld maintains this set for loaded block entities, so we don't scan
        // hundreds of thousands of empty block positions just to find a few chests.
        for (BlockEntity be : mc.world.getBlockEntities()) {
            if (!isStorage(be)) continue;
            BlockPos pos = be.getPos();
            double dx = pos.getX() + 0.5 - center.getX();
            double dy = pos.getY() + 0.5 - center.getY();
            double dz = pos.getZ() + 0.5 - center.getZ();
            if (dx * dx + dy * dy + dz * dz <= maxSq) next.add(pos.toImmutable());
        }

        next.sort(Comparator.comparingDouble(p -> p.getSquaredDistance(center)));
        if (next.size() > 200) next.subList(200, next.size()).clear();
        found.clear();
        found.addAll(next);
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        int i = 0;
        for (BlockPos pos : found) {
            Box b = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            Draw.box(ctx, mc, td, b, ColorUtil.accent(i++ * 0.018f));
        }
    }
}
