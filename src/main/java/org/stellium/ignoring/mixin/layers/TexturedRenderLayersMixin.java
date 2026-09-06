package org.stellium.ignoring.mixin.layers;

import net.minecraft.client.renderer.Sheets;
import net.minecraft.client.renderer.rendertype.RenderType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.stellium.ignoring.render.TransparencyLayers;
/*
 * This file is part of Transparent-Entities(https://github.com/LopyMine/Transparent-Entities)
 * Copyright (C) LopyMine(https://github.com/LopyMine)
 *
 * Modified by stellium1 in Ignoring(https://github.com/stellium1/Ignoring).
 * Licensed under the GNU Lesser General Public License v3.0
*/
@Mixin(Sheets.class)
public class TexturedRenderLayersMixin {

    @Inject(at = @At("RETURN"), method = {
            "cutoutBlockItemSheet",
            "cutoutItemSheet"
    }, cancellable = true)
    private static void swapRenderLayer(CallbackInfoReturnable<RenderType> cir) {
        cir.setReturnValue(TransparencyLayers.getItemLayer(cir::getReturnValue));
    }

}
