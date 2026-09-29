package com.example.client.modules;

import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.RotationAxis;

/** Replaces the first-person swing. Right-click to cycle: Slash / Spin / Poke. */
public class SwingAnimation extends Module {
    public SwingAnimation() { super("Swing Anim", Category.VISUALS, "Slash", "Spin", "Poke"); }

    public void apply(MatrixStack m, Arm arm, float p) {
        int side = arm == Arm.RIGHT ? 1 : -1;
        float sq = MathHelper.sin(MathHelper.sqrt(p) * (float) Math.PI);
        float sp = MathHelper.sin(p * p * (float) Math.PI);
        switch (mode) {
            case 0 -> {
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * (45f + sp * -20f)));
                m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * sq * -45f));
                m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sq * -100f));
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45f));
            }
            case 1 -> {
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * 45f));
                m.multiply(RotationAxis.POSITIVE_Z.rotationDegrees(side * p * 360f));
                m.multiply(RotationAxis.POSITIVE_Y.rotationDegrees(side * -45f));
            }
            default -> {
                m.translate(side * -0.05 * sq, 0.0, -0.6 * sq);
                m.multiply(RotationAxis.POSITIVE_X.rotationDegrees(sq * -25f));
            }
        }
    }
}
