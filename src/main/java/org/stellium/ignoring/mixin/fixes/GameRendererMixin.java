package org.stellium.ignoring.mixin.fixes;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.stellium.ignoring.entity.EntityCaptures;

/*
 * This file is part of Transparent-Entities(https://github.com/LopyMine/Transparent-Entities)
 * Copyright (C) LopyMine(https://github.com/LopyMine)
 *
 * Modified by stellium1 in Ignoring(https://github.com/stellium1/Ignoring).
 * Licensed under the GNU Lesser General Public License v3.0
*/
@Mixin(Gui.class)
public class GameRendererMixin {

    @WrapOperation(at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/screens/Screen;extractRenderStateWithTooltipAndSubtitles(Lnet/minecraft/client/gui/GuiGraphicsExtractor;IIF)V"), method = "extractRenderState")
    private void generated(Screen instance, GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, Operation<Void> original) {
        EntityCaptures.MAIN.setEnabled(false);
        try {
            original.call(instance, context, mouseX, mouseY, delta);
        } finally {
            EntityCaptures.MAIN.setEnabled(true);
        }
    }

}
