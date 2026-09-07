package org.stellium.ignoring.mixin.fixes;

import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.renderer.extract.LevelExtractor;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.stellium.ignoring.entity.ItemSubmitMarks;

/**
 * The marks tagged onto item submit nodes are only good for the frame they were
 * made in. Dropping them where the next frame starts extracting keeps the set
 * from growing for the length of the session.
 */
@Mixin(LevelExtractor.class)
public class LevelExtractorMixin {

    @Inject(method = "extract", at = @At("HEAD"))
    private void ignoring$clearItemSubmitMarks(DeltaTracker deltaTracker, Camera camera, float tickDelta, CallbackInfo ci) {
        ItemSubmitMarks.clear();
    }
}
