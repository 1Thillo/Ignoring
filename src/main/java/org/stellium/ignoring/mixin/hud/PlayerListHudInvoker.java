package org.stellium.ignoring.mixin.hud;

import net.minecraft.client.gui.components.PlayerTabOverlay;
import net.minecraft.client.multiplayer.PlayerInfo;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.List;

@Mixin(PlayerTabOverlay.class)
public interface PlayerListHudInvoker {
    @Invoker("getPlayerInfos")
    List<PlayerInfo> invokeCollectPlayerEntries();
}
