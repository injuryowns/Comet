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
import java.util.List;

/** Outlines chests, barrels, shulkers, hoppers and furnaces near you. Sweeps in small pieces across ticks, not the whole area every frame. */
public class StorageEsp extends Module {
    private int range = 24;
    private final List<BlockPos> found = new ArrayList<>();
    private int cursor;

    public StorageEsp() { super("Storage ESP", Category.BASE); }

    { addInt("Range", () -> range, v -> { range = v; found.clear(); cursor = 0; }, 8, 48); }

    private boolean isStorage(BlockEntity be) {
        return be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof ShulkerBoxBlockEntity
                || be instanceof HopperBlockEntity || be instanceof AbstractFurnaceBlockEntity || be instanceof EnderChestBlockEntity;
    }

    @Override
    protected void onDisable() { found.clear(); cursor = 0; }

    @Override
    public void onTick(MinecraftClient mc) {
        BlockPos p = mc.player.getBlockPos();
        int span = range * 2 + 1, total = span * span;
        int budget = 400;

        for (int n = 0; n < budget; n++) {
            if (cursor >= total) { cursor = 0; found.clear(); }
            int dx = cursor % span - range, dz = cursor / span - range;
            cursor++;
            for (int dy = -12; dy <= 12; dy++) {
                BlockPos pos = p.add(dx, dy, dz);
                var be = mc.world.getBlockEntity(pos);
                if (be != null && isStorage(be)) found.add(pos);
            }
            if (found.size() > 150) break;
        }
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        int i = 0;
        for (BlockPos pos : found) {
            Box b = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
            Draw.box(ctx, mc, td, b, ColorUtil.accent(i++ * 0.03f));
        }
    }
}
