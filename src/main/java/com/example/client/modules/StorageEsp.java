package com.example.client.modules;

import com.example.client.util.ColorUtil;
import com.example.client.util.Draw;
import com.example.client.util.Projector;
import net.minecraft.block.entity.*;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;

/** Outlines chests, barrels, shulkers, hoppers and furnaces through walls near you. */
public class StorageEsp extends Module {
    private static final int RANGE = 24;

    public StorageEsp() { super("Storage ESP", Category.BASE); }

    private boolean isStorage(BlockEntity be) {
        return be instanceof ChestBlockEntity || be instanceof BarrelBlockEntity || be instanceof ShulkerBoxBlockEntity
                || be instanceof HopperBlockEntity || be instanceof AbstractFurnaceBlockEntity || be instanceof EnderChestBlockEntity;
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (!Projector.usable(mc)) return;
        BlockPos p = mc.player.getBlockPos();
        int found = 0;
        for (int dx = -RANGE; dx <= RANGE && found < 60; dx++) {
            for (int dy = -12; dy <= 12 && found < 60; dy++) {
                for (int dz = -RANGE; dz <= RANGE && found < 60; dz++) {
                    BlockPos pos = p.add(dx, dy, dz);
                    var be = mc.world.getBlockEntity(pos);
                    if (be == null || !isStorage(be)) continue;
                    Box b = new Box(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1);
                    Draw.box(ctx, mc, td, b, ColorUtil.accent(found * 0.03f));
                    found++;
                }
            }
        }
    }
}
