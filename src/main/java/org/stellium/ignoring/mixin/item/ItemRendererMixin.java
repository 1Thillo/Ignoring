package org.stellium.ignoring.mixin.item;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.entity.ItemSubmitMarks;
import org.stellium.ignoring.render.TransparencyLayers;
import org.stellium.ignoring.util.ArgbUtils;


/*
 * This file is part of Transparent-Entities(https://github.com/LopyMine/Transparent-Entities)
 * Copyright (C) LopyMine(https://github.com/LopyMine)
 *
 * Modified by stellium1 in Ignoring(https://github.com/stellium1/Ignoring).
 * Licensed under the GNU Lesser General Public License v3.0
*/
@Mixin(ItemFeatureRenderer.class)
public class ItemRendererMixin {

    // Runs in the execute phase, so the entity mark is long gone. The submit
    // node carries the information instead, and prepareSubmit is the one place
    // that sees it before the drawing calls below.
    @Inject(method = "prepareSubmit", at = @At("HEAD"))
    private void ignoring$beginItemSubmit(ItemFeatureRenderer.Submit submit, boolean bl, CallbackInfo ci) {
        ItemSubmitMarks.beginSubmit(submit);
    }

    @Inject(method = "prepareSubmit", at = @At("RETURN"))
    private void ignoring$endItemSubmit(ItemFeatureRenderer.Submit submit, boolean bl, CallbackInfo ci) {
        ItemSubmitMarks.endSubmit();
    }

    @ModifyArg(
            method = "prepareMainSubmit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getVertexBuilder(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private RenderType modifyLayer(RenderType value) {
        if (ItemSubmitMarks.isCurrentMarked()) {
            return TransparencyLayers.getMarkedItemLayer(value);
        }

        return TransparencyLayers.getItemLayer(value);
    }

	@ModifyArg(
            method = "prepareMainSubmit",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;setColor(I)V")
    )
    private int generated(int originalColor) {
        if (!ItemSubmitMarks.isCurrentMarked()) {
            return originalColor;
        }

        return ArgbUtils.applyAlpha(originalColor, IgnoringConfig.get().transparency);
    }
}
