package com.example.client.mixin;

import com.example.client.Client;
import com.example.client.modules.Hands;
import com.example.client.modules.SwingAnimation;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(HeldItemRenderer.class)
public class HeldItemRendererMixin {
    @Inject(method = "applySwingOffset", at = @At("HEAD"), cancellable = true, require = 0)
    private void comet$swing(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        SwingAnimation m = Client.MODULES.get(SwingAnimation.class);
        if (m != null && m.isEnabled()) {
            m.apply(matrices, arm, swingProgress);
            ci.cancel();
        }
    }

    @Inject(method = "applyEquipOffset", at = @At("TAIL"), require = 0)
    private void comet$hands(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        Hands m = Client.MODULES.get(Hands.class);
        if (m != null && m.isEnabled()) m.apply(matrices, arm);
    }
}
