package org.stellium.ignoring.mixin;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.world.entity.Entity;
import org.stellium.ignoring.config.IgnoringConfig;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityRenderer.class)
public abstract class PlayerEntityRendererMixin{
    @Inject(method = "shouldRender", at = @At("HEAD"), cancellable = true)
    private void onShouldRender(Entity entity, Frustum frustum, double x, double y, double z, CallbackInfoReturnable<Boolean> cir) {
        if (entity instanceof AbstractClientPlayer playerEntity) {
            IgnoringConfig config = IgnoringConfig.get();
            if (config.ignoreRender && config.shouldIgnorePlayer(playerEntity)) {
                cir.setReturnValue(false);
            }
        }
    }
}
