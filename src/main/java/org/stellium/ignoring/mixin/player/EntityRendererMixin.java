package org.stellium.ignoring.mixin.player;

import net.minecraft.client.renderer.SubmitNodeCollection;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.entity.EntityCaptures;

import static org.stellium.ignoring.util.ArgbUtils.swapAlpha;

@Mixin(SubmitNodeCollection.class)
public class EntityRendererMixin {

    // Armour, elytra, capes and every other feature layer reach the collector
    // through EquipmentLayerRenderer and friends rather than through
    // LivingEntityRenderer.submit, so wrapping the call site there misses them.
    // ModelPart.compile used to catch the rest, but in 26.2 that runs in the
    // execute phase, long after the entity mark has been cleared. Taking the
    // colour here covers everything submitted while a player is marked.
    @ModifyVariable(
        method = "submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V",
        at = @At("HEAD"),
        argsOnly = true,
        index = 7
    )
    private int ignoring$adjustModelColor(int color) {
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
