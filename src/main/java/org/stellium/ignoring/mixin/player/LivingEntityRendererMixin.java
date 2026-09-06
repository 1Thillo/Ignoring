package org.stellium.ignoring.mixin.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.Model;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.entity.EntityCaptures;
import org.stellium.ignoring.util.ArgbUtils;

/*
 * This file is part of Transparent-Entities(https://github.com/LopyMine/Transparent-Entities)
 * Copyright (C) LopyMine(https://github.com/LopyMine)
 *
 * Modified by stellium1 in Ignoring(https://github.com/stellium1/Ignoring).
 * Licensed under the GNU Lesser General Public License v3.0
*/
@Mixin(LivingEntityRenderer.class)
public class LivingEntityRendererMixin {

    @WrapOperation(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitModel(Lnet/minecraft/client/model/Model;Ljava/lang/Object;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/rendertype/RenderType;IIILnet/minecraft/client/renderer/texture/TextureAtlasSprite;ILnet/minecraft/client/renderer/feature/ModelFeatureRenderer$CrumblingOverlay;)V"
        )
    )
    private void wrapRender(
        SubmitNodeCollector instance,
        Model<?> model,
        Object renderState,
        PoseStack matrixStack,
        RenderType renderLayer,
        int light,
        int overlay,
        int color,
        TextureAtlasSprite sprite,
        int outlineColor,
        ModelFeatureRenderer.CrumblingOverlay crumblingOverlayCommand,
        Operation<Void> original
    ) {
        Entity entity = EntityCaptures.MAIN.getEntity();
        if (entity != null) {
            IgnoringConfig config = IgnoringConfig.get();
            if (config.ignoreRender && config.shouldIgnorePlayer(entity)) {
                if (config.transparency <= 0) {
                    return;
                }
                color = ArgbUtils.swapAlpha(color, config.transparency);
            }
        }
        original.call(instance, model, renderState, matrixStack, renderLayer, light, overlay, color, sprite, outlineColor, crumblingOverlayCommand);
    }

    @WrapOperation(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/LivingEntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V",
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/layers/RenderLayer;submit(Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;ILnet/minecraft/client/renderer/entity/state/EntityRenderState;FF)V"
        )
    )
    private void wrapFeatureRender(
        RenderLayer<?, ?> instance,
        PoseStack matrixStack,
        SubmitNodeCollector provider,
        int i,
        EntityRenderState entityRenderState,
        float a,
        float b,
        Operation<Void> original
    ) {
        Entity entity = EntityCaptures.MAIN.getEntity();
        if (entity != null) {
            IgnoringConfig config = IgnoringConfig.get();
            if (config.ignoreRender && config.shouldIgnorePlayer(entity) && config.transparency <= 0) {
                return;
            }
        }
        original.call(instance, matrixStack, provider, i, entityRenderState, a, b);
    }
}
