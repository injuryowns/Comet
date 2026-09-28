package com.example.client.util;

import com.example.client.Client;
import com.example.client.Settings;
import com.example.client.modules.Gradient;

import java.awt.Color;

public final class ColorUtil {
    private ColorUtil() {}

    /** The UI accent; animated when the Gradient module is on. offset staggers colors across elements. */
    public static int accent(float offset) {
        int base = Settings.accent();
        Gradient g = Client.MODULES.get(Gradient.class);
        if (g == null || !g.isEnabled()) return base;

        double time = System.currentTimeMillis() / 1000.0;
        if (g.mode == 1) { // rainbow
            return Color.HSBtoRGB((float) ((time * 0.15 + offset) % 1.0), 0.75f, 1f) | 0xFF000000;
        }
        float[] hsb = Color.RGBtoHSB((base >> 16) & 255, (base >> 8) & 255, base & 255, null);
        int other = Color.HSBtoRGB((hsb[0] + 0.18f) % 1f, hsb[1], hsb[2]);
        float t = (float) (Math.sin(time * 1.6 + offset * 6) * 0.5 + 0.5);
        return lerp(base, other, t) | 0xFF000000;
    }

    public static int lerp(int a, int b, float t) {
        int r = (int) (((a >> 16) & 255) * (1 - t) + ((b >> 16) & 255) * t);
        int g = (int) (((a >> 8) & 255) * (1 - t) + ((b >> 8) & 255) * t);
        int bl = (int) ((a & 255) * (1 - t) + (b & 255) * t);
        return (r << 16) | (g << 8) | bl;
    }

    public static int alpha(int argb, float a) {
        int al = (int) (Math.max(0f, Math.min(1f, a)) * 255);
        return (al << 24) | (argb & 0xFFFFFF);
    }
}
