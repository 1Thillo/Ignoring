package org.stellium.ignoring.mixin.item;

import net.minecraft.client.renderer.feature.ItemFeatureRenderer;
import net.minecraft.client.renderer.rendertype.RenderType;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.entity.EntityCaptures;
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

    @ModifyArg(
            method = "prepareMainSubmit",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/feature/ItemFeatureRenderer;getVertexBuilder(Lnet/minecraft/client/renderer/rendertype/RenderType;)Lcom/mojang/blaze3d/vertex/VertexConsumer;")
    )
    private RenderType modifyLayer(RenderType value) {
        return TransparencyLayers.getItemLayer(value);
    }

	@ModifyArg(
            method = "prepareMainSubmit",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/QuadInstance;setColor(I)V")
    )
    private int generated(int originalColor) {
        Entity entity = EntityCaptures.MAIN.getEntity();
        if (entity == null) {
            return originalColor;
        }
        return ArgbUtils.applyAlpha(originalColor, IgnoringConfig.get().transparency);
    }
}
