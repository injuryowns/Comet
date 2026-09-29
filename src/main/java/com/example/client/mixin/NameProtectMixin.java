package com.example.client.mixin;

import com.example.client.Settings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.text.TextVisitFactory;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyVariable;

/**
 * Display-only name replacement: swaps your real username for a custom one in any text
 * YOUR client draws (chat, tab list, nametags). The server still sees your real account.
 */
@Mixin(TextVisitFactory.class)
public class NameProtectMixin {
    @ModifyVariable(
            method = "visitFormatted(Ljava/lang/String;ILnet/minecraft/text/Style;Lnet/minecraft/text/Style;Lnet/minecraft/text/CharacterVisitor;)Z",
            at = @At("HEAD"), argsOnly = true)
    private static String client$protectName(String text) {
        if (!Settings.nameProtect || text == null || Settings.fakeName.isEmpty()) return text;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getSession() == null) return text;
        String real = mc.getSession().getUsername();
        return (real == null || real.isEmpty() || !text.contains(real)) ? text : text.replace(real, Settings.fakeName);
    }
}
