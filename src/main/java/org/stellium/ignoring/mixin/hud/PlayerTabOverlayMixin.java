package org.stellium.ignoring.mixin.hud;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.stellium.ignoring.config.IgnoringConfig;

import java.util.ArrayList;
import java.util.List;

@Mixin(PlayerTabOverlay.class)
public class PlayerTabOverlayMixin {
    @Redirect(
            method = "extractRenderState",
        at = @At(
            value = "INVOKE",
                target = "Lnet/minecraft/client/gui/components/PlayerTabOverlay;getPlayerInfos()Ljava/util/List;"
        )
    )
    private List<PlayerInfo> redirectCollect(PlayerTabOverlay self) {
        List<PlayerInfo> original = ((PlayerListHudInvoker) self).invokeCollectPlayerEntries();
        IgnoringConfig config = IgnoringConfig.get();
        if (!config.ignoreTablist) {
            return original;
        }
        List<PlayerInfo> copy = new ArrayList<>(original);

        copy.removeIf(entry -> {
            String displayName = entry.getTabListDisplayName() != null
                ? entry.getTabListDisplayName().getString()
                : null;

            String profileName = entry.getProfile().name();

            return (displayName != null && config.isPlayerIgnored(displayName))
                || config.isPlayerIgnored(profileName);
        });

        return copy;
    }
}
