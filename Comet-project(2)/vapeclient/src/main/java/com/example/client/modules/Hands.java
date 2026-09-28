package com.example.client.modules;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;

/** First-person hand/viewmodel placement. Right-click to cycle: Low / Tiny / Far. */
public class Hands extends Module {
    public Hands() { super("Hands", Category.VISUALS, "Low", "Tiny", "Far"); }

    public void apply(MatrixStack m, Arm arm) {
        int side = arm == Arm.RIGHT ? 1 : -1;
        switch (mode) {
            case 0 -> m.translate(0.0, -0.15, 0.0);
            case 1 -> { m.translate(side * 0.05, -0.05, -0.1); m.scale(0.7f, 0.7f, 0.7f); }
            default -> m.translate(side * 0.08, 0.0, -0.3);
        }
    }
}
