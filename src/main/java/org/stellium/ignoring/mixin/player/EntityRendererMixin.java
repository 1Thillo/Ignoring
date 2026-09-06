package org.stellium.ignoring.mixin.player;

import net.minecraft.client.renderer.SubmitNodeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.entity.EntityCaptures;

import static org.stellium.ignoring.util.ArgbUtils.swapAlpha;

@Mixin(SubmitNodeCollection.class)
public class EntityRendererMixin {

    @ModifyArg(
        method = "submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/NameTagFeatureRenderer$Submit;<init>(Lorg/joml/Matrix4fc;FFLnet/minecraft/network/chat/Component;IIILnet/minecraft/client/gui/Font$DisplayMode;)V"
        ),
        index = 5
    )
    private int ignoring$adjustLabelColor(int color) {
        if (EntityCaptures.MAIN.getEntity() == null) {
            return color;
        }

        return swapAlpha(color, IgnoringConfig.get().transparency);
    }

    @ModifyArg(
        method = "submitNameTag(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/world/phys/Vec3;ILnet/minecraft/network/chat/Component;ZILnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/feature/NameTagFeatureRenderer$Submit;<init>(Lorg/joml/Matrix4fc;FFLnet/minecraft/network/chat/Component;IIILnet/minecraft/client/gui/Font$DisplayMode;)V"
        ),
        index = 6
    )
    private int ignoring$adjustLabelBackgroundColor(int backgroundColor) {
        if (EntityCaptures.MAIN.getEntity() == null) {
            return backgroundColor;
        }

        return swapAlpha(backgroundColor, IgnoringConfig.get().transparency);
    }

}
