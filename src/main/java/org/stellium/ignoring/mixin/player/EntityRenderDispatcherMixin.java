package org.stellium.ignoring.mixin.player;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.state.EntityRenderState;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.world.entity.Entity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import org.stellium.ignoring.config.IgnoringConfig;
import org.stellium.ignoring.render.TransparencyRenderer;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;

/*
 * This file is part of Transparent-Entities(https://github.com/LopyMine/Transparent-Entities)
 * Copyright (C) LopyMine(https://github.com/LopyMine)
 *
 * Modified by stellium1 in Ignoring(https://github.com/stellium1/Ignoring).
 * Licensed under the GNU Lesser General Public License v3.0
*/
@Mixin(EntityRenderDispatcher.class)
public class EntityRenderDispatcherMixin {

    @Unique
    private static final ThreadLocal<Map<EntityRenderState, Entity>> IGNORING$STATE_ENTITY =
        ThreadLocal.withInitial(IdentityHashMap::new);

    @Inject(method = "extractEntity(Lnet/minecraft/world/entity/Entity;F)Lnet/minecraft/client/renderer/entity/state/EntityRenderState;", at = @At("RETURN"))
    private <E extends Entity> void ignoring$captureEntity(E entity, float tickDelta, CallbackInfoReturnable<EntityRenderState> cir) {
        EntityRenderState state = cir.getReturnValue();
        if (state != null) {
            IGNORING$STATE_ENTITY.get().put(state, entity);
        }
    }

    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void ignoring$hideIgnoredNameplates(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (IgnoringConfig.get().shouldIgnoreNameplate(entity)) {
            cir.setReturnValue(false);
        }
    }

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/entity/EntityRenderer;submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;Lnet/minecraft/client/renderer/state/level/CameraRenderState;)V"
        ),
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
    )
    private void handleEntityRendering(
        EntityRenderer<?, EntityRenderState> instance,
        EntityRenderState state,
        PoseStack matrices,
        SubmitNodeCollector commandQueue,
        CameraRenderState cameraState,
        Operation<Void> original
    ) {
        Entity entity = IGNORING$STATE_ENTITY.get().get(state);
        if (entity == null) {
            original.call(instance, state, matrices, commandQueue, cameraState);
            return;
        }
        TransparencyRenderer.handleEntityRendering(entity, () -> original.call(instance, state, matrices, commandQueue, cameraState));
    }

    @WrapOperation(
        at = @At(
            value = "INVOKE",
            target = "Lnet/minecraft/client/renderer/SubmitNodeCollector;submitShadow(Lcom/mojang/blaze3d/vertex/PoseStack;FLjava/util/List;)V"
        ),
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V"
    )
    private void handleShadowRendering(
        SubmitNodeCollector instance,
        PoseStack matrices,
        float shadowRadius,
        List<EntityRenderState.ShadowPiece> shadowPieces,
        Operation<Void> original,
        @Local(argsOnly = true) EntityRenderState state
    ) {
        Entity entity = IGNORING$STATE_ENTITY.get().get(state);
        if (entity == null || !org.stellium.ignoring.render.TransparencyManager.canRenderTransparencyShadow(entity)) {
            original.call(instance, matrices, shadowRadius, shadowPieces);
            return;
        }

        float alpha = IgnoringConfig.get().transparency / 255F;
        List<EntityRenderState.ShadowPiece> adjusted = new ArrayList<>(shadowPieces.size());
        for (EntityRenderState.ShadowPiece piece : shadowPieces) {
            adjusted.add(new EntityRenderState.ShadowPiece(
                piece.relativeX(),
                piece.relativeY(),
                piece.relativeZ(),
                piece.shapeBelow(),
                alpha
            ));
        }
        original.call(instance, matrices, shadowRadius, adjusted);
    }

    @Inject(
        method = "submit(Lnet/minecraft/client/renderer/entity/state/EntityRenderState;Lnet/minecraft/client/renderer/state/level/CameraRenderState;DDDLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/SubmitNodeCollector;)V",
        at = @At("TAIL")
    )
    private void ignoring$clearCapturedEntity(
        EntityRenderState state,
        CameraRenderState cameraState,
        double x,
        double y,
        double z,
        PoseStack matrices,
        SubmitNodeCollector commandQueue,
        CallbackInfo ci
    ) {
        IGNORING$STATE_ENTITY.get().remove(state);
    }

}
