package com.example.client.mixin;

import com.example.client.modules.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * Overrides where the camera sits, right after vanilla finishes positioning it for the tick.
 * Uses reflection to find wherever Camera keeps its position, instead of calling a guessed method
 * name directly, so a wrong guess about this version's exact API logs a warning once instead of
 * failing the whole build or crashing the game.
 */
@Mixin(Camera.class)
public class CameraMixin {
    private static volatile boolean warned;
    private static Field posField;
    private static Method setPosVecMethod;
    private static boolean resolved;

    private static void resolve(Camera cam) {
        resolved = true;
        for (Method m : Camera.class.getDeclaredMethods()) {
            if (m.getParameterCount() == 1 && m.getParameterTypes()[0] == Vec3d.class) {
                m.setAccessible(true);
                setPosVecMethod = m;
                return;
            }
        }
        for (Field f : Camera.class.getDeclaredFields()) {
            if (f.getType() == Vec3d.class) {
                f.setAccessible(true);
                posField = f;
                return;
            }
        }
    }

    @Inject(method = "update", at = @At("TAIL"), require = 0)
    private void comet$freecam(CallbackInfo ci) {
        Vec3d target = Freecam.camPos;
        if (target == null) return;
        Camera self = (Camera) (Object) this;
        try {
            if (!resolved) resolve(self);
            if (setPosVecMethod != null) setPosVecMethod.invoke(self, target);
            else if (posField != null) posField.set(self, target);
            else if (!warned) { warned = true; System.err.println("[Comet] Freecam: couldn't find Camera's position field/method on this version - Freecam will move your body only, not the view."); }
        } catch (Exception e) {
            if (!warned) { warned = true; System.err.println("[Comet] Freecam: failed to set camera position: " + e); }
        }
    }
}
