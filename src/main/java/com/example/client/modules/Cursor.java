package com.example.client.modules;

import com.example.client.Client;
import com.example.client.util.ColorUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;

/**
 * Custom crosshair (gap widens while your attack recharges) and a custom mouse pointer in the
 * Comet GUI. The vanilla crosshair is hidden by a mixin while this is on.
 */
public class Cursor extends Module {
    public Cursor() { super("Cursor", Category.VISUALS); }

    public static boolean active() {
        Cursor m = Client.MODULES.get(Cursor.class);
        return m != null && m.isEnabled();
    }

    @Override
    public void onRender2D(DrawContext ctx, MinecraftClient mc, float td) {
        if (mc.currentScreen != null || !mc.options.getPerspective().isFirstPerson()) return;
        int cx = ctx.getScaledWindowWidth() / 2, cy = ctx.getScaledWindowHeight() / 2;
        int gap = 3 + Math.round((1f - mc.player.getAttackCooldownProgress(0f)) * 5f);
        int len = 4, col = ColorUtil.accent(0);
        ctx.fill(cx - gap - len, cy, cx - gap, cy + 1, col);
        ctx.fill(cx + gap + 1, cy, cx + gap + 1 + len, cy + 1, col);
        ctx.fill(cx, cy - gap - len, cx + 1, cy - gap, col);
        ctx.fill(cx, cy + gap + 1, cx + 1, cy + gap + 1 + len, col);
        ctx.fill(cx, cy, cx + 1, cy + 1, 0xFFFFFFFF);
    }

    public static void drawGuiCursor(DrawContext ctx, int mx, int my) {
        int col = ColorUtil.accent(0);
        for (int i = 0; i < 9; i++) {
            int w = 1 + Math.min(i, 8 - i);
            ctx.fill(mx + 1, my + i + 1, mx + w + 1, my + i + 2, 0xFF000000);
        }
        for (int i = 0; i < 9; i++) {
            int w = 1 + Math.min(i, 8 - i);
            ctx.fill(mx, my + i, mx + w, my + i + 1, col);
        }
    }
}
